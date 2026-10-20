package com.karbama.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.karbama.app.data.model.Applicant
import com.karbama.app.ui.components.GhostButton
import com.karbama.app.ui.components.PrimaryButton
import com.karbama.app.ui.components.SectionCard
import com.karbama.app.ui.components.SystemBars
import com.karbama.app.ui.components.TopBar
import com.karbama.app.ui.i18n.Loc
import com.karbama.app.ui.theme.AccentLight
import com.karbama.app.ui.theme.BrandLight
import com.karbama.app.ui.theme.CardBg
import com.karbama.app.ui.theme.FieldBg
import com.karbama.app.ui.theme.GraphiteLight
import com.karbama.app.ui.theme.Ink
import com.karbama.app.ui.theme.Ink2
import com.karbama.app.ui.theme.Ink3
import com.karbama.app.ui.theme.OkDeep
import com.karbama.app.ui.theme.OkLight
import com.karbama.app.ui.theme.SunLight

/**
 * مسیر پرداخت #35.
 *
 * صفحهٔ اصلی همیشه حفظ می‌شود و فقط این پنل جای فهرست آگهی‌ها را می‌گیرد.
 * مسیر درگاه‌های واقعی v38 به Cloud Functions اضافه می‌شود؛ این صفحهٔ امنِ رسید
 * برای واریز بانکی/تأیید مدیر همچنان به‌عنوان مسیر جایگزین باقی می‌ماند.
 */
@Composable
fun AdsLockPanel(
    loc: Loc,
    applicant: Applicant?,
    adsFee: Long,
    brokerCard: String,
    jobsCount: Int,
    onGoPay: () -> Unit
) {
    SectionCard {
        Text("🔒", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text(loc["pay_unpaid_t"], style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(6.dp))
        Text(
            loc.f("pay_locked_note", loc.n(jobsCount)),
            style = MaterialTheme.typography.bodyMedium,
            color = Ink2
        )
        Spacer(Modifier.height(14.dp))
        PaymentAmountCard(loc = loc, amount = adsFee)
        Spacer(Modifier.height(10.dp))
        Text(
            "${loc["pay_card"]}: $brokerCard",
            style = MaterialTheme.typography.bodySmall,
            color = Ink3
        )
        applicant?.let {
            Spacer(Modifier.height(8.dp))
            Text(
                "${loc["pay_ref"]}: ${it.payTrackCode}",
                style = MaterialTheme.typography.labelMedium,
                color = Ink2
            )
        }
        Spacer(Modifier.height(16.dp))
        PrimaryButton(text = loc["pay_my"], onClick = onGoPay)
    }
}

@Composable
fun PaymentScreen(
    loc: Loc,
    applicant: Applicant?,
    adsFee: Long,
    brokerCard: String,
    brokerHolder: String,
    onRequestPay: (String, ByteArray) -> Unit,
    onRecheck: () -> Unit,
    onDone: () -> Unit,
    onBack: () -> Unit
) {
    SystemBars(darkIcons = true)
    val me = applicant
    Column(Modifier.fillMaxSize().background(CardBg)) {
        TopBar(title = loc["pay_page_t"], onBack = onBack)
        if (me == null) {
            Column(
                Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("⚠️", style = MaterialTheme.typography.headlineLarge)
                Spacer(Modifier.height(12.dp))
                Text(loc["pay_title"], style = MaterialTheme.typography.titleMedium)
            }
            return@Column
        }

        if (me.payStatus == "pending" || me.payStatus == "paid") {
            PaymentReceipt(
                loc = loc,
                applicant = me,
                adsFee = adsFee,
                onRecheck = onRecheck,
                onDone = onDone
            )
        } else {
            PaymentForm(
                loc = loc,
                applicant = me,
                adsFee = adsFee,
                brokerCard = brokerCard,
                brokerHolder = brokerHolder,
                onRequestPay = onRequestPay
            )
        }
    }
}

private enum class PaymentChoice { IRAN, TURKEY, MANUAL }

@Composable
private fun PaymentForm(
    loc: Loc,
    applicant: Applicant,
    adsFee: Long,
    brokerCard: String,
    brokerHolder: String,
    onRequestPay: (String, ByteArray) -> Unit
) {
    var choice by remember { mutableStateOf(PaymentChoice.MANUAL) }
    var reference by remember { mutableStateOf("") }
    var receipt by remember { mutableStateOf<PickedPhoto?>(null) }
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionCard {
            Text("💳", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Text(loc["pay_title"], style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(6.dp))
            Text(loc["pay_desc"], style = MaterialTheme.typography.bodyMedium, color = Ink2)
        }
        PaymentAmountCard(loc = loc, amount = adsFee)
        SectionCard {
            Text(loc["pay_choose_method"], style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(10.dp))
            PaymentChoiceRow(
                emoji = "🇮🇷", title = loc["pay_iran"], note = loc["pay_iran_note"],
                selected = choice == PaymentChoice.IRAN, onClick = { choice = PaymentChoice.IRAN }
            )
            PaymentChoiceRow(
                emoji = "🇹🇷", title = loc["pay_turkey"], note = loc["pay_turkey_note"],
                selected = choice == PaymentChoice.TURKEY, onClick = { choice = PaymentChoice.TURKEY }
            )
            PaymentChoiceRow(
                emoji = "🧾", title = loc["pay_manual"], note = loc["pay_manual_note"],
                selected = choice == PaymentChoice.MANUAL, onClick = { choice = PaymentChoice.MANUAL }
            )
        }
        when (choice) {
            PaymentChoice.IRAN -> GatewaySetupCard(
                loc = loc, title = "🇮🇷 ${loc["pay_iran"]}",
                detail = loc["pay_rate_note"]
            )
            PaymentChoice.TURKEY -> GatewaySetupCard(
                loc = loc, title = "🇹🇷 ${loc["pay_turkey"]}",
                detail = "₺${String.format(java.util.Locale.US, "%,d", adsFee)}"
            )
            PaymentChoice.MANUAL -> {
                SectionCard {
                    Text(loc["pay_card"], style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(10.dp))
                    Text(brokerCard, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                    if (brokerHolder.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text("${loc["pay_holder"]}: $brokerHolder", style = MaterialTheme.typography.bodySmall, color = Ink3)
                    }
                    Spacer(Modifier.height(10.dp))
                    Text("${loc["pay_ref"]}: ${applicant.payTrackCode}", style = MaterialTheme.typography.labelMedium, color = Ink2)
                }
                SectionCard {
                    Text(loc["pay_tx_ref"], style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = reference,
                        onValueChange = { reference = it.filter { ch -> ch.isLetterOrDigit() || ch == '-' }.take(32) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                        placeholder = { Text(loc["pay_tx_ref"], color = Ink3) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = FieldBg,
                            unfocusedContainerColor = FieldBg,
                            focusedBorderColor = com.karbama.app.ui.theme.Brand,
                            unfocusedBorderColor = com.karbama.app.ui.theme.Line
                        )
                    )
                    Spacer(Modifier.height(12.dp))
                    PhotoBox(
                        loc = loc,
                        label = loc["pay_receipt_image"],
                        hint = loc["pay_receipt_hint"],
                        icon = Icons.Filled.PhotoCamera,
                        photo = receipt,
                        onPicked = { picked -> receipt = picked }
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(loc["pay_manual_required"], style = MaterialTheme.typography.bodySmall, color = Ink3)
                }
                PrimaryButton(
                    text = loc["pay_send"],
                    enabled = reference.length >= 3 && receipt?.isSet == true,
                    onClick = {
                        receipt?.bytes?.let { onRequestPay(reference, it) }
                    }
                )
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun PaymentChoiceRow(
    emoji: String,
    title: String,
    note: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    SectionCard(
        modifier = Modifier
            .padding(bottom = 8.dp)
            .clickable { onClick() }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.padding(5.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(note, style = MaterialTheme.typography.bodySmall, color = Ink3)
            }
            Text(if (selected) "✓" else "○", style = MaterialTheme.typography.titleMedium, color = if (selected) com.karbama.app.ui.theme.BrandDark else Ink3)
        }
    }
}

@Composable
private fun GatewaySetupCard(loc: Loc, title: String, detail: String) {
    SectionCard {
        Text(title, style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(8.dp))
        Text(detail, style = MaterialTheme.typography.bodyMedium, color = Ink2)
        Spacer(Modifier.height(8.dp))
        Text("⚙️ ${loc["pay_gateway_setup"]}", style = MaterialTheme.typography.bodySmall, color = com.karbama.app.ui.theme.WarnText)
    }
}

@Composable
private fun PaymentReceipt(
    loc: Loc,
    applicant: Applicant,
    adsFee: Long,
    onRecheck: () -> Unit,
    onDone: () -> Unit
) {
    val paid = applicant.payStatus == "paid"
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionCard {
            Text(if (paid) "✅" else "🧾", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Text(loc["pay_receipt"], style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Text(
                if (paid) loc["pay_after_paid"] else loc["pay_pending_d"],
                style = MaterialTheme.typography.bodyMedium,
                color = Ink2
            )
            Spacer(Modifier.height(12.dp))
            PaymentStatusChip(text = if (paid) loc["pay_st_paid"] else loc["pay_st_pending"], paid = paid)
        }
        PaymentAmountCard(loc = loc, amount = adsFee)
        SectionCard {
            Text(loc["pay_ref"], style = MaterialTheme.typography.labelMedium, color = Ink3)
            Spacer(Modifier.height(4.dp))
            Text(applicant.payTrackCode, style = MaterialTheme.typography.titleMedium)
            if (applicant.payReference.isNotBlank()) {
                Spacer(Modifier.height(10.dp))
                Text(loc["pay_tx_ref"], style = MaterialTheme.typography.labelMedium, color = Ink3)
                Spacer(Modifier.height(4.dp))
                Text(applicant.payReference, style = MaterialTheme.typography.bodyMedium)
            }
        }
        PaySteps(loc = loc, paid = paid)
        if (paid) {
            PrimaryButton(text = loc["pay_open"], onClick = onDone)
        } else {
            GhostButton(text = loc["pay_recheck"], onClick = onRecheck)
        }
        GhostButton(text = loc["pay_continue"], onClick = onDone)
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun PaymentAmountCard(loc: Loc, amount: Long) {
    SectionCard {
        Text(loc["pay_amount"], style = MaterialTheme.typography.labelMedium, color = Ink3)
        Spacer(Modifier.height(4.dp))
        Text(
            "₺${String.format(java.util.Locale.US, "%,d", amount)}",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = Ink
        )
    }
}

@Composable
private fun PaymentStatusChip(text: String, paid: Boolean) {
    val bg = if (paid) OkLight else SunLight
    val fg = if (paid) OkDeep else Ink2
    Row(
        Modifier.background(bg, MaterialTheme.shapes.small).padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, style = MaterialTheme.typography.labelMedium, color = fg)
    }
}

@Composable
private fun PaySteps(loc: Loc, paid: Boolean) {
    SectionCard {
        Text(loc["pay_receipt"], style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(12.dp))
        PayStep("۱", loc["pay_step1"], true)
        PayStep("۲", loc["pay_step2"], paid)
        PayStep("۳", loc["pay_step3"], paid)
    }
}

@Composable
private fun PayStep(number: String, text: String, done: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            number,
            modifier = Modifier
                .background(if (done) BrandLight else GraphiteLight, MaterialTheme.shapes.small)
                .padding(horizontal = 9.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelMedium,
            color = if (done) com.karbama.app.ui.theme.BrandDark else Ink3
        )
        Spacer(Modifier.padding(5.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = if (done) Ink2 else Ink3)
    }
    Spacer(Modifier.height(8.dp))
}


/** Shared payment badge for the management and applicant screens. */
@Composable
fun PayBadge(status: String, loc: Loc) {
    val paid = status == "paid"
    val label = when (status) {
        "paid" -> loc["pay_st_paid"]
        "pending" -> loc["pay_st_pending"]
        else -> loc["pay_st_unpaid"]
    }
    PaymentStatusChip(text = label, paid = paid)
}

/** Compact payment summary kept in the profile; it intentionally does not unlock jobs. */
@Composable
fun PayStatusCard(
    loc: Loc,
    applicant: Applicant,
    adsFee: Long,
    onGoPay: () -> Unit
) {
    val paid = applicant.payStatus == "paid"
    val pending = applicant.payStatus == "pending"
    SectionCard {
        Text(loc["pay_sec"], style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(8.dp))
        PayBadge(applicant.payStatus, loc)
        Spacer(Modifier.height(10.dp))
        if (paid) {
            Text(loc["pay_after_paid"], style = MaterialTheme.typography.bodySmall, color = Ink2)
        } else {
            PaymentAmountCard(loc, adsFee)
            Spacer(Modifier.height(10.dp))
            Text(
                if (pending) loc["pay_pending_d"] else loc["pay_locked_note"],
                style = MaterialTheme.typography.bodySmall,
                color = Ink2
            )
            Spacer(Modifier.height(10.dp))
            PrimaryButton(
                text = if (pending) loc["pay_view_status"] else loc["pay_my"],
                onClick = onGoPay
            )
        }
    }
}

/** A profile-level lock notice; liked jobs stay visible only after payment confirmation. */
@Composable
fun LikesLockedNote(loc: Loc, onGoPay: () -> Unit) {
    Text(loc["pay_locked_likes"], style = MaterialTheme.typography.bodySmall, color = Ink3)
    Spacer(Modifier.height(10.dp))
    GhostButton(text = loc["pay_my"], onClick = onGoPay)
}
