package com.karbama.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.karbama.app.data.model.Applicant
import com.karbama.app.data.model.Undertaking
import com.karbama.app.data.local.LocalStore
import com.karbama.app.data.sample.Catalog
import com.karbama.app.util.AuthUtils
import com.karbama.app.util.FormatUtils
import com.karbama.app.ui.components.*
import com.karbama.app.ui.i18n.Loc
import com.karbama.app.util.ImageUtils
import com.karbama.app.util.PhoneCountry
import com.karbama.app.util.PhoneUtils
import com.karbama.app.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class PickedPhoto(val uri: Uri?, val bytes: ByteArray?) {
    val isSet: Boolean get() = bytes != null && bytes.isNotEmpty()
}

private const val TOTAL_STEPS = 4

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RegistrationScreen(
    loc: Loc,
    busy: Boolean,
    brokerFee: Long,
    brokerCard: String,
    brokerHolder: String,
    onSubmit: (Applicant, ByteArray?, ByteArray?, Undertaking?) -> Unit,
    onBack: () -> Unit
) {
    SystemBars(darkIcons = true)

    var step by remember { mutableStateOf(1) }
    var showUndertaking by remember { mutableStateOf(false) }
    var undertaking by remember { mutableStateOf<Undertaking?>(null) }

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var fatherName by remember { mutableStateOf("") }
    var nationalId by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var otherJobs by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var phoneCountry by remember { mutableStateOf(PhoneCountry.TURKIYE) }
    var gender by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }        // استان
    var district by remember { mutableStateOf("") }    // شهرستان
    var address by remember { mutableStateOf("") }
    val jobKeys = remember { mutableStateListOf<String>() }
    var spokenLangs by remember { mutableStateOf(listOf<String>()) }   // v45
    var jobQuery by remember { mutableStateOf("") }
    var level by remember { mutableStateOf(40) }
    var idPhoto by remember { mutableStateOf<PickedPhoto?>(null) }
    var bodyPhoto by remember { mutableStateOf<PickedPhoto?>(null) }
    var consent by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val scroll = rememberScrollState()

    fun validate(current: Int): String? = when (current) {
        1 -> when {
            firstName.isBlank() || lastName.isBlank() || fatherName.isBlank() ->
                loc["required_field"]
            !FormatUtils.nationalIdIsValid(nationalId) -> loc["err_nid"]
            !AuthUtils.usernameIsValid(username) -> loc["username_hint"]
            LocalStore.findByUsername(username) != null -> loc["username_taken"]
            !AuthUtils.passwordIsValid(password) -> loc["password_short"]
            !PhoneUtils.isValid(phoneCountry, phone) -> loc[phoneErrorKey(phoneCountry)]
            else -> null
        }
        2 -> if (city.isBlank() || district.isBlank()) loc["err_city"]
            else if (address.isBlank()) loc["required_field"] else null
        3 -> if (jobKeys.isEmpty() && otherJobs.isBlank()) loc["err_job"] else null
        else -> when {
            !(idPhoto?.isSet ?: false) || !(bodyPhoto?.isSet ?: false) -> loc["err_docs"]
            undertaking?.signed != true -> loc["und_required"]
            !consent -> loc["err_consent"]
            else -> null
        }
    }

    // ---- برگه‌ی A4 تعهدنامه ----
    if (showUndertaking) {
        UndertakingDocument(
            loc = loc,
            signing = undertaking?.signed != true,
            applicantName = firstName,
            applicantLast = lastName,
            applicantFather = fatherName,
            applicantNationalId = nationalId,
            phone = if (PhoneUtils.isValid(phoneCountry, phone))
                PhoneUtils.format(phoneCountry, phone)
            else "+${phoneCountry.code} $phone",
            city = city,
            district = district,
            address = address,
            jobTitles = (jobKeys.map { loc.jobTitle(it) } +
                listOfNotNull(otherJobs.trim().takeIf { it.isNotEmpty() })).joinToString("، "),
            levelText = loc.n(level) + "% — " + loc.levelBand(level),
            idPhoto = idPhoto?.uri,
            brokerFee = brokerFee,
            brokerCard = brokerCard,
            brokerHolder = brokerHolder,
            existing = undertaking,
            onBack = { showUndertaking = false },
            onSigned = { u ->
                undertaking = u
                showUndertaking = false
            }
        )
        return
    }

    Scaffold(
        containerColor = Bg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Column {
                TopBar(
                    title = loc["reg_title"],
                    subtitle = loc.f("step_of", loc.n(step), loc.n(TOTAL_STEPS)),
                    onBack = { if (step > 1) step-- else onBack() }
                )
                LinearProgressIndicator(
                    progress = { step / TOTAL_STEPS.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp),
                    color = Brand,
                    trackColor = Line
                )
            }
        },
        bottomBar = {
            Surface(color = GlassBar, shadowElevation = 8.dp) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (step > 1) {
                        OutlinedButton(
                            onClick = { step--; error = null },
                            modifier = Modifier
                                .weight(0.36f)
                                .heightIn(min = 52.dp),
                            shape = MaterialTheme.shapes.medium,
                            border = androidx.compose.foundation.BorderStroke(1.4.dp, Line),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Ink)
                        ) { Text(loc["back"], style = MaterialTheme.typography.labelLarge) }
                    }
                    PrimaryButton(
                        text = if (step < TOTAL_STEPS) loc["next"] else loc["submit_app"],
                        loading = busy,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            val err = validate(step)
                            error = err
                            if (err == null) {
                                if (step < TOTAL_STEPS) step++ else {
                                    onSubmit(
                                        Applicant(
                                            firstName = firstName.trim(),
                                            fatherName = fatherName.trim(),
                                            lastName = lastName.trim(),
                                            nationalId = FormatUtils.digitsOnly(nationalId, 10),
                                            otherJobs = otherJobs.trim(),
                                            username = username.trim(),
                                            passwordHash = AuthUtils.hash(password),
                                            phone = PhoneUtils.format(phoneCountry, phone),
                                            phoneCountry = phoneCountry.iso,
                                            phoneLocal = PhoneUtils.digitsOf(phoneCountry, phone),
                                            city = city,
                                            district = district,
                                            address = address.trim(),
                                            jobKeys = jobKeys.toList(),
                                            spokenLangs = spokenLangs,
                                            trLevel = level,
                                            gender = gender,
                                            status = "new",
                                            consent = true,
                                            lang = loc.code
                                        ),
                                        idPhoto?.bytes,
                                        bodyPhoto?.bytes,
                                        undertaking
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scroll)
                .padding(16.dp)
        ) {
            when (step) {
                // ---------------- ۱) هویت و تماس ----------------
                1 -> {
                    SectionCard {
                        SectionTitle(loc["sec_identity"])
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            KbField(
                                value = firstName,
                                onValueChange = { firstName = it },
                                label = loc["first_name"],
                                placeholder = "Ali",
                                required = true,
                                modifier = Modifier.weight(1f)
                            )
                            KbField(
                                value = lastName,
                                onValueChange = { lastName = it },
                                label = loc["last_name"],
                                placeholder = "Rezaei",
                                required = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            KbField(
                                value = fatherName,
                                onValueChange = { fatherName = it },
                                label = loc["father_name"],
                                placeholder = "Reza",
                                required = true,
                                modifier = Modifier.weight(1f)
                            )
                            KbField(
                                value = nationalId,
                                onValueChange = {
                                    nationalId = FormatUtils.digitsOnly(it, 10)
                                },
                                label = loc["national_id"],
                                placeholder = "0079123456",
                                required = true,
                                keyboardType = KeyboardType.Number,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            loc["national_id_hint"],
                            style = MaterialTheme.typography.bodySmall,
                            color = Ink3
                        )
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            KbField(
                                value = username,
                                onValueChange = { username = AuthUtils.sanitizeUsername(it) },
                                label = loc["username"],
                                placeholder = "rezaei",
                                required = true,
                                keyboardType = KeyboardType.Ascii,
                                modifier = Modifier.weight(1f)
                            )
                            KbField(
                                value = password,
                                onValueChange = { password = it },
                                label = loc["password_label"],
                                placeholder = "••••",
                                required = true,
                                password = true,
                                keyboardType = KeyboardType.Ascii,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            loc["username_hint"] + " · " + loc["password_hint"],
                            style = MaterialTheme.typography.bodySmall,
                            color = Ink3
                        )
                        Spacer(Modifier.height(16.dp))
                        // v45: همه‌ی پیش‌شماره‌های دنیا با جست‌وجو
                        PhoneCountryPicker(
                            label = loc["phone_country"],
                            country = phoneCountry,
                            uiLang = loc.code,
                            searchPlaceholder = loc["search_country"],
                            onSelect = { phoneCountry = it; error = null }
                        )
                        Spacer(Modifier.height(12.dp))
                        KbField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = loc["phone"],
                            placeholder = phoneCountry.example.ifEmpty { "123 456 789" },
                            prefix = "+" + phoneCountry.code,
                            required = true,
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Done
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            loc["cc_hint"],
                            style = MaterialTheme.typography.bodySmall,
                            color = Ink3
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    SectionCard {
                        SectionTitle(loc["gender"])
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SelectChip(
                                text = loc["gender_m"],
                                selected = gender == "m",
                                onClick = { gender = "m" }
                            )
                            SelectChip(
                                text = loc["gender_f"],
                                selected = gender == "f",
                                onClick = { gender = "f" }
                            )
                            SelectChip(
                                text = loc["all"] + " / -",
                                selected = gender == "",
                                onClick = { gender = "" }
                            )
                        }
                    }
                }

                // ---------------- ۲) محل سکونت ----------------
                2 -> {
                    SectionCard {
                        SectionTitle(loc["sec_location_ir"])
                        Spacer(Modifier.height(16.dp))
                        PickerField(
                            label = loc["province"],
                            value = city,
                            placeholder = loc["select"],
                            required = true,
                            options = Catalog.IR_PROVINCES.map { it.first to it.first },
                            onSelect = { city = it; district = "" },
                            searchable = true,
                            searchPlaceholder = loc["search_profession"]
                        )
                        Spacer(Modifier.height(16.dp))
                        val counties = Catalog.countiesOf(city)
                        PickerField(
                            label = loc["county"],
                            value = district,
                            placeholder = loc["select"],
                            required = true,
                            options = counties.map { it to it },
                            onSelect = { district = it },
                            enabled = counties.isNotEmpty(),
                            searchable = counties.size > 12,
                            searchPlaceholder = loc["search_profession"]
                        )
                        Spacer(Modifier.height(16.dp))
                        KbField(
                            value = address,
                            onValueChange = { address = it },
                            label = loc["address"],
                            placeholder = loc["address_hint"],
                            required = true,
                            singleLine = false,
                            minLines = 2
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    InfoNote(
                        loc["sec_location_ir"] + ": " +
                            listOf(city, district).filter { it.isNotEmpty() }.joinToString(" / ")
                    )
                }

                // ---------------- ۳) تخصص و زبان ----------------
                3 -> {
                    SectionCard {
                        SectionTitle(loc["sec_jobs"])
                        Spacer(Modifier.height(8.dp))
                        Text(
                            loc["pick_jobs_hint"],
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = jobQuery,
                            onValueChange = { jobQuery = it },
                            placeholder = {
                                Text(loc["search_profession"], color = Ink3, fontSize = 13.sp)
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.small,
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = FieldBg,
                                focusedContainerColor = CardBg,
                                focusedBorderColor = Brand,
                                unfocusedBorderColor = Line
                            )
                        )
                        Spacer(Modifier.height(8.dp))

                        val filtered = Catalog.JOB_TITLES.filter {
                            jobQuery.isBlank() ||
                                it.names.values.any { n -> n.contains(jobQuery, ignoreCase = true) }
                        }
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            filtered.forEach { t ->
                                SelectChip(
                                    text = "${t.icon} ${t.name(loc.code)}",
                                    selected = jobKeys.contains(t.key),
                                    onClick = {
                                        if (jobKeys.contains(t.key)) jobKeys.remove(t.key)
                                        else jobKeys.add(t.key)
                                    }
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        SelectionSummary(loc.f("selected_count", loc.n(jobKeys.size)))
                        Spacer(Modifier.height(16.dp))
                        KbField(
                            value = otherJobs,
                            onValueChange = { otherJobs = it },
                            label = loc["other_jobs"],
                            placeholder = loc["other_jobs_ph"],
                            imeAction = ImeAction.Done
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            loc["other_jobs_hint"],
                            style = MaterialTheme.typography.bodySmall,
                            color = Ink3
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    SectionCard {
                        SectionTitle(loc["sec_lang"])
                        Spacer(Modifier.height(8.dp))
                        Text(loc["level_title"], style = MaterialTheme.typography.bodySmall)
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
                            Box(
                                modifier = Modifier
                                    .clip(MaterialTheme.shapes.small)
                                    .background(BrandLight)
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    loc.n(level) + "%",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = BrandDark
                                )
                            }
                        }
                        Text(
                            "🗣 " + loc.levelBand(level),
                            style = MaterialTheme.typography.titleSmall,
                            color = BrandDark,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        // v45: «زبان‌هایی که بلدم» — همه‌ی زبان‌های زنده‌ی دنیا
                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(color = Line)
                        Spacer(Modifier.height(16.dp))
                        SpokenLangsField(
                            loc = loc,
                            selected = spokenLangs,
                            onChange = { spokenLangs = it }
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            loc["spoken_langs_hint"],
                            style = MaterialTheme.typography.bodySmall,
                            color = Ink3
                        )
                    }
                }

                // ---------------- ۴) مدارک و رضایت ----------------
                else -> {
                    SectionCard {
                        SectionTitle(loc["sec_docs"])
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            PhotoBox(
                                loc = loc,
                                label = loc["doc_id"],
                                hint = loc["doc_id_hint"],
                                icon = Icons.Filled.Person,
                                photo = idPhoto,
                                onPicked = { idPhoto = it; error = null },
                                modifier = Modifier.weight(1f)
                            )
                            PhotoBox(
                                loc = loc,
                                label = loc["doc_body"],
                                hint = loc["doc_body_hint"],
                                icon = Icons.Filled.PhotoCamera,
                                photo = bodyPhoto,
                                onPicked = { bodyPhoto = it; error = null },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        DocRules(loc)
                        Spacer(Modifier.height(16.dp))
                        SectionTitle(loc["und_short"])
                        Spacer(Modifier.height(8.dp))
                        Text(
                            loc["und_intro"],
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 21.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (undertaking?.signed == true) "✓ " + loc["und_signed"]
                                else "⚠ " + loc["und_notsigned"],
                                color = if (undertaking?.signed == true) Ok else Accent,
                                style = MaterialTheme.typography.titleSmall
                            )
                            if (undertaking?.signed == true) {
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    loc.digits(undertaking?.date ?: "") +
                                        (undertaking?.fee?.let { " · " + loc.money(it) } ?: ""),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        if (undertaking?.signature?.isNotBlank() == true) {
                            Spacer(Modifier.height(8.dp))
                            AsyncImage(
                                model = undertaking?.signature,
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
                        Button(
                            onClick = { showUndertaking = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 52.dp),
                            shape = MaterialTheme.shapes.medium,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (undertaking?.signed == true) Brand else Ink,
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                (if (undertaking?.signed == true) "✅ " else "📜 ") +
                                    loc[if (undertaking?.signed == true) "und_reopen" else "und_open"],
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    SectionCard {
                        SectionTitle(loc["sec_consent"])
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.Top) {
                            Checkbox(
                                checked = consent,
                                onCheckedChange = { consent = it; error = null },
                                colors = CheckboxDefaults.colors(checkedColor = Brand)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                loc["consent_text"],
                                style = MaterialTheme.typography.bodySmall,
                                color = Ink2,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }
            }

            if (error != null) {
                // #23: پیام «خواندن و امضای تعهدنامه الزامی است» به رنگ نوک‌مدادی (بقیه‌ی خطاها سرخ می‌مانند)
                val undNote = error == loc["und_required"]
                Spacer(Modifier.height(12.dp))
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = if (undNote) GraphiteLight else DangerLight,
                    border = if (undNote) androidx.compose.foundation.BorderStroke(1.dp, GraphiteLine) else null
                ) {
                    Text(
                        (if (undNote) "✏️ " else "⚠️ ") + error,
                        color = if (undNote) Graphite else Danger,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun DocRules(loc: Loc) {
    Surface(shape = MaterialTheme.shapes.medium, color = AccentLight) {
        Column(Modifier.padding(12.dp)) {
            Text(
                loc["doc_note_title"],
                style = MaterialTheme.typography.labelMedium,
                color = WarnDeep,
                fontWeight = FontWeight.Black
            )
            Spacer(Modifier.height(8.dp))
            listOf(loc["doc_rule_women"], loc["doc_rule_men"], loc["doc_rule_quality"]).forEach {
                Text(
                    "• " + it,
                    style = MaterialTheme.typography.bodySmall,
                    color = WarnText,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
fun InfoNote(text: String) {
    Surface(shape = MaterialTheme.shapes.medium, color = BrandLight) {
        Text(
            "ℹ️ $text",
            style = MaterialTheme.typography.bodySmall,
            color = BrandDark,
            modifier = Modifier.padding(12.dp)
        )
    }
}

/** جعبه‌ی انتخاب عکس با پیش‌نمایش */
@Composable
fun PhotoBox(
    loc: Loc,
    label: String,
    hint: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    photo: PickedPhoto?,
    onPicked: (PickedPhoto) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            loading = true
            scope.launch {
                val bytes = withContext(Dispatchers.IO) { ImageUtils.prepare(context, uri) }
                loading = false
                onPicked(PickedPhoto(uri, bytes))
            }
        }
    }

    Column(
        modifier = modifier
            .heightIn(min = 170.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(if (photo?.isSet == true) BrandLight else FieldBg)
            .clickable { launcher.launch("image/*") }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (photo?.isSet == true) {
            AsyncImage(
                model = photo.uri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = Ok,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    loc["uploaded"],
                    style = MaterialTheme.typography.labelSmall,
                    color = Ok,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(loc["change_photo"], style = MaterialTheme.typography.bodySmall, color = BrandDark)
        } else if (loading) {
            CircularProgressIndicator(color = Brand, strokeWidth = 2.5.dp, modifier = Modifier.size(26.dp))
        } else {
            Icon(icon, contentDescription = null, tint = BrandDark, modifier = Modifier.size(26.dp))
            Spacer(Modifier.height(8.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
                color = Ink
            )
            Spacer(Modifier.height(2.dp))
            Text(
                hint,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                loc["tap_upload"],
                style = MaterialTheme.typography.labelSmall,
                color = BrandDark,
                fontWeight = FontWeight.Black
            )
        }
    }
}

// ---------------- اعتبارسنجی ----------------

/** اعتبارسنجی سریع شماره (برای فرم‌های دیگر) */
fun isValidTrPhone(input: String): Boolean = PhoneUtils.isValid(PhoneCountry.TURKIYE, input)

/** v45: کلید پیام خطای شماره بر اساس کشور */
fun phoneErrorKey(c: PhoneCountry): String = when (c.iso) {
    "IR" -> "err_phone_ir"
    "TR" -> "err_phone"
    else -> "err_phone_intl"
}

fun isValidEmail(input: String): Boolean =
    android.util.Patterns.EMAIL_ADDRESS.matcher(input.trim()).matches()
