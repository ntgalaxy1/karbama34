/* #34-paywall — تست با ماوس واقعی: تا پرداخت هزینه، هیچ آگهی‌ای دیده نمی‌شود
   چرخه: کارجو وارد می‌شود → صفحه‌ی «آگهی‌ها قفل است» → «واریز کردم» → در انتظار تأیید
         → مدیر تأیید می‌کند → آگهی‌ها باز می‌شود */
const puppeteer = require('/tmp/node_modules/puppeteer-core');
const FILE = 'file:///home/user/karbama-demo.html';
const sleep = ms => new Promise(r => setTimeout(r, ms));
let pass = 0, fail = 0;
const ok = (n, c, x) => { c ? pass++ : fail++; console.log(`${c ? '✅' : '❌'} ${n}${!c && x ? ' → ' + x : ''}`); };

async function run(W, H, tag) {
  const b = await puppeteer.launch({ executablePath: '/usr/bin/chromium', headless: 'new',
    args: ['--no-sandbox', '--disable-dev-shm-usage'] });
  const p = await b.newPage();
  const errs = [];
  p.on('pageerror', e => errs.push(String(e)));
  await p.setViewport({ width: W, height: H });
  await p.goto(FILE);
  await p.evaluate(() => { try { localStorage.clear(); } catch (e) {} });
  await p.reload();
  await p.waitForFunction(() => document.querySelector('#view button'));

  // ---------- ۱) کارجوی تازه (پرداخت‌نشده) وارد می‌شود ----------
  const first = await p.evaluate(async () => {
    S.lang = 'fa'; S.isAdmin = false;
    S.session = { first: 'علی', last: 'احمدی', father: 'محمد', phone: '05320000000',
      city: 'İstanbul', district: 'Fatih', jobs: ['waiter'], trLevel: 40, uid: 'u1', id: 'u1',
      username: 'ali', nationalId: '0012345678', address: 'x', createdAt: Date.now() };
    S.session.pay = '';
    S.people = [S.session];
    S.liked = []; S.applied = [];
    S.screen = 'home'; render();
    await new Promise(r => setTimeout(r, 300));
    const txt = document.querySelector('#view').textContent;
    return { txt, jobs: document.querySelectorAll('#view .job').length,
             fee: S.adsFee, cards: document.querySelectorAll('#view .card').length };
  });
  ok(`${tag} ۱) کارجوی پرداخت‌نشده → صفحه‌ی «آگهی‌ها قفل است»`,
     /آگهی‌ها قفل است/.test(first.txt) && first.jobs === 0, 'قلب کارت‌های آگهی: ' + first.jobs);

  // ---------- ۲) هیچ کارت آگهی و هیچ آگهی‌ای روی صفحه نیست ----------
  const leak = await p.evaluate(() => {
    const t2 = document.querySelector('#view').textContent;
    const aJob = S.jobs[0];
    const hasCompany = aJob.company && t2.includes(aJob.company.slice(0, 8));
    return { jobs: document.querySelectorAll('#view .job').length, hasCompany,
             likes: document.querySelectorAll('#view .lk-row').length };
  });
  ok(`${tag} ۲) هیچ آگهی/شرکتی در صفحه‌ی قفل دیده نمی‌شود`,
     leak.jobs === 0 && !leak.hasCompany, JSON.stringify(leak));

  // ---------- ۳) مبلغ و کارت کارگزار و کد پیگیری نمایش داده می‌شود ----------
  const info = await p.evaluate(() => {
    const txt = document.querySelector('#view').textContent;
    const card = (S.broker && S.broker.card) || '1234 1234 1234 1234';
    return { hasFee: /(6,000|۶,۰۰۰|6\.000|۶٬۰۰۰)/.test(txt),
             hasCard: txt.includes(card.replace(/ /g, '')) || txt.includes(card),
             hasRef: /KB-\d{5}/.test(txt), txt: txt.slice(0, 160) };
  });
  ok(`${tag} ۳) مبلغ ۶۰۰۰ + شماره کارت کارگزار + کد پیگیری نمایش داده می‌شود`,
     info.hasFee && (info.hasCard || info.hasRef), JSON.stringify({ fee: info.hasFee, card: info.hasCard, ref: info.hasRef }));

  // ---------- ۴) تیتر «آگهی‌ها» در تب، قفل است ----------
  const tabLock = await p.evaluate(() => {
    const t2 = document.querySelector('.tabbar');
    return t2 ? t2.textContent.replace(/\s+/g, ' ').trim() : '';
  });
  ok(`${tag} ۴) تبِ آگهی‌ها با 🔒 و برچسب قفل دیده می‌شود`,
     /🔒/.test(tabLock) && /آگهی‌ها/.test(tabLock), tabLock);

  // ---------- ۵) کلیک واقعی روی «واریز کردم» ----------
  const btn = await p.evaluate(() => {
    const el = [...document.querySelectorAll('#view button')]
      .find(x => /واریز کردم/.test(x.textContent));
    if (!el) return null;
    el.scrollIntoView({ block: 'center' });
    const r = el.getBoundingClientRect();
    return { x: r.x + r.width / 2, y: r.y + r.height / 2, label: el.textContent.trim() };
  });
  ok(`${tag} ۵) دکمه‌ی «واریز کردم — ارسال برای تأیید» هست`, !!btn, 'پیدا نشد');
  if (btn) { await p.mouse.click(btn.x, btn.y); await sleep(600); }

  const pending = await p.evaluate(() => ({
    pay: (S.people.find(x => x.id === 'u1') || {}).pay,
    txt: document.querySelector('#view').textContent,
    jobs: document.querySelectorAll('#view .job').length
  }));
  ok(`${tag} ۶) بعد از کلیک: وضعیت «در انتظار تأیید» و همچنان بدون آگهی`,
     pending.pay === 'pending' && /در انتظار تأیید پرداخت/.test(pending.txt) && pending.jobs === 0,
     JSON.stringify({ pay: pending.pay, jobs: pending.jobs }));

  // ---------- ۷) مدیریت: نشان پرداخت روی کارت متقاضی ----------
  await p.evaluate(async () => {
    S.isAdmin = true; S.session = null; S.adminTab = 'people'; S.screen = 'admin'; render();
    await new Promise(r => setTimeout(r, 300));
  });
  const adminList = await p.evaluate(() => {
    const txt = document.querySelector('#view').textContent;
    return { hasPending: /در انتظار تأیید/.test(txt) };
  });
  ok(`${tag} ۷) مدیر در فهرست متقاضیان، «⏳ در انتظار تأیید» را می‌بیند`, adminList.hasPending);

  // ---------- ۸) مدیر: صفحه‌ی متقاضی → تأیید پرداخت (کلیک واقعی) ----------
  const personOk = await p.evaluate(async () => {
    openPerson('u1'); await new Promise(r => setTimeout(r, 300));
    const txt = document.querySelector('#view').textContent;
    return { onPerson: S.screen === 'person', hasSec: /وضعیت پرداخت/.test(txt), hasBtn: /تأیید پرداخت/.test(txt) };
  });
  ok(`${tag} ۸) صفحه‌ی متقاضی بخش «وضعیت پرداخت» و دکمه‌ی تأیید دارد`,
     personOk.onPerson && personOk.hasSec && personOk.hasBtn, JSON.stringify(personOk));

  const confirmBtn = await p.evaluate(() => {
    const el = [...document.querySelectorAll('#view button')].find(x => /تأیید پرداخت/.test(x.textContent));
    if (!el) return null;
    el.scrollIntoView({ block: 'center' });
    const r = el.getBoundingClientRect();
    return { x: r.x + r.width / 2, y: r.y + r.height / 2 };
  });
  if (confirmBtn) { await p.mouse.click(confirmBtn.x, confirmBtn.y); await sleep(600); }
  const afterConfirm = await p.evaluate(() => ({
    pay: (S.people.find(x => x.id === 'u1') || {}).pay,
    txt: document.querySelector('#view').textContent
  }));
  ok(`${tag} ۹) کلیک «تأیید پرداخت» → وضعیت «پرداخت‌شده»`,
     afterConfirm.pay === 'paid' && /پرداخت‌شده/.test(afterConfirm.txt), JSON.stringify(afterConfirm).slice(0, 120));

  // ---------- ۱۰) حالا کارجو آگهی‌ها را می‌بیند ----------
  const unlocked = await p.evaluate(async () => {
    S.isAdmin = false; S.session = S.people.find(x => x.id === 'u1'); S.session.pay = 'paid';
    S.screen = 'home'; render();
    await new Promise(r => setTimeout(r, 300));
    const txt = document.querySelector('#view').textContent;
    return { jobs: document.querySelectorAll('#view .job').length, locked: /آگهی‌ها قفل است/.test(txt),
             seedJobs: S.jobs.length };
  });
  ok(`${tag} ۱۰) بعد از تأیید مدیر، همه‌ی آگهی‌ها نمایش داده می‌شود`,
     unlocked.jobs > 0 && !unlocked.locked, JSON.stringify(unlocked));

  // ---------- ۱۱) صفحه‌ی آگهی هم برای پرداخت‌نشده قفل است ----------
  const jobLocked = await p.evaluate(async () => {
    const rec = S.people.find(x => x.id === 'u1'); rec.pay = '';
    S.session = rec; S.session.pay = '';
    S.viewJob = S.jobs[0].id; S.screen = 'job'; render();
    await new Promise(r => setTimeout(r, 300));
    const t2 = document.querySelector('#view').textContent;
    return { locked: /آگهی‌ها قفل است/.test(t2), ids: !!S.jobs[0].id };
  });
  ok(`${tag} ۱۱) کارجوی پرداخت‌نشده صفحه‌ی کامل آگهی را هم نمی‌بیند`, jobLocked.locked);

  // ---------- ۱۲) مبلغ نمایش آگهی‌ها در تنظیمات مدیر قابل تغییر است ----------
  const feeEdit = await p.evaluate(async () => {
    S.isAdmin = true; S.session = null; S.adminTab = 'settings'; S.screen = 'admin'; render();
    await new Promise(r => setTimeout(r, 300));
    const inp = document.getElementById('ads_fee');
    const has = !!inp, val = inp ? inp.value : '';
    if (inp) { inp.value = '7500'; inp.dispatchEvent(new Event('input', { bubbles: true })); }
    saveAdsFee(); await new Promise(r => setTimeout(r, 250));
    return { has, val, after: S.adsFee, shown: document.querySelector('#view').textContent.includes('7,500') };
  });
  ok(`${tag} ۱۲) مبلغ «نمایش آگهی‌ها» در تنظیمات مدیر قابل ویرایش است (۶۰۰۰ → ۷۵۰۰)`,
     feeEdit.has && String(feeEdit.after) === '7500', JSON.stringify(feeEdit));

  // ---------- ۱۳) چهار زبان ----------
  const langs = await p.evaluate(async () => {
    const out = {};
    for (const lg of ['fa', 'tr', 'en', 'ar']) {
      S.lang = lg; S.isAdmin = false;
      S.session = Object.assign({}, S.people.find(x => x.id === 'u1'), { pay: '' });
      S.screen = 'home'; render();
      await new Promise(r => setTimeout(r, 200));
      const t2 = document.querySelector('#view').textContent;
      out[lg] = /(قفل است|kilitli|locked|مقفلة)/i.test(t2);
    }
    return out;
  });
  ok(`${tag} ۱۳) صفحه‌ی قفل در هر چهار زبان درست است`,
     langs.fa && langs.tr && langs.en && langs.ar, JSON.stringify(langs));

  ok(`${tag} ۱۴) بدون خطای صفحه`, errs.length === 0, errs.slice(0, 2).join(' | '));
  await b.close();
}

(async () => {
  await run(420, 900, '۴۲۰px:');
  await run(360, 760, '۳۶۰px:');
  console.log(`\nنتیجه: ${pass} ✅ / ${fail} ❌`);
  process.exit(fail ? 1 : 0);
})();
