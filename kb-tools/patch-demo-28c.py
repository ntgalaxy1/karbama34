#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""#28c — دمو: نام عربی حرفه‌ها + نام عربی استان‌های ایران + مقاوم‌سازی توابع"""
import pathlib, re

DEMO = pathlib.Path('/home/user/karbama-demo.html')
s = DEMO.read_text()
n0 = len(s)

# ---------------------------------------------------------------
# ۱) نام عربی ۲۸ حرفه (TITLES)
# ---------------------------------------------------------------
AR_TITLES = {
 'prod_worker': 'عامل خط إنتاج', 'sales': 'بائع متجر', 'cashier': 'أمين صندوق',
 'secretary': 'سكرتير مكتب', 'admin_staff': 'موظف إداري', 'chef': 'طاهٍ (شيف)',
 'sous_chef': 'مساعد طاهٍ', 'barista': 'باريستا', 'waiter': 'نادل',
 'warehouse': 'أمين مستودع', 'driver': 'سائق', 'courier': 'مندوب توصيل',
 'guard': 'حارس أمن', 'cleaner': 'عامل تنظيف', 'tailor': 'خيّاط',
 'hairdresser': 'مصفّف شعر', 'callcenter': 'موظف مركز اتصال (فارسية/تركية)',
 'accountant': 'محاسب', 'welder': 'لحّام', 'cnc': 'مشغّل آلة CNC',
 'electrician': 'كهربائي', 'plumber': 'سبّاك', 'nurse': 'ممرض / مرافق مسنّين',
 'babysitter': 'مرافقة أطفال', 'construction': 'عامل بناء', 'gardener': 'عامل بيوت محمية',
 'it_dev': 'مبرمج', 'packer': 'عامل تغليف',
}
done = 0
for k, ar in AR_TITLES.items():
    pat = re.compile(r'(\n  ' + re.escape(k) + r':\{fa:"[^"]*", tr:"[^"]*", en:"[^"]*")')
    m = pat.search(s)
    if m:
        s = s[:m.end(1)] + f', ar:"{ar}"' + s[m.end(1):]
        done += 1
print('titles patched:', done, '/', len(AR_TITLES))

# ---------------------------------------------------------------
# ۲) توابع مقاوم (fallback زبان)
# ---------------------------------------------------------------
old_jt = """function jobTitle(key){ const j=TITLES[key]; return j ? j[S.lang||"fa"] : key; }"""
assert old_jt in s
s = s.replace(old_jt, """function jobTitle(key){ const j=TITLES[key]; if(!j) return key; return j[S.lang||"fa"] || j.fa || j.en || key; }""", 1)

old_jobs = """  const jobsOf = Object.keys(TITLES).filter(k=> !f.jobQ || TITLES[k][S.lang].includes(f.jobQ) || TITLES[k].fa.includes(f.jobQ) || TITLES[k].en.toLowerCase().includes(f.jobQ.toLowerCase()));"""
assert old_jobs in s
s = s.replace(old_jobs, """  const jobsOf = Object.keys(TITLES).filter(k=> !f.jobQ || (TITLES[k][S.lang]||TITLES[k].fa||"").includes(f.jobQ) || (TITLES[k].fa||"").includes(f.jobQ) || (TITLES[k].en||"").toLowerCase().includes(f.jobQ.toLowerCase()));""", 1)

# ---------------------------------------------------------------
# ۳) نام عربی استان‌های ایران (برچسب‌ها؛ مقدار ذخیره‌شده همان نام استان است)
# ---------------------------------------------------------------
AR_PROV = {
 "آذربایجان شرقی": "أذربيجان الشرقية", "آذربایجان غربی": "أذربيجان الغربية", "اردبیل": "أردبيل",
 "اصفهان": "أصفهان", "البرز": "البرز", "ایلام": "إيلام", "بوشهر": "بوشهر", "تهران": "طهران",
 "چهارمحال و بختیاری": "تشهارمحال وبختياري", "خراسان جنوبی": "خراسان الجنوبية",
 "خراسان رضوی": "خراسان الرضوية", "خراسان شمالی": "خراسان الشمالية", "خوزستان": "خوزستان",
 "زنجان": "زنجان", "سمنان": "سمنان", "سیستان و بلوچستان": "سيستان وبلوشستان", "فارس": "فارس",
 "قزوین": "قزوين", "قم": "قم", "کردستان": "كردستان", "کرمان": "كرمان", "کرمانشاه": "كرمانشاه",
 "کهگیلویه و بویراحمد": "كهكيلويه وبوير أحمد", "گلستان": "كلستان", "گیلان": "جيلان",
 "لرستان": "لرستان", "مازندران": "مازندران", "مرکزی": "مركز", "هرمزگان": "هرمزكان",
 "همدان": "همدان", "یزد": "يزد",
}
anchor_prov = "const IR_PROVINCE_NAMES = Object.keys(IR_PROVINCES);"
assert anchor_prov in s
prov_js = "const IR_PROVINCE_NAMES = Object.keys(IR_PROVINCES);\n\n/* #28: نام عربی استان‌ها — مقدار ذخیره‌شده همان نام استان است، فقط برچسب عربی می‌شود */\nconst IR_PROVINCE_AR = {\n" + \
    "".join(f'  "{k}":"{v}",\n' for k, v in AR_PROV.items()).rstrip(",\n") + "\n};\n" + \
    "function provLabel(name){ return (S.lang===\"ar\" && IR_PROVINCE_AR[name]) ? IR_PROVINCE_AR[name] : name; }"
s = s.replace(anchor_prov, prov_js, 1)

# استفاده در دو <select>
old_opt1 = """          ${IR_PROVINCE_NAMES.map(c=>`<option ${f.city===c?"selected":""}>${c}</option>`).join("")}"""
assert s.count(old_opt1) == 1
s = s.replace(old_opt1, """          ${IR_PROVINCE_NAMES.map(c=>`<option value="${c}" ${f.city===c?"selected":""}>${provLabel(c)}</option>`).join("")}""", 1)
old_opt2 = """      ${IR_PROVINCE_NAMES.map(c=>`<option ${f.city===c?"selected":""}>${c}</option>`).join("")}"""
assert s.count(old_opt2) == 1
s = s.replace(old_opt2, """      ${IR_PROVINCE_NAMES.map(c=>`<option value="${c}" ${f.city===c?"selected":""}>${provLabel(c)}</option>`).join("")}""", 1)

DEMO.write_text(s)
print('demo:', n0, '->', len(s))
