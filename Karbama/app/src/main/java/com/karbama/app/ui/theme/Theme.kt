package com.karbama.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** انتخاب رنگ برنامه؛ مقدار system با رنگ گوشی هماهنگ می‌شود. */
object ThemeMode {
    const val SYSTEM = "system"
    const val LIGHT = "light"
    const val DARK = "dark"
    val all = listOf(SYSTEM, LIGHT, DARK)
}

/*
 * Every UI color is read from this small stateful palette.  Existing screens use names
 * such as `CardBg` and `Ink`; making these getters reactive lets day/night mode affect
 * old and new screens alike without a second set of screen implementations.
 */
private var nightPalette by mutableStateOf(false)
val isKarbamaDarkTheme: Boolean get() = nightPalette
private fun tone(light: Color, dark: Color): Color = if (nightPalette) dark else light

// برند v10.0.4 — زبان بصری مرجع: slate تیره با accent قرمز.
// نام‌های قدیمی عمداً حفظ شده‌اند تا screenهای فعلی به‌تدریج، بدون شکستن
// رفتار یا داده‌های Firebase، به design system جدید منتقل شوند.
val Brand get() = tone(Color(0xFFB91C1C), Color(0xFFF87171))
val BrandDark get() = tone(Color(0xFF7F1D1D), Color(0xFFFCA5A5))
val BrandDeep get() = tone(Color(0xFF450A0A), Color(0xFF991B1B))
val BrandLight get() = tone(Color(0xFFFEF2F2), Color(0xFF3F141A))
val BrandTeal get() = tone(Color(0xFFDC2626), Color(0xFFEF4444))
val BrandGreen get() = tone(Color(0xFFDC2626), Color(0xFFEF4444))
val BrandTint2 get() = tone(Color(0xFFFEE2E2), Color(0xFF32151A))

// متن، سطوح و قاب‌ها
val Ink get() = tone(Color(0xFF0F172A), Color(0xFFF8FAFC))
val Ink2 get() = tone(Color(0xFF334155), Color(0xFFCBD5E1))
val Ink3 get() = tone(Color(0xFF64748B), Color(0xFF94A3B8))
val Line get() = tone(Color(0xFFE2E8F0), Color(0xFF334155))
val Surface get() = tone(Color(0xFFF8FAFC), Color(0xFF020617))
val FieldBg get() = tone(Color(0xFFF1F5F9), Color(0xFF0F172A))
val CardBg get() = tone(Color(0xFFFFFFFF), Color(0xFF111827))
val Bg get() = tone(Color(0xFFF8FAFC), Color(0xFF020617))

// لهجه‌ها
val Sun get() = tone(Color(0xFFFFB020), Color(0xFFFFBF4F))
val SunLight get() = tone(Color(0xFFFFF4DE), Color(0xFF493513))
val SunLine get() = tone(Color(0xFFFDE68A), Color(0xFF725A1C))
val Accent get() = tone(Color(0xFFF59E0B), Color(0xFFFFB942))
val AccentLight get() = tone(Color(0xFFFFF4DE), Color(0xFF493513))
val AccentGold get() = tone(Color(0xFFFFB020), Color(0xFFFFBF4F))
val AccentGoldDeep get() = tone(Color(0xFFF59E0B), Color(0xFFFFB942))
val Violet get() = tone(Color(0xFF7C5CFF), Color(0xFFA895FF))
val VioletDeep get() = tone(Color(0xFF5B3FE0), Color(0xFFC0B2FF))
val VioletLight get() = tone(Color(0xFFEEEBFF), Color(0xFF2D294B))
val Purple get() = tone(Color(0xFF7C5CFF), Color(0xFFA895FF))
val PurpleLight get() = tone(Color(0xFFEEEBFF), Color(0xFF2D294B))
val Coral get() = tone(Color(0xFFFF7A59), Color(0xFFFF967B))
val CoralDeep get() = tone(Color(0xFFE4562F), Color(0xFFFFA38C))
val CoralLight get() = tone(Color(0xFFFFEDE7), Color(0xFF4A2B25))
val Mint get() = tone(Color(0xFF2FD6A8), Color(0xFF57DDB8))
val MintLight get() = tone(Color(0xFFE2FBF3), Color(0xFF173F36))

// وضعیت‌ها
val Danger get() = tone(Color(0xFFE5484D), Color(0xFFFF7D82))
val DangerLight get() = tone(Color(0xFFFEF0F0), Color(0xFF482529))
val DangerText get() = tone(Color(0xFFB33A18), Color(0xFFFFA58C))
val Ok get() = tone(Color(0xFF12A150), Color(0xFF51C880))
val OkDeep get() = tone(Color(0xFF0E7A44), Color(0xFF6BE69A))
val OkLight get() = tone(Color(0xFFE4F7EC), Color(0xFF183D2A))
val Pink get() = tone(Color(0xFFC2185B), Color(0xFFFF83B2))
val PinkLight get() = tone(Color(0xFFFFE3ED), Color(0xFF4D253A))
val Blue get() = tone(Color(0xFF1D6FD4), Color(0xFF78B7FF))
val BlueLight get() = tone(Color(0xFFE7F2FF), Color(0xFF193859))

// هشدار و خنثی‌ها
val WarnText get() = tone(Color(0xFF7A5310), Color(0xFFFFD17B))
val WarnDeep get() = tone(Color(0xFF5A3D06), Color(0xFFFFD17B))
val WarnStrong get() = tone(Color(0xFF8A5B00), Color(0xFFFFCC66))
val Graphite get() = tone(Color(0xFF3B4753), Color(0xFFBECBD0))
val GraphiteLight get() = tone(Color(0xFFEFF3F5), Color(0xFF26363B))
val GraphiteLine get() = tone(Color(0xFFE3ECF0), Color(0xFF2D464C))

// قاب‌های تزئینی
val Bezel get() = tone(Color(0xFF0F172A), Color(0xFF020617))
val Bezel2 get() = tone(Color(0xFF334155), Color(0xFF111827))
val PhMid get() = tone(Color(0xFFCBD5E1), Color(0xFF475569))
val PhLow get() = tone(Color(0xFFE2E8F0), Color(0xFF334155))
val GlassFill get() = tone(Color(0xFFFFFFFF), Color(0xFF111827))
val GlassFillSoft get() = tone(Color(0xFFF8FAFC), Color(0xFF0F172A))
val GlassStroke get() = tone(Color(0xB3FFFFFF), Color(0x55475569))
val GlassBar get() = tone(Color(0xFFFFFFFF), Color(0xFF111827))

val BrandBrush: Brush get() = Brush.linearGradient(listOf(BrandDeep, Brand, BrandTeal))
val HeaderBrush: Brush get() = BrandBrush

// شبکهٔ فاصله و گردی
val S1 = 4.dp
val S2 = 8.dp
val S3 = 12.dp
val S4 = 16.dp
val S5 = 20.dp
val S6 = 24.dp
val RSm = RoundedCornerShape(8.dp)
val RChip = RoundedCornerShape(12.dp)
val RField = RoundedCornerShape(16.dp)
val RCard = RoundedCornerShape(20.dp)
val RSheet = RoundedCornerShape(24.dp)
val RPill = RoundedCornerShape(999.dp)

private val AppShapes = Shapes(
    extraSmall = RSm, small = RChip, medium = RField, large = RCard, extraLarge = RSheet
)

private fun typography(ink: Color) = Typography(
    titleLarge = TextStyle(fontSize = 20.sp, lineHeight = 30.sp, fontWeight = FontWeight.ExtraBold, color = ink, letterSpacing = (-0.2).sp),
    titleMedium = TextStyle(fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.Bold, color = ink, letterSpacing = (-0.1).sp),
    titleSmall = TextStyle(fontSize = 14.sp, lineHeight = 21.sp, fontWeight = FontWeight.Bold, color = ink),
    bodyLarge = TextStyle(fontSize = 15.sp, lineHeight = 23.sp, fontWeight = FontWeight.Normal, color = ink),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 22.sp, fontWeight = FontWeight.Normal, color = Ink2),
    bodySmall = TextStyle(fontSize = 13.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium, color = Ink3),
    labelLarge = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Bold),
    labelMedium = TextStyle(fontSize = 12.sp, lineHeight = 18.sp, fontWeight = FontWeight.Bold, color = Ink2),
    labelSmall = TextStyle(fontSize = 11.sp, lineHeight = 16.sp, fontWeight = FontWeight.Bold, color = Ink3)
)

@Composable
fun KarbamaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // SideEffect keeps old screens (which use the named palette values directly) in
    // the same mode as MaterialTheme without keeping a second UI tree.
    SideEffect { nightPalette = darkTheme }
    val colors = if (darkTheme) {
        darkColorScheme(
            primary = Brand, onPrimary = Color(0xFF062D2B), primaryContainer = BrandLight,
            onPrimaryContainer = BrandDark, secondary = Accent, onSecondary = Color(0xFF332300),
            secondaryContainer = AccentLight, onSecondaryContainer = WarnText, tertiary = Violet,
            background = Surface, onBackground = Ink, surface = CardBg, onSurface = Ink,
            surfaceVariant = FieldBg, onSurfaceVariant = Ink2, outline = Line, outlineVariant = Line,
            error = Danger, onError = Color(0xFF3B0B10), errorContainer = DangerLight, onErrorContainer = Danger
        )
    } else {
        lightColorScheme(
            primary = Brand, onPrimary = Color.White, primaryContainer = BrandLight,
            onPrimaryContainer = BrandDark, secondary = Accent, onSecondary = Color(0xFF5A3D06),
            secondaryContainer = AccentLight, onSecondaryContainer = WarnText, tertiary = Violet,
            background = Surface, onBackground = Ink, surface = CardBg, onSurface = Ink,
            surfaceVariant = FieldBg, onSurfaceVariant = Ink2, outline = Line, outlineVariant = Line,
            error = Danger, onError = Color.White, errorContainer = DangerLight, onErrorContainer = Danger
        )
    }
    MaterialTheme(colorScheme = colors, shapes = AppShapes, typography = typography(Ink), content = content)
}
