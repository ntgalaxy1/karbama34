#!/usr/bin/env python3
"""#34 — قفل آگهی‌ها تا پرداخت هزینه (اندروید)
   • مدل: Applicant.payStatus ("", pending, paid) + payAt  → Firestore: paymentStatus / paymentAt
   • تنظیمات مدیر: مبلغ «هزینه نمایش آگهی‌ها» (پیش‌فرض ۶۰۰۰)
   • کارجو: صفحه‌ی قفل با کارت کارگزار و دکمه‌ی «واریز کردم»
   • مدیر: تأیید/لغو پرداخت روی صفحه‌ی کامل متقاضی + نشان وضعیت روی کارت و فهرست
اجرا:  python3 kb-tools/patch-android-34.py          (گزارش)
       python3 kb-tools/patch-android-34.py --apply  (اعمال)
"""
import sys, pathlib

ROOT = pathlib.Path('/home/user/Karbama')
S = ROOT / 'app/src/main/java/com/karbama/app'
APPLY = '--apply' in sys.argv
log = []

def patch(rel, pairs):
    f = ROOT / rel
    s = f.read_text()
    for old, new, label in pairs:
        if new in s and label.endswith('*'):
            log.append(f'{label} (از قبل بود)'); continue
        assert s.count(old) == 1, f'{label}: anchor count = {s.count(old)}'
        s = s.replace(old, new, 1)
        log.append(label)
    if APPLY:
        f.write_text(s)

# ============================================================
# ۱) Config — مبلغ پیش‌فرض نمایش آگهی‌ها
# ============================================================
patch('app/src/main/java/com/karbama/app/data/Config.kt', [(
    '    const val DEFAULT_BROKER_CARD = "1234 1234 1234 1234"',
    '    const val DEFAULT_BROKER_CARD = "1234 1234 1234 1234"\n\n'
    '    /** #34: مبلغ پیش‌فرض «هزینه نمایش آگهی‌ها» — کارجو تا پرداخت و تأیید مدیر، آگهی نمی‌بیند */\n'
    '    const val DEFAULT_ADS_FEE = 6000L',
    'Config: DEFAULT_ADS_FEE*')])

# ============================================================
# ۲) مدل متقاضی
# ============================================================
patch('app/src/main/java/com/karbama/app/data/model/Models.kt', [(
    '    val undertaking: Undertaking? = null\n) {\n'
    '    val fullName: String get() = "$firstName $lastName".trim()',
    '    val undertaking: Undertaking? = null,\n'
    '    // #34: وضعیت پرداخت «هزینه نمایش آگهی‌ها» — "" پرداخت‌نشده | pending در انتظار تأیید | paid تأییدشده\n'
    '    val payStatus: String = "",\n'
    '    val payAt: Long = 0L\n) {\n'
    '    val fullName: String get() = "$firstName $lastName".trim()\n\n'
    '    /** #34: آیا مدیر پرداخت این کارجو را تأیید کرده؟ (تنها راه باز شدن آگهی‌ها) */\n'
    '    val adsPaid: Boolean get() = payStatus == "paid"\n\n'
    '    /** #34: کد پیگیری پرداخت این کارجو (کاربر به مدیر می‌گوید، مدیر با آن تأیید می‌کند) */\n'
    '    val payTrackCode: String get() {\n'
    '        val base = username.ifEmpty { id } .ifEmpty { uid }\n'
    '        var h = 7\n'
    '        base.forEach { h = (h * 31 + it.code) % 90000 }\n'
    '        return "KB-" + (10000 + h)\n'
    '    }',
    'Models: Applicant.payStatus/payAt*')])

# ============================================================
# ۳) AppPrefs — مبلغ نمایش آگهی‌ها + JSON
# ============================================================
patch('app/src/main/java/com/karbama/app/data/local/AppPrefs.kt', [
 ('    var brokerHolder: String\n'
  '        get() = sp.getString(K_BROKER_HOLDER, "Karbama") ?: "Karbama"\n'
  '        set(v) = sp.edit().putString(K_BROKER_HOLDER, v).apply()',
  '    var brokerHolder: String\n'
  '        get() = sp.getString(K_BROKER_HOLDER, "Karbama") ?: "Karbama"\n'
  '        set(v) = sp.edit().putString(K_BROKER_HOLDER, v).apply()\n\n'
  '    // ---------- #34: هزینه‌ی نمایش آگهی‌ها (تنظیمات مدیر) ----------\n\n'
  '    var adsFee: Long\n'
  '        get() = sp.getLong(K_ADS_FEE, Config.DEFAULT_ADS_FEE)\n'
  '        set(v) = sp.edit().putLong(K_ADS_FEE, v).apply()',
  'AppPrefs: adsFee*'),
 ('        const val K_BROKER_CARD = "broker_card"',
  '        const val K_BROKER_CARD = "broker_card"\n'
  '        const val K_ADS_FEE = "ads_fee"            // #34',
  'AppPrefs: K_ADS_FEE*'),
 ('    put("phoneLocal", phoneLocal)\n    put("undertaking", undertaking?.toJson())',
  '    put("phoneLocal", phoneLocal)\n'
  '    put("payStatus", payStatus)          // #34\n'
  '    put("payAt", payAt)                  // #34\n'
  '    put("undertaking", undertaking?.toJson())',
  'AppPrefs.toJson: payStatus*'),
 ('    phoneLocal = o.optString("phoneLocal", ""),',
  '    phoneLocal = o.optString("phoneLocal", ""),\n'
  '    payStatus = o.optString("payStatus", ""),      // #34\n'
  '    payAt = o.optLong("payAt", 0L),                // #34',
  'AppPrefs.jsonToApplicant: payStatus*')])

# ============================================================
# ۴) FirestoreMapper — paymentStatus / paymentAt
# ============================================================
patch('app/src/main/java/com/karbama/app/data/repo/FirestoreMapper.kt', [
 ('    phoneLocal = getString("phoneLocal") ?: "",',
  '    phoneLocal = getString("phoneLocal") ?: "",\n'
  '    payStatus = getString("paymentStatus") ?: "",   // #34\n'
  '    payAt = getLong("paymentAt") ?: 0L,             // #34',
  'Mapper.read: paymentStatus*'),
 ('    "phoneLocal" to phoneLocal,',
  '    "phoneLocal" to phoneLocal,\n'
  '    "paymentStatus" to payStatus,      // #34\n'
  '    "paymentAt" to payAt,              // #34',
  'Mapper.write: paymentStatus*')])

# ============================================================
# ۵) LocalStore — setPayment (حالت بدون Firebase)
# ============================================================
patch('app/src/main/java/com/karbama/app/data/local/LocalStore.kt', [(
    '    /** تغییر مبلغ تعهدنامه‌ی یک متقاضی خاص (فقط مدیر) */',
    '    /** #34: تغییر وضعیت پرداخت هزینه‌ی نمایش آگهی‌ها (کارجو: pending — مدیر: paid/خالی) */\n'
    '    fun setPayment(id: String, status: String, at: Long) {\n'
    '        applicants.value.firstOrNull { it.id == id }?.let {\n'
    '            upsertApplicant(it.copy(payStatus = status, payAt = at))\n'
    '        }\n'
    '    }\n\n'
    '    /** تغییر مبلغ تعهدنامه‌ی یک متقاضی خاص (فقط مدیر) */',
    'LocalStore: setPayment*')])

# ============================================================
# ۶) ApplicantRepository — setPayment
# ============================================================
patch('app/src/main/java/com/karbama/app/data/repo/ApplicantRepository.kt', [(
    '    /**\n     * تغییر مبلغ تعهدنامه‌ی یک متقاضی خاص — از پنل مدیریت.',
    '    /**\n'
    '     * #34: ثبت وضعیت پرداخت «هزینه نمایش آگهی‌ها».\n'
    '     * کارجو فقط «pending» می‌گذارد؛ تأیید («paid») کارِ مدیر است.\n'
    '     */\n'
    '    suspend fun setPayment(applicant: Applicant, status: String): Result<Unit> {\n'
    '        val at = if (status.isEmpty()) 0L else System.currentTimeMillis()\n'
    '        val db = Service.db\n'
    '        if (db == null) {\n'
    '            LocalStore.setPayment(applicant.id, status, at)\n'
    '            return Result.success(Unit)\n'
    '        }\n'
    '        return runCatching {\n'
    '            db.collection(Config.COL_APPLICANTS).document(applicant.id)\n'
    '                .update(mapOf("paymentStatus" to status, "paymentAt" to at)).await()\n'
    '        }\n'
    '    }\n\n'
    '    /**\n     * تغییر مبلغ تعهدنامه‌ی یک متقاضی خاص — از پنل مدیریت.',
    'Repository: setPayment*')])

# ============================================================
# ۷) AppViewModel — وضعیت قفل، درخواست پرداخت، تأیید مدیر، مبلغ
# ============================================================
patch('app/src/main/java/com/karbama/app/ui/AppViewModel.kt', [
 ('    val brokerFee: Long get() = Config.DEFAULT_FEE',
  '    val brokerFee: Long get() = Config.DEFAULT_FEE\n\n'
  '    // ---------- #34: هزینه‌ی نمایش آگهی‌ها ----------\n\n'
  '    var adsFee by mutableStateOf(prefs.adsFee)\n'
  '        private set\n\n'
  '    /** مبلغ «هزینه نمایش آگهی‌ها» را ذخیره می‌کند (فقط از پنل مدیریت) */\n'
  '    fun saveAdsFee(value: Long) {\n'
  '        val safe = if (value > 0L) value else Config.DEFAULT_ADS_FEE\n'
  '        adsFee = safe\n'
  '        prefs.adsFee = safe\n'
  '        message = "✅ " + safe\n'
  '    }\n\n'
  '    /** مدیر همیشه آگهی‌ها را می‌بیند؛ کارجو تنها وقتی مدیر پرداختش را تأیید کرده باشد */\n'
  '    val adsUnlocked: Boolean\n'
  '        get() = adminMode != AdminMode.NONE || (myProfile?.payStatus == "paid")\n\n'
  '    /** «واریز کردم» — کارجو درخواست بررسی پرداخت را می‌فرستد (منتظر تأیید مدیر) */\n'
  '    fun requestAdsPayment() {\n'
  '        val me = myProfile ?: return\n'
  '        val updated = me.copy(payStatus = "pending", payAt = System.currentTimeMillis())\n'
  '        myProfile = updated\n'
  '        prefs.saveMyProfile(updated)\n'
  '        viewModelScope.launch { applicantRepo.setPayment(updated, "pending") }\n'
  '        message = "📨 " + updated.payTrackCode\n'
  '    }\n\n'
  '    /** مدیر: تأیید (paid) یا لغو ("" ) پرداخت یک متقاضی */\n'
  '    fun setApplicantPayment(applicant: com.karbama.app.data.model.Applicant, status: String) {\n'
  '        viewModelScope.launch {\n'
  '            applicantRepo.setPayment(applicant, status)\n'
  '            message = if (status == "paid") "✅ " + applicant.fullName\n'
  '            else "↩︎ " + applicant.fullName\n'
  '        }\n'
  '    }\n\n'
  '    /** کارجو: خواندن دوباره‌ی وضعیت پرداخت (وقتی مدیر تأیید می‌کند) */\n'
  '    fun refreshMyPayment(onDone: (String) -> Unit = {}) {\n'
  '        viewModelScope.launch {\n'
  '            val me = myProfile ?: return@launch\n'
  '            val uid = Service.currentUid\n'
  '            val remote = if (Service.ready && uid != null) applicantRepo.myProfile(uid) else null\n'
  '            val fresh = remote ?: LocalStore.applicants.value.firstOrNull { it.id == me.id }\n'
  '            if (fresh != null) {\n'
  '                myProfile = fresh\n'
  '                prefs.saveMyProfile(fresh)\n'
  '                onDone(fresh.payStatus)\n'
  '            } else onDone(me.payStatus)\n'
  '        }\n'
  '    }',
  'ViewModel: ads gate + payment*')])

# ============================================================
# ۸) i18n — کلیدهای چهارزبانه
# ============================================================
KEYS = {
'    "pasandide" to "پسندیده",': '''    "pay_title" to "برای دیدن آگهی‌ها هزینه را پرداخت کنید",
    "pay_unpaid_t" to "آگهی‌ها قفل است",
    "pay_desc" to "برای مشاهده‌ی همه‌ی آگهی‌های استخدام، مبلغ زیر را به کارت کارگزار واریز کنید و دکمه‌ی «واریز کردم» را بزنید. به‌محض تأیید مدیر، همه‌ی آگهی‌ها باز می‌شود.",
    "pay_fee" to "مبلغ قابل پرداخت",
    "pay_card" to "کارت کارگزار",
    "pay_holder" to "به نام",
    "pay_ref" to "کد پیگیری",
    "pay_send" to "واریز کردم — ارسال برای تأیید",
    "pay_pending_t" to "در انتظار تأیید پرداخت",
    "pay_pending_d" to "درخواست پرداخت شما ثبت شد. به‌محض تأیید مدیر، همه‌ی آگهی‌ها نمایش داده می‌شود.",
    "pay_recheck" to "بررسی مجدد وضعیت",
    "pay_sent" to "درخواست بررسی پرداخت ثبت شد",
    "pay_confirmed" to "پرداخت تأیید شد",
    "pay_unconfirmed" to "تأیید پرداخت لغو شد",
    "pay_sec" to "وضعیت پرداخت",
    "pay_st_unpaid" to "پرداخت‌نشده",
    "pay_st_pending" to "در انتظار تأیید",
    "pay_st_paid" to "پرداخت‌شده",
    "pay_confirm" to "تأیید پرداخت",
    "pay_cancel" to "لغو تأیید",
    "pay_ads_fee" to "هزینه نمایش آگهی‌ها (لیر)",
    "pay_ads_fee_hint" to "مبلغی که کارجو برای دیدن همه‌ی آگهی‌ها پرداخت می‌کند؛ تا تأیید مدیر، آگهی‌ها قفل می‌ماند.",
    "pay_my" to "پرداخت هزینه و دیدن آگهی‌ها",
    "pay_waiting" to "منتظر تأیید مدیر باشید",
    "pay_locked_tab" to "آگهی‌ها 🔒",
    "pay_locked_likes" to "برای دیدن جزئیات آگهی‌های پسندیده، اول هزینه‌ی نمایش آگهی‌ها را پرداخت کنید.",
''',
'    "pasandide" to "Beğendiklerim",': '''    "pay_title" to "İlanları görmek için ücreti ödeyin",
    "pay_unpaid_t" to "İlanlar kilitli",
    "pay_desc" to "Tüm iş ilanlarını görmek için aşağıdaki tutarı broker kartına yatırın ve «Yatırdım» düğmesine basın. Yönetici onaylar onaylamaz tüm ilanlar açılır.",
    "pay_fee" to "Ödenecek tutar",
    "pay_card" to "Broker kartı",
    "pay_holder" to "Adına",
    "pay_ref" to "Takip kodu",
    "pay_send" to "Yatırdım — onaya gönder",
    "pay_pending_t" to "Ödeme onayı bekleniyor",
    "pay_pending_d" to "Ödeme talebiniz kaydedildi. Yönetici onaylayınca tüm ilanlar görünecek.",
    "pay_recheck" to "Durumu yeniden kontrol et",
    "pay_sent" to "Ödeme inceleme talebi kaydedildi",
    "pay_confirmed" to "Ödeme onaylandı",
    "pay_unconfirmed" to "Ödeme onayı iptal edildi",
    "pay_sec" to "Ödeme durumu",
    "pay_st_unpaid" to "Ödenmedi",
    "pay_st_pending" to "Onay bekliyor",
    "pay_st_paid" to "Ödendi",
    "pay_confirm" to "Ödemeyi onayla",
    "pay_cancel" to "Onayı iptal et",
    "pay_ads_fee" to "İlan gösterim ücreti (TL)",
    "pay_ads_fee_hint" to "İş arayanın tüm ilanları görmek için ödediği tutar; yönetici onaylayana kadar ilanlar kilitli kalır.",
    "pay_my" to "Ücreti öde ve ilanları gör",
    "pay_waiting" to "Yönetici onayını bekleyin",
    "pay_locked_tab" to "İlanlar 🔒",
    "pay_locked_likes" to "Beğendiğiniz ilanların ayrıntısını görmek için önce ilan gösterim ücretini ödeyin.",
''',
'    "pasandide" to "Liked",': '''    "pay_title" to "Pay the fee to see the job ads",
    "pay_unpaid_t" to "Ads are locked",
    "pay_desc" to "To see all job ads, transfer the amount below to the broker card and press “I transferred”. Ads unlock as soon as the admin confirms.",
    "pay_fee" to "Amount to pay",
    "pay_card" to "Broker card",
    "pay_holder" to "Holder",
    "pay_ref" to "Tracking code",
    "pay_send" to "I transferred — send for approval",
    "pay_pending_t" to "Waiting for payment approval",
    "pay_pending_d" to "Your payment request was recorded. All ads appear as soon as the admin confirms.",
    "pay_recheck" to "Check status again",
    "pay_sent" to "Payment review request recorded",
    "pay_confirmed" to "Payment confirmed",
    "pay_unconfirmed" to "Payment confirmation cancelled",
    "pay_sec" to "Payment status",
    "pay_st_unpaid" to "Unpaid",
    "pay_st_pending" to "Pending approval",
    "pay_st_paid" to "Paid",
    "pay_confirm" to "Confirm payment",
    "pay_cancel" to "Cancel confirmation",
    "pay_ads_fee" to "Ads viewing fee (TL)",
    "pay_ads_fee_hint" to "The amount a job seeker pays to see all ads; ads stay locked until the admin confirms.",
    "pay_my" to "Pay the fee and see ads",
    "pay_waiting" to "Wait for admin confirmation",
    "pay_locked_tab" to "Ads 🔒",
    "pay_locked_likes" to "To open your liked ads, first pay the ads viewing fee.",
''',
'    "pasandide" to "المفضّلة",': '''    "pay_title" to "ادفع الرسوم لعرض الإعلانات",
    "pay_unpaid_t" to "الإعلانات مقفلة",
    "pay_desc" to "لمشاهدة جميع إعلانات العمل، حوّل المبلغ أدناه إلى بطاقة الوسيط ثم اضغط «قمت بالتحويل». تُفتح الإعلانات فور تأكيد المدير.",
    "pay_fee" to "المبلغ المطلوب",
    "pay_card" to "بطاقة الوسيط",
    "pay_holder" to "باسم",
    "pay_ref" to "رمز المتابعة",
    "pay_send" to "قمت بالتحويل — أرسل للتأكيد",
    "pay_pending_t" to "بانتظار تأكيد الدفع",
    "pay_pending_d" to "تم تسجيل طلب الدفع. ستظهر جميع الإعلانات فور تأكيد المدير.",
    "pay_recheck" to "تحقّق من الحالة مجددًا",
    "pay_sent" to "تم تسجيل طلب مراجعة الدفع",
    "pay_confirmed" to "تم تأكيد الدفع",
    "pay_unconfirmed" to "تم إلغاء تأكيد الدفع",
    "pay_sec" to "حالة الدفع",
    "pay_st_unpaid" to "غير مدفوع",
    "pay_st_pending" to "بانتظار التأكيد",
    "pay_st_paid" to "مدفوع",
    "pay_confirm" to "تأكيد الدفع",
    "pay_cancel" to "إلغاء التأكيد",
    "pay_ads_fee" to "رسوم عرض الإعلانات (ليرة)",
    "pay_ads_fee_hint" to "المبلغ الذي يدفعه الباحث عن عمل لمشاهدة جميع الإعلانات؛ تبقى الإعلانات مقفلة حتى تأكيد المدير.",
    "pay_my" to "ادفع الرسوم وشاهد الإعلانات",
    "pay_waiting" to "انتظر تأكيد المدير",
    "pay_locked_tab" to "الإعلانات 🔒",
    "pay_locked_likes" to "لمشاهدة تفاصيل إعلاناتك المفضّلة، ادفع أولًا رسوم عرض الإعلانات.",
''',
}
patch('app/src/main/java/com/karbama/app/ui/i18n/Strings.kt',
      [(k, v + k, f'i18n {k[-12:]}*') for k, v in KEYS.items()])

print('--- دموی اندروید #34:', 'اعمال شد ✅' if APPLY else 'گزارش فقط')
for l in log: print('   •', l)
