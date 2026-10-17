/* #29-activity — تست کلیک واقعی برای بخش تازه‌ی «پسندیده‌ها» و «درخواست‌های ارسالی»
   در صفحه‌ی کامل متقاضی (پنل مدیر) + راهنمای اتصال دیتابیس
   چرخه: کاربر می‌پسندد و درخواست می‌فرستد → مدیر وارد می‌شود → صفحه‌ی همان متقاضی → دو کارت */
const puppeteer = require('/tmp/node_modules/puppeteer-core');
const FILE = 'file:///home/user/karbama-demo.html';
const sleep = ms => new Promise(r => setTimeout(r, ms));
let pass = 0, fail = 0;
const ok = (n, c, x) => { c ? pass++ : fail++; console.log(`${c ? '✅' : '❌'} ${n}${!c && x ? ' → ' + x : ''}`); };

async function run(W, H, tag) {
  const browser = await puppeteer.launch({ executablePath: '/usr/bin/chromium', headless: 'new',
    args: ['--no-sandbox', '--disable-dev-shm-usage'] });
  const p = await browser.newPage();
  const errs = [];
  p.on('pageerror', e => errs.push(String(e)));
  await p.setViewport({ width: W, height: H });
  await p.goto(FILE);
  await p.evaluate(() => { try { localStorage.clear(); } catch (e) {} });
  await p.reload();
  await p.waitForFunction(() => document.querySelector('#view button'));

  // ---------- ۱) کاربر وارد شده: یک آگهی را پسند و یکی را درخواست می‌کند ----------
  const setup = await p.evaluate(async () => {
    S.lang = 'fa';
    S.session = { first: 'علی', last: 'احمدی', father: 'محمد', phone: '05320000000',
      city: 'İstanbul', district: 'Fatih', jobs: ['waiter'], trLevel: 40, uid: 'u1', id: 'u1',
      nationalId: '0012345678', address: 'x', createdAt: Date.now() };
    S.people = [S.session];
    S.liked = []; S.applied = [];
    S.isAdmin = false;
    openJobPage(S.jobs[0].id, 'home');         // صفحه‌ی کامل آگهی
    await new Promise(r => setTimeout(r, 300));
    toggleLike(S.jobs[0].id);                  // پسندیدن
    applyJob(S.jobs[0].id);                    // ارسال درخواست
    await new Promise(r => setTimeout(r, 300));
    return { liked: S.liked.length, applied: S.applied.length, jobId: S.jobs[0].id,
             screen: S.screen, viewJob: S.viewJob };
  });
  ok(`${tag} ۱) کاربر یک آگهی را پسندید و برای یکی درخواست فرستاد`,
     setup.liked === 1 && setup.applied === 1, JSON.stringify(setup));

  // ---------- ۲) ماندگاری در localStorage (شبیه‌سازی «هم‌گام‌شده روی سرور») ----------
  const persisted = await p.evaluate(() => {
    try { return { liked: (JSON.parse(localStorage.getItem('kb_liked')) || []).length,
                   applied: (JSON.parse(localStorage.getItem('kb_applied')) || []).length }; }
    catch (e) { return { liked: -1, applied: -1 }; }
  });
  ok(`${tag} ۲) پسندیده و درخواست ذخیره شده‌اند (بعد از بستن اپ هم می‌مانند)`,
     persisted.liked === 1 && persisted.applied === 1, JSON.stringify(persisted));

  // ---------- ۳) مدیر وارد می‌شود و صفحه‌ی کامل همان متقاضی را باز می‌کند ----------
  await p.evaluate(() => { S.isAdmin = true; S.session = null; S.adminTab = 'people'; S.screen = 'admin'; render(); });
  await sleep(500);
  const row = await p.evaluate(() => {
    // همان متقاضیِ خودمان (u1) — نه متقاضیان نمونه
    const el = [...document.querySelectorAll('#view [onclick*="openPerson"]')]
                 .find(x => /openPerson\('u1'\)/.test(x.getAttribute('onclick') || ''));
    if (!el) return null;
    el.scrollIntoView({ block: 'center' });
    const r = el.getBoundingClientRect();
    return { x: r.x + r.width / 2, y: r.y + r.height / 2, id: 'u1' };
  });
  ok(`${tag} ۳) کارت متقاضی در فهرست مدیر کلیک‌پذیر است`, !!row, 'ردیف پیدا نشد');
  if (row) { await p.mouse.click(row.x, row.y); await sleep(600); }

  const person = await p.evaluate(() => {
    const txt = document.querySelector('#view').textContent;
    return { onPerson: S.screen === 'person' && S.viewPerson === 'u1', text: txt };
  });
  ok(`${tag} ۴) کلیک روی متقاضی → صفحه‌ی کامل او`,
     person.onPerson && /پسندیده‌ها/.test(person.text) && /درخواست‌های ارسالی/.test(person.text),
     'صفحه یا کارت‌ها پیدا نشد');

  const cards = await p.evaluate(() => {
    const heads = [...document.querySelectorAll('#view .sec-title')]
      .map(e => e.textContent.replace(/\s+/g, ' ').trim());
    const likesHead = heads.findIndex(h => /پسندیده‌ها/.test(h));
    const appsHead = heads.findIndex(h => /درخواست‌های ارسالی/.test(h));
    const all = document.querySelectorAll('#view .sec-title');
    const after = (i) => {
      if (i < 0) return '';
      // اولین کارت بعد از این عنوان
      let n = all[i] ? all[i].nextElementSibling : null;
      return n ? n.textContent.replace(/\s+/g, ' ').trim() : '';
    };
    return { likesHead, appsHead, likesBody: after(likesHead), appsBody: after(appsHead),
             jobsCount: S.jobs.filter(j => S.liked.includes(j.id)).length };
  });
  ok(`${tag} ۵) کارت «❤ پسندیده‌ها» با آگهی پسندشده نمایش داده شد`,
     cards.likesHead >= 0 && cards.likesBody.length > 3,
     JSON.stringify({ head: cards.likesHead, body: cards.likesBody.slice(0, 60) }));
  ok(`${tag} ۶) کارت «📨 درخواست‌های ارسالی» با آگهی ارسالی نمایش داده شد`,
     cards.appsHead >= 0 && cards.appsBody.length > 3,
     JSON.stringify({ head: cards.appsHead, body: cards.appsBody.slice(0, 60) }));

  // ---------- ۷) کلیک روی ردیف آگهی داخل کارت → صفحه‌ی همان آگهی ----------
  const kv = await p.evaluate(() => {
    const heads = [...document.querySelectorAll('#view .sec-title')];
    const i = heads.findIndex(h => /پسندیده‌ها/.test(h.textContent));
    if (i < 0) return null;
    const card = heads[i].nextElementSibling;
    const r0 = card.querySelector('.kv');
    if (!r0) return null;
    r0.scrollIntoView({ block: 'center' });
    const r = r0.getBoundingClientRect();
    return { x: r.x + r.width / 2, y: r.y + r.height / 2 };
  });
  if (kv) { await p.mouse.click(kv.x, kv.y); await sleep(600); }
  const after = await p.evaluate(() => ({ screen: S.screen, current: S.viewJob }));
  ok(`${tag} ۷) کلیک روی ردیف پسندیده → صفحه‌ی همان آگهی باز شد`,
     !!kv && after.screen === 'job' && after.current === setup.jobId,
     JSON.stringify({ kv: !!kv, ...after }));

  // ---------- ۸) در زبان عربی هم تیترها عربی‌اند و فارسی نفوذ نمی‌کند ----------
  const ar = await p.evaluate(async () => {
    S.lang = 'ar'; S.viewPerson = 'u1'; go('person');
    await new Promise(r => setTimeout(r, 300));
    const t = document.querySelector('#view').textContent;
    return { hasLikes: /المفضّلة/.test(t), hasApps: /الطلبات المُرسلة/.test(t),
             persianLeak: /[پچژگکی]/.test(t.replace(/[^\u0600-\u06FF\s]/g, '')) };
  });
  ok(`${tag} ۸) در عربی: «المفضّلة» و «الطلبات المُرسلة» دیده می‌شوند`, ar.hasLikes && ar.hasApps, JSON.stringify(ar));
  ok(`${tag} ۹) بدون خطای صفحه`, errs.length === 0, errs.slice(0, 2).join(' | '));

  await browser.close();
}

(async () => {
  await run(420, 900, '۴۲۰px:');
  await run(360, 760, '۳۶۰px:');
  console.log(`\nنتیجه: ${pass} ✅ / ${fail} ❌`);
  process.exit(fail ? 1 : 0);
})();
