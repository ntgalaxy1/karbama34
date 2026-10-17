// #26 — تست مرورگری: ۱) دکمه‌ی افزودن آگهی ثابت می‌ماند  ۲) حذف آگهی با پرسش «مطمئن هستید؟»
const puppeteer = require('/tmp/node_modules/puppeteer-core');
const sleep = ms => new Promise(r => setTimeout(r, ms));
let pass = 0, fail = 0;
const ok = (n, c, x) => { c ? (pass++, console.log('✅ ' + n)) : (fail++, console.log('❌ ' + n + (x ? ' → ' + x : ''))); };
const clean = p => p.evaluate(() => { try { S.toast = null; document.querySelectorAll('.toast').forEach(e => e.remove()); } catch (e) {} });

(async () => {
  const b = await puppeteer.launch({ executablePath: '/usr/bin/chromium', headless: 'new', args: ['--no-sandbox', '--disable-dev-shm-usage'] });
  const p = await b.newPage(); const errs = []; p.on('pageerror', e => errs.push(String(e)));
  await p.setViewport({ width: 420, height: 900, deviceScaleFactor: 2 });
  await p.goto('file:///home/user/karbama-demo.html', { waitUntil: 'networkidle0' });
  await sleep(700);
  await p.evaluate(() => { S.lang = 'fa'; S.isAdmin = true; S.adminTab = 'jobs'; S.screen = 'admin'; render(); });
  await sleep(500);

  // ---------- ۱) دکمه‌ی افزودن آگهی: ثابت در نوار بالا ----------
  let st = await p.evaluate(() => {
    const a = document.querySelector('#view .admin-add');
    if (!a) return { missing: true };
    const bar = document.querySelector('#view .appbar');
    const r = a.getBoundingClientRect(), br = bar.getBoundingClientRect();
    return { inBar: bar.contains(a), svg: !!a.querySelector('svg'), label: a.textContent.trim(),
             offTop: Math.round(r.top - br.top), h: Math.round(r.height) };
  });
  ok('۱) دکمه‌ی «افزودن آگهی» داخل نوار بالای پنل مدیریت است', !st.missing && st.inBar && st.svg, JSON.stringify(st));
  ok('۲) دکمه‌ی شناوری که روی «حذف» می‌افتاد حذف شده', await p.evaluate(() => !document.querySelector('#view .fab')) === true);

  st = await p.evaluate(() => {
    const out = [];
    const v = document.getElementById('view');
    for (const s of [0, 400, 1200, 100000]) {
      v.scrollTop = s;
      const a = document.querySelector('#view .admin-add').getBoundingClientRect();
      out.push(Math.round(a.top));
    }
    v.scrollTop = 0;
    return { tops: out };
  });
  ok('۳) با اسکرول، دکمه ثابت می‌ماند (لرزش < ۴px)', Math.max(...st.tops) - Math.min(...st.tops) < 4, JSON.stringify(st));

  st = await p.evaluate(() => {
    const v = document.getElementById('view');
    const out = { overlaps: [] };
    for (let s = 0; s <= 9000; s += 150) {
      v.scrollTop = s;
      const dels = [...document.querySelectorAll('#view button')].filter(b => (b.getAttribute('onclick') || '').includes('delJob'));
      for (const d of dels) {
        const r = d.getBoundingClientRect();
        const cx = Math.round(r.x + r.width / 2), cy = Math.round(r.y + r.height / 2);
        if (r.top < 60 || r.bottom > window.innerHeight - 6) continue;
        const hit = document.elementFromPoint(cx, cy);
        if (!(hit === d || d.contains(hit))) out.overlaps.push({ s, cx, cy, hit: hit ? String(hit.className) : null });
      }
    }
    v.scrollTop = 0;
    return out;
  });
  ok('۴) هیچ عنصری روی دکمه‌های «حذف» نمی‌افتد (کلیک‌ها آزاد است)', st.overlaps.length === 0, JSON.stringify(st.overlaps.slice(0, 3)));
  await clean(p); await p.screenshot({ path: '/tmp/f26-header-btn.png' });

  // ---------- ۲) حذف آگهی: اول سؤال، بعد پاک‌شدن ----------
  st = await p.evaluate(() => {
    const before = S.jobs.length;
    const j = S.jobs[0];
    const title = typeof jobTitle === 'function' ? jobTitle(j.titleKey) : '';
    delJob(j.id);
    const box = document.querySelector('#overlay .cfm-box');
    return { before, after: S.jobs.length, id: j.id, title, box: !!box,
             html: box ? box.innerHTML : '', overlayHtml: document.querySelector('#overlay').innerHTML };
  });
  ok('۵) زدن «حذف» بی‌درنگ پاک نمی‌کند (اول می‌پرسد)', st.after === st.before, JSON.stringify({ b: st.before, a: st.after }));
  ok('۶) پنجره‌ی تأیید باز می‌شود', st.box === true);
  st = await p.evaluate(() => {
    const box = document.querySelector('#overlay .cfm-box');
    return { title: box.querySelector('.cfm-title').textContent.trim(),
             subject: box.querySelector('.cfm-sub').textContent.trim(),
             msg: box.querySelector('.cfm-msg').textContent.trim(),
             btns: [...box.querySelectorAll('button')].map(b => b.textContent.trim()),
             danger: !!box.querySelector('.cfm-danger') };
  });
  ok('۷) عنوان «حذف آگهی» روی پنجره هست', /حذف آگهی/.test(st.title), st.title);
  ok('۸) نام آگهی داخل پنجره نشان داده می‌شود', st.subject.length > 3, st.subject);
  ok('۹) متن پرسش «آیا مطمئن هستید؟» آمده', /مطمئن هستید/.test(st.msg), st.msg);
  ok('۱۰) دو دکمه‌ی «انصراف» و «بله، حذف شود» (دکمه‌ی قرمز)', st.btns.length === 2 && /انصراف/.test(st.btns[0]) && /بله، حذف/.test(st.btns[1]) && st.danger, JSON.stringify(st));

  // پنجره همیشه کامل و وسط صفحه (مستقل از اسکرول)
  await sleep(400);   // پایان انیمیشن ورود پنجره
  st = await p.evaluate(() => {
    const v = document.getElementById('view');
    const out = {};
    for (const pos of ['top', 'mid', 'end']) {
      v.scrollTop = pos === 'top' ? 0 : pos === 'mid' ? 400 : 100000;
      const r = document.querySelector('#overlay .cfm-box').getBoundingClientRect();
      out[pos] = { top: Math.round(r.top), bottom: Math.round(r.bottom), vh: window.innerHeight,
                   fullyVisible: r.top >= 0 && r.bottom <= window.innerHeight,
                   centered: Math.abs((r.top + r.bottom) / 2 - window.innerHeight / 2) < 40 };
    }
    v.scrollTop = 0;
    return out;
  });
  ok('۱۱) پنجره‌ی تأیید همیشه کامل داخل صفحه است (بالا/وسط/ته فهرست)',
    st.top.fullyVisible && st.mid.fullyVisible && st.end.fullyVisible, JSON.stringify(st));
  ok('۱۲) دقیقاً وسط صفحه می‌نشیند', st.top.centered && st.end.centered, JSON.stringify(st));
  await clean(p); await p.screenshot({ path: '/tmp/f26-confirm.png' });

  // انصراف → چیزی پاک نمی‌شود
  st = await p.evaluate(() => {
    const before = S.jobs.length;
    [...document.querySelectorAll('#overlay .cfm-box button')][0].click();
    return { before, after: S.jobs.length, box: !!document.querySelector('#overlay .cfm-box') };
  });
  ok('۱۳) «انصراف» → هیچ آگهی‌ای حذف نمی‌شود و پنجره بسته می‌شود', st.after === st.before && st.box === false, JSON.stringify(st));

  // تأیید → پاک می‌شود
  st = await p.evaluate(() => {
    const before = S.jobs.length, j = S.jobs[0], id = j.id;
    delJob(id);
    [...document.querySelectorAll('#overlay .cfm-box button')][1].click();
    return { before, after: S.jobs.length, gone: !S.jobs.some(x => x.id === id), box: !!document.querySelector('#overlay .cfm-box') };
  });
  ok('۱۴) «بله، حذف شود» → آگهی واقعاً پاک می‌شود', st.after === st.before - 1 && st.gone === true && st.box === false, JSON.stringify(st));

  // بستن با کلیک بیرون پنجره
  st = await p.evaluate(() => {
    const before = S.jobs.length;
    delJob(S.jobs[0].id);
    const wrap = document.querySelector('#overlay .cfm-wrap');
    wrap.dispatchEvent(new MouseEvent('click', { bubbles: true }));
    return { before, after: S.jobs.length };
  });
  await sleep(200);
  ok('۱۵) کلیک بیرون پنجره → لغو (چیزی حذف نمی‌شود)', st.after === st.before, JSON.stringify(st));

  // ---------- ۳) سه زبان ----------
  st = await p.evaluate(() => {
    const out = {};
    for (const lang of ['fa', 'tr', 'en']) {
      S.lang = lang; render(); delJob(S.jobs[0].id);
      const box = document.querySelector('#overlay .cfm-box');
      out[lang] = { msg: box.querySelector('.cfm-msg').textContent.trim(), yes: [...box.querySelectorAll('button')][1].textContent.trim() };
      closeConfirm();
    }
    return out;
  });
  ok('۱۶) در ترکی و انگلیسی هم پرسش درست است', /Emin misiniz/.test(st.tr.msg) && /Are you sure/.test(st.en.msg) && /Evet/.test(st.tr.yes) && /Yes/.test(st.en.yes), JSON.stringify(st));

  // ---------- ۴) FAB در تب‌های دیگر بی‌جا نباشد ----------
  st = await p.evaluate(() => {
    const out = {};
    S.lang = 'fa'; S.screen = 'admin'; S.adminTab = 'people'; render(); out.people = !!document.querySelector('#view .admin-add');
    S.screen = 'admin'; S.adminTab = 'settings'; render(); out.settings = !!document.querySelector('#view .admin-add');
    S.screen = 'admin'; S.adminTab = 'jobs'; render(); out.jobs = !!document.querySelector('#view .admin-add');
    return out;
  });
  ok('۱۷) دکمه‌ی ثابت افزودن آگهی در هر سه تب مدیریت دیده می‌شود', st.jobs && st.people && st.settings, JSON.stringify(st));

  ok('۱۸) بدون خطای صفحه', errs.length === 0, errs.slice(0, 2).join(' | '));
  console.log(`\nنتیجه: ${pass} ✅ / ${fail} ❌`);
  await b.close();
  process.exit(fail ? 1 : 0);
})();
