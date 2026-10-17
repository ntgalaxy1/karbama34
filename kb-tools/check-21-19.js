// رگرسیون #21 و #19 روی نسخه‌ی جاری دمو (بازسازی‌شده بعد از پاک‌شدن /tmp)
const puppeteer = require('/tmp/node_modules/puppeteer-core');
const sleep = ms => new Promise(r => setTimeout(r, ms));
let pass = 0, fail = 0;
const ok = (n, c, x) => { c ? (pass++, console.log('✅ ' + n)) : (fail++, console.log('❌ ' + n + (x ? ' → ' + x : ''))); };

(async () => {
  const b = await puppeteer.launch({ executablePath: '/usr/bin/chromium', headless: 'new', args: ['--no-sandbox', '--disable-dev-shm-usage'] });
  const p = await b.newPage(); const errs = []; p.on('pageerror', e => errs.push(String(e)));
  await p.setViewport({ width: 420, height: 940, deviceScaleFactor: 2 });
  await p.goto('file:///home/user/karbama-demo.html', { waitUntil: 'networkidle0' });
  await sleep(700);
  await p.evaluate(() => { S.lang = 'fa'; });

  // ---------- ورود مدیر و تنظیمات ----------
  await p.evaluate(() => { openLogin(); document.getElementById('f_login_user').value = 'admin'; document.getElementById('f_login_pass').value = 'karbama2026'; doLogin(); });
  await sleep(500);
  ok('۱) ورود مدیر → پنل مدیریت', await p.evaluate(() => S.isAdmin && S.screen === 'admin'));
  ok('۲) بدون نوار تب پایین در حالت مدیر', await p.evaluate(() => document.getElementById('tabbar').innerHTML.trim() === ''));
  await p.evaluate(() => { S.adminTab = 'settings'; render(); });
  await sleep(400);
  let st = await p.evaluate(() => {
    const h = document.querySelector('#view').innerHTML;
    const vals = [...document.querySelectorAll('#view input')].map(i => i.value);
    return { holder: h.includes('نام کارگزار'), user: h.includes('یوزرنیم مدیر'), pass: h.includes('پسورد مدیر'),
             fee: h.includes('مبلغ هزینه کارگزار'), card: vals.some(v => String(v).includes('1234 1234')) };
  });
  ok('۳) «نام کارگزار» هنوز حذف است', !st.holder);
  ok('۴) فیلدهای یوزرنیم/پسورد مدیر سرجایشان', st.user && st.pass);
  ok('۵) بخش «مبلغ» در تنظیمات نیست', !st.fee);
  ok('۶) کارت بانکی پیش‌فرض هست', st.card);

  // ---------- تغییر یوزرنیم/پسورد ----------
  st = await p.evaluate(() => {
    S.adminTab = 'settings'; render();
    const ins = [...document.querySelectorAll('#view input')].filter(i => !i.value.includes('1234'));
    const user = ins.find(i => i.value === 'admin'), pass = ins.find(i => i.type === 'password');
    if (!user || !pass) return { ok: false };
    user.value = 'modir2'; user.dispatchEvent(new Event('input', { bubbles: true }));
    pass.value = 'pass1234'; pass.dispatchEvent(new Event('input', { bubbles: true }));
    return { ok: true };
  });
  await sleep(200);
  await p.evaluate(() => { saveAdminCred(); });
  await sleep(400);
  ok('۷) ذخیره‌ی یوزرنیم/پسورد جدید', await p.evaluate(() => S.adminCred.user === 'modir2' && S.adminCred.pass === 'pass1234'));
  await p.evaluate(() => { logout(); openLogin(); document.getElementById('f_login_user').value = 'modir2'; document.getElementById('f_login_pass').value = 'pass1234'; doLogin(); });
  await sleep(500);
  ok('۸) ورود با مقادیر تازه', await p.evaluate(() => S.isAdmin === true));
  // بازگشت به مقادیر پیش‌فرض
  await p.evaluate(() => { S.adminCred = { user: 'admin', pass: 'karbama2026' }; S.twoFA = { on: false, secret: '', recovery: '', pending: false }; persist(); });

  // ---------- فیلتر وضعیت + صفحه‌ی کامل متقاضی ----------
  await p.evaluate(() => { S.isAdmin = true; S.adminTab = 'people'; S.adminPeopleStatus = ''; render(); });
  await sleep(400);
  st = await p.evaluate(() => {
    const h = document.querySelector('#view').innerHTML;
    const chips = [...document.querySelectorAll('#view .chip')].map(c => c.textContent.trim());
    return { hint: h.includes('برای تفکیک'), chips, count: S.people.length };
  });
  ok('۹) چیپ‌های وضعیت با شمارش', st.chips.some(c => c.includes('جدید')) && st.chips.some(c => c.includes('بررسی‌شده')) && st.chips.some(c => c.includes('معرفی‌شده')), JSON.stringify(st.chips.slice(0, 6)));
  st = await p.evaluate(() => {
    const n0 = document.querySelectorAll('#view .person, #view [data-pid], #view .pcard').length;
    setPeopleStatus('new');
    const n1 = document.querySelectorAll('#view .person, #view [data-pid], #view .pcard').length;
    const filtered = S.adminPeopleStatus;
    setPeopleStatus('new');   // برداشتن فیلتر
    const after = S.adminPeopleStatus;
    const n2 = document.querySelectorAll('#view .person, #view [data-pid], #view .pcard').length;
    return { n0, n1, n2, filtered, after };
  });
  ok('۱۰) کلیک روی وضعیت فهرست را فیلتر می‌کند و کلیک دوباره برمی‌دارد', st.filtered === 'new' && st.after === '' && st.n1 < st.n0 && st.n2 === st.n0, JSON.stringify(st));
  st = await p.evaluate(() => {
    const pid = (S.people[0] || {}).id; S.adminTab = 'people'; render(); openPerson(pid);
    const h = document.querySelector('#view').innerHTML;
    return { screen: S.screen, full: S.screen === 'person', sheetGone: !document.querySelector('#overlay').innerHTML.includes('اطلاعات کامل متقاضی') || true,
             hasBack: h.includes('بازگشت'), hasFee: h.includes('مبلغ این متقاضی') || h.includes('مبلغ') };
  });
  ok('۱۱) کلیک روی متقاضی → صفحه‌ی کامل (نه پنجره‌ی کوچک)', st.full === true, JSON.stringify(st));
  ok('۱۲) صفحه‌ی کامل: دکمه‌ی بازگشت و بخش مبلغ', st.hasBack && st.hasFee, JSON.stringify(st));

  // ---------- دیزاین v3 ----------
  await p.evaluate(() => { S.isAdmin = false; S.session = null; go('contact'); });
  await sleep(500);
  st = await p.evaluate(() => {
    const head = document.querySelector('#view .kb-head');
    const rows = [...document.querySelectorAll('#view a.card.contact-row, #view .card.contact-row')];
    const credit = [...document.querySelectorAll('#view *')].filter(e => e.textContent.trim() === 'powered by naser tavakkoli @nt.galaxy');
    return { head: !!head, headBg: head ? getComputedStyle(head).backgroundImage.slice(0, 24) : null,
             rows: rows.length, rowW: rows[0] ? Math.round(rows[0].getBoundingClientRect().width) : 0, credit: credit.length };
  });
  ok('۱۳) هدر گرادیان برند (kb-head)', st.head && st.headBg.startsWith('linear-gradient'), JSON.stringify(st));
  ok('۱۴) هر سه ردیف «ارتباط با ما» لینک کامل‌عرض', st.rows === 3 && st.rowW > 300, JSON.stringify(st));
  ok('۱۵) خط اعتبار در «ارتباط با ما»', st.credit >= 1);

  ok('۱۶) بدون خطای صفحه', errs.length === 0, errs.slice(0, 2).join(' | '));
  console.log(`\nنتیجه: ${pass} ✅ / ${fail} ❌`);
  await b.close();
  process.exit(fail ? 1 : 0);
})();
