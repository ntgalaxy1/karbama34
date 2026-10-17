package com.karbama.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karbama.app.ui.components.*
import com.karbama.app.ui.i18n.Loc
import com.karbama.app.ui.theme.*

/** نتیجه‌ی تلاش برای ورود */
enum class LoginOutcome { ADMIN, ADMIN_2FA, USER, FAIL }

/**
 * ورود به برنامه — یکی برای همه: کاربر عادی با نام کاربری/شماره تلفن خودش،
 * مدیر با نام کاربری مدیر (یا ایمیل حساب Firebase).
 */
@Composable
fun LoginScreen(
    loc: Loc,
    busy: Boolean,
    onLogin: (String, String, (LoginOutcome) -> Unit) -> Unit,
    onTwoFactor: (String, (Boolean) -> Unit) -> Unit,
    onRegister: () -> Unit,
    onBack: () -> Unit
) {
    SystemBars(darkIcons = true)

    var user by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var stage2 by remember { mutableStateOf(false) }
    var code by remember { mutableStateOf("") }

    Scaffold(
        containerColor = Bg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = { TopBar(title = loc["login_title"], onBack = onBack) }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            SectionCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(BrandLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Security, contentDescription = null, tint = BrandDark)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(loc["login_title"], style = MaterialTheme.typography.titleMedium)
                        Text(
                            loc["login_user_hint"],
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 19.sp
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))

                // ---------- مرحله‌ی دوم: کد ۶ رقمی برنامه‌ی Authenticator ----------
                if (stage2) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🔐", fontSize = 22.sp)
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text(loc["twofa_step_title"], style = MaterialTheme.typography.titleSmall)
                            Text(
                                loc["twofa_step_hint"],
                                style = MaterialTheme.typography.bodySmall,
                                lineHeight = 19.sp
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    KbField(
                        value = code,
                        onValueChange = { code = it.filter { c -> c.isDigit() || c == '-' }; error = null },
                        label = loc["twofa_code"],
                        placeholder = "123456",
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                        imeAction = ImeAction.Done
                    )
                    if (error != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "⚠️ $error",
                            color = Danger,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    PrimaryButton(
                        text = "✅ " + loc["twofa_verify"],
                        loading = busy,
                        enabled = !busy,
                        onClick = {
                            if (code.isBlank()) {
                                error = loc["required_field"]
                            } else {
                                error = null
                                onTwoFactor(code) { ok ->
                                    if (!ok) error = loc["twofa_wrong"]
                                }
                            }
                        }
                    )
                    Spacer(Modifier.height(8.dp))
                    GhostButton(
                        text = "↩︎ " + loc["login_back_step1"],
                        onClick = { stage2 = false; code = ""; error = null }
                    )
                } else {

                KbField(
                    value = user,
                    onValueChange = { user = it; error = null },
                    label = loc["login_user"],
                    placeholder = "rezaei",
                    imeAction = ImeAction.Next
                )
                Spacer(Modifier.height(16.dp))
                KbField(
                    value = pass,
                    onValueChange = { pass = it; error = null },
                    label = loc["login_pass"],
                    placeholder = "••••••",
                    password = true,
                    imeAction = ImeAction.Done
                )

                if (error != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "⚠️ $error",
                        color = Danger,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(16.dp))
                PrimaryButton(
                    text = "🔑 " + loc["login_btn"],
                    loading = busy,
                    enabled = !busy,
                    onClick = {
                        if (user.isBlank() || pass.isBlank()) {
                            error = loc["required_field"]
                        } else {
                            error = null
                            onLogin(user.trim(), pass) { outcome ->
                                when (outcome) {
                                    LoginOutcome.FAIL -> error = loc["login_fail"]
                                    LoginOutcome.ADMIN_2FA -> {
                                        stage2 = true
                                        code = ""
                                        error = null
                                    }
                                    else -> Unit
                                }
                            }
                        }
                    }
                )
                Spacer(Modifier.height(8.dp))
                GhostButton(text = loc["no_account"], onClick = onRegister)
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
