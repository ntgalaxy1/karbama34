package com.karbama.app.ui.components

import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.karbama.app.ui.theme.Ink
import com.karbama.app.ui.theme.Line
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.karbama.app.data.model.JobMedia
import com.karbama.app.data.model.MediaType
import com.karbama.app.ui.i18n.Loc
import com.karbama.app.util.MediaUtil

/**
 * نمایش تمام‌صفحه‌ی عکس‌ها و ویدیوهای یک آگهی.
 * روی عکس‌ها می‌توان با دو انگشت بزرگ‌نمایی کرد (و با دو ضربه)، ویدیو هم همان‌جا پخش می‌شود.
 */
@Composable
fun MediaViewer(
    items: List<JobMedia>,
    startIndex: Int,
    loc: Loc,
    onClose: () -> Unit
) {
    if (items.isEmpty()) return
    val safeDoc = items.size
    val pagerState = rememberPagerState(
        initialPage = startIndex.coerceIn(0, safeDoc - 1)
    ) { safeDoc }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Ink)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val m = items[page]
                if (m.type == MediaType.VIDEO) {
                    VideoPane(m, loc)
                } else {
                    ImagePane(m, onClose)
                }
            }

            // ---------- نوار بالا: بستن + شماره‌ی رسانه ----------
            Row(
                Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.16f))
                        .clickable { onClose() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Close, contentDescription = null, tint = Color.White)
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = loc["media_title"],
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    color = Color.White.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = loc.f("media_index", loc.n(pagerState.currentPage + 1), loc.n(items.size)),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }

            // ---------- راهنمای پایین ----------
            val current = items.getOrNull(pagerState.currentPage)
            if (current != null) {
                Text(
                    text = if (current.type == MediaType.VIDEO) loc["media_video_hint"] else loc["media_zoom_hint"],
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.75f),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(16.dp)
                )
            }
        }
    }
}

/** عکس با بزرگ‌نمایی (دو انگشت یا دو ضربه) و جابه‌جایی */
@Composable
private fun ImagePane(m: JobMedia, onClose: () -> Unit) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(
        Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(1f, 6f)
                    offset = if (scale <= 1.02f) Offset.Zero else {
                        Offset(
                            x = (offset.x + pan.x).coerceIn(-3000f, 3000f),
                            y = (offset.y + pan.y).coerceIn(-3000f, 3000f)
                        )
                    }
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        if (scale > 1.5f) {
                            scale = 1f; offset = Offset.Zero
                        } else {
                            scale = 2.5f
                        }
                    },
                    onTap = { onClose() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = MediaUtil.mediaAddress(m.url),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                }
        )
    }
}

/** ویدیو با پخش‌کننده‌ی خودِ اندروید (بدون کتابخانه‌ی اضافه) */
@Composable
private fun VideoPane(m: JobMedia, loc: Loc) {
    val ctx = LocalContext.current
    // v48: لینک صفحه‌ی ویدیو (یوتیوب، آپارات و …) داخل VideoView پخش نمی‌شود → باز کردن در برنامه/مرورگر
    val url = m.url.trim()
    val isWebPage = (url.startsWith("http://") || url.startsWith("https://")) &&
        !Regex("""\.(mp4|webm|3gp|mkv|m3u8)(\?.*)?$""", RegexOption.IGNORE_CASE).containsMatchIn(url)
    if (isWebPage) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                modifier = Modifier.clickable {
                    runCatching {
                        ctx.startActivity(
                            android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url))
                                .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    }
                }
            ) {
                Text(
                    "▶  " + loc["media_open_video"],
                    color = Ink,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp)
                )
            }
        }
        return
    }
    AndroidView(
        factory = { c ->
            VideoView(c).apply {
                setVideoURI(Uri.parse(MediaUtil.mediaAddress(m.url)))
                setOnPreparedListener { player ->
                    player.isLooping = true
                    val mc = MediaController(c)
                    mc.setAnchorView(this)
                    setMediaController(mc)
                    start()
                }
            }
        },
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 40.dp)
    )
}

/**
 * بندانگشتی یک رسانه — هم در فهرست آگهی‌ها و هم در برگه‌ی آگهی استفاده می‌شود.
 * برای ویدیو، تصویر پیش‌نمایش (پوستر) با نشان ▶ نشان داده می‌شود.
 */
@Composable
fun MediaThumb(
    m: JobMedia,
    loc: Loc,
    size: Dp,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(Line)
            .clickable { onClick() }
    ) {
        AsyncImage(
            model = MediaUtil.previewAddress(m),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        if (m.type == MediaType.VIDEO) {
            Box(
                Modifier
                    .align(Alignment.Center)
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.45f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.PlayArrow,
                    contentDescription = loc["media_video"],
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
