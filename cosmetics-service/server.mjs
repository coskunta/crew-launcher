import http from 'node:http';
import {gzip as gzipCallback, gunzip as gunzipCallback} from 'node:zlib';
import {promisify} from 'node:util';
const gzip=promisify(gzipCallback),gunzip=promisify(gunzipCallback);
import {randomBytes, createHash} from 'node:crypto';
import {mkdir, readFile, writeFile, rename, unlink} from 'node:fs/promises';
import {resolve, join} from 'node:path';
import {pathToFileURL} from 'node:url';

const MAX_CAPE = 8 + 2048 * 1024 * 4; // Width, height, then uncompressed ARGB. No image decoder on the service.
const uuid = s => typeof s === 'string' && /^[0-9a-f]{32}$/.test(s);
const hash = b => createHash('sha256').update(b).digest('hex');
const fail = (status, code) => Object.assign(new Error(code), {status});
const integer = (n, lo, hi) => Number.isInteger(n) && n >= lo && n <= hi;
export function validStyle(s) {return s && integer(s.cape,0,8) && integer(s.wings,0,9) && integer(s.color,0,4) && integer(s.emote,0,12);}
export function validCape(b) {
  if (b.length<8) return false;
  const w=b.readUInt32BE(0),h=b.readUInt32BE(4);
  return [64,128,256,512,1024,2048].includes(w) && h===w/2 && b.length===8+w*h*4;
}
async function body(req, limit) {
  const chunks=[];let size=0;
  for await (const b of req) {size+=b.length;if(size>limit)throw fail(413,'body_too_large');chunks.push(b);}
  return Buffer.concat(chunks);
}
async function json(req) {try {return JSON.parse((await body(req,4096)).toString());}catch(e){if(e.status)throw e;throw fail(400,'invalid_json');}}
async function mojangVerify({name,serverId}) {
  const url=new URL('https://sessionserver.mojang.com/session/minecraft/hasJoined');
  url.searchParams.set('username',name);url.searchParams.set('serverId',serverId);
  const response=await fetch(url,{signal:AbortSignal.timeout(8000),redirect:'error'});
  if(response.status!==200)throw fail(401,'minecraft_session_not_verified');
  return response.json();
}
// A verifier can be injected by tests; the executable always uses Mojang. No authentication bypass setting.
export async function createService({directory='./data', verify=mojangVerify, now=Date.now, maxProfiles=512}={}) {
  directory=resolve(directory);await mkdir(join(directory,'capes'),{recursive:true});
  const profiles=new Map(), challenges=new Map(), tokens=new Map(), rates=new Map();
  let saveQueue=Promise.resolve(), saveTimer;
  try {for(const p of JSON.parse(await readFile(join(directory,'profiles.json'),'utf8')))if(uuid(p.id)&&validStyle(p.style)&&(!p.capeHash||/^[a-f0-9]{64}$/.test(p.capeHash))&&profiles.size<maxProfiles)profiles.set(p.id,{...p,seen:0});}catch(e){if(e.code!=='ENOENT')throw e;}
  const persist=()=>{const data=JSON.stringify([...profiles.values()]);saveQueue=saveQueue.then(async()=>{await writeFile(join(directory,'profiles.tmp'),data,{mode:0o600});await rename(join(directory,'profiles.tmp'),join(directory,'profiles.json'));}).catch(e=>console.error('Cannot persist cosmetics:',e.code));return saveQueue;};
  const scheduleSave=()=>{if(!saveTimer)saveTimer=setTimeout(()=>{saveTimer=null;persist();},1000).unref();};
  function rate(key, max, duration=60000){const t=now();let r=rates.get(key);if(!r||r.until<t){if(rates.size>=20000&&!r)throw fail(429,'rate_capacity');r={count:0,until:t+duration};rates.set(key,r);}if(++r.count>max)throw fail(429,'rate_limited');}
  function authenticate(req){const value=req.headers.authorization;const token=typeof value==='string'&&value.startsWith('Bearer ')?value.slice(7):'';const session=tokens.get(hash(token));if(!session||session.until<now())throw fail(401,'session_expired');return session.id;}
  const timer=setInterval(()=>{const t=now();for(const map of [challenges,tokens,rates])for(const [k,v] of map)if(v.until<t)map.delete(k);},30000).unref();
  const server=http.createServer(async(req,res)=>{
    const reply=(status,data,type='application/json')=>{res.writeHead(status,{'content-type':type,'cache-control':'no-store','x-content-type-options':'nosniff'});res.end(type==='application/json'?JSON.stringify(data):data);};
    try {
      const url=new URL(req.url,'http://localhost');
      // Use the actual peer address, never client-supplied Forwarded/X-Forwarded-For.
      rate('ip:'+req.socket.remoteAddress,1800);
      if(req.method==='GET'&&url.pathname==='/health')return reply(200,{ok:true,protocol:1});
      if(req.method==='POST'&&url.pathname==='/v1/challenge'){
        rate('challenge:'+req.socket.remoteAddress,60);if(challenges.size>=10000)throw fail(503,'busy');
        const {id,name}=await json(req);if(!uuid(id)||typeof name!=='string'||!/^\w{1,16}$/.test(name))throw fail(400,'invalid_identity');
        const challenge=randomBytes(24).toString('hex'), serverId=randomBytes(20).toString('hex');
        challenges.set(challenge,{id,name,serverId,until:now()+60000});return reply(200,{challenge,serverId});
      }
      if(req.method==='POST'&&url.pathname==='/v1/session'){
        rate('verify:'+req.socket.remoteAddress,60);const {challenge}=await json(req), c=challenges.get(challenge);challenges.delete(challenge);
        if(!c||c.until<now())throw fail(401,'challenge_expired');
        const verified=await verify(c);if(!verified||verified.id?.replaceAll('-','').toLowerCase()!==c.id)throw fail(401,'identity_mismatch');
        if(tokens.size>=20000)throw fail(503,'busy');
        // Revoke this player's previous service token, not their Minecraft session.
        for(const [k,v]of tokens)if(v.id===c.id)tokens.delete(k);
        const token=randomBytes(32).toString('hex');tokens.set(hash(token),{id:c.id,until:now()+12*3600000});
        return reply(200,{token,expiresIn:43200});
      }
      if(req.method==='GET'&&url.pathname==='/v1/profiles'){
        const ids=(url.searchParams.get('ids')||'').split(',');if(ids.length>64||!ids.every(uuid))throw fail(400,'invalid_ids');
        const t=now(),result=[];for(const id of new Set(ids)){const p=profiles.get(id);if(p&&p.seen>t-90000)result.push({id,style:p.style,capeHash:p.style.cape===5?p.capeHash:null,elapsed:p.style.emote?Math.min(86400000,Math.max(0,t-p.emoteAt)):0,wingScale:p.wingScale,wingSpeed:p.wingSpeed});}
        return reply(200,{profiles:result});
      }
      if(req.method==='PUT'&&url.pathname==='/v1/profile'){
        const id=authenticate(req);rate('profile:'+id,300);const data=await json(req);
        if(!validStyle(data.style)||!integer(data.elapsed,0,86400000)||!Number.isFinite(data.wingScale)||data.wingScale<.5||data.wingScale>1.6||!Number.isFinite(data.wingSpeed)||data.wingSpeed<.3||data.wingSpeed>2)throw fail(400,'invalid_profile');
        if(!profiles.has(id)&&profiles.size>=maxProfiles)throw fail(507,'profile_capacity');
        const old=profiles.get(id)||{};profiles.set(id,{id,style:data.style,capeHash:old.capeHash||null,emoteAt:now()-data.elapsed,seen:now(),wingScale:data.wingScale,wingSpeed:data.wingSpeed});scheduleSave();return reply(200,{ok:true});
      }
      if(req.method==='PUT'&&url.pathname==='/v1/cape'){
        const id=authenticate(req);rate('upload:'+id,12);let b=await body(req,MAX_CAPE);if(req.headers['content-encoding']==='gzip'){try{b=await gunzip(b,{maxOutputLength:MAX_CAPE});}catch{throw fail(400,'invalid_compressed_cape');}}else if(req.headers['content-encoding'])throw fail(415,'unsupported_encoding');if(!validCape(b))throw fail(400,'invalid_cape');
        const p=profiles.get(id);if(!p)throw fail(409,'publish_profile_first');
        const digest=hash(b),path=join(directory,'capes',digest+'.argb');await writeFile(path,b,{mode:0o600});const old=p.capeHash;p.capeHash=digest;scheduleSave();
        if(old&&old!==digest&&![...profiles.values()].some(x=>x.capeHash===old))await unlink(join(directory,'capes',old+'.argb')).catch(()=>{});
        return reply(200,{hash:digest});
      }
      if(req.method==='DELETE'&&url.pathname==='/v1/profile'){
        const id=authenticate(req), p=profiles.get(id);profiles.delete(id);scheduleSave();
        if(p?.capeHash&&![...profiles.values()].some(x=>x.capeHash===p.capeHash))await unlink(join(directory,'capes',p.capeHash+'.argb')).catch(()=>{});
        return reply(200,{ok:true});
      }
      if(req.method==='DELETE'&&url.pathname==='/v1/cape'){
        const id=authenticate(req),p=profiles.get(id),old=p?.capeHash;if(p){p.capeHash=null;scheduleSave();}
        if(old&&![...profiles.values()].some(x=>x.capeHash===old))await unlink(join(directory,'capes',old+'.argb')).catch(()=>{});return reply(200,{ok:true});
      }
      if(req.method==='GET'&&/^\/v1\/capes\/[a-f0-9]{64}$/.test(url.pathname)){
        const digest=url.pathname.split('/').at(-1);
        if(![...profiles.values()].some(p=>p.capeHash===digest&&p.style.cape===5&&p.seen>now()-90000))throw fail(404,'cape_not_shared');
        let b;try{b=await readFile(join(directory,'capes',digest+'.argb'));}catch{throw fail(404,'not_found');}if(req.headers['accept-encoding']?.includes('gzip')){res.setHeader('content-encoding','gzip');return reply(200,await gzip(b),'application/octet-stream');}return reply(200,b,'application/octet-stream');
      }
      throw fail(404,'not_found');
    } catch(e) {if(!res.headersSent)reply(e.status||502,{error:e.status?e.message:'upstream_unavailable'});else res.destroy();}
  });
  server.requestTimeout=15000;server.headersTimeout=10000;server.maxHeadersCount=30;
  server.on('close',()=>{clearInterval(timer);if(saveTimer)clearTimeout(saveTimer);});
  return {server,flush:persist,close:async()=>{await new Promise(r=>server.close(r));await persist();}};
}
if(process.argv[1]&&import.meta.url===pathToFileURL(resolve(process.argv[1])).href){
  const app=await createService({directory:process.env.DATA_DIR||'./data'});const port=Number(process.env.PORT||8787);
  app.server.listen(port,'0.0.0.0',()=>console.log(`CREW Cosmetics :${port}`));
  for(const signal of ['SIGINT','SIGTERM'])process.on(signal,async()=>{await app.close();process.exit(0);});
}
