package com.karbama.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karbama.app.data.model.Applicant
import com.karbama.app.data.routes.Destinations
import com.karbama.app.data.model.Condition
import com.karbama.app.data.model.Contract
import com.karbama.app.data.model.Job
import com.karbama.app.data.model.JobMedia
import com.karbama.app.data.model.MediaType
import com.karbama.app.data.sample.Catalog
import com.karbama.app.ui.components.*
import androidx.compose.ui.graphics.Color
import com.karbama.app.ui.i18n.Loc
import com.karbama.app.ui.theme.*
import com.karbama.app.util.PhoneCountry
import com.karbama.app.util.PhoneUtils

// ==========================================================
// فرم ثبت / ویرایش آگهی (پنل مدیریت)
// ==========================================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun JobEditorScreen(
    loc: Loc,
    existing: Job?,
    busy: Boolean,
    onSave: (Job) -> Unit,
    onBack: () -> Unit
) {
    SystemBars(darkIcons = true)

    var titleKey by remember { mutableStateOf(existing?.titleKey ?: "") }
    var titleCustom by remember { mutableStateOf(existing?.titleCustom ?: "") }
    var company by remember { mutableStateOf(existing?.company ?: "") }
    var city by remember { mutableStateOf(existing?.city ?: "İstanbul") }
    var district by remember { mutableStateOf(existing?.district ?: "") }
    var salary by remember { mutableStateOf(existing?.salary?.toString() ?: "25000") }
    var contract by remember { mutableStateOf(existing?.contract ?: Contract.FULL) }
    val conditions = remember {
        mutableStateListOf<String>().apply { addAll(existing?.conditions ?: listOf(Condition.INSURANCE)) }
    }
    var description by remember { mutableStateOf(existing?.description ?: "") }
    var requirements by remember {
        mutableStateOf(existing?.requirements?.joinToString("\n") ?: "")
    }
    var contactPhone by remember { mutableStateOf(existing?.contactPhone ?: "") }
    var error by remember { mutableStateOf<String?>(null) }

    // ---------- عکس‌ها و ویدیوهای آگهی ----------
    val media = remember {
        mutableStateListOf<com.karbama.app.data.model.JobMedia>().apply {
            addAll(existing?.media ?: emptyList())
        }
    }
    var linkDialog by remember { mutableStateOf(false) }
    var linkText by remember { mutableStateOf("") }
    var linkIsVideo by remember { mutableStateOf(false) }

    val pickPhoto = rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            media.add(
                com.karbama.app.data.model.JobMedia(
                    com.karbama.app.data.model.MediaType.IMAGE,
                    it.toString()
                )
            )
        }
    }
    val pickVideo = rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            media.add(
                com.karbama.app.data.model.JobMedia(
                    com.karbama.app.data.model.MediaType.VIDEO,
                    it.toString(),
                    poster = ""
                )
            )
        }
    }

    val districts = Catalog.districtsOf(city)

    if (linkDialog) {
        AlertDialog(
            onDismissRequest = { linkDialog = false },
            title = { Text(loc["media_add_link"]) },
            text = {
                Column {
                    KbField(
                        value = linkText,
                        onValueChange = { linkText = it },
                        label = loc["media_link_label"],
                        placeholder = "https://…"
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = !linkIsVideo,
                            onClick = { linkIsVideo = false },
                            label = { Text(loc["media_photo"]) }
                        )
                        FilterChip(
                            selected = linkIsVideo,
                            onClick = { linkIsVideo = true },
                            label = { Text(loc["media_video"]) }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val u = linkText.trim()
                    if (u.isNotEmpty()) {
                        media.add(
                            JobMedia(
                                if (linkIsVideo) MediaType.VIDEO else MediaType.IMAGE,
                                u
                            )
                        )
                        linkText = ""
                        linkDialog = false
                    }
                }) { Text(loc["media_add_confirm"]) }
            },
            dismissButton = {
                TextButton(onClick = { linkDialog = false }) { Text(loc["cancel"]) }
            }
        )
    }

    Scaffold(
        containerColor = Bg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopBar(
                title = if (existing == null) loc["add_listing"] else loc["edit_listing"],
                onBack = onBack
            )
        },
        bottomBar = {
            Surface(color = GlassBar, shadowElevation = 10.dp) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp)
                ) {
                    PrimaryButton(
                        text = loc["save"],
                        icon = Icons.Filled.Save,
                        loading = busy,
                        onClick = {
                            if (titleKey.isEmpty() && titleCustom.isBlank()) {
                                error = loc["custom_title_or_key"]
                            } else if (company.isBlank()) {
                                error = loc["required_field"]
                            } else {
                                error = null
                                onSave(
                                    (existing ?: Job()).copy(
                                        titleKey = titleKey,
                                        titleCustom = titleCustom.trim(),
                                        company = company.trim(),
                                        city = city,
                                        district = district,
                                        salary = salary.filter { it.isDigit() }.toLongOrNull() ?: 0L,
                                        contract = contract,
                                        conditions = conditions.toList(),
                                        description = description.trim(),
                                        requirements = requirements.split("\n")
                                            .map { it.trim() }.filter { it.isNotEmpty() },
                                        contactPhone = contactPhone.trim(),
                                        media = media.toList()
                                    )
                                )
                            }
                        }
                    )
                    if (error != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "⚠️ $error",
                            color = Danger,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            SectionCard {
                SectionTitle(loc["job_title_label"])
                Spacer(Modifier.height(12.dp))
                PickerField(
                    label = loc["job_title_label"],
                    value = titleKey.takeIf { it.isNotEmpty() }?.let { loc.jobTitle(it) } ?: "",
                    placeholder = loc["select"],
                    options = Catalog.JOB_TITLES.map { it.key to "${it.icon} ${it.name(loc.code)}" },
                    onSelect = { titleKey = it },
                    searchable = true,
                    searchPlaceholder = loc["search_profession"]
                )
                Spacer(Modifier.height(16.dp))
                KbField(
                    value = titleCustom,
                    onValueChange = { titleCustom = it },
                    label = loc["custom_title"],
                    placeholder = "—"
                )
                Spacer(Modifier.height(16.dp))
                KbField(
                    value = company,
                    onValueChange = { company = it },
                    label = loc["company_label"],
                    placeholder = "Arma Tekstil",
                    required = true
                )
            }

            Spacer(Modifier.height(12.dp))

            SectionCard {
                SectionTitle(loc["sec_location"])
                Spacer(Modifier.height(12.dp))
                PickerField(
                    label = loc["city"],
                    value = city,
                    placeholder = loc["select"],
                    options = Catalog.CITIES.map { it to it },
                    onSelect = { city = it; district = "" }
                )
                Spacer(Modifier.height(16.dp))
                PickerField(
                    label = loc["district"],
                    value = district,
                    placeholder = loc["all_districts"],
                    options = districts.map { it to it },
                    onSelect = { district = it },
                    enabled = districts.isNotEmpty(),
                    searchable = districts.size > 12,
                    searchPlaceholder = loc["search_profession"]
                )
            }

            Spacer(Modifier.height(12.dp))

            SectionCard {
                SectionTitle(loc["salary"] + " / " + loc["contract_label"])
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    KbField(
                        value = salary,
                        onValueChange = { salary = it.filter { c -> c.isDigit() } },
                        label = loc["salary_label"],
                        placeholder = "25000",
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(16.dp))
                PickerField(
                    label = loc["contract_label"],
                    value = loc.contract(contract),
                    placeholder = loc["select"],
                    options = Contract.all.map { it to loc.contract(it) },
                    onSelect = { contract = it }
                )
                Spacer(Modifier.height(16.dp))
                SectionTitle(loc["conditions_label"])
                Spacer(Modifier.height(8.dp))
                FlowRowCompat {
                    Condition.all.forEach { c ->
                        SelectChip(
                            text = loc.condition(c),
                            selected = conditions.contains(c),
                            onClick = {
                                if (conditions.contains(c)) conditions.remove(c) else conditions.add(c)
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            SectionCard {
                SectionTitle(loc["description_label"])
                Spacer(Modifier.height(8.dp))
                KbField(
                    value = description,
                    onValueChange = { description = it },
                    label = loc["description"],
                    singleLine = false,
                    minLines = 4,
                    imeAction = ImeAction.Default
                )
                Spacer(Modifier.height(16.dp))
                KbField(
                    value = requirements,
                    onValueChange = { requirements = it },
                    label = loc["requirements_label"],
                    singleLine = false,
                    minLines = 4,
                    imeAction = ImeAction.Default
                )
                Spacer(Modifier.height(16.dp))
                KbField(
                    value = contactPhone,
                    onValueChange = { contactPhone = it },
                    label = loc["contact_label"],
                    placeholder = "+90 212 000 00 00",
                    keyboardType = KeyboardType.Phone
                )
            }

            // ---------- عکس‌ها و ویدیوهای آگهی ----------
            Spacer(Modifier.height(16.dp))
            SectionCard {
                SectionTitle(loc["media_title"])
                Spacer(Modifier.height(4.dp))
                Text(
                    loc["media_admin_hint"],
                    style = MaterialTheme.typography.bodySmall,
                    color = Ink3,
                    lineHeight = 20.sp
                )
                Spacer(Modifier.height(8.dp))

                if (media.isEmpty()) {
                    Text(
                        loc["media_empty"],
                        style = MaterialTheme.typography.bodySmall,
                        color = Ink3
                    )
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        itemsIndexed(media) { i, m ->
                            Box {
                                MediaThumb(m = m, loc = loc, size = 84.dp, onClick = {})
                                Box(
                                    Modifier
                                        .align(Alignment.TopEnd)
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Danger)
                                        .clickable { media.removeAt(i) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Filled.Close,
                                        contentDescription = loc["delete"],
                                        tint = Color.White,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                // v48: در حالت متصل ویدیوی داخل گوشی ذخیره نمی‌شود → فقط با لینک
                val connected = com.karbama.app.data.Service.db != null
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GhostButton(
                        text = loc["media_add_photo"],
                        icon = Icons.Filled.Image,
                        onClick = { pickPhoto.launch("image/*") },
                        modifier = Modifier.weight(1f)
                    )
                    if (!connected) {
                        GhostButton(
                            text = loc["media_add_video"],
                            icon = Icons.Filled.PlayArrow,
                            onClick = { pickVideo.launch("video/*") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                if (connected) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "🎬 " + loc["media_video_link_hint"],
                        style = MaterialTheme.typography.bodySmall,
                        color = Ink3,
                        lineHeight = 20.sp
                    )
                }
                Spacer(Modifier.height(8.dp))
                GhostButton(
                    text = loc["media_add_link"],
                    icon = Icons.Filled.Link,
                    onClick = { linkDialog = true }
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

// ==========================================================
// ویرایش پروفایل متقاضی
// ==========================================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditProfileScreen(
    loc: Loc,
    applicant: Applicant,
    busy: Boolean,
    onSave: (Applicant) -> Unit,
    onBack: () -> Unit
) {
    SystemBars(darkIcons = true)

    var firstName by remember { mutableStateOf(applicant.firstName) }
    var lastName by remember { mutableStateOf(applicant.lastName) }
    var fatherName by remember { mutableStateOf(applicant.fatherName) }
    var phone by remember { mutableStateOf(applicant.phone) }
    var phoneCountry by remember {
        mutableStateOf(PhoneCountry.ofIso(applicant.phoneCountry))
    }
    var city by remember { mutableStateOf(applicant.city) }        // استان
    var district by remember { mutableStateOf(applicant.district) } // شهرستان
    var address by remember { mutableStateOf(applicant.address) }
    var destinationCountry by remember { mutableStateOf(Destinations.normalize(applicant.destinationCountry)) }
    val jobKeys = remember { mutableStateListOf<String>().apply { addAll(applicant.jobKeys) } }
    var spokenLangs by remember { mutableStateOf(applicant.spokenLangs) }   // v45
    var level by remember { mutableStateOf(applicant.trLevel) }
    var error by remember { mutableStateOf<String?>(null) }

    val districts = Catalog.countiesOf(city)

    Scaffold(
        containerColor = Bg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = { TopBar(title = loc["edit_profile_title"], onBack = onBack) },
        bottomBar = {
            Surface(color = GlassBar, shadowElevation = 10.dp) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp)
                ) {
                    PrimaryButton(
                        text = loc["save_changes"],
                        loading = busy,
                        onClick = {
                            when {
                                firstName.isBlank() || lastName.isBlank() -> error = loc["required_field"]
                                !PhoneUtils.isValid(phoneCountry, phone) ->
                                    error = loc[phoneErrorKey(phoneCountry)]
                                destinationCountry.isBlank() -> error = loc["destination_required"]
                                city.isBlank() -> error = loc["err_city"]
                                jobKeys.isEmpty() -> error = loc["err_job"]
                                else -> {
                                    error = null
                                    onSave(
                                        applicant.copy(
                                            firstName = firstName.trim(),
                                            lastName = lastName.trim(),
                                            fatherName = fatherName.trim(),
                                            phone = PhoneUtils.format(phoneCountry, phone),
                                            phoneCountry = phoneCountry.iso,
                                            phoneLocal = PhoneUtils.digitsOf(phoneCountry, phone),
                                            city = city,
                                            district = district,
                                            address = address.trim(),
                                            destinationCountry = destinationCountry,
                                            jobKeys = jobKeys.toList(),
                                            spokenLangs = spokenLangs,
                                            trLevel = level
                                        )
                                    )
                                }
                            }
                        }
                    )
                    if (error != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "⚠️ $error",
                            color = Danger,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            SectionCard {
                SectionTitle(loc["sec_identity"])
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    KbField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = loc["first_name"],
                        modifier = Modifier.weight(1f)
                    )
                    KbField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = loc["last_name"],
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(16.dp))
                KbField(
                    value = fatherName,
                    onValueChange = { fatherName = it },
                    label = loc["father_name"]
                )
                Spacer(Modifier.height(16.dp))
                PhoneCountryPicker(
                    label = loc["phone_country"],
                    country = phoneCountry,
                    uiLang = loc.code,
                    searchPlaceholder = loc["search_country"],
                    onSelect = { phoneCountry = it }
                )
                Spacer(Modifier.height(12.dp))
                KbField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = loc["phone"],
                    placeholder = phoneCountry.example.ifEmpty { "123 456 789" },
                    prefix = "+" + phoneCountry.code,
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Done
                )
                Spacer(Modifier.height(8.dp))
                Text(loc["cc_hint"], style = MaterialTheme.typography.bodySmall, color = Ink3)
            }

            Spacer(Modifier.height(12.dp))

            SectionCard {
                SectionTitle(loc["sec_location_ir"])
                Spacer(Modifier.height(12.dp))
                PickerField(
                    label = loc["province"],
                    value = city,
                    placeholder = loc["select"],
                    options = Catalog.IR_PROVINCES.map { it.first to it.first },
                    onSelect = { city = it; district = "" },
                    searchable = true,
                    searchPlaceholder = loc["search_profession"]
                )
                Spacer(Modifier.height(16.dp))
                PickerField(
                    label = loc["county"],
                    value = district,
                    placeholder = loc["select"],
                    options = districts.map { it to it },
                    onSelect = { district = it },
                    enabled = districts.isNotEmpty(),
                    searchable = districts.size > 12,
                    searchPlaceholder = loc["search_profession"]
                )
                Spacer(Modifier.height(16.dp))
                KbField(
                    value = address,
                    onValueChange = { address = it },
                    label = loc["address"],
                    placeholder = loc["address_hint"],
                    singleLine = false,
                    minLines = 2
                )
            }

            Spacer(Modifier.height(12.dp))

            SectionCard {
                SectionTitle("🧭 " + loc["destination_country"])
                Spacer(Modifier.height(12.dp))
                PickerField(
                    label = loc["destination_country"],
                    value = Destinations.all.firstOrNull { it.code == destinationCountry }?.let { it.flag + " " + loc[it.nameKey] }.orEmpty(),
                    placeholder = loc["select"],
                    options = Destinations.all.map { it.code to (it.flag + " " + loc[it.nameKey]) },
                    onSelect = { destinationCountry = it }
                )
                Spacer(Modifier.height(8.dp))
                Text(loc["destination_route_hint"], style = MaterialTheme.typography.bodySmall, color = Ink3)
            }

            Spacer(Modifier.height(12.dp))

            SectionCard {
                SectionTitle(loc["sec_jobs"])
                Spacer(Modifier.height(12.dp))
                FlowRowCompat {
                    Catalog.JOB_TITLES.forEach { t ->
                        SelectChip(
                            text = "${t.icon} ${t.name(loc.code)}",
                            selected = jobKeys.contains(t.key),
                            onClick = {
                                if (jobKeys.contains(t.key)) jobKeys.remove(t.key) else jobKeys.add(t.key)
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            SectionCard {
                SectionTitle(loc["sec_lang"])
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Slider(
                        value = level.toFloat(),
                        onValueChange = { level = it.toInt() },
                        valueRange = 0f..100f,
                        steps = 19,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = Brand,
                            activeTrackColor = Brand,
                            inactiveTrackColor = Line
                        )
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        loc.n(level) + "%",
                        style = MaterialTheme.typography.labelLarge,
                        color = BrandDark
                    )
                }
                Text(
                    loc.levelBand(level),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = Line)
                Spacer(Modifier.height(16.dp))
                SpokenLangsField(
                    loc = loc,
                    selected = spokenLangs,
                    onChange = { spokenLangs = it }
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

/** FlowRow با تنظیمات پیش‌فرض (برای خوانایی بیشتر در فرم‌ها) */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRowCompat(
    modifier: Modifier = Modifier,
    content: @Composable FlowRowScope.() -> Unit
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        content = content
    )
}
