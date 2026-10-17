// #22 — تست مرورگری: پسورد پنهان + دکمه‌ی نمایش + ورود دومرحله‌ای (2FA)
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
  await p.evaluate(() => { S.lang = 'fa'; S.isAdmin = false; openLogin(); });
  await sleep(300);

  // ---------- ۱) فیلد پسورد ورود: پنهان + دکمه‌ی چشم ----------
  let st = await p.evaluate(() => {
    const i = document.getElementById('f_login_pass');
    const eye = document.querySelector('#view .pw-eye');
    return { type: i && i.type, eye: !!eye, eyeSvg: !!(eye && eye.querySelector('svg')), padEnd: i ? getComputedStyle(i).paddingRight : null };
  });
  ok('۱) پسورد ورود از نوع password است (پنهان)', st.type === 'password', JSON.stringify(st));
  ok('۲) دکمه‌ی نمایش کنار فیلد پسورد هست', st.eye && st.eyeSvg === true, JSON.stringify(st));
  ok('۳) فاصله‌ی امن برای دکمه‌ی چشم رعایت شده', String(st.padEnd).replace('px','') >= 40, String(st.padEnd));

  await p.evaluate(() => { const i = document.getElementById('f_login_pass'); i.value = 'karbama2026'; });
  await p.click('#view .pw-eye');  await sleep(350);
  st = await p.evaluate(() => {
    const i = document.getElementById('f_login_pass'); const eye = document.querySelector('#view .pw-eye');
    return { type: i.type, txt: eye.querySelector('svg') ? 'svg' : '', on: eye.classList.contains('on') };
  });
  ok('۴) کلیک روی چشم → پسورد دیده می‌شود', st.type === 'text', JSON.stringify(st));
  ok('۵) آیکون به حالت «پنهان‌کردن» (نقاب) می‌رود', st.on && st.txt === 'svg', JSON.stringify(st));
  await p.click('#view .pw-eye');  await sleep(350);
  st = await p.evaluate(() => { const e = document.querySelector('#view .pw-eye'); return { type: document.getElementById('f_login_pass').type, txt: e.querySelector('svg') ? 'svg' : '', on: e.classList.contains('on') }; });
  ok('۶) کلیک دوباره → پسورد پنهان می‌شود', st.type === 'password' && st.txt === 'svg' && !st.on, JSON.stringify(st));
  await p.screenshot({ path: '/tmp/a22-eye-login.png' });

  // ---------- ۲) ورود مدیر و تنظیمات ----------
  await p.evaluate(() => { document.getElementById('f_login_user').value = 'admin'; document.getElementById('f_login_pass').value = 'karbama2026'; doLogin(); });
  await sleep(500);
  ok('۷) ورود مدیر (۲FA خاموش → مستقیم)', await p.evaluate(() => S.isAdmin === true && S.screen === 'admin'));
  await p.evaluate(() => { S.adminTab = 'settings'; render(); });
  await sleep(400);
  st = await p.evaluate(() => {
    const html = document.querySelector('#view').innerHTML;
    const inputs = [...document.querySelectorAll('#view input')];
    const pw = inputs.find(i => i.value === 'karbama2026');
    return { pwType: pw ? pw.type : null, eyes: document.querySelectorAll('#view .pw-eye').length, has2fa: html.includes('ورود دومرحله‌ای'), hasEnable: html.includes('فعال‌سازی ورود دومرحله‌ای') };
  });
  ok('۸) پسورد مدیر در تنظیمات هم پنهان است', st.pwType === 'password', JSON.stringify(st));
  ok('۹) دکمه‌ی نمایش پسورد مدیر هست', st.eyes >= 1);
  ok('۱۰) کارت ورود دومرحله‌ای در تنظیمات هست', st.has2fa && st.hasEnable);

  // ---------- ۳) راه‌اندازی 2FA ----------
  await p.evaluate(() => { [...document.querySelectorAll('#view button')].find(x => x.textContent.includes('فعال‌سازی')).click(); });
  await sleep(450);
  st = await p.evaluate(() => {
    const html = document.querySelector('#view').innerHTML;
    const key = document.querySelector('#view .tfa-key b');
    return { masked: key && key.textContent.includes('••••'), uri: html.includes('otpauth://totp/Karbama:admin'), hasConfirm: !!document.getElementById('f_tfa_confirm'), code: (document.getElementById('tfa_now') || {}).textContent || '' };
  });
  ok('۱۱) کلید در ابتدا پنهان است', st.masked === true, JSON.stringify(st));
  ok('۱۲) آدرس otpauth نمایش داده می‌شود', st.uri);
  ok('۱۳) فیلد کد ۶ رقمی برای تأیید هست', st.hasConfirm);
  ok('۱۴) کد جاری برنامه (نمایشی دمو) تولید می‌شود', /^\d{6}$/.test(st.code), st.code);
  await p.evaluate(() => { [...document.querySelectorAll('#view button')].find(x => x.textContent.includes('نمایش')).click(); });
  await sleep(400);
  st = await p.evaluate(() => ({ key: document.querySelector('#view .tfa-key b').textContent.trim() }));
  ok('۱۵) دکمه‌ی نمایش، کلید ۳۲ کاراکتری را نشان می‌دهد', /^([A-Z2-7]{4} ){7}[A-Z2-7]{4}$/.test(st.key), st.key);
  await p.screenshot({ path: '/tmp/a22-2fa-setup.png' });
  await p.evaluate(() => { const i = document.getElementById('f_tfa_confirm'); i.value = '000000'; });
  await p.evaluate(() => { [...document.querySelectorAll('#view button')].find(x => x.textContent.includes('تأیید و فعال')).click(); });
  await sleep(400);
  ok('۱۶) کد اشتباه → فعال نمی‌شود و خطا نشان داده می‌شود',
    await p.evaluate(() => !S.twoFA.on && document.querySelector('#view .err') && document.querySelector('#view .err').textContent.includes('درست نیست')));
  await p.evaluate(() => { document.getElementById('f_tfa_confirm').value = window.totpCode(S.twoFA.secret, Date.now(), 0); });
  await p.evaluate(() => { [...document.querySelectorAll('#view button')].find(x => x.textContent.includes('تأیید و فعال')).click(); });
  await sleep(450);
  st = await p.evaluate(() => ({ on: S.twoFA.on, rec: (document.querySelector('#view .tfa-key b') || {}).textContent || '', html: document.querySelector('#view').innerHTML }));
  ok('۱۷) کد درست → 2FA فعال شد', st.on === true);
  ok('۱۸) کد بازیابی به‌صورت پنهان نمایش داده می‌شود', st.rec.includes('••••'), st.rec);
  ok('۱۹) دکمه‌ی غیرفعال‌کردن آمده', st.html.includes('غیرفعال‌کردن ورود دومرحله‌ای'));
  await p.screenshot({ path: '/tmp/a22-2fa-on.png' });

  // ---------- ۴) ورود با کد ----------
  await p.evaluate(() => { logout(); openLogin(); });
  await sleep(350);
  await p.evaluate(() => { document.getElementById('f_login_user').value = 'admin'; document.getElementById('f_login_pass').value = 'karbama2026'; doLogin(); });
  await sleep(500);
  st = await p.evaluate(() => ({ stage: S.tfaStage, admin: S.isAdmin, html: document.querySelector('#view').innerHTML, user: !!document.getElementById('f_login_user') }));
  ok('۲۰) پسورد درست → صفحه‌ی مرحله‌ی دوم', st.stage === true && st.admin === false && st.user === false);
  ok('۲۱) عنوان مرحله‌ی دوم و فیلد کد', st.html.includes('تأیید دومرحله‌ای') && !!st.html.match(/f_tfa_code/));
  ok('۲۲) پسورد در مرحله‌ی دوم دیده نمی‌شود (پنهان می‌ماند)', !st.html.includes('karbama2026'));
  await p.screenshot({ path: '/tmp/a22-step2.png' });
  await p.evaluate(() => { document.getElementById('f_tfa_code').value = '123456'; tfaCheck(); });
  await sleep(350);
  ok('۲۳) کد اشتباه → ورود رد می‌شود', await p.evaluate(() => !S.isAdmin && S.tfaErr === true));
  await p.evaluate(() => { document.getElementById('f_tfa_code').value = window.totpCode(S.twoFA.secret, Date.now(), 0); tfaCheck(); });
  await sleep(500);
  st = await p.evaluate(() => ({ admin: S.isAdmin, screen: S.screen, stage: S.tfaStage }));
  ok('۲۴) کد درست → ورود به پنل مدیریت', st.admin === true && st.screen === 'admin' && st.stage === false, JSON.stringify(st));
  await p.screenshot({ path: '/tmp/a22-admin-in.png' });

  // ---------- ۵) کد بازیابی یک‌بارمصرف ----------
  await p.evaluate(() => { const r = S.twoFA.recovery.replace('-', ''); logout(); openLogin(); document.getElementById('f_login_user').value = 'admin'; document.getElementById('f_login_pass').value = 'karbama2026'; doLogin(); document.getElementById('f_tfa_code').value = r; tfaCheck(); });
  await sleep(500);
  st = await p.evaluate(() => ({ admin: S.isAdmin, on: S.twoFA.on }));
  ok('۲۵) کد بازیابی → ورود انجام شد', st.admin === true);
  ok('۲۶) کد بازیابی یک‌بارمصرف است → 2FA خاموش شد', st.on === false);

  ok('۲۷) بدون خطای صفحه', errs.length === 0, errs.slice(0, 2).join(' | '));
  console.log(`\nنتیجه: ${pass} ✅ / ${fail} ❌`);
  await b.close();
  process.exit(fail ? 1 : 0);
})();
