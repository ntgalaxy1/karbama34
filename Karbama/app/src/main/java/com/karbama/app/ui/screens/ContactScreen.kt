package com.karbama.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karbama.app.R
import com.karbama.app.ui.components.BrandLogo
import com.karbama.app.ui.components.SystemBars
import com.karbama.app.ui.i18n.Loc
import com.karbama.app.ui.theme.Surface
import com.karbama.app.ui.theme.Bg
import com.karbama.app.ui.theme.Brand
import com.karbama.app.ui.theme.BrandBrush
import com.karbama.app.ui.theme.BrandDeep
import com.karbama.app.ui.theme.CardBg
import com.karbama.app.ui.theme.Danger
import com.karbama.app.ui.theme.DangerLight
import com.karbama.app.ui.theme.GlassFill
import com.karbama.app.ui.theme.GlassFillSoft
import com.karbama.app.ui.theme.GlassStroke
import com.karbama.app.ui.theme.Ink
import com.karbama.app.ui.theme.Ink2
import com.karbama.app.ui.theme.Ink3
import com.karbama.app.ui.theme.Line
import com.karbama.app.ui.theme.OkDeep

/** نام کاربری کارباما — روی همه‌ی راه‌های ارتباطی به‌صورت پیش‌فرض همین نوشته می‌شود */
private const val KARBAMA_HANDLE = "(karbama34)"

/**
 * یک راه ارتباطی.
 * appUri = اگر برنامه‌اش روی گوشی نصب باشد، همان برنامه باز می‌شود.
 * webUrl = اگر برنامه نصب نباشد، همین لینک باز می‌شود؛ متن لینک هم روی کارت دیده می‌شود.
 */
private data class Channel(
    val titleKey: String,
    /** بسته‌های رسمی همان برنامه — با تعیین بسته، اندروید مستقیم خودِ برنامه را باز می‌کند (بدون انتخاب‌گر و بدون مرورگر) */
    val packages: List<String>,
    val appUri: String,
    val webUrl: String,
    val shownLink: String,
    val accent: Color,
    val tile: Brush,
    val iconRes: Int,
    val iconSize: Dp
)

private val CHANNELS = listOf(
    Channel(
        titleKey = "contact_tg",
        packages = listOf(
            "org.telegram.messenger",
            "org.telegram.messenger.web",
            "org.telegram.plus",
            "org.thunderdog.challegram"
        ),
        appUri = "tg://resolve?domain=karbama34",
        webUrl = "https://t.me/karbama34",
        shownLink = "t.me/karbama34",
        accent = Color(0xFF229ED9),
        tile = Brush.linearGradient(listOf(Color(0xFF2AABEE), Color(0xFF229ED9))),
        iconRes = R.drawable.ic_brand_telegram,
        iconSize = 30.dp
    ),
    Channel(
        titleKey = "contact_wa",
        packages = listOf("com.whatsapp", "com.whatsapp.w4b"),
        appUri = "whatsapp://send?phone=905521213486",
        webUrl = "https://wa.me/905521213486",
        shownLink = "wa.me/905521213486",
        accent = OkDeep,
        tile = Brush.linearGradient(listOf(Color(0xFF3ED36F), Color(0xFF25D366))),
        iconRes = R.drawable.ic_brand_whatsapp,
        iconSize = 40.dp
    ),
    Channel(
        titleKey = "contact_ig",
        packages = listOf("com.instagram.android"),
        appUri = "instagram://user?username=karbama34",
        webUrl = "https://www.instagram.com/karbama34",
        shownLink = "instagram.com/karbama34",
        accent = Color(0xFFDD2A7B),
        tile = Brush.linearGradient(
            listOf(Color(0xFFF58529), Color(0xFFDD2A7B), Color(0xFF8134AF))
        ),
        iconRes = R.drawable.ic_brand_instagram,
        iconSize = 27.dp
    )
)

@Composable
fun ContactContent(loc: Loc) {
    val ctx = LocalContext.current
    SystemBars(darkIcons = false)

    Column(
        Modifier
            .fillMaxSize()
            .background(Bg)
    ) {
        // ---------------- هدر ----------------
        Column(
            Modifier
                .fillMaxWidth()
                .background(BrandBrush)
                .statusBarsPadding()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BrandLogo(size = 40.dp, onDark = true, modifier = Modifier.size(40.dp))
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = loc["tab_contact"],
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Text(
                        text = KARBAMA_HANDLE,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
                Icon(Icons.Filled.Call, contentDescription = null, tint = Color.White)
            }
        }

        // ---------------- بدنه ----------------
        Column(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // «ما هیچ شعبه یا نماینده‌ای نداریم»
            Surface(
                color = DangerLight,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Danger.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(Modifier.padding(16.dp)) {
                    Icon(
                        Icons.Filled.Warning,
                        contentDescription = null,
                        tint = Danger,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            text = loc["contact_no_branch"],
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Danger
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = loc["contact_only"],
                            style = MaterialTheme.typography.bodySmall,
                            color = Ink2,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            CHANNELS.forEach { ch ->
                ChannelCard(
                    ch = ch,
                    loc = loc,
                    onOpen = { openChannel(ctx, ch, loc) },
                    onCopy = {
                        val ok = copyLink(ctx, ch)
                        Toast.makeText(
                            ctx,
                            if (ok) loc["contact_copied"] else ch.webUrl,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
                Spacer(Modifier.height(8.dp))
            }

            Spacer(Modifier.height(4.dp))
            Text(
                text = loc["contact_tap"],
                style = MaterialTheme.typography.bodySmall,
                color = Ink3,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            // ---------- خط اعتبار در انتهای بخش «ارتباط با ما» ----------
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = GlassFillSoft,
                border = BorderStroke(1.dp, GlassStroke),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(   // #28: کم‌رنگ‌تر و کم‌تر
                    text = "powered by naser tavakkoli @nt.galaxy",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    color = Ink3.copy(alpha = 0.72f),
                    letterSpacing = 0.3.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 12.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ChannelCard(
    ch: Channel,
    loc: Loc,
    onOpen: () -> Unit,
    onCopy: () -> Unit
) {
    Surface(
        color = GlassFill,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, GlassStroke),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() }
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // لوگوی خود برنامه‌ی ارتباطی
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(ch.tile),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(ch.iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(ch.iconSize)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = loc[ch.titleKey],
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Ink
                )
                Text(
                    text = KARBAMA_HANDLE,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = ch.accent
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = ch.shownLink,
                    style = MaterialTheme.typography.bodySmall,
                    color = Ink3
                )
            }

            Spacer(Modifier.width(8.dp))

            TextButton(onClick = onCopy) {
                Text(text = loc["contact_copy"], fontSize = 12.sp, color = Brand)
            }
        }
    }
}

/**
 * انتقال به «خودِ برنامه» — با لمس هر نقطه‌ای از کارت.
 * تلگرام → خود تلگرام باز می‌شود و وارد کانال می‌شود · واتس‌اپ → چت همان شماره · اینستاگرام → همان پیج.
 *
 * ترتیب کار:
 *  ۱) اسکیم اختصاصی برنامه با تعیین بسته (مستقیم خودِ برنامه، بدون انتخاب‌گر و بدون مرورگر)
 *  ۲) همان اسکیم به‌صورت عمومی
 *  ۳) اگر برنامه نصب نبود → لینک وب (که روی گوشی‌های دارای برنامه، خودش برنامه را باز می‌کند)
 *  ۴) اگر هیچ‌کدام نشد → لینک در کلیپ‌بورد کپی و نشان داده می‌شود
 */
private fun openChannel(ctx: Context, ch: Channel, loc: Loc) {
    // ۱ و ۲) خودِ برنامه
    for (pkg in ch.packages) {
        if (tryOpen(ctx, ch.appUri, pkg)) return
    }
    if (tryOpen(ctx, ch.appUri, null)) return
    // ۳) برنامه نصب نبود → لینک وب (اول با تعیین بسته تا اگر برنامه هست، باز هم خودِ برنامه باز شود)
    for (pkg in ch.packages) {
        if (tryOpen(ctx, ch.webUrl, pkg)) return
    }
    if (tryOpen(ctx, ch.webUrl, null)) return
    // ۴) هیچ راهی باز نشد
    val copied = copyLink(ctx, ch)
    Toast.makeText(
        ctx,
        (if (copied) loc["contact_copied"] + " — " else "") + ch.webUrl,
        Toast.LENGTH_LONG
    ).show()
}

private fun tryOpen(ctx: Context, uri: String, pkg: String?): Boolean = try {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    if (pkg != null) intent.setPackage(pkg)   // مستقیم همان برنامه؛ اگر نصب نباشد، خطا می‌دهد و سراغ گزینه‌ی بعدی می‌رویم
    ctx.startActivity(intent)
    true
} catch (_: Exception) {
    false
}

/** لینک کامل را در کلیپ‌بورد می‌گذارد؛ اگر کپی ممکن نبود false برمی‌گرداند */
private fun copyLink(ctx: Context, ch: Channel): Boolean = try {
    val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText("Karbama", ch.webUrl))
    true
} catch (_: Exception) {
    false
}
