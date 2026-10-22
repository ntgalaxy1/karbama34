package com.karbama.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import com.karbama.app.data.model.Applicant
import com.karbama.app.data.model.Contract
import com.karbama.app.data.model.Job
import com.karbama.app.data.model.JobFilter
import com.karbama.app.data.model.JobMedia
import com.karbama.app.data.sample.Catalog
import com.karbama.app.ui.components.*
import com.karbama.app.ui.i18n.Loc
import com.karbama.app.ui.theme.*

@Composable
fun HomeContent(
    loc: Loc,
    userName: String,
    jobs: List<Job>,
    filter: JobFilter,
    appliedIds: Set<String>,
    likedIds: Set<String> = emptySet(),          // #28
    me: Applicant? = null,                       // #35: کارجوی وارد‌شده (وضعیت پرداخت)
    adsLocked: Boolean = false,                  // #35: بخش آگهی‌ها تا پرداخت پوشیده است
    adsFee: Long = 500L,                        // #35
    brokerCard: String = "",                     // #35
    onGoPay: () -> Unit = {},                    // #35: رفتن به صفحه‌ی پرداخت
    loading: Boolean,
    onFilterChange: (JobFilter) -> Unit,
    onOpenJob: (Job) -> Unit
) {
    SystemBars(darkIcons = false)

    // عکس/ویدیوی بزرگ‌شده (تمام‌صفحه)
    var viewer by remember { mutableStateOf<Pair<List<JobMedia>, Int>?>(null) }

    val districts = Catalog.districtsOf(filter.city)

    Column(Modifier.fillMaxSize().background(Bg)) {
        // ---------------- هدر ----------------
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 26.dp, bottomEnd = 26.dp))
                .background(BrandBrush)
                .statusBarsPadding()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BrandLogo(size = 40.dp, onDark = true, modifier = Modifier.size(40.dp))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        loc["jobs_title"],
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    if (userName.isNotBlank()) {
                        Text(
                            "${loc["hello"]} $userName 👋",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = filter.query,
                onValueChange = { onFilterChange(filter.copy(query = it)) },
                placeholder = {
                    Text(loc["search_jobs_hint"], color = Ink3, fontSize = 14.sp)
                },
                leadingIcon = {
                    Text("🔎", fontSize = 16.sp)
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = CardBg,
                    unfocusedContainerColor = CardBg,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = Brand
                )
            )
        }

        // ---------------- فیلتر شهرها ----------------
        HorizontalChipRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardBg)
                .padding(vertical = 8.dp, horizontal = 12.dp)
        ) {
            SelectChip(
                text = "🇹🇷 " + loc["all_cities"],
                selected = filter.city.isEmpty(),
                onClick = { onFilterChange(filter.copy(city = "", district = "")) }
            )
            Catalog.CITIES.forEach { c ->
                SelectChip(
                    text = c,
                    selected = filter.city == c,
                    onClick = { onFilterChange(filter.copy(city = c, district = "")) }
                )
            }
        }

        // ---------------- فیلتر مناطق + ترتیب ----------------
        Column(
            Modifier
                .fillMaxWidth()
                .background(CardBg)
                .padding(bottom = 8.dp)
        ) {
            if (districts.isNotEmpty()) {
                HorizontalChipRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                ) {
                    SelectChip(
                        text = loc["all_districts"],
                        selected = filter.district.isEmpty(),
                        onClick = { onFilterChange(filter.copy(district = "")) }
                    )
                    districts.forEach { d ->
                        SelectChip(
                            text = "📍 $d",
                            selected = filter.district == d,
                            onClick = { onFilterChange(filter.copy(district = d)) }
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
            HorizontalChipRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            ) {
                SelectChip(
                    text = "🆕 " + loc["sort_newest"],
                    selected = filter.sort == "new",
                    onClick = { onFilterChange(filter.copy(sort = "new")) }
                )
                SelectChip(
                    text = "💰 " + loc["sort_salary"],
                    selected = filter.sort == "salary",
                    onClick = { onFilterChange(filter.copy(sort = "salary")) }
                )
                SelectChip(
                    text = "🪙 " + loc["sort_salary_low"],
                    selected = filter.sort == "salaryLow",
                    onClick = { onFilterChange(filter.copy(sort = "salaryLow")) }
                )
                Contract.all.forEach { c ->
                    SelectChip(
                        text = loc.contract(c),
                        selected = filter.contract == c,
                        onClick = {
                            onFilterChange(filter.copy(contract = if (filter.contract == c) "" else c))
                        }
                    )
                }
            }
        }

        // ---------------- فهرست ----------------
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                loc.f("jobs_found", loc.n(jobs.size)),
                style = MaterialTheme.typography.bodySmall,
                color = Ink3
            )
            Spacer(Modifier.weight(1f))
            if (filter.query.isNotBlank() || filter.city.isNotBlank() ||
                filter.district.isNotBlank() || filter.contract.isNotBlank()
            ) {
                TextButton(onClick = { onFilterChange(JobFilter()) }) {
                    Text(
                        "✕ " + loc["clear_filters"],
                        style = MaterialTheme.typography.bodySmall,
                        color = BrandDark,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (adsLocked) {
            // #35: صفحه همان صفحه است؛ فقط جای فهرست آگهی‌ها → پنل «نیاز به پرداخت»
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                AdsLockPanel(
                    loc = loc,
                    applicant = me,
                    adsFee = adsFee,
                    brokerCard = brokerCard,
                    jobsCount = jobs.size,
                    onGoPay = onGoPay
                )
            }
        } else if (loading) {
            LoadingBox(loc["loading"])
        } else if (jobs.isEmpty()) {
            EmptyState(
                emoji = "🔍",
                title = loc["no_jobs"],
                subtitle = loc["no_jobs_sub"],
                actionLabel = loc["clear_filters"],
                onAction = { onFilterChange(JobFilter()) }
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(jobs, key = { it.id }) { job ->
                    JobCard(
                        job = job,
                        loc = loc,
                        applied = appliedIds.contains(job.id),
                        onClick = { onOpenJob(job) },
                        onMediaClick = { i -> viewer = job.media to i },
                        liked = likedIds.contains(job.id)          // #28
                    )
                }
            }
            Text(
                loc["demo_data_note"],
                style = MaterialTheme.typography.bodySmall,
                color = Ink3,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
    // ---------- نمایش تمام‌صفحه‌ی عکس/ویدیوی آگهی ----------
    viewer?.let { (items, start) ->
        MediaViewer(
            items = items,
            startIndex = start,
            loc = loc,
            onClose = { viewer = null }
        )
    }

}

/** چرخه‌ی ساده برای بخش خطای بارگذاری */
@Composable
fun LoadErrorBox(text: String, retryLabel: String, onRetry: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(DangerLight),
            contentAlignment = Alignment.Center
        ) {
            Text("🔄", fontSize = 22.sp)
        }
        Spacer(Modifier.height(12.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = Ink2)
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            shape = MaterialTheme.shapes.small,
            colors = ButtonDefaults.buttonColors(containerColor = Brand)
        ) { Text(retryLabel) }
    }
}

/** دکمه‌ی کوچک کنار عنوان‌ها */
@Composable
fun MiniTag(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(BrandLight)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelSmall, color = BrandDark)
    }
}
