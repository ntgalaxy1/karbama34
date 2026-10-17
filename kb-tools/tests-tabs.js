let errs=0;
function chk(c,m){ if(!c){errs++;console.log("FAIL:",m)} else console.log("OK  :",m) }
const TBhtml=()=>document.getElementById('tabbar').innerHTML;
S.lang='fa';

// ---------- کاربر عادی: تب‌های پایین سر جایشان ----------
S.isAdmin=false; S.session=null; S.screen='home'; render();
chk(TBhtml().includes('آگهی‌ها') && TBhtml().includes('پروفایل من') && TBhtml().includes('ارتباط با ما'), "کاربر عادی: تب‌های «آگهی‌ها»، «پروفایل من» و «ارتباط با ما» هست");
chk(!TBhtml().includes('مدیریت'), "و تب «مدیریت» برای کاربر عادی نیست");

// ---------- ورود مدیر: هیچ تب پایینی نباشد ----------
openLogin();
document.getElementById('f_login_user').value='admin';
document.getElementById('f_login_pass').value=PWD;
doLogin();
chk(S.isAdmin===true && S.screen==='admin', "مدیر مستقیم وارد پنل می‌شود");
chk(TBhtml()==='', "در پنل مدیر، تب‌های پایین (آگهی‌ها/پروفایل من) حذف شده‌اند");
let h=document.getElementById('view').innerHTML;
chk(!/tabbar/.test(TBhtml()), "تب‌بار خالی است");
// تب‌های داخلی خود پنل باید سر جایشان باشند
chk(h.includes('آگهی‌ها') && h.includes('متقاضیان') && h.includes('تنظیمات'), "تب‌های داخلی پنل (آگهی‌ها/متقاضیان/تنظیمات) کار می‌کنند");
S.adminTab='people'; render();
chk(TBhtml()==='' && document.getElementById('view').innerHTML.includes('متقاضیان'), "در تب متقاضیان هم تب پایین نیست");
S.adminTab='settings'; render();
chk(TBhtml()==='', "در تب تنظیمات هم تب پایین نیست");

// ---------- خروج مدیر → صفحه‌ی ورود ----------
adminExit();
chk(S.isAdmin===false && S.screen==='login', "خروج مدیر (⏻) → برگشت به صفحه‌ی ورود");
chk(TBhtml()==='', "در صفحه‌ی ورود تب پایین نیست");

// ---------- ورود کاربر عادی: تب‌ها برمی‌گردند ----------
S.people[0].username='ali'; S.people[0].pass='1234';
document.getElementById('f_login_user').value='ali';
document.getElementById('f_login_pass').value='1234';
doLogin();
chk(S.session && S.screen==='profile', "کاربر عادی وارد قسمت شخصی می‌شود");
chk(TBhtml().includes('آگهی‌ها') && TBhtml().includes('پروفایل من') && TBhtml().includes('ارتباط با ما'), "برای کاربر عادی، تب‌های پایین سر جایشان هستند");
console.log(errs===0 ? "\nALL TESTS PASSED ✅" : "\nERRORS: "+errs);
