/* عکس‌های #35 — صفحه‌ی اصلی با آگهی‌های پوشیده → صفحه‌ی پرداخت → تأییدیه پرداخت → آگهی‌های باز */
const puppeteer = require('/tmp/node_modules/puppeteer-core');
const fs = require('fs');
const sleep = ms => new Promise(r => setTimeout(r, ms));
const PERSON = { first: 'علی', last: 'رضایی', father: 'حسین', phone: '05320000000',
  city: 'İstanbul', district: 'Fatih', jobs: ['waiter'], trLevel: 45, uid: 'u1', id: 'u1',
  username: 'ali', nationalId: '0012345678', address: 'İstanbul, Fatih', createdAt: Date.now() };

(async () => {
  const browser = await puppeteer.launch({ executablePath: '/usr/bin/chromium', headless: 'new',
    args: ['--no-sandbox', '--disable-dev-shm-usage'] });
  fs.mkdirSync('/home/user/kb-tools/shots', { recursive: true });
  const page = await browser.newPage();
  await page.setViewport({ width: 420, height: 900, deviceScaleFactor: 2 });
  await page.goto('file:///home/user/karbama-demo.html');
  await page.evaluate(() => { try { localStorage.clear(); } catch (e) {} });
  await page.reload();
  await page.waitForFunction(() => document.querySelector('#view button'));
  const shot = async name => { await sleep(500); await page.screenshot({ path: '/home/user/kb-tools/shots/s35-' + name + '.png' }); };

  // ۱) صفحه‌ی اصلی: همان صفحه، فقط بخش آگهی‌ها پوشیده
  await page.evaluate(p => {
    S.lang = 'fa'; S.isAdmin = false;
    S.session = Object.assign({}, p, { pay: '' });
    S.people = [S.session]; S.liked = []; S.applied = [];
    S.filters = { city: '', district: '', q: '', sort: 'new', spec: '', minLang: 0 };
    S.screen = 'home'; render();
  }, PERSON);
  await shot('home-locked-fa');

  // ۲) صفحه‌ی پرداخت
  await page.evaluate(() => { go('pay'); });
  await shot('pay-fa');

  // ۳) تأییدیه‌ی پرداخت (در انتظار تأیید مدیر)
  await page.evaluate(() => { requestPay(); });
  await shot('receipt-fa');

  // ۴) برگشت به صفحه‌ی اصلی: آگهی‌ها همچنان پوشیده + وضعیت پرداخت
  await page.evaluate(() => { go('home'); });
  await shot('home-pending-fa');

  // ۵) پروفایل کارجو: کارت وضعیت پرداخت
  await page.evaluate(() => { S.screen = 'profile'; render(); });
  await shot('profile-pay-fa');

  // ۶) مدیر: فهرست متقاضیان با نشان پرداخت
  await page.evaluate(() => { S.isAdmin = true; S.session = null; S.adminTab = 'people'; S.screen = 'admin'; render(); });
  await shot('admin-people-fa');

  // ۷) مدیر: صفحه‌ی متقاضی (بخش وضعیت پرداخت)
  await page.evaluate(() => { openPerson('u1'); });
  await sleep(400);
  await page.evaluate(() => {
    const t = [...document.querySelectorAll('#view .sec-title')].find(x => /وضعیت پرداخت/.test(x.textContent));
    if (t && t.scrollIntoView) t.scrollIntoView({ block: 'start' });
  });
  await shot('admin-person-fa');

  // ۸) تأیید مدیر → آگهی‌های باز روی همان صفحه‌ی اصلی
  await page.evaluate(() => {
    adminPayConfirm('u1');
    S.isAdmin = false; S.session = S.people.find(x => x.id === 'u1'); S.screen = 'home'; render();
  });
  await shot('home-unlocked-fa');

  // ۹) عربی: صفحه‌ی اصلی با آگهی‌های پوشیده
  await page.evaluate(() => {
    S.lang = 'ar'; S.isAdmin = false;
    S.people = S.people.map(x => Object.assign({}, x, { pay: '' }));
    S.session = S.people[0];
    S.screen = 'home'; render();
  });
  await shot('home-locked-ar');

  await browser.close();
  console.log('shots s35 ok');
})();
