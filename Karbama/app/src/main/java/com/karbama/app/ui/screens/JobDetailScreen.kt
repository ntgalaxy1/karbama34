package com.karbama.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karbama.app.data.model.Job
import com.karbama.app.ui.components.*
import com.karbama.app.ui.i18n.Loc
import com.karbama.app.ui.theme.*

@Composable
fun JobDetailScreen(
    job: Job,
    loc: Loc,
    applied: Boolean,
    canApply: Boolean,
    liked: Boolean = false,          // #28
    onLike: () -> Unit = {},         // #28
    onApply: () -> Unit,
    onBack: () -> Unit
) {
    SystemBars(darkIcons = true)

    // عکس/ویدیوی بزرگ‌شده
    var viewerIndex by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        containerColor = Bg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = { TopBar(title = loc["job_page"], onBack = onBack) },   // #28
        bottomBar = {
            Surface(color = GlassBar, shadowElevation = 10.dp) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp)
                ) {
                    if (!canApply) {
                        Text(
                            loc["apply_login_first"],
                            style = MaterialTheme.typography.bodySmall,
                            color = Danger,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    PrimaryButton(
                        text = if (applied) loc["applied"] else loc["apply"],
                        icon = Icons.Filled.Send,
                        enabled = !applied,
                        onClick = onApply
                    )
                    Spacer(Modifier.height(8.dp))
                    // #28: پسندیدن آگهی — دکمه‌ی مستقل، متناسب با حالت
                    GhostButton(
                        text = if (liked) loc["unlike"] else loc["like"],
                        icon = if (liked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        onClick = onLike
                    )
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
            // ---------- هدر آگهی ----------
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = GlassFill,
                border = BorderStroke(1.dp, GlassStroke),
                shadowElevation = 8.dp
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.Top) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(BrandBrush),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(loc.jobIcon(job.titleKey), fontSize = 24.sp)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                job.titleKey.takeIf { it.isNotEmpty() }
                                    ?.let { loc.jobTitle(it) } ?: job.titleCustom,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.Business,
                                    contentDescription = null,
                                    tint = Ink3,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(job.company, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TagChip(
                            "📍 " + listOf(job.city, job.district)
                                .filter { it.isNotEmpty() }.joinToString(" / ")
                        )
                        TagChip("🕒 " + loc.contract(job.contract), TagTone.Purple)
                        if (liked) TagChip("❤ " + loc["pasandide"])   // #28
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ---------- سه کارت خلاصه ----------
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MiniStat(loc.money(job.salary), loc["salary"], Modifier.weight(1f))
                MiniStat(job.district.ifEmpty { job.city }, loc["district_label"], Modifier.weight(1f))
                MiniStat(loc.timeAgo(job.publishedAt), loc["published"], Modifier.weight(1f))
            }

            // ---------- عکس‌ها و ویدیوهای آگهی ----------
            if (job.media.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                SectionCard {
                    SectionTitle(
                        loc["media_title"] + " (" + loc.n(job.media.size) + ")"
                    )
                    Spacer(Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        itemsIndexed(job.media) { i, m ->
                            MediaThumb(
                                m = m,
                                loc = loc,
                                size = 96.dp,
                                onClick = { viewerIndex = i }
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        loc["media_hint"],
                        style = MaterialTheme.typography.bodySmall,
                        color = Ink3
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ---------- توضیحات ----------
            SectionCard {
                SectionTitle(loc["description"])
                Spacer(Modifier.height(8.dp))
                Text(
                    job.description.ifBlank { "—" },
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 26.sp,
                    color = Ink2
                )
            }

            Spacer(Modifier.height(12.dp))

            // ---------- شرایط و مهارت‌ها ----------
            if (job.requirements.isNotEmpty()) {
                SectionCard {
                    SectionTitle(loc["requirements"])
                    Spacer(Modifier.height(8.dp))
                    job.requirements.forEach { r ->
                        Row(
                            Modifier.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                Modifier
                                    .padding(top = 8.dp)
                                    .size(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Brand)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                r,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Ink2,
                                lineHeight = 24.sp
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            // ---------- مزایا ----------
            if (job.conditions.isNotEmpty()) {
                SectionCard {
                    SectionTitle(loc["conditions"])
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        job.conditions.forEach { TagChip(loc.condition(it)) }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            // ---------- تماس ----------
            SectionCard {
                SectionTitle(loc["contact"])
                Spacer(Modifier.height(4.dp))
                InfoRow(loc["employer"], job.company)
                HorizontalDivider(color = Line)
                InfoRow(
                    loc["phone_label"],
                    job.contactPhone.ifEmpty { "+90 212 000 00 00" }
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
    viewerIndex?.let { idx ->
        MediaViewer(
            items = job.media,
            startIndex = idx,
            loc = loc,
            onClose = { viewerIndex = null }
        )
    }

}

@Composable
private fun MiniStat(value: String, label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = CardBg,
        shadowElevation = 1.dp
    ) {
        Column(
            Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                value,
                style = MaterialTheme.typography.titleSmall,
                color = BrandDark,
                maxLines = 1
            )
            Spacer(Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall)
        }
    }
}
