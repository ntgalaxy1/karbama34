/* #22: تست پسورد پنهان + نمایش + ورود دومرحله‌ای */
let errs=0; function chk(c,m){ if(!c){errs++;console.log("❌ FAIL:",m)} else console.log("✅ OK  :",m) }
S.lang='fa';

/* ---------- ۱) صحت الگوریتم TOTP با بردارهای آزمون RFC 6238 ---------- */
const RFC="GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ";       // "12345678901234567890"
chk(b32decode(RFC).length===20, "base32 → 20 بایت");
chk(String.fromCharCode(...b32decode("MZXW6YTBOI")).toUpperCase()==="FOOBAR", "base32 decode نمونه");
chk(totpCode(RFC, 59*1000, 0)==="287082",       "TOTP RFC6238 T=59 → 287082");
chk(totpCode(RFC, 1111111109*1000, 0)==="081804","TOTP RFC6238 T=1111111109 → 081804");
chk(totpCode(RFC, 1234567890*1000, 0)==="005924","TOTP RFC6238 T=1234567890 → 005924");
chk(totpCode(RFC, 2000000000*1000, 0)==="279037","TOTP RFC6238 T=2000000000 → 279037");
const sec2=totpSecret();
chk(sec2.length===32 && /^[A-Z2-7]+$/.test(sec2), "کلید تولیدی ۳۲ کاراکتر Base32");
chk(b32encode(b32decode(sec2))===sec2, "رفت‌وبرگشت Base32");
chk(totpPretty(sec2).length===39, "نمایش کلید به‌صورت گروه‌های ۴تایی");
chk(totpUri(sec2).startsWith("otpauth://totp/Karbama:admin?secret=") && totpUri(sec2).includes("issuer=Karbama"), "آدرس otpauth");
const rc=recoveryCode();
chk(/^\d{4}-\d{4}$/.test(rc), "کد بازیابی ۴+۴ رقمی");
chk(totpVerify(sec2, totpCode(sec2, Date.now(), 0)), "verify کد فعلی را قبول می‌کند");
chk(totpVerify(sec2, totpCode(sec2, Date.now()+30000, 0)), "پنجره‌ی ±۱ مرحله (۳۰ ثانیه بعد)");
chk(totpVerify(sec2, totpCode(sec2, Date.now()-30000, 0)), "پنجره‌ی ±۱ مرحله (۳۰ ثانیه قبل)");
chk(!totpVerify(sec2, "000000")===false || !totpVerify(sec2,"111111") , "کد اشتباه رد می‌شود");
chk(!totpVerify(sec2, "12345"), "کد ناقص رد می‌شود");

/* ---------- ۲) همه‌ی فیلدهای پسورد پنهان + دکمه‌ی چشم ---------- */
const raw=require("fs").readFileSync("/home/user/kb-tools/demo.js","utf8");
const html=require("fs").readFileSync("/home/user/karbama-demo.html","utf8");
const pwInputs=(raw.match(/type="password"/g)||[]).length;
const pwWraps=(raw.match(/pwWrap\(`/g)||[]).length;
chk(pwInputs===3, `سه فیلد پسورد در دمو (${pwInputs})`);
chk(pwWraps===pwInputs, `همه‌ی فیلدهای پسورد داخل pwWrap با دکمه‌ی نمایش (${pwWraps})`);
chk(html.includes(".pw-eye{") && html.includes(".pw-wrap > input{"), "CSS دکمه‌ی چشم");

startReg(); let h=document.getElementById('view').innerHTML;
chk(h.includes('type="password" id="f_pass"') && h.includes('class="pw-eye"'), "ثبت‌نام: پسورد پنهان + چشم");
openLogin(); h=document.getElementById('view').innerHTML;
chk(h.includes('type="password" id="f_login_pass"') && h.includes('class="pw-eye"'), "ورود: پسورد پنهان + چشم");
S.isAdmin=true; S.adminTab='settings'; S.screen='admin'; render(); h=document.getElementById('view').innerHTML;
chk((h.match(/pw-eye/g)||[]).length>=1, "تنظیمات مدیر: پسورد پنهان + چشم");
chk(h.includes("ورود دومرحله‌ای"), "تنظیمات: کارت ورود دومرحله‌ای");
chk(h.includes("•••• •••• •••• ••••") || h.includes(t("twofa_off")), "تنظیمات: وضعیت 2FA");

/* ---------- ۳) فعال‌سازی 2FA از تنظیمات ---------- */
S.twoFA={ on:false, secret:"", recovery:"", pending:false };
tfaBegin(); render();
chk(S.twoFA.pending && !S.twoFA.on && S.twoFA.secret.length===32, "شروع راه‌اندازی: کلید ساخته شد ولی هنوز فعال نیست");
h=document.getElementById('view').innerHTML;
chk(h.includes("•••• •••• •••• ••••"), "کلید در ابتدا پنهان است");
chk(h.includes("otpauth://totp/Karbama:admin"), "آدرس otpauth در تنظیمات");
tfaReveal(); h=document.getElementById('view').innerHTML;
chk(h.includes(totpPretty(S.twoFA.secret)), "دکمه‌ی نمایش کلید را نشان می‌دهد");
const wrong="999999"===totpCode(S.twoFA.secret,Date.now(),0)?"999998":"999999";
document.getElementById('f_tfa_confirm').value=wrong;
tfaConfirm();
chk(S.tfaErr && !S.twoFA.on, "کد اشتباه → 2FA فعال نمی‌شود");
document.getElementById('f_tfa_confirm').value=totpCode(S.twoFA.secret, Date.now(), 0);
tfaConfirm();
chk(S.twoFA.on && !S.twoFA.pending && /^\d{4}-\d{4}$/.test(S.twoFA.recovery), "کد درست → 2FA فعال + کد بازیابی");
render(); h=document.getElementById('view').innerHTML;
chk(h.includes(t("twofa_on")) && h.includes("••••-••••"), "کد بازیابی پنهان نمایش داده می‌شود");

/* ---------- ۴) ورود دومرحله‌ای: پسورد درست → پرسیدن کد ---------- */
logout(); openLogin();
document.getElementById('f_login_user').value="admin";
document.getElementById('f_login_pass').value="karbama2026";
doLogin();
chk(S.tfaStage && !S.isAdmin && S.screen==='login', "با 2FA فعال، پنل بدون کد باز نمی‌شود");
h=document.getElementById('view').innerHTML;
chk(h.includes(t("twofa_step_title")) && h.includes('id="f_tfa_code"'), "صفحه‌ی مرحله‌ی دوم با فیلد کد");
chk(!h.includes('id="f_login_pass"'), "مرحله‌ی دوم جای فرم اول را گرفته");
chk(h.includes('id="tfa_now"') && h.includes(t("twofa_demo_tag")), "کد نمایشی دمو در دسترس است");
document.getElementById('f_tfa_code').value=wrong;
tfaCheck();
chk(S.tfaErr && !S.isAdmin, "کد اشتباه در ورود → اجازه‌ی ورود نیست");
document.getElementById('f_tfa_code').value=totpCode(S.twoFA.secret, Date.now(), 0);
tfaCheck();
chk(S.isAdmin && S.screen==='admin' && !S.tfaStage, "کد درست → ورود به پنل مدیریت");

/* ---------- ۵) دکمه‌ی بازگشت و کد بازیابی یک‌بارمصرف ---------- */
logout(); openLogin();
document.getElementById('f_login_user').value="admin";
document.getElementById('f_login_pass').value="karbama2026";
doLogin(); tfaBack();
chk(!S.tfaStage && !S.isAdmin, "بازگشت از مرحله‌ی دوم");
document.getElementById('f_login_user').value="admin";
document.getElementById('f_login_pass').value="karbama2026";
doLogin();
const rec=S.twoFA.recovery.replace("-","");
document.getElementById('f_tfa_code').value=rec;
tfaCheck();
chk(S.isAdmin && !S.twoFA.on, "کد بازیابی → ورود + خاموش‌شدن خودکار 2FA");

/* ---------- ۶) غیرفعال‌کردن از تنظیمات ---------- */
S.adminTab='settings'; S.screen='admin'; render();
chk(document.getElementById('view').innerHTML.includes(t("twofa_off")), "تنظیمات بعد از مصرف کد بازیابی: غیرفعال");
tfaBegin();
document.getElementById('f_tfa_confirm').value=totpCode(S.twoFA.secret, Date.now(), 0);
tfaConfirm();
chk(S.twoFA.on, "فعال‌سازی مجدد");
tfaDisable();
chk(!S.twoFA.on && !S.twoFA.secret && !S.twoFA.pending, "غیرفعال‌کردن 2FA");
logout(); openLogin();
document.getElementById('f_login_user').value="admin";
document.getElementById('f_login_pass').value="karbama2026";
doLogin();
chk(S.isAdmin && S.screen==='admin' && !S.tfaStage, "بدون 2FA → ورود مستقیم به پنل");
S.isAdmin=false; persist();

/* ---------- ۷) کلیدهای ترجمه در سه زبان ---------- */
const used=new Set();
const re=/\bt\("([A-Za-z_][A-Za-z0-9_]*)"\)/g; let m;
while((m=re.exec(raw))) used.add(m[1]);
const need=["pass_show","pass_hide","pass_note_shown","twofa_title","twofa_hint","twofa_enable","twofa_disable",
  "twofa_scan","twofa_code","twofa_confirm","twofa_verify","twofa_on","twofa_off","twofa_show","twofa_hide",
  "twofa_copy","twofa_recovery","twofa_recovery_note","twofa_wrong","twofa_step_title","twofa_step_hint",
  "twofa_back","twofa_demo_tag","twofa_recovery_now"];
for(const k of need) chk(["fa","tr","en"].every(l=>I18N[l] && I18N[l][k]!==undefined), `کلید ${k} در fa/tr/en`);
const missing=[];
for(const l of ["fa","tr","en"]) for(const k of used) if(I18N[l][k]===undefined) missing.push(l+":"+k);
chk(missing.length===0, "هیچ کلید ترجمه‌ای جا نمانده" + (missing.length?" → "+missing.slice(0,8).join(", "):""));

/* ---------- ۸) رگرسیون کوتاه ---------- */
S.lang='fa'; S.isAdmin=true; S.adminTab='settings'; S.screen='admin'; render();
h=document.getElementById('view').innerHTML;
chk(h.includes("1234 1234 1234 1234"), "کارت پیش‌فرض کارگزار سر جایش");
chk(!h.includes("مبلغ هزینه کارگزار"), "بخش مبلغ هنوز در تنظیمات نیست");
S.lang='en'; render();
chk(document.getElementById('view').innerHTML.includes("Two-step sign-in (2FA)"), "ترجمه‌ی انگلیسی 2FA");
S.lang='tr'; render();
chk(document.getElementById('view').innerHTML.includes("İki adımlı giriş (2FA)"), "ترجمه‌ی ترکی 2FA");

console.log(errs? `\n❌ ${errs} خطا` : "\n🎉 #22 همه‌ی تست‌ها PASSED");
process.exit(errs?1:0);
