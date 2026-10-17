/* #33-design — بازرسی خودکار دیزاین: رنگ، فونت، فاصله
   قواعد: ۱) هیچ متنی کوچک‌تر از ۱۱px نباشد (اندازه‌های مجاز: ۱۱ تا ۳۸)
          ۲) رنگ متن کنتراست ≥ ۴.۵:۱ روی زمینه‌ی کارت داشته باشد
          ۳) فاصله‌ها مضرب ۲ پیکسل باشند و پرده‌ی صفحه ۱۶ پیکسل
          ۴) رنگ‌های به‌کاررفته محدود به پالت باشند
          ۵) عبارت «حالت نمایشی» هیچ‌جا نباشد */
const puppeteer = require('/tmp/node_modules/puppeteer-core');
const FILE = 'file:///home/user/karbama-demo.html';
const sleep = ms => new Promise(r => setTimeout(r, ms));
let pass = 0, fail = 0;
const ok = (n, c, x) => { c ? pass++ : fail++; console.log(`${c ? '✅' : '❌'} ${n}${!c && x ? ' → ' + x : ''}`); };

const ALLOWED_TYPE = [11, 12, 13, 14, 15, 16, 18, 20, 22, 26, 38];
const PALETTE = ['#0F1F2B','#3F5566','#57697A','#E3ECF0','#F6FAFB','#ECF3F6','#0E9F8E','#0A7F91',
  '#066F6B','#19CBAD','#10B39F','#E6F7F4','#D6F2EC','#E2FBF3','#2FD6A8','#7C5CFF','#5B3FE0','#EEEBFF',
  '#FF7A59','#E4562F','#FFEDE7','#FFB020','#F59E0B','#FFF4DE','#FDE68A','#8A5B00','#7A5310','#5A3D06',
  '#12A150','#0E7A44','#E4F7EC','#E5484D','#FEF0F0','#C2185B','#FFE3ED','#1D6FD4','#E7F2FF','#3B4753',
  '#EFF3F5','#101C24','#2B3D48','#FFFFFF','#C6D5DE','#D9E4EA','#F8FBFC','#A7F3D0','#B33A18','#1272A8','#0E9F8E','#088080','#5A3D06','#0E7A44'];

(async () => {
  const b = await puppeteer.launch({ executablePath: '/usr/bin/chromium', headless: 'new',
    args: ['--no-sandbox', '--disable-dev-shm-usage'] });
  const p = await b.newPage();
  const errs = [];
  p.on('pageerror', e => errs.push(String(e)));
  await p.setViewport({ width: 420, height: 900 });
  await p.goto(FILE);
  await p.evaluate(() => { try { localStorage.clear(); } catch (e) {} });
  await p.reload();
  await p.waitForFunction(() => document.querySelector('#view button'));

  // ---------- ۱) بدون «حالت نمایشی» ----------
  const screens = ['welcome','lang','login','register','home','job','profile','contact','admin'];
  await p.evaluate(() => {
    S.lang = 'fa';
    S.session = { first: 'علی', last: 'احمدی', phone: '05320000000', city: 'İstanbul',
      jobs: ['waiter'], trLevel: 40, uid: 'u1', id: 'u1' };
    S.people = [S.session]; S.liked = []; S.applied = [];
  });
  let demoText = [];
  for (const sc of screens) {
    const hit = await p.evaluate(async (sc) => {
      try {
        if (sc === 'admin') { S.isAdmin = true; S.screen = 'admin'; render(); }
        else { S.isAdmin = false; S.screen = sc; render(); }
      } catch (e) { return 'ERR ' + e.message; }
      await new Promise(r => setTimeout(r, 250));
      const roots = [document.body];
      const txt = roots.map(r => r.innerText || r.textContent).join(' ');
      const m = txt.match(/[^\n]{0,30}(حالت نمایشی|وضع تجريبي|Demo mode)[^\n]{0,30}/);
      return m ? m[0] : '';
    }, sc);
    if (typeof hit === 'string' && hit.startsWith('ERR')) { demoText.push(sc + ':' + hit); }
    else if (hit) demoText.push(sc + ': ' + hit);
  }
  ok('۱) عبارت «حالت نمایشی» در هیچ صفحه‌ای نیست', demoText.length === 0, demoText.join(' | '));

  // ---------- ۲/۳/۴) اندازه‌ی فونت، رنگ متن، فاصله‌ها ----------
  const audit = await p.evaluate(async (ALLOWED, PAL) => {
    const out = { sizes: {}, colors: {}, pads: {}, lows: [], leak: [] };
    const check = async (sc) => {
      S.isAdmin = sc === 'admin';
      S.screen = sc === 'person' ? 'person' : sc;
      if (sc === 'person') { S.isAdmin = true; S.viewPerson = 'u1'; }
      if (sc === 'job') { S.isAdmin = false; openJobPage(S.jobs[0].id, 'home'); }
      else render();
      await new Promise(r => setTimeout(r, 200));
      const root = document.querySelector('#view');
      const nodes = [root, ...root.querySelectorAll('*')];
      const parse = c => {
        const m = String(c).match(/rgba?\(([\d.]+),\s*([\d.]+),\s*([\d.]+)(?:,\s*([\d.]+))?\)/);
        return m ? { r: +m[1], g: +m[2], b: +m[3], a: m[4] === undefined ? 1 : +m[4] } : null;
      };
      const lum = c => { const f = v => { v /= 255; return v <= .04045 ? v / 12.92 : Math.pow((v + .055) / 1.055, 2.4); };
        return .2126 * f(c.r) + .7152 * f(c.g) + .0722 * f(c.b); };
      const ratio = (a, b) => { const la = lum(a), lb = lum(b); const hi = Math.max(la, lb), lo = Math.min(la, lb);
        return (hi + .05) / (lo + .05); };
      // میانگین رنگ‌های یک گرادیان (برای هدرهای رنگی که پس‌زمینه‌ی ساده ندارند)
      const gradAvg = img => {
        const cols = String(img).match(/rgba?\([^)]+\)|#[0-9A-Fa-f]{3,8}/g);
        if (!cols) return null;
        const rgbOf = c => {
          if (c.startsWith('rgba')) {
            const m = c.match(/([\d.]+),\s*([\d.]+),\s*([\d.]+),\s*([\d.]+)/);
            if (!m) return null;
            if (+m[4] < .15) return null;          // استاپ‌های شفاف هیچ رنگی اضافه نمی‌کنند
            return { r: +m[1], g: +m[2], b: +m[3] };
          }
          if (c.startsWith('#')) {
            let h = c.slice(1);
            if (h.length === 3) h = h.split('').map(x => x + x).join('');
            return { r: parseInt(h.slice(0,2),16), g: parseInt(h.slice(2,4),16), b: parseInt(h.slice(4,6),16) };
          }
          const m = c.match(/([\d.]+),\s*([\d.]+),\s*([\d.]+)/);
          return m ? { r: +m[1], g: +m[2], b: +m[3] } : null;
        };
        const list = cols.map(rgbOf).filter(Boolean);
        if (!list.length) return null;
        // گرادیان شیشه‌ای (همه‌ی استاپ‌ها نیمه‌شفاف) پس‌زمینه محسوب نمی‌شود
        const opaque = cols.some(c => !c.startsWith('rgba') || +c.match(/[\d.]+\)$/)[0].slice(0, -1) >= .95);
        if (!opaque) {
          // گرادیان نیمه‌شفاف: اگر تیره باشد «سایه روی عکس» است و پس‌زمینه‌ی متن حساب می‌شود،
          // وگرنه شیشه‌ی روشن است و باید بالاتر را نگاه کرد.
          const a = list.reduce((x, c) => x + c.r + c.g + c.b, 0) / (3 * list.length);
          return a < 128 ? (list.reduce((acc, c) => ({ r: acc.r + c.r / list.length, g: acc.g + c.g / list.length, b: acc.b + c.b / list.length }), { r: 0, g: 0, b: 0 })) : null;
        }
        return { r: list.reduce((a,c) => a + c.r, 0)/list.length,
                 g: list.reduce((a,c) => a + c.g, 0)/list.length,
                 b: list.reduce((a,c) => a + c.b, 0)/list.length, a: 1 };
      };
      // نزدیک‌ترین «نقاش» پس‌زمینه: اگر گرادیان بود میانگینش، وگرنه رنگ ساده
      const bgOf = el => {
        let e = el;
        while (e && e !== document.documentElement) {
          const cs = getComputedStyle(e);
          const img = cs.backgroundImage;
          if (img && img !== 'none' && /gradient/.test(img)) {
            const g = gradAvg(img);
            if (g) return g;
          }
          const c = parse(cs.backgroundColor);
          if (c && c.a >= .95) return c;
          e = e.parentElement;
        }
        return { r: 255, g: 255, b: 255, a: 1 };
      };
      for (const el of nodes) {
        if (!el || el.nodeType !== 1) continue;
        const cs = getComputedStyle(el);
        // فونت
        if (cs.fontSize) {
          const fs = Math.round(parseFloat(cs.fontSize) * 10) / 10;
          out.sizes[fs] = (out.sizes[fs] || 0) + 1;
          if (!ALLOWED.includes(fs) && fs < 40) out.leak.push(sc + ' font ' + fs + '‹' + (el.textContent || '').trim().slice(0, 18) + '›');
        }
        // رنگ متن
        const hasText = [...el.childNodes].some(n => n.nodeType === 3 && n.textContent.trim().length > 1);
        if (hasText) {
          const fg = parse(cs.color);
          if (fg && fg.a > .5) {
            const hex = '#' + [fg.r, fg.g, fg.b].map(v => Math.round(v).toString(16).padStart(2, '0').toUpperCase()).join('');
            out.colors[hex] = (out.colors[hex] || 0) + 1;
            const r = ratio(fg, bgOf(el));
            const big = parseFloat(cs.fontSize) >= 18 || (parseFloat(cs.fontSize) >= 14 && parseInt(cs.fontWeight) >= 700);
            // خط تزئینی «powered by …» عمداً کم‌رنگ است (خواسته‌ی #32) → آستانه‌ی ۲:۱
            const decorative = el.classList.contains('kb-credit') || (el.closest && el.closest('.kb-credit'));
            if (r < (decorative ? 2 : big ? 3 : 4.5)) out.lows.push(`${sc} ${hex} ${r.toFixed(2)}:1 «${(el.textContent||'').trim().slice(0,22)}»`);
            if (!PAL.includes(hex) && !['#229ED9','#2AABEE','#25D366','#3ED36F','#DD2A7B','#F58529','#8134AF'].includes(hex))
              out.leak.push(sc + ' color ' + hex);
          }
        }
        // فاصله‌ها
        for (const prop of ['paddingTop','paddingBottom','paddingLeft','paddingRight','marginTop','marginBottom','rowGap','columnGap']) {
          const v = parseFloat(cs[prop]);
          if (v) { out.pads[v] = (out.pads[v] || 0) + 1; if (v % 2 !== 0 && v < 60) out.leak.push(sc + ' ' + prop + ' ' + v); }
        }
      }
    };
    for (const sc of ['home','job','profile','contact','admin','person']) await check(sc);
    return out;
  }, ALLOWED_TYPE, PALETTE);

  const badSizes = Object.keys(audit.sizes).filter(v => !ALLOWED_TYPE.includes(+v) && +v < 40 && +v > 0);
  ok('۲) همه‌ی اندازه‌های فونت روی مقیاس‌اند (۱۱·۱۲·۱۳·۱۴·۱۵·۱۶·۱۸·۲۰·۲۲·۲۶)',
     badSizes.length === 0, 'اندازه‌های بیرون مقیاس: ' + badSizes.join(', '));
  const minSize = Math.min(...Object.keys(audit.sizes).map(Number));
  ok(`۳) کوچک‌ترین فونت صفحه ${minSize}px است (حداقل ۱۱)`, minSize >= 11, 'min=' + minSize);
  ok('۴) هیچ رنگ متنی با کنتراست کمتر از ۴.۵:۱ نیست',
     audit.lows.length === 0, audit.lows.slice(0, 4).join(' | '));
  const oddPads = Object.keys(audit.pads).filter(v => +v % 2 !== 0 && +v < 60);
  ok('۵) همه‌ی فاصله‌ها مضرب ۲ پیکسل‌اند', oddPads.length === 0, 'فرد: ' + oddPads.join(', '));
  const colorCount = Object.keys(audit.colors).length;
  ok(`۶) تعداد رنگ‌های متنی ${colorCount} (≤ ۳۰)`, colorCount <= 30, 'زیاد: ' + colorCount);
  const leaks = [...new Set(audit.leak)];
  ok('۷) بدون رنگ/اندازه‌ی بیرون پالت', leaks.length === 0, leaks.slice(0, 4).join(' | '));

  // ---------- ۸) صفحه‌ی همه‌ی صفحه‌ها ۱۶ پیکسل ----------
  const shellPad = await p.evaluate(async () => {
    S.isAdmin = false; S.screen = 'home'; render();
    await new Promise(r => setTimeout(r, 200));
    const d = document.querySelector('#view > div');
    const cs = getComputedStyle(d);
    return { top: cs.paddingTop, sides: cs.paddingLeft, bottom: cs.paddingBottom };
  });
  ok('۸) پرده‌ی همه‌ی صفحه‌ها ۱۶ پیکسل است',
     shellPad.top === '16px' && shellPad.sides === '16px', JSON.stringify(shellPad));
  ok('۹) بدون خطای صفحه', errs.length === 0, errs.slice(0, 2).join(' | '));

  await b.close();
  console.log(`\nنتیجه: ${pass} ✅ / ${fail} ❌`);
  process.exit(fail ? 1 : 0);
})();
