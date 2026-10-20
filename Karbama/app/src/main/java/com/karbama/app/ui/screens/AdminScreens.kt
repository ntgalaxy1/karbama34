@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.karbama.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.karbama.app.util.TotpUtil
import com.karbama.app.util.FormatUtils
import com.karbama.app.data.model.AdminMode
import com.karbama.app.data.model.Applicant
import com.karbama.app.data.model.Job
import com.karbama.app.data.sample.Catalog
import com.karbama.app.ui.components.*
import com.karbama.app.ui.i18n.Loc
import com.karbama.app.ui.theme.*
import com.karbama.app.util.WorldLanguages
import androidx.compose.foundation.border
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.Dp

// ==========================================================
// ورود مدیر
// ==========================================================

@Composable
fun AdminLoginScreen(
    loc: Loc,
    busy: Boolean,
    onPasswordLogin: (String, (Boolean) -> Unit) -> Unit,
    onAccountLogin: (String, String, (Boolean, Boolean) -> Unit) -> Unit,
    onBack: () -> Unit
) {
    SystemBars(darkIcons = true)

    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var showAccountForm by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var accPassword by remember { mutableStateOf("") }
    var accError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = Bg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = { TopBar(title = loc["admin_login_title"], onBack = onBack) }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            SectionCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(PurpleLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Security, contentDescription = null, tint = Purple)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(loc["admin_login_title"], style = MaterialTheme.typography.titleMedium)
                        Text(loc["admin_hint"], style = MaterialTheme.typography.bodySmall)
                    }
                }
                Spacer(Modifier.height(16.dp))
                KbField(
                    value = password,
                    onValueChange = { password = it; error = null },
                    label = loc["password"],
                    placeholder = "••••••••",
                    error = error,
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Password,
                    singleLine = true
                )
                Spacer(Modifier.height(16.dp))
                PrimaryButton(
                    text = loc["login"],
                    loading = busy,
                    onClick = {
                        onPasswordLogin(password) { ok ->
                            if (!ok) error = loc["wrong_password"]
                        }
                    }
                )
            }

            Spacer(Modifier.height(16.dp))

            SectionCard {
                Row(
                    Modifier.clickable { showAccountForm = !showAccountForm },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        loc["admin_account_login"],
                        style = MaterialTheme.typography.labelMedium,
                        color = Purple,
                        modifier = Modifier.weight(1f)
                    )
                    Text(if (showAccountForm) "▲" else "▼", color = Purple)
                }
                if (showAccountForm) {
                    Spacer(Modifier.height(12.dp))
                    KbField(
                        value = email,
                        onValueChange = { email = it; accError = null },
                        label = loc["admin_email"],
                        placeholder = "admin@mail.com",
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Email
                    )
                    Spacer(Modifier.height(12.dp))
                    KbField(
                        value = accPassword,
                        onValueChange = { accPassword = it; accError = null },
                        label = loc["admin_password"],
                        placeholder = "••••••••",
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Password
                    )
                    if (accError != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            accError!!,
                            color = Danger,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    GhostButton(
                        text = loc["login"],
                        onClick = {
                            onAccountLogin(email, accPassword) { ok, signedIn ->
                                accError = when {
                                    ok -> null
                                    !signedIn -> "ورود ناموفق / giriş başarısız"
                                    else -> loc["not_admin_account"]
                                }
                            }
                        }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Surface(shape = MaterialTheme.shapes.medium, color = AccentLight) {
                Text(
                    "🔒 " + loc["only_admin_note"],
                    style = MaterialTheme.typography.bodySmall,
                    color = WarnText,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

// ==========================================================
// پنل مدیریت
// ==========================================================

@Composable
fun AdminContent(
    loc: Loc,
    mode: String,
    jobs: List<Job>,
    applicants: List<Applicant>,
    onNewJob: () -> Unit,
    onEditJob: (Job) -> Unit,
    onDeleteJob: (Job) -> Unit,
    onOpenApplicant: (Applicant) -> Unit,
    brokerCard: String,
    brokerHolder: String,
    onSaveSettings: (String) -> Unit,
    twoFAEnabled: Boolean,
    twoFAPending: Boolean,
    twoFASecret: String,
    twoFARecovery: String,
    twoFAUri: String,
    onBegin2FA: () -> Unit,
    onConfirm2FA: (String) -> Boolean,
    onDisable2FA: () -> Unit,
    onSetStatus: (String, String) -> Unit,
    onSetFee: (Applicant, Long) -> Unit,
    adsFee: Long = 6000L,                                   // #34: هزینه‌ی نمایش آگهی‌ها
    onSaveAdsFee: (Long) -> Unit = {},                      // #34
    onSetPayment: (Applicant, String) -> Unit = { _, _ -> },  // #34: تأیید/لغو پرداخت
    onExit: () -> Unit
) {
    SystemBars(darkIcons = false)

    var tab by remember { mutableIntStateOf(0) }
    var jobQuery by remember { mutableStateOf("") }
    var peopleQuery by remember { mutableStateOf("") }
    var cityFilter by remember { mutableStateOf("") }
    var specFilter by remember { mutableStateOf("") }
    var langFilter by remember { mutableStateOf("") }   // v45: زبان‌هایی که بلد است
    var minLevel by remember { mutableStateOf(0) }
    var statusFilter by remember { mutableStateOf("") }
    var pendingDelete by remember { mutableStateOf<Job?>(null) }   // #26: آگهی‌ای که منتظر تأیید حذف است

    val live = mode == AdminMode.LIVE

    val visibleJobs = remember(jobs, jobQuery, loc.code) {
        jobs.filter { j ->
            jobQuery.isBlank() ||
                loc.jobTitle(j.titleKey).contains(jobQuery, ignoreCase = true) ||
                j.company.contains(jobQuery, ignoreCase = true) ||
                j.city.contains(jobQuery, ignoreCase = true) ||
                j.district.contains(jobQuery, ignoreCase = true)
        }.sortedByDescending { it.publishedAt }
    }

    val visiblePeople = remember(
        applicants, peopleQuery, cityFilter, specFilter, langFilter, minLevel, statusFilter, loc.code
    ) {
        applicants.filter { p ->
            val textOk = peopleQuery.isBlank() ||
                p.fullName.contains(peopleQuery, ignoreCase = true) ||
                p.phone.contains(peopleQuery) ||
                p.email.contains(peopleQuery, ignoreCase = true) ||
                p.city.contains(peopleQuery, ignoreCase = true) ||
                p.jobKeys.any { loc.jobTitle(it).contains(peopleQuery, ignoreCase = true) } ||
                p.spokenLangs.any { WorldLanguages.label(it, loc.code).contains(peopleQuery, ignoreCase = true) }
            val langOk = langFilter.isEmpty() || p.spokenLangs.contains(langFilter)
            val cityOk = cityFilter.isEmpty() || p.city == cityFilter
            val specOk = specFilter.isEmpty() || p.jobKeys.contains(specFilter)
            val levelOk = p.trLevel >= minLevel
            val statusOk = statusFilter.isEmpty() || p.status.ifEmpty { "new" } == statusFilter
            textOk && cityOk && specOk && langOk && levelOk && statusOk
        }.sortedByDescending { it.createdAt }
    }

    // v46: عین دمو — هر تب یک صفحه‌ی اسکرول‌شونده؛ تب‌ها (قرصی) بالای همان صفحه
    val statusCounts = remember(applicants) {
        applicants.groupingBy { it.status.ifEmpty { "new" } }.eachCount()
    }
    // v45: فیلتر «زبان‌هایی که بلد است» — زبان‌های متقاضیان فعلی بالای فهرست
    val worldLangs = remember(loc.code) { WorldLanguages.all(loc.code) }
    val langOptions = remember(worldLangs, applicants) {
        val used = applicants.flatMap { it.spokenLangs }.groupingBy { it }.eachCount()
        val (u, rest) = worldLangs.partition { used.containsKey(it.code) }
        u.sortedByDescending { used[it.code] ?: 0 }
            .map { it.code to "${it.label}  (${loc.n(used[it.code] ?: 0)})" } +
            rest.map { it.code to it.label }
    }
    val langSearch = remember(worldLangs) { worldLangs.associate { it.code to it.search } }

    Scaffold(
        containerColor = Bg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            // دمو: 🛡️ پنل مدیریت  [+]  [خروج از حساب] — در تنظیمات: ⚙️ تنظیمات کارگزار
            AdminHeader(
                icon = if (tab == 2) "⚙️" else "🛡️",
                title = if (tab == 2) loc["und_adminSettings"] else loc["admin_panel"],
                addLabel = loc["add_listing"],
                onAdd = onNewJob,
                exitLabel = loc["logout"],
                onExit = onExit
            )
        },
    ) { padding ->
        when (tab) {
            0 -> LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { AdminTabs(loc, tab, applicants.size) { tab = it } }
                // ---------- آمار (فقط در تب آگهی‌ها، مثل دمو) ----------
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatCard(
                            loc.n(jobs.count { it.active }), loc["stats_jobs"], BrandDark,
                            Modifier.weight(1f)
                        )
                        StatCard(
                            loc.n(applicants.size), loc["stats_people"], VioletDeep,
                            Modifier.weight(1f),
                            onClick = { tab = 1; statusFilter = "" }
                        )
                        StatCard(
                            loc.n(statusCounts["new"] ?: 0), "🆕 " + loc["st_new"], AccentGoldDeep,
                            Modifier.weight(1f),
                            onClick = { tab = 1; statusFilter = "new" }
                        )
                    }
                }
                item {
                    AdmSearch(jobQuery, { jobQuery = it }, loc["search_jobs_hint"])
                }
                if (visibleJobs.isEmpty()) {
                    item { EmptyState("📭", loc["no_listings"]) }
                }
                items(visibleJobs, key = { it.id }) { job ->
                    AdminJobRow(
                        job = job,
                        loc = loc,
                        onEdit = { onEditJob(job) },
                        onDelete = { pendingDelete = job }   // #26: اول می‌پرسد، بعد پاک می‌کند
                    )
                }
                item {
                    Spacer(Modifier.height(4.dp))
                    PrimaryButton(text = "＋ " + loc["add_listing"], onClick = onNewJob)
                }
            }

            2 -> AdminSettingsTab(
                loc = loc,
                brokerCard = brokerCard,
                adsFee = adsFee,                        // #34
                onSaveAdsFee = onSaveAdsFee,            // #34
                twoFAEnabled = twoFAEnabled,
                twoFAPending = twoFAPending,
                twoFASecret = twoFASecret,
                twoFARecovery = twoFARecovery,
                twoFAUri = twoFAUri,
                onSave = onSaveSettings,
                onBegin2FA = onBegin2FA,
                onConfirm2FA = onConfirm2FA,
                onDisable2FA = onDisable2FA,
                signedCount = applicants.count { it.undertaking?.signed == true },
                totalCount = applicants.size,
                modifier = Modifier.padding(padding),
                tabs = { AdminTabs(loc, tab, applicants.size) { tab = it } }
            )

            else -> LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { AdminTabs(loc, tab, applicants.size) { tab = it } }
                // ---------- تفکیک متقاضیان با کلیک روی وضعیت‌ها (دمو) ----------
                item {
                    Text(
                        "👆 " + loc["adm2_filter_hint"],
                        style = MaterialTheme.typography.bodySmall,
                        color = Ink3,
                        lineHeight = 22.sp
                    )
                }
                item {
                    FlowRowCompat {
                        StatusChip(
                            "🧑‍🤝‍🧑 " + loc["all_applicants"] + " " + loc.n(applicants.size),
                            statusFilter.isEmpty(), ChipTone.Plain
                        ) { statusFilter = "" }
                        StatusChip(
                            "🆕 " + loc["st_new"] + " " + loc.n(statusCounts["new"] ?: 0),
                            statusFilter == "new", ChipTone.Plain
                        ) { statusFilter = if (statusFilter == "new") "" else "new" }
                        StatusChip(
                            "🔎 " + loc["st_reviewed"] + " " + loc.n(statusCounts["reviewed"] ?: 0),
                            statusFilter == "reviewed", ChipTone.Amber
                        ) { statusFilter = if (statusFilter == "reviewed") "" else "reviewed" }
                        StatusChip(
                            "🤝 " + loc["st_referred"] + " " + loc.n(statusCounts["referred"] ?: 0),
                            statusFilter == "referred", ChipTone.Green
                        ) { statusFilter = if (statusFilter == "referred") "" else "referred" }
                    }
                }
                item {
                    Spacer(Modifier.height(4.dp))
                    AdmSearch(peopleQuery, { peopleQuery = it }, loc["search_applicants"])
                }
                item {
                    Text(
                        "👆 " + loc["adm2_open_hint"],
                        style = MaterialTheme.typography.bodySmall,
                        color = Ink3
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(Modifier.weight(1f)) {
                            PickerField(
                                label = loc["province"],
                                value = cityFilter,
                                placeholder = loc["all"],
                                options = Catalog.IR_PROVINCES.map { it.first to it.first },
                                onSelect = { cityFilter = it },
                                emptyOptionLabel = loc["all"],
                                inlineLabel = true,
                                containerColor = Color.White
                            )
                        }
                        Box(Modifier.weight(1f)) {
                            PickerField(
                                label = loc["filter_specialty"],
                                value = specFilter.takeIf { it.isNotEmpty() }
                                    ?.let { loc.jobTitle(it) } ?: "",
                                placeholder = loc["all"],
                                options = Catalog.JOB_TITLES.map { it.key to "${it.icon} ${it.name(loc.code)}" },
                                onSelect = { specFilter = it },
                                searchable = true,
                                searchPlaceholder = loc["search_profession"],
                                emptyOptionLabel = loc["all"],
                                inlineLabel = true,
                                containerColor = Color.White
                            )
                        }
                    }
                }
                item {
                    // v45: زبان‌هایی که بلد است
                    PickerField(
                        label = "🗣 " + loc["spoken_langs"],
                        value = langFilter.takeIf { it.isNotEmpty() }
                            ?.let { WorldLanguages.label(it, loc.code) } ?: "",
                        placeholder = loc["all"],
                        options = langOptions,
                        onSelect = { langFilter = it },
                        searchable = true,
                        searchPlaceholder = loc["search_lang"],
                        emptyOptionLabel = loc["all"],
                        matcher = { v, q -> langSearch[v]?.contains(q, ignoreCase = true) == true },
                        inlineLabel = true,
                        containerColor = Color.White
                    )
                }
                item {
                    Column {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            loc["filter_min_level"] + ": " +
                                if (minLevel == 0) loc["all"] else loc.n(minLevel) + "%",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Ink2
                        )
                        Slider(
                            value = minLevel.toFloat(),
                            onValueChange = { minLevel = (it / 10).toInt() * 10 },
                            valueRange = 0f..100f,
                            steps = 9,
                            colors = SliderDefaults.colors(
                                thumbColor = Brand,
                                activeTrackColor = Brand,
                                inactiveTrackColor = Line
                            )
                        )
                    }
                }
                if (visiblePeople.isEmpty()) {
                    item { EmptyState("👥", loc["no_applicants"]) }
                }
                items(visiblePeople, key = { it.id }) { p ->
                    ApplicantRow(
                        applicant = p,
                        loc = loc,
                        onClick = { onOpenApplicant(p) }
                    )
                }
            }
        }
    }

    // ---------- #26: تأیید حذف آگهی («مطمئن هستید؟») ----------
    val toDelete = pendingDelete
    if (toDelete != null) {
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            icon = {
                Box(
                    Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(DangerLight),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🗑", fontSize = 24.sp)
                }
            },
            title = {
                Text(
                    loc["del_confirm_title"],
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = BrandLight,
                        border = BorderStroke(1.dp, Color(0x3810B39F)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = loc.jobTitle(toDelete.titleKey) +
                                (if (toDelete.company.isBlank()) "" else " · " + toDelete.company),
                            style = MaterialTheme.typography.labelMedium,
                            color = BrandDark,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 8.dp)
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        loc["del_confirm_sure"],
                        style = MaterialTheme.typography.bodySmall,
                        color = Graphite,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteJob(toDelete)
                        pendingDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Danger,
                        contentColor = Color.White
                    )
                ) {
                    Text(loc["del_yes"], fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text(loc["del_no"], color = Ink2)
                }
            }
        )
    }
}

// ==========================================================
// v46 — اجزای پنل مدیریت دقیقاً مطابق karbama-demo.html
// ==========================================================

/** گرادیان appbar.teal دمو: linear-gradient(135deg,#088080,#0A7F91) */
private val AdminBarBrush = Brush.linearGradient(listOf(Color(0xFF088080), Color(0xFF0A7F91), Color(0xFF0A7F91)))
private val AdmCardShape = RoundedCornerShape(20.dp)

/** نوار بالای پنل: آیکون + عنوان، دکمه‌ی طلایی «+» و دکمه‌ی «خروج از حساب» */
@Composable
private fun AdminHeader(
    icon: String,
    title: String,
    addLabel: String,
    onAdd: () -> Unit,
    exitLabel: String,
    onExit: () -> Unit
) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .background(AdminBarBrush)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .defaultMinSize(minHeight = 62.dp)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(icon, fontSize = 20.sp)
            Text(
                title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                lineHeight = 22.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            AdminAddButton(text = addLabel, onClick = onAdd)      // #27
            AdminExitButton(text = exitLabel, onClick = onExit)   // #25
        }
    }
}

/** تب‌های قرصی دمو (.tabs): زمینه‌ی #ECF3F6، تب فعال سفید با سایه */
@Composable
private fun AdminTabs(loc: Loc, tab: Int, peopleCount: Int, onTab: (Int) -> Unit) {
    val labels = listOf(
        loc["tab_listings"],
        loc["tab_applicants"] + " (" + loc.n(peopleCount) + ")",
        loc["und_tabSettings"]
    )
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFECF3F6))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        labels.forEachIndexed { i, label ->
            val on = i == tab
            Box(
                Modifier
                    .weight(1f)
                    .then(if (on) Modifier.shadow(3.dp, RoundedCornerShape(12.dp)) else Modifier)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (on) Color.White else Color.Transparent)
                    .clickable { onTab(i) }
                    .padding(vertical = 10.dp, horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (on) BrandDark else Ink2,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/** کارت سفید مات دمو (.card / .person): گوشه‌ی گرد، قاب #E3ECF0، سایه‌ی نرم */
@Composable
private fun AdmCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    color: Color = Color.White,
    padding: Dp = 12.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clip(AdmCardShape).clickable(onClick = onClick) else Modifier),
        shape = AdmCardShape,
        color = color,
        border = BorderStroke(1.dp, Line),
        shadowElevation = 3.dp
    ) {
        Column(Modifier.padding(padding), content = content)
    }
}

/** کادر جست‌وجوی دمو */
@Composable
private fun AdmSearch(value: String, onChange: (String) -> Unit, placeholder: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        placeholder = { Text(placeholder, color = Ink3, fontSize = 14.sp) },
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        textStyle = MaterialTheme.typography.bodyMedium,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = Color.White,
            focusedContainerColor = Color.White,
            focusedBorderColor = Brand,
            unfocusedBorderColor = Line
        )
    )
}

/** برچسب کوچک .mini دمو (💵 حقوق، 🕒 نوع قرارداد، 🗓 زمان) */
@Composable
private fun Mini(text: String) {
    Box(
        Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF6FAFB))
            .border(1.dp, Color(0xFFEDF3F6), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Ink2, maxLines = 1)
    }
}

@Composable
private fun StatCard(
    value: String,
    label: String,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    AdmCard(modifier = modifier, onClick = onClick) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Black, color = accent)
            Spacer(Modifier.height(2.dp))
            Text(label, fontSize = 12.sp, color = Ink3, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

/** دکمه‌ی کوچک دمو: .btn-ghost.btn-sm / .btn-danger.btn-sm */
@Composable
private fun AdmSmallButton(text: String, danger: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier
            .clip(shape)
            .background(
                if (danger) Brush.linearGradient(listOf(Color(0xFFFEF0F0), Color(0xFFFFEDE7)))
                else Brush.linearGradient(listOf(Color.White, Color.White))
            )
            .border(1.5.dp, if (danger) Color(0xFFFFEDE7) else Line, shape)
            .clickable(onClick = onClick)
            .padding(vertical = 11.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (danger) DangerText else Ink,
            maxLines = 1
        )
    }
}

@Composable
private fun AdminJobRow(
    job: Job,
    loc: Loc,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    AdmCard(onClick = onEdit) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(BrandBrush),
                contentAlignment = Alignment.Center
            ) { Text(loc.jobIcon(job.titleKey), fontSize = 17.sp) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    job.titleKey.takeIf { it.isNotEmpty() }?.let { loc.jobTitle(it) } ?: job.titleCustom,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = Ink,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    listOf(job.company, job.city + if (job.district.isBlank()) "" else " / " + job.district)
                        .filter { it.isNotBlank() }.joinToString(" · "),
                    fontSize = 13.sp,
                    color = Ink3,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                FlowRowCompat {
                    Mini("💵 " + loc.money(job.salary))
                    Mini("🕒 " + loc.contract(job.contract))
                    Mini("🗓 " + loc.timeAgo(job.publishedAt))
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AdmSmallButton("✏️ " + loc["adm2_edit_job"], false, Modifier.weight(1f), onEdit)
            AdmSmallButton("🗑 " + loc["delete"], true, Modifier.weight(1f), onDelete)
        }
    }
}

/** ردیف متقاضی (.person دمو): آواتار، نام، محل/سطح ترکی، مبلغ، تخصص‌ها؛ سمت دیگر وضعیت/پرداخت/زمان */
@Composable
private fun ApplicantRow(
    applicant: Applicant,
    loc: Loc,
    onClick: () -> Unit
) {
    AdmCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val photo = photoModel(applicant.bodyPhotoUrl)
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .shadow(3.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Violet, Brand)))
                    .border(2.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (photo != null) {
                    AsyncImage(
                        model = photo,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        initials(applicant.fullName),
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(applicant.fullName, fontSize = 14.sp, fontWeight = FontWeight.Black, color = Ink)
                Spacer(Modifier.height(4.dp))
                Text(
                    "📍 " + listOf(applicant.city, applicant.district)
                        .filter { it.isNotEmpty() }.joinToString(" / ") +
                        " · 🗣 " + loc.n(applicant.trLevel) + "%",
                    fontSize = 13.sp,
                    color = Ink3
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "💰 " + loc.money(applicant.undertaking?.fee ?: 0L),
                    fontSize = 13.sp,
                    color = Ink3
                )
                if (applicant.jobKeys.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    FlowRowCompat {
                        applicant.jobKeys.take(3).forEach {
                            Mini(loc.jobIcon(it) + " " + loc.jobTitle(it))
                        }
                    }
                }
                if (applicant.spokenLangs.isNotEmpty()) {   // v45
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "🌐 " + WorldLanguages.labels(applicant.spokenLangs.take(4), loc.code),
                        fontSize = 12.sp,
                        color = Ink3,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                StatusBadge(applicant.status, loc)
                Spacer(Modifier.height(8.dp))
                PayBadge(applicant.payStatus, loc)   // #34
                Spacer(Modifier.height(8.dp))
                Text(loc.timeAgo(applicant.createdAt), fontSize = 12.sp, color = Ink3)
            }
        }
    }
}

private enum class ChipTone { Plain, Amber, Green }

/** چیپ وضعیت دمو (.chip / .chip.on) با رنگ‌های «بررسی‌شده» و «معرفی‌شده» */
@Composable
private fun StatusChip(text: String, selected: Boolean, tone: ChipTone, onClick: () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    val (bg, line, fg) = when {
        selected -> Triple(Brush.linearGradient(listOf(Color(0xFFE2FBF3), Color(0xFFD6F2EC))), Brand.copy(alpha = 0.42f), BrandDark)
        tone == ChipTone.Amber -> Triple(Brush.linearGradient(listOf(AccentLight, AccentLight)), SunLine, WarnText)
        tone == ChipTone.Green -> Triple(Brush.linearGradient(listOf(OkLight, OkLight)), Color(0xFFA7F3D0), OkDeep)
        else -> Triple(Brush.linearGradient(listOf(Color.White, Color(0xFFF6FAFB))), Line, Ink2)
    }
    Box(
        Modifier
            .then(if (selected) Modifier.shadow(3.dp, shape) else Modifier)
            .clip(shape)
            .background(bg)
            .border(1.dp, line, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(text, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = fg, maxLines = 1)
    }
}

// ==========================================================
// پروفایل متقاضی (فقط مدیر)
// ==========================================================

@Composable
fun ApplicantDetailScreen(
    applicant: Applicant,
    loc: Loc,
    brokerFee: Long,
    brokerCard: String,
    brokerHolder: String,
    onSetStatus: (String) -> Unit,
    onSetFee: (Long) -> Unit,
    adsFee: Long = 6000L,                              // #34
    onSetPayment: (String) -> Unit = {},               // #34: paid | ""
    onBack: () -> Unit,
    likedItems: List<com.karbama.app.data.repo.ActivityItem> = emptyList(),      // #29
    appliedItems: List<com.karbama.app.data.repo.ActivityItem> = emptyList(),    // #29
    activityLive: Boolean = false                                                // #29
) {
    SystemBars(darkIcons = true)
    var showUndertaking by remember { mutableStateOf(false) }
    val personFee = applicant.undertaking?.fee ?: brokerFee
    var feeText by remember(applicant.id, personFee) { mutableStateOf(personFee.toString()) }

    // ---- مشاهده‌ی برگه‌ی تعهدنامه‌ی امضاشده (نمای خواندنی) ----
    if (showUndertaking) {
        UndertakingDocument(
            loc = loc,
            signing = false,
            applicantName = applicant.firstName,
            applicantLast = applicant.lastName,
            applicantFather = applicant.fatherName,
            applicantNationalId = applicant.nationalId,
            phone = applicant.phone,
            city = applicant.city,
            district = applicant.district,
            address = applicant.address,
            jobTitles = (applicant.jobKeys.map { loc.jobTitle(it) } +
                listOfNotNull(applicant.otherJobs.trim().takeIf { it.isNotEmpty() }))
                .joinToString("، "),
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

    Scaffold(
        containerColor = Bg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = { TopBar(title = applicant.fullName, onBack = onBack) }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // ---------- مشخصات ----------
            SectionCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ApplicantAvatar(                       // v50: عکس ثبت‌نام
                        applicant = applicant,
                        size = 58.dp,
                        background = Brush.linearGradient(listOf(Purple, Brand)),
                        initialsColor = Color.White,
                        fontSize = 20.sp
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(applicant.fullName, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "📍 " + listOf(applicant.city, applicant.district)
                                .filter { it.isNotEmpty() }.joinToString(" / "),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    StatusBadge(applicant.status, loc)
                }
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = Line)
                InfoRow(loc["phone_label"], applicant.phone)
                HorizontalDivider(color = Line)
                InfoRow(loc["father_name"], applicant.fatherName.ifBlank { "—" })
                InfoRow(
                    loc["username"],
                    applicant.username.ifBlank { "—" }
                )
                InfoRow(
                    loc["national_id"],
                    if (applicant.nationalId.isBlank()) "—"
                    else loc.digits(applicant.nationalId)
                )
                HorizontalDivider(color = Line)
                InfoRow(loc["residence_label"], listOf(applicant.city, applicant.district)
                    .filter { it.isNotEmpty() }.joinToString(" / ").ifBlank { "—" })
                HorizontalDivider(color = Line)
                InfoRow(loc["address"], applicant.address.ifBlank { "—" })
                if (applicant.email.isNotBlank()) {
                    HorizontalDivider(color = Line)
                    InfoRow(loc["email_label"], applicant.email)
                }
                HorizontalDivider(color = Line)
                InfoRow(
                    loc["my_level"],
                    loc.n(applicant.trLevel) + "% — " + loc.levelBand(applicant.trLevel)
                )
                HorizontalDivider(color = Line)
                InfoRow(
                    (loc["my_specialties"] +
                        if (applicant.otherJobs.isBlank()) ""
                        else " · " + loc["other_jobs"] + ": " + applicant.otherJobs),
                    applicant.jobKeys.joinToString("، ") { loc.jobTitle(it) }.ifEmpty { "—" }
                )
                HorizontalDivider(color = Line)
                InfoRow(
                    loc["spoken_langs"],
                    WorldLanguages.labels(applicant.spokenLangs, loc.code).ifEmpty { "—" }
                )
                HorizontalDivider(color = Line)
                InfoRow(loc["published"], loc.timeAgo(applicant.createdAt))
            }

            Spacer(Modifier.height(12.dp))

            // ---------- مدارک ----------
            SectionCard {
                SectionTitle(loc["documents"])
                Spacer(Modifier.height(8.dp))
                Surface(shape = MaterialTheme.shapes.small, color = AccentLight) {
                    Row(
                        Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Lock,
                            null,
                            tint = WarnText,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            loc["only_admin_note"],
                            style = MaterialTheme.typography.bodySmall,
                            color = WarnText
                        )
                    }
                }
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
                Spacer(Modifier.height(12.dp))
                DocRules(loc)
            }

            Spacer(Modifier.height(12.dp))

            // ---------- #34: وضعیت پرداخت هزینه‌ی نمایش آگهی‌ها ----------
            SectionCard {
                SectionTitle("💳 " + loc["pay_sec"])
                Spacer(Modifier.height(8.dp))
                InfoRow(loc["pay_fee"], loc.money(adsFee))
                HorizontalDivider(color = Line)
                InfoRow(loc["pay_ref"], applicant.payTrackCode)
                if (applicant.payMethod.isNotBlank()) {
                    HorizontalDivider(color = Line)
                    InfoRow(loc["pay_method"], when (applicant.payMethod) {
                        "manual" -> loc["pay_manual"]
                        "zarinpal" -> loc["pay_iran"]
                        "iyzico" -> loc["pay_turkey"]
                        else -> applicant.payMethod
                    })
                }
                if (applicant.payReference.isNotBlank()) {
                    HorizontalDivider(color = Line)
                    InfoRow(loc["pay_tx_ref"], applicant.payReference)
                }
                HorizontalDivider(color = Line)
                InfoRow(loc["pay_sec"],
                    when (applicant.payStatus) {
                        "pending" -> "⏳ " + loc["pay_st_pending"]
                        "paid" -> "✅ " + loc["pay_st_paid"]
                        else -> "⛔ " + loc["pay_st_unpaid"]
                    }
                )
                if (applicant.payAt > 0L) {
                    HorizontalDivider(color = Line)
                    InfoRow(loc["date"], loc.timeAgo(applicant.payAt))
                }
                if (applicant.payReceiptUrl.isNotBlank()) {
                    Spacer(Modifier.height(12.dp))
                    DocThumb(
                        model = photoModel(applicant.payReceiptUrl),
                        label = loc["pay_receipt_image"],
                        emptyText = loc["no_doc"],
                        modifier = Modifier.fillMaxWidth()
                    )
                } else if (applicant.payStatus == "pending" && applicant.payMethod == "manual") {
                    Spacer(Modifier.height(8.dp))
                    Text("⚠️ " + loc["pay_manual_required"], style = MaterialTheme.typography.bodySmall, color = WarnText)
                }
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PayBadge(applicant.payStatus, loc)
                    Spacer(Modifier.weight(1f))
                }
                Spacer(Modifier.height(12.dp))
                if (applicant.payStatus == "paid") {
                    GhostButton(
                        text = "↩︎ " + loc["pay_cancel"],
                        onClick = { onSetPayment("") }
                    )
                } else {
                    PrimaryButton(
                        text = "✅ " + loc["pay_confirm"],
                        onClick = { onSetPayment("paid") }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // ---------- مبلغ اختصاصی این متقاضی ----------
            SectionCard {
                SectionTitle(loc["und_personFee"])
                Spacer(Modifier.height(8.dp))
                Text(
                    loc["und_personFeeHint"],
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = 20.sp
                )
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = feeText,
                        onValueChange = { feeText = it.filter { c -> c.isDigit() }.take(9) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = FieldBg,
                            focusedContainerColor = CardBg,
                            focusedBorderColor = Brand,
                            unfocusedBorderColor = Line
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    Box(
                        Modifier
                            .clip(MaterialTheme.shapes.medium)
                            .background(BrandLight)
                            .padding(horizontal = 12.dp, vertical = 16.dp)
                    ) {
                        Text(
                            loc.money(feeText.toLongOrNull() ?: personFee),
                            style = MaterialTheme.typography.labelLarge,
                            color = BrandDark
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                PrimaryButton(
                    text = loc["save"] + " ✓",
                    onClick = {
                        onSetFee(feeText.toLongOrNull() ?: personFee)
                    }
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    loc["und_defaultFee"] + ": " + loc.money(brokerFee),
                    style = MaterialTheme.typography.bodySmall,
                    color = Ink3
                )
            }

            Spacer(Modifier.height(12.dp))

            // ---------- تعهدنامه ----------
            SectionCard {
                SectionTitle(loc["und_short"])
                Spacer(Modifier.height(8.dp))
                InfoRow(
                    loc["status_label"],
                    if (applicant.undertaking?.signed == true) "✓ " + loc["und_signed"]
                    else "⚠ " + loc["und_notsigned"]
                )
                HorizontalDivider(color = Line)
                InfoRow(loc["und_date"], loc.digits(applicant.undertaking?.date ?: "—"))
                HorizontalDivider(color = Line)
                InfoRow(
                    loc["und_nationalId"],
                    loc.digits(
                        applicant.nationalId.ifBlank {
                            applicant.undertaking?.nationalId ?: "—"
                        }
                    )
                )
                HorizontalDivider(color = Line)
                InfoRow(loc["und_payerCard"], loc.digits(applicant.undertaking?.payerCard ?: "—"))
                HorizontalDivider(color = Line)
                InfoRow(loc["und_fee"], loc.money(applicant.undertaking?.fee ?: brokerFee))
                if (applicant.undertaking?.signature?.isNotBlank() == true) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        loc["und_sigThumb"],
                        style = MaterialTheme.typography.labelSmall,
                        color = Ink2
                    )
                    Spacer(Modifier.height(8.dp))
                    AsyncImage(
                        model = applicant.undertaking?.signature,
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(70.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(Color.White)
                    )
                }
                Spacer(Modifier.height(12.dp))
                GhostButton(
                    text = "📜 " + loc["und_reopen"],
                    onClick = { showUndertaking = true }
                )
            }

            Spacer(Modifier.height(12.dp))

            // ---------- وضعیت ----------
            SectionCard {
                SectionTitle(loc["status_label"])
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("new", "reviewed", "referred").forEach { st ->
                        SelectChip(
                            text = loc.status(st),
                            selected = applicant.status == st,
                            onClick = { onSetStatus(st) }
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            // ---------- #29: پسندیده‌ها و درخواست‌های این متقاضی (از حساب خودِ کاربر در سرور) ----------
            ActivityCard(
                title = "❤ " + loc["likes_section"],
                items = likedItems,
                loc = loc,
                live = activityLive,
                empty = loc["no_likes_yet"]
            )
            Spacer(Modifier.height(12.dp))
            ActivityCard(
                title = "📨 " + loc["apps_section"],
                items = appliedItems,
                loc = loc,
                live = activityLive,
                empty = loc["no_apps_yet"]
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ==========================================================
// تنظیمات کارگزار — مبلغ و شماره کارت در متن تعهدنامه
// ==========================================================

/** #23: کادر ورودی یکسان و مرتب برای تب «تنظیمات» پنل مدیر */
private val AdmFieldShape = RoundedCornerShape(16.dp)

@Composable
private fun admFieldColors() = OutlinedTextFieldDefaults.colors(
    unfocusedContainerColor = FieldBg,
    focusedContainerColor = CardBg,
    focusedBorderColor = Brand,
    unfocusedBorderColor = Line,
    cursorColor = Brand
)

/** #27/v46: دکمه‌ی «افزودن آگهی» دمو (.admin-add) — مربع طلایی با «+» (در گوشی، متن پنهان است) */
@Composable
private fun AdminAddButton(text: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        Modifier
            .size(40.dp)
            .shadow(6.dp, shape, ambientColor = Accent, spotColor = Accent)
            .clip(shape)
            .background(Brush.verticalGradient(listOf(Color(0xFFFFB020), Color(0xFFF59E0B))))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Filled.Add,
            contentDescription = text,
            tint = WarnDeep,
            modifier = Modifier.size(20.dp)
        )
    }
}

/** #25/v46: دکمه‌ی خروج دمو (.admin-exit) — قاب سفید نیمه‌شفاف، زمینه‌ی تیره‌ی مات، آیکون + متن */
@Composable
private fun AdminExitButton(text: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        Modifier
            .height(40.dp)
            .clip(shape)
            .background(Color(0xFF08606E))      // معادل مات rgba(6,50,58,.4) روی نوار → بدون دو رنگی
            .border(1.4.dp, Color.White.copy(alpha = 0.55f), shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.AutoMirrored.Filled.Logout,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(17.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1
        )
    }
}

@Composable
private fun IconTextButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit
) {
    TextButton(onClick = onClick, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)) {
        Icon(icon, contentDescription = null, tint = Ink2, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun AdmLabel(text: String) {
    Text(
        text,
        fontSize = 13.sp,
        fontWeight = FontWeight.ExtraBold,
        color = Ink2,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun AdmField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    revealed: Boolean = false,
    revealable: Boolean = false,
    onToggleReveal: () -> Unit = {},
    revealLabel: String = "",
    isError: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        isError = isError,
        textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
        placeholder = placeholder?.let {
            { Text(it, color = Ink3, style = MaterialTheme.typography.bodyLarge) }
        },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .shadow(3.dp, AdmFieldShape),
        shape = AdmFieldShape,
        visualTransformation = if (revealable && !revealed) PasswordVisualTransformation()
        else VisualTransformation.None,
        trailingIcon = if (revealable) {
            {
                IconButton(onClick = onToggleReveal) {
                    Icon(
                        imageVector = if (revealed) Icons.Filled.VisibilityOff
                        else Icons.Filled.Visibility,
                        contentDescription = revealLabel,
                        tint = Ink3
                    )
                }
            }
        } else null,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        colors = admFieldColors()
    )
}

/** عنوان بخش دمو (.sec-title) + توضیح کم‌رنگ زیر آن */
@Composable
private fun AdmSection(title: String, hint: String?) {
    SectionTitle(title)
    if (hint != null) {
        Spacer(Modifier.height(4.dp))
        Text(hint, fontSize = 13.sp, color = Ink3, lineHeight = 24.sp)
    }
    Spacer(Modifier.height(12.dp))
}

/**
 * v46: تب «تنظیمات» عین دمو:
 *  ۱) یک کارت: تنظیمات کارگزار (کارت بانکی) ── هزینه نمایش آگهی‌ها ── 💾 ذخیره
 *  ۲) کارت ورود دومرحله‌ای
 *  ۳) کارت خلاصه‌ی خاکستری (کارت کارگزار، وضعیت ۲FA، تعهدنامه‌های امضاشده)
 * بخش «یوزرنیم/پسورد مدیر» دمو عمداً حذف است: ورود مدیر فقط با Firebase است
 * و هیچ اطلاعات ورود مدیر در برنامه نمایش داده یا ذخیره نمی‌شود.
 */
@Composable
private fun AdminSettingsTab(
    loc: Loc,
    brokerCard: String,
    adsFee: Long = 6000L,                       // #34
    onSaveAdsFee: (Long) -> Unit = {},          // #34
    twoFAEnabled: Boolean,
    twoFAPending: Boolean,
    twoFASecret: String,
    twoFARecovery: String,
    twoFAUri: String,
    onSave: (String) -> Unit,
    onBegin2FA: () -> Unit,
    onConfirm2FA: (String) -> Boolean,
    onDisable2FA: () -> Unit,
    signedCount: Int = 0,
    totalCount: Int = 0,
    modifier: Modifier = Modifier,
    tabs: @Composable () -> Unit = {}
) {
    var cardText by remember { mutableStateOf(FormatUtils.formatCard(brokerCard)) }
    var adsFeeText by remember(adsFee) { mutableStateOf(adsFee.toString()) }   // #34
    var secretShown by remember { mutableStateOf(false) }
    var recoveryShown by remember { mutableStateOf(false) }
    var codeText by remember { mutableStateOf("") }
    var codeBad by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }
    val clipboard = LocalClipboardManager.current
    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 28.dp)
    ) {
        tabs()
        Spacer(Modifier.height(12.dp))

        // ---------- ۱) کارگزار + هزینه‌ی نمایش آگهی‌ها (یک کارت، مثل دمو) ----------
        AdmCard(padding = 16.dp) {
            AdmSection(loc["und_adminSettings"], loc["und_adminHint"])
            AdmLabel(loc["und_adminCard"])
            AdmField(
                value = cardText,
                onValueChange = { input ->
                    cardText = FormatUtils.formatCard(input)
                    saved = false
                },
                placeholder = "1234 1234 1234 1234",
                keyboardType = KeyboardType.Number
            )
            Spacer(Modifier.height(8.dp))
            Text(loc["adm2_card_hint"], fontSize = 13.sp, color = Ink3, lineHeight = 20.sp)
            if (cardText.isNotBlank() && !FormatUtils.cardIsValid(cardText)) {
                Spacer(Modifier.height(4.dp))
                Text(
                    "⚠️ " + loc["und_cardWarn"],
                    fontSize = 13.sp,
                    color = Danger,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = Line)
            Spacer(Modifier.height(16.dp))

            AdmSection(loc["pay_ads_fee"], loc["pay_ads_fee_hint"])
            AdmLabel(loc["pay_amount"])
            AdmField(
                value = adsFeeText,
                onValueChange = { v ->
                    adsFeeText = FormatUtils.digitsOnly(v, 9)
                    saved = false
                },
                placeholder = "500",
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            )
            Spacer(Modifier.height(8.dp))
            Text(
                loc.money(adsFeeText.toLongOrNull() ?: adsFee),
                fontSize = 13.sp,
                color = Ink3
            )
            Spacer(Modifier.height(12.dp))
            PrimaryButton(
                text = "💾 " + loc["save"],
                onClick = {
                    onSave(FormatUtils.digitsOnly(cardText, 16))
                    onSaveAdsFee(adsFeeText.toLongOrNull() ?: adsFee)
                    saved = true
                }
            )
            if (saved) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "✅ " + loc["adm2_saved"],
                    fontSize = 13.sp,
                    color = Ok,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // ---------- ۲) ورود دومرحله‌ای (2FA) ----------
        AdmCard(padding = 16.dp) {
            AdmSection(loc["twofa_title"], loc["twofa_hint"])
            Text(
                text = (if (twoFAEnabled) "🟢 " else "⚪ ") +
                    loc[if (twoFAEnabled) "twofa_on" else "twofa_off"],
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (twoFAEnabled) OkDeep else Ink3
            )

            if (!twoFAEnabled && !twoFAPending) {
                Spacer(Modifier.height(12.dp))
                PrimaryButton(
                    text = "🔐 " + loc["twofa_enable"],
                    onClick = onBegin2FA
                )
            }

            if (twoFAPending || twoFAEnabled) {
                Spacer(Modifier.height(12.dp))
                Text(loc["twofa_scan"], fontSize = 13.sp, color = Ink3, lineHeight = 22.sp)
                Spacer(Modifier.height(8.dp))
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
                            .padding(vertical = 12.dp, horizontal = 8.dp)
                    )
                }
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // #24: آیکون به‌جای ایموجی (ایموجی روی نوشته می‌افتاد)
                    IconTextButton(
                        icon = if (secretShown) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        text = loc[if (secretShown) "twofa_hide" else "twofa_show"],
                        onClick = { secretShown = !secretShown }
                    )
                    Spacer(Modifier.width(8.dp))
                    IconTextButton(
                        icon = Icons.Filled.ContentCopy,
                        text = loc["twofa_copy"],
                        onClick = { clipboard.setText(AnnotatedString(twoFASecret)) }
                    )
                }
                Text(
                    twoFAUri,
                    style = MaterialTheme.typography.bodySmall,
                    color = Ink3,
                    maxLines = 2
                )

                if (twoFAEnabled) {
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = Line)
                    Spacer(Modifier.height(12.dp))
                    SectionTitle(loc["twofa_recovery"])
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = AccentLight,
                        border = BorderStroke(1.dp, SunLine),
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
                                .padding(vertical = 8.dp)
                        )
                    }
                    IconTextButton(
                        icon = if (recoveryShown) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        text = loc[if (recoveryShown) "twofa_hide" else "twofa_show"],
                        onClick = { recoveryShown = !recoveryShown }
                    )
                    Text(
                        loc["twofa_recovery_note"],
                        fontSize = 13.sp,
                        color = Ink3,
                        lineHeight = 22.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    GhostButton(
                        text = "🚫 " + loc["twofa_disable"],
                        onClick = onDisable2FA
                    )
                } else {
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = Line)
                    Spacer(Modifier.height(12.dp))
                    AdmLabel(loc["twofa_code"])
                    AdmField(
                        value = codeText,
                        onValueChange = { codeText = it.filter { c -> c.isDigit() }.take(6); codeBad = false },
                        placeholder = "123456",
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done,
                        isError = codeBad
                    )
                    if (codeBad) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "⚠️ " + loc["twofa_wrong"],
                            fontSize = 13.sp,
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

        // ---------- ۳) کارت خلاصه‌ی خاکستری دمو ----------
        AdmCard(padding = 16.dp, color = Color(0xFFF6FAFB)) {
            SummaryLine(
                loc["und_brokerCard"],
                if (cardText.isBlank()) "—" else loc.digits(cardText)
            )
            SummaryLine(loc["twofa_title"], loc[if (twoFAEnabled) "twofa_on" else "twofa_off"])
            SummaryLine(loc["und_signed"], loc.n(signedCount) + " / " + loc.n(totalCount))
        }
    }
}


@Composable
private fun SummaryLine(k: String, v: String) {
    Row(Modifier.padding(vertical = 3.dp)) {
        Text("$k: ", fontSize = 13.sp, color = Ink3, lineHeight = 26.sp)
        Text(v, fontSize = 13.sp, color = Ink, fontWeight = FontWeight.Black, lineHeight = 26.sp)
    }
}

/** #29: کارت «پسندیده‌ها» یا «درخواست‌های ارسالی» یک متقاضی در پنل مدیر */
@Composable
private fun ActivityCard(
    title: String,
    items: List<com.karbama.app.data.repo.ActivityItem>,
    loc: Loc,
    live: Boolean,
    empty: String
) {
    SectionCard {
        SectionTitle(title)
        Spacer(Modifier.height(8.dp))
        if (!live) {
            Text(loc["activity_offline"], style = MaterialTheme.typography.bodySmall, color = Ink3)
        } else if (items.isEmpty()) {
            Text(empty, style = MaterialTheme.typography.bodySmall, color = Ink3)
        } else {
            Text(loc["activity_hint"], style = MaterialTheme.typography.labelSmall, color = Ink3)
            Spacer(Modifier.height(8.dp))
            items.forEach { item ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(11.dp))
                            .background(BrandLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(loc.jobIcon(item.titleKey), fontSize = 16.sp)
                    }
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            item.titleKey.takeIf { k -> k.isNotEmpty() }?.let { loc.jobTitle(it) } ?: item.titleCustom,
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            item.company + " · " + listOf(item.city, item.district).filter { x -> x.isNotEmpty() }.joinToString(" / "),
                            style = MaterialTheme.typography.bodySmall,
                            color = Ink3,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
