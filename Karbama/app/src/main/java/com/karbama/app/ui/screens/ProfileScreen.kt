package com.karbama.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.karbama.app.data.model.Applicant
import com.karbama.app.data.model.Job
import com.karbama.app.data.model.Status
import com.karbama.app.ui.components.*
import com.karbama.app.ui.i18n.Loc
import com.karbama.app.ui.i18n.SUPPORTED_LANGS
import com.karbama.app.ui.i18n.langFlag
import com.karbama.app.ui.i18n.langLabel
import com.karbama.app.ui.theme.*
import com.karbama.app.util.WorldLanguages

@Composable
fun ProfileContent(
    loc: Loc,
    applicant: Applicant?,
    appliedCount: Int,
    adminMode: String,
    brokerFee: Long,
    brokerCard: String,
    brokerHolder: String,
    onRegister: () -> Unit,
    onEdit: () -> Unit,
    onLanguage: (String) -> Unit,
    onLogout: () -> Unit,
    likedJobs: List<Job> = emptyList(),          // #28
    onOpenJob: (Job) -> Unit = {},               // #28
    onUnlike: (Job) -> Unit = {},                // #28
    adsFee: Long = 0L,                           // #34
    adsLocked: Boolean = false,                  // #34
    onGoPay: () -> Unit = {}                     // #34
) {
    SystemBars(darkIcons = false)
    var showUndertaking by remember { mutableStateOf(false) }

    // ---- برگه‌ی تعهدنامه (نمای خواندنی) ----
    if (showUndertaking && applicant != null) {
        UndertakingDocument(
            loc = loc,
            signing = false,
            applicantName = applicant.firstName,
            applicantLast = applicant.lastName,
            applicantFather = applicant.fatherName,
            phone = applicant.phone,
            city = applicant.city,
            district = applicant.district,
            address = applicant.address,
            jobTitles = applicant.jobKeys.joinToString("، ") { loc.jobTitle(it) },
            levelText = loc.n(applicant.trLevel) + "% — " + loc.levelBand(applicant.trLevel),
            idPhoto = photoModel(applicant.idPhotoUrl),
            brokerFee = brokerFee,
            brokerCard = brokerCard,
            brokerHolder = brokerHolder,
            existing = applicant.undertaking,
            onBack = { showUndertaking = false }
        )
        return
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Bg)
            .verticalScroll(rememberScrollState())
    ) {
        // ---------------- هدر ----------------
        Column(
            Modifier
                .fillMaxWidth()
                .background(BrandBrush)
                .statusBarsPadding()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (applicant == null) {
                Text(
                    loc["profile_not_registered"],
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
            } else {
                // v50: عکس ثبت‌نام = عکس پروفایل
                ApplicantAvatar(applicant = applicant, size = 76.dp)
                Spacer(Modifier.height(8.dp))
                Text(
                    applicant.fullName,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    listOf(applicant.phone, applicant.email)
                        .filter { it.isNotBlank() }.joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "📍 " + listOf(applicant.city, applicant.district)
                                .filter { it.isNotEmpty() }.joinToString(" / "),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    StatusBadge(applicant.status, loc)
                }
            }
        }

        Column(Modifier.padding(16.dp)) {
            if (applicant == null) {
                PrimaryButton(text = loc["go_register"], onClick = onRegister)
                Spacer(Modifier.height(20.dp))
            } else {
                // ---------------- #34: وضعیت پرداخت هزینه‌ی نمایش آگهی‌ها ----------------
                PayStatusCard(
                    loc = loc,
                    applicant = applicant,
                    adsFee = adsFee,
                    onGoPay = onGoPay
                )
                Spacer(Modifier.height(12.dp))

                // ---------------- تخصص‌ها و زبان ----------------
                SectionCard {
                    SectionTitle(loc["my_specialties"])
                    Spacer(Modifier.height(8.dp))
                    Text(
                        (applicant.jobKeys.map { loc.jobIcon(it) + " " + loc.jobTitle(it) } +
                            listOfNotNull(
                                applicant.otherJobs.trim().takeIf { it.isNotEmpty() }
                                    ?.let { "✍️ " + it }
                            )).joinToString("  •  ").ifEmpty { "—" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Ink
                    )
                    Spacer(Modifier.height(16.dp))
                    SectionTitle(loc["my_level"])
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LinearProgressIndicator(
                            progress = { applicant.trLevel / 100f },
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            color = Brand,
                            trackColor = Line
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            loc.n(applicant.trLevel) + "%",
                            style = MaterialTheme.typography.labelLarge,
                            color = BrandDark
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        loc.levelBand(applicant.trLevel),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    // v45: «زبان‌هایی که بلدم»
                    Spacer(Modifier.height(16.dp))
                    SectionTitle("🗣 " + loc["spoken_langs"])
                    Spacer(Modifier.height(8.dp))
                    Text(
                        WorldLanguages.labels(applicant.spokenLangs, loc.code).ifEmpty { "—" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Ink
                    )
                }

                Spacer(Modifier.height(12.dp))

                // ---------------- مدارک ----------------
                SectionCard {
                    SectionTitle(loc["my_docs"])
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        DocThumb(
                            model = photoModel(applicant.idPhotoUrl),
                            label = loc["id_card"],
                            emptyText = loc["no_doc"],
                            modifier = Modifier.weight(1f)
                        )
                        DocThumb(
                            model = photoModel(applicant.bodyPhotoUrl),
                            label = loc["full_body"],
                            emptyText = loc["no_doc"],
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // ---------------- تعهدنامه ----------------
                SectionCard {
                    SectionTitle(loc["und_short"])
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (applicant.undertaking?.signed == true) "✓ " + loc["und_signed"]
                            else "⚠ " + loc["und_notsigned"],
                            style = MaterialTheme.typography.titleSmall,
                            color = if (applicant.undertaking?.signed == true) Ok else Accent
                        )
                        if (applicant.undertaking?.date?.isNotBlank() == true) {
                            Spacer(Modifier.width(8.dp))
                            Text(
                                loc.digits(applicant.undertaking?.date ?: ""),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    if (applicant.undertaking?.signature?.isNotBlank() == true) {
                        Spacer(Modifier.height(8.dp))
                        AsyncImage(
                            model = applicant.undertaking?.signature,
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(MaterialTheme.shapes.small)
                                .background(Color.White)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    GhostButton(
                        text = "📜 " + loc["und_reopen"],
                        onClick = { showUndertaking = true }
                    )
                }

                Spacer(Modifier.height(12.dp))

                SectionCard {
                    InfoRow(loc["my_applications"], loc.f("applied_count", loc.n(appliedCount)))
                    HorizontalDivider(color = Line)
                    InfoRow(loc["address"], applicant.address.ifBlank { "—" })
                HorizontalDivider(color = Line)
                InfoRow(loc["national_id"], loc.digits(applicant.nationalId).ifBlank { "—" })
                InfoRow(loc["residence"], listOf(applicant.city, applicant.district)
                        .filter { it.isNotEmpty() }.joinToString(" / "))
                    HorizontalDivider(color = Line)
                    InfoRow(loc["status_label"], loc.status(applicant.status))
                }

                Spacer(Modifier.height(12.dp))
                GhostButton(text = "✏️ " + loc["edit_info"], onClick = onEdit)
                Spacer(Modifier.height(20.dp))
            }

            // ---------------- #28: آگهی‌های پسندیده ----------------
            SectionCard {
                SectionTitle(if (adsLocked) "🔒 " + loc["pasandide"] else "❤ " + loc["pasandide"] + " (" + likedJobs.size + ")")
                Spacer(Modifier.height(8.dp))
                if (adsLocked) {
                    // #34: تا پرداخت هزینه، جزئیات آگهی‌های پسندیده هم بسته است
                    LikesLockedNote(loc = loc, onGoPay = onGoPay)
                } else if (likedJobs.isEmpty()) {
                    Text(
                        loc["liked_empty"],
                        style = MaterialTheme.typography.bodySmall,
                        color = Ink3
                    )
                } else {
                    likedJobs.forEach { job ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.medium)
                                .clickable { onOpenJob(job) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(13.dp))
                                    .background(BrandLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(loc.jobIcon(job.titleKey), fontSize = 18.sp)
                            }
                            Spacer(Modifier.width(8.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    job.titleKey.takeIf { it.isNotEmpty() }
                                        ?.let { loc.jobTitle(it) } ?: job.titleCustom,
                                    style = MaterialTheme.typography.titleSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    job.company + " · " + listOf(job.city, job.district)
                                        .filter { it.isNotEmpty() }.joinToString(" / "),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Ink3,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    loc.money(job.salary),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = BrandDark
                                )
                            }
                            IconButton(onClick = { onUnlike(job) }) {
                                Text("❤️", fontSize = 18.sp)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
            }

            Spacer(Modifier.height(16.dp))

            // ---------------- تنظیمات ----------------
            SectionCard {
                SectionTitle(loc["settings"])
                Spacer(Modifier.height(12.dp))
                Text(loc["language"], style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SUPPORTED_LANGS.forEach { code ->
                        SelectChip(
                            text = "${langFlag(code)} ${langLabel(code)}",
                            selected = loc.code == code,
                            onClick = { onLanguage(code) }
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            Spacer(Modifier.height(16.dp))
            GhostButton(
                text = loc["logout"],
                icon = Icons.AutoMirrored.Filled.Logout,
                onClick = onLogout
            )
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
fun DocThumb(
    model: Any?,
    label: String,
    emptyText: String,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(FieldBg),
            contentAlignment = Alignment.Center
        ) {
            if (model != null) {
                AsyncImage(
                    model = model,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text("🖼️", fontSize = 26.sp)
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            if (model != null) label else emptyText,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

fun initials(name: String): String =
    name.trim().split(" ").filter { it.isNotEmpty() }.take(2)
        .joinToString("") { it.first().uppercase() }
        .ifEmpty { "?" }

/** انتخاب زبان به‌صورت صفحه‌ی مستقل (از پروفایل یا صفحه اصلی) */
@Composable
fun LanguagePickerScreen(loc: Loc, onPick: (String) -> Unit, onBack: () -> Unit) {
    SystemBars(darkIcons = true)
    Scaffold(
        containerColor = Bg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = { TopBar(title = loc["language"], onBack = onBack) }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            SectionCard {
                SectionTitle(loc["lang_title"])
                Spacer(Modifier.height(12.dp))
                SUPPORTED_LANGS.forEach { code ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium)
                            .background(if (loc.code == code) BrandLight else Color.Transparent)
                            .clickable { onPick(code) }
                            .padding(horizontal = 12.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(langFlag(code), fontSize = 22.sp)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            langLabel(code),
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.weight(1f)
                        )
                        if (loc.code == code) {
                            Text(
                                "✓",
                                color = BrandDark,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

/** کارت خالی برای وضعیت‌های بدون داده */
@Composable
fun PlaceholderCard(text: String) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = GlassFillSoft,
        border = BorderStroke(1.dp, GlassStroke)
    ) {
        Text(
            text,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            textAlign = TextAlign.Center
        )
    }
}
