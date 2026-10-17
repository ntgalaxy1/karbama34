#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""#28b — اندروید: دکمه‌ی پسندیدن در صفحه‌ی آگهی + بخش «پسندیده» در پروفایل + powered by کم‌رنگ‌تر"""
import pathlib

BASE = pathlib.Path('/home/user/Karbama/app/src/main/java/com/karbama/app')
errs = []


def edit(rel, pairs, count=1):
    p = BASE / rel
    s = p.read_text()
    n0 = len(s)
    for old, new in pairs:
        c = s.count(old)
        if c != count:
            errs.append(f'{rel}: anchor x{c} :: {old[:70]!r}')
            continue
        s = s.replace(old, new, count)
    p.write_text(s)
    print(f'{rel}: {n0} -> {len(s)}')


# ═════════════════════════════════════════════════════════════
# ۱) JobDetailScreen: دکمه‌ی «پسندیدم» + عنوان «صفحه‌ی آگهی»
# ═════════════════════════════════════════════════════════════
edit('ui/screens/JobDetailScreen.kt', [
    ('import androidx.compose.material.icons.filled.Business',
     'import androidx.compose.material.icons.filled.Business\n'
     'import androidx.compose.material.icons.filled.Favorite\n'
     'import androidx.compose.material.icons.filled.FavoriteBorder'),
    ('''fun JobDetailScreen(
    job: Job,
    loc: Loc,
    applied: Boolean,
    canApply: Boolean,
    onApply: () -> Unit,
    onBack: () -> Unit
) {''',
     '''fun JobDetailScreen(
    job: Job,
    loc: Loc,
    applied: Boolean,
    canApply: Boolean,
    liked: Boolean = false,          // #28
    onLike: () -> Unit = {},         // #28
    onApply: () -> Unit,
    onBack: () -> Unit
) {'''),
    ('topBar = { TopBar(title = loc["description"], onBack = onBack) },',
     'topBar = { TopBar(title = loc["job_page"], onBack = onBack) },   // #28'),
    ('''                    PrimaryButton(
                        text = if (applied) loc["applied"] else loc["apply"],
                        icon = Icons.Filled.Send,
                        enabled = !applied,
                        onClick = onApply
                    )''',
     '''                    PrimaryButton(
                        text = if (applied) loc["applied"] else loc["apply"],
                        icon = Icons.Filled.Send,
                        enabled = !applied,
                        onClick = onApply
                    )
                    Spacer(Modifier.height(10.dp))
                    // #28: پسندیدن آگهی — دکمه‌ی مستقل، متناسب با حالت
                    GhostButton(
                        text = if (liked) loc["unlike"] else loc["like"],
                        icon = if (liked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        onClick = onLike
                    )'''),
    ('''                        TagChip("🕒 " + loc.contract(job.contract), TagTone.Purple)''',
     '''                        TagChip("🕒 " + loc.contract(job.contract), TagTone.Purple)
                        if (liked) TagChip("❤ " + loc["pasandide"])   // #28'''),
])

# ═════════════════════════════════════════════════════════════
# ۲) ProfileScreen: بخش «پسندیده»
# ═════════════════════════════════════════════════════════════
edit('ui/screens/ProfileScreen.kt', [
    ('import androidx.compose.material.icons.filled.Security',
     'import androidx.compose.material.icons.filled.Favorite\n'
     'import androidx.compose.material.icons.filled.Security'),
    ('import androidx.compose.ui.text.style.TextAlign',
     'import androidx.compose.ui.text.style.TextAlign\nimport androidx.compose.ui.text.style.TextOverflow'),
    ('import com.karbama.app.data.model.Applicant',
     'import com.karbama.app.data.model.Applicant\nimport com.karbama.app.data.model.Job'),
    ('''    onEdit: () -> Unit,
    onLanguage: (String) -> Unit,
    onLogout: () -> Unit
) {''',
     '''    onEdit: () -> Unit,
    onLanguage: (String) -> Unit,
    onLogout: () -> Unit,
    likedJobs: List<Job> = emptyList(),          // #28
    onOpenJob: (Job) -> Unit = {},               // #28
    onUnlike: (Job) -> Unit = {}                 // #28
) {'''),
    ('''            // ---------------- تنظیمات ----------------''',
     '''            // ---------------- #28: آگهی‌های پسندیده ----------------
            SectionCard {
                SectionTitle("❤ " + loc["pasandide"] + " (" + likedJobs.size + ")")
                Spacer(Modifier.height(10.dp))
                if (likedJobs.isEmpty()) {
                    Text(
                        loc["liked_empty"],
                        style = MaterialTheme.typography.bodySmall,
                        color = Ink3
                    )
                } else {
                    likedJobs.forEach { job ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.medium)
                                .clickable { onOpenJob(job) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(13.dp))
                                    .background(BrandLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(loc.jobIcon(job.titleKey), fontSize = 18.sp)
                            }
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    job.titleKey.takeIf { it.isNotEmpty() }
                                        ?.let { loc.jobTitle(it) } ?: job.titleCustom,
                                    style = MaterialTheme.typography.titleSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    job.company + " · " + listOf(job.city, job.district)
                                        .filter { it.isNotEmpty() }.joinToString(" / "),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Ink3,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    loc.money(job.salary),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = BrandDark
                                )
                            }
                            IconButton(onClick = { onUnlike(job) }) {
                                Icon(
                                    Icons.Filled.Favorite,
                                    contentDescription = loc["unlike"],
                                    tint = Color(0xFFC2185B)
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
            }

            Spacer(Modifier.height(16.dp))

            // ---------------- تنظیمات ----------------'''),
])

# ═════════════════════════════════════════════════════════════
# ۳) MainActivity: وصل‌کردن پسندیده‌ها
# ═════════════════════════════════════════════════════════════
edit('MainActivity.kt', [
    ('''                        JobDetailScreen(
                            job = job,
                            loc = loc,
                            applied = vm.appliedIds.contains(job.id),
                            canApply = vm.isRegistered,
                            onApply = { vm.applyToJob(job.id) },
                            onBack = { nav.popBackStack() }
                        )''',
     '''                        JobDetailScreen(
                            job = job,
                            loc = loc,
                            applied = vm.appliedIds.contains(job.id),
                            canApply = vm.isRegistered,
                            liked = vm.likedIds.contains(job.id),                  // #28
                            onLike = { vm.toggleLike(job.id) },                    // #28
                            onApply = { vm.applyToJob(job.id) },
                            onBack = { nav.popBackStack() }
                        )'''),
    ('''                    onLanguage = { vm.applyLang(it) },
                    onLogout = {
                        vm.logout()
                        tab = 0
                        onLoggedOut()
                    }
                )''',
     '''                    onLanguage = { vm.applyLang(it) },
                    onLogout = {
                        vm.logout()
                        tab = 0
                        onLoggedOut()
                    },
                    // #28: آگهی‌های پسندیده‌ی همین کاربر
                    likedJobs = jobs.filter { vm.likedIds.contains(it.id) },
                    onOpenJob = onOpenJob,
                    onUnlike = { vm.toggleLike(it.id) }
                )'''),
])

# ═════════════════════════════════════════════════════════════
# ۴) powered by — کم‌رنگ‌تر و کم‌تر
# ═════════════════════════════════════════════════════════════
edit('ui/screens/StartScreens.kt', [
    ('''            Text(
                text = "powered by naser tavakkoli @nt.galaxy",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.7f),
                letterSpacing = 0.4.sp,
                textAlign = TextAlign.Center
            )''',
     '''            Text(   // #28: کم‌رنگ‌تر و کم‌تر
                text = "powered by naser tavakkoli @nt.galaxy",
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.42f),
                letterSpacing = 0.3.sp,
                textAlign = TextAlign.Center
            )'''),
])

edit('ui/screens/ContactScreen.kt', [
    ('''                Text(
                    text = "powered by naser tavakkoli @nt.galaxy",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink2,
                    letterSpacing = 0.4.sp,''',
     '''                Text(   // #28: کم‌رنگ‌تر و کم‌تر
                    text = "powered by naser tavakkoli @nt.galaxy",
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Normal,
                    color = Ink3.copy(alpha = 0.72f),
                    letterSpacing = 0.3.sp,'''),
])

print('ERRORS:' if errs else 'همه‌ی انکرها پیدا شدند ✅')
for e in errs:
    print(' -', e)
