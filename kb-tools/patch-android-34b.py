#!/usr/bin/env python3
"""#34 (بخش دوم) — سیم‌کشی UI اندروید: قفل آگهی‌ها، پروفایل، پنل مدیریت
اجرا:  python3 kb-tools/patch-android-34b.py          (گزارش)
       python3 kb-tools/patch-android-34b.py --apply  (اعمال)
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

MAIN = 'app/src/main/java/com/karbama/app/MainActivity.kt'
PROF = 'app/src/main/java/com/karbama/app/ui/screens/ProfileScreen.kt'
ADM  = 'app/src/main/java/com/karbama/app/ui/screens/AdminScreens.kt'

# ============================================================
# ۱) MainActivity — قفل تب آگهی‌ها + قفل صفحه‌ی آگهی + پاس‌دادن props
# ============================================================
patch(MAIN, [
 # --- وضعیت قفل در MainShell ---
 ('    var tab by remember { mutableIntStateOf(0) }\n    val isAdmin = vm.adminMode != AdminMode.NONE',
  '    var tab by remember { mutableIntStateOf(0) }\n'
  '    val isAdmin = vm.adminMode != AdminMode.NONE\n'
  '    // #34: تا هزینه‌ی نمایش آگهی‌ها پرداخت و توسط مدیر تأیید نشود، هیچ آگهی‌ای نمایش داده نمی‌شود\n'
  '    val adsUnlocked = vm.adsUnlocked',
  'MainShell: adsUnlocked'),

 # --- برچسب تب اول ---
 ('                        icon = { Icon(Icons.Filled.Home, contentDescription = null) },\n'
  '                        label = {\n'
  '                            Text(loc["tab_jobs"], style = MaterialTheme.typography.labelSmall)\n'
  '                        },',
  '                        icon = {\n'
  '                            Icon(\n'
  '                                if (adsUnlocked) Icons.Filled.Home else Icons.Filled.Lock,\n'
  '                                contentDescription = null\n'
  '                            )\n'
  '                        },\n'
  '                        label = {\n'
  '                            Text(\n'
  '                                if (adsUnlocked) loc["tab_jobs"] else loc["pay_locked_tab"],\n'
  '                                style = MaterialTheme.typography.labelSmall\n'
  '                            )\n'
  '                        },',
  'MainShell: locked tab label'),

 # --- محتوای تب آگهی‌ها: قفل ---
 ('                tab == 0 -> HomeContent(\n'
  '                    loc = loc,\n'
  '                    userName = vm.myProfile?.firstName ?: "",',
  '                // #34: کارجوی پرداخت‌نکرده فقط صفحه‌ی پرداخت را می‌بیند\n'
  '                tab == 0 -> if (!adsUnlocked) PaywallScreen(\n'
  '                    loc = loc,\n'
  '                    applicant = vm.myProfile,\n'
  '                    adsFee = vm.adsFee,\n'
  '                    brokerCard = vm.brokerCard,\n'
  '                    brokerHolder = vm.brokerHolder,\n'
  '                    onRequestPay = { vm.requestAdsPayment() },\n'
  '                    onRecheck = { vm.refreshMyPayment() }\n'
  '                ) else HomeContent(\n'
  '                    loc = loc,\n'
  '                    userName = vm.myProfile?.firstName ?: "",',
  'MainShell: tab0 paywall'),

 # --- پروفایل: کارت پرداخت + قفل پسندیده‌ها + رفتن به تب آگهی‌ها ---
 ('                    // #28: آگهی‌های پسندیده‌ی همین کاربر\n'
  '                    likedJobs = jobs.filter { vm.likedIds.contains(it.id) },\n'
  '                    onOpenJob = onOpenJob,\n'
  '                    onUnlike = { vm.toggleLike(it.id) }',
  '                    // #28: آگهی‌های پسندیده‌ی همین کاربر\n'
  '                    likedJobs = jobs.filter { vm.likedIds.contains(it.id) },\n'
  '                    onOpenJob = onOpenJob,\n'
  '                    onUnlike = { vm.toggleLike(it.id) },\n'
  '                    adsFee = vm.adsFee,            // #34\n'
  '                    adsLocked = !adsUnlocked,      // #34\n'
  '                    onGoPay = { tab = 0 }          // #34',
  'MainShell: profile props'),

 # --- صفحه‌ی آگهی: قفل ---
 ('                    if (job != null) {\n                        JobDetailScreen(',
  '                    if (!vm.adsUnlocked) {\n'
  '                        // #34: تا پرداخت هزینه، صفحه‌ی کامل آگهی هم بسته است\n'
  '                        PaywallScreen(\n'
  '                            loc = loc,\n'
  '                            applicant = vm.myProfile,\n'
  '                            adsFee = vm.adsFee,\n'
  '                            brokerCard = vm.brokerCard,\n'
  '                            brokerHolder = vm.brokerHolder,\n'
  '                            onRequestPay = { vm.requestAdsPayment() },\n'
  '                            onRecheck = { vm.refreshMyPayment() },\n'
  '                            onBack = { nav.popBackStack() }\n'
  '                        )\n'
  '                    } else if (job != null) {\n                        JobDetailScreen(',
  'MainShell: job route paywall'),

 # --- پنل مدیریت: props جدید ---
 ('                    onSetStatus = { id, st -> vm.setApplicantStatus(id, st) },\n'
  '                    onSetFee = { person, fee -> vm.setApplicantFee(person, fee) },',
  '                    onSetStatus = { id, st -> vm.setApplicantStatus(id, st) },\n'
  '                    onSetFee = { person, fee -> vm.setApplicantFee(person, fee) },\n'
  '                    adsFee = vm.adsFee,                                            // #34\n'
  '                    onSaveAdsFee = { v -> vm.saveAdsFee(v) },                      // #34\n'
  '                    onSetPayment = { person, st -> vm.setApplicantPayment(person, st) },  // #34',
  'MainShell: admin props'),

 # --- صفحه‌ی متقاضی (مدیر) ---
 ('                            onSetStatus = { vm.setApplicantStatus(person.id, it) },\n'
  '                            onSetFee = { vm.setApplicantFee(person, it) },',
  '                            onSetStatus = { vm.setApplicantStatus(person.id, it) },\n'
  '                            onSetFee = { vm.setApplicantFee(person, it) },\n'
  '                            adsFee = vm.adsFee,                                       // #34\n'
  '                            onSetPayment = { st -> vm.setApplicantPayment(person, st) },  // #34',
  'MainActivity: applicant route props'),
])

# ============================================================
# ۲) ProfileScreen — کارت وضعیت پرداخت + قفل پسندیده‌ها
# ============================================================
patch(PROF, [
 ('    likedJobs: List<Job> = emptyList(),          // #28\n'
  '    onOpenJob: (Job) -> Unit = {},               // #28\n'
  '    onUnlike: (Job) -> Unit = {}                 // #28',
  '    likedJobs: List<Job> = emptyList(),          // #28\n'
  '    onOpenJob: (Job) -> Unit = {},               // #28\n'
  '    onUnlike: (Job) -> Unit = {},                // #28\n'
  '    adsFee: Long = 0L,                           // #34\n'
  '    adsLocked: Boolean = false,                  // #34\n'
  '    onGoPay: () -> Unit = {}                     // #34',
  'Profile: params'),
 ('            } else {\n'
  '                // ---------------- تخصص‌ها و زبان ----------------',
  '            } else {\n'
  '                // ---------------- #34: وضعیت پرداخت هزینه‌ی نمایش آگهی‌ها ----------------\n'
  '                PayStatusCard(\n'
  '                    loc = loc,\n'
  '                    applicant = applicant,\n'
  '                    adsFee = adsFee,\n'
  '                    onGoPay = onGoPay\n'
  '                )\n'
  '                Spacer(Modifier.height(12.dp))\n\n'
  '                // ---------------- تخصص‌ها و زبان ----------------',
  'Profile: pay card'),
 ('                SectionTitle("❤ " + loc["pasandide"] + " (" + likedJobs.size + ")")\n'
  '                Spacer(Modifier.height(8.dp))\n'
  '                if (likedJobs.isEmpty()) {',
  '                SectionTitle(if (adsLocked) "🔒 " + loc["pasandide"] else "❤ " + loc["pasandide"] + " (" + likedJobs.size + ")")\n'
  '                Spacer(Modifier.height(8.dp))\n'
  '                if (adsLocked) {\n'
  '                    // #34: تا پرداخت هزینه، جزئیات آگهی‌های پسندیده هم بسته است\n'
  '                    LikesLockedNote(loc = loc, onGoPay = onGoPay)\n'
  '                } else if (likedJobs.isEmpty()) {',
  'Profile: likes lock'),
])

# ============================================================
# ۳) AdminScreens — مبلغ نمایش آگهی‌ها، نشان پرداخت، تأیید پرداخت
# ============================================================
patch(ADM, [
 # --- پارامترهای AdminContent ---
 ('    onSetStatus: (String, String) -> Unit,\n'
  '    onSetFee: (Applicant, Long) -> Unit,\n'
  '    onExit: () -> Unit\n) {',
  '    onSetStatus: (String, String) -> Unit,\n'
  '    onSetFee: (Applicant, Long) -> Unit,\n'
  '    adsFee: Long = 6000L,                                   // #34: هزینه‌ی نمایش آگهی‌ها\n'
  '    onSaveAdsFee: (Long) -> Unit = {},                      // #34\n'
  '    onSetPayment: (Applicant, String) -> Unit = { _, _ -> },  // #34: تأیید/لغو پرداخت\n'
  '    onExit: () -> Unit\n) {',
  'Admin: AdminContent params'),

 # --- پاس‌دادن به تب تنظیمات ---
 ('                AdminSettingsTab(\n'
  '                    loc = loc,\n'
  '                    brokerCard = brokerCard,',
  '                AdminSettingsTab(\n'
  '                    loc = loc,\n'
  '                    brokerCard = brokerCard,\n'
  '                    adsFee = adsFee,                        // #34\n'
  '                    onSaveAdsFee = onSaveAdsFee,            // #34',
  'Admin: settings props'),

 # --- نشان پرداخت روی کارت متقاضی ---
 ('                Column(horizontalAlignment = Alignment.End) {\n'
  '                    StatusBadge(applicant.status, loc)\n'
  '                    Spacer(Modifier.height(4.dp))\n'
  '                    Text(loc.timeAgo(applicant.createdAt), style = MaterialTheme.typography.labelSmall)\n'
  '                }',
  '                Column(horizontalAlignment = Alignment.End) {\n'
  '                    StatusBadge(applicant.status, loc)\n'
  '                    Spacer(Modifier.height(4.dp))\n'
  '                    PayBadge(applicant.payStatus, loc)   // #34\n'
  '                    Spacer(Modifier.height(4.dp))\n'
  '                    Text(loc.timeAgo(applicant.createdAt), style = MaterialTheme.typography.labelSmall)\n'
  '                }',
  'Admin: row pay badge'),

 # --- صفحه‌ی کامل متقاضی: پارامترها ---
 ('    onSetStatus: (String) -> Unit,\n'
  '    onSetFee: (Long) -> Unit,\n'
  '    onBack: () -> Unit,',
  '    onSetStatus: (String) -> Unit,\n'
  '    onSetFee: (Long) -> Unit,\n'
  '    adsFee: Long = 6000L,                              // #34\n'
  '    onSetPayment: (String) -> Unit = {},               // #34: paid | ""\n'
  '    onBack: () -> Unit,',
  'Admin: detail params'),

 # --- بخش پرداخت در صفحه‌ی متقاضی (قبل از مبلغ اختصاصی) ---
 ('            Spacer(Modifier.height(12.dp))\n\n'
  '            // ---------- مبلغ اختصاصی این متقاضی ----------',
  '            Spacer(Modifier.height(12.dp))\n\n'
  '            // ---------- #34: وضعیت پرداخت هزینه‌ی نمایش آگهی‌ها ----------\n'
  '            SectionCard {\n'
  '                SectionTitle("💳 " + loc["pay_sec"])\n'
  '                Spacer(Modifier.height(8.dp))\n'
  '                InfoRow(loc["pay_fee"], loc.money(adsFee))\n'
  '                HorizontalDivider(color = Line)\n'
  '                InfoRow(loc["pay_ref"], applicant.payTrackCode)\n'
  '                HorizontalDivider(color = Line)\n'
  '                InfoRow(loc["pay_sec"],\n'
  '                    when (applicant.payStatus) {\n'
  '                        "pending" -> "⏳ " + loc["pay_st_pending"]\n'
  '                        "paid" -> "✅ " + loc["pay_st_paid"]\n'
  '                        else -> "⛔ " + loc["pay_st_unpaid"]\n'
  '                    }\n'
  '                )\n'
  '                if (applicant.payAt > 0L) {\n'
  '                    HorizontalDivider(color = Line)\n'
  '                    InfoRow(loc["date"], loc.timeAgo(applicant.payAt))\n'
  '                }\n'
  '                Spacer(Modifier.height(12.dp))\n'
  '                Row(verticalAlignment = Alignment.CenterVertically) {\n'
  '                    PayBadge(applicant.payStatus, loc)\n'
  '                    Spacer(Modifier.weight(1f))\n'
  '                }\n'
  '                Spacer(Modifier.height(12.dp))\n'
  '                if (applicant.payStatus == "paid") {\n'
  '                    GhostButton(\n'
  '                        text = "↩︎ " + loc["pay_cancel"],\n'
  '                        onClick = { onSetPayment("") }\n'
  '                    )\n'
  '                } else {\n'
  '                    PrimaryButton(\n'
  '                        text = "✅ " + loc["pay_confirm"],\n'
  '                        onClick = { onSetPayment("paid") }\n'
  '                    )\n'
  '                }\n'
  '            }\n\n'
  '            Spacer(Modifier.height(12.dp))\n\n'
  '            // ---------- مبلغ اختصاصی این متقاضی ----------',
  'Admin: detail pay section'),

 # --- تب تنظیمات: پارامترها ---
 ('private fun AdminSettingsTab(\n'
  '    loc: Loc,\n'
  '    brokerCard: String,',
  'private fun AdminSettingsTab(\n'
  '    loc: Loc,\n'
  '    brokerCard: String,\n'
  '    adsFee: Long = 6000L,                       // #34\n'
  '    onSaveAdsFee: (Long) -> Unit = {},          // #34',
  'Admin: settings signature'),

 # --- تب تنظیمات: فیلد مبلغ نمایش آگهی‌ها ---
 ('    var cardText by remember { mutableStateOf(brokerCard) }',
  '    var cardText by remember { mutableStateOf(brokerCard) }\n'
  '    var adsFeeText by remember(adsFee) { mutableStateOf(adsFee.toString()) }   // #34\n'
  '    var adsSaved by remember { mutableStateOf(false) }                          // #34',
  'Admin: settings state'),

 # --- تب تنظیمات: بخش جدید قبل از دکمه‌ی ذخیره‌ی نهایی ---
 ('        Spacer(Modifier.height(16.dp))\n'
  '        PrimaryButton(\n'
  '            text = loc["save"] + " ✓",\n'
  '            onClick = {\n'
  '                if (valid) {\n'
  '                    onSave(\n'
  '                        FormatUtils.digitsOnly(cardText, 16),\n'
  '                        userText.trim(),\n'
  '                        passText\n'
  '                    )\n'
  '                    saved = true\n'
  '                }\n'
  '            }\n'
  '        )',
  '        Spacer(Modifier.height(16.dp))\n\n'
  '        // ---------- #34: هزینه‌ی نمایش آگهی‌ها ----------\n'
  '        SectionCard {\n'
  '            SectionTitle("💳 " + loc["pay_ads_fee"])\n'
  '            Spacer(Modifier.height(8.dp))\n'
  '            Text(\n'
  '                loc["pay_ads_fee_hint"],\n'
  '                style = MaterialTheme.typography.bodySmall,\n'
  '                color = Ink2,\n'
  '                lineHeight = 20.sp\n'
  '            )\n'
  '            Spacer(Modifier.height(12.dp))\n'
  '            AdmLabel(loc["pay_ads_fee"])\n'
  '            AdmField(\n'
  '                value = adsFeeText,\n'
  '                onValueChange = { v ->\n'
  '                    adsFeeText = FormatUtils.digitsOnly(v, 9)\n'
  '                    adsSaved = false\n'
  '                },\n'
  '                placeholder = "6000",\n'
  '                keyboardType = KeyboardType.Number,\n'
  '                imeAction = ImeAction.Done\n'
  '            )\n'
  '            Spacer(Modifier.height(8.dp))\n'
  '            Text(\n'
  '                loc.money(adsFeeText.toLongOrNull() ?: adsFee),\n'
  '                style = MaterialTheme.typography.labelLarge,\n'
  '                color = BrandDark\n'
  '            )\n'
  '            Spacer(Modifier.height(12.dp))\n'
  '            PrimaryButton(\n'
  '                text = if (adsSaved) "✅ " + loc["save"] else loc["save"] + " ✓",\n'
  '                onClick = {\n'
  '                    onSaveAdsFee(adsFeeText.toLongOrNull() ?: adsFee)\n'
  '                    adsSaved = true\n'
  '                }\n'
  '            )\n'
  '        }\n\n'
  '        Spacer(Modifier.height(16.dp))\n'
  '        PrimaryButton(\n'
  '            text = loc["save"] + " ✓",\n'
  '            onClick = {\n'
  '                if (valid) {\n'
  '                    onSave(\n'
  '                        FormatUtils.digitsOnly(cardText, 16),\n'
  '                        userText.trim(),\n'
  '                        passText\n'
  '                    )\n'
  '                    saved = true\n'
  '                }\n'
  '            }\n'
  '        )',
  'Admin: settings ads fee field'),
])

# ============================================================
# ۴) firestore.rules — کارجو خودش نمی‌تواند «paid» کند
# ============================================================
patch('firestore.rules', [(
 '    match /applicants/{uid} {\n'
 '      allow create: if isSignedIn() && request.auth.uid == uid;\n'
 '      allow read, update: if isSignedIn() && (request.auth.uid == uid || isAdmin());\n'
 '      allow delete: if isAdmin();\n'
 '    }',
 '    match /applicants/{uid} {\n'
 '      allow create: if isSignedIn() && request.auth.uid == uid;\n'
 '      allow read: if isSignedIn() && (request.auth.uid == uid || isAdmin());\n'
 '      // #34: تأیید پرداخت («paid») فقط با مدیر است؛ کارجو حداکثر می‌تواند وضعیت را از\n'
 '      // «چیزی» به «pending / خالی» ببرد و هیچ‌وقت خودش را پرداخت‌شده اعلام کند.\n'
 '      allow update: if isSignedIn() && (isAdmin() || (\n'
 '                      request.auth.uid == uid && (\n'
 '                        !(request.resource.data.diff(resource.data).affectedKeys()\n'
 '                            .hasAny(["paymentStatus", "paymentAt"]))\n'
 '                        || (request.resource.data.paymentStatus in ["", "pending", "paid"]\n'
 '                            && (request.resource.data.paymentStatus != "paid"\n'
 '                                || resource.data.paymentStatus == "paid"))\n'
 '                      )));\n'
 '      allow delete: if isAdmin();\n'
 '    }',
 'rules: applicant cannot self-approve')])

print('--- #34 اندروید (UI):', 'اعمال شد ✅' if APPLY else 'گزارش فقط')
for l in log: print('   •', l)
