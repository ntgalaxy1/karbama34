#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""#21 (اندروید) بخش دوم: تب تنظیمات با پسورد پنهان + بخش 2FA، اتصال‌ها، کلیدهای ترجمه"""
import pathlib

BASE = pathlib.Path('/home/user/Karbama/app/src/main/java/com/karbama/app')

def patch(rel, pairs, imports=()):
    p = BASE / rel
    s = p.read_text(); n0 = len(s)
    for line, before in imports:
        if line not in s:
            i = s.index(before)
            s = s[:i] + line + '\n' + s[i:]
    for old, new, expect in pairs:
        c = s.count(old)
        assert c == expect, f"{rel}: expected {expect}, found {c} for {old[:70]!r}"
        s = s.replace(old, new)
    p.write_text(s); print(f"  {rel}: {n0} → {len(s)}")

# ==================================================================
# ۱) AdminContent — پارامترهای 2FA
# ==================================================================
patch('ui/screens/AdminScreens.kt', [
("""    adminUser: String,
    adminPass: String,
    onSaveSettings: (String, String, String) -> Unit,""",
 """    adminUser: String,
    adminPass: String,
    onSaveSettings: (String, String, String) -> Unit,
    twoFAEnabled: Boolean,
    twoFAPending: Boolean,
    twoFASecret: String,
    twoFARecovery: String,
    twoFAUri: String,
    onBegin2FA: () -> Unit,
    onConfirm2FA: (String) -> Boolean,
    onDisable2FA: () -> Unit,""", 1),
("""                AdminSettingsTab(
                    loc = loc,
                    brokerCard = brokerCard,
                    adminUser = adminUser,
                    adminPass = adminPass,
                    onSave = onSaveSettings
                )""",
 """                AdminSettingsTab(
                    loc = loc,
                    brokerCard = brokerCard,
                    adminUser = adminUser,
                    adminPass = adminPass,
                    twoFAEnabled = twoFAEnabled,
                    twoFAPending = twoFAPending,
                    twoFASecret = twoFASecret,
                    twoFARecovery = twoFARecovery,
                    twoFAUri = twoFAUri,
                    onSave = onSaveSettings,
                    onBegin2FA = onBegin2FA,
                    onConfirm2FA = onConfirm2FA,
                    onDisable2FA = onDisable2FA
                )""", 1),
])

# ==================================================================
# ۲) بازنویسی کامل تب تنظیمات
# ==================================================================
p = BASE / 'ui/screens/AdminScreens.kt'
s = p.read_text()
i = s.index("""@Composable
private fun AdminSettingsTab(""")
NEW = '''@Composable
private fun AdminSettingsTab(
    loc: Loc,
    brokerCard: String,
    adminUser: String,
    adminPass: String,
    twoFAEnabled: Boolean,
    twoFAPending: Boolean,
    twoFASecret: String,
    twoFARecovery: String,
    twoFAUri: String,
    onSave: (String, String, String) -> Unit,
    onBegin2FA: () -> Unit,
    onConfirm2FA: (String) -> Boolean,
    onDisable2FA: () -> Unit
) {
    var cardText by remember { mutableStateOf(brokerCard) }
    var userText by remember { mutableStateOf(adminUser) }
    var passText by remember { mutableStateOf(adminPass) }
    var passShown by remember { mutableStateOf(false) }
    var secretShown by remember { mutableStateOf(false) }
    var recoveryShown by remember { mutableStateOf(false) }
    var codeText by remember { mutableStateOf("") }
    var codeBad by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }
    val clipboard = LocalClipboardManager.current
    val valid = userText.trim().isNotEmpty() && passText.isNotEmpty()

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // ---------- یوزرنیم و پسورد مدیر ----------
        SectionCard {
            SectionTitle(loc["admin_credentials"])
            Spacer(Modifier.height(8.dp))
            Text(
                loc["admin_hint"],
                style = MaterialTheme.typography.bodySmall,
                lineHeight = 20.sp
            )
            Spacer(Modifier.height(14.dp))

            Text(loc["admin_user"], style = MaterialTheme.typography.labelSmall, color = Ink2)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = userText,
                onValueChange = { userText = it.replace(" ", ""); saved = false },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = FieldBg,
                    focusedContainerColor = CardBg,
                    focusedBorderColor = Brand,
                    unfocusedBorderColor = Line
                )
            )

            Spacer(Modifier.height(12.dp))
            Text(loc["admin_pass"], style = MaterialTheme.typography.labelSmall, color = Ink2)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = passText,
                onValueChange = { passText = it; saved = false },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                visualTransformation = if (passShown) VisualTransformation.None
                else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                trailingIcon = {
                    IconButton(onClick = { passShown = !passShown }) {
                        Icon(
                            imageVector = if (passShown) Icons.Filled.VisibilityOff
                            else Icons.Filled.Visibility,
                            contentDescription = loc[if (passShown) "pass_hide" else "pass_show"],
                            tint = Ink3
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = FieldBg,
                    focusedContainerColor = CardBg,
                    focusedBorderColor = Brand,
                    unfocusedBorderColor = Line
                )
            )
            Spacer(Modifier.height(6.dp))
            Text(
                loc[if (passShown) "pass_note_shown" else "admin_pass_note"],
                style = MaterialTheme.typography.bodySmall,
                color = if (passShown) Color(0xFF8A5B00) else Ink3
            )
            if (!valid) {
                Spacer(Modifier.height(6.dp))
                Text(
                    loc["admin_empty"],
                    style = MaterialTheme.typography.bodySmall,
                    color = Danger,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // ---------- ورود دومرحله‌ای (2FA) ----------
        SectionCard {
            SectionTitle(loc["twofa_title"])
            Spacer(Modifier.height(8.dp))
            Text(
                loc["twofa_hint"],
                style = MaterialTheme.typography.bodySmall,
                lineHeight = 20.sp
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = (if (twoFAEnabled) "🟢 " else "⚪ ") +
                    loc[if (twoFAEnabled) "twofa_on" else "twofa_off"],
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (twoFAEnabled) Ok else Ink3
            )

            if (!twoFAEnabled && !twoFAPending) {
                Spacer(Modifier.height(12.dp))
                PrimaryButton(
                    text = "🔐 " + loc["twofa_enable"],
                    onClick = onBegin2FA
                )
            }

            if (twoFAPending || twoFAEnabled) {
                Spacer(Modifier.height(14.dp))
                Text(loc["twofa_scan"], style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(7.dp))
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = FieldBg,
                    border = BorderStroke(1.dp, Line),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (secretShown) TotpUtil.pretty(twoFASecret)
                        else "•••• •••• •••• ••••",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 10.dp)
                    )
                }
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { secretShown = !secretShown }) {
                        Text("👁 " + loc[if (secretShown) "twofa_hide" else "twofa_show"])
                    }
                    TextButton(onClick = {
                        clipboard.setText(AnnotatedString(twoFASecret))
                    }) {
                        Text("📋 " + loc["twofa_copy"])
                    }
                }
                Text(
                    twoFAUri,
                    style = MaterialTheme.typography.bodySmall,
                    color = Ink3,
                    maxLines = 2
                )

                if (twoFAEnabled) {
                    Spacer(Modifier.height(12.dp))
                    Text(loc["twofa_recovery"], style = MaterialTheme.typography.labelSmall, color = Ink2)
                    Spacer(Modifier.height(5.dp))
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = AccentLight,
                        border = BorderStroke(1.dp, Color(0xFFF0CF8A)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (recoveryShown) twoFARecovery else "••••-••••",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp)
                        )
                    }
                    TextButton(onClick = { recoveryShown = !recoveryShown }) {
                        Text("👁 " + loc[if (recoveryShown) "twofa_hide" else "twofa_show"])
                    }
                    Text(
                        loc["twofa_recovery_note"],
                        style = MaterialTheme.typography.bodySmall,
                        color = Ink3,
                        lineHeight = 19.sp
                    )
                    Spacer(Modifier.height(10.dp))
                    GhostButton(
                        text = "🚫 " + loc["twofa_disable"],
                        onClick = onDisable2FA
                    )
                } else {
                    Spacer(Modifier.height(14.dp))
                    Text(loc["twofa_code"], style = MaterialTheme.typography.labelSmall, color = Ink2)
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = codeText,
                        onValueChange = { codeText = it.filter { c -> c.isDigit() }.take(6); codeBad = false },
                        singleLine = true,
                        placeholder = { Text("123456", color = Ink3, fontSize = 13.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        isError = codeBad,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = FieldBg,
                            focusedContainerColor = CardBg,
                            focusedBorderColor = Brand,
                            unfocusedBorderColor = Line
                        )
                    )
                    if (codeBad) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "⚠️ " + loc["twofa_wrong"],
                            style = MaterialTheme.typography.bodySmall,
                            color = Danger,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    PrimaryButton(
                        text = "✅ " + loc["twofa_confirm"],
                        enabled = codeText.length == 6,
                        onClick = {
                            if (onConfirm2FA(codeText)) {
                                codeText = ""
                                codeBad = false
                                secretShown = false
                            } else {
                                codeBad = true
                            }
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // ---------- کارت بانکی کارگزار (متن تعهدنامه) ----------
        SectionCard {
            SectionTitle(loc["und_adminSettings"])
            Spacer(Modifier.height(8.dp))
            Text(
                loc["und_adminHint"],
                style = MaterialTheme.typography.bodySmall,
                lineHeight = 20.sp
            )
            Spacer(Modifier.height(12.dp))
            Text(loc["und_adminCard"], style = MaterialTheme.typography.labelSmall, color = Ink2)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = cardText,
                onValueChange = { input ->
                    cardText = FormatUtils.formatCard(input)
                    saved = false
                },
                singleLine = true,
                placeholder = { Text("0000 0000 0000 0000", color = Ink3, fontSize = 13.sp) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = FieldBg,
                    focusedContainerColor = CardBg,
                    focusedBorderColor = Brand,
                    unfocusedBorderColor = Line
                )
            )
            Spacer(Modifier.height(14.dp))
            Text(
                loc["und_cardWarn"],
                style = MaterialTheme.typography.bodySmall,
                color = if (FormatUtils.cardIsValid(cardText)) Ink3 else Danger
            )
        }

        Spacer(Modifier.height(14.dp))
        PrimaryButton(
            text = loc["save"] + " ✓",
            onClick = {
                if (valid) {
                    onSave(
                        FormatUtils.digitsOnly(cardText, 16),
                        userText.trim(),
                        passText
                    )
                    saved = true
                }
            }
        )
        if (saved) {
            Spacer(Modifier.height(8.dp))
            Text(
                "✓ " + loc["admin_saved"],
                style = MaterialTheme.typography.bodySmall,
                color = Ok,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(12.dp))

        SectionCard {
            SectionTitle(loc["und_short"])
            Spacer(Modifier.height(8.dp))
            InfoRow(loc["admin_user"], userText.trim().ifBlank { "—" })
            HorizontalDivider(color = Line)
            InfoRow(loc["admin_pass"], if (passText.isEmpty()) "—" else "••••••")
            HorizontalDivider(color = Line)
            InfoRow(
                loc["twofa_title"],
                loc[if (twoFAEnabled) "twofa_on" else "twofa_off"]
            )
            HorizontalDivider(color = Line)
            InfoRow(
                loc["und_brokerCard"],
                if (cardText.isBlank()) loc["und_brokerCardEmpty"] else loc.digits(cardText)
            )
        }
        Spacer(Modifier.height(24.dp))
    }
}
'''
s = s[:i] + NEW
p.write_text(s); print(f"  AdminSettingsTab rewritten: → {len(s)}")

# ==================================================================
# ۳) MainActivity — اتصال 2FA به ورود و پنل
# ==================================================================
patch('MainActivity.kt', [
("""                            vm.loginUser(user, pass) { outcome ->
                                cb(outcome)
                                if (outcome != LoginOutcome.FAIL) {
                                    nav.navigate(Routes.MAIN) {
                                        popUpTo(Routes.LOGIN) { inclusive = true }
                                    }
                                }
                            }
                        },
                        onRegister = { nav.navigate(Routes.REGISTER) },""",
 """                            vm.loginUser(user, pass) { outcome ->
                                cb(outcome)
                                when (outcome) {
                                    // مرحله‌ی دوم (کد 2FA) در همان صفحه پرسیده می‌شود
                                    LoginOutcome.ADMIN_2FA, LoginOutcome.FAIL -> Unit
                                    else -> nav.navigate(Routes.MAIN) {
                                        popUpTo(Routes.LOGIN) { inclusive = true }
                                    }
                                }
                            }
                        },
                        onTwoFactor = { code, cb ->
                            val ok = vm.verifyTwoFactor(code)
                            cb(ok)
                            if (ok) {
                                vm.completeAdminLogin()
                                nav.navigate(Routes.MAIN) {
                                    popUpTo(Routes.LOGIN) { inclusive = true }
                                }
                            }
                        },
                        onRegister = { nav.navigate(Routes.REGISTER) },""", 1),
("""                    adminUser = vm.adminUsername,
                    adminPass = vm.adminPassword,
                    onSaveSettings = { card, user, pass ->
                        vm.saveBrokerSettings(card, vm.brokerHolder)
                        vm.saveAdminCredentials(user, pass)
                    },""",
 """                    adminUser = vm.adminUsername,
                    adminPass = vm.adminPassword,
                    onSaveSettings = { card, user, pass ->
                        vm.saveBrokerSettings(card, vm.brokerHolder)
                        vm.saveAdminCredentials(user, pass)
                    },
                    twoFAEnabled = vm.twoFactorEnabled,
                    twoFAPending = vm.twoFactorPending,
                    twoFASecret = vm.twoFactorSecret,
                    twoFARecovery = vm.twoFactorRecovery,
                    twoFAUri = vm.twoFactorUri,
                    onBegin2FA = { vm.beginTwoFactorSetup() },
                    onConfirm2FA = { code -> vm.confirmTwoFactor(code) },
                    onDisable2FA = { vm.disableTwoFactor() },""", 1),
])

# ==================================================================
# ۴) کلیدهای ترجمه
# ==================================================================
KEYS = [
("""    "admin_empty" to "یوزرنیم و پسورد نمی‌توانند خالی باشند",""",
 """    "admin_empty" to "یوزرنیم و پسورد نمی‌توانند خالی باشند",
    "pass_show" to "نمایش رمز",
    "pass_hide" to "پنهان‌کردن رمز",
    "pass_note_shown" to "رمز الان روی صفحه دیده می‌شود — مراقب باشید کسی نبیند.",
    "twofa_title" to "ورود دومرحله‌ای (2FA)",
    "twofa_hint" to "با فعال‌کردن این گزینه، بعد از یوزرنیم و پسورد یک کد ۶ رقمی هم پرسیده می‌شود که برنامه‌ی Authenticator روی گوشی شما می‌سازد (Google Authenticator، Authy و…).",
    "twofa_enable" to "فعال‌سازی ورود دومرحله‌ای",
    "twofa_scan" to "این کلید را در برنامه‌ی Authenticator خودتان اضافه کنید (کد دستی):",
    "twofa_code" to "کد ۶ رقمی برنامه",
    "twofa_confirm" to "تأیید و فعال‌سازی",
    "twofa_on" to "فعال است",
    "twofa_off" to "غیرفعال است",
    "twofa_show" to "نمایش",
    "twofa_hide" to "پنهان‌کردن",
    "twofa_copy" to "کپی کلید",
    "twofa_recovery" to "کد بازیابی (یک‌بارمصرف)",
    "twofa_recovery_note" to "اگر گوشی یا برنامه‌ی Authenticator را از دست دادید، با این کد وارد شوید؛ بعد از استفاده، ورود دومرحله‌ای خودش خاموش می‌شود.",
    "twofa_disable" to "غیرفعال‌کردن ورود دومرحله‌ای",
    "twofa_wrong" to "کد واردشده درست نیست؛ کد تازه‌ی برنامه را وارد کنید",
    "twofa_step_title" to "تأیید دومرحله‌ای",
    "twofa_step_hint" to "کد ۶ رقمی برنامه‌ی Authenticator را وارد کنید (یا کد بازیابی).",
    "twofa_verify" to "تأیید و ورود","""),
("""    "admin_empty" to "Kullanıcı adı ve şifre boş olamaz",""",
 """    "admin_empty" to "Kullanıcı adı ve şifre boş olamaz",
    "pass_show" to "Şifreyi göster",
    "pass_hide" to "Şifreyi gizle",
    "pass_note_shown" to "Şifre şu an ekranda görünüyor — kimsenin görmemesine dikkat edin.",
    "twofa_title" to "İki adımlı giriş (2FA)",
    "twofa_hint" to "Bu açıkken kullanıcı adı ve şifreden sonra, Authenticator uygulamanızın ürettiği 6 haneli kod da sorulur (Google Authenticator, Authy vb.).",
    "twofa_enable" to "İki adımlı girişi aç",
    "twofa_scan" to "Bu anahtarı Authenticator uygulamanıza elle ekleyin:",
    "twofa_code" to "Uygulamadaki 6 haneli kod",
    "twofa_confirm" to "Onayla ve aç",
    "twofa_on" to "Açık",
    "twofa_off" to "Kapalı",
    "twofa_show" to "Göster",
    "twofa_hide" to "Gizle",
    "twofa_copy" to "Anahtarı kopyala",
    "twofa_recovery" to "Kurtarma kodu (tek kullanımlık)",
    "twofa_recovery_note" to "Telefonu veya Authenticator uygulamasını kaybederseniz bu kodla girin; kullanıldıktan sonra iki adımlı giriş kapanır.",
    "twofa_disable" to "İki adımlı girişi kapat",
    "twofa_wrong" to "Kod yanlış; uygulamadaki yeni kodu girin",
    "twofa_step_title" to "İki adımlı doğrulama",
    "twofa_step_hint" to "Authenticator uygulamasındaki 6 haneli kodu girin (veya kurtarma kodunu).",
    "twofa_verify" to "Doğrula ve gir","""),
("""    "admin_empty" to "Username and password cannot be empty",""",
 """    "admin_empty" to "Username and password cannot be empty",
    "pass_show" to "Show password",
    "pass_hide" to "Hide password",
    "pass_note_shown" to "The password is visible on screen right now — make sure nobody is watching.",
    "twofa_title" to "Two-step sign-in (2FA)",
    "twofa_hint" to "When this is on, a 6-digit code from your authenticator app is asked after the username and password (Google Authenticator, Authy, …).",
    "twofa_enable" to "Turn on two-step sign-in",
    "twofa_scan" to "Add this key to your authenticator app (manual entry):",
    "twofa_code" to "6-digit app code",
    "twofa_confirm" to "Confirm and enable",
    "twofa_on" to "Enabled",
    "twofa_off" to "Disabled",
    "twofa_show" to "Show",
    "twofa_hide" to "Hide",
    "twofa_copy" to "Copy key",
    "twofa_recovery" to "Recovery code (one-time)",
    "twofa_recovery_note" to "If you lose the phone or the authenticator app, sign in with this code; after it is used, two-step sign-in turns off.",
    "twofa_disable" to "Turn off two-step sign-in",
    "twofa_wrong" to "That code is wrong; enter the fresh code from the app",
    "twofa_step_title" to "Two-step verification",
    "twofa_step_hint" to "Enter the 6-digit code from your authenticator app (or the recovery code).",
    "twofa_verify" to "Verify and sign in","""),
]
p = BASE / 'ui/i18n/Strings.kt'; s = p.read_text(); n0 = len(s)
for old, new in KEYS:
    assert s.count(old) == 1, old[:40]
    s = s.replace(old, new, 1)
p.write_text(s); print(f"  ui/i18n/Strings.kt: {n0} → {len(s)}")

# ==================================================================
# ۵) importهای لازم در AdminScreens.kt
# ==================================================================
p = BASE / 'ui/screens/AdminScreens.kt'; s = p.read_text()
adds = [
 ('import androidx.compose.material.icons.filled.Visibility', 'import androidx.compose.material.icons.filled.Security'),
 ('import androidx.compose.material.icons.filled.VisibilityOff', 'import androidx.compose.material.icons.filled.Security'),
 ('import androidx.compose.ui.platform.LocalClipboardManager', 'import androidx.compose.ui.unit.dp'),
 ('import androidx.compose.ui.text.AnnotatedString', 'import androidx.compose.ui.text.font.FontWeight'),
 ('import androidx.compose.ui.text.font.FontFamily', 'import androidx.compose.ui.text.font.FontWeight'),
 ('import androidx.compose.ui.text.input.KeyboardType', 'import androidx.compose.ui.text.input.PasswordVisualTransformation'),
 ('import androidx.compose.ui.text.input.ImeAction', 'import androidx.compose.ui.text.input.PasswordVisualTransformation'),
 ('import androidx.compose.ui.text.style.TextAlign', 'import androidx.compose.ui.text.style.TextOverflow'),
 ('import androidx.compose.foundation.text.KeyboardOptions', 'import androidx.compose.foundation.verticalScroll'),
 ('import com.karbama.app.util.TotpUtil', 'import com.karbama.app.util.FormatUtils'),
]
for line, before in adds:
    if line not in s:
        i = s.index(before)
        s = s[:i] + line + '\n' + s[i:]
p.write_text(s); print(f"  AdminScreens.kt imports ok: → {len(s)}")

print("android 2FA part 2 applied ✅")
