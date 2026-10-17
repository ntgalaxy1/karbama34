let errs=0;
function chk(c,m){ if(!c){errs++;console.log("FAIL:",m)} else console.log("OK  :",m) }
S.lang='fa';
S.form={jobs:['chef'],otherJobs:'',trLevel:60,jobQ:"",cc:90,first:"علی",last:"رضایی",father:"حسین",
  nationalId:"0079123456",phone:"05321234567",city:"تهران",district:"تهران",address:"تهران",
  idPhoto:"data:,x",bodyPhoto:"data:,y"};
S.formErr={}; S.regStep=3; go('register');
openUndertaking();
chk(S.undDraft && S.undDraft.fee===DEFAULT_FEE, "مبلغ پیش‌فرض ۶۰۰۰ روی فرم تعهدنامه آماده است");
let h=document.getElementById('view').innerHTML;
chk(h.includes('id="f_und_fee"'), "ورودی مبلغ روی برگه‌ی تعهدنامه هست");
chk(h.includes('value="6000"'), "مقدار اولیه ۶۰۰۰ داخل فیلد است");
chk(h.includes(money(DEFAULT_FEE)), "بند اول تعهدنامه مبلغ ۶۰۰۰ را نشان می‌دهد");

setUndFee('9500');
chk(S.undDraft.fee===9500, "مبلغ تایپ‌شده (۹۵۰۰) در فرم ذخیره شد");
h=document.getElementById('view').innerHTML;
chk(h.includes(money(9500)), "بند اول با مبلغ جدید به‌روز شد");
chk(h.includes('value="9500"'), "همان مقدار داخل فیلد ماند");

setUndFee('');
chk(S.undDraft.fee===0 && S.undDraft.feeText==='', "خالی‌کردن فیلد، متن را خالی و مقدار را ۰ می‌کند");
h=document.getElementById('view').innerHTML;
chk(h.includes('value=""'), "فیلد خالی می‌ماند (صفر نوشته نمی‌شود)");
chk(h.includes('………'), "بند اول نقطه‌چین می‌شود");

S.undDraft.payerCard='6037991122334455'; S.undDraft.sig='data:,sig'; S.undDraft.signed=true;
window.__sigHasInk=true;   // امضا با انگشت کشیده شده
undConfirm();
chk(!(S.form.undertaking&&S.form.undertaking.signed), "بدون مبلغ، تعهدنامه تأیید نمی‌شود");
chk(JSON.stringify(document.getElementById('overlay').innerHTML).includes('مبلغ'), "و پیام «مبلغ را وارد کنید» نشان داده می‌شود");

setUndFee('7500');
window.__sigHasInk=true;   // در مرورگر واقعی بوم امضا موجود است (در تست شبیه‌سازی می‌شود)
undConfirm();
chk(S.form.undertaking && S.form.undertaking.signed && S.form.undertaking.fee===7500, "با مبلغ ۷۵۰۰ تعهدنامه تأیید و ذخیره شد");
chk(S.form.fee===7500, "مبلغ در فرم ثبت‌نام هم ذخیره شد");
S.screen='register'; render();
h=document.getElementById('view').innerHTML;
chk(h.includes(money(7500)), "کارت تعهدنامه در ثبت‌نام مبلغ ۷۵۰۰ را نشان می‌دهد");

S.form.consent=true; submitReg();
chk(S.session.undertaking.fee===7500, "بعد از ثبت‌نام، مبلغ وارد‌شده در پروفایل متقاضی محفوظ است");
openMyUndertaking(S.session.uid||S.session.id,'profile');
h=document.getElementById('view').innerHTML;
chk(h.includes(money(7500)), "روی برگه‌ی امضاشده همین ۷۵۰۰ چاپ می‌شود");
chk(!h.includes('id="f_und_fee"'), "در نمای خواندنی، ورودی مبلغ وجود ندارد");
S.viewUnd=null;

// مدیر می‌تواند بعداً همان مبلغ را جدا تغییر دهد
S.isAdmin=true; S.adminTab='people'; S.screen='admin'; render();
openPerson(S.session.id);
chk(S.feeEdit==='7500', "مدیر مبلغ ۷۵۰۰ را می‌بیند و می‌تواند تغییرش دهد");
setPersonFee(S.session.id, 8200);
openMyUndertaking(S.session.id,'admin'); h=document.getElementById('view').innerHTML;
chk(h.includes(money(8200)), "تغییر مدیر هم روی برگه اعمال می‌شود");
S.viewUnd=null;
console.log(errs===0 ? "\nALL TESTS PASSED ✅" : "\nERRORS: "+errs);
