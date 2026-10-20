# v47: Storage upload content-type + friendly error messages
import re
B='/home/user/Karbama/app/src/main/java/com/karbama/app/'
def rep(f, pairs):
    p=B+f; s=open(p,encoding='utf-8').read()
    for a,b in pairs:
        assert s.count(a)==1,(f,a[:70],s.count(a)); s=s.replace(a,b)
    open(p,'w',encoding='utf-8').write(s)

rep('data/repo/ApplicantRepository.kt',[
('''        val ref = Service.storage!!.reference.child(path)
        ref.putBytes(bytes).await()''','''        val ref = Service.storage!!.reference.child(path)
        // v47: نوع فایل صریح — قواعد Storage فقط image/* را می‌پذیرند
        val meta = com.google.firebase.storage.StorageMetadata.Builder()
            .setContentType("image/jpeg")
            .build()
        ref.putBytes(bytes, meta).await()''')])
rep('data/repo/JobMediaStore.kt',[
('''            val ref = storage.reference.child("jobs/$jobKey/$name")
            ref.putBytes(bytes).await()''','''            val ref = storage.reference.child("jobs/$jobKey/$name")
            val meta = com.google.firebase.storage.StorageMetadata.Builder()
                .setContentType(if (video) "video/mp4" else "image/jpeg")   // v47
                .build()
            ref.putBytes(bytes, meta).await()'''),
('''            val ref = Service.storage!!.reference.child("jobs/$jobKey/poster_$base.jpg")
            ref.putBytes(bytes).await()''','''            val ref = Service.storage!!.reference.child("jobs/$jobKey/poster_$base.jpg")
            val meta = com.google.firebase.storage.StorageMetadata.Builder()
                .setContentType("image/jpeg")   // v47
                .build()
            ref.putBytes(bytes, meta).await()''')])
rep('ui/AppViewModel.kt',[
('message = "خطا: " + (it.message ?: "ثبت‌نام انجام نشد")','message = "⚠️ " + friendlyError(it, "err_register_failed")   // v47'),
('message = "⚠️ " + (it.message ?: "پرداخت ثبت نشد")','message = "⚠️ " + friendlyError(it, "err_pay_failed")   // v47'),
('    fun loc(): Loc = Loc(lang)\n','''    fun loc(): Loc = Loc(lang)

    /**
     * v47: پیام خطای قابل‌فهم به زبان کاربر به‌جای متن خام انگلیسی Firebase.
     * فقط یک کد کوتاه و امن (مثل storage:-13010) کنار پیام می‌آید؛ هیچ داده‌ی حساب یا کلیدی نه.
     */
    private fun friendlyError(e: Throwable, fallbackKey: String): String {
        val l = loc()
        val (key, code) = when (e) {
            is com.google.firebase.storage.StorageException -> when (e.errorCode) {
                com.google.firebase.storage.StorageException.ERROR_OBJECT_NOT_FOUND,
                com.google.firebase.storage.StorageException.ERROR_BUCKET_NOT_FOUND,
                com.google.firebase.storage.StorageException.ERROR_PROJECT_NOT_FOUND -> "err_storage_missing"
                com.google.firebase.storage.StorageException.ERROR_NOT_AUTHORIZED,
                com.google.firebase.storage.StorageException.ERROR_NOT_AUTHENTICATED -> "err_storage_denied"
                com.google.firebase.storage.StorageException.ERROR_QUOTA_EXCEEDED -> "err_storage_quota"
                com.google.firebase.storage.StorageException.ERROR_RETRY_LIMIT_EXCEEDED -> "err_network"
                else -> fallbackKey
            } to "storage:${e.errorCode}"
            is com.google.firebase.firestore.FirebaseFirestoreException -> when (e.code) {
                com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED -> "err_db_denied"
                com.google.firebase.firestore.FirebaseFirestoreException.Code.UNAVAILABLE -> "err_network"
                else -> fallbackKey
            } to "firestore:${e.code.name}"
            is com.google.firebase.FirebaseNetworkException -> "err_network" to "network"
            is com.google.firebase.auth.FirebaseAuthException -> fallbackKey to e.errorCode
            else -> fallbackKey to e.javaClass.simpleName
        }
        return l[key] + "\\n(\\u200E" + code + ")"
    }
'''),
])

p=B+'ui/i18n/Strings.kt'; s=open(p,encoding='utf-8').read()
new={'fa':[('err_register_failed','ثبت‌نام انجام نشد. لطفاً دوباره تلاش کنید.'),('err_pay_failed','پرداخت ثبت نشد. لطفاً دوباره تلاش کنید.'),
  ('err_storage_missing','فضای ذخیره‌ی عکس‌ها (Firebase Storage) هنوز فعال نشده است؛ مدیر باید آن را راه‌اندازی کند. اطلاعات شما ذخیره نشد.'),
  ('err_storage_denied','اجازه‌ی بارگذاری عکس داده نشد (قوانین Storage). لطفاً به مدیر اطلاع دهید.'),
  ('err_storage_quota','سهمیه‌ی فضای ذخیره‌ی عکس‌ها تمام شده است. لطفاً به مدیر اطلاع دهید.'),
  ('err_db_denied','اجازه‌ی ذخیره‌ی اطلاعات داده نشد (قوانین Firestore). لطفاً به مدیر اطلاع دهید.'),
  ('err_network','اتصال اینترنت برقرار نیست. اتصال را بررسی کنید و دوباره تلاش کنید.')],
 'tr':[('err_register_failed','Kayıt tamamlanamadı. Lütfen tekrar deneyin.'),('err_pay_failed','Ödeme kaydedilemedi. Lütfen tekrar deneyin.'),
  ('err_storage_missing','Fotoğraf depolama alanı (Firebase Storage) henüz etkin değil; yönetici kurmalıdır. Bilgileriniz kaydedilmedi.'),
  ('err_storage_denied','Fotoğraf yükleme izni verilmedi (Storage kuralları). Lütfen yöneticiye bildirin.'),
  ('err_storage_quota','Fotoğraf depolama kotası doldu. Lütfen yöneticiye bildirin.'),
  ('err_db_denied','Bilgileri kaydetme izni verilmedi (Firestore kuralları). Lütfen yöneticiye bildirin.'),
  ('err_network','İnternet bağlantısı yok. Bağlantınızı kontrol edip tekrar deneyin.')],
 'en':[('err_register_failed','Registration failed. Please try again.'),('err_pay_failed','Payment could not be submitted. Please try again.'),
  ('err_storage_missing','Photo storage (Firebase Storage) is not enabled yet; the administrator must set it up. Your details were not saved.'),
  ('err_storage_denied','Photo upload was not permitted (Storage rules). Please inform the administrator.'),
  ('err_storage_quota','The photo storage quota is exhausted. Please inform the administrator.'),
  ('err_db_denied','Saving your details was not permitted (Firestore rules). Please inform the administrator.'),
  ('err_network','No internet connection. Check your connection and try again.')],
 'ar':[('err_register_failed','لم يكتمل التسجيل. يرجى المحاولة مرة أخرى.'),('err_pay_failed','لم يتم تسجيل الدفع. يرجى المحاولة مرة أخرى.'),
  ('err_storage_missing','مساحة تخزين الصور (Firebase Storage) غير مفعّلة بعد؛ يجب على المدير إعدادها. لم يتم حفظ بياناتك.'),
  ('err_storage_denied','لم يُسمح برفع الصورة (قواعد Storage). يرجى إبلاغ المدير.'),
  ('err_storage_quota','نفدت حصة تخزين الصور. يرجى إبلاغ المدير.'),
  ('err_db_denied','لم يُسمح بحفظ البيانات (قواعد Firestore). يرجى إبلاغ المدير.'),
  ('err_network','لا يوجد اتصال بالإنترنت. تحقق من الاتصال وحاول مرة أخرى.')]}
lines=s.split('\n')
idx=[i for i,l in enumerate(lines) if re.match(r'\s*"phone_country" to ',l)]
assert len(idx)==4
for i,lang in reversed(list(zip(idx,['fa','tr','en','ar']))):
    ind=re.match(r'(\s*)',lines[i]).group(1)
    lines[i+1:i+1]=[f'{ind}"{k}" to "{v}",' for k,v in new[lang]]
s='\n'.join(lines)
for k,_ in new['fa']: assert s.count(f'"{k}" to')==4,k
open(p,'w',encoding='utf-8').write(s)
print('v47-a OK')
