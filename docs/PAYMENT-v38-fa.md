# طرح پرداخت v38 — زرین‌پال، iyzico و فیش بانکی

> **وضعیت:** طراحیِ تأییدشده؛ پرداخت واقعی تا زمان داشتن حساب پذیرندگی و secretهای هر سرویس فعال نمی‌شود.

## تصمیم‌های تأییدشده

| مورد | تصمیم |
|---|---|
| مبلغ پایه | `₺500` برای نمایش همهٔ آگهی‌ها |
| پرداخت ایران | زرین‌پال؛ معادل ریالی مبلغ پایه |
| نرخ تبدیل | نرخ حواله‌ای لیر، آیتم `try_hav` |
| اعتبار نرخ | ۱۵ دقیقه از هنگام ساخت درخواست پرداخت |
| پرداخت ترکیه | iyzico، مبلغ ثابت `₺500` |
| گزینهٔ جایگزین | انتقال بانکی/کارت‌به‌کارت + عکس فیش + کد پیگیری + تأیید مدیر |
| بک‌اند | Firebase Cloud Functions نسل ۲ + Firestore + Storage |

## رفتار امن و قابل حسابرسی

1. برنامهٔ Android برای پرداخت ایران از Cloud Function یک **quote** می‌خواهد؛ خود APK هیچ API key نرخ ارز ندارد.
2. Function نرخ `try_hav` را می‌خواند، نتیجه را با `tryAmount=500`، مبلغ ریالی، منبع، زمان دریافت و `expiresAt` ثبت می‌کند.
3. مبلغ ثبت‌شده تا ۱۵ دقیقه ثابت است. پس از انقضا، کاربر باید quote تازه بسازد.
4. Function درخواست درگاه را با مبلغ ثبت‌شده ایجاد می‌کند؛ اپ تنها به `paymentPageUrl` امن درگاه منتقل می‌شود.
5. callback/webhook سمت سرور، امضا و وضعیت پرداخت را اعتبارسنجی می‌کند. تنها بعد از اعتبارسنجی، `paymentStatus` کاربر `paid` می‌شود.
6. برای فیش بانکی، اپ تصویر JPEG فشردهٔ حداکثر ۵MB را در مسیر اختصاصی `applicants/{uid}/payment_receipt.jpg` آپلود می‌کند و همان سند `applicants/{uid}` را با `paymentStatus=pending`، `paymentMethod=manual`، `paymentReference` و `paymentReceiptUrl` به‌روزرسانی می‌کند. فقط مدیر می‌تواند آن را `paid` کند.
7. کلیدهای درگاه، نرخ ارز و webhook هرگز در APK، Firestore، فایل git یا GitHub قرار نمی‌گیرند.

## Functionهای لازم

| Function | ورود | خروجی/کار |
|---|---|---|
| `createIranQuote` | کاربر واردشده | quote نرخ `try_hav` با TTL ۱۵ دقیقه |
| `startZarinpalPayment` | quote معتبر | URL پرداخت زرین‌پال |
| `zarinpalCallback` | callback HTTPS | verify تراکنش و پرداخت‌شدن امن |
| `startIyzicoPayment` | کاربر واردشده | صفحهٔ hosted checkout برای `₺500` |
| `iyzicoCallback` | callback HTTPS / webhook | بازیابی نتیجه و اعتبارسنجی امضا |
| `reviewManualPayment` | مدیر + رسید موجود | مسیر آیندهٔ server-side برای تأیید/رد فیش؛ فعلاً rule مدیر در Firestore کنترل می‌کند |

## secretهای موردنیاز هنگام راه‌اندازی

```text
ZARINPAL_MERCHANT_ID
IYZICO_API_KEY
IYZICO_SECRET_KEY
NAVASAN_API_KEY
```

این مقادیر با Firebase Secret Manager ثبت می‌شوند، نه در فایل `.env` نسخه‌بندی‌شده. هنگام deploy، از دستور `firebase functions:secrets:set <NAME>` استفاده می‌شود.

## پیش‌نیازهای فعال‌سازی زنده

- حساب پذیرندگی تأییدشدهٔ زرین‌پال و Merchant ID.
- حساب پذیرندگی iyzico و API Key / Secret Key محیط sandbox یا live.
- API key سرویس نرخ ارز برای خواندن آیتم `try_hav`.
- پروژه Firebase با Authentication، Firestore، Storage و Cloud Functions فعال.
- دامنه/نشانی HTTPS callback که در پنل هر دو درگاه ثبت می‌شود.
- مشخصات کارت/IBAN و نام دارنده برای روش فیش بانکی.

## وضعیت کنونی

جریان دستی «ثبت درخواست و تأیید مدیر» در نسخهٔ فعلی وجود دارد. درگاه‌های آنلاین تا واردشدن merchant credentialها و deploy موفق Cloud Functions **نباید فعال اعلام شوند**.
