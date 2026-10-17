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
    adminUser: String,
    adminPass: String,
    onSaveSettings: (String, String, String) -> Unit,
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
    onExit: () -> Unit
) {
    SystemBars(darkIcons = false)

    var tab by remember { mutableIntStateOf(0) }
    var jobQuery by remember { mutableStateOf("") }
    var peopleQuery by remember { mutableStateOf("") }
    var cityFilter by remember { mutableStateOf("") }
    var specFilter by remember { mutableStateOf("") }
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
        applicants, peopleQuery, cityFilter, specFilter, minLevel, statusFilter, loc.code
    ) {
        applicants.filter { p ->
            val textOk = peopleQuery.isBlank() ||
                p.fullName.contains(peopleQuery, ignoreCase = true) ||
                p.phone.contains(peopleQuery) ||
                p.email.contains(peopleQuery, ignoreCase = true) ||
                p.city.contains(peopleQuery, ignoreCase = true) ||
                p.jobKeys.any { loc.jobTitle(it).contains(peopleQuery, ignoreCase = true) }
            val cityOk = cityFilter.isEmpty() || p.city == cityFilter
            val specOk = specFilter.isEmpty() || p.jobKeys.contains(specFilter)
            val levelOk = p.trLevel >= minLevel
            val statusOk = statusFilter.isEmpty() || p.status == statusFilter
            textOk && cityOk && specOk && levelOk && statusOk
        }.sortedByDescending { it.createdAt }
    }

    Scaffold(
        containerColor = Bg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            // #33: نوار «حالت نمایشی / متصل به Firebase» از بالای برنامه حذف شد
            TealTopBar(title = loc["admin_panel"]) {
                AdminAddButton(text = loc["add_listing"], onClick = onNewJob)   // #27
                AdminExitButton(text = loc["logout"], onClick = onExit)
            }
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(
                selectedTabIndex = tab,
                containerColor = GlassBar,
                contentColor = BrandDark
            ) {
                Tab(
                    selected = tab == 0,
                    onClick = { tab = 0 },
                    text = { Text(loc["tab_listings"] + " (" + loc.n(jobs.size) + ")") }
                )
                Tab(
                    selected = tab == 1,
                    onClick = { tab = 1 },
                    text = { Text(loc["tab_applicants"] + " (" + loc.n(applicants.size) + ")") }
                )
                Tab(
                    selected = tab == 2,
                    onClick = { tab = 2 },
                    text = { Text(loc["und_tabSettings"]) }
                )
            }

            // ---------- آمار (یک پله پایین‌تر: زیر دکمه‌های تب) ----------
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    loc.n(jobs.count { it.active }),
                    loc["stats_jobs"],
                    BrandDark,
                    Modifier.weight(1f)
                )
                StatCard(
                    loc.n(applicants.size),
                    loc["stats_people"],
                    Purple,
                    Modifier.weight(1f),
                    onClick = { tab = 1; statusFilter = "" }
                )
                StatCard(
                    loc.n(applicants.count { it.status == "new" }),
                    loc["stats_new"],
                    WarnStrong,
                    Modifier.weight(1f),
                    onClick = { tab = 1; statusFilter = "new" }
                )
            }

            if (tab == 0) {
                // ============ آگهی‌ها ============
                Column(Modifier.fillMaxSize()) {
                    OutlinedTextField(
                        value = jobQuery,
                        onValueChange = { jobQuery = it },
                        placeholder = { Text(loc["search_jobs_hint"], color = Ink3, fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Filled.Search, null, tint = Ink3) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = CardBg,
                            focusedContainerColor = CardBg,
                            focusedBorderColor = Brand,
                            unfocusedBorderColor = Line
                        )
                    )
                    LazyColumn(
                        Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (visibleJobs.isEmpty()) {
                            item {
                                EmptyState("📭", loc["no_listings"])
                            }
                        }
                        items(visibleJobs, key = { it.id }) { job ->
                            AdminJobRow(
                                job = job,
                                loc = loc,
                                onEdit = { onEditJob(job) },
                                onDelete = { pendingDelete = job }   // #26: اول می‌پرسد، بعد پاک می‌کند
                            )
                        }
                    }
                }
            } else if (tab == 2) {
                // ============ تنظیمات کارگزار (برای متن تعهدنامه) ============
                AdminSettingsTab(
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
                )
            } else {
                // ============ متقاضیان ============
                Column(Modifier.fillMaxSize()) {
                    OutlinedTextField(
                        value = peopleQuery,
                        onValueChange = { peopleQuery = it },
                        placeholder = { Text(loc["search_applicants"], color = Ink3, fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Filled.Search, null, tint = Ink3) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                            .padding(top = 8.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = CardBg,
                            focusedContainerColor = CardBg,
                            focusedBorderColor = Brand,
                            unfocusedBorderColor = Line
                        )
                    )

                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(Modifier.weight(1f)) {
                            PickerField(
                                label = loc["province"],
                                value = cityFilter,
                                placeholder = loc["all"],
                                options = Catalog.IR_PROVINCES.map { it.first to it.first },
                                onSelect = { cityFilter = it },
                                emptyOptionLabel = loc["all"]
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
                                emptyOptionLabel = loc["all"]
                            )
                        }
                    }

                    Column(Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            loc["filter_min_level"] + ": " + if (minLevel == 0) loc["all"] else loc.n(minLevel) + "%",
                            style = MaterialTheme.typography.labelSmall,
                            color = Ink2
                        )
                        Slider(
                            value = minLevel.toFloat(),
                            onValueChange = { minLevel = (it / 10).toInt() * 10 },
                            valueRange = 0f..100f,
                            steps = 9,
                            colors = SliderDefaults.colors(
                                thumbColor = Purple,
                                activeTrackColor = Purple,
                                inactiveTrackColor = Line
                            )
                        )
                    }

                    // ---------- تفکیک متقاضیان با کلیک روی وضعیت‌ها ----------
                    Text(
                        loc["status_filter_hint"],
                        style = MaterialTheme.typography.labelSmall,
                        color = Ink3,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(Modifier.weight(1f)) {
                            StatusChip(
                                "🧑‍🤝‍🧑 " + loc["all_applicants"] + " " + loc.n(applicants.size),
                                statusFilter.isEmpty()
                            ) { statusFilter = "" }
                        }
                        Box(Modifier.weight(1f)) {
                            StatusChip(
                                "🆕 " + loc["st_new"] + " " + loc.n(applicants.count { it.status == "new" }),
                                statusFilter == "new"
                            ) { statusFilter = if (statusFilter == "new") "" else "new" }
                        }
                    }
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(Modifier.weight(1f)) {
                            StatusChip(
                                "🔎 " + loc["st_reviewed"] + " " + loc.n(applicants.count { it.status == "reviewed" }),
                                statusFilter == "reviewed"
                            ) { statusFilter = if (statusFilter == "reviewed") "" else "reviewed" }
                        }
                        Box(Modifier.weight(1f)) {
                            StatusChip(
                                "🤝 " + loc["st_referred"] + " " + loc.n(applicants.count { it.status == "referred" }),
                                statusFilter == "referred"
                            ) { statusFilter = if (statusFilter == "referred") "" else "referred" }
                        }
                    }
                    Text(
                        loc["open_applicant_hint"],
                        style = MaterialTheme.typography.labelSmall,
                        color = Ink3,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    LazyColumn(
                        Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (visiblePeople.isEmpty()) {
                            item { EmptyState("👥", loc["no_applicants"]) }
                        }
                        items(visiblePeople, key = { it.id }) { p ->
                            ApplicantRow(
                                applicant = p,
                                loc = loc,
                                onClick = { onOpenApplicant(p) },
                                onSetStatus = { st -> onSetStatus(p.id, st) }
                            )
                        }
                    }
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

@Composable
private fun StatCard(
    value: String,
    label: String,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier = if (onClick != null) modifier.clickable(onClick = onClick) else modifier,
        shape = MaterialTheme.shapes.large,
        color = GlassFill,
        border = BorderStroke(1.dp, GlassStroke),
        shadowElevation = 6.dp
    ) {
        Column(
            Modifier.padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Black, color = accent)
            Text(label, style = MaterialTheme.typography.labelSmall, maxLines = 1)
        }
    }
}

@Composable
private fun AdminJobRow(
    job: Job,
    loc: Loc,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = GlassFill,
        border = BorderStroke(1.dp, GlassStroke),
        shadowElevation = 6.dp
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(BrandBrush),
                    contentAlignment = Alignment.Center
                ) { Text(loc.jobIcon(job.titleKey), fontSize = 18.sp) }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        job.titleKey.takeIf { it.isNotEmpty() }?.let { loc.jobTitle(it) } ?: job.titleCustom,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        job.company,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        TagChip(loc.money(job.salary), TagTone.Green)
                        TagChip(loc.contract(job.contract), TagTone.Purple)
                        TagChip(loc.timeAgo(job.publishedAt), TagTone.Plain)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.small,
                    border = androidx.compose.foundation.BorderStroke(1.3.dp, Line),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Ink)
                ) {
                    Icon(Icons.Filled.Edit, null, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(loc["edit"], style = MaterialTheme.typography.labelMedium)
                }
                DangerButton(
                    text = loc["delete"],
                    icon = Icons.Filled.Delete,
                    modifier = Modifier.weight(1f),
                    onClick = onDelete
                )
            }
        }
    }
}

@Composable
private fun ApplicantRow(
    applicant: Applicant,
    loc: Loc,
    onClick: () -> Unit,
    onSetStatus: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        color = GlassFill,
        border = BorderStroke(1.dp, GlassStroke),
        shadowElevation = 6.dp
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Purple, Brand))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        initials(applicant.fullName),
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(applicant.fullName, style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "📍 " + listOf(applicant.city, applicant.district)
                            .filter { it.isNotEmpty() }.joinToString(" / ") +
                            "  🗣 " + loc.n(applicant.trLevel) + "%",
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "💰 " + loc.money(applicant.undertaking?.fee ?: 0L),
                        style = MaterialTheme.typography.labelSmall,
                        color = BrandDark
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        applicant.jobKeys.take(3).joinToString(" • ") {
                            loc.jobIcon(it) + " " + loc.jobTitle(it)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Ink2,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    StatusBadge(applicant.status, loc)
                    Spacer(Modifier.height(4.dp))
                    Text(loc.timeAgo(applicant.createdAt), style = MaterialTheme.typography.labelSmall)
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusChip(loc["st_new"], applicant.status == "new") { onSetStatus("new") }
                StatusChip(loc["st_reviewed"], applicant.status == "reviewed") { onSetStatus("reviewed") }
                StatusChip(loc["st_referred"], applicant.status == "referred") { onSetStatus("referred") }
            }
        }
    }
}

@Composable
private fun StatusChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = if (selected) BrandLight else FieldBg,
        border = androidx.compose.foundation.BorderStroke(
            1.2.dp,
            if (selected) Brand else Line
        ),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) BrandDark else Ink2,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
        )
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
                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(Purple, Brand))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            initials(applicant.fullName),
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp
                        )
                    }
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

/** #27: دکمه‌ی «افزودن آگهی» در نوار بالا — ثابت، بدون هم‌پوشانی با «حذف» */
@Composable
private fun AdminAddButton(text: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .padding(end = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent,
        border = BorderStroke(1.4.dp, Color.White.copy(alpha = 0.65f)),
        shadowElevation = 6.dp
    ) {
        Row(
            Modifier
                .background(Brush.linearGradient(listOf(SunLine, Accent)))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Add,
                contentDescription = null,
                tint = WarnDeep,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = text,
                color = WarnDeep,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/** #25: دکمه‌ی خروج شیک پنل مدیریت — شیشه‌ای، گرد، آیکون + متن */
@Composable
private fun AdminExitButton(text: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .padding(end = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        // #33: زمینه‌ی تیره‌تر تا متن سفید «خروج از حساب» کنتراست کافی داشته باشد
        color = Color(0xFF06323A).copy(alpha = 0.46f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.55f)),
        shadowElevation = 6.dp
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
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
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Black,
                maxLines = 1
            )
        }
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
        style = MaterialTheme.typography.labelMedium,
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
        textStyle = MaterialTheme.typography.bodyLarge,
        placeholder = placeholder?.let {
            { Text(it, color = Ink3, style = MaterialTheme.typography.bodyLarge) }
        },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp),
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

@Composable
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
            Spacer(Modifier.height(16.dp))

            AdmLabel(loc["admin_user"])
            AdmField(
                value = userText,
                onValueChange = { userText = it.replace(" ", ""); saved = false },
                placeholder = "rezaei"
            )

            Spacer(Modifier.height(12.dp))
            AdmLabel(loc["admin_pass"])
            AdmField(
                value = passText,
                onValueChange = { passText = it; saved = false },
                placeholder = "••••••",
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
                revealed = passShown,
                revealable = true,
                onToggleReveal = { passShown = !passShown },
                revealLabel = loc[if (passShown) "pass_hide" else "pass_show"]
            )
            Spacer(Modifier.height(8.dp))
            Text(
                loc[if (passShown) "pass_note_shown" else "admin_pass_note"],
                style = MaterialTheme.typography.bodySmall,
                color = if (passShown) WarnStrong else Ink3
            )
            if (!valid) {
                Spacer(Modifier.height(8.dp))
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
            Spacer(Modifier.height(8.dp))
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
                Spacer(Modifier.height(16.dp))
                Text(loc["twofa_scan"], style = MaterialTheme.typography.bodySmall)
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
                    Text(loc["twofa_recovery"], style = MaterialTheme.typography.labelSmall, color = Ink2)
                    Spacer(Modifier.height(4.dp))
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
                        style = MaterialTheme.typography.bodySmall,
                        color = Ink3,
                        lineHeight = 19.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    GhostButton(
                        text = "🚫 " + loc["twofa_disable"],
                        onClick = onDisable2FA
                    )
                } else {
                    Spacer(Modifier.height(16.dp))
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
            AdmLabel(loc["und_adminCard"])
            AdmField(
                value = cardText,
                onValueChange = { input ->
                    cardText = FormatUtils.formatCard(input)
                    saved = false
                },
                placeholder = "0000 0000 0000 0000",
                keyboardType = KeyboardType.Number
            )
            Spacer(Modifier.height(16.dp))
            Text(
                loc["und_cardWarn"],
                style = MaterialTheme.typography.bodySmall,
                color = if (FormatUtils.cardIsValid(cardText)) Ink3 else Danger
            )
        }

        Spacer(Modifier.height(16.dp))
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
