package com.karbama.app.ui.screens

import android.graphics.Bitmap
import android.graphics.Paint
import android.util.Base64
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.karbama.app.data.model.Undertaking
import com.karbama.app.data.undertaking.UndertakingText
import com.karbama.app.ui.components.*
import com.karbama.app.ui.components.photoModel
import com.karbama.app.ui.i18n.Loc
import com.karbama.app.ui.theme.*
import com.karbama.app.util.FormatUtils
import java.io.ByteArrayOutputStream

/**
 * تعهدنامه تسویه حساب هزینه کارگزار — برگه A4 با امضای الکترونیکی.
 *
 * دو حالت دارد:
 *  • حالت امضا (signing = true): فیلدهای شماره ملی و شماره کارت باز است و بوم امضا فعال است.
 *  • حالت خواندنی: همان برگه، ولی فقط نمایش (برای پروفایل و پنل مدیریت).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UndertakingDocument(
    loc: Loc,
    signing: Boolean,
    applicantName: String,
    applicantLast: String,
    applicantFather: String,
    applicantNationalId: String = "",
    phone: String,
    city: String,
    district: String,
    address: String,
    jobTitles: String,
    levelText: String,
    idPhoto: Any?,
    brokerFee: Long,
    brokerCard: String,
    brokerHolder: String,
    existing: Undertaking?,
    onBack: () -> Unit,
    onSigned: ((Undertaking) -> Unit)? = null
) {
    SystemBars(darkIcons = true)

    // ---- وضعیت فرم ----
    var nationalId by remember { mutableStateOf(existing?.nationalId ?: "") }
    // مبلغ هزینه کارگزار — متقاضی همین‌جا روی برگه وارد می‌کند
    var feeText by remember { mutableStateOf(existing?.fee?.toString() ?: "") }
    var feeEntry by remember { mutableStateOf(feeText.ifBlank { brokerFee.toString() }) }
    var payerCard by remember { mutableStateOf(existing?.payerCard ?: "") }
    var error by remember { mutableStateOf<String?>(null) }
    val signature = remember { SignatureState() }

    val signedAlready = existing?.signed == true
    val readOnly = !signing || signedAlready

    Scaffold(
        containerColor = Line,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopBar(
                title = loc["und_short"],
                subtitle = if (readOnly) loc["und_signed"] + " · " + (existing?.date ?: "") else loc["und_required"],
                // #23: «خواندن و امضای تعهدنامه الزامی است» به رنگ نوک‌مدادی
                subtitleColor = if (readOnly) Ink3 else Graphite,
                onBack = onBack
            ) {
                Box(
                    Modifier
                        .clip(RoundedCornerShape(7.dp))
                        .background(AccentLight)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("A4", fontSize = 11.sp, fontWeight = FontWeight.Black, color = WarnText)
                }
            }
        },
        bottomBar = {
            Surface(color = GlassBar, shadowElevation = 10.dp) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp)
                ) {
                    if (readOnly) {
                        PrimaryButton(text = loc["close"], onClick = onBack)
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { signature.clear() },
                                modifier = Modifier.heightIn(min = 52.dp),
                                shape = MaterialTheme.shapes.medium,
                                border = androidx.compose.foundation.BorderStroke(1.4.dp, Line),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Ink)
                            ) { Text("🧽 " + loc["und_clear"], style = MaterialTheme.typography.labelMedium) }
                            PrimaryButton(
                                text = loc["und_confirm"],
                                icon = Icons.Filled.Check,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    when {
                                        (feeEntry.toLongOrNull() ?: 0L) <= 0L ->
                                            error = loc["und_feeWarn"]
                                        !FormatUtils.cardIsValid(payerCard) ->
                                            error = loc["und_cardWarn"]
                                        !signature.hasInk ->
                                            error = loc["und_signFirst"]
                                        else -> {
                                            error = null
                                            onSigned?.invoke(
                                                Undertaking(
                                                    signed = true,
                                                    date = FormatUtils.todayIso(),
                                                    signedAt = System.currentTimeMillis(),
                                                    nationalId = FormatUtils.digitsOnly(
                                                        applicantNationalId.ifBlank { nationalId }, 10
                                                    ),
                                                    payerCard = FormatUtils.formatCard(payerCard),
                                                    fee = feeEntry.toLongOrNull() ?: brokerFee,
                                                    brokerCard = brokerCard,
                                                    signature = signature.toDataUrl()
                                                )
                                            )
                                        }
                                    }
                                }
                            )
                        }
                        if (error != null) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "⚠️ $error",
                                color = Danger,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
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
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ==================== برگه A4 ====================
            Box(
                Modifier
                    .fillMaxWidth()
                    .widthIn(max = 560.dp)
                    .aspectRatio(1f / 1.414f)          // نسبت دقیق A4
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.White)
                    .border(1.dp, Line, RoundedCornerShape(4.dp))
            ) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // ---------- عنوان ----------
                    Text(
                        text = loc["und_title"],
                        fontSize = 24.sp,
                        lineHeight = 38.sp,
                        fontWeight = FontWeight.Black,
                        color = Ink,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(2.5.dp)
                            .background(Ink)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "$brokerHolder · ${loc["und_originalNote"]}",
                        fontSize = 11.sp,
                        color = Ink3,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            FormatUtils.todayPersian(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Ink2
                        )
                        Text(
                            loc["und_date"] + ": " +
                                (existing?.date?.let { loc.digits(it) } ?: loc.digits(FormatUtils.todayIso())),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Ink2
                        )
                    }
                    Spacer(Modifier.height(12.dp))

                    // ---------- بندها ----------
                    val enteredFee = feeEntry.toLongOrNull()
                        ?: existing?.fee ?: brokerFee
                    val feeText = if (!readOnly && enteredFee <= 0L) "………"
                    else loc.money(enteredFee)
                    val nid = applicantNationalId.ifBlank { nationalId }
                        .ifBlank { existing?.nationalId ?: "" }
                    UndertakingText.clauses(
                        name = "$applicantName $applicantLast".trim(),
                        father = applicantFather.ifBlank { applicantLast }.ifBlank { "—" },
                        nationalId = loc.digits(nid.ifBlank { "………" }),
                        feeText = feeText
                    ).forEach { clause ->
                        Text(
                            text = clause,
                            fontSize = 12.sp,
                            lineHeight = 26.sp,
                            color = Ink,
                            textAlign = TextAlign.Justify,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    PaperDivider(loc["und_details"])

                    PaperRow(loc["first_name"] + " / " + loc["last_name"]) {
                        PaperValue("$applicantName $applicantLast", big = true)
                    }
                    PaperRow(loc["phone"]) { PaperValue(phone, big = true, ltr = true) }
                    PaperRow(loc["father_name"]) { PaperValue(applicantFather.ifBlank { "—" }) }
                    PaperRow(loc["und_residence"]) {
                        PaperValue(listOf(city, district).filter { it.isNotBlank() }.joinToString(" / "))
                    }
                    if (address.isNotBlank()) {
                        PaperRow(loc["address"]) { PaperValue(address) }
                    }
                    PaperRow(loc["my_specialties"]) { PaperValue(jobTitles) }
                    PaperRow(loc["my_level"]) { PaperValue(levelText) }
                    // کد ملی در مرحله‌ی ۱ ثبت‌نام گرفته می‌شود؛ اینجا فقط نمایش داده می‌شود
                    PaperRow(loc["und_nationalId"]) {
                        PaperValue(loc.digits(nid).ifBlank { "—" }, ltr = true)
                    }

                    PaperDivider(loc["und_payment"])
                    PaperRow(loc["und_fee"]) {
                        if (readOnly) PaperValue(feeText, big = true)
                        else Column {
                            PaperInputField(
                                value = feeEntry,
                                onValueChange = { feeEntry = FormatUtils.digitsOnly(it, 9) },
                                placeholder = brokerFee.toString(),
                                numeric = true,
                                ltr = true
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                loc["und_feeEntryHint"],
                                fontSize = 11.sp,
                                color = Ink3,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    PaperRow(loc["und_payerCard"]) {
                        if (readOnly) PaperValue(loc.digits(existing?.payerCard ?: "—"), ltr = true)
                        else PaperInputField(
                            value = FormatUtils.formatCard(payerCard),
                            onValueChange = { payerCard = FormatUtils.digitsOnly(it, 16) },
                            placeholder = "0000 0000 0000 0000",
                            numeric = true,
                            ltr = true
                        )
                    }
                    PaperRow(loc["und_brokerCard"]) {
                        val card = existing?.brokerCard?.takeIf { it.isNotBlank() } ?: brokerCard
                        PaperValue(
                            if (card.isBlank()) loc["und_brokerCardEmpty"] else loc.digits(card),
                            ltr = true
                        )
                    }

                    // ---------- الصاق کارت شناسایی ----------
                    PaperDivider(loc["und_idAttach"])
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(Surface)
                            .border(
                                1.5.dp,
                                Line,
                                RoundedCornerShape(9.dp)
                            )
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (idPhoto != null) {
                            AsyncImage(
                                model = idPhoto,
                                contentDescription = null,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 160.dp)
                            )
                        } else {
                            Text(
                                loc["und_noId"],
                                fontSize = 11.sp,
                                color = Ink3,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // ---------- امضا ----------
                    PaperDivider(loc["und_signature"])
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(Color.White)
                            .border(1.4.dp, Line, RoundedCornerShape(9.dp))
                    ) {
                        if (readOnly) {
                            val sig = existing?.signature ?: ""
                            if (sig.isNotBlank()) {
                                AsyncImage(
                                    model = sig,
                                    contentDescription = null,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(8.dp)
                                )
                            } else {
                                Text(
                                    "—",
                                    modifier = Modifier.align(Alignment.Center),
                                    color = Ink3
                                )
                            }
                            Text(
                                text = loc["und_date"] + ": " + loc.digits(existing?.date ?: "—"),
                                fontSize = 11.sp,
                                color = Ink3,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 8.dp)
                            )
                        } else {
                            SignaturePad(
                                state = signature,
                                modifier = Modifier.fillMaxSize()
                            )
                            if (!signature.hasInk) {
                                Text(
                                    "✍️ " + loc["und_signHint"],
                                    fontSize = 11.sp,
                                    color = Ink3,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 8.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "$brokerHolder · ${loc.digits(FormatUtils.todayIso())}",
                        fontSize = 11.sp,
                        color = Ink3,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

// ==========================================================
// اجزای برگه
// ==========================================================

@Composable
private fun PaperDivider(title: String) {
    Spacer(Modifier.height(16.dp))
    HorizontalDivider(thickness = 1.dp, color = Line)
    Spacer(Modifier.height(8.dp))
    Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.Black,
        color = Ink
    )
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun PaperRow(label: String, content: @Composable () -> Unit) {
    Column(Modifier.padding(bottom = 8.dp)) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Ink3)
        Spacer(Modifier.height(4.dp))
        content()
    }
}

@Composable
private fun PaperValue(text: String, big: Boolean = false, ltr: Boolean = false) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(7.dp))
            .background(Surface)
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            fontSize = if (big) 18.sp else 13.sp,
            fontWeight = FontWeight.Black,
            color = BrandDark
        )
    }
}

@Composable
private fun PaperInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    numeric: Boolean = false,
    ltr: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = Ink3, fontSize = 12.sp) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
            keyboardType = if (numeric) androidx.compose.ui.text.input.KeyboardType.Number
            else androidx.compose.ui.text.input.KeyboardType.Text
        ),
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = Surface,
            focusedContainerColor = Color.White,
            focusedBorderColor = Brand,
            unfocusedBorderColor = Line
        )
    )
}

// ==========================================================
// بوم امضای الکترونیکی
// ==========================================================

class SignatureState {
    private val paths = mutableStateListOf<Path>()
    private var current = Path()
    var hasInk by mutableStateOf(false)
        private set

    /** با هر جابه‌جایی قلم زیاد می‌شود تا بوم دوباره ترسیم شود */
    var version by mutableIntStateOf(0)
        private set

    private var canvasSize by mutableStateOf(IntSize(600, 300))
    private var drawing = false

    fun start(x: Float, y: Float) {
        current = Path().apply { moveTo(x, y) }
        drawing = true
        version++
    }

    fun move(x: Float, y: Float) {
        if (!drawing) return
        current.lineTo(x, y)
        if (!hasInk) hasInk = true
        version++
    }

    fun end() {
        drawing = false
        paths.add(current)
        current = Path()
        version++
    }

    fun tap(x: Float, y: Float) {
        paths.add(Path().apply {
            addOval(androidx.compose.ui.geometry.Rect(Offset(x - 1.5f, y - 1.5f), 3f))
        })
        hasInk = true
        version++
    }

    fun clear() {
        paths.clear()
        current = Path()
        hasInk = false
        version++
    }

    internal fun setSize(size: IntSize) { canvasSize = size }

    internal fun allPaths(): List<Path> = paths + listOf(current)

    /** تبدیل امضا به تصویر PNG و خروجی data URL برای ذخیره در پروفایل */
    fun toDataUrl(): String {
        val w = canvasSize.width.coerceAtLeast(300)
        val h = canvasSize.height.coerceAtLeast(150)
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bmp)
        canvas.drawColor(android.graphics.Color.WHITE)
        val paint = Paint().apply {
            color = 0xFF0B2A37.toInt()
            style = Paint.Style.STROKE
            strokeWidth = 3f
            isAntiAlias = true
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        allPaths().forEach { canvas.drawPath(it.asAndroidPath(), paint) }
        val out = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.PNG, 90, out)
        val base64 = Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
        bmp.recycle()
        return "data:image/png;base64,$base64"
    }
}

@Composable
fun SignaturePad(state: SignatureState, modifier: Modifier = Modifier) {
    val inkColor = Ink
    Canvas(
        modifier = modifier
            .onSizeChanged { state.setSize(it) }
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    // مصرف کردن رویداد تا اسکرول برگه وسط امضا کشیده نشود
                    down.consume()
                    state.start(down.position.x, down.position.y)
                    var moved = false
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (!change.pressed) {
                            change.consume()
                            if (moved) state.end() else { state.tap(down.position.x, down.position.y) }
                            break
                        }
                        state.move(change.position.x, change.position.y)
                        moved = true
                        change.consume()
                    }
                }
            }
    ) {
        // خواندن نسخه باعث می‌شود با هر حرکت قلم، بوم دوباره ترسیم شود
        val tick = state.version
        val stroke = Stroke(
            width = (3.dp.toPx() + (tick * 0f)),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
        state.allPaths().forEach { path ->
            drawPath(path = path, color = inkColor, style = stroke)
        }
    }
}
