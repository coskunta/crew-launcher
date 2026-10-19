'use strict';
const BASE='https://login.microsoftonline.com/consumers/oauth2/v2.0/';
const SCOPE='XboxLive.signin offline_access';
async function form(endpoint,data,signal){const r=await fetch(BASE+endpoint,{method:'POST',headers:{'Content-Type':'application/x-www-form-urlencoded'},body:new URLSearchParams(data),signal});const d=await r.json();return {ok:r.ok,data:d};}
async function request(url,data,token,signal){const r=await fetch(url,{method:data?'POST':'GET',headers:{...(data?{'Content-Type':'application/json'}:{}),...(token?{Authorization:'Bearer '+token}:{})},body:data?JSON.stringify(data):undefined,signal});if(!r.ok){let suffix='';if(r.status===403&&url.includes('login_with_xbox'))suffix=' Die eigene App-ID muss für die Minecraft Services freigegeben sein.';if(r.status===404&&url.endsWith('/profile'))suffix=' Minecraft-Java-Profil fehlt oder Java Edition ist nicht verfügbar.';throw Error('Anmeldung fehlgeschlagen ('+r.status+').'+suffix);}return r.json();}
async function minecraft(ms,signal){
 const xbl=await request('https://user.auth.xboxlive.com/user/authenticate',{Properties:{AuthMethod:'RPS',SiteName:'user.auth.xboxlive.com',RpsTicket:'d='+ms.access_token},RelyingParty:'http://auth.xboxlive.com',TokenType:'JWT'},null,signal);
 const xsts=await request('https://xsts.auth.xboxlive.com/xsts/authorize',{Properties:{SandboxId:'RETAIL',UserTokens:[xbl.Token]},RelyingParty:'rp://api.minecraftservices.com/',TokenType:'JWT'},null,signal);
 const claim=xsts.DisplayClaims?.xui?.[0];if(!claim?.uhs)throw Error('Xbox-Profil konnte nicht gelesen werden.');
 const mc=await request('https://api.minecraftservices.com/authentication/login_with_xbox',{identityToken:'XBL3.0 x='+claim.uhs+';'+xsts.Token},null,signal);
 // Profile lookup is the final ownership/profile gate; no offline account bypass.
 const profile=await request('https://api.minecraftservices.com/minecraft/profile',null,mc.access_token,signal);
 if(!/^[a-f0-9]{32}$/i.test(profile.id)||!profile.name)throw Error('Ungültiges Minecraft-Profil.');
 return {name:profile.name,uuid:profile.id,xuid:claim.xid||'',accessToken:mc.access_token,expires:Date.now()+mc.expires_in*1000,refreshToken:ms.refresh_token};
}
async function login(clientId,onCode,signal){if(!/^[0-9a-f-]{36}$/i.test(clientId))throw Error('Zuerst die eigene Microsoft-App-ID in den Einstellungen eintragen.');const first=await form('devicecode',{client_id:clientId,scope:SCOPE},signal);if(!first.ok)throw Error('App-ID ist für die Geräteanmeldung nicht eingerichtet.');const d=first.data;onCode({code:d.user_code,url:d.verification_uri,expires:d.expires_in});let interval=Math.max(5,d.interval||5);const end=Date.now()+d.expires_in*1000;
 while(Date.now()<end){await new Promise((resolve,reject)=>{const done=()=>{clearTimeout(t);reject(Error('Anmeldung abgebrochen.'));};const t=setTimeout(()=>{signal?.removeEventListener('abort',done);resolve();},interval*1000);if(signal?.aborted)done();else signal?.addEventListener('abort',done,{once:true});});const poll=await form('token',{grant_type:'urn:ietf:params:oauth:grant-type:device_code',client_id:clientId,device_code:d.device_code},signal);if(poll.ok)return minecraft(poll.data,signal);if(poll.data.error==='authorization_pending')continue;if(poll.data.error==='slow_down'){interval+=5;continue;}throw Error('Microsoft-Anmeldung abgebrochen oder abgelaufen.');}throw Error('Anmeldecode abgelaufen.');
}
async function refresh(clientId,token,signal){const r=await form('token',{client_id:clientId,grant_type:'refresh_token',refresh_token:token,scope:SCOPE},signal);if(!r.ok)throw Error('Bitte erneut mit Microsoft anmelden.');return minecraft(r.data,signal);}
module.exports={login,refresh};
