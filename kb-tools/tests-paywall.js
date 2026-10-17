/* ---------- #35: صفحه‌ی اصلی همان صفحه می‌ماند، فقط آگهی‌ها پوشیده می‌شود؛
     مسیر: صفحه‌ی اصلی → صفحه‌ی پرداخت → تأییدیه پرداخت و ادامه‌ی مراحل ---------- */
let perrs = 0;
function pchk(c, m) { if (!c) { perrs++; console.log('FAIL:', m); } else console.log('OK  :', m); }
const jobsOn = html => (html.match(/class="job"/g) || []).length;

S.lang = 'fa'; S.isAdmin = false; S.filters = { city: '', district: '', q: '', sort: 'new', spec: '', minLang: 0 };
S.session = S.people.find(p => p.id === 'p1');
S.session.pay = '';                       // کارجوی پرداخت‌نشده
S.screen = 'home';

/* ---------- ۱) صفحه‌ی اصلی همان صفحه است ---------- */
const home = screenHome();
pchk(home.includes('id="q_home"'), 'صفحه‌ی اصلی: کادر جست‌وجو سرِ جای خودش است');
pchk(/class="chip/.test(home), 'صفحه‌ی اصلی: چیپ‌های شهر/فیلتر/ترتیب سرِ جای خودش هستند');
pchk(home.includes(t('greetings')), 'صفحه‌ی اصلی: عنوان «آگهی‌های استخدام» هست');
pchk(home.includes(t('found', { n: num(filtered().length) })), 'صفحه‌ی اصلی: خط «{n} آگهی یافت شد» هست');
pchk(jobsOn(home) === 0, 'بخش آگهی‌ها پنهان است (هیچ کارت آگهی‌ای نیست)');
pchk(/آگهی‌ها قفل است/.test(home), 'پنل قفل جای آگهی‌ها نشان می‌دهد «آگهی‌ها قفل است»');
pchk(/پرداخت هزینه و دیدن آگهی‌ها/.test(home), 'دکمه‌ی «پرداخت هزینه و دیدن آگهی‌ها» روی پنل قفل هست');
pchk(home.includes(money(adsFeeVal())) && /KB-\d{5}/.test(home), 'مبلغ و کد پیگیری روی پنل قفل دیده می‌شود');
pchk(/🔒/.test(tabbarHtml()), 'تب پایین هم قفل را نشان می‌دهد');

/* ---------- ۲) صفحه‌ی پرداخت ---------- */
const pay = screenPay();
pchk(/پرداخت هزینه‌ی نمایش آگهی‌ها/.test(pay), 'صفحه‌ی پرداخت باز می‌شود (عنوان درست)');
pchk(pay.includes(cardFmt(S.broker.card)), 'صفحه‌ی پرداخت: شماره کارت کارگزار');
pchk(pay.includes(money(adsFeeVal())), 'صفحه‌ی پرداخت: مبلغ');
pchk(/KB-\d{5}/.test(pay), 'صفحه‌ی پرداخت: کد پیگیری');
pchk(/واریز کردم/.test(pay), 'صفحه‌ی پرداخت: دکمه‌ی «واریز کردم»');
pchk(jobsOn(pay) === 0, 'صفحه‌ی پرداخت هیچ آگهی‌ای نشان نمی‌دهد');

/* ---------- ۳) واریز → تأییدیه پرداخت ---------- */
requestPay();
pchk(payOf(S.people.find(p => p.id === 'p1')) === 'pending', '«واریز کردم» → وضعیت pending');
pchk(S.screen === 'paid', 'بعد از واریز، صفحه‌ی تأییدیه‌ی پرداخت می‌آید');
const done = screenPayDone();
pchk(/تأییدیه پرداخت/.test(done), 'تأییدیه پرداخت: عنوان رسید');
pchk(/پرداخت شما ثبت شد/.test(done), 'تأییدیه پرداخت: «پرداخت شما ثبت شد»');
pchk(/در انتظار تأیید/.test(done), 'تأییدیه پرداخت: وضعیت «در انتظار تأیید مدیر»');
pchk(done.includes(t('pay_step1')) && done.includes(t('pay_step2')) && done.includes(t('pay_step3')),
  'ادامه‌ی مراحل (۱ ثبت · ۲ تأیید مدیر · ۳ ادامه‌ی کارجو) نشان داده می‌شود');
pchk(/بررسی مجدد وضعیت/.test(done) && /بازگشت به صفحه‌ی اصلی/.test(done), 'دکمه‌های «بررسی مجدد» و «بازگشت» هست');

/* ---------- ۴) تا تأیید مدیر، آگهی‌ها بسته می‌مانند ---------- */
S.screen = 'home';
pchk(jobsOn(screenHome()) === 0, 'پرداخت‌نشده‌ی در انتظار، هنوز آگهی نمی‌بیند');
pchk(/مشاهده‌ی وضعیت پرداخت/.test(screenHome()), 'پنل قفل حالا «مشاهده‌ی وضعیت پرداخت» می‌دهد');
recheckPay();
pchk(payOf(meRec()) === 'pending', '«بررسی مجدد» تا تأیید مدیر، تغییری نمی‌دهد');

/* ---------- ۵) تأیید مدیر → ادامه‌ی مراحل ---------- */
S.isAdmin = true; S.session = null;
adminPayConfirm('p1');
pchk(payOf(S.people.find(p => p.id === 'p1')) === 'paid', 'مدیر پرداخت را تأیید کرد');
S.isAdmin = false; S.session = S.people.find(p => p.id === 'p1');
recheckPay();
pchk(S.screen === 'home', '«بررسی مجدد» بعد از تأیید، به صفحه‌ی اصلی می‌برد');
const openHome = screenHome();
pchk(jobsOn(openHome) > 0, 'حالا آگهی‌ها روی همان صفحه‌ی اصلی نمایش داده می‌شوند');
pchk(!/آگهی‌ها قفل است/.test(openHome), 'پنل قفل برداشته شد');
pchk(!/🔒/.test(tabbarHtml()), 'قفل تب پایین برداشته شد');
pchk(/دیدن آگهی‌ها/.test(screenPayDone()), 'تأییدیه‌ی پرداخت بعد از تأیید، «دیدن آگهی‌ها» می‌دهد');

/* ---------- ۶) لغو تأیید → قفل برمی‌گردد ---------- */
S.isAdmin = true; S.session = null;
adminPayCancel('p1');
S.isAdmin = false; S.session = S.people.find(p => p.id === 'p1');
pchk(jobsOn(screenHome()) === 0, 'با لغو تأیید، آگهی‌ها باز قفل می‌شوند');

/* ---------- ۷) صفحه‌ی آگهی برای پرداخت‌نشده → صفحه‌ی پرداخت ---------- */
S.viewJob = S.jobs[0].id;
pchk(/پرداخت هزینه‌ی نمایش آگهی‌ها/.test(screenJobView()), 'کارجوی پرداخت‌نشده به‌جای آگهی، صفحه‌ی پرداخت می‌بیند');

/* ---------- ۸) مبلغ از تنظیمات مدیر ---------- */
S.isAdmin = true; S.session = null; S.adminTab = 'settings'; S.screen = 'admin';
pchk(screenAdmin().includes('id="ads_fee"'), 'فیلد «هزینه نمایش آگهی‌ها» در تنظیمات مدیر هست');
S.adsFee = '9900'; saveAdsFee();
pchk(adsFeeVal() === 9900, 'مبلغ نمایش آگهی‌ها از تنظیمات قابل تغییر است');
pchk(screenAdmin().includes(money(9900)), 'مبلغ جدید در تنظیمات دیده می‌شود');
S.adsFee = String(DEFAULT_ADS_FEE); saveAdsFee();
pchk(adsFeeVal() === DEFAULT_ADS_FEE, 'مبلغ پیش‌فرض ۶۰۰۰ برمی‌گردد');

/* ---------- ۹) کد پیگیری + ذخیره ---------- */
const codes = S.people.map(p => trackCode(p));
pchk(new Set(codes).size === codes.length, 'کد پیگیری هر کارجو یکتاست');
S.adsFee = '8200'; persist();
pchk(store.get('kb_adsFee') === '8200', 'مبلغ نمایش آگهی‌ها ذخیره می‌شود');
S.adsFee = String(DEFAULT_ADS_FEE); persist();

/* ---------- ۱۰) صفحه‌ی متقاضی در پنل مدیر ---------- */
S.isAdmin = true; S.session = null; S.viewPerson = 'p1'; S.screen = 'person';
const personHtml = screenPerson();
pchk(/وضعیت پرداخت/.test(personHtml), 'پنل مدیر: بخش «وضعیت پرداخت» در صفحه‌ی متقاضی');
pchk(/تأیید پرداخت/.test(personHtml) || /لغو تأیید/.test(personHtml), 'پنل مدیر: دکمه‌ی تأیید/لغو پرداخت');

console.log(perrs === 0 ? '\nALL TESTS PASSED ✅ (#35)' : '\nERRORS: ' + perrs);
process.exit(perrs ? 1 : 0);
