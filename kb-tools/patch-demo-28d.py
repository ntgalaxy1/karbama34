#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""#28d — دمو: پاک‌کردن باقی‌مانده‌های فارسی در حالت عربی + فلش بازگشت درست برای RTL"""
import pathlib

DEMO = pathlib.Path('/home/user/karbama-demo.html')
s = DEMO.read_text()
n0 = len(s)
rep = []

# ۱) راهنمای صفحه‌ی زبان: دیگر فارسیِ ثابت نباشد
old = """      <div style="font-size:13px;opacity:.9;font-weight:700;margin-top:6px">Choose your language</div>
      <div style="font-size:12px;opacity:.75;margin-top:4px">زبان برنامه را انتخاب کنید · Dil seçin</div>"""
new = """      <div style="font-size:13px;opacity:.9;font-weight:700;margin-top:6px">Choose your language</div>
      <div style="font-size:12px;opacity:.75;margin-top:4px">${t("langHint")}</div>"""
assert old in s, 'lang subtitle'
s = s.replace(old, new, 1)
rep.append('lang-subtitle')

# ۲) langHint عربی
old_ar = '    langHint:"Choose your language · Dil seçin",'
assert old_ar in s, 'ar langHint'
s = s.replace(old_ar, '    langHint:"اختر اللغة · Choose your language · Dil seçin",', 1)
rep.append('ar-langHint')

# ۳) فلش بازگشت: برای عربی هم «→» (راست‌به‌چپ)
cnt = s.count('${S.lang==="fa"?"→":"←"}')
s = s.replace('${S.lang==="fa"?"→":"←"}', '${backArrow()}')
rep.append(f'back-arrows x{cnt}')

DEMO.write_text(s)
print('demo:', n0, '->', len(s), rep)
