/* شبیه‌ساز سبک DOM برای اجرای منطق دمو در Node (بدون مرورگر) */
const els = {};
function mkEl(id){ return {id, innerHTML:"", textContent:"", value:"", scrollTop:0,
  dataset:{},
  setAttribute(){}, getAttribute(){return null}, classList:{add(){},remove(){}},
  querySelector(){return null}, appendChild(){}, click(){}, style:{},
  getBoundingClientRect:function(){return {width:340,height:150,left:0,top:0}},
  getContext:function(){return {lineWidth:1,lineCap:"",lineJoin:"",strokeStyle:"",beginPath(){},moveTo(){},lineTo(){},stroke(){},clearRect(){},drawImage(){}}},
  toDataURL:function(){return "data:image/png;base64,AAA"},
  addEventListener(){}, focus(){}, setSelectionRange(){}, selectionStart:0 }; }
/* رویدادها و وضعیت صفحه (برای تست انتقال به «خودِ برنامه») */
const _ev = {};
global.document = {
  documentElement:{ setAttribute(){}, getAttribute(){return null} },
  body: mkEl('body'),
  hidden:false, visibilityState:"visible",
  getElementById:(id)=> els[id] || (els[id]=mkEl(id)),
  createElement:()=>mkEl('x'),
  querySelector:()=>null,
  querySelectorAll:()=>[],
  addEventListener:(t,f)=>{ (_ev[t]=_ev[t]||[]).push(f); },
  removeEventListener:(t,f)=>{ _ev[t]=(_ev[t]||[]).filter(x=>x!==f); },
  fireEvent:(t)=>{ (_ev[t]||[]).slice().forEach(f=>{ try{ f({type:t}); }catch(e){} }); }
};
global.window = { addEventListener(){}, removeEventListener(){} };
global.Image = function(){ return {onload:null,onerror:null} };
global.FileReader = function(){ this.readAsDataURL=()=>{} };
global.confirm = ()=>true;
global.setInterval = ()=>{};
/* تایمرها اجرا نمی‌شوند ولی ثبت می‌شوند تا تست بتواند آگاهانه اجرایشان کند (runTimers) */
global.setTimeout = (f, ms)=>{ (global.__timers = global.__timers || []).push({f, ms}); return 0; };
global.clearTimeout = ()=>{};
global.runTimers = ()=>{ const t = global.__timers || []; global.__timers = []; t.forEach(x=>{ try{ x.f && x.f(); }catch(e){} }); };
