package com.karbama.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.karbama.app.data.model.Job
import com.karbama.app.data.model.MediaType
import com.karbama.app.ui.i18n.Loc
import com.karbama.app.ui.theme.*
import com.karbama.app.util.MediaUtil

@Composable
fun JobCard(
    job: Job,
    loc: Loc,
    applied: Boolean,
    onClick: () -> Unit,
    onMediaClick: (Int) -> Unit = {},
    liked: Boolean = false          // #28
) {
    val isNew = System.currentTimeMillis() - job.publishedAt < 24L * 3600_000L

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.extraLarge,
        color = GlassFill,
        border = BorderStroke(1.dp, GlassStroke),
        shadowElevation = 9.dp
    ) {
        Box {
            if (isNew) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .clip(RoundedCornerShape(bottomStart = 12.dp))
                        .background(Brush.linearGradient(listOf(Coral, Coral)))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        "NEW",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }
            Column(Modifier.padding(16.dp)) {
                // ---------- کاور عکس/ویدیوی آگهی (بالای کارت) ----------
                if (job.media.isNotEmpty()) {
                    val first = job.media.firstOrNull { it.type == MediaType.IMAGE } ?: job.media.first()
                    val photos = job.media.count { it.type == MediaType.IMAGE }
                    val videos = job.media.count { it.type == MediaType.VIDEO }
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(158.dp)
                            .padding(bottom = 12.dp)
                            .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                            .background(Line)
                            .clickable { onMediaClick(job.media.indexOf(first).coerceAtLeast(0)) }
                    ) {
                        AsyncImage(
                            model = MediaUtil.previewAddress(first),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        if (first.type == MediaType.VIDEO) {
                            Box(
                                Modifier
                                    .align(Alignment.Center)
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.PlayArrow,
                                    contentDescription = loc["media_video"],
                                    tint = Color.White,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }
                        Surface(
                            color = GlassBar,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, GlassStroke),
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(8.dp)
                        ) {
                            Text(
                                "🖼 " + loc.n(photos) + "   🎬 " + loc.n(videos),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = Ink,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(BrandBrush),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(loc.jobIcon(job.titleKey), fontSize = 20.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = job.titleKey.takeIf { it.isNotEmpty() }
                                ?.let { loc.jobTitle(it) } ?: job.titleCustom,
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "🏢 ${job.company}",
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (liked) {                       // #28: نشان «پسندیده»
                        Spacer(Modifier.width(8.dp))
                        Pill("❤ " + loc["pasandide"], PinkLight, Pink)
                    }
                    if (applied) {
                        Spacer(Modifier.width(8.dp))
                        Pill(loc["applied"], OkLight, OkDeep)
                    }
                }

                Spacer(Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TagChip("📍 " + listOf(job.city, job.district).filter { it.isNotEmpty() }.joinToString(" / "))
                    TagChip("🕒 " + loc.contract(job.contract))
                    job.conditions.take(1).forEach { TagChip(loc.condition(it), tone = TagTone.Plain) }
                }

                Spacer(Modifier.height(12.dp))
                HorizontalDivider(thickness = 1.dp, color = Line)
                Spacer(Modifier.height(12.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "💵 " + loc.money(job.salary),
                        style = MaterialTheme.typography.titleSmall,
                        color = BrandDark
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Schedule,
                            contentDescription = null,
                            tint = Ink3,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(loc.timeAgo(job.publishedAt), style = MaterialTheme.typography.bodySmall)
                    }
                }

                // ---------- عکس‌ها و ویدیوهای آگهی (اگر کاور بالا آمده، تکرار نمی‌شود) ----------
                if (job.media.size > 3) {
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        job.media.take(3).forEachIndexed { i, m ->
                            MediaThumb(
                                m = m,
                                loc = loc,
                                size = 58.dp,
                                modifier = Modifier.padding(end = 8.dp),
                                onClick = { onMediaClick(i) }
                            )
                        }
                        if (job.media.size > 3) {
                            Box(
                                Modifier
                                    .size(58.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(FieldBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "+" + loc.n(job.media.size - 3),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Ink2
                                )
                            }
                        }
                        Spacer(Modifier.weight(1f))
                        Text(
                            loc.f(
                                "media_counts",
                                loc.n(job.media.count { it.type == MediaType.IMAGE }),
                                loc.n(job.media.count { it.type == MediaType.VIDEO })
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = Ink3
                        )
                    }
                }
            }
        }
    }
}

enum class TagTone { Green, Purple, Plain }

@Composable
fun TagChip(text: String, tone: TagTone = TagTone.Green) {
    val (bg, fg) = when (tone) {
        TagTone.Green -> Brush.linearGradient(listOf(MintLight, MintLight)) to BrandDark
        TagTone.Purple -> Brush.linearGradient(listOf(VioletLight, VioletLight)) to VioletDeep
        TagTone.Plain -> Brush.linearGradient(listOf(Surface, Line)) to Ink2
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(9.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = fg, maxLines = 1)
    }
}

@Composable
fun LocationTag(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Filled.LocationOn, contentDescription = null, tint = BrandDark, modifier = Modifier.size(13.dp))
        Spacer(Modifier.width(4.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = BrandDark)
    }
}
