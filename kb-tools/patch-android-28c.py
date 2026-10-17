#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""#28c — اندروید: نشان «پسندیده» روی کارت آگهی (هم‌سان با دمو)"""
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


# ---------- JobCard: پارامتر liked + نشان ❤ ----------
edit('ui/components/JobCard.kt', [
    ('''fun JobCard(
    job: Job,
    loc: Loc,
    applied: Boolean,
    onClick: () -> Unit,
    onMediaClick: (Int) -> Unit = {}
) {''',
     '''fun JobCard(
    job: Job,
    loc: Loc,
    applied: Boolean,
    onClick: () -> Unit,
    onMediaClick: (Int) -> Unit = {},
    liked: Boolean = false          // #28
) {'''),
    ('''                    if (applied) {
                        Spacer(Modifier.width(6.dp))
                        Pill(loc["applied"], OkLight, Color(0xFF0E7A44))
                    }''',
     '''                    if (liked) {                       // #28: نشان «پسندیده»
                        Spacer(Modifier.width(6.dp))
                        Pill("❤ " + loc["pasandide"], Color(0xFFFFE3ED), Color(0xFFB3155A))
                    }
                    if (applied) {
                        Spacer(Modifier.width(6.dp))
                        Pill(loc["applied"], OkLight, Color(0xFF0E7A44))
                    }'''),
])

# ---------- HomeContent: likedIds ----------
edit('ui/screens/HomeScreen.kt', [
    ('''    appliedIds: Set<String>,
    loading: Boolean,''',
     '''    appliedIds: Set<String>,
    likedIds: Set<String> = emptySet(),          // #28
    loading: Boolean,'''),
    ('''                    JobCard(
                        job = job,
                        loc = loc,
                        applied = appliedIds.contains(job.id),
                        onClick = { onOpenJob(job) },
                        onMediaClick = { i -> viewer = job.media to i }
                    )''',
     '''                    JobCard(
                        job = job,
                        loc = loc,
                        applied = appliedIds.contains(job.id),
                        onClick = { onOpenJob(job) },
                        onMediaClick = { i -> viewer = job.media to i },
                        liked = likedIds.contains(job.id)          // #28
                    )'''),
])

# ---------- MainShell: وصل‌کردن likedIds ----------
edit('MainActivity.kt', [
    ('''                    appliedIds = vm.appliedIds,
                    loading = loading,''',
     '''                    appliedIds = vm.appliedIds,
                    likedIds = vm.likedIds,                  // #28
                    loading = loading,'''),
])

print('ERRORS:' if errs else 'همه‌ی انکرها پیدا شدند ✅')
for e in errs:
    print(' -', e)
