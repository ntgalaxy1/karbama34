// #25 — تست مرورگری: ۱) سه کارت آماری زیر دکمه‌های تب  ۲) دکمه‌ی خروج شیک پنل مدیریت
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
  await p.evaluate(() => { S.lang = 'fa'; S.isAdmin = true; S.adminTab = 'jobs'; S.screen = 'admin'; render(); });
  await sleep(500);

  // ---------- ۱) ترتیب: نوار بالای مدیریت → دکمه‌های تب → سه کارت آماری ----------
  let st = await p.evaluate(() => {
    const v = document.querySelector('#view');
    const appbar = v.querySelector('.appbar');
    const tabs = v.querySelector('.tabs');
    const stats = v.querySelector('.stats');
    const y = el => el ? Math.round(el.getBoundingClientRect().top) : null;
    return { has: !!(appbar && tabs && stats), appbarY: y(appbar), tabsY: y(tabs), statsY: y(stats),
             statsAbove: y(stats) < y(tabs), statsBelow: y(stats) > y(tabs),
             cards: stats ? stats.querySelectorAll('.card').length : 0,
             labels: stats ? [...stats.querySelectorAll('.small')].map(x => x.textContent.trim()) : [] };
  });
  ok('۱) در تب «آگهی‌ها» هر سه بخش سرجایشان هستند', st.has && st.cards === 3, JSON.stringify(st));
  ok('۲) سه کارت آماری **زیر** دکمه‌های تب آمده‌اند (یک پله پایین‌تر)', st.statsBelow === true && st.statsAbove === false,
    `tabsY=${st.tabsY} statsY=${st.statsY}`);
  ok('۳) برچسب‌ها همان سه مورد «آگهی فعال / متقاضی / جدید»', st.labels.some(t => t.includes('آگهی')) && st.labels.some(t => t.includes('متقاضی')) && st.labels.some(t => t.includes('جدید')), JSON.stringify(st.labels));
  ok('۴) فاصله‌ی منطقی بین تب‌ها و کارت‌ها', (st.statsY - st.tabsY) > 30 && (st.statsY - st.tabsY) < 90, (st.statsY - st.tabsY) + 'px');

  // کارت‌ها هنوز کلیک‌پذیرند
  st = await p.evaluate(() => {
    const stats = document.querySelector('#view .stats');
    const cards = [...stats.querySelectorAll('.card')];
    cards[2].click();
    return { tab: S.adminTab, filter: S.adminPeopleStatus };
  });
  ok('۵) کارت «جدید» هنوز به فهرست فیلترشده می‌برد', st.tab === 'people' && st.filter === 'new', JSON.stringify(st));

  // ---------- ۲) دکمه‌ی خروج شیک ----------
  await p.evaluate(() => { S.adminTab = 'jobs'; render(); });
  await sleep(400);
  st = await p.evaluate(() => {
    const btn = document.querySelector('#view .admin-exit');
    if (!btn) return { missing: true };
    const cs = getComputedStyle(btn);
    const appbar = btn.closest('.appbar');
    const svg = btn.querySelector('svg');
    return { label: btn.textContent.trim(), hasSvg: !!svg,
             radius: cs.borderRadius, bg: cs.backgroundImage.slice(0, 20), border: cs.borderTopColor,
             shadow: cs.boxShadow !== 'none', inAppbar: !!appbar, h: Math.round(btn.getBoundingClientRect().height),
             iconName: svg ? (svg.getAttribute('viewBox') || '') : '', hasEmoji: /[⏻🔌🚪]/.test(btn.textContent) };
  });
  ok('۶) دکمه‌ی خروج با کلاس تازه در نوار بالای مدیریت هست', !st.missing && st.inAppbar, JSON.stringify(st));
  ok('۷) آیکون خروج به‌صورت SVG است (نه ایموجی ⏻)', st.hasSvg && !st.hasEmoji, JSON.stringify(st));
  ok('۸) متن «خروج از حساب» روی دکمه نوشته شده', /خروج/.test(st.label), st.label);
  ok('۹) ظاهر شیک: شیشه‌ای + گرد + قاب روشن + سایه', /gradient/.test(st.bg) && parseFloat(st.radius) >= 12 && st.shadow, JSON.stringify(st));

  // در تب متقاضیان و تنظیمات هم همان دکمه باشد
  st = await p.evaluate(() => {
    const out = {};
    for (const tab of ['people', 'settings']) {
      S.adminTab = tab; render();
      const btn = document.querySelector('#view .admin-exit');
      out[tab] = !!btn && !!btn.querySelector('svg');
    }
    return out;
  });
  ok('۱۰) دکمه‌ی خروج در تب‌های «متقاضیان» و «تنظیمات» هم یکسان است', st.people && st.settings, JSON.stringify(st));

  // کارکرد دکمه
  await p.evaluate(() => { S.adminTab = 'jobs'; render(); document.querySelector('#view .admin-exit').click(); });
  await sleep(500);
  st = await p.evaluate(() => ({ admin: S.isAdmin, screen: S.screen }));
  ok('۱۱) کلیک روی دکمه → خروج از پنل مدیریت', st.admin === false && st.screen === 'login', JSON.stringify(st));

  // سه زبان
  st = await p.evaluate(() => {
    const out = {};
    for (const lang of ['fa', 'tr', 'en']) {
      S.lang = lang; S.isAdmin = true; S.adminTab = 'jobs'; S.screen = 'admin'; render();
      const v = document.querySelector('#view');
      const tabs = v.querySelector('.tabs'), stats = v.querySelector('.stats');
      out[lang] = { label: v.querySelector('.admin-exit').textContent.trim(),
                    below: stats.getBoundingClientRect().top > tabs.getBoundingClientRect().top };
    }
    return out;
  });
  ok('۱۲) در ترکی و انگلیسی هم ترتیب و دکمه درست است', st.tr.below && st.en.below && /Çıkış/.test(st.tr.label) && /Log out/.test(st.en.label), JSON.stringify(st));

  // تمیزکاری + اسکرین‌شات
  await p.evaluate(() => { S.lang = 'fa'; S.adminTab = 'jobs'; render(); });
  await sleep(400);
  await p.evaluate(() => { try { S.toast = null; document.querySelectorAll('.toast').forEach(e => e.remove()); } catch (e) {} });
  await p.screenshot({ path: '/tmp/e25-admin-top.png' });
  await p.evaluate(() => { S.adminTab = 'people'; render(); });
  await sleep(400);
  await p.evaluate(() => { try { S.toast = null; document.querySelectorAll('.toast').forEach(e => e.remove()); } catch (e) {} });
  await p.screenshot({ path: '/tmp/e25-admin-people.png' });

  ok('۱۳) بدون خطای صفحه', errs.length === 0, errs.slice(0, 2).join(' | '));
  console.log(`\nنتیجه: ${pass} ✅ / ${fail} ❌`);
  await b.close();
  process.exit(fail ? 1 : 0);
})();
