/* رگرسیون: خواسته‌های قبلی هم سالم بمانند */
let errs=0;
function chk(c,m){ if(!c){errs++;console.log("FAIL:",m)} else console.log("OK  :",m) }
S.lang='fa';
for(const sc of ['lang','welcome','login','register','home','profile','admin','jobEditor']){
  S.screen=sc;
  try{ render() }catch(e){ errs++; console.log("ERR",sc,e.message) }
}
S.screen='welcome'; render();
let h=document.getElementById('view').innerHTML;
chk(h.includes('شروع ثبت‌نام') && h.includes('ورود به برنامه') && !h.includes('فقط می‌خواهم'), "خوش‌آمد: ثبت‌نام + ورود، بدون مرور آگهی");
openLogin(); h=document.getElementById('view').innerHTML;
chk(!/karbama2026/.test(h) && !h.includes('حساب ندارم'), "صفحه‌ی ورود بدون رمز مدیر و بدون ایجاد حساب");
S.people[0].username='ali'; S.people[0].pass='1234';
document.getElementById('f_login_user').value='ali'; document.getElementById('f_login_pass').value='1234';
doLogin();
chk(S.session && S.screen==='profile', "ورود کاربر → قسمت شخصی");
logout(); openLogin();
document.getElementById('f_login_user').value='admin'; document.getElementById('f_login_pass').value=PWD; doLogin();
chk(S.isAdmin && S.screen==='admin', "ورود مدیر → پنل مدیریت");
S.adminTab='settings'; S.screen='admin'; render(); h=document.getElementById('view').innerHTML;
chk(!h.includes('مبلغ هزینه کارگزار') && h.includes('1234 1234 1234 1234'), "تنظیمات: بدون مبلغ، کارت ثابت");
S.broker.card=cardFmt('5555666677778888');
chk(S.broker.card==='5555 6666 7777 8888', "شماره کارت قابل تغییر است");
S.broker.card=DEFAULT_CARD; persist();
S.adminTab='people'; render();
const pp=S.people.find(p=>p.undertaking&&p.undertaking.signed);
openPerson(pp.id); chk(+S.feeEdit>0, "مبلغ هر شخص در پرونده قابل تغییر است");
const before=pp.undertaking.fee; setPersonFee(pp.id, 9100);
chk(pp.undertaking.fee===9100 && before!==9100, "تغییر مبلغ شخصی ذخیره شد");
S.isAdmin=false; startReg();
S.form={jobs:[],trLevel:40,jobQ:"",cc:90,first:"سارا",last:"م",father:"اکبر",phone:"05321234567"};
S.regStep=1; step(1);
chk(S.regStep===1 && S.formErr.nationalId && S.formErr.username, "اعتبارسنجی مرحله ۱");
S.form.nationalId='0079123456'; S.form.username='sara'; S.form.pass='1234';
setUsername('ساراsara۹۹');
chk(S.form.username==='sara', "نام کاربری فقط انگلیسی");
step(1); S.form.city='تهران'; S.form.district='تهران'; S.form.address='تهران';
S.form.otherJobs='جوشکار'; step(1); step(1);
chk(S.regStep===3, "مرحله ۲ با «سایر حرفه‌ها» قبول می‌شود");
console.log(errs===0 ? "\nALL TESTS PASSED ✅" : "\nERRORS: "+errs);
