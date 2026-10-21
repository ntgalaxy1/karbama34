package com.karbama.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.karbama.app.data.model.Applicant
import com.karbama.app.data.model.Hotel
import com.karbama.app.data.model.Job
import com.karbama.app.ui.components.KbField
import com.karbama.app.ui.components.PrimaryButton
import com.karbama.app.ui.components.SectionCard
import com.karbama.app.ui.components.ThemeModePicker
import com.karbama.app.ui.components.TopBar
import com.karbama.app.ui.i18n.Loc
import com.karbama.app.ui.theme.Danger
import com.karbama.app.ui.theme.Ink3
import com.karbama.app.util.AuthUtils

/**
 * Workspace for an authenticated collaborator. The caller supplies only
 * ownership-filtered streams, while Firestore rules independently enforce the
 * same boundary for jobs, hotels, addresses/locations, and jobseekers.
 */
@Composable
fun CollaboratorContent(
    loc: Loc,
    busy: Boolean,
    jobs: List<Job>,
    hotels: List<Hotel>,
    applicants: List<Applicant>,
    themeMode: String,
    onThemeChange: (String) -> Unit,
    onNewJob: () -> Unit,
    onEditJob: (Job) -> Unit,
    onDeleteJob: (Job) -> Unit,
    onOpenApplicant: (Applicant) -> Unit,
    onSaveHotel: (Hotel) -> Unit,
    onDeleteHotel: (Hotel) -> Unit,
    onCreateApplicant: (String, String, String, String, String, String) -> Unit,
    onExit: () -> Unit
) {
    var showApplicantDialog by remember { mutableStateOf(false) }

    Column {
        TopBar(
            title = "پنل همکار",
            subtitle = "فقط پرونده‌ها، آگهی‌ها و هتل‌های شما",
            onBack = onExit
        )
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SectionCard {
                    Text("حساب همکار", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "کارجوی جدید با نام کاربری و رمز عبور ساخته می‌شود؛ ایمیل یا رمز در پرونده ذخیره نمی‌شود.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Ink3
                    )
                    Spacer(Modifier.height(12.dp))
                    PrimaryButton(
                        text = "＋ ایجاد حساب و پروندهٔ کارجو",
                        enabled = !busy,
                        loading = busy,
                        onClick = { showApplicantDialog = true }
                    )
                }
            }

            item {
                Text(
                    "کارجوهای من (${applicants.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            if (applicants.isEmpty()) {
                item {
                    SectionCard {
                        Text("هنوز کارجویی توسط شما ایجاد نشده است.", color = Ink3)
                    }
                }
            } else {
                items(applicants, key = { it.id }) { applicant ->
                    SectionCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    applicant.fullName.ifBlank { applicant.username },
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    listOf(applicant.username, applicant.phone).filter { it.isNotBlank() }.joinToString(" · "),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Ink3,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            TextButton(onClick = { onOpenApplicant(applicant) }) { Text("مشاهده") }
                        }
                    }
                }
            }

            item {
                Text(
                    "آگهی‌های من (${jobs.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            item {
                PrimaryButton(text = "＋ ایجاد آگهی", enabled = !busy, onClick = onNewJob)
            }
            if (jobs.isEmpty()) {
                item {
                    SectionCard { Text("هنوز آگهی‌ای توسط شما ثبت نشده است.", color = Ink3) }
                }
            } else {
                items(jobs, key = { it.id }) { job ->
                    SectionCard {
                        Text(
                            job.company.ifBlank { job.titleCustom.ifBlank { loc.jobTitle(job.titleKey) } },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            listOf(job.city, job.district).filter { it.isNotBlank() }.joinToString(" · "),
                            style = MaterialTheme.typography.bodySmall,
                            color = Ink3
                        )
                        Spacer(Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = { onEditJob(job) }) { Text("ویرایش") }
                            TextButton(onClick = { onDeleteJob(job) }) { Text("حذف", color = Danger) }
                        }
                    }
                }
            }

            item { HotelManagerCard(loc, hotels, onSaveHotel, onDeleteHotel) }
            item {
                SectionCard { ThemeModePicker(loc, themeMode, onThemeChange) }
            }
        }
    }

    if (showApplicantDialog) {
        CollaboratorApplicantDialog(
            busy = busy,
            onDismiss = { if (!busy) showApplicantDialog = false },
            onCreate = { username, password, firstName, lastName, nationalId, phone ->
                onCreateApplicant(username, password, firstName, lastName, nationalId, phone)
                showApplicantDialog = false
            }
        )
    }
}

@Composable
private fun CollaboratorApplicantDialog(
    busy: Boolean,
    onDismiss: () -> Unit,
    onCreate: (String, String, String, String, String, String) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var nationalId by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    val valid = AuthUtils.usernameIsValid(username) && AuthUtils.passwordIsValid(password) &&
        firstName.isNotBlank() && lastName.isNotBlank() && nationalId.filter { it.isDigit() }.length == 10 && phone.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ایجاد کارجو") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text("ایمیل لازم نیست. نام کاربری فقط با حروف انگلیسی است.", style = MaterialTheme.typography.bodySmall, color = Ink3)
                KbField(username, { username = AuthUtils.sanitizeUsername(it) }, "نام کاربری انگلیسی", required = true)
                KbField(password, { password = it }, "رمز عبور (حداقل ۸ کاراکتر)", password = true, required = true)
                HorizontalDivider()
                KbField(firstName, { firstName = it }, "نام", required = true)
                KbField(lastName, { lastName = it }, "نام خانوادگی", required = true)
                KbField(nationalId, { nationalId = it.filter(Char::isDigit).take(10) }, "کد ملی", required = true, keyboardType = KeyboardType.Number)
                KbField(phone, { phone = it }, "شماره تلفن", required = true, keyboardType = KeyboardType.Phone)
            }
        },
        confirmButton = {
            TextButton(
                enabled = valid && !busy,
                onClick = {
                    onCreate(
                        username.trim().lowercase(), password, firstName.trim(), lastName.trim(),
                        nationalId.filter(Char::isDigit), phone.trim()
                    )
                }
            ) { Text(if (busy) "در حال ایجاد…" else "ایجاد") }
        },
        dismissButton = { TextButton(enabled = !busy, onClick = onDismiss) { Text("انصراف") } }
    )
}
