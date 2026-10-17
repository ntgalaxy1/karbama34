// آزمون کلیکِ واقعی (مثل کاربر): دکمه‌ی ثابت افزودن آگهی + پرسش «مطمئن هستید؟» قبل از حذف
const puppeteer = require('/tmp/node_modules/puppeteer-core');
const sleep = ms => new Promise(r => setTimeout(r, ms));
let pass=0, fail=0; const ok=(n,c,x)=>c?(pass++,console.log('✅ '+n)):(fail++,console.log('❌ '+n+(x?' → '+x:'')));
const clickCenter = async (p, sel, label) => {
  const pt = await p.evaluate((sel) => {
    const els=[...document.querySelectorAll(sel)];
    for (const e of els) {
      const r=e.getBoundingClientRect();
      const cx=Math.round(r.x+r.width/2), cy=Math.round(r.y+r.height/2);
      if (cy<2 || cy>window.innerHeight-6 || cx<2 || cx>window.innerWidth-6) continue;   // داخل قاب باشد
      const hit=document.elementFromPoint(cx,cy);
      if (hit===e || (hit && e.contains(hit))) return {cx,cy,label:e.textContent.trim().slice(0,20)};
    }
    return null;
  }, sel);
  if(!pt) throw new Error('no visible/clickable element for '+sel);
  await p.mouse.click(pt.cx, pt.cy);
  return pt;
};
(async () => {
  const b = await puppeteer.launch({ executablePath:'/usr/bin/chromium', headless:'new', args:['--no-sandbox','--disable-dev-shm-usage'] });
  for (const [W,H,tag] of [[420,880,'۴۲۰px'],[360,780,'۳۶۰px']]) {
    const p = await b.newPage(); const errs=[]; p.on('pageerror',e=>errs.push(String(e)));
    await p.setViewport({ width: W, height: H, deviceScaleFactor: 2 });
    await p.goto('file:///home/user/karbama-demo.html', { waitUntil:'networkidle0' });
    // اگر زبان قبلاً ذخیره شده، صفحه‌ی خوش‌آمد مستقیم می‌آید
    await p.waitForFunction(() => document.querySelectorAll('#view .langbtn, #view button').length > 0, { timeout: 15000 });
    await sleep(600);
    await p.evaluate(() => { const lb=document.querySelector('#view .langbtn'); if (lb) lb.click(); });
    await sleep(300);
    console.log(`\n--- ${tag} ---`);
    // ورود با کلیک
    await p.evaluate(()=>{ [...document.querySelectorAll('#view button')].find(b=>b.textContent.includes('ورود به برنامه')).click(); }); await sleep(350);
    await p.type('#f_login_user','admin'); await p.type('#f_login_pass','karbama2026');
    await p.evaluate(()=>{ document.querySelector('#view .btn-primary').click(); }); await sleep(600);

    // دکمه‌ی افزودن آگهی: ثابت در نوار بالا
    let st = await p.evaluate(()=>{
      const a=document.querySelector('#view .admin-add'); const bar=document.querySelector('#view .appbar');
      if(!a) return {missing:true};
      const r=a.getBoundingClientRect(), br=bar.getBoundingClientRect(); const inBar = bar.contains(a);
      return { inBar: inBar, top:Math.round(r.top), h:Math.round(r.height), svg:!!a.querySelector('svg'),
               label:a.textContent.trim(), distFromTop:Math.round(r.top - br.top) };
    });
    ok(`${tag}: دکمه‌ی افزودن آگهی ثابت در نوار بالا هست`, !st.missing && st.inBar && st.svg, JSON.stringify(st));

    // با اسکرول جابه‌جا نمی‌شود
    const p0 = await p.evaluate(()=>{ const r=document.querySelector('#view .admin-add').getBoundingClientRect(); return Math.round(r.top); });
    await p.mouse.move(210, Math.round(H/2));
    for (const d of [600, 900, 900]) { await p.mouse.wheel({deltaY:d}); await sleep(350); }
    const p1 = await p.evaluate(()=>{ const r=document.querySelector('#view .admin-add').getBoundingClientRect(); return Math.round(r.top); });
    ok(`${tag}: با اسکرول هم سرجای خودش می‌ماند`, Math.abs(p1-p0)<3, `${p0} → ${p1}`);

    // کلیک روی دکمه‌ی ثابت → صفحه‌ی افزودن آگهی
    let pt = await clickCenter(p, '#view .admin-add');
    await sleep(600);
    st = await p.evaluate(()=>({screen:S.screen}));
    ok(`${tag}: کلیک روی آن، صفحه‌ی «افزودن آگهی» را باز می‌کند`, st.screen==='jobEditor', JSON.stringify(st));
    await p.evaluate(()=>{ go('admin'); }); await sleep(450);

    // هیچ دکمه‌ای روی دکمه‌ی «حذف» نمی‌افتد
    st = await p.evaluate(()=>{
      const v=document.getElementById('view');
      const bad=[];
      for(let s=0;s<=9000;s+=120){
        v.scrollTop=s;
        const dels=[...document.querySelectorAll('#view button')].filter(b=>(b.getAttribute('onclick')||'').includes('delJob'));
        for(const d of dels){
          const r=d.getBoundingClientRect();
          const cx=Math.round(r.x+r.width/2), cy=Math.round(r.y+r.height/2);
          if(r.top<60||r.bottom>window.innerHeight-6) continue;   // فقط دکمه‌های کاملاً داخل قاب
          const hit=document.elementFromPoint(cx,cy);
          if(!(hit===d||d.contains(hit))) bad.push({scroll:s, cy, hit: hit?hit.className:null});
          // لبه‌ی چپ دکمه هم بررسی شود
          const lx=Math.round(r.left+8);
          const hit2=document.elementFromPoint(lx,cy);
          if(!(hit2===d||d.contains(hit2))) bad.push({scroll:s, edge:'left', lx, cy, hit: hit2?hit2.className:null});
        }
      }
      v.scrollTop=0;
      return {bad: bad.slice(0,4)};
    });
    ok(`${tag}: هیچ عنصری روی دکمه‌های «حذف» نمی‌افتد (وسط و لبه‌ی چپ)`, st.bad.length===0, JSON.stringify(st.bad));

    // حذف با کلیک واقعی: اول می‌پرسد
    const before = await p.evaluate(()=>S.jobs.length);
    pt = await clickCenter(p, '#view button.btn-danger');
    await sleep(600);
    st = await p.evaluate(()=>{
      const box=document.querySelector('#overlay .cfm-box');
      return {open:!!box, count:S.jobs.length, msg: box?box.querySelector('.cfm-msg').textContent.trim():null};
    });
    ok(`${tag}: با کلیک روی «حذف» اول می‌پرسد و پاک نمی‌کند`, st.open && st.count===before, JSON.stringify({...st, before}));
    ok(`${tag}: متن پرسش «آیا مطمئن هستید؟» دیده می‌شود`, /مطمئن هستید/.test(st.msg||''), st.msg);

    // «بله، حذف شود» با کلیک واقعی
    pt = await p.evaluate(()=>{ const bs=[...document.querySelectorAll('#overlay .cfm-box button')]; const b=bs[bs.length-1]; const r=b.getBoundingClientRect(); return {cx:Math.round(r.x+r.width/2), cy:Math.round(r.y+r.height/2), label:b.textContent.trim()}; });
    await p.mouse.click(pt.cx, pt.cy);
    await sleep(600);
    const after = await p.evaluate(()=>S.jobs.length);
    ok(`${tag}: «بله، حذف شود» آگهی را پاک می‌کند`, after===before-1, `${before} → ${after}`);

    ok(`${tag}: بدون خطای صفحه`, errs.length===0, errs.slice(0,2).join(' | '));
    await p.screenshot({ path: `/tmp/h27-${W}.png` });
    await p.close();
  }
  console.log(`\nنتیجه: ${pass} ✅ / ${fail} ❌`);
  await b.close(); process.exit(fail?1:0);
})();
