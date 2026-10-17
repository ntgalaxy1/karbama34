const fs=require('fs');
const s=fs.readFileSync('/home/user/karbama-demo.html','utf8');
const m=s.match(/<script>([\s\S]*)<\/script>/);
fs.writeFileSync('/home/user/kb-tools/demo.js', m[1]);
console.log('demo.js بروزرسانی شد ('+m[1].length+' بایت)');
