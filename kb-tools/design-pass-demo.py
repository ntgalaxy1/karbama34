#!/usr/bin/env python3
"""پاس دیزاین دمو (#33) — یک‌دست‌سازی رنگ، فونت، فاصله، گردی گوشه و سایه.
dry-run:  python3 kb-tools/design-pass-demo.py
اعمال:    python3 kb-tools/design-pass-demo.py --apply
"""
import re, sys, pathlib, collections

F = pathlib.Path('/home/user/karbama-demo.html')
APPLY = '--apply' in sys.argv
s = F.read_text()

# =============== پالت نهایی (تک‌منبع) ===============
PALETTE = {
    '#0F1F2B': 'Ink',      '#3F5566': 'Ink2',  '#57697A': 'Ink3',
    '#E3ECF0': 'Line',     '#F6FAFB': 'Surface', '#ECF3F6': 'Bg',
    '#0E9F8E': 'Brand',    '#0A7F91': 'BrandDeep', '#066F6B': 'BrandDark',
    '#19CBAD': 'BrandTeal','#10B39F': 'BrandGreen',
    '#E6F7F4': 'BrandTint','#D6F2EC': 'BrandTint2', '#E2FBF3': 'MintTint',
    '#2FD6A8': 'Mint',     '#7C5CFF': 'Violet', '#5B3FE0': 'VioletDeep',
    '#EEEBFF': 'VioletTint','#FF7A59': 'Coral', '#E4562F': 'CoralDeep',
    '#FFEDE7': 'CoralTint','#FFB020': 'Sun', '#F59E0B': 'SunDeep',
    '#FFF4DE': 'SunTint',  '#8A5B00': 'WarnText', '#7A5310': 'WarnText2',
    '#12A150': 'Ok',       '#0E7A44': 'OkDeep', '#E4F7EC': 'OkTint',
    '#E5484D': 'Danger',   '#FEF0F0': 'DangerTint', '#E5484D': 'Danger',
    '#C2185B': 'Pink',     '#FFE3ED': 'PinkTint',
    '#1D6FD4': 'Blue',     '#E7F2FF': 'BlueTint',
    '#3B4753': 'Graphite', '#EFF3F5': 'GraphiteTint',
    '#101C24': 'Bezel',    '#2B3D48': 'Bezel2',
    '#FFFFFF': 'White',
    # تُن‌های تصویرسازی داخل SVG (زمینه‌ی آواتار/سند/نمودار)
    '#C6D5DE': 'PhMid',    '#D9E4EA': 'PhLow',  '#ECF3F6': 'PhBg',
    # رنگ‌های گرادیان (بدون تغییر - فقط رسمی می‌شوند)
    '#A7F3D0': 'GradMint', '#063C44': 'ShadowInk', '#FFC8DC': 'PinkLine', '#FDE68A': 'GradSun', '#FDBA74': 'GradCoral',
    '#10303D': 'Ink2',     '#0B5F7A': 'BrandDark', '#CDE7F3': 'BlueTint',
    # رنگ‌های رسمی برندهای بیرونی (تلگرام/واتساپ/اینستاگرام) — دست‌نخورده
    '#229ED9': 'TgLogo', '#2AABEE': 'TgLogo', '#25D366': 'WaLogo',
    '#3ED36F': 'WaLogo', '#DD2A7B': 'IgLogo', '#F58529': 'IgLogo',
    '#8134AF': 'IgLogo',
}
# استثناهای دستی (رنگ‌هایی که برچسب درست را باید بگیرند)
MANUAL = {
    '#05090C':'#0F1F2B','#0A1A24':'#0F1F2B','#0d1a22':'#0F1F2B','#0e2430':'#0F1F2B',
    '#12242f':'#0F1F2B','#123040':'#3F5566','#33454f':'#3F5566','#33525f':'#3F5566',
    '#3D5261':'#3F5566','#44586A':'#3F5566','#2b3d48':'#2B3D48','#101c24':'#101C24',
    '#8496A6':'#5F7587','#7C8D9A':'#5F7587','#8A9BA9':'#5F7587','#8fa3b1':'#5F7587',
    '#8fb0bd':'#5F7587','#86a4b0':'#5F7587','#9db0ba':'#5F7587','#9fb6c2':'#5F7587',
    '#a9b8c4':'#5F7587','#b3c8d2':'#5F7587','#a7c3cd':'#5F7587','#bcd7e0':'#5F7587',
    '#7b8c97':'#5F7587','#63798a':'#5F7587',
    '#0a3d4d':'#066F6B','#06505f':'#066F6B','#05635F':'#066F6B','#066C69':'#066F6B',
    '#077A6D':'#066F6B','#0A8E8C':'#0E9F8E','#12B39F':'#10B39F','#16C3AC':'#19CBAD',
    '#22C79B':'#2FD6A8','#3FE3C6':'#2FD6A8','#5BE9C4':'#2FD6A8','#0E9F6E':'#12A150',
    '#0EA394':'#0E9F8E','#c9e6e1':'#D6F2EC','#c8f0e9':'#D6F2EC','#cfefea':'#D6F2EC',
    '#c4efe6':'#D6F2EC','#d7f4ee':'#D6F2EC','#d9f2ee':'#D6F2EC','#d5f6ef':'#D6F2EC',
    '#dbf8f1':'#E2FBF3','#dff7f2':'#E2FBF3','#e2f7f3':'#E2FBF3','#f2fffb':'#E6F7F4',
    '#e6f7f5':'#E6F7F4','#e8f6f4':'#E6F7F4',
    '#cfdde4':'#E3ECF0','#d5dfe6':'#E3ECF0','#d3e0e6':'#E3ECF0','#d6e0e6':'#E3ECF0',
    '#dbe5ea':'#E3ECF0','#dfe7ec':'#E3ECF0','#e7eef3':'#E3ECF0','#e8eef2':'#E3ECF0',
    '#eaf2f7':'#E3ECF0','#e2e9ee':'#E3ECF0','#e4eaee':'#E3ECF0','#e4edf2':'#E3ECF0',
    '#e6eef2':'#E3ECF0','#e9f1f6':'#E3ECF0','#ede3f7':'#E3ECF0','#d7e0e5':'#E3ECF0',
    '#cbd8e0':'#E3ECF0','#d7e1e8':'#D9E4EA','#c3d0da':'#C6D5DE','#b7c6d1':'#C6D5DE',
    '#f3f6f8':'#F6FAFB','#f4f8fa':'#F6FAFB','#f6fafb':'#F6FAFB','#f7fafb':'#F6FAFB',
    '#f8fbfc':'#F6FAFB','#fbfdfe':'#F6FAFB','#fdfeff':'#F6FAFB','#fafdfe':'#F6FAFB',
    '#eef4f7':'#E3ECF0','#f3f8fb':'#ECF3F6','#f1f6f8':'#F6FAFB','#e9f5f3':'#F6FAFB',
    '#eaf1f5':'#ECF3F6','#eef3f6':'#ECF3F6','#eef7f8':'#F6FAFB','#f4fbfd':'#F6FAFB',
    '#e9eff3':'#ECF3F6','#EDF3F7':'#ECF3F6','#eef4f7':'#E3ECF0',
    '#3D2606':'#5A3D06','#3d2606':'#5A3D06','#40270a':'#5A3D06','#42290a':'#5A3D06',
    '#43220a':'#5A3D06','#432c04':'#5A3D06','#5c3d07':'#7A5310','#7a5310':'#7A5310',
    '#C97F00':'#8A5B00','#c97f00':'#8A5B00','#F7A928':'#F59E0B',
    '#B3155A':'#C2185B','#F2605F':'#E5484D','#f4c9c9':'#FEF0F0','#f8d7d8':'#FEF0F0',
    '#FEF3C7':'#FFF4DE','#FFF3DC':'#FFF4DE','#fdf0d5':'#FFF4DE','#fff7e6':'#FFF4DE',
    '#ffeccd':'#FFF4DE','#fff2d6':'#FFF4DE','#FFDB86':'#FFF4DE','#ffdb86':'#FFF4DE',
    '#FDE68A':'#FDE68A','#F7DFA5':'#FDE68A','#FFE7B8':'#FDE68A','#FFE9C2':'#FDE68A',
    '#f7dfa5':'#FDE68A','#ffe7b8':'#FDE68A','#ffe9c2':'#FDE68A',
    '#ffd7d1':'#FFEDE7','#ffe7dc':'#FFEDE7','#FFE3E1':'#FFEDE7','#FFF1F0':'#FEF0F0',
    '#fdba74':'#FDBA74','#FFBB8A':'#FDBA74','#FFA07A':'#FDBA74','#ffbb8a':'#FDBA74',
    '#ffa07a':'#FDBA74','#ffcd57':'#FFB020','#FFD15C':'#FFB020','#FFCD57':'#FFB020',
    '#ffd15c':'#FFB020','#FBBF24':'#FFB020','#fbbf24':'#FFB020','#fdc24f':'#FFB020',
    '#FDC24F':'#FFB020',
    '#A28CFF':'#7C5CFF','#9B7CFF':'#7C5CFF','#a28cff':'#7C5CFF','#9b7cff':'#7C5CFF',
    '#D6CEFF':'#EEEBFF','#E3DEFF':'#EEEBFF','#d6ceff':'#EEEBFF','#e3deff':'#EEEBFF',
    '#ece6ff':'#EEEBFF','#4B31C7':'#5B3FE0','#5B3FD1':'#5B3FE0','#5344a8':'#5B3FE0',
    '#6E56CF':'#7C5CFF','#6e56cf':'#7C5CFF','#A7F3D0':'#A7F3D0','#a7f3d0':'#A7F3D0',
    '#000':'#0F1F2B','#000000':'#0F1F2B','#fff':'#FFFFFF','#ffffff':'#FFFFFF',
    '#10303d':'#3F5566','#0B5F7A':'#066F6B','#0b5f7a':'#066F6B',
    '#E9EFF3':'#ECF3F6','#e9eff3':'#ECF3F6',
    '#A9EFDD':'#D6F2EC','#FFFFFF':'#FFFFFF',
    '#5F7587':'#57697A', '#063C44':'#063C44','#042832':'#063C44','#032832':'#063C44','#08141A':'#063C44',
    '#063C46':'#063C44','#08424C':'#063C44',
    '#5B6B78':'#5F7587','#FFC8DC':'#FFC8DC','#F0CF8A':'#FDE68A','#9FDCB9':'#A7F3D0',
    '#B0793A':'#8A5B00',
}
def hx(h):
    h = h.lstrip('#')
    if len(h) == 3: h = ''.join(c*2 for c in h)
    return tuple(int(h[i:i+2], 16) for i in (0, 2, 4))
def redmean(a, b):
    r1,g1,b1 = hx(a); r2,g2,b2 = hx(b)
    rm = (r1+r2)/2; dr, dg, db = r1-r2, g1-g2, b1-b2
    return ((2+rm/256)*dr*dr + 4*dg*dg + (2+(255-rm)/256)*db*db) ** .5
PAL_KEYS = list(PALETTE)
DIST_LIMIT = 26.0

TYPE_MAP = {10:11, 10.4:11, 10.5:11, 10.6:11, 11:11, 11.5:12, 11.6:12,
            12:12, 12.4:12, 12.5:13, 12.6:13, 12.8:13, 13:13, 13.5:14,
            14:14, 14.5:14, 14.6:14, 15:15, 15.5:16, 16:16, 17:16, 18:18,
            19:20, 20:20, 21:20, 22:22, 24:22, 26:26, 27:26, 30:26, 38:38,
            9.5:11, 40:38}
RADIUS = {2:8, 3:8, 4:8, 6:8, 7:8, 8:8, 9:12, 10:12, 11:12, 12:12, 13:12,
          14:16, 15:16, 16:16, 17:16, 18:20, 19:20, 20:20, 21:20, 22:20,
          23:24, 24:24, 25:24, 26:24, 27:24, 28:24, 30:24, 32:24}
LH = {1:1.5, 1.45:1.5, 1.5:1.5, 1.6:1.6, 1.7:1.7, 1.75:1.8, 1.8:1.8,
      1.85:1.9, 1.9:1.9, 1.95:2, 2:2, 2.15:2}
LS = {.15:.2, .2:.2, .3:.4, .5:.4, .6:.4, -.1:-.2, -.2:-.2, 0:0, 3:3}
ALPHA = [.05,.08,.12,.16,.2,.25,.3,.35,.42,.5,.55,.6,.62,.7,.75,.8,.85,.86,.9,.92,.95]
NEG = {-1:-2, -2:-2, -4:-4, -6:-8, -8:-8, -13:-16, -14:-16, -15:-16, -16:-16, -20:-20, -22:-24}

report = collections.Counter()
unknown = collections.Counter()

def snap_alpha(a):
    try: v = float(a)
    except ValueError: return a
    best = min(ALPHA, key=lambda x: abs(x - v))
    return ('%g' % best)

# ---------- ۱) رنگ‌های hex ----------
def fix_color(m):
    body = m.group(0)[1:]
    alpha = ''
    if len(body) in (4, 8):
        alpha = body[-1] if len(body) == 4 else body[-2:]
        body = body[:-1] if len(body) == 4 else body[:-2]
    if len(body) == 3: body = ''.join(c*2 for c in body)
    key = '#' + body
    target = None
    if len(key) == 7:
        up = key.upper()
        if up in PALETTE: target = up
        else:
            for k, v in MANUAL.items():
                if k.upper() == up: target = v.upper(); break
            if target is None:
                near, d = min(((k, redmean(up, k)) for k in PAL_KEYS), key=lambda kv: kv[1])
                if d <= DIST_LIMIT: target = near; report['color:auto'] += 1
                else:
                    unknown[up] += 1; report['color:keep'] += 1; return m.group(0)
    if not target:
        report['color:keep'] += 1; return m.group(0)
    out = target + (alpha.upper() if alpha else '')
    if out.upper() != m.group(0).upper(): report['color:changed'] += 1
    return out

# ---------- ۲) rgba/rgb ----------
def fix_rgba(m):
    fn = m.group(1); inner = m.group(2)
    parts = [p.strip() for p in inner.replace('/', ' ').split(',')] if ',' in inner else inner.split()
    if len(parts) < 3: return m.group(0)
    try: rgb = tuple(int(float(p)) for p in parts[:3])
    except ValueError: return m.group(0)
    a = parts[3] if len(parts) > 3 else '1'
    if rgb == (255, 255, 255):
        return f'rgba(255,255,255,{snap_alpha(a)})'
    if rgb == (0, 0, 0):
        return f'rgba(15,31,43,{snap_alpha(a)})'
    key = '#%02X%02X%02X' % rgb
    up = key.upper()
    man = next((v for k, v in MANUAL.items() if k.upper() == up), None)
    if man:
        return 'rgba(%d,%d,%d,%s)' % (hx(man)[0], hx(man)[1], hx(man)[2], snap_alpha(a))
    if up in PALETTE:
        return 'rgba(%d,%d,%d,%s)' % (rgb[0], rgb[1], rgb[2], snap_alpha(a))
    near, d = min(((k, redmean(key, k)) for k in PAL_KEYS), key=lambda kv: kv[1])
    if d > DIST_LIMIT:
        unknown['rgba ' + key] += 1; report['rgba:keep'] += 1
        return m.group(0)
    report['rgba:changed'] += 1
    return 'rgba(%d,%d,%d,%s)' % (hx(near)[0], hx(near)[1], hx(near)[2], snap_alpha(a))

body_start = s.index('/* ============ THEME')
body_end = s.index('</style>')
head, theme, tail = s[:body_start], s[body_start:body_end], s[body_end:]
theme = re.sub(r'#[0-9A-Fa-f]{3,8}', fix_color, theme)
tail = re.sub(r'#[0-9A-Fa-f]{3,8}', fix_color, tail)
for part, i in ((theme, 0), (tail, 1)):
    pass
theme = re.sub(r'(rgba?)\(([^)]+)\)', fix_rgba, theme)
tail = re.sub(r'(rgba?)\(([^)]+)\)', fix_rgba, tail)
s = head + theme + tail

# ---------- ۳) فونت ----------
def fix_font(m):
    v = float(m.group(1))
    new = TYPE_MAP.get(v)
    if new is None: report['font:keep'] += 1; unknown[f'font {v}'] += 1; return m.group(0)
    if new != v: report['font:changed'] += 1
    return f'font-size:{new}px'
s = re.sub(r'font-size:\s*([0-9.]+)px', fix_font, s)
s = re.sub(r'font:\s*([0-9]+)?\s*([0-9.]+)px/([0-9.]+)\s*', lambda m: 'font:' + (m.group(1) + ' ' if m.group(1) else '') + f"{TYPE_MAP.get(float(m.group(2)), float(m.group(2))):g}px/{LH.get(float(m.group(3)), float(m.group(3))):g} ", s)


# ---------- ۴) فاصله‌ها ----------
def one_space(tok):
    t = tok.group(0); n = float(t[:-2])
    if abs(n) >= 34: return t
    if n < 0:
        new = NEG.get(int(n))
        if new is None: new = -round(abs(n)/2)*2
    else:
        new = round(n/2)*2 or 2
    report['space:changed'] += 1
    return f'{new}px'
def fix_space(m):
    prop, val = m.group(1), m.group(2)
    if '%' in val or 'auto' in val or 'var(' in val: return m.group(0)
    return prop + ':' + re.sub(r'-?[0-9.]+px', one_space, val)
s = re.sub(r'\b(padding|margin|padding-top|padding-bottom|padding-left|padding-right|padding-inline|padding-inline-start|padding-inline-end|padding-block|margin-top|margin-bottom|margin-left|margin-right|margin-inline-start|margin-inline-end|row-gap|column-gap|gap):\s*([^;}"\']+)',
           fix_space, s)
# پدینگ صفحه‌ی همه‌ی صفحه‌ها یک‌دست: ۱۶ پیکسل
s = s.replace('padding:14px 16px ${fab?"86px":"26px"}', 'padding:16px const_16 ${fab?"88px":"28px"}')

# ---------- ۵) گردی گوشه ----------
def fix_radius(m):
    v = float(m.group(1))
    if v >= 34 or v == 99: return m.group(0)
    new = RADIUS.get(v) or min([8,12,16,20,24], key=lambda x: abs(x-v))
    if new != v: report['radius:changed'] += 1
    return f'border-radius:{new}px'
s = re.sub(r'border-radius:\s*([0-9.]+)px', fix_radius, s)

# ---------- ۶) ضخامت خط ----------
s = re.sub(r'border:\s*2\.5px solid', 'border:2px solid', s)
s = re.sub(r'border:\s*3px solid', 'border:2px solid', s)
report['border:changed'] += len(re.findall(r'border:\s*1\.6px solid', s)) + len(re.findall(r'border:\s*1\.5px solid', s))
s = re.sub(r'border:\s*1\.6px solid\s*(var\(--line\)|[^;"\'}]+)', r'border:1px solid \1', s)
s = re.sub(r'border:\s*1\.5px solid\s*(var\(--line\)|[^;"\'}]+)', r'border:1px solid \1', s)

# ---------- ۷) سایه‌ها ----------
def fix_shadow(m):
    val = m.group(1)
    if '0 0 0 10px' in val or 'var(' in val: return m.group(0)
    if 'inset' in val and '0 0 0' in val: return m.group(0)
    low = val.lower()
    if re.search(r'rgba\(1[0-9],1[5-9][0-9],1[0-9][0-9]', low) or 'rgba(16,179,159' in low or 'rgba(14,159,142' in low:
        return 'box-shadow:var(--sh-brand)'
    if 'rgba(245,158,11' in low or 'rgba(255,176,32' in low:
        return 'box-shadow:var(--sh-sun)'
    if 'rgba(229,72,77' in low:
        return 'box-shadow:var(--sh-danger)'
    if re.search(r'\b(0|1|2)px\b\s+([0-7])px', val.strip()):
        return 'box-shadow:var(--sh)'
    report['shadow:changed'] += 1
    return 'box-shadow:var(--sh2)'
s = re.sub(r'box-shadow:\s*([^;}"\']+)', fix_shadow, s)
# گرادیان نام برند → توکن
s = s.replace('linear-gradient(170deg,#0E9F8E,#0A7F91 60%,#066F6B)', 'var(--brand-g2)')

# ---------- ۸) ارتفاع خط و فاصله‌ی حروف ----------
def fix_lh(m):
    v = float(m.group(1)); new = LH.get(v)
    if new is None: report['lh:keep'] += 1; return m.group(0)
    if new != v: report['lh:changed'] += 1
    return f'line-height:{new}'
s = re.sub(r'line-height:\s*([0-9.]+)\s*(?=[;"\'}])', fix_lh, s)
def fix_ls(m):
    v = float(m.group(1)); new = LS.get(v)
    if new is None: report['ls:keep'] += 1; return m.group(0)
    if new != v: report['ls:changed'] += 1
    return f'letter-spacing:{new}px'
s = re.sub(r'letter-spacing:\s*(-?[0-9.]+)px', fix_ls, s)

# ---------- ۱۰) قاعده‌های پایه و پاک‌سازی نهایی ----------
BASE = """/* #33: پایه‌ی یک‌دست کنترل‌ها — فونت و اندازه‌ی همه‌ی دکمه‌ها/فیلدها یکی است */
button, input, select, textarea{ font-family:var(--ff); font-size:14px; line-height:1.5; color:var(--ink); }
button{ padding:0; }
.iconbtn, .heart-btn, .pw-eye, .fab{ padding:0; }
.kb-credit{ font-size:11px; color:rgba(95,117,135,.72); }
input::placeholder, textarea::placeholder{ color:#57697A; opacity:1; }
input:not([type=range]):not([type=checkbox]):not([type=radio]), textarea, select{ padding:12px 14px; }
input[type=range], input[type=checkbox], input[type=radio]{ padding:0; }
"""
marker = '<style>\n'
import re as _re
if '/* #33: پایه‌ی یک‌دست کنترل‌ها' in s:
    s = _re.sub(r'<style>\n/\* #33: پایه‌ی یک‌دست کنترل‌ها.*?\n(?=/\* =+ THEME)', '<style>\n' + BASE, s, count=1, flags=_re.S)
else:
    s = s.replace(marker, marker + BASE, 1)
    report['base:added'] += 1


# ---------- ۱۱) شبکه‌ی ۴ پیکسلی برای فاصله‌ها ----------
def snap4(v):
    n = int(round(float(v) / 4.0)) * 4
    if n == 0: return 2 if float(v) > 0 else 0     # فقط لبه‌های ریز ۲ پیکسلی می‌مانند
    return n
def one_space4(tok):
    t = tok.group(0); n = float(t[:-2])
    if abs(n) >= 34: return t
    new = snap4(n) if n >= 0 else -snap4(abs(n))
    if new != n: report['space4:changed'] += 1
    return f'{new}px'
def fix_space4(m):
    prop, val = m.group(1), m.group(2)
    if '%' in val or 'var(' in val: return m.group(0)
    return prop + ':' + re.sub(r'-?[0-9.]+px', one_space4, val)   # auto دست‌نخورده می‌ماند
s = re.sub(r'\b(padding|margin|padding-top|padding-bottom|padding-left|padding-right|padding-inline|padding-inline-start|padding-inline-end|padding-block|margin-top|margin-bottom|margin-left|margin-right|margin-inline-start|margin-inline-end|row-gap|column-gap|gap):\s*([^;}"\']+)',
           fix_space4, s)


# ---------- ۹) حذف «حالت نمایشی» و یک‌دست‌سازی توکن‌ها ----------
CANON = '''/* ============ THEME — تنها منبع رنگ/فونت/فاصله (دیزاین v4) ============ */
:root{
  /* --- رنگ‌های پایه --- */
  --brand:#0E9F8E; --brand-d:#066F6B; --brand-deep:#0A7F91; --brand-l:#E6F7F4;
  --accent:#F59E0B; --accent-l:#FFF4DE;
  --ink:#0F1F2B; --ink2:#3F5566; --ink3:#57697A;      /* همه ≥ ۴.۵:۱ کنتراست */
  --bg:#ECF3F6; --card:#FFFFFF; --line:#E3ECF0;
  --danger:#E5484D; --ok:#12A150; --purple:#7C5CFF;
  --violet:#7C5CFF; --violet-d:#5B3FE0; --violet-l:#EEEBFF;
  --coral:#FF7A59; --coral-d:#E4562F; --coral-l:#FFEDE7;
  --sun:#FFB020; --sun-d:#F59E0B; --sun-l:#FFF4DE;
  --mint:#2FD6A8; --mint-l:#E2FBF3;
  /* --- گرادیان‌ها --- */
  --grad-brand:linear-gradient(135deg,#0E9F8E 0%,#0A8E8C 45%,#0A7F91 100%);
  --brand-g:var(--grad-brand);
  --brand-g2:linear-gradient(170deg,#0E9F8E,#0A7F91 60%,#066F6B);
  --accent-g:linear-gradient(180deg,#FFB020,#F59E0B);
  --grad-violet:linear-gradient(135deg,#7C5CFF,#5B3FE0);
  --grad-coral:linear-gradient(135deg,#FF7A59,#E4562F);
  --grad-sun:linear-gradient(135deg,#FFB020,#F59E0B);
  --grad-mint:linear-gradient(135deg,#2FD6A8,#19CBAD);
  /* --- شیشه (glassmorphism) --- */
  --glass-1:linear-gradient(150deg,rgba(255,255,255,.9) 0%,rgba(255,255,255,.62) 100%);
  --glass-2:linear-gradient(150deg,rgba(255,255,255,.75) 0%,rgba(255,255,255,.5) 100%);
  --glass-line:rgba(255,255,255,.75);
  /* --- سایه‌ها: سه سطح + درخشش رنگی --- */
  --sh:0 1px 2px rgba(12,38,50,.05), 0 10px 26px -10px rgba(12,38,50,.16);
  --sh2:0 2px 4px rgba(12,38,50,.05), 0 14px 30px -12px rgba(12,38,50,.18);
  --sh3:0 22px 48px -22px rgba(6,52,60,.38);
  --sh-brand:0 14px 28px -14px rgba(16,179,159,.6);
  --sh-sun:0 14px 28px -14px rgba(245,158,11,.55);
  --sh-danger:0 14px 28px -14px rgba(229,72,77,.45);
  --glass-shadow:var(--sh2);
  --glow:var(--sh-brand);
  --ring:0 0 0 4px rgba(16,179,159,.16);
  /* --- گردی گوشه --- */
  --r:16px; --r2:20px; --r3:24px;
  /* --- شبکه‌ی فاصله (۴ پیکسلی) --- */
  --s1:4px; --s2:8px; --s3:12px; --s4:16px; --s5:20px; --s6:24px;
  /* --- فونت --- */
  --ff:"Vazirmatn","Segoe UI",Tahoma,"Iranian Sans",system-ui,-apple-system,sans-serif;
}'''
blocks = list(re.finditer(r':root\s*\{[^}]*\}', s))
assert 1 <= len(blocks) <= 3, f'تعداد بلوک‌های :root = {len(blocks)}'
s = _re.sub(r'(/\* =+ THEME ====+ \*/\n)+', '', s)   # تیتر تکراری
blocks = list(_re.finditer(r':root\s*\{[^}]*\}', s))
s = s[:blocks[0].start()] + CANON + s[blocks[0].end():]
for b in reversed(list(re.finditer(r':root\s*\{[^}]*\}', s))[1:]):
    s = s[:b.start()] + s[b.end():]
s = s.replace('const_16', '16px')

F.write_text(s) if APPLY else None
print('--- گزارش پاس دیزاین (دمو) ---')
for k in sorted(report): print(f'  {k:16s} {report[k]}')
print('  ناشناخته (نگه‌داشته شد):', len(unknown))
for k, v in unknown.most_common(15): print('     ', k, 'x', v)
print('\nبلوک‌های :root باقی‌مانده:', len(re.findall(r':root\s*\{', s)))
print('✅ اعمال شد' if APPLY else '(گزارش فقط)')
