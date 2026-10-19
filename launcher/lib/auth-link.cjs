'use strict';
// Always follow the verification URI issued for this device-code request.
// Personal accounts may use microsoft.com/link instead of devicelogin.
function verificationUrl(value){
 const url=new URL(value);
 const permitted=(['www.microsoft.com','microsoft.com'].includes(url.hostname)&&['/link','/devicelogin'].includes(url.pathname.replace(/\/$/,'')))||
 (url.hostname==='login.microsoftonline.com'&&/^\/(common|consumers|organizations)\/oauth2\/deviceauth\/?$/.test(url.pathname));
 if(url.protocol!=='https:'||url.username||url.password||url.port||!permitted)throw Error('Microsoft hat eine unerwartete Anmelde-URL geliefert.');
 return url.href;
}
module.exports={verificationUrl};
