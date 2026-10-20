# Firebase Functions پرداخت کارباما — v38

این پوشه **معماری امن و stubهای غیرزنده** برای پرداخت را نگه می‌دارد. تا زمانی که تمام پیش‌نیازهای پایین تکمیل نشده‌اند، callableهای درگاه عمداً `failed-precondition` برمی‌گردانند و هیچ پرداختی را `paid` نمی‌کنند.

## رازها — فقط Firebase Secret Manager

```bash
firebase functions:secrets:set NAVASAN_API_KEY
firebase functions:secrets:set ZARINPAL_MERCHANT_ID
firebase functions:secrets:set IYZICO_API_KEY
firebase functions:secrets:set IYZICO_SECRET_KEY
```

هیچ‌کدام از مقدارها را در Android، `.env` نسخه‌بندی‌شده، Firestore یا GitHub وارد نکنید.

## متغیر غیرمحرمانهٔ فعال‌سازی

ابتدا همهٔ callback URLها را در پنل پذیرندگان ثبت و verify کنید، سپس مقدار زیر را در محیط Functions قرار دهید:

```bash
firebase functions:config:set payments.enabled=true
```

> در پیاده‌سازی Gen 2 نهایی، این مقدار باید با parameter/environment غیرمحرمانهٔ معادل پیکربندی شود. قرار دادن آن روی `true` پیش از تکمیل verifyهای واقعی ممنوع است.

## قراردادهای داده‌ای

- `paymentSessions/{id}`: فقط Cloud Functions می‌نویسد. شامل `uid`, `method`, `tryAmount`, `irrAmount`, `rateItem`, `rateIrrPerTry`, `createdAt`, `expiresAt`, `providerStatus`, `providerToken` است.
- `applicants/{uid}` برای فیش دستی: `paymentStatus=pending`, `paymentMethod=manual`, `paymentReference`, `paymentReceiptUrl`, `paymentAt`.
- رسید تصویر JPEG فشرده در `applicants/{uid}/payment_receipt.jpg` قرار می‌گیرد؛ قاعدهٔ Storage آن را به ۵MB و MIME تصویر محدود می‌کند.

## چرخهٔ راه‌اندازی زنده

1. Merchant ID زرین‌پال، API key/secret iyzico و کلید نرخ Navasan را دریافت کنید.
2. رازها را با دستورهای بالا وارد کنید و Firebase Auth، Firestore و Storage را فعال کنید.
3. endpointهای provider را دقیقاً مطابق نسخهٔ فعلی مستندات رسمی‌شان پیاده و ابتدا در sandbox/provider test verify کنید.
4. callbackهای HTTPS را در پنل هر درگاه ثبت کنید؛ callback باید provider token را با API سرور به سرور verify کند.
5. فقط پس از test تراکنش موفق، وضعیت فعال‌سازی را روشن و deploy کنید:

```bash
cd functions
npm ci
npm run lint
firebase deploy --only functions,firestore:rules,storage
```

در هیچ مرحله‌ای redirect سمت کلاینت به‌تنهایی مدرک پرداخت نیست؛ تنها callback/verify سرور می‌تواند `paymentStatus=paid` بگذارد.
