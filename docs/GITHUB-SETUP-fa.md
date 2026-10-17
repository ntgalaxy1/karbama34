# اتصال این پروژه به GitHub

مخزن گیت **آماده و کامیت‌شده** است (شاخه‌ی `main`، ۱۷۸ فایل، ۷ مگابایت).
چون Arena کانکتور GitHub ندارد و توکن هم در دسترس نیست، تنها قدم باقی‌مانده «_push_ کردن» است که باید با حساب خودت انجام شود. سه راه دارد:

---

## روش ۱ — از روی همین فضای کاری (ساده‌ترین اگر به ترمینال دسترسی داری)

```bash
# ۱) در گیت‌هاب یک مخزن خالی بساز (بدون README و .gitignore)
#    مثلاً: https://github.com/new  →  نام: karbama  →  Private

# ۲) داخل پوشه‌ی پروژه:
git remote add origin https://github.com/<USERNAME>/karbama.git
git branch -M main
git push -u origin main
```

برای احراز هویت به‌جای رمز عبور از **Personal Access Token** استفاده کن:

```bash
# ساخت توکن: GitHub → Settings → Developer settings → Personal access tokens → Fine-grained
# دسترسی لازم: Contents = Read and write
git remote set-url origin https://<USERNAME>:<TOKEN>@github.com/<USERNAME>/karbama.git
git push -u origin main
```

---

## روش ۲ — با فایل Bundle (بدون نیاز به انتقال پوشه)

فایل `karbama-repo.bundle` (۶ مگابایت) تمام تاریخچه را در خود دارد:

```bash
# دانلود karbama-repo.bundle از فضای کاری، سپس:
git clone karbama-repo.bundle karbama
cd karbama
git remote set-url origin https://github.com/<USERNAME>/karbama.git
git push -u origin main
```

---

## روش ۳ — با GitHub Desktop / VS Code

پوشه‌ی پروژه را باز کن، «Publish repository» را بزن و حساب گیت‌هاب را وصل کن.

---

## بعد از push — دو کار مهم

### ۱) APK را به‌عنوان Release بگذار
فایل‌های APK عمداً در مخزن نیستند (باینری سنگین). آن‌ها را در بخش **Releases** آپلود کن:

```
Releases → Draft a new release → Tag: v37
  ├─ Karbama-v37.apk        (۴.۹ مگابایت — نسخه‌ی نهایی)
  └─ Karbama-v37-debug.apk  (۲۴.۹ مگابایت — نسخه‌ی آزمایشی)
```

لینک دانلود همیشگی می‌شود، مثلاً:
`https://github.com/<USERNAME>/karbama/releases/latest/download/Karbama-v37.apk`

### ۲) اگر خواستی صفحه‌ی دانلود روی GitHub Pages برود
`download/index.html` را به شاخه‌ی `gh-pages` منتقل کن و در Settings → Pages فعالش کن؛
سپس لینک‌های داخل صفحه را به آدرس Releases تغییر بده.

---

## نکته‌ی امنیتی

- توکن را **هیچ‌وقت** داخل فایل‌های مخزن نگذار (`.gitignore` از قبل جلوی APK و کش‌ها را گرفته است).
- اگر توکن را جایی لو دادی، همان‌جا Revoke کن.
- فایل `google-services.json` واقعی را کامیت نکن؛ فقط نمونه‌اش (`google-services.json.example`) در مخزن است.

---

## وضعیت فعلی مخزن

| مورد | مقدار |
|---|---|
| شاخه | `main` |
| کامیت | `feat: Karbama — اپ کاریابی ترکیه (اندروید + دموی وب)` |
| فایل‌های ردیابی‌شده | ۱۷۸ |
| حجم مخزن | ~۷ مگابایت |
| APKها | خارج از git — برای Releases |
| Bundle آماده | `karbama-repo.bundle` (۶ مگابایت) |
