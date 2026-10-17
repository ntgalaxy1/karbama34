#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""#27 دمو: دکمه‌ی «افزودن آگهی» از حالت شناور (که روی دکمه‌ی حذف می‌افتاد) به نوار بالای پنل منتقل شد
   → واقعاً ثابت، همیشه دیده می‌شود، و هیچ دکمه‌ای را نمی‌پوشاند (حذف هم بی‌مانع کار می‌کند)"""
import pathlib

SRC = pathlib.Path('/home/user/karbama-demo.html')
s = SRC.read_text(); n0 = len(s)

def rep(old, new, expect=1):
    global s
    c = s.count(old)
    assert c == expect, f"expected {expect}, found {c}: {old[:90]!r}"
    s = s.replace(old, new, expect)

# ==================================================================
# ۱) CSS: دکمه‌ی ثابت «افزودن آگهی» در نوار بالا
# ==================================================================
rep('''/* ---------- #26: پنجره‌ی «مطمئن هستید؟» ---------- */''',
'''/* ---------- #27: دکمه‌ی ثابت «افزودن آگهی» در نوار بالای پنل مدیریت ---------- */
.admin-add{
  display:inline-flex; align-items:center; justify-content:center; gap:6px; flex:0 0 auto;
  height:38px; min-width:38px; padding:0 12px; cursor:pointer;
  border:0; border-radius:14px; font-family:inherit; font-size:12.5px; font-weight:900;
  background:linear-gradient(180deg,#FFD15C,#F59E0B); color:#3D2606;
  box-shadow:0 10px 22px -12px rgba(245,158,11,.95), inset 0 1px 0 rgba(255,255,255,.55);
  transition:transform .12s, filter .16s;
}
.admin-add svg{ width:17px; height:17px; display:block; }
.admin-add:hover{ filter:brightness(1.04); }
.admin-add:active{ transform:scale(.95); }
@media (max-width:400px){ .admin-add{ padding:0 10px; } .admin-add span{ display:none; } }

/* ---------- #26: پنجره‌ی «مطمئن هستید؟» ---------- */''')

# ==================================================================
# ۲) آیکون + سازنده‌ی دکمه
# ==================================================================
rep('''const ICO_EXIT = ''', '''const ICO_PLUS = '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.6" stroke-linecap="round" aria-hidden="true"><path d="M12 5.5v13"/><path d="M5.5 12h13"/></svg>';
const ICO_EXIT = ''')

rep('''function adminExitBtn(){''',
'''/* #27: دکمه‌ی ثابت افزودن آگهی در نوار بالای پنل (جای دکمه‌ی شناور که روی «حذف» می‌افتاد) */
function adminAddBtn(){
  return '<button type="button" class="admin-add" onclick="newJob()" title="' + t("addJob") + '" aria-label="' + t("addJob") + '">' +
         ICO_PLUS + '<span>' + t("addJob") + '</span></button>';
}
function adminExitBtn(){''')

# ==================================================================
# ۳) در هر سه نوار بالای مدیریت: دکمه‌ی افزودن + دکمه‌ی خروج
# ==================================================================
rep('''    ${adminExitBtn()}
  </div>''',
'''    ${adminAddBtn()}
    ${adminExitBtn()}
  </div>''', 3)

# ==================================================================
# ۴) حذف دکمه‌ی شناور (تا هیچ دکمه‌ای را نپوشاند و کلیک‌ها درست بنشینند)
# ==================================================================
rep('''function screenAdmin(){
  const body = S.adminTab==="jobs"?adminJobs():S.adminTab==="settings"?adminSettings():adminPeople();
  return shell({body,tabbar:true,fab:S.adminTab==="jobs"?`<button class="fab" title="${t("addJob")}" onclick="newJob()">＋</button>`:""});
}''',
'''function screenAdmin(){
  const body = S.adminTab==="jobs"?adminJobs():S.adminTab==="settings"?adminSettings():adminPeople();
  // #27: بدون دکمه‌ی شناور — دکمه‌ی «افزودن آگهی» ثابت در نوار بالا نشسته است
  return shell({body,tabbar:true});
}''')

# ==================================================================
# ۵) دکمه‌های ردیف: رویداد به بیرون سرایت نکند و نوع دکمه صریح باشد
# ==================================================================
rep('''        <button class="btn btn-ghost btn-sm" style="flex:1" onclick="editJob('${j.id}')">✏️ ${t("editJob")}</button>
        <button class="btn btn-danger btn-sm" style="flex:1" onclick="delJob('${j.id}')">🗑 ${t("del")}</button>''',
'''        <button type="button" class="btn btn-ghost btn-sm" style="flex:1" onclick="event.stopPropagation();editJob('${j.id}')">✏️ ${t("editJob")}</button>
        <button type="button" class="btn btn-danger btn-sm" style="flex:1" onclick="event.stopPropagation();delJob('${j.id}')">🗑 ${t("del")}</button>''')

SRC.write_text(s)
print(f"demo #27 patched: {n0} → {len(s)} chars")
