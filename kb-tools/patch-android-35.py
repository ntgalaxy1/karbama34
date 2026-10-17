#!/usr/bin/env python3
"""#35 (اصلاح مسیر) — اندروید: صفحه‌ی اصلی همان صفحه می‌ماند، فقط بخش آگهی‌ها
   پوشیده می‌شود؛ کلیک → صفحه‌ی پرداخت → «واریز کردم» → تأییدیه پرداخت و ادامه‌ی مراحل.
اجرا:  python3 kb-tools/patch-android-35.py          (گزارش)
       python3 kb-tools/patch-android-35.py --apply  (اعمال)
"""
import sys, pathlib

ROOT = pathlib.Path('/home/user/Karbama')
APPLY = '--apply' in sys.argv
log = []

def patch(rel, pairs):
    f = ROOT / rel
    s = f.read_text()
    for old, new, label in pairs:
        if new in s:
            log.append(f'{label} (از قبل بود)'); continue
        assert s.count(old) == 1, f'{label}: anchor count = {s.count(old)}'
        s = s.replace(old, new, 1)
        log.append(label)
    if APPLY:
        f.write_text(s)

# ============================================================
# ۱) i18n — کلیدهای تازه (چهار زبان)
# ============================================================
K = {
 '    "pay_amount" to "مبلغ (لیر)",\n': '''    "pay_page_t" to "پرداخت هزینه‌ی نمایش آگهی‌ها",
    "pay_back" to "بازگشت",
    "pay_locked_note" to "🔒 {0} آگهی آماده‌ی نمایش است؛ برای باز شدن، هزینه‌ی نمایش آگهی‌ها را پرداخت کنید.",
    "pay_view_status" to "مشاهده‌ی وضعیت پرداخت",
    "pay_open" to "دیدن آگهی‌ها",
    "pay_receipt" to "تأییدیه پرداخت",
    "pay_reg_t" to "پرداخت شما ثبت شد",
    "pay_recheck" to "بررسی مجدد وضعیت",
    "pay_continue" to "بازگشت به صفحه‌ی اصلی",
    "pay_step1" to "ثبت درخواست پرداخت",
    "pay_step2" to "تأیید توسط مدیر",
    "pay_step3" to "ادامه‌ی مراحل: دیدن آگهی‌ها، ارسال درخواست و امضای تعهدنامه",
    "pay_after_paid" to "پرداخت شما تأیید شد؛ همه‌ی آگهی‌ها باز است و می‌توانید مراحل را ادامه دهید.",
''',
 '    "pay_amount" to "Tutar (TL)",\n': '''    "pay_page_t" to "İlan gösterim ücretini öde",
    "pay_back" to "Geri",
    "pay_locked_note" to "🔒 {0} ilan gösterime hazır; açılması için ilan gösterim ücretini ödeyin.",
    "pay_view_status" to "Ödeme durumunu gör",
    "pay_open" to "İlanları gör",
    "pay_receipt" to "Ödeme makbuzu",
    "pay_reg_t" to "Ödemeniz kaydedildi",
    "pay_recheck" to "Durumu yeniden kontrol et",
    "pay_continue" to "Ana sayfaya dön",
    "pay_step1" to "Ödeme talebi kaydı",
    "pay_step2" to "Yönetici onayı",
    "pay_step3" to "Devam: ilanları görme, başvuru gönderme ve taahhüt imzası",
    "pay_after_paid" to "Ödemeniz onaylandı; tüm ilanlar açık, adımlara devam edebilirsiniz.",
''',
 '    "pay_amount" to "Amount (TL)",\n': '''    "pay_page_t" to "Pay the ads viewing fee",
    "pay_back" to "Back",
    "pay_locked_note" to "🔒 {0} ads are waiting; pay the ads viewing fee to unlock them.",
    "pay_view_status" to "View payment status",
    "pay_open" to "View ads",
    "pay_receipt" to "Payment receipt",
    "pay_reg_t" to "Your payment is recorded",
    "pay_recheck" to "Check status again",
    "pay_continue" to "Back to home",
    "pay_step1" to "Payment request recorded",
    "pay_step2" to "Admin confirmation",
    "pay_step3" to "Continue: view ads, apply and sign the undertaking",
    "pay_after_paid" to "Your payment is confirmed; all ads are open and you can continue the steps.",
''',
 '    "pay_amount" to "المبلغ (ليرة)",\n': '''    "pay_page_t" to "ادفع رسوم عرض الإعلانات",
    "pay_back" to "رجوع",
    "pay_locked_note" to "🔒 {0} إعلانًا جاهز للعرض؛ ادفع رسوم عرض الإعلانات لفتحها.",
    "pay_view_status" to "عرض حالة الدفع",
    "pay_open" to "عرض الإعلانات",
    "pay_receipt" to "إيصال الدفع",
    "pay_reg_t" to "تم تسجيل دفعتك",
    "pay_recheck" to "تحقّق من الحالة مجددًا",
    "pay_continue" to "العودة إلى الصفحة الرئيسية",
    "pay_step1" to "تسجيل طلب الدفع",
    "pay_step2" to "تأكيد المدير",
    "pay_step3" to "المتابعة: مشاهدة الإعلانات وإرسال الطلبات وتوقيع التعهد",
    "pay_after_paid" to "تم تأكيد دفعتك؛ جميع الإعلانات مفتوحة ويمكنك متابعة الخطوات.",
''',
}
patch('app/src/main/java/com/karbama/app/ui/i18n/Strings.kt',
      [(k, v + k, f'i18n {k[:32]}*') for k, v in K.items()])

# ============================================================
# ۲) HomeScreen — بخش آگهی‌ها پوشیده می‌شود، بقیه‌ی صفحه دست‌نخورده
# ============================================================
H = 'app/src/main/java/com/karbama/app/ui/screens/HomeScreen.kt'
patch(H, [
 ('import com.karbama.app.data.model.Contract',
  'import com.karbama.app.data.model.Applicant\nimport com.karbama.app.data.model.Contract',
  'Home: Applicant import'),
 ('    likedIds: Set<String> = emptySet(),          // #28',
  '    likedIds: Set<String> = emptySet(),          // #28\n'
  '    me: Applicant? = null,                       // #35: کارجوی وارد‌شده (وضعیت پرداخت)\n'
  '    adsLocked: Boolean = false,                  // #35: بخش آگهی‌ها تا پرداخت پوشیده است\n'
  '    adsFee: Long = 6000L,                        // #35\n'
  '    brokerCard: String = "",                     // #35\n'
  '    onGoPay: () -> Unit = {},                    // #35: رفتن به صفحه‌ی پرداخت',
  'Home: pay params'),
 ('''        if (loading) {
            LoadingBox(loc["loading"])
        } else if (jobs.isEmpty()) {''',
  '''        if (adsLocked) {
            // #35: صفحه همان صفحه است؛ فقط جای فهرست آگهی‌ها → پنل «نیاز به پرداخت»
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                AdsLockPanel(
                    loc = loc,
                    applicant = me,
                    adsFee = adsFee,
                    brokerCard = brokerCard,
                    jobsCount = jobs.size,
                    onGoPay = onGoPay
                )
            }
        } else if (loading) {
            LoadingBox(loc["loading"])
        } else if (jobs.isEmpty()) {''',
  'Home: lock panel'),
])

# ============================================================
# ۳) MainActivity — روت صفحه‌ی پرداخت و سیم‌کشی
# ============================================================
M = 'app/src/main/java/com/karbama/app/MainActivity.kt'
patch(M, [
 ('    const val EDIT_PROFILE = "edit_profile"',
  '    const val EDIT_PROFILE = "edit_profile"\n'
  '    const val PAY = "pay"                      // #35: صفحه‌ی پرداخت هزینه‌ی نمایش آگهی‌ها',
  'Routes: PAY'),
 ('    onOpenRegister: () -> Unit,\n'
  '    onLoggedOut: () -> Unit\n'
  ') {\n'
  '    var tab by remember { mutableIntStateOf(0) }',
  '    onOpenRegister: () -> Unit,\n'
  '    onOpenPay: () -> Unit,                     // #35\n'
  '    onLoggedOut: () -> Unit\n'
  ') {\n'
  '    var tab by remember { mutableIntStateOf(0) }',
  'MainShell: onOpenPay param'),
 ('                        onOpenRegister = { nav.navigate(Routes.REGISTER) },\n'
  '                        onLoggedOut = {',
  '                        onOpenRegister = { nav.navigate(Routes.REGISTER) },\n'
  '                        onOpenPay = { nav.navigate(Routes.PAY) },   // #35\n'
  '                        onLoggedOut = {',
  'MAIN → onOpenPay'),
 # تب آگهی‌ها: همان صفحه با پنل قفل
 ('''                // #34: کارجوی پرداخت‌نکرده فقط صفحه‌ی پرداخت را می‌بیند
                tab == 0 -> if (!adsUnlocked) PaywallScreen(
                    loc = loc,
                    applicant = vm.myProfile,
                    adsFee = vm.adsFee,
                    brokerCard = vm.brokerCard,
                    brokerHolder = vm.brokerHolder,
                    onRequestPay = { vm.requestAdsPayment() },
                    onRecheck = { vm.refreshMyPayment() }
                ) else HomeContent(
                    loc = loc,
                    userName = vm.myProfile?.firstName ?: "",''',
  '''                // #35: صفحه‌ی اصلی همان صفحه‌ی قبلی است؛ فقط بخش آگهی‌ها
                // تا تأیید پرداخت با پنل «نیاز به پرداخت» پوشیده می‌شود.
                tab == 0 -> HomeContent(
                    loc = loc,
                    me = vm.myProfile,
                    adsLocked = !adsUnlocked,
                    adsFee = vm.adsFee,
                    brokerCard = vm.brokerCard,
                    onGoPay = onOpenPay,
                    userName = vm.myProfile?.firstName ?: "",''',
  'tab0: same home + lock panel'),
 # پروفایل: دکمه‌ها → صفحه‌ی پرداخت
 ('                    onGoPay = { tab = 0 }          // #34',
  '                    onGoPay = onOpenPay            // #35: رفتن به صفحه‌ی پرداخت',
  'profile → pay page'),
 # صفحه‌ی آگهی: صفحه‌ی پرداخت
 ('''                    if (!vm.adsUnlocked) {
                        // #34: تا پرداخت هزینه، صفحه‌ی کامل آگهی هم بسته است
                        PaywallScreen(
                            loc = loc,
                            applicant = vm.myProfile,
                            adsFee = vm.adsFee,
                            brokerCard = vm.brokerCard,
                            brokerHolder = vm.brokerHolder,
                            onRequestPay = { vm.requestAdsPayment() },
                            onRecheck = { vm.refreshMyPayment() },
                            onBack = { nav.popBackStack() }
                        )
                    } else if (job != null) {''',
  '''                    if (!vm.adsUnlocked) {
                        // #35: تا تأیید پرداخت، به‌جای آگهی → صفحه‌ی پرداخت
                        PaymentScreen(
                            loc = loc,
                            applicant = vm.myProfile,
                            adsFee = vm.adsFee,
                            brokerCard = vm.brokerCard,
                            brokerHolder = vm.brokerHolder,
                            onRequestPay = { vm.requestAdsPayment() },
                            onRecheck = { vm.refreshMyPayment() },
                            onDone = { nav.popBackStack() },
                            onBack = { nav.popBackStack() }
                        )
                    } else if (job != null) {''',
  'job route → payment page'),
 # روت تازه‌ی صفحه‌ی پرداخت (قبل از روت ویرایش پروفایل)
 ('''                // ---------- ویرایش پروفایل ----------''',
  '''                // ---------- #35: صفحه‌ی پرداخت هزینه‌ی نمایش آگهی‌ها ----------
                composable(Routes.PAY) {
                    PaymentScreen(
                        loc = loc,
                        applicant = vm.myProfile,
                        adsFee = vm.adsFee,
                        brokerCard = vm.brokerCard,
                        brokerHolder = vm.brokerHolder,
                        onRequestPay = { vm.requestAdsPayment() },
                        onRecheck = { vm.refreshMyPayment() },
                        onDone = { nav.popBackStack() },
                        onBack = { nav.popBackStack() }
                    )
                }

                // ---------- ویرایش پروفایل ----------''',
  'PAY route'),
])
# فراخوانی MainShell در MAIN (است، چون الگوی قبلی روی MainShell هم می‌خورد)
f = ROOT / M
s = f.read_text()
old = '    onOpenRegister: () -> Unit,\n    onLoggedOut: () -> Unit\n) {'
if old in s:
    s = s.replace(old, '    onOpenRegister: () -> Unit,\n    onOpenPay: () -> Unit,   // #35\n    onLoggedOut: () -> Unit\n) {', 1)
    log.append('MainShell signature: onOpenPay')
    if APPLY:
        f.write_text(s)

print('--- #35 اندروید:', 'اعمال شد ✅' if APPLY else 'گزارش فقط')
for l in log:
    print('   •', l)
