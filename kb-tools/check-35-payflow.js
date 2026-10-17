/* #35-clicks — تست با ماوس واقعی: مسیر کامل پرداخت روی دمو
   صفحه‌ی اصلی (همان صفحه، آگهی‌ها پوشیده) → کلیک «پرداخت هزینه…» → صفحه‌ی پرداخت
   → کلیک «واریز کردم» → تأییدیه‌ی پرداخت → تأیید مدیر → باز شدن آگهی‌ها */
const puppeteer = require('/tmp/node_modules/puppeteer-core');
const FILE = 'file:///home/user/karbama-demo.html';
const sleep = ms => new Promise(r => setTimeout(r, ms));
let pass = 0, fail = 0;
const ok = (n, c, x) => { c ? pass++ : fail++; console.log(`${c ? '✅' : '❌'} ${n}${!c && x ? ' → ' + x : ''}`); };

async function clickText(p, re) {
  const box = await p.evaluate(src => {
    const rx = new RegExp(src);
    const el = [...document.querySelectorAll('#view button')].find(b => rx.test(b.textContent));
    if (!el) return null;
    el.scrollIntoView({ block: 'center' });
    const r = el.getBoundingClientRect();
    return { x: r.x + r.width / 2, y: r.y + r.height / 2, label: el.textContent.trim() };
  }, re.source);
  if (box) { await p.mouse.click(box.x, box.y); await sleep(700); }
  return box;
}

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

  // کارجوی پرداخت‌نشده روی صفحه‌ی اصلی
  await p.evaluate(() => {
    S.lang = 'fa'; S.isAdmin = false;
    S.session = { first: 'علی', last: 'رضایی', father: 'حسین', phone: '05320000000',
      city: 'İstanbul', district: 'Fatih', jobs: ['waiter'], trLevel: 45, uid: 'u1', id: 'u1',
      username: 'ali', nationalId: '0012345678', address: 'x', createdAt: Date.now(), pay: '' };
    S.people = [S.session]; S.liked = []; S.applied = [];
    S.filters = { city: '', district: '', q: '', sort: 'new', spec: '', minLang: 0 };
    S.screen = 'home'; render();
  });
  await sleep(400);

  // ---------- ۱) صفحه‌ی اصلی همان صفحه است ----------
  const home = await p.evaluate(() => {
    const v = document.querySelector('#view');
    return {
      screen: S.screen,
      search: !!v.querySelector('#q_home'),
      chips: v.querySelectorAll('.chip').length,
      title: /آگهی‌های استخدام/.test(v.textContent),
      found: /آگهی یافت شد/.test(v.textContent),
      jobCards: v.querySelectorAll('.job').length,
      lockTitle: /آگهی‌ها قفل است/.test(v.textContent),
      payBtn: [...v.querySelectorAll('button')].some(b => /پرداخت هزینه و دیدن آگهی‌ها/.test(b.textContent)),
      tabLock: /🔒/.test(document.querySelector('.tabbar').textContent)
    };
  });
  ok(`${tag} ۱) صفحه‌ی اصلی همان صفحه‌ی قبلی است (جست‌وجو + چیپ‌ها + عنوان)`,
     home.search && home.chips >= 8 && home.title && home.found, JSON.stringify(home).slice(0, 160));
  ok(`${tag} ۲) فقط بخش آگهی‌ها پنهان است (۰ کارت آگهی)`, home.jobCards === 0, 'کارت‌ها: ' + home.jobCards);
  ok(`${tag} ۳) پنل «آگهی‌ها قفل است» + دکمه‌ی پرداخت جای آگهی‌ها آمده`,
     home.lockTitle && home.payBtn && home.tabLock, JSON.stringify({ lock: home.lockTitle, btn: home.payBtn }));

  // ---------- ۴) کلیک روی دکمه‌ی پرداخت → صفحه‌ی پرداخت ----------
  const step1 = await clickText(p, /پرداخت هزینه و دیدن آگهی‌ها/);
  const pay = await p.evaluate(() => {
    const v = document.querySelector('#view');
    return { screen: S.screen, title: /پرداخت هزینه‌ی نمایش آگهی‌ها/.test(v.textContent),
      card: /1234 1234 1234 1234/.test(v.textContent), ref: /KB-\d{5}/.test(v.textContent),
      fee: /(6,000|۶,۰۰۰)/.test(v.textContent), send: [...v.querySelectorAll('button')].some(b => /واریز کردم/.test(b.textContent)),
      jobs: v.querySelectorAll('.job').length };
  });
  ok(`${tag} ۴) کلیک روی «پرداخت هزینه…» → صفحه‌ی پرداخت باز می‌شود`, !!step1 && pay.screen === 'pay' && pay.title,
     JSON.stringify({ clicked: !!step1, screen: pay.screen }));
  ok(`${tag} ۵) صفحه‌ی پرداخت: مبلغ + کارت کارگزار + کد پیگیری + دکمه‌ی «واریز کردم»`,
     pay.fee && pay.card && pay.ref && pay.send && pay.jobs === 0, JSON.stringify(pay));

  // ---------- ۶) کلیک «واریز کردم» → تأییدیه‌ی پرداخت ----------
  const step2 = await clickText(p, /واریز کردم/);
  const done = await p.evaluate(() => {
    const v = document.querySelector('#view');
    const txt = v.textContent;
    return { screen: S.screen, pay: (S.people.find(x => x.id === 'u1') || {}).pay,
      receipt: /تأییدیه پرداخت/.test(txt), reg: /پرداخت شما ثبت شد/.test(txt),
      pending: /در انتظار تأیید/.test(txt), steps: /ثبت درخواست پرداخت/.test(txt) && /تأیید توسط مدیر/.test(txt),
      recheck: [...v.querySelectorAll('button')].some(b => /بررسی مجدد وضعیت/.test(b.textContent)),
      back: [...v.querySelectorAll('button')].some(b => /بازگشت به صفحه‌ی اصلی/.test(b.textContent)),
      jobs: v.querySelectorAll('.job').length };
  });
  ok(`${tag} ۶) کلیک «واریز کردم» → تأییدیه‌ی پرداخت می‌آید`, !!step2 && done.screen === 'paid' && done.receipt && done.reg,
     JSON.stringify({ clicked: !!step2, screen: done.screen }));
  ok(`${tag} ۷) تأییدیه: وضعیت «در انتظار تأیید» + ادامه‌ی مراحل + دکمه‌ها`,
     done.pending && done.steps && done.recheck && done.back && done.jobs === 0, JSON.stringify(done).slice(0, 160));

  // ---------- ۸) بازگشت به صفحه‌ی اصلی: همچنان آگهی‌ها بسته ----------
  await clickText(p, /بازگشت به صفحه‌ی اصلی/);
  const backHome = await p.evaluate(() => {
    const v = document.querySelector('#view');
    return { screen: S.screen, jobs: v.querySelectorAll('.job').length,
      status: [...v.querySelectorAll('button')].some(b => /مشاهده‌ی وضعیت پرداخت/.test(b.textContent)) };
  });
  ok(`${tag} ۸) برگشت به صفحه‌ی اصلی: آگهی‌ها همان‌طور قفل و وضعیت پرداخت دیده می‌شود`,
     backHome.screen === 'home' && backHome.jobs === 0 && backHome.status, JSON.stringify(backHome));

  // ---------- ۹) «بررسی مجدد» تا تأیید مدیر: همان‌جا می‌ماند ----------
  await clickText(p, /مشاهده‌ی وضعیت پرداخت/);          // از صفحه‌ی اصلی → تأییدیه‌ی پرداخت
  await clickText(p, /بررسی مجدد وضعیت/);
  const recheck = await p.evaluate(() => {
    const v = document.querySelector('#view');
    return { screen: S.screen, pay: (S.people.find(x => x.id === 'u1') || {}).pay,
      receipt: /تأییدیه پرداخت/.test(v.textContent),
      pending: /در انتظار تأیید/.test(v.textContent) };
  });
  ok(`${tag} ۹) «بررسی مجدد» قبل از تأیید مدیر: همچنان «در انتظار تأیید» روی صفحه‌ی رسید`,
     recheck.receipt && recheck.pending && recheck.pay === 'pending', JSON.stringify(recheck));

  // ---------- ۱۰) مدیر تأیید می‌کند ----------
  const admin = await p.evaluate(async () => {
    S.isAdmin = true; S.session = null; S.adminTab = 'people'; S.screen = 'admin'; render();
    await new Promise(r => setTimeout(r, 300));
    const list = document.querySelector('#view').textContent;
    openPerson('u1');
    await new Promise(r => setTimeout(r, 300));
    const el = [...document.querySelectorAll('#view button')].find(b => /تأیید پرداخت/.test(b.textContent));
    if (el) el.scrollIntoView({ block: 'center' });
    return { sawPending: /در انتظار تأیید/.test(list), hasBtn: !!el };
  });
  ok(`${tag} ۱۰) مدیر در فهرست، «در انتظار تأیید» و در پرونده، دکمه‌ی «تأیید پرداخت» را می‌بیند`,
     admin.sawPending && admin.hasBtn, JSON.stringify(admin));
  const step3 = await clickText(p, /تأیید پرداخت/);
  const confirmed = await p.evaluate(() => (S.people.find(x => x.id === 'u1') || {}).pay);
  ok(`${tag} ۱۱) کلیک «تأیید پرداخت» → وضعیت «پرداخت‌شده»`, !!step3 && confirmed === 'paid', String(confirmed));

  // ---------- ۱۲) کارجو: تأییدیه‌ی «پرداخت تأیید شد» → دیدن آگهی‌ها ----------
  await p.evaluate(() => {
    S.isAdmin = false; S.session = S.people.find(x => x.id === 'u1'); S.screen = 'paid'; render();
  });
  await sleep(300);
  const paidView = await p.evaluate(() => {
    const v = document.querySelector('#view');
    return { title: /پرداخت تأیید شد/.test(v.textContent),
      open: [...v.querySelectorAll('button')].some(b => /دیدن آگهی‌ها/.test(b.textContent)) };
  });
  ok(`${tag} ۱۲) تأییدیه‌ی نهایی: «پرداخت تأیید شد» + دکمه‌ی «دیدن آگهی‌ها»`,
     paidView.title && paidView.open, JSON.stringify(paidView));
  await clickText(p, /دیدن آگهی‌ها/);
  const open = await p.evaluate(() => {
    const v = document.querySelector('#view');
    const tb = document.querySelector('.tabbar');
    return { screen: S.screen, jobs: v.querySelectorAll('.job').length,
      locked: /آگهی‌ها قفل است/.test(v.textContent),
      search: !!v.querySelector('#q_home'),
      tab: tb ? tb.textContent.replace(/\s+/g, ' ').trim() : '' };
  });
  ok(`${tag} ۱۳) کلیک «دیدن آگهی‌ها» → همان صفحه‌ی اصلی با آگهی‌های باز`,
     open.screen === 'home' && open.jobs > 0 && !open.locked && open.search,
     JSON.stringify({ screen: open.screen, jobs: open.jobs }));
  ok(`${tag} ۱۴) قفل تب پایین برداشته شد`, open.tab.length > 0 && !/🔒/.test(open.tab), open.tab);

  // ---------- ۱۳) چهار زبان ----------
  const langs = await p.evaluate(async () => {
    const out = {};
    for (const lg of ['fa', 'tr', 'en', 'ar']) {
      S.lang = lg;
      S.people = S.people.map(x => Object.assign({}, x, { pay: '' }));
      S.session = S.people[0];
      S.screen = 'home'; render();
      await new Promise(r => setTimeout(r, 200));
      const txt = document.querySelector('#view').textContent;
      const payPg = (S.screen = 'pay', render(), document.querySelector('#view').textContent);
      out[lg] = /(قفل است|kilitli|locked|مقفلة)/i.test(txt) && /(واریز کردم|Yatırdım|I transferred|قمت بالتحويل)/.test(payPg);
    }
    S.lang = 'fa';
    return out;
  });
  ok(`${tag} ۱۵) مسیر قفل→پرداخت در هر چهار زبان درست است`,
     langs.fa && langs.tr && langs.en && langs.ar, JSON.stringify(langs));

  ok(`${tag} ۱۶) بدون خطای صفحه`, errs.length === 0, errs.slice(0, 2).join(' | '));
  await b.close();
}

(async () => {
  await run(420, 900, '۴۲۰px:');
  await run(360, 760, '۳۶۰px:');
  console.log(`\nنتیجه: ${pass} ✅ / ${fail} ❌`);
  process.exit(fail ? 1 : 0);
})();
