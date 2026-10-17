// #23 — تست مرورگری: رنگ نوک‌مدادی تعهدنامه · کادرهای یکسان تنظیمات مدیر · اصلاح دکمه‌ی چشم
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

  // ---------- ۱) رنگ نوک‌مدادی متن الزامی‌بودن تعهدنامه ----------
  let st = await p.evaluate(() => {
    startReg();
    S.form = { jobs: [], trLevel: 40, jobQ: '', cc: 90, first: 'سارا', last: 'م', father: 'اکبر', phone: '05321234567',
               idPhoto: { id: 'p1', data: 'data:image/png;base64,AAA' }, bodyPhoto: { id: 'p2', data: 'data:image/png;base64,AAA' } };
    S.undDraft = { signed: false, sig: null, payerCard: '', fee: 6000, feeText: '6000' };
    S.screen = 'undertaking'; render();
    const bar = [...document.querySelectorAll('#view span')].find(x => x.textContent.includes('خواندن و امضای تعهدنامه'));
    const cs = bar ? getComputedStyle(bar) : null;
    return { txt: bar ? bar.textContent.trim() : null, color: cs ? cs.color : null, weight: cs ? cs.fontWeight : null };
  });
  ok('۱) متن «خواندن و امضای تعهدنامه الزامی است» در صفحه‌ی تعهدنامه هست', !!st.txt, JSON.stringify(st));
  ok('۲) رنگش نوک‌مدادی است (طوسی تیره، نه آبی کم‌رنگ)', st.color === 'rgb(59, 71, 83)', st.color);
  ok('۳) پررنگ (خوانا) نوشته شده', String(st.weight) === '800', st.weight);
  await p.screenshot({ path: '/tmp/a23-und-note.png' });
  st = await p.evaluate(() => {
    go('register'); S.regStep = 3; S.formErr = { und: true }; render();
    const e = [...document.querySelectorAll('#view div')].find(x => x.textContent.trim().startsWith('✏️') && x.textContent.includes('خواندن و امضا'));
    return e ? { color: getComputedStyle(e).color, bg: getComputedStyle(e).backgroundColor } : null;
  });
  ok('۴) پیام خطای همان متن هم نوک‌مدادی است (نه سرخ)', !!st && st.color === 'rgb(59, 71, 83)', JSON.stringify(st));
  await p.screenshot({ path: '/tmp/a23-und-error.png' });

  // ---------- ۲) تنظیمات مدیر: کادرها یکسان + دکمه‌ی چشم ----------
  await p.evaluate(() => { S.lang = 'fa'; S.isAdmin = true; S.adminTab = 'settings'; S.screen = 'admin'; S.twoFA = { on: false, secret: '', recovery: '', pending: false }; render(); });
  await sleep(400);
  st = await p.evaluate(() => {
    const ins = [...document.querySelectorAll('#view .adm-inp')];
    const styles = ins.map(i => {
      const cs = getComputedStyle(i);
      return { id: i.id || i.getAttribute('placeholder') || 'x', h: cs.height, br: cs.borderRadius, fs: cs.fontSize, fw: cs.fontWeight, bd: cs.borderWidth, padL: cs.paddingLeft, padR: cs.paddingRight, bg: cs.backgroundImage.slice(0, 30) };
    });
    return { n: ins.length, styles, allClass: ins.every(i => i.classList.contains('adm-inp')) };
  });
  ok('۵) همه‌ی کادرهای تنظیمات کلاس یکسان دارند', st.n >= 3 && st.allClass, JSON.stringify(st.styles.map(s => s.id)));
  const keys = ['h', 'br', 'fs', 'fw', 'bd'];
  const uniq = k => [...new Set(st.styles.map(s => s[k]))];
  ok('۶) قد همه‌ی کادرها یکی است (۵۲px)', uniq('h').length === 1 && uniq('h')[0] === '52px', uniq('h').join('/'));
  ok('۷) گوشه‌ی همه یکی است (۱۶px)', uniq('br').length === 1 && uniq('br')[0] === '16px', uniq('br').join('/'));
  ok('۸) فونت و ضخامت و قاب همه یکسان', uniq('fs').length === 1 && uniq('fw').length === 1 && uniq('bd').length === 1,
    JSON.stringify({ fs: uniq('fs'), fw: uniq('fw'), bd: uniq('bd') }));

  // دکمه‌ی چشم: سمت راستِ داخل کادر، با فضای امن از متن
  st = await p.evaluate(() => {
    const w = document.querySelector('#view .pw-wrap');
    const i = w.querySelector('input'); const eye = w.querySelector('.pw-eye');
    const ir = i.getBoundingClientRect(), er = eye.getBoundingClientRect();
    const cs = getComputedStyle(i);
    return { inside: er.right <= ir.right + 1 && er.left >= ir.left - 1 && er.top >= ir.top - 1 && er.bottom <= ir.bottom + 1,
             padR: cs.paddingRight, padL: cs.paddingLeft, gap: Math.round(ir.right - er.right),
             eyeW: Math.round(er.width), svg: !!eye.querySelector('svg') };
  });
  ok('۹) فضای امن سمت راست کادر رزرو شده (۵۲px)', st.padR === '52px', st.padR);
  ok('۱۰) آیکون چشم داخل کادر، سمت راست و با فاصله‌ی ۹px از لبه', st.inside && st.gap === 9, JSON.stringify(st));
  ok('۱۱) جای متن کاملاً از دکمه جدا است (۵۲ ≥ ۹+۳۲+۸=۴۹)', parseFloat(st.padR) >= st.gap + st.eyeW + 8, JSON.stringify(st));
  ok('۱۲) آیکون چشم به‌صورت SVG است (نه ایموجی که جابه‌جا بیفتد)', st.svg === true);

  // همان چیز در ورود و ثبت‌نام (هر جا فیلد رمز هست)
  st = await p.evaluate(() => {
    const out = [];
    for (const where of ['login', 'register']) {
      if (where === 'login') openLogin(); else startReg();
      const w = document.querySelector('#view .pw-wrap'), i = w.querySelector('input'), e = w.querySelector('.pw-eye');
      const ir = i.getBoundingClientRect(), er = e.getBoundingClientRect();
      out.push({ where, padR: getComputedStyle(i).paddingRight, padL: getComputedStyle(i).paddingLeft,
                 svg: !!e.querySelector('svg'), rightSide: er.left > ir.left + ir.width / 2, gap: Math.round(ir.right - er.right) });
    }
    return out;
  });
  ok('۱۳) صفحه‌ی ورود: فضای امن + آیکون سمت راست', st[0].padR === '52px' && st[0].svg && st[0].rightSide && st[0].gap === 9, JSON.stringify(st[0]));
  ok('۱۴) ثبت‌نام: فضای امن + آیکون سمت راست', st[1].padR === '52px' && st[1].svg && st[1].rightSide && st[1].gap === 9, JSON.stringify(st[1]));

  // نوشته‌ی داخل فیلد رمز زیر دکمه پنهان نیست
  st = await p.evaluate(() => {
    openLogin();
    const i = document.getElementById('f_login_pass'); i.value = 'KarbamaSecret1234';
    const w = document.querySelector('#view .pw-wrap'); const eye = w.querySelector('.pw-eye');
    const ir = i.getBoundingClientRect(), er = eye.getBoundingClientRect();
    // اندازه‌ی متن: پهنای متن واردشده
    const cv = document.createElement('canvas').getContext('2d');
    cv.font = getComputedStyle(i).fontSize + ' ' + getComputedStyle(i).fontWeight;
    const tw = cv.measureText('KarbamaSecret1234').width;
    return { textEnd: Math.round(ir.left + parseFloat(getComputedStyle(i).paddingLeft) + tw), eyeStart: Math.round(er.left),
             padL: getComputedStyle(i).paddingLeft };
  });
  ok('۱۵) انتهای متنِ واردشده از شروع دکمه‌ی چشم فاصله دارد', st.textEnd <= st.eyeStart, JSON.stringify(st));
  ok('۱۵b) پدینگ چپ متن هم روی شبکه‌ی ۱۶px است', st.padL === '16px', st.padL);

  // ---------- ۳) دوره‌ی کامل 2FA هنوز سالم ----------
  await p.evaluate(() => { S.lang='fa'; S.isAdmin=true; S.adminTab='settings'; S.screen='admin'; render(); });
  await sleep(300);
  await p.evaluate(() => { tfaBegin(); render(); });
  await sleep(350);
  st = await p.evaluate(() => {
    const html = document.querySelector('#view').innerHTML;
    const eyes = [...document.querySelectorAll('#view .pw-eye')];
    return { eyes: eyes.length, svg: eyes.every(e => !!e.querySelector('svg')), uri: html.includes('otpauth://') };
  });
  // ---------- باریک‌ترین حالت (۳۶۰px): بدون ایموجی چشم، بدون هم‌پوشانی ----------
  {
    const p2 = await b.newPage();
    await p2.setViewport({ width: 360, height: 900, deviceScaleFactor: 2 });
    await p2.goto('file:///home/user/karbama-demo.html', { waitUntil: 'networkidle0' });
    await sleep(600);
    const r = await p2.evaluate(() => {
      const LONG = 'Karbama2026Secret';
      S.lang = 'fa';
      startReg();
      const i = document.getElementById('f_pass'); i.value = LONG;
      const w = i.closest('.pw-wrap'), e = w.querySelector('.pw-eye');
      const ir = i.getBoundingClientRect(), er = e.getBoundingClientRect();
      const cs = getComputedStyle(i);
      const cv = document.createElement('canvas').getContext('2d');
      cv.font = cs.fontSize + ' ' + cs.fontWeight;
      const tw = cv.measureText(LONG).width;
      // مرورگر متن را در لبه‌ی پدینگ می‌بُرد ⇒ متن دیده‌شده هرگز از این لبه جلوتر نمی‌رود
      const clipEdge = ir.right - parseFloat(cs.paddingRight);
      return { fieldW: Math.round(ir.width),
               textArea: Math.round(ir.width - parseFloat(cs.paddingRight) - parseFloat(cs.paddingLeft)),
               textRawW: Math.round(tw),
               noOverlap: clipEdge <= er.left - 2,
               clipToEyeGap: Math.round(er.left - clipEdge),
               eyeW: Math.round(er.width),
               emoji: /[\u{1F441}\u{1F648}]/u.test(document.querySelector('#view').innerHTML),
               svg: !!e.querySelector('svg') };
    });
    ok('۱۶) ۳۶۰px: ایموجی چشم حذف شده و آیکون SVG است', r.emoji === false && r.svg === true, JSON.stringify(r));
    ok('۱۷) ۳۶۰px: متن (بُرش‌خورده) هرگز زیر دکمه‌ی چشم نمی‌رود', r.noOverlap === true && r.clipToEyeGap >= 4, JSON.stringify(r));
    ok('۱۸) ۳۶۰px: در فیلد باریک دست‌کم ~۷۵px فضا برای متن می‌ماند', r.textArea >= 70, JSON.stringify(r));
    await p2.screenshot({ path: '/tmp/c23-narrow-360.png' });
    await p2.close();
  }

  ok('۱۹) دکمه‌های نمایش کلید/کد بازیابی هم آیکون SVG دارند', st.eyes >= 1 && st.svg, JSON.stringify(st));
  // دکمه‌ی نمایش کلید روی متن کلید نیفتد
  st = await p.evaluate(() => {
    const k = document.querySelector('#view .tfa-key'); const b = k.querySelector('b'); const e = k.querySelector('.pw-eye');
    const br = b.getBoundingClientRect(), er = e.getBoundingClientRect();
    return { shown: b.textContent.trim().slice(0, 4), overlap: !(er.left >= br.right - 2 || er.right <= br.left + 2) };
  });
  ok('۲۰) دکمه‌ی نمایش کلید روی متن کلید نمی‌افتد', !st.overlap, JSON.stringify(st));
  await p.evaluate(() => { tfaReveal(); render(); });
  await sleep(300);
  await p.screenshot({ path: '/tmp/a23-admin-uniform.png' });

  ok('۲۱) بدون خطای صفحه', errs.length === 0, errs.slice(0, 2).join(' | '));
  console.log(`\nنتیجه: ${pass} ✅ / ${fail} ❌`);
  await b.close();
  process.exit(fail ? 1 : 0);
})();
