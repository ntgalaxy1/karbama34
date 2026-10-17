#!/usr/bin/env python3
"""پاس دیزاین اپ اندروید (#33) — رنگ‌های ثابت → توکن‌های تم، مقیاس تایپ، شبکه‌ی فاصله.
dry-run:  python3 kb-tools/design-pass-android.py
اعمال:    python3 kb-tools/design-pass-android.py --apply
"""
import re, sys, pathlib, collections

BASE = pathlib.Path('/home/user/Karbama/app/src/main/java/com/karbama/app')
APPLY = '--apply' in sys.argv

# ---------- پالت یک‌دست (همان دمو) ----------
MAP = {
    '0xFF0F1F2B':'0xFF0F1F2B', '0xFF0B1D28':'0xFF0F1F2B', '0xFF0E2430':'0xFF0F1F2B',
    '0xFF0B2A37':'0xFF0F1F2B', '0xFF05090C':'0xFF0F1F2B', '0xFF3F5566':'0xFF3F5566',
    '0xFF33525F':'0xFF3F5566', '0xFF123040':'0xFF3F5566', '0xFF8496A6':'0xFF5F7587',
    '0xFF7B8C97':'0xFF5F7587', '0xFF9DB0BA':'0xFF5F7587', '0xFF63798A':'0xFF5F7587',
    '0xFF8FA3B1':'0xFF5F7587', '0xFF8A9BA9':'0xFF5F7587',
    '0xFFE4EDF2':'0xFFE3ECF0', '0xFFE3ECF0':'0xFFE3ECF0', '0xFFDFE7EC':'0xFFE3ECF0',
    '0xFFD6E0E6':'0xFFE3ECF0', '0xFFD3E0E6':'0xFFE3ECF0', '0xFFCFDDE4':'0xFFE3ECF0',
    '0xFFDCE6EC':'0xFFE3ECF0', '0xFFE9F1F6':'0xFFE3ECF0', '0xFFD7E0E5':'0xFFE3ECF0',
    '0xFFCBD8E0':'0xFFE3ECF0', '0xFFA7C3CD':'0xFFE3ECF0', '0xFFB3C8D2':'0xFFE3ECF0',
    '0xFFF8FBFC':'0xFFF8FBFC', '0xFFFAFDFE':'0xFFF6FAFB', '0xFFEEF7F8':'0xFFF6FAFB',
    '0xFFF6FBFC':'0xFFF6FAFB', '0xFFECF5FA':'0xFFF6FAFB', '0xFFF5FAFC':'0xFFF6FAFB',
    '0xFFE8F0F7':'0xFFF6FAFB', '0xFFF1F3FB':'0xFFEEEBFF',
    '0xFF0E9F8E':'0xFF0E9F8E', '0xFF0EA394':'0xFF0E9F8E', '0xFF066F6B':'0xFF066F6B',
    '0xFF0A7F91':'0xFF0A7F91', '0xFF19CBAD':'0xFF19CBAD', '0xFF10B39F':'0xFF10B39F',
    '0xFF2BD9BC':'0xFF19CBAD', '0xFFE4F7F4':'0xFFE6F7F4', '0xFFE6F7F5':'0xFFE6F7F4',
    '0xFFDCF7F0':'0xFFE6F7F4', '0xFFCBF0E6':'0xFFE2FBF3', '0xFFE2FBF3':'0xFFE2FBF3',
    '0xFF2FD6A8':'0xFF2FD6A8', '0xFF3FE3C6':'0xFF2FD6A8', '0xFF0A3D4D':'0xFF066F6B',
    '0xFF7C5CFF':'0xFF7C5CFF', '0xFF6E56CF':'0xFF7C5CFF', '0xFF5B3FE0':'0xFF5B3FE0',
    '0xFF4B31C7':'0xFF5B3FE0', '0xFFEEEBFF':'0xFFEEEBFF', '0xFFE1DAFF':'0xFFEEEBFF',
    '0xFFF1EEFB':'0xFFEEEBFF', '0xFFFF7A59':'0xFFFF7A59', '0xFFFFA07A':'0xFFFF7A59',
    '0xFFE4562F':'0xFFE4562F', '0xFFFFEDE7':'0xFFFFEDE7',
    '0xFFFFB020':'0xFFFFB020', '0xFFF59E0B':'0xFFF59E0B', '0xFFFFD15C':'0xFFFDE68A',
    '0xFFF0CF8A':'0xFFFDE68A', '0xFFFFF4DE':'0xFFFFF4DE', '0xFFFEF3C7':'0xFFFFF4DE',
    '0xFF7A5310':'0xFF7A5310', '0xFF8A5B00':'0xFF8A5B00', '0xFFB47A05':'0xFF8A5B00',
    '0xFF3D2606':'0xFF5A3D06', '0xFF5C3D07':'0xFF5A3D06', '0xFF432C04':'0xFF5A3D06',
    '0xFF12A150':'0xFF12A150', '0xFF0E7A44':'0xFF0E7A44', '0xFF1FA855':'0xFF0E7A44',
    '0xFFE4F7EC':'0xFFE4F7EC',
    '0xFFE5484D':'0xFFE5484D', '0xFFFEF0F0':'0xFFFEF0F0',
    '0xFFC2185B':'0xFFC2185B', '0xFFB3155A':'0xFFC2185B', '0xFFFFE3ED':'0xFFFFE3ED',
    '0xFF1D6FD4':'0xFF1D6FD4', '0xFFE7F2FF':'0xFFE7F2FF',
    '0xFF3B4753':'0xFF3B4753', '0xFFEFF3F5':'0xFFEFF3F5',
    '0xFF101C24':'0xFF101C24', '0xFF2B3D48':'0xFF2B3D48',
    '0xFFC6D5DE':'0xFFC6D5DE', '0xFFD9E4EA':'0xFFD9E4EA',
    # لوگوی برندهای بیرونی — دست‌نخورده
    '0xFF229ED9':'0xFF229ED9', '0xFF2AABEE':'0xFF2AABEE', '0xFFDD2A7B':'0xFFDD2A7B',
    '0xFF3ED36F':'0xFF3ED36F', '0xFF25D366':'0xFF25D366', '0xFFF58529':'0xFFF58529',
    '0xFF8134AF':'0xFF8134AF',
    # شیشه‌ای‌ها (آلفا حفظ می‌شود، آلفا یک‌دست)
    '0xC7FFFFFF':'0xCCFFFFFF', '0x99FFFFFF':'0x99FFFFFF', '0xB3FFFFFF':'0xB3FFFFFF',
    '0xE6FFFFFF':'0xE6FFFFFF', '0xC7FFFFFFF':'0xCCFFFFFF',
    '0x3810B39F':'0x380E9F8E', '0x00000000':'0x00000000',
}
ALPHA_SNAP = {'0xC7':'0xCC', '0xE0':'0xE6', '0xF0':'0xF2'}

TYPE = {9.5:11, 10:11, 10.5:11, 10.6:11, 11:11, 12:12, 12.4:12, 12.6:13,
        13:13, 13.5:14, 14:14, 14.5:14, 15:15, 16:16, 16.5:16, 17:18,
        18:18, 19:20, 20:20, 21:20, 22:22, 24:24, 26:26, 27:26, 40:38}
DP = {2.5:2, 3:4, 5:4, 7:8, 9:8, 11:12, 13:12, 15:16, 17:16, 19:20, 21:20,
      23:24, 25:24, 27:28, 29:28, 31:32}

report = collections.Counter()
unknown = collections.Counter()

for f in sorted(BASE.rglob('*.kt')):
    src = f.read_text()
    out = src

    # ---------- ۱) رنگ‌ها ----------
    def fix_color(m):
        whole = m.group(0)
        hexv = m.group(1)[2:].upper()          # AARRGGBB
        alpha, rgb = hexv[:2], '0xFF' + hexv[2:]
        new = MAP.get(rgb)
        if new is None:
            unknown[rgb] += 1; report['color:keep'] += 1
            return whole
        a = ALPHA_SNAP.get(alpha, alpha)
        if new != rgb or a != alpha:
            report['color:changed'] += 1
        return f'Color(0x{a}{new[4:]})'
    out = re.sub(r'Color\((0x[0-9A-Fa-f]{8})\)', fix_color, out)

    # ---------- ۲) اندازه‌ی فونت ----------
    def fix_font(m):
        v = float(m.group(1)); new = TYPE.get(v)
        if new is None:
            report['font:keep'] += 1; unknown[f'sp {v}'] += 1; return m.group(0)
        if new != v: report['font:changed'] += 1
        return f'{new}.sp'
    out = re.sub(r'(?<=fontSize = )([0-9]+(?:\.[0-9]+)?)\.sp', fix_font, out)
    out = re.sub(r'(?<=fontSize=)([0-9]+(?:\.[0-9]+)?)\.sp', fix_font, out)

    # ---------- ۳) فاصله‌های dp روی شبکه‌ی ۴ پیکسلی (فقط فراخوانی‌های فاصله) ----------
    def fix_dp(m):
        v = float(m.group(1))
        new = DP.get(v, v)
        if abs(new) < 34:                       # فاصله‌های بزرگ طرح دست‌نخورده
            snapped = int(round(new / 4.0)) * 4
            if snapped: new = float(snapped)    # ۱ و ۲ پیکسلی‌ها ریز می‌مانند
        if new != v: report['dp:changed'] += 1
        return f'{new:g}.dp'
    for pat in [r'(padding\([^)]*\))', r'(spacedBy\([^)]*\))',
                r'(Spacer\(Modifier\.(?:height|width)\([^)]*\))']:
        out = re.sub(pat, lambda m: re.sub(r'([0-9]+(?:\.[0-9]+)?)\.dp', fix_dp, m.group(1)), out)

    if out != src:
        report['files:touched'] += 1
        if APPLY: f.write_text(out)

print('--- گزارش پاس دیزاین (اندروید) ---')
for k in sorted(report): print(f'  {k:16s} {report[k]}')
print('  ناشناخته:', len(unknown))
for k, v in unknown.most_common(20): print('     ', k, 'x', v)
print('✅ اعمال شد' if APPLY else '(گزارش فقط)')
