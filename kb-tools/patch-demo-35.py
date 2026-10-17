#!/usr/bin/env python3
"""#35 (اصلاح مسیر) — صفحه‌ی اصلی همان صفحه‌ی قبلی می‌ماند؛ فقط بخش آگهی‌ها
   با یک پنل «نیاز به پرداخت» پوشیده می‌شود. مسیر:
   صفحه‌ی اصلی (قفل روی آگهی‌ها) → کلیک → صفحه‌ی پرداخت → «واریز کردم»
   → تأییدیه پرداخت + ادامه‌ی مراحل.
اجرا:  python3 kb-tools/patch-demo-35.py          (گزارش)
       python3 kb-tools/patch-demo-35.py --apply  (اعمال)
"""
import sys, pathlib

F = pathlib.Path('/home/user/karbama-demo.html')
APPLY = '--apply' in sys.argv
s = F.read_text()
done = []

def rep(old, new, label, count=1):
    global s
    assert s.count(old) == count, f'{label}: anchor count = {s.count(old)} (expected {count})'
    s = s.replace(old, new, count)
    done.append(label)

# ---------------- ۱) کلیدهای تازه‌ی چهارزبانه ----------------
KEYS = {
 'fa': ('pay_time:"زمان پرداخت", pay_save:"ذخیره شد", ',
  'pay_page_t:"پرداخت هزینه‌ی نمایش آگهی‌ها", pay_back:"بازگشت", '
  'pay_locked_note:"🔒 {n} آگهی آماده‌ی نمایش است؛ برای باز شدن، هزینه‌ی نمایش آگهی‌ها را پرداخت کنید.", '
  'pay_view_status:"مشاهده‌ی وضعیت پرداخت", pay_open:"دیدن آگهی‌ها", '
  'pay_receipt:"تأییدیه پرداخت", pay_reg_t:"پرداخت شما ثبت شد", pay_recheck:"بررسی مجدد وضعیت", '
  'pay_continue:"بازگشت به صفحه‌ی اصلی", '
  'pay_step1:"ثبت درخواست پرداخت", pay_step2:"تأیید توسط مدیر", '
  'pay_step3:"ادامه‌ی مراحل: دیدن آگهی‌ها، ارسال درخواست و امضای تعهدنامه", '
  'pay_after_paid:"پرداخت شما تأیید شد؛ همه‌ی آگهی‌ها باز است و می‌توانید مراحل را ادامه دهید.", '),
 'tr': ('pay_time:"Ödeme zamanı", pay_save:"Kaydedildi", ',
  'pay_page_t:"İlan gösterim ücretini öde", pay_back:"Geri", '
  'pay_locked_note:"🔒 {n} ilan gösterime hazır; açılması için ilan gösterim ücretini ödeyin.", '
  'pay_view_status:"Ödeme durumunu gör", pay_open:"İlanları gör", '
  'pay_receipt:"Ödeme makbuzu", pay_reg_t:"Ödemeniz kaydedildi", pay_recheck:"Durumu yeniden kontrol et", '
  'pay_continue:"Ana sayfaya dön", '
  'pay_step1:"Ödeme talebi kaydı", pay_step2:"Yönetici onayı", '
  'pay_step3:"Devam: ilanları görme, başvuru gönderme ve taahhüt imzası", '
  'pay_after_paid:"Ödemeniz onaylandı; tüm ilanlar açık, adımlara devam edebilirsiniz.", '),
 'en': ('pay_time:"Payment time", pay_save:"Saved", ',
  'pay_page_t:"Pay the ads viewing fee", pay_back:"Back", '
  'pay_locked_note:"🔒 {n} ads are waiting; pay the ads viewing fee to unlock them.", '
  'pay_view_status:"View payment status", pay_open:"View ads", '
  'pay_receipt:"Payment receipt", pay_reg_t:"Your payment is recorded", pay_recheck:"Check status again", '
  'pay_continue:"Back to home", '
  'pay_step1:"Payment request recorded", pay_step2:"Admin confirmation", '
  'pay_step3:"Continue: view ads, apply and sign the undertaking", '
  'pay_after_paid:"Your payment is confirmed; all ads are open and you can continue the steps.", '),
 'ar': ('pay_time:"وقت الدفع", pay_save:"تم الحفظ", ',
  'pay_page_t:"ادفع رسوم عرض الإعلانات", pay_back:"رجوع", '
  'pay_locked_note:"🔒 {n} إعلانًا جاهز للعرض؛ ادفع رسوم عرض الإعلانات لفتحها.", '
  'pay_view_status:"عرض حالة الدفع", pay_open:"عرض الإعلانات", '
  'pay_receipt:"إيصال الدفع", pay_reg_t:"تم تسجيل دفعتك", pay_recheck:"تحقّق من الحالة مجددًا", '
  'pay_continue:"العودة إلى الصفحة الرئيسية", '
  'pay_step1:"تسجيل طلب الدفع", pay_step2:"تأكيد المدير", '
  'pay_step3:"المتابعة: مشاهدة الإعلانات وإرسال الطلبات وتوقيع التعهد", '
  'pay_after_paid:"تم تأكيد دفعتك؛ جميع الإعلانات مفتوحة ويمكنك متابعة الخطوات.", '),
}
for lg, (anchor, ins) in KEYS.items():
    rep(anchor, ins + anchor, f'i18n:{lg}')

# ---------------- ۲) صفحه‌ی اصلی: همان صفحه، فقط بخش آگهی‌ها پوشیده ----------------
rep('''function screenHome(){
  // #34: تا هزینه پرداخت و توسط مدیر تأیید نشده، هیچ آگهی‌ای نمایش داده نمی‌شود
  if(!userUnlocked()) return screenPaywall();
  const f=S.filters, list=filtered();''',
'''function screenHome(){
  // #35: صفحه‌ی اصلی دست‌نخورده باز می‌شود (همان سرتیتر، جست‌وجو و فیلترها)؛
  // فقط بخش آگهی‌ها تا تأیید پرداخت، با پنل «نیاز به پرداخت» پوشیده می‌شود.
  const locked=!userUnlocked();
  const f=S.filters, list=filtered();''',
    'home:keep page, lock ads only')

rep('''  ${list.length? list.map(jobCard).join("") : `<div class="empty"><div class="big">🔍</div><b>${t("noJobs")}</b><div class="small" style="margin-top:8px">${t("noJobsSub")}</div><div style="height:12px"></div><button class="btn btn-ghost btn-sm" onclick="clearF()">${t("clear")}</button></div>`}`;''',
'''  ${locked ? payLockPanel(list.length)
    : (list.length? list.map(jobCard).join("") : `<div class="empty"><div class="big">🔍</div><b>${t("noJobs")}</b><div class="small" style="margin-top:8px">${t("noJobsSub")}</div><div style="height:12px"></div><button class="btn btn-ghost btn-sm" onclick="clearF()">${t("clear")}</button></div>`)}`;''',
    'home:results area = lock panel or cards')

# ---------------- ۳) صفحه‌ی آگهی: هدایت به صفحه‌ی پرداخت ----------------
rep('''  // #34: صفحه‌ی آگهی هم برای کارجوی پرداخت‌نکرده قفل است
  if(!userUnlocked()) return screenPaywall();''',
'''  // #35: برای آگهی هم اگر پرداخت تأیید نشده باشد، به صفحه‌ی پرداخت هدایت می‌شود
  if(!userUnlocked()) return screenPay();''',
    'jobview → payment page')

# ---------------- ۴) توابع: پنل قفل، صفحه‌ی پرداخت، تأییدیه پرداخت ----------------
i = s.find('/** صفحه‌ی قفل: کارجو تا پرداخت و تأیید مدیر آگهی‌ای نمی‌بیند */')
j = s.find('/** عنوان آگهی (کلید یا متن آزاد) برای نمایش در کارت فعالیت */')
assert i > 0 and j > i, 'screenPaywall block not found'
NEW = '''/* ----------------------------------------------------------
   #35 — مسیر پرداخت هزینه‌ی نمایش آگهی‌ها
   ۱) صفحه‌ی اصلی: همان صفحه‌ی قبلی، فقط بخش آگهی‌ها پوشیده
   ۲) صفحه‌ی پرداخت: مبلغ، کارت کارگزار، کد پیگیری، دکمه‌ی «واریز کردم»
   ۳) تأییدیه پرداخت: رسید + وضعیت «در انتظار تأیید مدیر» + ادامه‌ی مراحل
   ---------------------------------------------------------- */
/** پنل قفل داخل صفحه‌ی اصلی — جای فهرست آگهی‌ها می‌نشیند (بقیه‌ی صفحه دست‌نخورده است) */
function payLockPanel(n){
  const me=meRec(), st=payOf(me), pending=(st==="pending");
  return `<div class="card" style="text-align:center;margin-bottom:8px">
    <div style="font-size:38px;line-height:1.2">${pending?"⏳":"🔒"}</div>
    <h3 style="font-size:15px;font-weight:900;margin:8px 0">${pending?t("pay_pending_t"):t("pay_unpaid_t")}</h3>
    <p class="small muted" style="line-height:2;margin:0 0 12px">${t("pay_locked_note",{n:num(n)})}</p>
    <div class="kv"><span>${t("pay_fee")}</span><b>${money(adsFeeVal())}</b></div>
    <div class="kv"><span>${t("pay_card")}</span><b dir="ltr">${esc(S.broker?.card||DEFAULT_CARD)}</b></div>
    <div class="kv"><span>${t("pay_ref")}</span><b dir="ltr">${trackCode(me)}</b></div>
    <div style="height:12px"></div>
    <button class="btn btn-primary" onclick="go('pay')">${pending?"⏳ "+t("pay_view_status"):"💳 "+t("pay_my")}</button>
  </div>`;
}
/** سرتیتر مشترک صفحه‌ی پرداخت و تأییدیه */
function payPageHead(){
  return `<div class="kb-head" style="background:linear-gradient(135deg,#088080,#0A7F91 55%,#0A7F91);padding:16px;margin:-16px -16px 16px;color:#FFFFFF">
    <div style="display:flex;align-items:center;gap:12px">
      <button class="iconbtn" onclick="go('home')">${backArrow()}</button>
      <div style="flex:1;min-width:0">
        <div style="font-size:15px;font-weight:900">${t("pay_page_t")}</div>
        <div style="font-size:12px;opacity:.85;font-weight:700">${esc(S.session?.first||"")} · ${esc(S.session?.city||"Türkiye")}</div>
      </div>
      <span style="font-size:20px">💳</span>
    </div>
  </div>`;
}
/** ردیف‌های رسید پرداخت (مبلغ، کارت، نام، کد پیگیری، زمان) */
function payReceiptRows(me){
  return `
    <div class="kv"><span>${t("pay_fee")}</span><b>${money(adsFeeVal())}</b></div>
    <div class="kv"><span>${t("pay_card")}</span><b dir="ltr">${esc(S.broker?.card||DEFAULT_CARD)}</b></div>
    <div class="kv"><span>${t("pay_holder")}</span><b>${esc(S.broker?.holder||"Karbama")}</b></div>
    <div class="kv"><span>${t("pay_ref")}</span><b dir="ltr">${trackCode(me)}</b></div>
    ${(me&&me.payAt)?`<div class="kv"><span>${t("pay_time")}</span><b>${agoFmt(me.payAt)}</b></div>`:""}`;
}
/** مراحل بعدی پرداخت (۱ ثبت شد · ۲ منتظر مدیر · ۳ ادامه‌ی کارجو) */
function paySteps(paid){
  const row=(n,txt,state)=>`<div style="display:flex;align-items:center;gap:8px;padding:6px 0">
    <span class="badge" style="flex:0 0 auto;${state==="done"?"background:#E4F7EC;border-color:#A7F3D0;color:#0E7A44":state==="now"?"background:#FFF4DE;border-color:#FDE68A;color:#7A5310":"background:#F1F5F7;border-color:#E3ECF0;color:#57697A"}">${state==="done"?"✅":state==="now"?"⏳":num(n)}</span>
    <span class="small" style="font-weight:700">${txt}</span></div>`;
  return `<div style="height:8px"></div>
    <div class="hr"></div>
    ${row("۱",t("pay_step1"),"done")}
    ${row("۲",t("pay_step2"),paid?"done":"now")}
    ${row("۳",t("pay_step3"),paid?"now":"")}`;
}
/** ۲) صفحه‌ی پرداخت — اگر پرداخت ثبت شده باشد، همان تأییدیه نشان داده می‌شود */
function screenPay(){
  const me=meRec(), st=payOf(me);
  if(st==="pending" || st==="paid") return screenPayDone();
  const body=`
  ${payPageHead()}
  <div class="card">
    <div class="sec-title"><span class="dot"></span>${t("pay_ads_fee")}</div>
    <p class="small muted" style="margin:-4px 0 12px;line-height:1.95">${t("pay_desc")}</p>
    ${payReceiptRows(me)}
  </div>
  <div style="height:8px"></div>
  <button class="btn btn-primary" onclick="requestPay()">💳 ${t("pay_send")}</button>
  <div style="height:8px"></div>
  <button class="btn btn-ghost" onclick="go('home')">${t("pay_back")}</button>
  <div class="small muted" style="margin-top:8px;line-height:1.95;text-align:center">🛡 ${t("pay_ads_fee_hint")}</div>`;
  return shell({body,tabbar:true});
}
/** ۳) تأییدیه پرداخت + ادامه‌ی مراحل */
function screenPayDone(){
  const me=meRec(), paid=(payOf(me)==="paid");
  const body=`
  ${payPageHead()}
  <div class="card" style="text-align:center">
    <div style="font-size:44px;line-height:1.2">${paid?"✅":"⏳"}</div>
    <h2 style="font-size:16px;font-weight:900;margin:8px 0">${paid?t("pay_confirmed"):t("pay_reg_t")}</h2>
    <p class="small muted" style="line-height:2;margin:0 0 12px">${paid?t("pay_after_paid"):t("pay_pending_d")}</p>
    <div class="sec-title" style="justify-content:center"><span class="dot"></span>🧾 ${t("pay_receipt")}</div>
    ${payReceiptRows(me)}
    ${paySteps(paid)}
    <div style="height:12px"></div>
    ${paid
      ? `<button class="btn btn-primary" onclick="go('home')">🔓 ${t("pay_open")}</button>`
      : `<button class="btn btn-primary" onclick="recheckPay()">🔄 ${t("pay_recheck")}</button>
         <div style="height:8px"></div>
         <button class="btn btn-ghost" onclick="go('home')">${t("pay_continue")}</button>`}
  </div>`;
  return shell({body,tabbar:true});
}
/** بررسی مجدد وضعیت: اگر مدیر تأیید کرده باشد، آگهی‌ها باز می‌شود */
function recheckPay(){
  const me=meRec();
  if(payOf(me)==="paid"){ toast("✅ "+t("pay_confirmed")); go("home"); return; }
  toast("⏳ "+t("pay_waiting")); render();
}
'''
s = s[:i] + NEW + '\n' + s[j:]
done.append('functions: payLockPanel/screenPay/screenPayDone/recheckPay')

# ---------------- ۵) requestPay → رفتن به تأییدیه پرداخت ----------------
rep('''  persist(); render(); toast("📨 "+t("pay_sent"));
}''',
'''  persist(); toast("📨 "+t("pay_sent")); go("paid");   // #35: بعد از واریز، تأییدیه‌ی پرداخت می‌آید
}''',
    'requestPay → paid screen')

# ---------------- ۶) مسیر صفحه‌ها ----------------
rep('''    person:screenPerson};''',
'''    person:screenPerson,
    pay:screenPay, paid:screenPayDone};        // #35: صفحه‌ی پرداخت و تأییدیه''',
    'render map: pay/paid')

# ---------------- ۷) دکمه‌های کارت پرداخت در پروفایل → صفحه‌ی پرداخت ----------------
rep('''         <button class="btn btn-primary" onclick="go('home')">🔓 ${t("pay_my")}</button>`}''',
    '''         <button class="btn btn-primary" onclick="go('pay')">💳 ${t("pay_my")}</button>`}''',
    'profile pay card → pay page')

F.write_text(s) if APPLY else None
print('--- #35 دمو:', 'اعمال شد ✅' if APPLY else 'گزارش فقط')
for d in done: print('   •', d)
print('طول فایل:', len(s))
