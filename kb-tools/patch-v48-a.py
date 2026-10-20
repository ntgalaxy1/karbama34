# v48: photos stored in Firestore instead of Firebase Storage (no Blaze needed)
import re
B='/home/user/Karbama/app/src/main/java/com/karbama/app/'
def rep(f, pairs):
    p=B+f; s=open(p,encoding='utf-8').read()
    for a,b in pairs:
        assert s.count(a)==1,(f,a[:80],s.count(a)); s=s.replace(a,b)
    open(p,'w',encoding='utf-8').write(s)

# ---- ImageUtils: fitUnder ----
rep('util/ImageUtils.kt',[('''        // ۵) فشرده‌سازی
        ByteArrayOutputStream().use { out ->
            scaled.compress(Bitmap.CompressFormat.JPEG, quality, out)
            out.toByteArray()
        }
    }.getOrNull()
}''','''        // ۵) فشرده‌سازی
        ByteArrayOutputStream().use { out ->
            scaled.compress(Bitmap.CompressFormat.JPEG, quality, out)
            out.toByteArray()
        }
    }.getOrNull()

    /**
     * v48: JPEG را آن‌قدر فشرده/کوچک می‌کند که زیر [maxBytes] شود
     * (برای ذخیره در یک سند Firestore). اگر نشد → null.
     */
    fun fitUnder(bytes: ByteArray, maxBytes: Int): ByteArray? = runCatching {
        if (bytes.size <= maxBytes) return@runCatching bytes
        var bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return@runCatching null
        repeat(8) {
            for (q in intArrayOf(80, 70, 60, 50, 40)) {
                val out = ByteArrayOutputStream().use { o ->
                    bmp.compress(Bitmap.CompressFormat.JPEG, q, o); o.toByteArray()
                }
                if (out.size <= maxBytes) return@runCatching out
            }
            val w = (bmp.width * 0.8f).toInt().coerceAtLeast(1)
            val h = (bmp.height * 0.8f).toInt().coerceAtLeast(1)
            bmp = Bitmap.createScaledBitmap(bmp, w, h, true)
        }
        null
    }.getOrNull()
}''')])

# ---- Coil: register Firestore fetcher ----
rep('KarbamaApp.kt',[
('import android.app.Application\n','import android.app.Application\nimport coil.ImageLoader\nimport coil.ImageLoaderFactory\nimport com.karbama.app.ui.components.FirestoreImageFetcher\n'),
('class KarbamaApp : Application() {\n','class KarbamaApp : Application(), ImageLoaderFactory {\n'),
('''        // پس اگر کاربر اینترنت نداشته باشد آخرین آگهی‌ها نمایش داده می‌شوند.
    }''','''        // پس اگر کاربر اینترنت نداشته باشد آخرین آگهی‌ها نمایش داده می‌شوند.
    }

    /** v48: Coil عکس‌های fsimg://… را از Firestore می‌خواند */
    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .components { add(FirestoreImageFetcher.Factory()) }
            .build()''')])

# ---- ApplicantRepository ----
rep('data/repo/ApplicantRepository.kt',[
('''     * در حالت متصل: ورود ناشناس + آپلود دو عکس در Storage + ثبت سند در Firestore''',
 '''     * در حالت متصل: ورود ناشناس + ذخیره‌ی دو عکس در Firestore (v48) + ثبت سند در Firestore'''),
('''        val db = Service.db
        val auth = Service.auth
        val storage = Service.storage

        if (db == null || auth == null || storage == null) {
            // ---------- حالت نمایشی ----------''','''        val db = Service.db
        val auth = Service.auth

        if (db == null || auth == null) {
            // ---------- حالت نمایشی ----------'''),
('''            val idUrl = idCardBytes?.let {
                uploadPhoto("${Config.STORAGE_ROOT}/$uid/id_card.jpg", it)
            } ?: ""
            val bodyUrl = bodyPhotoBytes?.let {
                uploadPhoto("${Config.STORAGE_ROOT}/$uid/body_photo.jpg", it)
            } ?: ""''','''            val idUrl = idCardBytes?.let {
                FirestoreFiles.putApplicantImage(uid, FirestoreFiles.KIND_ID_CARD, it)
            } ?: ""
            val bodyUrl = bodyPhotoBytes?.let {
                FirestoreFiles.putApplicantImage(uid, FirestoreFiles.KIND_BODY, it)
            } ?: ""'''),
('''    private suspend fun uploadPhoto(path: String, bytes: ByteArray): String {
        val ref = Service.storage!!.reference.child(path)
        // v47: نوع فایل صریح — قواعد Storage فقط image/* را می‌پذیرند
        val meta = com.google.firebase.storage.StorageMetadata.Builder()
            .setContentType("image/jpeg")
            .build()
        ref.putBytes(bytes, meta).await()
        return ref.downloadUrl.await().toString()
    }

''',''),
('''        val db = Service.db
        val auth = Service.auth
        val storage = Service.storage
        if (db == null || auth == null || storage == null) {
            val receiptPath''','''        val db = Service.db
        val auth = Service.auth
        if (db == null || auth == null) {
            val receiptPath'''),
('''            val receiptUrl = uploadPhoto("${Config.STORAGE_ROOT}/$uid/payment_receipt.jpg", receiptBytes)''',
 '''            val receiptUrl = FirestoreFiles.putApplicantImage(uid, FirestoreFiles.KIND_RECEIPT, receiptBytes)'''),
('''     * In connected mode the compressed JPEG goes to the applicant-owned Storage path;''',
 '''     * In connected mode the compressed JPEG goes to applicantFiles/{uid}/files/payment_receipt (Firestore, v48);'''),
])

# ---- JobMediaStore ----
p=B+'data/repo/JobMediaStore.kt'; s=open(p,encoding='utf-8').read()
a=s.index('    private suspend fun uploadOrCopy('); b=s.index('    private fun copyPoster(')
s=s[:a]+'''    private suspend fun uploadOrCopy(jobKey: String, item: JobMedia): JobMedia {
        val ctx = Service.app
        val video = item.type == com.karbama.app.data.model.MediaType.VIDEO

        if (Service.db != null) {
            // v48: حالت متصل → عکس در Firestore؛ ویدیوی داخل گوشی جا نمی‌شود (باید لینک بگذارید)
            if (video) throw IllegalArgumentException("device video not supported; use a link")
            val bytes = ImageUtils.prepare(ctx, Uri.parse(item.url)) ?: return item
            return item.copy(url = FirestoreFiles.putJobImage(jobKey, bytes))
        }

        // ---------- حالت نمایشی: کپی داخل حافظه‌ی برنامه ----------
        val bytes = ctx.contentResolver.openInputStream(Uri.parse(item.url))?.use { it.readBytes() }
            ?: return item
        val ext = if (video) "mp4" else "jpg"
        val name = "job_${jobKey}_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.$ext"
        val path = LocalPhotoCache.saveFile(name, bytes)
        val poster = if (video) copyPoster(item.poster, name) else item.poster
        return item.copy(url = path.ifBlank { item.url }, poster = poster)
    }

'''+s[b:]
s=s.replace('import com.karbama.app.util.MediaUtil\n','import com.karbama.app.util.MediaUtil\nimport com.karbama.app.util.ImageUtils\n')
s=s.replace(''' *      · اگر Firebase وصل باشد → در Storage آپلود می‌شود و لینک آن ذخیره می‌گردد
 *        (تا کارجو هم همان عکس/ویدیو را ببیند)''',''' *      · اگر Firebase وصل باشد → عکس در Firestore (jobFiles) ذخیره می‌شود (v48)؛
 *        ویدیوی داخل گوشی پشتیبانی نمی‌شود و باید به‌صورت لینک اضافه شود''')
open(p,'w',encoding='utf-8').write(s)

# ---- FormScreens: hide device-video button when connected ----
rep('ui/screens/FormScreens.kt',[('''                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GhostButton(
                        text = loc["media_add_photo"],
                        icon = Icons.Filled.Image,
                        onClick = { pickPhoto.launch("image/*") },
                        modifier = Modifier.weight(1f)
                    )
                    GhostButton(
                        text = loc["media_add_video"],
                        icon = Icons.Filled.PlayArrow,
                        onClick = { pickVideo.launch("video/*") },
                        modifier = Modifier.weight(1f)
                    )
                }''','''                // v48: در حالت متصل ویدیوی داخل گوشی ذخیره نمی‌شود → فقط با لینک
                val connected = com.karbama.app.data.Service.db != null
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GhostButton(
                        text = loc["media_add_photo"],
                        icon = Icons.Filled.Image,
                        onClick = { pickPhoto.launch("image/*") },
                        modifier = Modifier.weight(1f)
                    )
                    if (!connected) {
                        GhostButton(
                            text = loc["media_add_video"],
                            icon = Icons.Filled.PlayArrow,
                            onClick = { pickVideo.launch("video/*") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                if (connected) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "🎬 " + loc["media_video_link_hint"],
                        style = MaterialTheme.typography.bodySmall,
                        color = Ink3,
                        lineHeight = 20.sp
                    )
                }''')])

# ---- MediaViewer: web video links open externally ----
rep('ui/components/MediaViewer.kt',[('                    VideoPane(m)','                    VideoPane(m, loc)'),('''@Composable
private fun VideoPane(m: JobMedia) {
    val ctx = LocalContext.current
    AndroidView(''','''@Composable
private fun VideoPane(m: JobMedia, loc: Loc) {
    val ctx = LocalContext.current
    // v48: لینک صفحه‌ی ویدیو (یوتیوب، آپارات و …) داخل VideoView پخش نمی‌شود → باز کردن در برنامه/مرورگر
    val url = m.url.trim()
    val isWebPage = (url.startsWith("http://") || url.startsWith("https://")) &&
        !Regex("""\\.(mp4|webm|3gp|mkv|m3u8)(\\?.*)?$""", RegexOption.IGNORE_CASE).containsMatchIn(url)
    if (isWebPage) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                modifier = Modifier.clickable {
                    runCatching {
                        ctx.startActivity(
                            android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url))
                                .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    }
                }
            ) {
                Text(
                    "▶  " + loc["media_open_video"],
                    color = Ink,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp)
                )
            }
        }
        return
    }
    AndroidView(''')])

# ---- strings ----
p=B+'ui/i18n/Strings.kt'; s=open(p,encoding='utf-8').read()
new={'fa':[('media_video_link_hint','ویدیو را با «افزودن لینک» اضافه کنید (یوتیوب، آپارات یا لینک مستقیم فایل). ویدیوی داخل گوشی ذخیره نمی‌شود.'),('media_open_video','تماشای ویدیو')],
 'tr':[('media_video_link_hint','Videoyu «Bağlantı ekle» ile ekleyin (YouTube, Aparat veya doğrudan dosya bağlantısı). Telefondaki video kaydedilmez.'),('media_open_video','Videoyu izle')],
 'en':[('media_video_link_hint','Add videos with “Add link” (YouTube, Aparat or a direct file link). Videos from the phone are not stored.'),('media_open_video','Watch video')],
 'ar':[('media_video_link_hint','أضف الفيديو عبر «إضافة رابط» (يوتيوب أو آبارات أو رابط مباشر للملف). لا يُحفظ الفيديو الموجود على الهاتف.'),('media_open_video','مشاهدة الفيديو')]}
lines=s.split('\n')
idx=[i for i,l in enumerate(lines) if re.match(r'\s*"phone_country" to ',l)]
assert len(idx)==4
for i,lang in reversed(list(zip(idx,['fa','tr','en','ar']))):
    ind=re.match(r'(\s*)',lines[i]).group(1)
    lines[i+1:i+1]=[f'{ind}"{k}" to "{v}",' for k,v in new[lang]]
s='\n'.join(lines)
for k,_ in new['fa']: assert s.count(f'"{k}" to')==4,k
# err_storage_* → generic wording no longer about Storage setup
open(p,'w',encoding='utf-8').write(s)
print('v48-a OK')
