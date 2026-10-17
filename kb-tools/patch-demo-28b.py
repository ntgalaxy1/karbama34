#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""#28b — صفحه‌ی کامل آگهی + بخش «پسندیده» + کلیدهای سه زبان + کم‌رنگ‌کردن powered by"""
import pathlib, re

DEMO = pathlib.Path('/home/user/karbama-demo.html')
s = DEMO.read_text()
n0 = len(s)

# ═════════════════════════════════════════════════════════════
# ۲) کلیدهای تازه در سه دیکشنری
# ═════════════════════════════════════════════════════════════
NEW = {
 'fa': ('    delYes:"بله، حذف شود", delNo:"انصراف",',
        '\n    // #28 — بخش «پسندیده» و صفحه‌ی کامل آگهی\n'
        '    pasandide:"پسندیده", like:"پسندیدم", liked:"پسندیده شد ✓",\n'
        '    unlike:"برداشتن از پسندیده", likedEmpty:"هنوز آگهی‌ای را پسند نکرده‌اید",\n'
        '    likedAdded:"به فهرست پسندیده‌ها اضافه شد ✓", likedRemoved:"از فهرست پسندیده‌ها برداشته شد",\n'
        '    jobPage:"صفحه‌ی آگهی",'),
 'tr': ('    delYes:"Evet, sil", delNo:"Vazgeç",',
        '\n    pasandide:"Beğendiklerim", like:"Beğendim", liked:"Beğenildi ✓",\n'
        '    unlike:"Beğeniyi kaldır", likedEmpty:"Henüz beğendiğiniz ilan yok",\n'
        '    likedAdded:"Beğendiklerinize eklendi ✓", likedRemoved:"Beğendiklerinizden çıkarıldı",\n'
        '    jobPage:"İlan sayfası",'),
 'en': ('    delYes:"Yes, delete", delNo:"Cancel",',
        '\n    pasandide:"Liked", like:"Like", liked:"Liked ✓",\n'
        '    unlike:"Remove from liked", likedEmpty:"You have not liked any job yet",\n'
        '    likedAdded:"Added to liked ✓", likedRemoved:"Removed from liked",\n'
        '    jobPage:"Job page",'),
}
for lang, (anchor, add) in NEW.items():
    assert s.count(anchor) == 1, (lang, s.count(anchor))
    s = s.replace(anchor, anchor + add, 1)

# ═════════════════════════════════════════════════════════════
# ۳) انتخاب زبان: افزودن عربی
# ═════════════════════════════════════════════════════════════
old_pick = """      <button class="langbtn" onclick="setLang('en')"><span class="flag">🇬🇧</span><span><b>English</b><span>English</span></span><span class="chev">›</span></button>"""
new_pick = old_pick + """
      <button class="langbtn" onclick="setLang('ar')"><span class="flag">🇸🇦</span><span><b>العربية</b><span>Arabic</span></span><span class="chev">›</span></button>"""
assert old_pick in s
s = s.replace(old_pick, new_pick, 1)

old_chips = '${["fa","tr","en"].map(l=>`<button class="chip ${S.lang===l?"on":""}" onclick="setLang(\'${l}\')">${l.toUpperCase()}</button>`).join("")}'
new_chips = '${["fa","tr","en","ar"].map(l=>`<button class="chip ${S.lang===l?"on":""}" onclick="setLang(\'${l}\')">${l.toUpperCase()}</button>`).join("")}'
assert old_chips in s
s = s.replace(old_chips, new_chips, 1)

# ═════════════════════════════════════════════════════════════
# ۴) وضعیت پسندیده + ذخیره/بازیابی
# ═════════════════════════════════════════════════════════════
old_state = "  jobs:[], people:[], applied:[], toast:null"
assert old_state in s
s = s.replace(old_state, "  jobs:[], people:[], applied:[], liked:[], viewJob:null, jobFrom:\"home\", viewJobAdmin:false, toast:null", 1)

old_p = '  store.set("kb_applied", JSON.stringify(S.applied));'
assert old_p in s
s = s.replace(old_p, old_p + '\n  store.set("kb_liked", JSON.stringify(S.liked));', 1)

old_r = '  try{ const a=store.get("kb_applied"); if(a) S.applied=JSON.parse(a); }catch(e){}'
assert old_r in s
s = s.replace(old_r, old_r + '\n  try{ const l=store.get("kb_liked"); if(l) S.liked=JSON.parse(l); }catch(e){}', 1)

# ═════════════════════════════════════════════════════════════
# ۵) کارت آگهی: نشان پسندیده + کلیک = صفحه‌ی کامل
# ═════════════════════════════════════════════════════════════
old_card = "function jobCard(j){\n  const isNew = (Date.now()-new Date(j.publishedAt).getTime()) < 24*H;\n  const applied = S.applied.includes(j.id);"
assert old_card in s
s = s.replace(old_card, "function jobCard(j){\n  const isNew = (Date.now()-new Date(j.publishedAt).getTime()) < 24*H;\n  const applied = S.applied.includes(j.id);\n  const liked = (S.liked||[]).includes(j.id);   // #28", 1)

old_badge = """      ${applied?`<span class="badge b-ref">${t("applied")}</span>`:""}"""
assert old_badge in s
s = s.replace(old_badge, """      ${liked?`<span class="badge b-like">❤ ${t("pasandide")}</span>`:""}
      ${applied?`<span class="badge b-ref">${t("applied")}</span>`:""}""", 1)

# ═════════════════════════════════════════════════════════════
# ۶) تابع بازکردن آگهی → صفحه‌ی کامل
# ═════════════════════════════════════════════════════════════
old_open = """function openJobSheet(id){
  const j=S.jobs.find(x=>x.id===id); if(!j) return;
  S.openJob=id; renderOverlay();
}"""
assert old_open in s
new_open = """/* #28: هر آگهی در یک «صفحه‌ی کامل» باز می‌شود (نه برگه‌ی پایین) */
function backArrow(){ return (I18N[S.lang||"fa"].dir==="rtl")?"→":"←"; }
function openJobPage(id, from){
  const j=S.jobs.find(x=>x.id===id); if(!j) return;
  S.viewJob=id; S.jobFrom=from||S.screen||"home"; go("job");
}
function openJobSheet(id){ openJobPage(id); }                 // سازگاری با نام قدیمی
function openJobAsAdmin(id){ S.viewJobAdmin=true; openJobPage(id,"admin"); }
function jobBack(){ S.viewJobAdmin=false; go(S.jobFrom||"home"); }
/** صفحه‌ی کامل اطلاعات آگهی — همان اطلاعات کامل: مشخصات، رسانه، حقوق، شرایط، توضیحات، تماس */
function screenJobView(){
  const j=S.jobs.find(x=>x.id===S.viewJob);
  if(!j) return shell({body:`<div class="empty"><div class="big">📭</div><b>${t("noJobs")}</b></div>`,tabbar:true});
  const applied=S.applied.includes(j.id), liked=(S.liked||[]).includes(j.id), adminView=!!S.viewJobAdmin;
  const body=`
  <div class="kb-head" style="background:linear-gradient(135deg,#19CBAD,#10B39F 45%,#0A7F91);padding:16px;margin:-14px -16px 14px;color:#fff">
    <div style="display:flex;align-items:center;gap:11px">
      <button class="iconbtn" onclick="jobBack()">${backArrow()}</button>
      <div class="logo" style="width:48px;height:48px;flex:0 0 48px;font-size:19px">${titleIco(j.titleKey)}</div>
      <div style="flex:1;min-width:0">
        <div style="font-size:10.5px;font-weight:800;opacity:.85;letter-spacing:.4px">📄 ${t("jobPage")}</div>
        <div style="font-size:16px;font-weight:900;margin-top:2px">${jobTitle(j.titleKey)}</div>
        <div style="font-size:11.5px;opacity:.92;margin-top:3px">🏢 ${esc(j.company)}</div>
      </div>
    </div>
    <div style="display:flex;gap:6px;flex-wrap:wrap;margin-top:11px">
      <span class="tag2 g">📍 ${j.city}${j.district?" / "+j.district:""}</span>
      <span class="tag2 p">🕒 ${t(j.contract)}</span>
      <span class="tag2">🗓 ${agoFmt(j.publishedAt)}</span>
      ${liked?`<span class="tag2" style="background:rgba(255,255,255,.9);color:#C2185B">❤ ${t("pasandide")}</span>`:""}
    </div>
  </div>

  <div class="stats" style="margin:0 0 6px">
    <div class="card" style="padding:10px;text-align:center"><b style="display:block;font-size:14px;color:var(--brand-d)">${money(j.salary)}</b><span class="small muted">${t("salary")}</span></div>
    <div class="card" style="padding:10px;text-align:center"><b style="display:block;font-size:13px">${j.district||j.city}</b><span class="small muted">${t("districtL")}</span></div>
    <div class="card" style="padding:10px;text-align:center"><b style="display:block;font-size:13px">${agoFmt(j.publishedAt)}</b><span class="small muted">${t("date")}</span></div>
  </div>

  ${mediaGallery(j)}

  <div style="margin-top:16px"><div class="sec-title"><span class="dot"></span>${t("desc")}</div>
    <p style="font-size:13.5px;line-height:2.05;color:var(--ink2);margin:0">${esc(j.desc||"")}</p></div>

  <div style="margin-top:16px"><div class="sec-title"><span class="dot"></span>${t("req")}</div>
    <ul style="margin:0;padding-inline-start:20px;font-size:13.5px;line-height:2.2;color:var(--ink2)">
      ${(j.reqs||[]).map(r=>`<li>${esc(r)}</li>`).join("")}</ul></div>

  <div style="margin-top:16px"><div class="sec-title"><span class="dot"></span>${t("cond")}</div>
    <div class="chips">${(j.cond||[]).map(c=>`<span class="chip on">${t(c)}</span>`).join("")}</div></div>

  <div style="margin-top:16px"><div class="sec-title"><span class="dot"></span>${t("contact")}</div>
    <div class="card" style="box-shadow:none;background:#F7FBFC">
      <div class="kv"><span>${t("company")}</span><b>${esc(j.company)}</b></div>
      <div class="kv"><span>${t("phoneL")}</span><b dir="ltr">+90 212 000 00 00</b></div>
    </div></div>

  <div class="gap"></div>
  ${adminView?`<button class="btn btn-ghost" onclick="jobBack()">${t("adm_back")}</button>`:`
    <button class="btn ${applied?"btn-ghost":"btn-primary"}" onclick="applyJob('${j.id}')">${applied?t("applied"):"📨 "+t("apply")}</button>
    <div style="height:9px"></div>
    <button class="btn ${liked?"btn-ghost":"btn-accent"}" onclick="toggleLike('${j.id}')">❤ ${liked?t("unlike"):t("like")}</button>`}
  `;
  return shell({body,tabbar:true});
}"""
s = s.replace(old_open, new_open, 1)

# نقشه‌ی صفحه‌ها
old_map = "    home:screenHome,profile:screenProfile,contact:screenContact,admin:screenAdmin,"
assert old_map in s
s = s.replace(old_map, old_map + "\n    job:screenJobView,   // #28", 1)

# ═════════════════════════════════════════════════════════════
# ۷) برگه‌ی پایین آگهی حذف شد (جایش صفحه‌ی کامل آمد)
# ═════════════════════════════════════════════════════════════
i = s.index("  if(S.openJob){ const j=S.jobs.find(x=>x.id===S.openJob);")
j = s.index("  if(S.confirm){", i)
removed = j - i
s = s[:i] + s[j:]

# applyJob دیگر برگه را نمی‌بندد (صفحه می‌ماند)
old_apply = 'function applyJob(id){ if(!S.applied.includes(id)) S.applied.push(id); persist(); closeSheet(); renderKeep(); toast("✅ "+t("applied")); }'
assert old_apply in s
s = s.replace(old_apply, 'function applyJob(id){ if(!S.applied.includes(id)) S.applied.push(id); persist(); renderKeep(); toast("✅ "+t("applied")); }\n'
  '/** #28: پسندیدن / برداشتن از پسندیده */\n'
  'function toggleLike(id){\n'
  '  if(!S.liked) S.liked=[];\n'
  '  const k=S.liked.indexOf(id);\n'
  '  if(k<0){ S.liked.push(id); persist(); toast("❤ "+t("likedAdded")); }\n'
  '  else { S.liked.splice(k,1); persist(); toast(t("likedRemoved")); }\n'
  '  renderKeep();\n'
  '}\n'
  'function unlikeJob(id){ S.liked=(S.liked||[]).filter(x=>x!==id); persist(); toast(t("likedRemoved")); renderKeep(); }', 1)

# ═════════════════════════════════════════════════════════════
# ۸) بخش «پسندیده» در پروفایل متقاضی
# ═════════════════════════════════════════════════════════════
anchor_set = """  <div class="card">
    <div class="sec-title"><span class="dot"></span>${t("settings")}</div>"""
assert anchor_set in s
liked_card = """  <div class="card" id="likedSec">
    <div class="sec-title"><span class="dot"></span>❤ ${t("pasandide")} (${num((S.liked||[]).length)})</div>
    ${(S.liked||[]).map(id=>S.jobs.find(x=>x.id===id)).filter(Boolean).map(j=>`
      <div class="lk-row" onclick="openJobPage('${j.id}','profile')">
        <div class="logo" style="width:38px;height:38px;flex:0 0 38px;font-size:15px">${titleIco(j.titleKey)}</div>
        <div style="flex:1;min-width:0">
          <b style="font-size:13.5px">${jobTitle(j.titleKey)}</b>
          <div class="small muted" style="margin-top:3px">🏢 ${esc(j.company)} · 📍 ${j.city}${j.district?" / "+j.district:""}</div>
          <div class="mini" style="margin-top:4px">💵 ${money(j.salary)}</div>
        </div>
        <button type="button" class="iconbtn" title="${t("unlike")}" onclick="event.stopPropagation();unlikeJob('${j.id}')">✕</button>
      </div>`).join("") || `<div class="empty" style="padding:14px"><div class="big">❤</div><b>${t("likedEmpty")}</b></div>`}
  </div>

""" + anchor_set
s = s.replace(anchor_set, liked_card, 1)

# ═════════════════════════════════════════════════════════════
# ۹) کلیک روی ردیف آگهی در پنل مدیریت = باز شدن صفحه‌ی کامل
# ═════════════════════════════════════════════════════════════
old_row = """    <div class="card" style="padding:13px">
      <div style="display:flex;gap:11px;align-items:flex-start">
        <div class="logo" style="width:40px;height:40px;flex:0 0 40px;font-size:15px">${titleIco(j.titleKey)}</div>"""
assert old_row in s
s = s.replace(old_old := old_row, """    <div class="card" style="padding:13px;cursor:pointer" onclick="openJobAsAdmin('${j.id}')">
      <div style="display:flex;gap:11px;align-items:flex-start">
        <div class="logo" style="width:40px;height:40px;flex:0 0 40px;font-size:15px">${titleIco(j.titleKey)}</div>""", 1)

# ═════════════════════════════════════════════════════════════
# ۱۰) CSS تازه + کم‌رنگ‌کردن powered by
# ═════════════════════════════════════════════════════════════
css_anchor = "/* ---------- v3.3: خط اعتبار (فوتر) ---------- */"
assert css_anchor in s
new_css = """/* ---------- #28: پسندیده (Liked) ---------- */
.badge.b-like{background:linear-gradient(135deg,#FFE3ED,#FFC8DC);color:#B3155A;font-weight:900}
.lk-row{
  display:flex; gap:11px; align-items:center; padding:10px; border-radius:16px; cursor:pointer;
  background:linear-gradient(150deg,rgba(255,255,255,.86),rgba(255,255,255,.6));
  border:1px solid rgba(255,255,255,.9); box-shadow:0 10px 22px -18px rgba(7,34,45,.5);
  margin-bottom:9px; transition:transform .12s ease;
}
.lk-row:hover{transform:translateY(-2px)}
.lk-row:active{transform:scale(.995)}
.lk-row .iconbtn{width:30px;height:30px;font-size:13px;flex:0 0 30px}

""" + css_anchor
s = s.replace(css_anchor, new_css, 1)

old_credit = """  font:800 11.5px/1.6 "Segoe UI",Tahoma,sans-serif; letter-spacing:.4px; color:#5B6B78;
  background:linear-gradient(150deg,rgba(255,255,255,.74) 0%,rgba(255,255,255,.46) 100%);
  -webkit-backdrop-filter:blur(12px) saturate(150%); backdrop-filter:blur(12px) saturate(150%);
  border:1px solid rgba(255,255,255,.8);
  box-shadow:0 16px 30px -24px rgba(7,34,45,.55);"""
assert old_credit in s
new_credit = """  font:600 9.5px/1.7 "Segoe UI",Tahoma,sans-serif; letter-spacing:.3px; color:rgba(91,107,120,.5);   /* #28: کم‌رنگ‌تر و کم‌تر */
  background:linear-gradient(150deg,rgba(255,255,255,.42) 0%,rgba(255,255,255,.22) 100%);
  -webkit-backdrop-filter:blur(10px) saturate(130%); backdrop-filter:blur(10px) saturate(130%);
  border:1px solid rgba(255,255,255,.55);
  box-shadow:0 12px 24px -22px rgba(7,34,45,.35); opacity:.72;"""
s = s.replace(old_credit, new_credit, 1)

old_foot = """    <div style="color:rgba(255,255,255,.75);font-size:11px;text-align:center;margin-top:26px;line-height:1.9">
      Karbama · İş bulma platformu<br>نسخه دمو ۱٫۰<br><span dir="ltr">powered by naser tavakkoli @nt.galaxy</span>
    </div>"""
assert old_foot in s
new_foot = """    <div style="color:rgba(255,255,255,.42);font-size:10px;text-align:center;margin-top:26px;line-height:1.85">
      Karbama · İş bulma platformu<br>نسخه دمو ۱٫۰<br><span dir="ltr" style="font-size:9.5px;opacity:.62;letter-spacing:.3px">powered by naser tavakkoli @nt.galaxy</span>
    </div>"""
s = s.replace(old_foot, new_foot, 1)

DEMO.write_text(s)
print(f'حذف برگه‌ی آگهی: {removed} کاراکتر')
print('demo:', n0, '->', len(s))
