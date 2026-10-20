package com.karbama.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/* ==========================================================
   #33 — تم یک‌دست کارباما (دیزاین v4)
   این فایل «تنها منبع» رنگ، فونت، فاصله و گردی گوشه است.
   هیچ صفحه‌ای نباید رنگ یا اندازه‌ی دلخواه خودش را بسازد.
   ========================================================== */

// ---------- برند (فیروزه‌ای) ----------
val Brand = Color(0xFF0E9F8E)          // رنگ اصلی
val BrandDark = Color(0xFF066F6B)      // متن روی زمینه‌ی روشن
val BrandDeep = Color(0xFF0A7F91)      // سرِ گرادیان
val BrandLight = Color(0xFFE6F7F4)     // زمینه‌ی بسیار روشن
val BrandTeal = Color(0xFF12B39F)   // فقط برای تزئین (متن روی آن نه)
val BrandGreen = Color(0xFF10B39F)
val BrandTint2 = Color(0xFFD6F2EC)

// ---------- متن (همه ≥ ۴.۵:۱ کنتراست روی سفید) ----------
val Ink = Color(0xFF0F1F2B)            // تیتر — ۱۶.۸:۱
val Ink2 = Color(0xFF3F5566)           // متن اصلی — ۷.۴:۱
val Ink3 = Color(0xFF57697A)           // متن کم‌رنگ/راهنما — ۵.۰:۱
val Line = Color(0xFFE3ECF0)           // خط و قاب
val Surface = Color(0xFFF6FAFB)        // سطح روشن
val FieldBg = Color(0xFFF8FBFC)        // زمینه‌ی فیلد ورودی
val CardBg = Color(0xFFFFFFFF)         // v44: مات و یک‌رنگ مثل دمو (بدون دو رنگی با سایه)
val Bg = Color(0x00000000)             // شفاف تا مِش رنگی ریشه دیده شود

// ---------- لهجه‌ها ----------
val Sun = Color(0xFFFFB020)
val SunLight = Color(0xFFFFF4DE)
val SunLine = Color(0xFFFDE68A)
val Accent = Color(0xFFF59E0B)
val AccentLight = Color(0xFFFFF4DE)
val AccentGold = Color(0xFFFFB020)
val AccentGoldDeep = Color(0xFFF59E0B)

val Violet = Color(0xFF7C5CFF)
val VioletDeep = Color(0xFF5B3FE0)
val VioletLight = Color(0xFFEEEBFF)
val Purple = Color(0xFF7C5CFF)
val PurpleLight = Color(0xFFEEEBFF)

val Coral = Color(0xFFFF7A59)
val CoralDeep = Color(0xFFE4562F)
val CoralLight = Color(0xFFFFEDE7)

val Mint = Color(0xFF2FD6A8)
val MintLight = Color(0xFFE2FBF3)

// ---------- وضعیت‌ها ----------
val Danger = Color(0xFFE5484D)
val DangerLight = Color(0xFFFEF0F0)
val DangerText = Color(0xFFB33A18)     // #34: متن قرمز خوانا روی زمینه‌ی روشن (۴.۵:۱)
val Ok = Color(0xFF12A150)
val OkDeep = Color(0xFF0E7A44)
val OkLight = Color(0xFFE4F7EC)
val Pink = Color(0xFFC2185B)
val PinkLight = Color(0xFFFFE3ED)
val Blue = Color(0xFF1D6FD4)
val BlueLight = Color(0xFFE7F2FF)

// ---------- هشدار (نوک‌مدادی) ----------
val WarnText = Color(0xFF7A5310)       // متن هشدار
val WarnDeep = Color(0xFF5A3D06)       // متن هشدار پررنگ
val WarnStrong = Color(0xFF8A5B00)
val Graphite = Color(0xFF3B4753)
val GraphiteLight = Color(0xFFEFF3F5)
val GraphiteLine = Color(0xFFE3ECF0)

// ---------- قاب گوشی و تصویرسازی ----------
val Bezel = Color(0xFF101C24)
val Bezel2 = Color(0xFF2B3D48)
val PhMid = Color(0xFFC6D5DE)
val PhLow = Color(0xFFD9E4EA)

// ---------- شیشه‌ی مات (glassmorphism) ----------
val GlassFill = Color(0xFFFFFFFF)       // v44: مات
val GlassFillSoft = Color(0xFFF6FAFB)   // v44: مات
val GlassStroke = Color(0xB3FFFFFF)
val GlassBar = Color(0xFFFFFFFF)        // v44: مات

// ---------- گرادیان‌ها ----------
val BrandBrush = Brush.linearGradient(listOf(Brand, Color(0xFF0A8E8C), BrandDeep))
val HeaderBrush = Brush.linearGradient(listOf(Brand, Color(0xFF0A8E8C), BrandDeep))

// ---------- شبکه‌ی فاصله (۴ پیکسلی) ----------
val S1 = 4.dp
val S2 = 8.dp
val S3 = 12.dp
val S4 = 16.dp
val S5 = 20.dp
val S6 = 24.dp

// ---------- گردی گوشه ----------
val RSm = RoundedCornerShape(8.dp)
val RChip = RoundedCornerShape(12.dp)
val RField = RoundedCornerShape(16.dp)
val RCard = RoundedCornerShape(20.dp)
val RSheet = RoundedCornerShape(24.dp)
val RPill = RoundedCornerShape(999.dp)

/* ---------- مقیاس تایپ یک‌دست: ۱۱ · ۱۲ · ۱۳ · ۱۴ · ۱۵ · ۱۶ · ۱۸ · ۲۱ ---------- */
private val AppTypography = Typography(
    titleLarge = TextStyle(fontSize = 20.sp, lineHeight = 30.sp, fontWeight = FontWeight.ExtraBold, color = Ink, letterSpacing = (-0.2).sp),
    titleMedium = TextStyle(fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.Bold, color = Ink, letterSpacing = (-0.1).sp),
    titleSmall = TextStyle(fontSize = 14.sp, lineHeight = 21.sp, fontWeight = FontWeight.Bold, color = Ink),
    bodyLarge = TextStyle(fontSize = 15.sp, lineHeight = 23.sp, fontWeight = FontWeight.Normal, color = Ink),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 22.sp, fontWeight = FontWeight.Normal, color = Ink2),
    bodySmall = TextStyle(fontSize = 13.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium, color = Ink3),
    labelLarge = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Bold),
    labelMedium = TextStyle(fontSize = 12.sp, lineHeight = 18.sp, fontWeight = FontWeight.Bold, color = Ink2),
    labelSmall = TextStyle(fontSize = 11.sp, lineHeight = 16.sp, fontWeight = FontWeight.Bold, color = Ink3)
)

private val AppShapes = Shapes(
    extraSmall = RSm,
    small = RChip,
    medium = RField,
    large = RCard,
    extraLarge = RSheet
)

private val LightColors = lightColorScheme(
    primary = Brand,
    onPrimary = Color.White,
    primaryContainer = BrandLight,
    onPrimaryContainer = BrandDark,
    secondary = Accent,
    onSecondary = Color(0xFF5A3D06),
    secondaryContainer = AccentLight,
    onSecondaryContainer = WarnText,
    tertiary = Violet,
    background = Bg,
    onBackground = Ink,
    surface = CardBg,
    onSurface = Ink,
    surfaceVariant = FieldBg,
    onSurfaceVariant = Ink2,
    outline = Line,
    outlineVariant = Line,
    error = Danger,
    onError = Color.White,
    errorContainer = DangerLight,
    onErrorContainer = Danger
)

@Composable
fun KarbamaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        shapes = AppShapes,
        typography = AppTypography,
        content = content
    )
}
