#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""#26 دمو: ۱) دکمه‌ی افزودن آگهی ثابت (sticky، همیشه دیده می‌شود)  ۲) پنجره‌ی «مطمئن هستید؟» قبل از حذف آگهی"""
import pathlib

SRC = pathlib.Path('/home/user/karbama-demo.html')
s = SRC.read_text(); n0 = len(s)

def rep(old, new, expect=1):
    global s
    c = s.count(old)
    assert c == expect, f"expected {expect}, found {c}: {old[:90]!r}"
    s = s.replace(old, new, expect)

# ==================================================================
# ۱) دکمه‌ی شناور افزودن آگهی → چسبیده (همیشه روی صفحه)
# ==================================================================
rep('''.fab{
  position:absolute; inset-inline-end:18px; bottom:18px; width:56px;height:56px;border-radius:19px;border:0;cursor:pointer;
  background:linear-gradient(180deg,#FBBF24,#F59E0B); color:#3d2606; font-size:26px; font-weight:900;
  box-shadow:0 14px 30px -8px rgba(245,158,11,.85); z-index:40; display:flex;align-items:center;justify-content:center
}''',
'''/* #26: دکمه‌ی افزودن آگهی ثابت می‌ماند و با اسکرول پایین نمی‌رود */
.fab{
  position:sticky; bottom:18px; width:56px;height:56px;border-radius:19px;border:0;cursor:pointer;
  margin-inline-start:auto; margin-inline-end:18px;
  background:linear-gradient(180deg,#FBBF24,#F59E0B); color:#3d2606; font-size:26px; font-weight:900;
  box-shadow:0 14px 30px -8px rgba(245,158,11,.85); z-index:40; display:flex;align-items:center;justify-content:center
}''')

# ==================================================================
# ۲) کلیدهای تازه‌ی پنجره‌ی حذف (سه زبان)
# ==================================================================
rep('    delConfirm:"این آگهی حذف شود؟", saved:"ذخیره شد", deleted:"حذف شد",',
    '''    delConfirm:"این آگهی حذف شود؟", saved:"ذخیره شد", deleted:"حذف شد",
    delConfirmTitle:"حذف آگهی", delSure:"آیا مطمئن هستید؟ این آگهی برای همیشه حذف می‌شود و برگشتی ندارد.",
    delYes:"بله، حذف شود", delNo:"انصراف",''')
rep('    delConfirm:"Bu ilan silinsin mi?", saved:"Kaydedildi", deleted:"Silindi",',
    '''    delConfirm:"Bu ilan silinsin mi?", saved:"Kaydedildi", deleted:"Silindi",
    delConfirmTitle:"İlanı sil", delSure:"Emin misiniz? Bu ilan kalıcı olarak silinir ve geri alınamaz.",
    delYes:"Evet, sil", delNo:"Vazgeç",''')
rep('    delConfirm:"Delete this listing?", saved:"Saved", deleted:"Deleted",',
    '''    delConfirm:"Delete this listing?", saved:"Saved", deleted:"Deleted",
    delConfirmTitle:"Delete listing", delSure:"Are you sure? This listing will be deleted permanently and cannot be undone.",
    delYes:"Yes, delete", delNo:"Cancel",''')

# ==================================================================
# ۳) askConfirm با عنوان و زیرنویس
# ==================================================================
rep('''function askConfirm(msg, onYes){
  S.confirm={ msg:msg, onYes:onYes };
  renderOverlay();
}''',
'''function askConfirm(msg, onYes, opts){
  S.confirm={ msg:msg, onYes:onYes, title:(opts&&opts.title)||"", subject:(opts&&opts.subject)||"", ico:(opts&&opts.ico)||"🗑" };
  renderOverlay();
}''')

rep('''function delJob(id){
  askConfirm(t("delConfirm"), function(){
    S.jobs=S.jobs.filter(j=>j.id!==id); persist(); toast("🗑 "+t("deleted")); render();
  });
}''',
'''function delJob(id){
  const j=S.jobs.find(x=>x.id===id); if(!j) return;
  // #26: اول می‌پرسد «مطمئن هستید؟» و فقط با تأیید پاک می‌شود
  askConfirm(t("delSure"), function(){
    S.jobs=S.jobs.filter(x=>x.id!==id); persist(); toast("🗑 "+t("deleted")); render();
  }, { title:t("delConfirmTitle"), ico:"🗑", subject:jobTitle(j.titleKey)+" · "+esc(j.company) });
}''')

# ==================================================================
# ۴) ظاهر پنجره‌ی تأیید: عنوان + نام آگهی + متن هشدار + دو دکمه
# ==================================================================
rep('''  if(S.confirm){
    html+=`<div class="sheetwrap" style="align-items:center;padding:22px;z-index:95" onclick="if(event.target===this)closeConfirm()">
      <div style="background:#fff;border-radius:22px;padding:20px;width:100%;max-width:360px;box-shadow:var(--sh);animation:up .2s ease">
        <div style="text-align:center;font-size:34px;margin-bottom:6px">🗑</div>
        <div style="font-size:14.5px;font-weight:800;text-align:center;line-height:1.9;margin-bottom:16px">${esc(S.confirm.msg)}</div>
        <div class="row">
          <button class="btn btn-ghost" style="flex:1" onclick="closeConfirm()">${t("cancel")}</button>
          <button class="btn" style="flex:1;background:var(--danger);color:#fff" onclick="confirmYes()">${t("yes")}</button>
        </div>
      </div>
    </div>`;
  }''',
'''  if(S.confirm){
    html+=`<div class="sheetwrap" style="align-items:center;padding:22px;z-index:95" onclick="if(event.target===this)closeConfirm()">
      <div class="cfm-box">
        <div class="cfm-ico">${S.confirm.ico||"🗑"}</div>
        ${S.confirm.title?`<div class="cfm-title">${esc(S.confirm.title)}</div>`:""}
        ${S.confirm.subject?`<div class="cfm-sub" dir="auto">${S.confirm.subject}</div>`:""}
        <div class="cfm-msg">${esc(S.confirm.msg)}</div>
        <div class="row">
          <button class="btn btn-ghost" style="flex:1" onclick="closeConfirm()">${esc(S.confirm.no||t("delNo"))}</button>
          <button class="btn cfm-danger" style="flex:1" onclick="confirmYes()">${esc(S.confirm.yes||t("delYes"))}</button>
        </div>
      </div>
    </div>`;
  }''')

# CSS پنجره‌ی تأیید
rep('''/* ---------- #25: دکمه‌ی خروج زیبا در نوار بالای پنل مدیریت ---------- */''',
'''/* ---------- #26: پنجره‌ی «مطمئن هستید؟» ---------- */
.cfm-box{
  background:linear-gradient(180deg,#fff,#FBFDFE); border-radius:22px; padding:20px;
  width:100%; max-width:360px; box-shadow:var(--sh); animation:up .2s ease;
  border:1px solid rgba(255,255,255,.9);
}
.cfm-ico{
  width:54px;height:54px;margin:0 auto 10px;border-radius:18px;font-size:26px;
  display:flex;align-items:center;justify-content:center;
  background:var(--danger-l,#FEF0F0); color:var(--danger);
}
.cfm-title{ font-size:15.5px; font-weight:900; text-align:center; color:var(--ink); margin-bottom:8px; }
.cfm-sub{
  font-size:13px; font-weight:800; color:var(--brand-d); text-align:center;
  background:rgba(16,179,159,.10); border:1px solid rgba(16,179,159,.22);
  border-radius:12px; padding:8px 10px; margin-bottom:12px; line-height:1.8;
}
.cfm-msg{ font-size:13px; font-weight:700; color:#3B4753; text-align:center; line-height:1.95; margin-bottom:16px; }
.cfm-danger{ background:linear-gradient(180deg,#F2605F,#E5484D) !important; color:#fff !important; box-shadow:0 12px 24px -14px rgba(229,72,77,.95) !important; }

/* ---------- #25: دکمه‌ی خروج زیبا در نوار بالای پنل مدیریت ---------- */''')

SRC.write_text(s)
print(f"demo #26 patched: {n0} → {len(s)} chars")
