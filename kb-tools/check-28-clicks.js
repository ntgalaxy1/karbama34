/* #28-clicks — تست با «ماوس واقعی»: پنج درخواست #28 روی دمو
   ۱) بازنشانی حذف شده  ۲) زبان عربی  ۳) کلیک روی آگهی = صفحه‌ی کامل
   ۴) بخش «پسندیده» + دکمه  ۵) powered by کم‌رنگ */
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
  await p.evaluate(() => { const b = document.querySelector('#view .langbtn'); if (b) b.click(); });
  await sleep(400);
  await p.evaluate(() => { S.lang = 'fa'; S.screen = 'home'; S.isAdmin = false;
    S.session = { first: 'علی', last: 'احمدی', phone: '05320000000', city: 'İstanbul', jobs: [], trLevel: 40, uid: 'u1', id: 'u1', pay: 'paid' };
    S.liked = []; render(); });
  await sleep(500);

  // ---------- ۱) دکمه‌ی بازنشانی: هیچ‌جا نیست (کلیک‌پذیر یا نه) ----------
  const reset = await p.evaluate(() => {
    S.isAdmin = true; S.adminTab = 'jobs'; S.screen = 'admin'; render();
    const has = (html) => /resetData|♻|بازنشانی|sıfırla|Reset sample/i.test(html);
    const r1 = { jobs: has(document.querySelector('#view').innerHTML) };
    S.adminTab = 'people'; render(); r1.people = has(document.querySelector('#view').innerHTML);
    S.adminTab = 'settings'; render(); r1.settings = has(document.querySelector('#view').innerHTML);
    S.isAdmin = false; return r1;
  });
  ok(`${tag} ۱) پنل مدیریت هیچ دکمه‌ی بازنشانی ندارد`, !reset.jobs && !reset.people && !reset.settings, JSON.stringify(reset));

  // ---------- ۲) زبان عربی با کلیک واقعی ----------
  await p.evaluate(() => { S.screen = 'lang'; render(); });
  await sleep(400);
  const arBtn = await p.evaluate(() => {
    const b = [...document.querySelectorAll('#view .langbtn')].find(x => /setLang\('ar'\)/.test(x.getAttribute('onclick') || ''));
    if (!b) return null;
    const r = b.getBoundingClientRect();
    return { x: r.x + r.width / 2, y: r.y + r.height / 2, label: b.textContent };
  });
  ok(`${tag} ۲) کارت «العربية» در صفحه‌ی زبان هست`, !!arBtn, 'پیدا نشد');
  if (arBtn) {
    await p.mouse.click(arBtn.x, arBtn.y);
    await sleep(600);
    const state = await p.evaluate(() => ({
      lang: S.lang, dir: document.documentElement.getAttribute('dir'),
      html: document.documentElement.getAttribute('lang'),
      screen: S.screen,
      arabicOnScreen: /[\u0600-\u06FF]/.test(document.querySelector('#view').textContent),
      persianLeak: /[\u067E\u0686\u0698\u06AF\u06A9\u06CC]/.test(document.querySelector('#view').textContent)
    }));
    ok(`${tag} ۳) کلیک روی عربی → برنامه عربی و راست‌به‌چپ شد`, state.lang === 'ar' && state.dir === 'rtl' && state.html === 'ar', JSON.stringify(state));
    // متن‌های فارسیِ محتوای نمونه (نام شرکت/توضیح آگهی) داده‌اند، نه رابط کاربری → جدا سنجیده می‌شوند
    const uiLeak = await p.evaluate(() => {
      const PERSIAN = /[\u067E\u0686\u0698\u06AF\u06A9\u06CC\u06F0-\u06F9]/;
      const html = document.querySelector('#view').innerHTML;
      // تگ‌های متنی رابط را برمی‌داریم و متن‌های محتوای آگهی (desc/reqs/co) را حذف می‌کنیم
      const div = document.createElement('div'); div.innerHTML = html;
      let txt = div.textContent;
      (S.jobs || []).forEach(j => {
        [j.company, j.desc, ...(j.reqs || [])].forEach(x => { if (x) txt = txt.split(x).join(' '); });
        const a = (window.AR_SAMPLE || {})[j.id]; if (a) { [a.co, a.desc, ...(a.reqs||[])].forEach(x => txt = txt.split(x).join(' ')); }
      });
      (S.people || []).forEach(p2 => { [p2.first, p2.last, p2.address, p2.district].forEach(x => { if (x) txt = txt.split(x).join(' '); }); });
      const bad = [];
      txt.split('\n').forEach(l => { const t = l.trim(); if (t && PERSIAN.test(t)) bad.push(t.slice(0, 60)); });
      return bad;
    });
    ok(`${tag} ۴) رابط کاربری در حالت عربی هیچ متن فارسیِ جامانده ندارد`, uiLeak.length === 0, JSON.stringify(uiLeak).slice(0, 160));
    // زبان را برگردان
    await p.evaluate(() => { setLang('fa'); });
    await sleep(400);
  }

  // ---------- ۳) کلیک واقعی روی آگهی = صفحه‌ی کامل ----------
  await p.evaluate(() => { S.screen = 'home'; render(); });
  await sleep(500);
  const card = await p.evaluate(() => {
    const c = document.querySelector('#view .job');
    if (!c) return null;
    // روی «عنوان شغل» کلیک می‌کنیم (نه روی کاور عکس که کلیکش برای تمام‌صفحه است)
    const h = c.querySelector('h4') || c;
    const r = h.getBoundingClientRect();
    return { x: r.x + r.width / 2, y: r.y + r.height / 2, id: (c.getAttribute('onclick') || '').match(/'([^']+)'/)[1], hit: h.tagName };
  });
  ok(`${tag} ۵) کارت آگهی در فهرست هست`, !!card);
  if (card) {
    await p.mouse.click(card.x, card.y);            // کلیک روی بدنه‌ی کارت (نه روی کاور)
    await sleep(600);
    const st = await p.evaluate(() => {
      const v = document.querySelector('#view');
      const txt = v.textContent;
      return { screen: S.screen, viewJob: S.viewJob, page: /صفحه‌ی آگهی/.test(txt),
        salary: /حقوق/.test(txt), req: /شرایط و مهارت‌های لازم/.test(txt), desc: /توضیحات آگهی/.test(txt),
        contact: /اطلاعات تماس/.test(txt), apply: [...v.querySelectorAll('button')].some(b => /ارسال درخواست/.test(b.textContent)),
        like: [...v.querySelectorAll('button')].some(b => /پسندیدم/.test(b.textContent)),
        heart: !!v.querySelector('.heart-btn'),
        overlap: (() => { const b = [...v.querySelectorAll('button')].find(x => /پسندیدم/.test(x.textContent));
          if (b && b.getBoundingClientRect().top > innerHeight) return 'off-screen';
          if (!b) return 'no-like-btn'; const r = b.getBoundingClientRect();
          const el = document.elementFromPoint(r.x + r.width / 2, r.y + r.height / 2);
          return b === el || b.contains(el) ? 'ok' : (el ? el.className || el.tagName : 'null'); })()
      };
    });
    ok(`${tag} ۶) کلیک ماوس روی آگهی → «صفحه‌ی کامل» باز شد`, st.screen === 'job' && st.viewJob === card.id, JSON.stringify(st).slice(0, 140));
    ok(`${tag} ۷) صفحه‌ی کامل همه‌ی اطلاعات را دارد (حقوق/توضیحات/شرایط/تماس/درخواست)`, st.page && st.salary && st.req && st.desc && st.contact && st.apply, JSON.stringify(st).slice(0, 200));
    ok(`${tag} ۸) دکمه‌ی «پسندیدم» + دکمه‌ی ❤ در هدر هست`, st.like && st.heart, JSON.stringify(st).slice(0, 200));
    ok(`${tag} ۸ب) چیز دیگری روی دکمه‌ها نیست`, st.overlap === 'ok' || st.overlap === 'off-screen', String(st.overlap));

    // ---------- کلیک واقعی روی ❤ هدر ----------
    const hb = await p.evaluate(() => { const b = document.querySelector('.heart-btn'); if (!b) return null;
      const r = b.getBoundingClientRect(); return { x: r.x + r.width / 2, y: r.y + r.height / 2,
        hit: (() => { const el = document.elementFromPoint(r.x + r.width / 2, r.y + r.height / 2); return !el ? 'null' : (el === b || b.contains(el) ? 'ok' : (el.className || el.tagName)); })() }; });
    ok(`${tag} ۸پ) دکمه‌ی ❤ هدر آزاد است (چیزی رویش نیست)`, !!hb && hb.hit === 'ok', JSON.stringify(hb));

    // ---------- ۴) کلیک واقعی روی «پسندیدم» ----------
    const likeBox = await p.evaluate(() => {
      const b = [...document.querySelectorAll('#view button')].find(x => /پسندیدم/.test(x.textContent));
      if (!b) return null; const r = b.getBoundingClientRect();
      return { x: r.x + r.width / 2, y: r.y + r.height / 2 };
    });
    if (likeBox) {
      if (likeBox.y > H - 5) { await p.evaluate(() => { document.querySelector('#view').scrollTop = 99999; }); await sleep(400); }
      const b2 = await p.evaluate(() => {
        const b = [...document.querySelectorAll('#view button')].find(x => /پسندیدم/.test(x.textContent));
        const r = b.getBoundingClientRect(); return { x: r.x + r.width / 2, y: r.y + r.height / 2, onScreen: r.y > 0 && r.y + r.height < innerHeight };
      });
      if (b2.onScreen) {
        await p.mouse.click(b2.x, b2.y);
        await sleep(600);
        const after = await p.evaluate(() => ({
          liked: (S.liked || []).slice(),
          btnNow: [...document.querySelectorAll('#view button')].map(b => b.textContent.trim()).filter(x => /پسندیده|برداشتن/.test(x)),
          toast: (document.querySelector('.toast') || {}).textContent || ''
        }));
        ok(`${tag} ۹) کلیک ماوس روی «پسندیدم» آگهی را پسندید`, after.liked.length === 1 && /پسندیده/.test(after.toast), JSON.stringify(after).slice(0, 160));
        ok(`${tag} ۱۰) دکمه به «برداشتن از پسندیده» تغییر کرد`, after.btnNow.some(x => /برداشتن/.test(x)), JSON.stringify(after.btnNow));
      } else {
        ok(`${tag} ۹) دکمه‌ی پسندیدم روی صفحه آمد`, false, 'خارج از کادر');
      }

      // نشان ❤ روی کارت پس از بازگشت
      await p.evaluate(() => { jobBack(); });
      await sleep(600);
      const badge = await p.evaluate(() => {
        const c = [...document.querySelectorAll('#view .job')].find(x => /b-like/.test(x.innerHTML));
        return { found: !!c, text: c ? (c.querySelector('.badge.b-like') || {}).textContent : '' };
      });
      ok(`${tag} ۱۱) نشان «❤ پسندیده» روی کارت آگهی آمد`, badge.found, JSON.stringify(badge));

      // ---------- بخش «پسندیده» در پروفایل ----------
      await p.evaluate(() => { S.screen = 'profile'; render(); });
      await sleep(500);
      const prof = await p.evaluate(() => {
        const v = document.querySelector('#view');
        const sec = v.querySelector('#likedSec');
        const row = v.querySelector('#likedSec .lk-row');
        if (!row) return { sec: !!sec, row: false };
        row.scrollIntoView({ block: 'center' });   // مثل کاربر واقعی: ردیف را وسط صفحه می‌آوریم
        const r = row.getBoundingClientRect();
        const mid = { x: r.x + r.width / 2, y: r.y + r.height / 2 };
        const hit = document.elementFromPoint(mid.x, mid.y);
        return { sec: !!sec, title: sec.querySelector('.sec-title').textContent.trim(), row: true, mid,
          hitInside: row === hit || row.contains(hit), onScreen: r.y > 0 && r.y + r.height < innerHeight };
      });
      ok(`${tag} ۱۲) بخش «پسندیده» در پروفایل با عنوان درست`, prof.sec && /پسندیده/.test(prof.title || ''), JSON.stringify(prof).slice(0, 120));
      if (prof.row && prof.onScreen) {
        ok(`${tag} ۱۳) ردیف پسندیده آزاد است (چیزی رویش نیست)`, prof.hitInside, JSON.stringify(prof).slice(0, 120));
        await p.mouse.click(prof.mid.x, prof.mid.y);
        await sleep(600);
        const opened = await p.evaluate(() => ({ screen: S.screen, from: S.jobFrom, job: S.viewJob }));
        ok(`${tag} ۱۴) کلیک ماوس روی ردیف پسندیده → صفحه‌ی همان آگهی`, opened.screen === 'job' && opened.from === 'profile', JSON.stringify(opened));
        // بازگشت و حذف از پسندیده با ماوس
        await p.evaluate(() => { jobBack(); });
        await sleep(500);
        const xBox = await p.evaluate(() => {
          const row = document.querySelector('#likedSec .lk-row'); if (!row) return null;
          row.scrollIntoView({ block: 'center' });
          const b = row.querySelector('button'); const r = b.getBoundingClientRect();
          return { x: r.x + r.width / 2, y: r.y + r.height / 2 };
        });
        if (xBox) {
          await sleep(300);
          await p.mouse.click(xBox.x, xBox.y);
          await sleep(500);
          const gone = await p.evaluate(() => ({ liked: (S.liked || []).length, empty: !!document.querySelector('#likedSec .empty') }));
          ok(`${tag} ۱۵) کلیک روی ✕، آگهی را از پسندیده‌ها برداشت`, gone.liked === 0 && gone.empty, JSON.stringify(gone));
        }
      }
    }
  }

  // ---------- ۵) powered by ----------
  const credit = await p.evaluate(() => {
    S.screen = 'contact'; render();
    const el = document.querySelector('.kb-credit'); const cs = getComputedStyle(el);
    return { text: el.textContent.trim(), size: parseFloat(cs.fontSize), weight: cs.fontWeight, opacity: parseFloat(cs.opacity),
      boxH: Math.round(el.getBoundingClientRect().height) };
  });
  ok(`${tag} ۱۶) powered by دست‌نخورده ولی کم‌رنگ‌تر و کم‌تر`,
    credit.text === 'powered by naser tavakkoli @nt.galaxy' && credit.size <= 11 && parseInt(credit.weight) <= 600 && credit.opacity < 0.8,
    JSON.stringify(credit));

  ok(`${tag} ۱۷) بدون خطای صفحه`, errs.length === 0, errs.slice(0, 2).join(' | '));
  await browser.close();
}

(async () => {
  await run(420, 880, '۴۲۰px:');
  await run(360, 780, '۳۶۰px:');
  console.log(`\nنتیجه: ${pass} ✅ / ${fail} ❌`);
  process.exit(fail ? 1 : 0);
})();
