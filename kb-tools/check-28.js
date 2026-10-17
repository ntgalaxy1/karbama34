/* #28 — تست دمو: حذف بازنشانی، زبان عربی، صفحه‌ی کامل آگهی، بخش «پسندیده»، powered by کم‌رنگ */
const puppeteer = require('/tmp/node_modules/puppeteer-core');
const FILE = 'file:///home/user/karbama-demo.html';
const sleep = ms => new Promise(r => setTimeout(r, ms));
let pass = 0, fail = 0;
const ok = (name, cond, extra) => { cond ? pass++ : fail++; console.log(`${cond ? '✅' : '❌'} ${name}${extra && !cond ? ' → ' + extra : ''}`); };

(async () => {
  const browser = await puppeteer.launch({ executablePath: '/usr/bin/chromium', headless: 'new',
    args: ['--no-sandbox', '--disable-dev-shm-usage'] });
  const p = await browser.newPage();
  const errs = [];
  p.on('pageerror', e => errs.push(String(e)));
  await p.setViewport({ width: 420, height: 880 });
  await p.goto(FILE);
  await p.evaluate(() => { try { localStorage.clear(); } catch (e) {} });
  await p.reload();
  await p.waitForFunction(() => document.querySelector('#view button'));

  // ---------- ۱) چهار زبان ----------
  const langs = await p.evaluate(() => {
    const out = {};
    ['fa', 'tr', 'en', 'ar'].forEach(l => {
      S.lang = l; S.screen = 'lang'; render();
      out[l] = {
        dir: document.documentElement.getAttribute('dir'),
        htmlLang: document.documentElement.getAttribute('lang'),
        btn: [...document.querySelectorAll('#view .langbtn')].length,
        hasArBtn: !!document.querySelector('#view .langbtn[onclick*="\'ar\'"]')
      };
    });
    return out;
  });
  ok('۱) عربی در فهرست زبان‌ها هست', langs.ar.hasArBtn && langs.fa.btn === 4, JSON.stringify(langs.fa));
  ok('۲) عربی RTL می‌شود', langs.ar.dir === 'rtl' && langs.ar.htmlLang === 'ar', JSON.stringify(langs.ar));
  ok('۳) سه زبان دیگر هم سالم‌اند', langs.fa.dir === 'rtl' && langs.tr.dir === 'ltr' && langs.en.dir === 'ltr');

  // کلیدهای دیکشنری: هیچ ترجمه‌ی گم‌شده‌ای نباشد
  const dict = await p.evaluate(() => {
    const fk = Object.keys(I18N.fa);
    const res = {};
    ['tr', 'en', 'ar'].forEach(l => {
      const mk = fk.filter(k => !(k in I18N[l]));
      const empty = fk.filter(k => k !== 'flag' && k !== 'latin' && String(I18N[l][k] || '').trim() === '');
      const SAME_OK = ['langHint', 'lv3', 'del'];   // این‌ها در عربی و فارسی عیناً یکی‌اند (متوسط/حذف/راهنمای زبان)
      const same = fk.filter(k => !SAME_OK.includes(k) && k !== 'latin' && k !== 'tr' && k !== 'en' && k !== 'ar' && I18N[l][k] === I18N.fa[k] && /[آ-ی]/.test(String(I18N.fa[k])));
      res[l] = { missing: mk, empty, sameAsFa: same.length, total: fk.length };
    });
    return res;
  });
  ok('۴) عربی همه‌ی کلیدها را دارد', dict.ar.missing.length === 0 && dict.ar.empty.length === 0, JSON.stringify(dict.ar).slice(0, 200));
  ok('۵) tr/en هم کامل‌اند', dict.tr.missing.length === 0 && dict.en.missing.length === 0);
  ok('۶) متن‌های عربی واقعاً ترجمه شده‌اند', dict.ar.sameAsFa === 0, `${dict.ar.sameAsFa} کلید = متن فارسی`);

  // ---------- ۲) دکمه‌ی بازنشانی حذف شده ----------
  const reset = await p.evaluate(() => {
    const body = document.body.innerHTML;
    S.lang = 'fa'; S.isAdmin = true; S.adminTab = 'jobs'; S.screen = 'admin'; render();
    const jobs = document.querySelector('#view').innerHTML;
    S.adminTab = 'settings'; render();
    const set = document.querySelector('#view').innerHTML;
    return {
      inBody: /بازنشانی|resetData|Örnek verileri sıfırla/.test(body),
      inJobs: /resetData/.test(jobs), inSet: /resetData/.test(set),
      fn: typeof window.resetData, key: ('seed' in I18N.fa)
    };
  });
  ok('۷) دکمه‌ی «بازنشانی داده‌های نمونه» کاملاً رفته', !reset.inBody && reset.inJobs === false && reset.inSet === false && reset.fn === 'undefined' && reset.key === false, JSON.stringify(reset));

  // ---------- ۳) صفحه‌ی کامل آگهی ----------
  const jobPage = await p.evaluate(() => {
    S.lang = 'fa'; S.isAdmin = false; S.screen = 'home';
    S.session = S.session || { first: 'علی', last: 'احمدی', pay: 'paid' };   // #34: پرداخت تأییدشده
    render();
    const cards = [...document.querySelectorAll('#view .job')];
    const id = cards.length ? (S.jobs.find(j => S.jobs.length) || {}).id : null;
    const before = !!document.querySelector('#view .job');
    document.querySelector('#view .job').click();
    const v = document.querySelector('#view');
    const txt = v.textContent;
    return {
      cards: cards.length, before, screen: S.screen, viewJob: S.viewJob,
      title: txt.includes('صفحه‌ی آگهی'), salary: txt.includes('حقوق'),
      desc: txt.includes('توضیحات آگهی'), req: txt.includes('شرایط و مهارت‌های لازم'),
      cond: txt.includes('شرایط'), contact: txt.includes('اطلاعات تماس'),
      applyBtn: [...v.querySelectorAll('button')].some(b => /ارسال درخواست/.test(b.textContent)),
      likeBtn: [...v.querySelectorAll('button')].some(b => /پسندیدم/.test(b.textContent)),
      back: !!v.querySelector('.iconbtn'),
      sheetGone: !document.querySelector('.sheetwrap')
    };
  });
  ok('۸) کلیک روی هر آگهی، یک «صفحه» باز می‌کند (نه برگه‌ی پایین)', jobPage.screen === 'job' && jobPage.sheetGone && jobPage.viewJob, JSON.stringify(jobPage).slice(0, 160));
  ok('۹) صفحه‌ی آگهی اطلاعات کامل را نشان می‌دهد', jobPage.title && jobPage.salary && jobPage.desc && jobPage.req && jobPage.cond && jobPage.contact, JSON.stringify(jobPage).slice(0, 220));
  ok('۱۰) صفحه‌ی آگهی دکمه‌ی «ارسال درخواست» و بازگشت دارد', jobPage.applyBtn && jobPage.back);

  // ---------- ۴) بخش «پسندیده» + دکمه ----------
  const liked = await p.evaluate(async () => {
    const v = document.querySelector('#view');
    const likeBtn = [...v.querySelectorAll('button')].find(b => /پسندیدم/.test(b.textContent));
    const id = S.viewJob;
    likeBtn.click();
    const afterClick = { liked: S.liked.slice(), toast: (document.querySelector('.toast') || {}).textContent || '' };
    const btnNow = [...document.querySelector('#view').querySelectorAll('button')].find(b => /برداشتن از پسندیده/.test(b.textContent));
    // پروفایل
    S.screen = 'profile'; S.session = S.session || { first: 'علی', last: 'احمدی', phone: '05320000000', city: 'İstanbul', jobs: [], trLevel: 40, pay: 'paid' };
    render();
    const pv = document.querySelector('#view');
    const sec = pv.querySelector('#likedSec');
    const rows = pv.querySelectorAll('#likedSec .lk-row');
    // کلیک روی ردیف → صفحه‌ی آگهی
    if (rows[0]) rows[0].click();
    const rowOpened = S.screen === 'job' && S.jobFrom === 'profile';
    S.screen = 'profile'; render();
    const pv2 = document.querySelector('#view');
    const row2 = pv2.querySelectorAll('#likedSec .lk-row');
    let removed = false;
    if (row2[0]) {
      const x = row2[0].querySelector('button');
      x.click();
      removed = !S.liked.includes(id);
    }
    return {
      afterClick, hasBtnNow: !!btnNow, secTitle: sec ? sec.querySelector('.sec-title').textContent.trim() : '',
      rows: rows.length, rowOpened, removed, empty: !!pv2.querySelector('#likedSec .empty')
    };
  });
  ok('۱۱) دکمه‌ی پسندیدن کار می‌کند', liked.afterClick.liked.length > 0 && /پسندیده/.test(liked.afterClick.toast), JSON.stringify(liked.afterClick));
  ok('۱۲) حالت دکمه بعد از پسندیدن = «برداشتن از پسندیده»', liked.hasBtnNow);
  ok('۱۳) بخش «پسندیده» در پروفایل هست', /پسندیده/.test(liked.secTitle) && liked.rows === 1, liked.secTitle);
  ok('۱۴) کلیک روی ردیف پسندیده، صفحه‌ی همان آگهی را باز می‌کند', liked.rowOpened);
  ok('۱۵) حذف از پسندیده کار می‌کند و حالت خالی می‌آید', liked.removed && liked.empty, JSON.stringify(liked).slice(0, 120));

  // ---------- ۵) powered by کم‌رنگ‌تر و کم‌تر ----------
  const credit = await p.evaluate(() => {
    S.lang = 'fa'; S.screen = 'contact'; render();
    const el = document.querySelector('.kb-credit');
    const cs = getComputedStyle(el);
    // نکته: مقادیر استایل باید همین‌جا خوانده شوند (به‌محض رندر شدن صفحه‌ی دیگر، عنصر جدا می‌شود)
    const out = { text: el.textContent.trim(), size: parseFloat(cs.fontSize), weight: cs.fontWeight,
      opacity: parseFloat(cs.opacity), color: cs.color };
    S.screen = 'lang'; render();
    const langHtml = document.querySelector('#view').innerHTML;
    const m = langHtml.match(/powered by[^<]*/);
    out.welcomeHas = !!m;
    out.welcomeLine = (langHtml.split('powered by')[0].slice(-120));
    out.footerStyle = (langHtml.match(/<div style="color:rgba\(255,255,255,[^"]*"/) || [])[0] || '';
    const sp = document.querySelector('#view span[dir="ltr"]');
    out.spanStyle = sp ? sp.getAttribute('style') : '';
    return out;
  });
  ok('۱۶) متن powered by دست‌نخورده است', credit.text === 'powered by naser tavakkoli @nt.galaxy', credit.text);
  ok('۱۷) powered by کم‌رنگ‌تر و کم‌تر شده (اندازه/ضخامت/محو‌شدگی)', credit.size <= 11 && parseInt(credit.weight) <= 600 && credit.opacity < 0.8, JSON.stringify(credit));
  ok('۱۸) فوتر صفحه‌ی اول (انتخاب زبان) هم کم‌رنگ شده', credit.welcomeHas && /rgba\(255,255,255,[.0]*42\)/.test(credit.footerStyle || '') && /font-size:11px/.test(credit.spanStyle || ''), (credit.footerStyle || 'NO-STYLE') + ' || ' + credit.spanStyle);

  // ---------- ۶) صفحه‌ی کامل متقاضی هنوز کار می‌کند ----------
  const person = await p.evaluate(() => {
    S.isAdmin = true; S.adminTab = 'people'; S.screen = 'admin'; render();
    const pid = (S.people[0] || {}).id;
    openPerson(pid);
    const v = document.querySelector('#view');
    return { screen: S.screen, full: v.textContent.length > 400, hasHeader: !!v.querySelector('.kb-head') };
  });
  ok('۱۹) صفحه‌ی کامل متقاضی (مدیر) سالم است', person.screen === 'person' && person.full && person.hasHeader, JSON.stringify(person));

  ok('۲۰) بدون خطای صفحه', errs.length === 0, errs.slice(0, 2).join(' | '));
  // ---------- ۲۱) ترجمه‌ی حرفه‌ها و استان‌ها در عربی ----------
  const dataAr = await p.evaluate(() => {
    const out = {};
    S.lang = 'ar';
    out.titlesMissing = Object.keys(TITLES).filter(k => !TITLES[k].ar);
    out.firstTitle = jobTitle(Object.keys(TITLES)[0]);
    out.provs = IR_PROVINCE_NAMES.map(provLabel);
    out.faProvs = (S.lang='fa', IR_PROVINCE_NAMES.map(provLabel));
    // صفحه‌ی ثبت‌نام، مرحله‌ی ۲: برچسب استان‌ها عربی باشد و مقدارشان فارسی
    S.lang = 'ar'; S.screen='register'; S.regStep=2; startReg(); S.regStep = 2; render();
    const sel = document.querySelectorAll('#view select');
    const opts = sel[0] ? [...sel[0].querySelectorAll('option')] : [];
    out.optionValues = opts.length ? opts[1].getAttribute('value') : null;
    out.optionLabels = opts.length ? opts[1].textContent.trim() : null;
    return out;
  });
  ok('۲۱) نام عربی همه‌ی حرفه‌ها هست', dataAr.titlesMissing.length === 0 && /[\u0600-\u06FF]/.test(dataAr.firstTitle), JSON.stringify(dataAr.titlesMissing));
  ok('۲۲) استان‌های ایران در عربی ترجمه می‌شوند', dataAr.provs[0] === 'أذربيجان الشرقية' && /[\u0600-\u06FF]/.test(dataAr.provs[0]));
  ok('۲۳) استان‌ها در فارسی دست‌نخورده‌اند', dataAr.faProvs[0] === 'آذربایجان شرقی', dataAr.faProvs[0]);
  ok('۲۴) مقدار ذخیره‌شده‌ی استان همان نام استان است (برچسب عربی است)', dataAr.optionValues === 'آذربایجان شرقی' && dataAr.optionLabels === 'أذربيجان الشرقية', `${dataAr.optionValues} / ${dataAr.optionLabels}`);

  ok('۲۵) بدون خطای صفحه (پس از عربی‌سازی)', errs.length === 0, errs.slice(0, 2).join(' | '));


  console.log(`\nنتیجه: ${pass} ✅ / ${fail} ❌`);
  await browser.close();
  process.exit(fail ? 1 : 0);
})();
