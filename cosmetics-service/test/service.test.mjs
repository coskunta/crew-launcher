import {test} from 'node:test';
import assert from 'node:assert/strict';
import {mkdtemp,rm} from 'node:fs/promises';
import {tmpdir} from 'node:os';
import {join} from 'node:path';
import {createService,validCape} from '../server.mjs';
const A='11111111111141118111111111111111', B='22222222222242228222222222222222';
const style={cape:5,wings:8,color:0,emote:9};
test('Two independent players: authenticated profiles, HD cape, replay/spoof defense, privacy, expiry, restart',async()=>{
 const directory=await mkdtemp(join(tmpdir(),'crew-'));let clock=1000000;
 const app=await createService({directory,now:()=>clock,verify:async c=>({id:c.name==='Alice'?A:B,name:c.name})});
 await new Promise(r=>app.server.listen(0,'127.0.0.1',r));const base=`http://127.0.0.1:${app.server.address().port}`;
 const request=async(path,method='GET',data,token)=>{const r=await fetch(base+path,{method,headers:token?{authorization:`Bearer ${token}`}:{},body:data===undefined?undefined:Buffer.isBuffer(data)?data:JSON.stringify(data)});return {status:r.status,data:r.headers.get('content-type')==='application/octet-stream'?Buffer.from(await r.arrayBuffer()):await r.json()};};
 const challenge=async(id,name)=>(await request('/v1/challenge','POST',{id,name})).data;
 const login=async(id,name)=>{const c=await challenge(id,name);return (await request('/v1/session','POST',{challenge:c.challenge})).data.token;};
 try {
  const c=await challenge(A,'Alice');assert.equal((await request('/v1/session','POST',{challenge:c.challenge})).status,200);assert.equal((await request('/v1/session','POST',{challenge:c.challenge})).status,401);
  const spoof=await challenge(B,'Alice');assert.equal((await request('/v1/session','POST',{challenge:spoof.challenge})).status,401);
  const expired=await challenge(A,'Alice');clock+=61000;assert.equal((await request('/v1/session','POST',{challenge:expired.challenge})).status,401);
  const alice=await login(A,'Alice'),bob=await login(B,'Bob');
  assert.equal((await request('/v1/profile','PUT',{style,elapsed:0,wingScale:1,wingSpeed:1})).status,401);
  assert.equal((await request('/v1/profile','PUT',{style:{...style,wings:99},elapsed:0,wingScale:1,wingSpeed:1},alice)).status,400);
  assert.equal((await request('/v1/profile','PUT',{id:B,style,elapsed:2500,wingScale:1.2,wingSpeed:.75},alice)).status,200);
  assert.equal((await request('/v1/profile','PUT',{style:{cape:7,wings:3,color:1,emote:0},elapsed:0,wingScale:1,wingSpeed:1},bob)).status,200);
  const hd=Buffer.alloc(8+2048*1024*4,127);hd.writeUInt32BE(2048,0);hd.writeUInt32BE(1024,4);
  assert(validCape(hd));assert(!validCape(hd.subarray(0,-1)));
  const uploaded=await request('/v1/cape','PUT',hd,alice);assert.equal(uploaded.status,200);
  clock+=1500;const both=(await request(`/v1/profiles?ids=${A},${B}`)).data.profiles;
  assert.equal(both.length,2);assert.equal(both.find(p=>p.id===A).elapsed,4000);assert.equal(both.find(p=>p.id===B).style.wings,3);
  assert.deepEqual((await request('/v1/capes/'+uploaded.data.hash)).data,hd);
  // A third client joining late sees the already published look without any Minecraft server plugin.
  assert.equal((await request(`/v1/profiles?ids=${A}`)).data.profiles[0].capeHash,uploaded.data.hash);
  assert.equal((await request('/v1/cape','PUT',Buffer.alloc(12),alice)).status,400);
  assert.equal((await request('/v1/cape','DELETE',undefined,alice)).status,200);
  assert.equal((await request('/v1/capes/'+uploaded.data.hash)).status,404);
  assert.equal((await request(`/v1/profiles?ids=${A}`)).data.profiles[0].capeHash,null);
  await request('/v1/profile','DELETE',undefined,alice);assert.equal((await request(`/v1/profiles?ids=${A},${B}`)).data.profiles.length,1);
  clock+=90001;assert.equal((await request(`/v1/profiles?ids=${B}`)).data.profiles.length,0);
  await app.flush();
 }finally{await app.close();}
 const restarted=await createService({directory});await new Promise(r=>restarted.server.listen(0,'127.0.0.1',r));await restarted.close();await rm(directory,{recursive:true,force:true});
});
