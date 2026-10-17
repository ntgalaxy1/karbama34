#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""#28 — اندروید: زبان عربی + بخش «پسندیده» + دکمه‌ی پسندیدن + powered by کم‌رنگ‌تر"""
import pathlib, json, re

BASE = pathlib.Path('/home/user/Karbama/app/src/main/java/com/karbama/app')
AR = json.loads(pathlib.Path('/home/user/kb-tools/kt-ar.json').read_text())
errs = []


def edit(rel, pairs, count=1):
    p = BASE / rel
    s = p.read_text()
    n0 = len(s)
    for old, new in pairs:
        c = s.count(old)
        if c != count:
            errs.append(f'{rel}: anchor x{c} (expected {count}) :: {old[:60]!r}')
            continue
        s = s.replace(old, new, count)
    p.write_text(s)
    print(f'{rel}: {n0} -> {len(s)}')


# ═════════════════════════════════════════════════════════════
# ۱) Strings.kt — زبان عربی
# ═════════════════════════════════════════════════════════════
NEW_KEYS = {
    'fa': [("pasandide", "پسندیده"), ("like", "پسندیدم"), ("liked_btn", "پسندیده شد ✓"),
           ("unlike", "برداشتن از پسندیده"), ("liked_empty", "هنوز آگهی‌ای را پسند نکرده‌اید"),
           ("liked_added", "به فهرست پسندیده‌ها اضافه شد ✓"), ("liked_removed", "از فهرست پسندیده‌ها برداشته شد"),
           ("job_page", "صفحه‌ی آگهی"), ("ar", "عربی")],
    'tr': [("pasandide", "Beğendiklerim"), ("like", "Beğendim"), ("liked_btn", "Beğenildi ✓"),
           ("unlike", "Beğeniyi kaldır"), ("liked_empty", "Henüz beğendiğiniz ilan yok"),
           ("liked_added", "Beğendiklerinize eklendi ✓"), ("liked_removed", "Beğendiklerinizden çıkarıldı"),
           ("job_page", "İlan sayfası"), ("ar", "Arapça")],
    'en': [("pasandide", "Liked"), ("like", "Like"), ("liked_btn", "Liked ✓"),
           ("unlike", "Remove from liked"), ("liked_empty", "You have not liked any job yet"),
           ("liked_added", "Added to liked ✓"), ("liked_removed", "Removed from liked"),
           ("job_page", "Job page"), ("ar", "Arabic")],
}
def kt_lines(pairs):
    return '\n'.join(f'    "{k}" to "{v}",' for k, v in pairs).rstrip(',')

end_anchors = {
    'fa': "\n)\n\nprivate val TURKISH = mapOf(",
    'tr': "\n)\n\nprivate val ENGLISH = mapOf(",
    'en': "\n)\n\nprivate val DICTS: Map<String, Map<String, String>> = mapOf(",
}
pairs = []
for lg, anchor in end_anchors.items():
    add = "\n\n    // #28 — بخش «پسندیده» و صفحه‌ی کامل آگهی\n" + kt_lines(NEW_KEYS[lg]) + "\n"
    pairs.append((anchor, add + anchor))
edit('ui/i18n/Strings.kt', pairs)

# نقشه‌ی عربی
p = BASE / 'ui/i18n/Strings.kt'
s = p.read_text()
ar_lines = '\n'.join(f'    "{k}" to "{v}",' for k, v in AR.items()).rstrip(',')
ar_map = f'''

// =====================================================================
// #28 — دیکشنری عربی (Modern Standard Arabic)
// =====================================================================

private val ARABIC = mapOf(
{ar_lines}
)
'''
anchor = "\nprivate val DICTS: Map<String, Map<String, String>> = mapOf("
assert s.count(anchor) == 1
s = s.replace(anchor, ar_map + anchor, 1)
s = s.replace('''    "fa" to PERSIAN,
    "tr" to TURKISH,
    "en" to ENGLISH
)''', '''    "fa" to PERSIAN,
    "tr" to TURKISH,
    "en" to ENGLISH,
    "ar" to ARABIC
)''', 1)
p.write_text(s)
print('ARABIC map + DICTS:', len(s))

edit('ui/i18n/Strings.kt', [
    ('const val LANG_EN = "en"', 'const val LANG_EN = "en"\nconst val LANG_AR = "ar"'),
    ('val SUPPORTED_LANGS = listOf(LANG_FA, LANG_TR, LANG_EN)',
     'val SUPPORTED_LANGS = listOf(LANG_FA, LANG_TR, LANG_EN, LANG_AR)'),
    ('''fun langLabel(code: String): String = when (code) {
    LANG_TR -> "Türkçe"
    LANG_EN -> "English"
    else -> "فارسی"
}''', '''fun langLabel(code: String): String = when (code) {
    LANG_TR -> "Türkçe"
    LANG_EN -> "English"
    LANG_AR -> "العربية"
    else -> "فارسی"
}'''),
    ('''fun langFlag(code: String): String = when (code) {
    LANG_TR -> "🇹🇷"
    LANG_EN -> "🇬🇧"
    else -> "🇮🇷"
}''', '''fun langFlag(code: String): String = when (code) {
    LANG_TR -> "🇹🇷"
    LANG_EN -> "🇬🇧"
    LANG_AR -> "🇸🇦"
    else -> "🇮🇷"
}'''),
    ('    val rtl: Boolean get() = code == "fa"',
     '    val rtl: Boolean get() = code == "fa" || code == "ar"   // #28: عربی هم راست‌به‌چپ'),
    ('    fun digits(text: String): String = if (!rtl) text else buildString {',
     '    fun digits(text: String): String = if (code != LANG_FA) text else buildString {'),
])

# ═════════════════════════════════════════════════════════════
# ۲) AppPrefs — پسندیده‌ها
# ═════════════════════════════════════════════════════════════
edit('data/local/AppPrefs.kt', [
    ('''    // ---------- داده‌های حالت نمایشی ----------''',
     '''    // ---------- #28: آگهی‌های پسندیده ----------

    fun likedIds(): MutableSet<String> =
        (sp.getStringSet(K_LIKED, emptySet()) ?: emptySet()).toMutableSet()

    fun toggleLiked(jobId: String): Boolean {
        val s = likedIds()
        val nowLiked = if (s.contains(jobId)) { s.remove(jobId); false } else { s.add(jobId); true }
        sp.edit().putStringSet(K_LIKED, s).apply()
        return nowLiked
    }

    // ---------- داده‌های حالت نمایشی ----------'''),
    ('        const val K_APPLIED = "applied_jobs"',
     '        const val K_APPLIED = "applied_jobs"\n        const val K_LIKED = "liked_jobs"'),
])

# ═════════════════════════════════════════════════════════════
# ۳) AppViewModel — وضعیت پسندیده
# ═════════════════════════════════════════════════════════════
edit('ui/AppViewModel.kt', [
    ('''    var appliedIds by mutableStateOf(prefs.appliedIds())
        private set''',
     '''    var appliedIds by mutableStateOf(prefs.appliedIds())
        private set

    // #28: آگهی‌های پسندیده‌ی کاربر (روی خود گوشی ذخیره می‌شود)
    var likedIds by mutableStateOf(prefs.likedIds())
        private set'''),
    ('''    fun applyToJob(jobId: String) {
        prefs.addApplied(jobId)
        appliedIds = prefs.appliedIds()
    }''',
     '''    fun applyToJob(jobId: String) {
        prefs.addApplied(jobId)
        appliedIds = prefs.appliedIds()
    }

    /** #28: پسندیدن / برداشتن از پسندیده — خروجی: آیا الان پسندیده است؟ */
    fun toggleLike(jobId: String): Boolean {
        val liked = prefs.toggleLiked(jobId)
        likedIds = prefs.likedIds()
        return liked
    }'''),
])
