#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""#22 دمو: پسورد پنهان با دکمه‌ی نمایش + ورود دومرحله‌ای (TOTP)"""
import pathlib, shutil

SRC = pathlib.Path('/home/user/karbama-demo.html')
BAK = pathlib.Path('/tmp/demo-pre-2fa.html')
if not BAK.exists():
    shutil.copy(SRC, BAK)
s = SRC.read_text()
n0 = len(s)

def rep(old, new, expect=1):
    global s
    c = s.count(old)
    assert c == expect, f"expected {expect}, found {c}: {old[:80]!r}"
    s = s.replace(old, new, expect)

# ==================================================================
# ۱) CSS
# ==================================================================
CSS = '''
/* ---------- #22: نمایش رمز و ورود دومرحله‌ای ---------- */
.pw-wrap{ position:relative; display:block; }
.pw-wrap > input{ padding-inline-end:47px !important; }
.pw-eye{
  position:absolute; inset-inline-end:6px; top:50%; transform:translateY(-50%);
  width:38px; height:38px; border:0; border-radius:12px; cursor:pointer;
  background:rgba(16,179,159,.10); color:#0A7F91; font-size:17px; line-height:1;
  display:flex; align-items:center; justify-content:center; padding:0;
}
.pw-eye:hover{ background:rgba(16,179,159,.20); }
.pw-eye.on{ background:rgba(255,176,32,.22); color:#8A5B00; }
.tfa-state{ font-weight:900; font-size:13px; margin:4px 0 12px; }
.tfa-state.on{ color:#0E9F6E; }
.tfa-key{
  display:flex; align-items:center; justify-content:space-between; gap:8px;
  background:var(--field,#F3F6F8); border:1px solid var(--line,#E4EAEE);
  border-radius:14px; padding:12px 10px 12px 14px; margin:8px 0 10px;
}
.tfa-key b{ font-family:ui-monospace,Menlo,Consolas,monospace; letter-spacing:1px; font-size:14px; }
.tfa-demo{
  margin-top:14px; border:1px dashed rgba(124,92,255,.55); background:rgba(124,92,255,.07);
  border-radius:14px; padding:10px 12px;
}
.tfa-demo-tag{ font-size:11px; font-weight:800; color:#5B3FD1; margin-bottom:6px; }
.tfa-now{ display:flex; align-items:center; gap:10px; justify-content:center; }
.tfa-now b{ font-family:ui-monospace,Menlo,Consolas,monospace; font-size:24px; letter-spacing:5px; font-weight:900; color:#0A7F91; }
.tfa-left{
  min-width:30px; height:30px; border-radius:50%; display:flex; align-items:center; justify-content:center;
  font-size:12px; font-weight:900; background:rgba(16,179,159,.16); color:#0A7F91;
}
'''
rep('\n</style>', CSS + '\n</style>')

# ==================================================================
# ۲) کلیدهای ترجمه (fa / tr / en)
# ==================================================================
FAQ = '''    pass_show:"نمایش رمز", pass_hide:"پنهان‌کردن رمز",
    pass_note_shown:"رمز الان روی صفحه دیده می‌شود — مراقب باشید کسی نبیند.",
    twofa_title:"ورود دومرحله‌ای (2FA)",
    twofa_hint:"با فعال‌کردن این گزینه، بعد از یوزرنیم و پسورد یک کد ۶ رقمی هم پرسیده می‌شود که برنامه‌ی Authenticator روی گوشی شما می‌سازد (Google Authenticator، Authy و…).",
    twofa_enable:"فعال‌سازی ورود دومرحله‌ای", twofa_disable:"غیرفعال‌کردن ورود دومرحله‌ای",
    twofa_scan:"این کلید را در برنامه‌ی Authenticator خودتان اضافه کنید (کد دستی):",
    twofa_code:"کد ۶ رقمی برنامه", twofa_confirm:"تأیید و فعال‌سازی",
    twofa_verify:"تأیید و ورود", twofa_on:"فعال است", twofa_off:"غیرفعال است",
    twofa_show:"نمایش", twofa_hide:"پنهان‌کردن", twofa_copy:"کپی کلید",
    twofa_recovery:"کد بازیابی (یک‌بارمصرف)",
    twofa_recovery_note:"اگر گوشی یا برنامه‌ی Authenticator را از دست دادید با این کد وارد شوید؛ بعد از استفاده، ورود دومرحله‌ای خودش خاموش می‌شود.",
    twofa_wrong:"کد واردشده درست نیست؛ کد تازه‌ی برنامه را وارد کنید",
    twofa_step_title:"تأیید دومرحله‌ای",
    twofa_step_hint:"کد ۶ رقمی برنامه‌ی Authenticator را وارد کنید (یا کد بازیابی).",
    twofa_back:"بازگشت به مرحله‌ی اول",
    twofa_demo_tag:"🔎 دمو: کد فعلی برنامه (در نسخه‌ی واقعی، کد از برنامه‌ی Authenticator گوشی خوانده می‌شود)",
    twofa_recovery_now:"کد بازیابی با موفقیت استفاده شد؛ ورود دومرحله‌ای خاموش شد",
'''
TRQ = '''    pass_show:"Şifreyi göster", pass_hide:"Şifreyi gizle",
    pass_note_shown:"Şifre şu an ekranda görünüyor — kimsenin görmemesine dikkat edin.",
    twofa_title:"İki adımlı giriş (2FA)",
    twofa_hint:"Bu açıkken kullanıcı adı ve şifreden sonra, Authenticator uygulamanızın ürettiği 6 haneli kod da sorulur (Google Authenticator, Authy vb.).",
    twofa_enable:"İki adımlı girişi aç", twofa_disable:"İki adımlı girişi kapat",
    twofa_scan:"Bu anahtarı Authenticator uygulamanıza elle ekleyin:",
    twofa_code:"Uygulamadaki 6 haneli kod", twofa_confirm:"Onayla ve aç",
    twofa_verify:"Doğrula ve gir", twofa_on:"Açık", twofa_off:"Kapalı",
    twofa_show:"Göster", twofa_hide:"Gizle", twofa_copy:"Anahtarı kopyala",
    twofa_recovery:"Kurtarma kodu (tek kullanımlık)",
    twofa_recovery_note:"Telefonu veya Authenticator uygulamasını kaybederseniz bu kodla girin; kullanıldıktan sonra iki adımlı giriş kapanır.",
    twofa_wrong:"Kod yanlış; uygulamadaki yeni kodu girin",
    twofa_step_title:"İki adımlı doğrulama",
    twofa_step_hint:"Authenticator uygulamasındaki 6 haneli kodu girin (veya kurtarma kodunu).",
    twofa_back:"İlk adıma dön",
    twofa_demo_tag:"🔎 Demo: uygulamadaki geçerli kod (gerçek sürümde kod telefondaki Authenticator uygulamasından okunur)",
    twofa_recovery_now:"Kurtarma kodu kullanıldı; iki adımlı giriş kapatıldı",
'''
ENQ = '''    pass_show:"Show password", pass_hide:"Hide password",
    pass_note_shown:"The password is visible on screen right now — make sure nobody is watching.",
    twofa_title:"Two-step sign-in (2FA)",
    twofa_hint:"When this is on, a 6-digit code from your authenticator app is asked after the username and password (Google Authenticator, Authy, …).",
    twofa_enable:"Turn on two-step sign-in", twofa_disable:"Turn off two-step sign-in",
    twofa_scan:"Add this key to your authenticator app (manual entry):",
    twofa_code:"6-digit app code", twofa_confirm:"Confirm and enable",
    twofa_verify:"Verify and sign in", twofa_on:"Enabled", twofa_off:"Disabled",
    twofa_show:"Show", twofa_hide:"Hide", twofa_copy:"Copy key",
    twofa_recovery:"Recovery code (one-time)",
    twofa_recovery_note:"If you lose the phone or the authenticator app, sign in with this code; after it is used, two-step sign-in turns off.",
    twofa_wrong:"That code is wrong; enter the fresh code from the app",
    twofa_step_title:"Two-step verification",
    twofa_step_hint:"Enter the 6-digit code from your authenticator app (or the recovery code).",
    twofa_back:"Back to step one",
    twofa_demo_tag:"🔎 Demo: current app code (in the real build the code is read from the authenticator app on the phone)",
    twofa_recovery_now:"Recovery code used; two-step sign-in has been turned off",
'''
rep('    adm_empty:"یوزرنیم و پسورد نمی‌توانند خالی باشند", adm_saved:"یوزرنیم و پسورد مدیر ذخیره شد",\n',
    '    adm_empty:"یوزرنیم و پسورد نمی‌توانند خالی باشند", adm_saved:"یوزرنیم و پسورد مدیر ذخیره شد",\n' + FAQ)
rep('    adm_empty:"Kullanıcı adı ve şifre boş olamaz", adm_saved:"Yönetici bilgileri kaydedildi",\n',
    '    adm_empty:"Kullanıcı adı ve şifre boş olamaz", adm_saved:"Yönetici bilgileri kaydedildi",\n' + TRQ)
rep('    adm_empty:"Username and password cannot be empty", adm_saved:"Admin credentials saved",\n',
    '    adm_empty:"Username and password cannot be empty", adm_saved:"Admin credentials saved",\n' + ENQ)

# ==================================================================
# ۳) state + persist/restore
# ==================================================================
rep('''  adminCred:{ user:"admin", pass:"karbama2026" },                    // یوزرنیم و پسورد مدیر (در تنظیمات قابل تغییر)''',
    '''  adminCred:{ user:"admin", pass:"karbama2026" },                    // یوزرنیم و پسورد مدیر (در تنظیمات قابل تغییر)
  twoFA:{ on:false, secret:"", recovery:"", pending:false },          // #22: ورود دومرحله‌ای
  tfaShown:false, tfaStage:false, tfaErr:false,                       // #22''')
rep('  store.set("kb_admin", JSON.stringify(S.adminCred));',
    '''  store.set("kb_admin", JSON.stringify(S.adminCred));
  store.set("kb_2fa", JSON.stringify(S.twoFA));''')
rep('''  if(!S.broker.card) S.broker.card=DEFAULT_CARD;   // شماره کارت ثابت پیش‌فرض''',
    '''  try{ const f=store.get("kb_2fa"); if(f){ const o=JSON.parse(f);
        S.twoFA={ on:!!o.on, secret:o.secret||"", recovery:o.recovery||"", pending:!!o.pending }; } }catch(e){}
  if(!S.broker.card) S.broker.card=DEFAULT_CARD;   // شماره کارت ثابت پیش‌فرض''')

# ==================================================================
# ۴) go() — ریست مرحله‌ی دوم
# ==================================================================
rep('function go(s){ S.screen=s; S.adminErr=false; render(); V().scrollTop=0; }',
    '''function go(s){
  S.screen=s; S.adminErr=false;
  if(s!=="login"){ S.tfaStage=false; S.tfaErr=false; }   // #22: خروج از مرحله‌ی دوم
  render(); V().scrollTop=0;
}''')

# ==================================================================
# ۵) توابع کمکی (#22) — قبل از clock
# ==================================================================
HELPERS = r'''
/* ---------- #22: نمایش/پنهان‌کردن پسورد ---------- */
function pwWrap(inner){
  return '<div class="pw-wrap">' + inner +
    '<button type="button" class="pw-eye" onclick="pwToggle(this)" title="' + t("pass_show") +
    '" aria-label="' + t("pass_show") + '">👁️</button></div>';
}
function pwToggle(btn){
  if(!btn) return;
  const w=btn.closest(".pw-wrap"); if(!w) return;
  const i=w.querySelector("input"); if(!i) return;
  const show=(i.type==="password");
  i.type = show ? "text" : "password";
  btn.textContent = show ? "🙈" : "👁️";
  btn.classList.toggle("on", show);
  const lbl=t(show?"pass_hide":"pass_show");
  btn.setAttribute("title",lbl); btn.setAttribute("aria-label",lbl);
  if(show) toast("⚠️ "+t("pass_note_shown"));
}
function kbCopy(text, msg){
  const ok=()=>toast(msg||"📋 کپی شد");
  try{
    if(navigator.clipboard && navigator.clipboard.writeText){
      navigator.clipboard.writeText(text).then(ok).catch(()=>copyFallback(text,ok)); return;
    }
  }catch(e){}
  copyFallback(text, ok);
}

/* ---------- #22: TOTP (RFC 6238) — همان الگوریتم نسخه‌ی اندروید ---------- */
const B32A="ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
function rotl(x,n){ return ((x<<n)|(x>>>(32-n)))>>>0; }
function b32encode(bytes){
  let bits=0, val=0, out="";
  for(const b of bytes){ val=(val<<8)|b; bits+=8;
    while(bits>=5){ out+=B32A[(val>>>(bits-5))&31]; bits-=5; } }
  if(bits>0) out+=B32A[(val<<(5-bits))&31];
  return out;
}
function b32decode(str){
  const t=String(str||"").toUpperCase().replace(/[^A-Z2-7]/g,""); let bits=0, val=0; const out=[];
  for(const c of t){ const i=B32A.indexOf(c); if(i<0) continue; val=(val<<5)|i; bits+=5;
    if(bits>=8){ out.push((val>>>(bits-8))&255); bits-=8; } }
  return out;
}
function sha1bytes(bytes){
  const ml=bytes.length*8, msg=bytes.slice(); msg.push(0x80);
  while(msg.length%64!==56) msg.push(0);
  const hi=Math.floor(ml/4294967296), lo=ml>>>0;
  msg.push((hi>>>24)&255,(hi>>>16)&255,(hi>>>8)&255,hi&255,(lo>>>24)&255,(lo>>>16)&255,(lo>>>8)&255,lo&255);
  let h0=0x67452301,h1=0xEFCDAB89,h2=0x98BADCFE,h3=0x10325476,h4=0xC3D2E1F0;
  const w=new Array(80);
  for(let i=0;i<msg.length;i+=64){
    for(let j=0;j<16;j++) w[j]=((msg[i+j*4]<<24)|(msg[i+j*4+1]<<16)|(msg[i+j*4+2]<<8)|msg[i+j*4+3])>>>0;
    for(let j=16;j<80;j++) w[j]=rotl(w[j-3]^w[j-8]^w[j-14]^w[j-16],1);
    let a=h0,b=h1,c=h2,d=h3,e=h4;
    for(let j=0;j<80;j++){
      let f,k;
      if(j<20){ f=(b&c)|((~b)&d); k=0x5A827999; }
      else if(j<40){ f=b^c^d; k=0x6ED9EBA1; }
      else if(j<60){ f=(b&c)|(b&d)|(c&d); k=0x8F1BBCDC; }
      else { f=b^c^d; k=0xCA62C1D6; }
      const t=(rotl(a,5)+f+e+k+w[j])>>>0;
      e=d; d=c; c=rotl(b,30); b=a; a=t;
    }
    h0=(h0+a)>>>0; h1=(h1+b)>>>0; h2=(h2+c)>>>0; h3=(h3+d)>>>0; h4=(h4+e)>>>0;
  }
  const out=[];
  for(const h of [h0,h1,h2,h3,h4]) out.push((h>>>24)&255,(h>>>16)&255,(h>>>8)&255,h&255);
  return out;
}
function hmacSha1(keyBytes, msgBytes){
  let k=keyBytes.slice();
  if(k.length>64) k=sha1bytes(k);
  while(k.length<64) k.push(0);
  const o=[], ip=[];
  for(let i=0;i<64;i++){ o.push(k[i]^0x5c); ip.push(k[i]^0x36); }
  return sha1bytes(o.concat(sha1bytes(ip.concat(msgBytes))));
}
function totpCode(secret, timeMs, offsetSteps){
  const step=Math.floor(timeMs/1000/30)+(offsetSteps||0);
  const msg=[0,0,0,0,(step>>>24)&255,(step>>>16)&255,(step>>>8)&255,step&255];
  const h=hmacSha1(b32decode(secret), msg);
  const o=h[19]&0x0f;
  const bin=((h[o]&0x7f)<<24)|(h[o+1]<<16)|(h[o+2]<<8)|h[o+3];
  return String(bin%1000000).padStart(6,"0");
}
function totpVerify(secret, code){
  code=String(code||"").replace(/[^0-9]/g,"");
  if(code.length!==6 || !secret) return false;
  const now=Date.now();
  for(const off of [-1,0,1]) if(totpCode(secret, now, off)===code) return true;
  return false;
}
function totpSecret(){ const b=[]; for(let i=0;i<20;i++) b.push(Math.floor(Math.random()*256)); return b32encode(b); }
function totpPretty(str){ return (String(str||"").match(/.{1,4}/g)||[]).join(" "); }
function totpUri(secret){
  return "otpauth://totp/Karbama:admin?secret="+secret+"&issuer=Karbama&algorithm=SHA1&digits=6&period=30";
}
function recoveryCode(){
  let d=""; for(let i=0;i<8;i++) d+=String(Math.floor(Math.random()*10));
  return d.slice(0,4)+"-"+d.slice(4);
}
/* ---------- #22: عملیات ورود دومرحله‌ای ---------- */
function tfaBegin(){
  S.twoFA={ on:false, secret:totpSecret(), recovery:"", pending:true };
  S.tfaShown=true; S.tfaErr=false; persist(); render();
  toast("🔐 "+t("twofa_enable"));
}
function tfaReveal(){ S.tfaShown=!S.tfaShown; render(); }
function tfaCopySecret(){ kbCopy(totpPretty(S.twoFA.secret), "📋 "+t("twofa_copy")); }
function tfaCopyRecovery(){ kbCopy(String(S.twoFA.recovery||""), "📋 "+t("twofa_copy")); }
function tfaConfirm(){
  const el=document.getElementById("f_tfa_confirm");
  const code=String(el?el.value:"").replace(/[^0-9]/g,"");
  if(!totpVerify(S.twoFA.secret, code)){ S.tfaErr=true; render(); toast("⚠️ "+t("twofa_wrong")); return; }
  S.twoFA.on=true; S.twoFA.pending=false; S.twoFA.recovery=recoveryCode();
  S.tfaErr=false; S.tfaShown=true; persist(); render(); toast("✅ "+t("twofa_on"));
}
function tfaDisable(){
  S.twoFA={ on:false, secret:"", recovery:"", pending:false }; S.tfaShown=false;
  persist(); render(); toast("🚫 "+t("twofa_off"));
}
function tfaBack(){ S.tfaStage=false; S.tfaErr=false; render(); }
function tfaNowBox(){
  return `<div class="tfa-demo">
    <div class="tfa-demo-tag">${t("twofa_demo_tag")}</div>
    <div class="tfa-now"><b id="tfa_now" dir="ltr">------</b><span class="tfa-left" id="tfa_left">30</span></div>
  </div>`;
}
function tfaTick(){
  const n=document.getElementById("tfa_now");
  if(!n){ return; }
  const sec=(S.twoFA&&S.twoFA.secret)||(S.tfaSeed||"");
  if(!sec){ n.textContent="------"; return; }
  n.textContent=totpCode(sec, Date.now(), 0);
  const l=document.getElementById("tfa_left");
  if(l) l.textContent=num(String(30-(Math.floor(Date.now()/1000)%30)));
}
'''
rep('/* ---------- clock ---------- */', HELPERS + '\n/* ---------- clock ---------- */')
rep('setInterval(tickClock,20000);', 'setInterval(tickClock,20000);\nsetInterval(tfaTick,1000);   // #22: به‌روزرسانی کد نمایشی')

# ==================================================================
# ۶) فیلد پسورد مرحله‌ی ثبت‌نام (چشم)
# ==================================================================
rep('''        <input type="password" id="f_pass" dir="ltr" value="${esc(f.pass||"")}" oninput="upd('pass',this.value)" placeholder="••••">''',
    '''        ${pwWrap(`<input type="password" id="f_pass" dir="ltr" value="${esc(f.pass||"")}" oninput="upd('pass',this.value)" placeholder="••••">`)}''')

# ==================================================================
# ۷) صفحه‌ی ورود: چشم + مرحله‌ی دوم (2FA)
# ==================================================================
i = s.index('function screenLogin(){')
j = s.index('/* ---------- 4. success ---------- */')
NEWLOGIN = '''function screenLogin(){
  if(S.tfaStage) return screenTfa();
  return shell({body:`
  <div class="appbar" style="margin:-14px -16px 14px">
    <button class="iconbtn" onclick="go('welcome')">${S.lang==="fa"?"→":"←"}</button>
    <h3>${t("loginTitle")}</h3>
  </div>
  <div class="card" style="padding:22px 16px">
    <div style="text-align:center;font-size:38px;margin-bottom:6px">🔐</div>
    <h2 style="font-size:17px;font-weight:900;text-align:center;margin-bottom:6px">${t("loginTitle")}</h2>
    <div class="field" style="text-align:start"><label>${t("username")}</label>
      <input type="text" id="f_login_user" dir="ltr" value="${esc(S.loginUser||"")}" placeholder="${t("userPh")}"
             onkeydown="if(event.key==='Enter')doLogin()"></div>
    <div class="field" style="text-align:start"><label>${t("passPh")}</label>
      ${pwWrap(`<input type="password" id="f_login_pass" dir="ltr" placeholder="••••••" onkeydown="if(event.key==='Enter')doLogin()">`)}</div>
    ${S.loginErr?`<div class="err" style="text-align:start">${t("loginFail")}</div>`:""}
    <div class="gap"></div>
    <button class="btn btn-primary" onclick="doLogin()">🔑 ${t("login")}</button>
  </div>`});
}
/** #22: مرحله‌ی دوم ورود — کد ۶ رقمی برنامه‌ی Authenticator */
function screenTfa(){
  return shell({body:`
  <div class="appbar" style="margin:-14px -16px 14px">
    <button class="iconbtn" onclick="tfaBack()">${S.lang==="fa"?"→":"←"}</button>
    <h3>${t("twofa_step_title")}</h3>
  </div>
  <div class="card" style="padding:22px 16px">
    <div style="text-align:center;font-size:38px;margin-bottom:6px">🛡️</div>
    <h2 style="font-size:17px;font-weight:900;text-align:center;margin-bottom:6px">${t("twofa_step_title")}</h2>
    <p class="small muted" style="text-align:center;margin:0 0 14px;line-height:1.95">${t("twofa_step_hint")}</p>
    <div class="field" style="text-align:start;margin-bottom:0"><label>${t("twofa_code")}</label>
      <input id="f_tfa_code" dir="ltr" inputmode="numeric" maxlength="8" placeholder="123456"
             style="text-align:center;letter-spacing:8px;font-weight:900;font-size:20px"
             oninput="this.value=this.value.replace(/[^0-9]/g,'').slice(0,8)"
             onkeydown="if(event.key==='Enter')tfaCheck()"></div>
    ${S.tfaErr?`<div class="err" style="text-align:start">${t("twofa_wrong")}</div>`:""}
    <div class="gap"></div>
    <button class="btn btn-primary" onclick="tfaCheck()">✅ ${t("twofa_verify")}</button>
    ${tfaNowBox()}
    <div style="height:9px"></div>
    <button class="btn btn-ghost" onclick="tfaBack()">↩︎ ${t("twofa_back")}</button>
  </div>`});
}

'''
s = s[:i] + NEWLOGIN + s[j:]

# ==================================================================
# ۸) doLogin — شاخه‌ی مدیر با 2FA
# ==================================================================
rep('''  if(u.toLowerCase()===AU.trim().toLowerCase() && p===AP){
    S.session=null; S.isAdmin=true; S.loginErr=false; persist(); go("admin");
    toast("🛡️ "+t("adminPanel")); return;
  }''',
    '''  if(u.toLowerCase()===AU.trim().toLowerCase() && p===AP){
    if(S.twoFA && S.twoFA.on){        // #22: مرحله‌ی دوم — کد ۶ رقمی
      S.loginErr=false; S.tfaStage=true; S.tfaErr=false; render();
      toast("🛡️ "+t("twofa_step_title")); return;
    }
    S.tfaStage=false; S.session=null; S.isAdmin=true; S.loginErr=false; persist(); go("admin");
    toast("🛡️ "+t("adminPanel")); return;
  }''')

# تابع بررسی کد — بعد از doLogin
rep('''  S.loginErr=true; renderKeep(); toast("⛔ "+t("loginFail"));
}''',
    '''  S.loginErr=true; renderKeep(); toast("⛔ "+t("loginFail"));
}
/** #22: بررسی کد مرحله‌ی دوم (کد ۶ رقمی یا کد بازیابی یک‌بارمصرف) */
function tfaCheck(){
  const el=document.getElementById("f_tfa_code");
  const code=String(el?el.value:"").replace(/[^0-9]/g,"");
  if(totpVerify(S.twoFA && S.twoFA.secret, code)){
    S.tfaStage=false; S.tfaErr=false; S.session=null; S.isAdmin=true; S.loginErr=false;
    persist(); go("admin"); toast("🛡️ "+t("adminPanel")); return;
  }
  const rec=String((S.twoFA&&S.twoFA.recovery)||"").replace(/[^0-9]/g,"");
  if(rec && code===rec){                       // کد بازیابی: یک‌بارمصرف
    S.twoFA={ on:false, secret:"", recovery:"", pending:false };
    S.tfaStage=false; S.tfaErr=false; S.session=null; S.isAdmin=true; S.loginErr=false;
    persist(); go("admin"); toast("⚠️ "+t("twofa_recovery_now")); return;
  }
  S.tfaErr=true; render(); toast("⛔ "+t("twofa_wrong"));
}''')

# ==================================================================
# ۹) تنظیمات پنل: پسورد مدیر با چشم + کارت 2FA
# ==================================================================
rep('''    <div class="field" style="margin-bottom:0"><label>${t("adm_pass")}</label>
      <input dir="ltr" autocapitalize="off" autocomplete="off" spellcheck="false"
             value="${esc((S.adminCred&&S.adminCred.pass)||"")}" oninput="S.adminCred.pass=this.value"></div>
    <div class="gap"></div>
    <button class="btn btn-primary" onclick="saveAdminCred()">💾 ${t("save")}</button>
  </div>''',
    '''    <div class="field" style="margin-bottom:0"><label>${t("adm_pass")}</label>
      ${pwWrap(`<input dir="ltr" autocapitalize="off" autocomplete="off" spellcheck="false"
             value="${esc((S.adminCred&&S.adminCred.pass)||"")}" oninput="S.adminCred.pass=this.value">`)}</div>
    <div class="gap"></div>
    <button class="btn btn-primary" onclick="saveAdminCred()">💾 ${t("save")}</button>
  </div>
  ${tfaSettingsCard()}''')

# ==================================================================
# ۱۰) کارت تنظیمات 2FA + ردیف خلاصه
# ==================================================================
rep('''  <div class="card" style="background:#F7FBFC">
    <div class="small muted" style="line-height:2.1">
      ${t("und_brokerCard")}: <b dir="ltr">${S.broker.card?num(S.broker.card):"—"}</b><br>
      ${t("adm_user")}: <b dir="ltr">${esc((S.adminCred&&S.adminCred.user)||"")}</b><br>
      ${t("und_signed")}: <b>${num(S.people.filter(p=>p.undertaking&&p.undertaking.signed).length)}</b> / ${num(S.people.length)}
    </div>
  </div>`;''',
    '''  <div class="card" style="background:#F7FBFC">
    <div class="small muted" style="line-height:2.1">
      ${t("und_brokerCard")}: <b dir="ltr">${S.broker.card?num(S.broker.card):"—"}</b><br>
      ${t("adm_user")}: <b dir="ltr">${esc((S.adminCred&&S.adminCred.user)||"")}</b><br>
      ${t("twofa_title")}: <b>${t(S.twoFA.on?"twofa_on":"twofa_off")}</b><br>
      ${t("und_signed")}: <b>${num(S.people.filter(p=>p.undertaking&&p.undertaking.signed).length)}</b> / ${num(S.people.length)}
    </div>
  </div>`;''')

rep('''function screenJobEditor(){''',
    '''/** #22: کارت ورود دومرحله‌ای در تنظیمات پنل مدیر */
function tfaSettingsCard(){
  const on=!!S.twoFA.on, pending=!!S.twoFA.pending, has=on||pending;
  const secret=String(S.twoFA.secret||"");
  return `
  <div class="card">
    <div class="sec-title"><span class="dot"></span>${t("twofa_title")}</div>
    <p class="small muted" style="margin:-4px 0 10px;line-height:1.95">${t("twofa_hint")}</p>
    <div class="tfa-state ${on?"on":""}">${on?"🟢":"⚪"} ${t(on?"twofa_on":"twofa_off")}</div>
    ${(!has)?`<button class="btn btn-primary" onclick="tfaBegin()">🔐 ${t("twofa_enable")}</button>`:``}
    ${has?`
      <div class="small muted" style="line-height:1.95">${t("twofa_scan")}</div>
      <div class="tfa-key">
        <b dir="ltr">${S.tfaShown?esc(totpPretty(secret)):"•••• •••• •••• ••••"}</b>
        <button type="button" class="pw-eye" style="position:static;transform:none" onclick="tfaReveal()"
                title="${t(S.tfaShown?"twofa_hide":"twofa_show")}">${S.tfaShown?"🙈":"👁️"}</button>
      </div>
      <div style="display:flex;gap:8px;flex-wrap:wrap;margin-bottom:10px">
        <button class="chip" onclick="tfaCopySecret()">📋 ${t("twofa_copy")}</button>
        <button class="chip" onclick="tfaReveal()">${S.tfaShown?"🙈 "+t("twofa_hide"):"👁️ "+t("twofa_show")}</button>
      </div>
      <div class="small muted" dir="ltr" style="line-height:1.7;word-break:break-all">${esc(totpUri(secret))}</div>
      ${on?`
        <div class="hr"></div>
        <div class="sec-title" style="margin-bottom:6px"><span class="dot"></span>${t("twofa_recovery")}</div>
        <div class="tfa-key">
          <b dir="ltr">${S.tfaShown?esc(String(S.twoFA.recovery||"")):"••••-••••"}</b>
          <button type="button" class="pw-eye" style="position:static;transform:none" onclick="tfaReveal()"
                  title="${t(S.tfaShown?"twofa_hide":"twofa_show")}">${S.tfaShown?"🙈":"👁️"}</button>
        </div>
        <div style="display:flex;gap:8px;flex-wrap:wrap;margin-bottom:8px">
          <button class="chip" onclick="tfaCopyRecovery()">📋 ${t("twofa_copy")}</button>
        </div>
        <p class="small muted" style="margin:0 0 12px;line-height:1.95">${t("twofa_recovery_note")}</p>
        <button class="btn btn-ghost" onclick="tfaDisable()">🚫 ${t("twofa_disable")}</button>
      `:`
        <div class="hr"></div>
        <div class="field" style="margin-bottom:0"><label>${t("twofa_code")}</label>
          <input id="f_tfa_confirm" dir="ltr" inputmode="numeric" maxlength="6" placeholder="123456"
                 style="text-align:center;letter-spacing:8px;font-weight:900;font-size:20px"
                 oninput="this.value=this.value.replace(/[^0-9]/g,'').slice(0,6)"
                 onkeydown="if(event.key==='Enter')tfaConfirm()"></div>
        ${S.tfaErr?`<div class="err" style="text-align:start">${t("twofa_wrong")}</div>`:""}
        ${tfaNowBox()}
        <div class="gap"></div>
        <button class="btn btn-primary" onclick="tfaConfirm()">✅ ${t("twofa_confirm")}</button>
      `}
    `:``}
  </div>`;
}
function screenJobEditor(){''')

SRC.write_text(s)
print(f"demo patched: {n0} → {len(s)} chars")
