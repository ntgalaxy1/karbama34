let errs=0;
function chk(c,m){ if(!c){errs++;console.log("FAIL:",m)} else console.log("OK  :",m) }
const TBhtml=()=>document.getElementById('tabbar').innerHTML;
const VIEW=()=>document.getElementById('view').innerHTML;
S.lang='fa';

// ---------- تب «ارتباط با ما» کنار تب پروفایل ----------
S.isAdmin=false; S.session=null; S.screen='home'; render();
chk(TBhtml().includes('ارتباط با ما'), "تب «ارتباط با ما» کنار تب‌های کاربر هست");
chk(TBhtml().includes('پروفایل من') && TBhtml().includes('آگهی‌ها'), "هر سه تب کاربر سر جایشان‌اند (آگهی‌ها/پروفایل من/ارتباط با ما)");
chk((TBhtml().match(/<button/g)||[]).length===3, "دقیقاً سه تب پایین برای کاربر عادی");
const iProf=TBhtml().indexOf('پروفایل من'), iCont=TBhtml().indexOf('ارتباط با ما');
chk(iProf>0 && iCont>iProf, "ترتیب تب‌ها: … پروفایل من ، ارتباط با ما");

// ---------- باز شدن صفحه ----------
go('contact');
let h=VIEW();
chk(S.screen==='contact' && h.includes('ارتباط با ما'), "صفحه‌ی ارتباط با ما باز می‌شود");
chk(TBhtml().includes('ارتباط با ما'), "در این صفحه هم تب‌بار کاربر دیده می‌شود");
chk(h.includes('ما هیچ شعبه یا نماینده‌ای نداریم'), "متن «ما هیچ شعبه یا نماینده‌ای نداریم» هست");
chk(h.includes('تنها راه‌های ارتباطی گذاشته شده است'), "متن «تنها راه‌های ارتباطی گذاشته شده است» هست");
chk(h.includes('کانال تلگرام') && h.includes('واتس‌اپ') && h.includes('پیج اینستاگرام'), "سه راه ارتباطی نام‌برده شده‌اند");

// ---------- نام کاربری پیش‌فرض روی همه‌ی راه‌ها ----------
chk(HANDLE==="(karbama34)", "نام کاربری پیش‌فرض (karbama34) است");
chk((h.match(/\(karbama34\)/g)||[]).length>=4, "«(karbama34)» هم در هدر و هم روی هر سه راه ارتباطی نوشته شده");

// ---------- کل ردیف یک لینک واقعی است (کلیک روی هر بخشی → انتقال) ----------
const anchors=h.match(/<a class="card contact-row"[^>]*>/g)||[];
chk(anchors.length===3, "هر سه راه ارتباطی، لینک واقعی (<a>) هستند — کل ردیف قابل کلیک");
const L={
  telegram:{app:"tg://resolve?domain=karbama34", web:"https://t.me/karbama34"},
  whatsapp:{app:"whatsapp://send?phone=905521213486", web:"https://wa.me/905521213486"},
  instagram:{app:"instagram://user?username=karbama34", web:"https://www.instagram.com/karbama34"}
};
for(const k in L){
  const c=CONTACT.find(x=>x.key===k);
  chk(!!c && c.app===L[k].app, k+": اسکیم باز کردن خودِ برنامه درست است ("+L[k].app+")");
  chk(!!c && c.web===L[k].web, k+": لینک درست است ("+L[k].web+")");
  chk(h.includes(`href="${L[k].web}"`), k+": href کارت همان لینک است → با کلیک روی هر نقطه، انتقال انجام می‌شود");
  chk(h.includes(`target="_blank"`) && (h.match(/rel="noopener"/g)||[]).length===3, k+": باز شدن در تب تازه و ایمن");
  chk(h.includes(c.show), k+": متن لینک روی کارت نمایش داده می‌شود ("+c.show+")");
}
chk((h.match(/<\/a>/g)||[]).length===3, "هر سه لینک بسته شده‌اند (ساختار درست)");
chk(!/onclick="openContact/.test(h), "دیگر به onclick وابسته نیست (پاپ‌آپ‌بلاکر نمی‌تواند جلوی انتقال را بگیرد)");

// ---------- لوگوی خود برنامه‌ها ----------
chk((h.match(/<svg viewBox="0 0 24 24"/g)||[]).length===3, "سه لوگوی برند (SVG) داخل همان لینک‌ها هست");
chk(h.includes('#229ED9') && h.includes('#25D366') && h.includes('#F58529'), "رنگ برند هر برنامه (آبی/سبز/گرادیان اینستاگرام) استفاده شده");

// ---------- دکمه‌ی کپی: جلوی انتقال را می‌گیرد و خودش کپی می‌کند ----------
const copies=h.match(/<span class="btn btn-ghost btn-sm"[^>]*>/g)||[];
chk(copies.length===3, "دکمه‌ی «کپی» برای هر سه راه هست");
chk(copies.every(t=>t.includes('event.preventDefault();event.stopPropagation();copyContact(')), "کلیک روی «کپی» فقط کپی می‌کند و صفحه را ترک نمی‌کند");
openContact('telegram'); copyContact('whatsapp');
chk(true, "فراخوانی openContact/copyContact خطا نمی‌دهد");

// ---------- نکته‌ی قاب پیش‌نمایش ----------
chk(typeof inFrame==='function', "تابع تشخیص قاب پیش‌نمایش وجود دارد");

// ---------- انتقال به «خودِ برنامه»، نه صفحه‌ی وب ----------
chk(typeof launch==='function', "تابع launch برای انتقال به خودِ برنامه وجود دارد");
const anchorHtml=VIEW().match(/<a class="card contact-row"[^>]*>/g)||[];
chk(anchorHtml.length===3 && anchorHtml.every(a=>a.includes("onclick=\"return launch(")), "کلیک روی هر کارت از مسیر launch می‌گذرد");

let openedWeb=[];
window.open=(u)=>{ openedWeb.push(u); return {close(){}}; };
document.hidden=false; document.visibilityState="visible";

// الف) برنامه نصب نیست (صفحه پنهان نمی‌شود) → باید لینک وب باز شود
openedWeb=[]; global.__timers=[];
launch('telegram', {preventDefault(){}});
chk((global.__timers||[]).length>0, "در همان لحظه‌ی کلیک، تلاش برای باز کردن خودِ برنامه انجام می‌شود (اسکیم tg://)");
runTimers();
chk(openedWeb.includes('https://t.me/karbama34'), "اگر برنامه نصب نباشد → لینک وب باز می‌شود (پشتیبان)");

// ب) برنامه باز شد (صفحه پنهان شد) → نباید هیچ صفحه/مرورگری باز شود
openedWeb=[]; global.__timers=[];
launch('whatsapp', {preventDefault(){}});
document.hidden=true; document.visibilityState="hidden"; document.fireEvent("visibilitychange");
document.hidden=false; document.visibilityState="visible";
runTimers();
chk(openedWeb.length===0, "اگر خودِ برنامه باز شود، هیچ لینک/مرورگری باز نمی‌شود");

// ج) همین برای اینستاگرام
openedWeb=[]; global.__timers=[];
launch('instagram', {preventDefault(){}});
document.hidden=true; document.fireEvent("visibilitychange");
document.hidden=false; runTimers();
chk(openedWeb.length===0, "اینستاگرام: با باز شدن برنامه، مرورگر باز نمی‌شود");

// ---------- مدیر: بدون تب پایین ----------
S.isAdmin=true; S.session=null; render();
chk(TBhtml()==='', "برای مدیر همچنان هیچ تب پایینی نیست (پنل تمام‌صفحه)");
S.isAdmin=false;

// ---------- برگشت از صفحه و تب‌های دیگر ----------
S.session=S.people[0]; go('home');
chk(TBhtml().includes('ارتباط با ما'), "با ورود کاربر عادی و بازگشت به آگهی‌ها، تب‌ها هستند");
go('profile');
chk(TBhtml().includes('ارتباط با ما') && VIEW().includes(S.people[0].first), "در تب پروفایل هم تب «ارتباط با ما» در دسترس است");
console.log(errs===0 ? "\nALL TESTS PASSED ✅" : "\nERRORS: "+errs);
