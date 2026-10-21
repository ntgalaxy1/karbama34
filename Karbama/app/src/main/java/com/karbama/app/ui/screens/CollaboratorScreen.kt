package com.karbama.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karbama.app.data.model.Applicant
import com.karbama.app.data.model.Hotel
import com.karbama.app.data.model.Job
import com.karbama.app.ui.components.KbField
import com.karbama.app.ui.components.PrimaryButton
import com.karbama.app.ui.components.SectionCard
import com.karbama.app.ui.components.SystemBars
import com.karbama.app.ui.components.ThemeModePicker
import com.karbama.app.ui.components.TopBar
import com.karbama.app.ui.i18n.Loc
import com.karbama.app.ui.theme.Ink3

/** Collaborator-facing, ownership-scoped workspace with no manager credential/payment controls. */
@Composable
fun CollaboratorContent(
    loc: Loc, busy: Boolean, jobs: List<Job>, hotels: List<Hotel>, applicants: List<Applicant>,
    themeMode: String, onThemeChange: (String) -> Unit, onNewJob: () -> Unit,
    onEditJob: (Job) -> Unit, onDeleteJob: (Job) -> Unit, onOpenApplicant: (Applicant) -> Unit,
    onSaveHotel: (Hotel) -> Unit, onDeleteHotel: (Hotel) -> Unit,
    onCreateApplicant: (String, String, String, String, String, String) -> Unit, onExit: () -> Unit
) {
    SystemBars(darkIcons = false)
    Column(Modifier.fillMaxSize()) {
        TopBar(title = "پنل همکار", subtitle = "اطلاعات و پرونده‌های مختص شما", onBack = onExit)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            CollaboratorApplicantCard(busy, onCreateApplicant)
            SectionCard {
                Text("آگهی‌های من")
                Spacer(Modifier.height(6.dp))
                Text("فقط آگهی‌هایی که خودتان ساخته‌اید در این بخش قابل ویرایش هستند.", fontSize = 13.sp, color = Ink3)
                Spacer(Modifier.height(10.dp))
                PrimaryButton(text = "＋ ایجاد آگهی", onClick = onNewJob)
                jobs.sortedByDescending { it.publishedAt }.forEach { job ->
                    HorizontalDivider(Modifier.padding(top = 10.dp))
                    Text(job.company.ifBlank { job.titleCustom.ifBlank { loc.jobTitle(job.titleKey) } }, modifier = Modifier.padding(top = 10.dp))
                    Text(listOf(job.city, job.district).filter { it.isNotBlank() }.joinToString(" · "), fontSize = 12.sp, color = Ink3)
                    TextButton(onClick = { onEditJob(job) }) { Text("ویرایش") }
                    TextButton(onClick = { onDeleteJob(job) }) { Text("حذف") }
                }
            }
            SectionCard {
                Text("کارجوهای من")
                Spacer(Modifier.height(6.dp))
                Text("این فهرست فقط پرونده‌هایی است که با حساب شما ایجاد شده‌اند.", fontSize = 13.sp, color = Ink3)
                applicants.forEach { applicant ->
                    HorizontalDivider(Modifier.padding(top = 10.dp))
                    TextButton(onClick = { onOpenApplicant(applicant) }, modifier = Modifier.fillMaxWidth()) { Text(applicant.fullName.ifBlank { applicant.username }) }
                }
            }
            HotelManagerCard(loc = loc, hotels = hotels, onSave = onSaveHotel, onDelete = onDeleteHotel)
            SectionCard { Text("تنظیمات من"); Spacer(Modifier.height(8.dp)); ThemeModePicker(loc = loc, mode = themeMode, onChange = onThemeChange) }
        }
    }
}

@Composable
private fun CollaboratorApplicantCard(busy: Boolean, onCreate: (String, String, String, String, String, String) -> Unit) {
    var username by remember { mutableStateOf("") }; var password by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }; var lastName by remember { mutableStateOf("") }
    var nationalId by remember { mutableStateOf("") }; var phone by remember { mutableStateOf("") }
    SectionCard {
        Text("ایجاد کارجو"); Spacer(Modifier.height(6.dp))
        Text("ابتدا حساب Firebase و سپس پروندهٔ وابسته به شما ساخته می‌شود. ایمیل و هش رمز ذخیره نمی‌شود.", fontSize = 13.sp, color = Ink3)
        Spacer(Modifier.height(12.dp))
        KbField(username, { username = it.lowercase().filter { c -> c.isLetterOrDigit() || c in ".-_" } }, "نام کاربری انگلیسی", placeholder = "jobseeker01")
        Spacer(Modifier.height(8.dp)); KbField(password, { password = it }, "رمز عبور", placeholder = "حداقل ۸ کاراکتر", keyboardType = KeyboardType.Password, password = true)
        Spacer(Modifier.height(8.dp)); KbField(firstName, { firstName = it }, "نام", placeholder = "Ali")
        Spacer(Modifier.height(8.dp)); KbField(lastName, { lastName = it }, "نام خانوادگی", placeholder = "Rezaei")
        Spacer(Modifier.height(8.dp)); KbField(nationalId, { nationalId = it.filter(Char::isDigit).take(10) }, "کد ملی", placeholder = "")
        Spacer(Modifier.height(8.dp)); KbField(phone, { phone = it }, "تلفن", placeholder = "+90 ...", keyboardType = KeyboardType.Phone)
        Spacer(Modifier.height(12.dp))
        PrimaryButton(text = "ایجاد حساب و پرونده", loading = busy, onClick = {
            if (username.length >= 3 && password.length >= 8 && firstName.isNotBlank() && lastName.isNotBlank()) {
                onCreate(username, password, firstName, lastName, nationalId, phone); password = ""
            }
        })
    }
}
