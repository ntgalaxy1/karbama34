// #26 — تست در «محیط پیش‌نمایش واقعی»: دمو داخل iframe با sandbox="allow-scripts"
// هدف: مطمئن شویم دکمه‌ی افزودن آگهی واقعاً ثابت است و پنجره‌ی تأیید حذف در همین محیط هم کار می‌کند.
const puppeteer = require('/tmp/node_modules/puppeteer-core');
const sleep = ms => new Promise(r => setTimeout(r, ms));
let pass = 0, fail = 0;
const ok = (n, c, x) => { c ? (pass++, console.log('✅ ' + n)) : (fail++, console.log('❌ ' + n + (x ? ' → ' + x : ''))); };

(async () => {
  const b = await puppeteer.launch({ executablePath: '/usr/bin/chromium', headless: 'new', args: ['--no-sandbox', '--disable-dev-shm-usage'] });
  const p = await b.newPage();
  await p.setViewport({ width: 600, height: 940, deviceScaleFactor: 2 });
  await p.goto('http://127.0.0.1:8099/kb-tools/preview-wrapper.html', { waitUntil: 'networkidle0' });
  await sleep(1200);

  const frame = p.frames().find(f => f.url().includes('karbama-demo.html'));
  ok('۰) دموی داخل iframe سندباکس‌شده بالا آمد', !!frame, p.frames().map(f => f.url()).join(' | '));
  if (!frame) { console.log('\n❌ اجرا نتوانست ادامه دهد'); await b.close(); process.exit(1); }

  // ---------- ۱) دکمه‌ی ثابت افزودن آگهی داخل پیش‌نمایش ----------
  let st = await frame.evaluate(() => {
    S.lang = 'fa'; S.isAdmin = true; S.adminTab = 'jobs'; S.screen = 'admin'; render();
    const a = document.querySelector('#view .admin-add');
    const bar = document.querySelector('#view .appbar');
    return { exists: !!a, inBar: a && bar ? bar.contains(a) : false, svg: a ? !!a.querySelector('svg') : false };
  });
  ok('۱) در پیش‌نمایش، دکمه‌ی «افزودن آگهی» ثابت در نوار بالا هست', st.exists && st.inBar && st.svg, JSON.stringify(st));

  st = await frame.evaluate(() => {
    const v = document.getElementById('view');
    const a = document.querySelector('#view .admin-add');
    const tops = [];
    for (const s of [0, 300, 900, 100000]) { v.scrollTop = s; tops.push(Math.round(a.getBoundingClientRect().top)); }
    v.scrollTop = 0;
    return { tops };
  });
  const spread = Math.max(...st.tops) - Math.min(...st.tops);
  ok('۲) با هر اسکرولی سرجای خودش می‌ماند (لرزش < ۴px)', spread < 4, JSON.stringify(st));
  ok('۳) دکمه‌ی شناور روی «حذف» دیگر وجود ندارد', await frame.evaluate(() => !document.querySelector('#view .fab')) === true);

  // ---------- ۲) پنجره‌ی تأیید حذف داخل پیش‌نمایش ----------
  st = await frame.evaluate(() => {
    const before = S.jobs.length, j = S.jobs[0], id = j.id, name = j.company;
    delJob(id);
    const box = document.querySelector('#overlay .cfm-box');
    const wrap = document.querySelector('#overlay .cfm-wrap');
    const br = box && box.getBoundingClientRect();
    return { before, after: S.jobs.length, id, name, box: !!box, wrap: !!wrap,
             pos: wrap ? getComputedStyle(wrap).position : null,
             rect: br ? { t: Math.round(br.top), b: Math.round(br.bottom), vh: window.innerHeight } : null };
  });
  await sleep(400);
  ok('۴) در پیش‌نمایش هم اول می‌پرسد (چیزی پاک نشده)', st.box && st.after === st.before, JSON.stringify({ b: st.before, a: st.after, box: st.box }));
  ok('۵) پوشش پنجره fixed است و در پیش‌نمایش کامل دیده می‌شود',
    st.pos === 'fixed' && st.rect && st.rect.t >= 0 && st.rect.b <= st.rect.vh, JSON.stringify(st.rect));
  st = await frame.evaluate(() => {
    const box = document.querySelector('#overlay .cfm-box');
    const r = box.getBoundingClientRect();
    return { centered: Math.abs((r.top + r.bottom) / 2 - window.innerHeight / 2) < 40,
             text: box.querySelector('.cfm-msg').textContent,
             yes: [...box.querySelectorAll('button')][1].textContent.trim() };
  });
  ok('۶) پنجره وسط صفحه است و متن «مطمئن هستید» دیده می‌شود', st.centered && /مطمئن هستید/.test(st.text), JSON.stringify(st));

  st = await frame.evaluate(() => {
    const id = S.jobs[0].id, before = S.jobs.length;
    delJob(id);
    [...document.querySelectorAll('#overlay .cfm-box button')][0].click();   // انصراف
    const afterCancel = S.jobs.length;
    delJob(id);
    [...document.querySelectorAll('#overlay .cfm-box button')][1].click();   // بله، حذف
    return { before, afterCancel, afterYes: S.jobs.length, gone: !S.jobs.some(x => x.id === id), stillOpen: !!document.querySelector('#overlay .cfm-box') };
  });
  ok('۷) «انصراف» پاک نمی‌کند، «بله حذف شود» پاک می‌کند', st.afterCancel === st.before && st.afterYes === st.before - 1 && st.gone && !st.stillOpen, JSON.stringify(st));

  // ---------- ۳) سه زبان داخل پیش‌نمایش ----------
  st = await frame.evaluate(() => {
    const out = {};
    for (const lang of ['fa', 'tr', 'en']) {
      S.lang = lang; render(); delJob(S.jobs[0].id);
      const box = document.querySelector('#overlay .cfm-box');
      out[lang] = box.querySelector('.cfm-msg').textContent.slice(0, 18);
      closeConfirm();
    }
    S.lang = 'fa'; render();
    return out;
  });
  ok('۸) سه زبان داخل پیش‌نمایش هم درست است', /مطمئن/.test(st.fa) && /Emin/.test(st.tr) && /Are you sure/.test(st.en), JSON.stringify(st));

  console.log(`\nنتیجه‌ی محیط پیش‌نمایش: ${pass} ✅ / ${fail} ❌`);
  await b.close();
  process.exit(fail ? 1 : 0);
})();
