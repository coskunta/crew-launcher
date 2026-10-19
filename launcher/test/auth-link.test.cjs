const {test}=require('node:test'),assert=require('node:assert/strict');
const {verificationUrl}=require('../lib/auth-link.cjs');
test('preserves Microsoft consumer link returned by device endpoint',()=>{
 assert.equal(verificationUrl('https://www.microsoft.com/link'),'https://www.microsoft.com/link');
 assert.equal(verificationUrl('https://microsoft.com/devicelogin'),'https://microsoft.com/devicelogin');
});
test('rejects external hosts, credentials, insecure transport and unrelated Microsoft paths',()=>{
 for(const url of ['https://microsoft.com.evil.test/link','https://evil.test','http://www.microsoft.com/link','https://name:pass@www.microsoft.com/link','https://www.microsoft.com:8443/link','https://www.microsoft.com/unrelated','javascript:alert(1)'])assert.throws(()=>verificationUrl(url));
});
