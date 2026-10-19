const {contextBridge,ipcRenderer}=require('electron');
const calls={state:'crew:state',settings:'crew:settings',chooseJava:'crew:java',automaticJava:'crew:java-auto',install:'crew:install',login:'crew:login',cancel:'crew:cancel',logout:'crew:logout',launch:'crew:launch',module:'crew:module',cosmetic:'crew:cosmetic',importCape:'crew:cape',capePreview:'crew:cape-preview',folder:'crew:folder',help:'crew:help'};
const api={};for(const [name,channel] of Object.entries(calls))api[name]=(...args)=>ipcRenderer.invoke(channel,...args);
api.onEvent=listener=>{const fn=(_event,payload)=>listener(payload);ipcRenderer.on('crew:event',fn);return()=>ipcRenderer.removeListener('crew:event',fn);};contextBridge.exposeInMainWorld('crew',api);
