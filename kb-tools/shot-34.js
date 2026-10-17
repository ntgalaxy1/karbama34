/* عکس‌های #34 — قفل آگهی‌ها تا پرداخت هزینه */
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

  const shot = async name => { await sleep(500); await page.screenshot({ path: '/home/user/kb-tools/shots/s34-' + name + '.png' }); };

  // ۱) کارجوی پرداخت‌نشده → صفحه‌ی قفل
  await page.evaluate(p => {
    S.lang = 'fa'; S.isAdmin = false;
    S.session = Object.assign({}, p, { pay: '' });
    S.people = [S.session]; S.liked = []; S.applied = [];
    S.screen = 'home'; render();
  }, PERSON);
  await shot('lock-fa');

  // ۲) بعد از «واریز کردم» → در انتظار تأیید
  await page.evaluate(() => { requestPay(); S.screen = 'home'; render(); });
  await shot('pending-fa');

  // ۳) پروفایل کارجو با کارت وضعیت پرداخت
  await page.evaluate(() => { S.screen = 'profile'; render(); });
  await shot('profile-pay-fa');

  // ۴) فهرست متقاضیان مدیر با نشان پرداخت
  await page.evaluate(async () => {
    S.isAdmin = true; S.session = null; S.adminTab = 'people'; S.screen = 'admin'; render();
  });
  await shot('admin-people-fa');

  // ۵) تنظیمات مدیر: فیلد هزینه نمایش آگهی‌ها (اسکرول تا فیلد)
  await page.evaluate(() => { S.adminTab = 'settings'; S.screen = 'admin'; render(); });
  await sleep(400);
  await page.evaluate(() => {
    const f = document.getElementById('ads_fee');
    if (f && f.scrollIntoView) f.scrollIntoView({ block: 'center' });
  });
  await shot('admin-settings-fa');

  // ۶) صفحه‌ی کامل متقاضی: تأیید پرداخت (اسکرول تا بخش پرداخت)
  await page.evaluate(() => { openPerson('u1'); render(); });
  await sleep(400);
  await page.evaluate(() => {
    const t = [...document.querySelectorAll('#view .sec-title')]
      .find(x => /وضعیت پرداخت/.test(x.textContent));
    if (t && t.scrollIntoView) t.scrollIntoView({ block: 'start' });
  });
  await shot('admin-person-fa');

  // ۷) تأیید مدیر → باز شدن آگهی‌ها
  await page.evaluate(() => {
    adminPayConfirm('u1');
    S.isAdmin = false; S.session = S.people.find(x => x.id === 'u1'); S.screen = 'home'; render();
  });
  await shot('unlocked-fa');

  // ۸) همان قفل به زبان عربی
  await page.evaluate(() => {
    S.lang = 'ar'; S.isAdmin = false;
    S.session = Object.assign({}, S.people.find(x => x.id === 'u1'), { pay: '' });
    S.screen = 'home'; render();
  });
  await shot('lock-ar');

  await browser.close();
  console.log('shots s34 ok');
})();
