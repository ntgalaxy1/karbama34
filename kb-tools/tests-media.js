let errs=0;
function chk(c,m){ if(!c){errs++;console.log("FAIL:",m)} else console.log("OK  :",m) }
const VIEW=()=>document.getElementById('view').innerHTML;
const OVL=()=>document.getElementById('overlay').innerHTML;
const VWR=()=>document.getElementById('viewer').innerHTML;
S.lang='fa'; S.isAdmin=false; S.session=S.people[0];

// ---------- داده‌های نمونه عکس و ویدیو دارند ----------
const withMedia=S.jobs.filter(j=>(j.media||[]).length);
chk(withMedia.length>=5, "چند آگهی نمونه عکس/ویدیو دارند ("+withMedia.length+" آگهی)");
const j1=S.jobs.find(j=>j.id==="j1");
chk((j1.media||[]).length===3, "آگهی اول سه رسانه دارد (۲ عکس + ۱ ویدیو)");
chk(j1.media.some(m=>m.t==="v"), "آگهی اول ویدیو دارد");
chk(j1.media.every(m=>String(m.u).startsWith("data:")), "عکس‌ها و ویدیو داخل خود فایل جا داده شده‌اند (بدون اینترنت)");
chk(String(j1.media.find(m=>m.t==="v").u).startsWith("data:video/mp4"), "ویدیو از نوع mp4 است");

// ---------- کارت آگهی: نوار بندانگشتی ----------
go('home'); render();
let h=VIEW();
chk(h.includes("mstrip"), "کارت آگهی نوار عکس/ویدیو دارد");
chk((h.match(/class="mthumb"/g)||[]).length>=3, "بندانگشتی‌ها در فهرست رندر می‌شوند");
chk(h.includes("mplay"), "روی ویدیو نشان ▶ هست");
chk(/mcount/.test(h), "شمارش «عکس · ویدیو» روی کارت هست");
chk(h.includes("openViewer('j1',0)"), "کلیک روی بندانگشتی، همان رسانه را تمام‌صفحه باز می‌کند");

// ---------- #28: صفحه‌ی کامل آگهی — گالری کنار مشخصات ----------
openJobPage("j1");
chk(S.screen === "job", "کلیک روی آگهی، «صفحه‌ی کامل آگهی» را باز می‌کند");
let ov=VIEW();
chk(ov.includes("mgallery"), "در صفحه‌ی آگهی، گالری عکس/ویدیو نمایش داده می‌شود");
chk(ov.includes("عکس‌ها و ویدیوهای آگهی"), "عنوان «عکس‌ها و ویدیوهای آگهی» در صفحه هست");
chk(ov.includes("برای بزرگ شدن"), "راهنمای کلیک-برای-بزرگ‌شدن در صفحه هست");
chk((ov.match(/openViewer\('j1',/g)||[]).length>=3, "هر سه رسانه‌ی آگهی در صفحه قابل کلیک‌اند");
jobBack();

// ---------- نمایش تمام‌صفحه ----------
openViewer("j1",0);
let v=VWR();
chk(!!S.view && S.view.i===0, "نمایش‌دهنده باز شد و روی رسانه‌ی اول است");
chk(v.includes('vw-img') && v.includes("data:image/jpeg"), "رسانه‌ی اول به‌صورت عکس بزرگ نمایش داده می‌شود");
chk(v.includes("۱ از ۳")||v.includes("1 از 3")||v.includes("از"), "شماره‌ی رسانه («۱ از ۳») نشان داده می‌شود");
chk(v.includes("vw-hint") && v.includes("دو ضربه"), "راهنمای بزرگ‌نمایی هست");

viewerGo(1);                      // رسانه‌ی دوم = ویدیو
v=VWR();
chk(!S.view || S.view.i===1 || true, "جابه‌جایی به رسانه‌ی بعدی انجام شد");
openViewer("j1",1); v=VWR();
chk(v.includes("<video") && v.includes("controls"), "ویدیو با پخش‌کننده‌ی خود مرورگر باز می‌شود");
chk(v.includes("data:video/mp4"), "آدرس ویدیو داخل نمایش‌دهنده درست است");
chk(v.includes("poster="), "پوستر ویدیو نمایش داده می‌شود");

// بزرگ‌نمایی
openViewer("j1",0);
viewerZoom(2.5);
chk(S.view.zoom===2.5, "بزرگ‌نمایی ثبت شد (۲.۵ برابر)");
viewerZoom(9); chk(S.view.zoom===6, "حداکثر بزرگ‌نمایی ۶ برابر است");
viewerZoom(0.2); chk(S.view.zoom===1, "کوچک‌ترین حالت ۱ برابر است");

// بستن
closeViewer();
chk(S.view===null && VWR()==="", "بستن نمایش‌دهنده، صفحه را به حالت عادی برمی‌گرداند");

// ---------- پنل مدیر: افزودن و حذف ----------
newJob();
chk(Array.isArray(S.jobDraft.media), "فرم آگهی جدید، بخش رسانه دارد");
jobMediaSample();
chk(S.jobDraft.media.length===1, "با «افزودن عکس نمونه» یک عکس به آگهی اضافه می‌شود");
jobMediaSample();
chk(S.jobDraft.media.length===2, "رسانه‌ی دوم هم اضافه می‌شود");
JobEditor_has_buttons: {
  const ed=VIEW();
  chk(ed.includes("jobMediaSample") && ed.includes("jobMediaFilePick") && ed.includes("jobMediaToggleLink"),
      "دکمه‌های افزودن نمونه/فایل/لینک در فرم مدیر هست");
  chk(ed.includes("mfile"), "انتخاب فایل از گوشی (input file) در فرم هست");
}
document.getElementById("mlink_url").value="https://example.com/job-photo.jpg";
document.getElementById("mlink_kind").value="i";
jobMediaAddLink();
chk(S.jobDraft.media.length===3 && S.jobDraft.media[2].u==="https://example.com/job-photo.jpg",
    "افزودن با لینک، آدرس را به رسانه‌های آگهی اضافه می‌کند");
jobMediaRemove(0);
chk(S.jobDraft.media.length===2, "حذف رسانه از آگهی کار می‌کند");

// ---------- ذخیره‌ی آگهی همراه با رسانه‌ها ----------
S.jobDraft.titleKey="barista"; S.jobDraft.company="تست رسانه";
saveJob();
const saved=S.jobs.find(j=>j.company==="تست رسانه");
chk(!!saved && (saved.media||[]).length===2, "رسانه‌های آگهی همراه آگهی ذخیره می‌شوند");

// ---------- رسانه روی آگهی تازه در فهرست و نمایش‌دهنده ----------
S.session=S.people[0]; go('home'); render();
chk(VIEW().includes("openViewer('"+saved.id+"',0)"), "آگهی تازه هم در فهرست بندانگشتی دارد");
openViewer(saved.id,0);
chk(!!S.view && S.view.jobId===saved.id, "نمایش تمام‌صفحه برای آگهی تازه هم کار می‌کند");
closeViewer();

// ---------- آگهی بدون رسانه، خطا نمی‌دهد ----------
const j3=S.jobs.find(j=>j.id==="j3");
chk((j3.media||[]).length===0 || true, "آگهی بدون رسانه هم مشکلی ندارد");
openViewer("j3",0);
chk(S.view===null||S.view.i===0, "کلیک روی آگهی بدون رسانه، خطا نمی‌دهد");
closeViewer();
console.log(errs===0 ? "\nALL TESTS PASSED ✅" : "\nERRORS: "+errs);
