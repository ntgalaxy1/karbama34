#!/usr/bin/env python3
"""#34 — قفل آگهی‌ها تا پرداخت هزینه (دمو):
   کارجو تا هزینه‌ی «نمایش آگهی‌ها» را واریز نکند آگهی‌ای نمی‌بیند؛
   او دکمه‌ی «واریز کردم» را می‌زند → وضعیت «در انتظار تأیید» → مدیر تأیید می‌کند.
اجرا:  python3 kb-tools/patch-demo-34.py          (گزارش)
       python3 kb-tools/patch-demo-34.py --apply  (اعمال)
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

# ---------------- ۱) کلیدهای چهارزبانه ----------------
KEYS = {
'fa': '''    pay_title:"برای دیدن آگهی‌ها هزینه را پرداخت کنید", pay_unpaid_t:"آگهی‌ها قفل است",
    pay_desc:"برای مشاهده‌ی همه‌ی آگهی‌های استخدام، مبلغ زیر را به کارت کارگزار واریز کنید و دکمه‌ی «واریز کردم» را بزنید. به‌محض تأیید مدیر، همه‌ی آگهی‌ها باز می‌شود.",
    pay_fee:"مبلغ قابل پرداخت", pay_card:"کارت کارگزار", pay_holder:"به نام", pay_ref:"کد پیگیری",
    pay_send:"واریز کردم — ارسال برای تأیید",
    pay_pending_t:"در انتظار تأیید پرداخت", pay_pending_d:"درخواست پرداخت شما ثبت شد. به‌محض تأیید مدیر، همه‌ی آگهی‌ها نمایش داده می‌شود.",
    pay_sent:"درخواست بررسی پرداخت ثبت شد", pay_confirmed:"پرداخت تأیید شد", pay_unconfirmed:"تأیید پرداخت لغو شد",
    pay_sec:"وضعیت پرداخت", pay_st_unpaid:"پرداخت‌نشده", pay_st_pending:"در انتظار تأیید", pay_st_paid:"پرداخت‌شده",
    pay_confirm:"تأیید پرداخت", pay_cancel:"لغو تأیید",
    pay_ads_fee:"هزینه نمایش آگهی‌ها (لیر)", pay_ads_fee_hint:"مبلغی که کارجو برای دیدن همه‌ی آگهی‌ها پرداخت می‌کند؛ تا تأیید مدیر، آگهی‌ها قفل می‌ماند.",
    pay_my:"پرداخت هزینه و دیدن آگهی‌ها", pay_waiting:"منتظر تأیید مدیر باشید", pay_locked_tab:"آگهی‌ها 🔒",
''',
'tr': '''    pay_title:"İlanları görmek için ücreti ödeyin", pay_unpaid_t:"İlanlar kilitli",
    pay_desc:"Tüm iş ilanlarını görmek için aşağıdaki tutarı broker kartına yatırın ve «Yatırdım» düğmesine basın. Yönetici onaylar onaylamaz tüm ilanlar açılır.",
    pay_fee:"Ödenecek tutar", pay_card:"Broker kartı", pay_holder:"Adına", pay_ref:"Takip kodu",
    pay_send:"Yatırdım — onaya gönder",
    pay_pending_t:"Ödeme onayı bekleniyor", pay_pending_d:"Ödeme talebiniz kaydedildi. Yönetici onaylayınca tüm ilanlar görünecek.",
    pay_sent:"Ödeme inceleme talebi kaydedildi", pay_confirmed:"Ödeme onaylandı", pay_unconfirmed:"Ödeme onayı iptal edildi",
    pay_sec:"Ödeme durumu", pay_st_unpaid:"Ödenmedi", pay_st_pending:"Onay bekliyor", pay_st_paid:"Ödendi",
    pay_confirm:"Ödemeyi onayla", pay_cancel:"Onayı iptal et",
    pay_ads_fee:"İlan gösterim ücreti (TL)", pay_ads_fee_hint:"İş arayanın tüm ilanları görmek için ödediği tutar; yönetici onaylayana kadar ilanlar kilitli kalır.",
    pay_my:"Ücreti öde ve ilanları gör", pay_waiting:"Yönetici onayını bekleyin", pay_locked_tab:"İlanlar 🔒",
''',
'en': '''    pay_title:"Pay the fee to see the job ads", pay_unpaid_t:"Ads are locked",
    pay_desc:"To see all job ads, transfer the amount below to the broker card and press “I transferred”. Ads unlock as soon as the admin confirms.",
    pay_fee:"Amount to pay", pay_card:"Broker card", pay_holder:"Holder", pay_ref:"Tracking code",
    pay_send:"I transferred — send for approval",
    pay_pending_t:"Waiting for payment approval", pay_pending_d:"Your payment request was recorded. All ads appear as soon as the admin confirms.",
    pay_sent:"Payment review request recorded", pay_confirmed:"Payment confirmed", pay_unconfirmed:"Payment confirmation cancelled",
    pay_sec:"Payment status", pay_st_unpaid:"Unpaid", pay_st_pending:"Pending approval", pay_st_paid:"Paid",
    pay_confirm:"Confirm payment", pay_cancel:"Cancel confirmation",
    pay_ads_fee:"Ads viewing fee (TL)", pay_ads_fee_hint:"The amount a job seeker pays to see all ads; ads stay locked until the admin confirms.",
    pay_my:"Pay the fee and see ads", pay_waiting:"Wait for admin confirmation", pay_locked_tab:"Ads 🔒",
''',
'ar': '''    pay_title:"ادفع الرسوم لعرض الإعلانات", pay_unpaid_t:"الإعلانات مقفلة",
    pay_desc:"لمشاهدة جميع إعلانات العمل، حوّل المبلغ أدناه إلى بطاقة الوسيط ثم اضغط «قمت بالتحويل». تُفتح الإعلانات فور تأكيد المدير.",
    pay_fee:"المبلغ المطلوب", pay_card:"بطاقة الوسيط", pay_holder:"باسم", pay_ref:"رمز المتابعة",
    pay_send:"قمت بالتحويل — أرسل للتأكيد",
    pay_pending_t:"بانتظار تأكيد الدفع", pay_pending_d:"تم تسجيل طلب الدفع. ستظهر جميع الإعلانات فور تأكيد المدير.",
    pay_sent:"تم تسجيل طلب مراجعة الدفع", pay_confirmed:"تم تأكيد الدفع", pay_unconfirmed:"تم إلغاء تأكيد الدفع",
    pay_sec:"حالة الدفع", pay_st_unpaid:"غير مدفوع", pay_st_pending:"بانتظار التأكيد", pay_st_paid:"مدفوع",
    pay_confirm:"تأكيد الدفع", pay_cancel:"إلغاء التأكيد",
    pay_ads_fee:"رسوم عرض الإعلانات (ليرة)", pay_ads_fee_hint:"المبلغ الذي يدفعه الباحث عن عمل لمشاهدة جميع الإعلانات؛ تبقى الإعلانات مقفلة حتى تأكيد المدير.",
    pay_my:"ادفع الرسوم وشاهد الإعلانات", pay_waiting:"انتظر تأكيد المدير", pay_locked_tab:"الإعلانات 🔒",
''',
}
anchors = {
 'fa': '    pasandide:"پسندیده", like:"پسندیدم", liked:"پسندیده شد ✓",\n',
 'tr': '    pasandide:"Beğendiklerim", like:"Beğendim", liked:"Beğenildi ✓",\n',
 'en': '    pasandide:"Liked", like:"Like", liked:"Liked ✓",\n',
 'ar': '    pasandide:"المفضّلة",\n',
}
for lg in ['fa', 'tr', 'en', 'ar']:
    rep(anchors[lg], KEYS[lg] + anchors[lg], f'i18n:{lg}')

# ---------------- ۲) حالت و پیش‌فرض مبلغ ----------------
rep('const DEFAULT_CARD = "1234 1234 1234 1234";',
    'const DEFAULT_CARD = "1234 1234 1234 1234";\nconst DEFAULT_ADS_FEE = 6000;                        // #34: هزینه‌ی پیش‌فرض «نمایش آگهی‌ها» (در تنظیمات مدیر قابل تغییر)',
    'const:DEFAULT_ADS_FEE')

rep('''  broker:{ card:"1234 1234 1234 1234", holder:"Karbama" },''',
    '''  broker:{ card:"1234 1234 1234 1234", holder:"Karbama" },
  adsFee:String(DEFAULT_ADS_FEE),                                       // #34: هزینه‌ی نمایش آگهی‌ها (تنظیمات مدیر)''',
    'state:adsFee')

rep('''  store.set("kb_2fa", JSON.stringify(S.twoFA));''',
    '''  store.set("kb_2fa", JSON.stringify(S.twoFA));
  store.set("kb_adsFee", String(S.adsFee||DEFAULT_ADS_FEE));   // #34''',
    'persist:kb_adsFee')

rep('''  try{ const s=store.get("kb_session"); if(s) S.session=JSON.parse(s); }catch(e){}''',
    '''  try{ const s=store.get("kb_session"); if(s) S.session=JSON.parse(s); }catch(e){}
  try{ const f=store.get("kb_adsFee"); if(f) S.adsFee=f; }catch(e){}   // #34''',
    'restore:kb_adsFee')

# ---------------- ۳) توابع پرداخت ----------------
HELPERS = '''/* ==========================================================
   #34 — قفل آگهی‌ها تا پرداخت هزینه (تأیید توسط مدیر)
   وضعیت روی خودِ رکورد متقاضی است: p.pay = "" | "pending" | "paid"
   در اپ اندرویدی همان فیلد روی Firestore → applicants/{uid}.paymentStatus
   ========================================================== */
/** وضعیت پرداخت یک کارجو */
function payOf(p){ return (p && p.pay) || ""; }
/** رکورد کارجوی وارد‌شده (اگر در فهرست نبود، خودِ ورود) */
function meRec(){
  if(!S.session) return null;
  return S.people.find(x=>x.id===S.session.id) || S.session;
}
/** مبلغ نمایش آگهی‌ها (تنظیمات مدیر) */
function adsFeeVal(){ const v=+String(S.adsFee||"").replace(/\\D/g,""); return v>0 ? v : DEFAULT_ADS_FEE; }
/** آیا این کاربر می‌تواند آگهی‌ها را ببیند؟ (مدیر همیشه؛ کارجو فقط با پرداخت تأییدشده) */
function userUnlocked(){ return !!(S.isAdmin || payOf(meRec())==="paid"); }
/** کد پیگیری یکتای هر کارجو (کاربر به مدیر می‌گوید، مدیر با آن تأیید می‌کند) */
function trackCode(p){
  const base=String((p&&(p.username||p.id))||"karbama");
  let h=7; for(let i=0;i<base.length;i++) h=(h*31+base.charCodeAt(i))%90000;
  return "KB-"+(10000+h);
}
/** نشان وضعیت پرداخت */
function payBadge(p){
  const st=payOf(p);
  const map={
    "":[ "⛔", t("pay_st_unpaid"), "background:#FEF0F0;border-color:#FFE3ED;color:#E5484D" ],
    "pending":[ "⏳", t("pay_st_pending"), "background:#FFF4DE;border-color:#FDE68A;color:#7A5310" ],
    "paid":[ "✅", t("pay_st_paid"), "background:#E4F7EC;border-color:#A7F3D0;color:#0E7A44" ] };
  const m=map[st]||map[""];
  return `<span class="badge" style="${m[2]}">${m[0]} ${m[1]}</span>`;
}
/** کارجو: «واریز کردم» → وضعیت در انتظار تأیید */
function requestPay(){
  const me=meRec(); if(!me) return;
  me.pay="pending"; me.payAt=Date.now();
  if(S.session && S.session.id===me.id) S.session.pay="pending";
  persist(); render(); toast("📨 "+t("pay_sent"));
}
/** مدیر: تأیید پرداخت */
function adminPayConfirm(id){
  const p=S.people.find(x=>x.id===id); if(!p) return;
  p.pay="paid"; p.payAt=Date.now();
  if(S.session && S.session.id===p.id) S.session.pay="paid";
  persist(); render(); renderOverlay(); toast("✅ "+t("pay_confirmed"));
}
/** مدیر: لغو تأیید پرداخت */
function adminPayCancel(id){
  const p=S.people.find(x=>x.id===id); if(!p) return;
  p.pay=""; p.payAt=0;
  if(S.session && S.session.id===p.id) S.session.pay="";
  persist(); render(); renderOverlay(); toast("↩︎ "+t("pay_unconfirmed"));
}
/** مدیر: ذخیره‌ی مبلغ نمایش آگهی‌ها */
function saveAdsFee(){
  const v=+String(S.adsFee||"").replace(/\\D/g,"");
  S.adsFee=String(v>0?v:DEFAULT_ADS_FEE);
  persist(); render(); toast("✅ "+t("pay_save"));
}
/** صفحه‌ی قفل: کارجو تا پرداخت و تأیید مدیر آگهی‌ای نمی‌بیند */
function screenPaywall(){
  const me=meRec(), st=payOf(me), pending=(st==="pending");
  const body=`
  <div class="kb-head" style="background:linear-gradient(135deg,#088080,#0A7F91 55%,#087A8A);padding:16px;margin:-16px -16px 16px;color:#FFFFFF">
    <div style="display:flex;align-items:center;gap:12px">
      <img src="${LOGO_SM}" alt="Karbama" width="40" height="40"
        style="width:40px;height:40px;border-radius:12px;object-fit:cover;display:block">
      <div style="flex:1;min-width:0">
        <div style="font-size:15px;font-weight:900">${t("tabJobs")}</div>
        <div style="font-size:12px;opacity:.85;font-weight:700">${esc(S.session?.first||"")} · ${esc(S.session?.city||"Türkiye")}</div>
      </div>
      <span style="font-size:20px">🔒</span>
    </div>
  </div>

  <div class="card" style="text-align:center">
    <div style="font-size:44px">${pending?"⏳":"🔒"}</div>
    <h2 style="font-size:16px;font-weight:900;margin:8px 0 8px">${pending?t("pay_pending_t"):t("pay_unpaid_t")}</h2>
    <p class="small muted" style="line-height:2;margin:0 0 12px">${pending?t("pay_pending_d"):t("pay_desc")}</p>
    <div class="kv"><span>${t("pay_fee")}</span><b>${money(adsFeeVal())}</b></div>
    <div class="kv"><span>${t("pay_card")}</span><b dir="ltr">${esc(S.broker?.card||DEFAULT_CARD)}</b></div>
    <div class="kv"><span>${t("pay_holder")}</span><b>${esc(S.broker?.holder||"Karbama")}</b></div>
    <div class="kv"><span>${t("pay_ref")}</span><b dir="ltr">${trackCode(me)}</b></div>
    ${pending
      ? `<div style="height:12px"></div><div class="small muted">⏳ ${t("pay_waiting")}</div>`
      : `<div style="height:12px"></div><button class="btn btn-primary" onclick="requestPay()">💳 ${t("pay_send")}</button>`}
  </div>
  `;
  return shell({body,tabbar:true});
}
'''
anchor = '/** عنوان آگهی (کلید یا متن آزاد) برای نمایش در کارت فعالیت */'
rep(anchor, HELPERS + anchor, 'helpers')

# ---------------- ۴) قفل صفحه‌های آگهی ----------------
rep('''function screenHome(){
  const f=S.filters, list=filtered();''',
    '''function screenHome(){
  // #34: تا هزینه پرداخت و توسط مدیر تأیید نشده، هیچ آگهی‌ای نمایش داده نمی‌شود
  if(!userUnlocked()) return screenPaywall();
  const f=S.filters, list=filtered();''',
    'guard:home')

rep('''function screenJobView(){
  const j=S.jobs.find(x=>x.id===S.viewJob);''',
    '''function screenJobView(){
  // #34: صفحه‌ی آگهی هم برای کارجوی پرداخت‌نکرده قفل است
  if(!userUnlocked()) return screenPaywall();
  const j=S.jobs.find(x=>x.id===S.viewJob);''',
    'guard:jobpage')

# ---------------- ۵) تب «آگهی‌ها» با قفل ----------------
rep('''  const items=[["home","tabJobs","🏠"],["profile","tabProfile","👤"],["contact","tabContact","☎️"]];
  return `<div class="tabbar">${items.map(([id,lbl,ico])=>
    `<button class="${S.screen===id?"on":""}" onclick="go('${id}')"><span class="ti">${ico}</span>${t(lbl)}</button>`).join("")}</div>`;''',
    '''  const items=[["home","tabJobs","🏠"],["profile","tabProfile","👤"],["contact","tabContact","☎️"]];
  const locked=!userUnlocked();      // #34: تب آگهی‌ها تا پرداخت، قفل است
  return `<div class="tabbar">${items.map(([id,lbl,ico])=>
    (id==="home" && locked)
      ? `<button class="${S.screen===id?"on":""}" onclick="go('home')"><span class="ti">🔒</span>${t("pay_locked_tab")}</button>`
      : `<button class="${S.screen===id?"on":""}" onclick="go('${id}')"><span class="ti">${ico}</span>${t(lbl)}</button>`).join("")}</div>`;''',
    'tabbar:lock')

# ---------------- ۶) کارت متقاضی در پنل مدیر: نشان پرداخت ----------------
rep('''      <div style="text-align:end;flex:0 0 auto">${statusBadge(p.status)}<div class="small muted" style="margin-top:8px">${agoFmt(p.createdAt)}</div></div>''',
    '''      <div style="text-align:end;flex:0 0 auto">${statusBadge(p.status)}<div style="margin-top:8px">${payBadge(p)}</div><div class="small muted" style="margin-top:8px">${agoFmt(p.createdAt)}</div></div>''',
    'admin:peopleCardBadge')

# ---------------- ۷) صفحه‌ی کامل متقاضی: بخش پرداخت ----------------
rep('''  ${activitySec(p)}
  <div class="sec-title" style="margin-top:16px"><span class="dot"></span>${t("und_personFee")}</div>''',
    '''  ${paySec(p)}
  ${activitySec(p)}
  <div class="sec-title" style="margin-top:16px"><span class="dot"></span>${t("und_personFee")}</div>''',
    'admin:personPaySection')

rep('''/** ذخیره‌ی یوزرنیم و پسورد مدیر (از تنظیمات پنل) */''',
    '''/** #34: بخش «وضعیت پرداخت» در صفحه‌ی کامل متقاضی (فقط مدیر تأیید می‌کند) */
function paySec(p){
  const st=payOf(p);
  return `
  <div class="sec-title" style="margin-top:16px"><span class="dot"></span>💳 ${t("pay_sec")}</div>
  <div class="card" style="margin-top:8px">
    <div class="kv"><span>${t("pay_fee")}</span><b>${money(adsFeeVal())}</b></div>
    <div class="kv"><span>${t("pay_ref")}</span><b dir="ltr">${trackCode(p)}</b></div>
    <div class="kv"><span>${t("pay_sec")}</span><b>${payBadge(p)}</b></div>
    ${p.payAt?`<div class="kv"><span>${t("date")}</span><b>${agoFmt(p.payAt)}</b></div>`:""}
    <div style="height:12px"></div>
    ${st==="paid"
      ? `<button class="btn btn-ghost" onclick="adminPayCancel('${p.id}')">↩︎ ${t("pay_cancel")}</button>`
      : `<button class="btn btn-primary" onclick="adminPayConfirm('${p.id}')">✅ ${t("pay_confirm")}</button>`}
  </div>`;
}
/** ذخیره‌ی یوزرنیم و پسورد مدیر (از تنظیمات پنل) */''',
    'admin:paySecFn')

# ---------------- ۸) تنظیمات مدیر: مبلغ نمایش آگهی‌ها ----------------
rep('''    <div class="field"><label class="adm-lbl">${t("und_adminCard")}</label>
      <input class="adm-inp" inputmode="numeric" dir="ltr" value="${esc(S.broker.card||"")}" placeholder="${DEFAULT_CARD}"
             oninput="S.broker.card=cardFmt(this.value)">
      <div class="small muted adm-note">${t("und_cardHint")}</div></div>''',
    '''    <div class="field"><label class="adm-lbl">${t("und_adminCard")}</label>
      <input class="adm-inp" inputmode="numeric" dir="ltr" value="${esc(S.broker.card||"")}" placeholder="${DEFAULT_CARD}"
             oninput="S.broker.card=cardFmt(this.value)">
      <div class="small muted adm-note">${t("und_cardHint")}</div></div>
    <div class="hr"></div>
    <div class="sec-title"><span class="dot"></span>${t("pay_ads_fee")}</div>
    <p class="small muted" style="margin:-4px 0 12px;line-height:1.95">${t("pay_ads_fee_hint")}</p>
    <div class="field"><label class="adm-lbl">${t("pay_ads_fee")}</label>
      <input class="adm-inp" inputmode="numeric" dir="ltr" id="ads_fee" value="${adsFeeVal()}"
             oninput="S.adsFee=digitsOnly(this.value,9)">
      <div class="small muted adm-note">${money(adsFeeVal())}</div></div>
    <button class="btn btn-primary" onclick="saveAdsFee()">💾 ${t("save")}</button>''',
    'admin:adsFeeField')

# ---------------- ۹) پروفایل کارجو: نمایش وضعیت پرداخت ----------------
rep('''function screenProfile(){
  const u=S.session;''', '''function screenProfile(){
  const u=S.session;
  const myPay=u?payOf(meRec()):"";        // #34: وضعیت پرداخت همین کارجو''', 'profile:myPay')

rep('''  </div>
  <div class="card">
    <div class="sec-title"><span class="dot"></span>${t("myJobs")}</div>''',
    '''  </div>
  <div class="card">
    <div class="sec-title"><span class="dot"></span>💳 ${t("pay_sec")}</div>
    <div style="display:flex;align-items:center;justify-content:space-between;gap:8px">
      ${payBadge(meRec())}
      <b style="font-size:14px">${money(adsFeeVal())}</b>
    </div>
    ${myPay==="paid"
      ? `<div class="small muted" style="margin-top:8px">✅ ${t("pay_st_paid")}${u.payAt?" · "+agoFmt(u.payAt):""}</div>`
      : `<div class="small muted" style="margin-top:8px;line-height:1.9">${t("pay_ads_fee_hint")}</div>
         <div style="height:12px"></div>
         <button class="btn btn-primary" onclick="go('home')">🔓 ${t("pay_my")}</button>`}
  </div>
  <div class="card">
    <div class="sec-title"><span class="dot"></span>${t("myJobs")}</div>''', 'profile:payCard')

F.write_text(s) if APPLY else None
print('--- #34 دمو:', 'اعمال شد ✅' if APPLY else 'گزارش فقط')
for d in done: print('   •', d)
print('طول فایل:', len(s))
