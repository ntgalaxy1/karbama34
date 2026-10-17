#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""#28e — دمو: محتوای عربی آگهی‌های نمونه + توستِ بدون بلعیدنِ لمس + دکمه‌ی ❤ در هدر صفحه‌ی آگهی"""
import pathlib

DEMO = pathlib.Path('/home/user/karbama-demo.html')
s = DEMO.read_text()
n0 = len(s)
rep = []

# ═════════════════════════════════════════════════════════════
# ۱) محتوای عربی آگهی‌های نمونه (نام کارفرما + توضیحات + شرایط)
# ═════════════════════════════════════════════════════════════
AR_SAMPLE = {
 'j1': ("شركة بوشاك آرما / Arma Tekstil",
        "عامل خط إنتاج الخياطة، 45 ساعة أسبوعيًا، وردية نهارية ثابتة. تدريب أولي مجاني.",
        ["العمر من 18 إلى 45 سنة", "القدرة على العمل واقفًا", "الخبرة السابقة ميزة", "معرفة أولية بالتركية"]),
 'j2': ("مودا سنتر / Moda Center",
        "بائع في متجر ملابس في منطقة الفاتح، حسن التعامل مع العملاء والقدرة على العمل الجماعي.",
        ["خبرة في البيع", "تركية محادثة (B1 على الأقل)", "مظهر مرتب", "المرونة في الورديات"]),
 'j3': ("كانتكت سنتر بوغاز / Boğaz Call",
        "موظف هاتف بالفارسية والتركية للرد على العملاء الإيرانيين. بيئة مكتبية ووردية 8 ساعات.",
        ["إتقان الفارسية (إلزامي)", "تركية بمستوى متوسط", "مهارات الحاسوب", "صوت واضح"]),
 'j4': ("كافتيريا ملات / Melat Cafe",
        "باريستا بخبرة في الإسبريسو وفن اللاتيه. وردية مسائية وفي نهاية الأسبوع.",
        ["خبرة في المقاهي", "تركية محادثة", "سرعة في العمل", "ابتسامة وطاقة إيجابية"]),
 'j5': ("لوجستيك بسا / Bessa Lojistik",
        "أمين مستودع لتحميل وترتيب البضائع والعمل بماسح الباركود. وردية 12 ساعة بالتناوب.",
        ["قدرة بدنية جيدة", "معرفة بأعمال المستودعات", "تركية A2 على الأقل", "تحمّل البرد والحرارة"]),
 'j6': ("مطعم المأكولات البحرية الأناضولي",
        "طاهٍ محترف متمكن من المأكولات الإيرانية والمتوسطية لمطعم يتسع لـ 90 ضيفًا.",
        ["خبرة 3 سنوات على الأقل", "شهادة صحة (Hijyen belgesi)", "إدارة المشتريات ومراقبة الجودة", "تركية B1"]),
 'j7': ("توزيع الأناضول / Anadolu Dağıtım",
        "سائق شاحنة صغيرة لتوزيع المواد الغذائية في نطاق إستنبول الأوروبية. رخصة فئة B سارية.",
        ["رخصة تركية (B)", "تصريح إقامة ساري", "معرفة بشوارع إستنبول", "العمر فوق 25 سنة"]),
 'j8': ("دار المسنين مرمر",
        "ممرض أو مرافق مسنّين لوردية ليلية، رعاية ومرافقة المسنين الناطقين بالفارسية.",
        ["خبرة في الرعاية", "معرفة بالتركية", "صبر وتحمل المسؤولية"]),
 'j9': ("ورشة تكستيل أتيليه",
        "خيّاط متمكن من تصميم القوالب وماكينة الخياطة الصناعية لملابس نسائية.",
        ["خبرة خياطة سنتين على الأقل", "معرفة بآلة الخياطة الصناعية"]),
 'j10': ("سكوريتاس / Securitas TR",
        "حارس أمن لمبنى إداري، وردية 12 ساعة (يومان عمل ويومان راحة).",
        ["بطاقة أمنية (Silah,Kimlik)", "تركية محادثة", "لياقة بدنية جيدة"]),
 'j11': ("فندق مرمر بالاس",
        "عامل تنظيف لغرف الفندق، مع تدريب كامل. بيئة عمل دافئة ومنظّمة.",
        ["قدرة بدنية جيدة", "الالتزام بالمواعيد"]),
 'j12': ("شركة آنا للتصدير / Ana İhracat",
        "موظف إداري في مكتب التصدير، العمل على Excel ومراسلات بالفارسية والتركية والإنجليزية.",
        ["Excel وبرامج المكتب", "مراسلات بثلاث لغات"]),
 'j13': ("باساج كاربي / Carpi AVM",
        "بائع أحذية وحقائب، وردية نهارية ثابتة، وعمولة بيع شهرية.",
        ["خبرة في البيع", "مهارات تواصل"]),
 'j14': ("صناعات نيلوفر للسيارات",
        "عامل خط تجميع قطع السيارات، مع خدمة النقل والغداء.",
        ["العمر من 20 إلى 40 سنة", "القدرة على العمل بنظام الورديات"]),
 'j15': ("بيوت كولوجا المحمية",
        "عامل بيوت محمية لزراعة الطماطم، عمل كامل الموسم، وسكن مجاني للعاملين من مدن أخرى.",
        ["خبرة زراعية", "تحمّل العمل في الحرارة"]),
 'j16': ("قبزة الصناعية / Gebze Industrial",
        "لحّام أرجون وCO2 لقطع صناعية. اختبار مهارة في يوم المقابلة.",
        ["خبرة لحام 3 سنوات على الأقل", "شهادة لحام (اختيارية)"]),
 'j17': ("مكتب تأمين آنا",
        "سكرتير مكتب تأمين، الرد على الهاتف، تسجيل الملفات وتنسيق المواعيد.",
        ["تركية B2 وفارسية بطلاقة", "مهارات تنظيمية"]),
 'j18': ("مصنع تعبئة بيلسوي",
        "تعبئة المواد الغذائية، وردية 8 ساعات مع ساعات إضافية اختيارية.",
        ["سرعة اليدين", "الالتزام بالنظافة"]),
}
lines = []
for k, (co, desc, reqs) in AR_SAMPLE.items():
    rq = ",".join('"' + r.replace('"', '\\"') + '"' for r in reqs)
    lines.append(f'  {k}:{{co:"{co}", desc:"{desc}", reqs:[{rq}]}}')
blk = ("/* ---------- #28: محتوای عربی آگهی‌های نمونه (فقط برای حالت عربی) ---------- */\n"
       "const AR_SAMPLE = {\n" + ",\n".join(lines) + "\n};\n"
       "/** نام کارفرما/توضیحات/شرایط آگهی به زبان انتخاب‌شده (اگر ترجمه‌ی نمونه باشد) */\n"
       "function jCo(j){ return (S.lang===\"ar\" && AR_SAMPLE[j.id] ? AR_SAMPLE[j.id].co : j.company); }\n"
       "function jDesc(j){ return (S.lang===\"ar\" && AR_SAMPLE[j.id] ? AR_SAMPLE[j.id].desc : (j.desc||\"\")); }\n"
       "function jReqs(j){ return (S.lang===\"ar\" && AR_SAMPLE[j.id] ? AR_SAMPLE[j.id].reqs : (j.reqs||[])); }\n\n")

anchor = "function seedPeople(){"
assert anchor in s
s = s.replace(anchor, blk + anchor, 1)
rep.append('AR_SAMPLE')

# استفاده در همه‌ی محل‌های نمایش
for old, new in [
    ('<div class="co">🏢 ${esc(j.company)}</div>', '<div class="co">🏢 ${esc(jCo(j))}</div>'),
    ('<div style="font-size:11.5px;opacity:.92;margin-top:3px">🏢 ${esc(j.company)}</div>',
     '<div style="font-size:11.5px;opacity:.92;margin-top:3px">🏢 ${esc(jCo(j))}</div>'),
    ('<p style="font-size:13.5px;line-height:2.05;color:var(--ink2);margin:0">${esc(j.desc||"")}</p></div>',
     '<p style="font-size:13.5px;line-height:2.05;color:var(--ink2);margin:0">${esc(jDesc(j))}</p></div>'),
    ('${(j.reqs||[]).map(r=>`<li>${esc(r)}</li>`).join("")}</ul></div>',
     '${jReqs(j).map(r=>`<li>${esc(r)}</li>`).join("")}</ul></div>'),
    ('<div class="kv"><span>${t("company")}</span><b>${esc(j.company)}</b></div>',
     '<div class="kv"><span>${t("company")}</span><b>${esc(jCo(j))}</b></div>'),
    ('<div class="small muted" style="margin-top:3px">🏢 ${esc(j.company)} · 📍 ${j.city}${j.district?" / "+j.district:""}</div>',
     '<div class="small muted" style="margin-top:3px">🏢 ${esc(jCo(j))} · 📍 ${j.city}${j.district?" / "+j.district:""}</div>'),
    ('<div class="small muted" style="margin-top:3px">${esc(j.company)} · ${j.city}${j.district?" / "+j.district:""}</div>',
     '<div class="small muted" style="margin-top:3px">${esc(jCo(j))} · ${j.city}${j.district?" / "+j.district:""}</div>'),
    ('subject:jobTitle(j.titleKey)+" · "+esc(j.company) }',
     'subject:jobTitle(j.titleKey)+" · "+esc(jCo(j)) }'),
]:
    c = s.count(old)
    if c:
        s = s.replace(old, new)
        rep.append(f'use x{c}: {old[:38]}…')
    else:
        rep.append(f'MISS: {old[:44]}…')

# ═════════════════════════════════════════════════════════════
# ۲) توست نباید لمس را ببلعد (باگ واقعی: وسط ردیف را می‌پوشاند)
# ═════════════════════════════════════════════════════════════
old_toast = """.toast{
  position:absolute;bottom:82px;inset-inline:22px;background:#0F1F2B;color:#fff;padding:13px 16px;border-radius:14px;
  font-size:13.5px;font-weight:800;text-align:center;z-index:99;box-shadow:0 14px 34px -10px rgba(0,0,0,.6);
  animation:up .22s ease; display:flex; align-items:center; justify-content:center; gap:8px
}"""
assert old_toast in s
s = s.replace(old_toast, old_toast[:-1] + "  pointer-events:none;   /* #28: پیام نباید کلیک/لمس را ببلعد */\n}", 1)
rep.append('toast pointer-events')
old_toast2 = """.toast{
  border-radius:16px;"""
assert old_toast2 in s
s = s.replace(old_toast2, """.toast{
  pointer-events:none;   /* #28 */
  border-radius:16px;""", 1)

# ═════════════════════════════════════════════════════════════
# ۳) دکمه‌ی ❤ در هدر صفحه‌ی آگهی (بدون نیاز به اسکرول) + هندلر
# ═════════════════════════════════════════════════════════════
old_head = """      <div style="flex:1;min-width:0">
        <div style="font-size:10.5px;font-weight:800;opacity:.85;letter-spacing:.4px">📄 ${t("jobPage")}</div>"""
assert old_head in s
new_head = """      <div style="flex:1;min-width:0">
        <div style="font-size:10.5px;font-weight:800;opacity:.85;letter-spacing:.4px">📄 ${t("jobPage")}</div>"""
# دکمه‌ی قلب بعد از بلوک عنوان (قبل از بستن div ردیف هدر)
old_after_title = """        <div style="font-size:11.5px;opacity:.92;margin-top:3px">🏢 ${esc(jCo(j))}</div>
      </div>
    </div>"""
assert old_after_title in s
new_after_title = """        <div style="font-size:11.5px;opacity:.92;margin-top:3px">🏢 ${esc(jCo(j))}</div>
      </div>
      ${adminView?"":`<button class="heart-btn ${liked?"on":""}" title="${liked?t("unlike"):t("like")}"
          onclick="toggleLike('${j.id}')">${liked?"❤":"🤍"}</button>`}
    </div>"""
s = s.replace(old_after_title, new_after_title, 1)
rep.append('heart in header')

# CSS دکمه‌ی قلب
css_anchor = "/* ---------- #28: پسندیده (Liked) ---------- */"
assert css_anchor in s
s = s.replace(css_anchor, """/* #28: دکمه‌ی قلب در هدر صفحه‌ی آگهی */
.heart-btn{
  width:42px;height:42px;flex:0 0 42px;border-radius:50%;border:1.5px solid rgba(255,255,255,.75);
  background:rgba(255,255,255,.22); color:#fff; font-size:19px; line-height:1; cursor:pointer;
  display:flex;align-items:center;justify-content:center; transition:transform .12s ease, background .18s ease;
  box-shadow:0 10px 22px -12px rgba(0,0,0,.5)
}
.heart-btn:hover{transform:translateY(-2px)}
.heart-btn:active{transform:scale(.94)}
.heart-btn.on{background:rgba(255,255,255,.92); border-color:#fff; color:#C2185B}

""" + css_anchor, 1)

DEMO.write_text(s)
print('demo:', n0, '->', len(s))
for r in rep:
    print(' -', r)
