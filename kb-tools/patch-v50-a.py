# v50: profile avatar = registration photo, WhatsApp logo fix, gender required (no «all»),
#      LTR phone fields, duplicate national-ID / phone → «already registered»
B='/home/user/Karbama/app/src/main/'
J=B+'java/com/karbama/app/'
def rep(f, pairs, base=J):
    p=base+f; s=open(p,encoding='utf-8').read()
    for a,b in pairs:
        assert s.count(a)==1,(f,a[:90],s.count(a)); s=s.replace(a,b)
    open(p,'w',encoding='utf-8').write(s)

# ---------- 1) WhatsApp logo: arc flags written out (Android's parser misreads «0 01» / «0 0012.05») ----------
rep('res/drawable/ic_brand_whatsapp.xml',[
 ('a 9.87 9.87 0 01 -5.031 -1.378','a 9.87 9.87 0 0 1 -5.031 -1.378'),
 ('a 9.86 9.86 0 01 -1.51 -5.26','a 9.86 9.86 0 0 1 -1.51 -5.26'),
 ('a 9.825 9.825 0 012.893 6.994','a 9.825 9.825 0 0 1 2.893 6.994'),
 ('A 11.815 11.815 0 0012.05 0','A 11.815 11.815 0 0 0 12.05 0'),
 ('a 11.882 11.882 0 005.683 1.448','a 11.882 11.882 0 0 0 5.683 1.448'),
 ('a 11.821 11.821 0 00 -3.48 -8.413','a 11.821 11.821 0 0 0 -3.48 -8.413'),
], base=B)

# ---------- 2) Ui.kt: LTR phone field + ApplicantAvatar ----------
rep('ui/components/Ui.kt',[
 ('''    prefix: String? = null,
    revealable: Boolean = true
) {
    var revealed by remember { mutableStateOf(false) }''','''    prefix: String? = null,
    revealable: Boolean = true,
    ltr: Boolean = keyboardType == KeyboardType.Phone   // v50: شماره تلفن چپ‌چین (+90 در چپ)
) {
    var revealed by remember { mutableStateOf(false) }'''),
 ('''        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = singleLine,
            minLines = minLines,''','''        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.ui.platform.LocalLayoutDirection provides
                if (ltr) androidx.compose.ui.unit.LayoutDirection.Ltr
                else androidx.compose.ui.platform.LocalLayoutDirection.current
        ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = singleLine,
            minLines = minLines,'''),
 ('''            textStyle = MaterialTheme.typography.bodyLarge,
            shape = MaterialTheme.shapes.medium,
            visualTransformation = if (password && !revealed) PasswordVisualTransformation()''','''            textStyle = if (ltr) MaterialTheme.typography.bodyLarge.copy(
                textAlign = androidx.compose.ui.text.style.TextAlign.Left,
                textDirection = androidx.compose.ui.text.style.TextDirection.Ltr
            ) else MaterialTheme.typography.bodyLarge,
            shape = MaterialTheme.shapes.medium,
            visualTransformation = if (password && !revealed) PasswordVisualTransformation()'''),
 ('''                focusedBorderColor = Brand,
                unfocusedBorderColor = Line,
                cursorColor = Brand
            )
        )
        if (error != null) {''','''                focusedBorderColor = Brand,
                unfocusedBorderColor = Line,
                cursorColor = Brand
            )
        )
        }
        if (error != null) {'''),
 ('''fun photoModel(path: String): Any? = when {''','''/**
 * v50: آواتار کارجو = همان عکسی که هنگام ثبت‌نام فرستاده (عکس تمام‌قد، از بالا برش می‌خورد تا صورت دیده شود).
 * تا عکس برسد — یا اگر نبود — حروف اول نام نمایش داده می‌شود.
 */
@Composable
fun ApplicantAvatar(
    applicant: com.karbama.app.data.model.Applicant,
    size: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
    background: androidx.compose.ui.graphics.Brush =
        androidx.compose.ui.graphics.SolidColor(Color.White),
    initialsColor: Color = BrandDark,
    fontSize: androidx.compose.ui.unit.TextUnit = 26.sp
) {
    val model = photoModel(applicant.bodyPhotoUrl) ?: photoModel(applicant.idPhotoUrl)
    Box(
        modifier = modifier
            .size(size)
            .clip(androidx.compose.foundation.shape.CircleShape)
            .background(background),
        contentAlignment = Alignment.Center
    ) {
        Text(
            initialsOf(applicant.fullName),
            fontSize = fontSize,
            fontWeight = FontWeight.Black,
            color = initialsColor
        )
        if (model != null) {
            coil.compose.AsyncImage(
                model = model,
                contentDescription = null,
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                alignment = Alignment.TopCenter,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

private fun initialsOf(name: String): String =
    name.trim().split(" ").filter { it.isNotEmpty() }.take(2)
        .joinToString("") { it.first().uppercase() }
        .ifEmpty { "?" }

fun photoModel(path: String): Any? = when {'''),
])

# ---------- 3) Profile header + admin detail use the avatar ----------
rep('ui/screens/ProfileScreen.kt',[
 ('''                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        initials(applicant.fullName),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        color = BrandDark
                    )
                }''','''                // v50: عکس ثبت‌نام = عکس پروفایل
                ApplicantAvatar(applicant = applicant, size = 76.dp)'''),
])
rep('ui/screens/AdminScreens.kt',[
 ('''                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(Purple, Brand))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            initials(applicant.fullName),
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp
                        )
                    }''','''                    ApplicantAvatar(                       // v50: عکس ثبت‌نام
                        applicant = applicant,
                        size = 58.dp,
                        background = Brush.linearGradient(listOf(Purple, Brand)),
                        initialsColor = Color.White,
                        fontSize = 20.sp
                    )'''),
])

# ---------- 4) Strings ----------
S={'PERSIAN':('"err_nid" to "کد ملی باید ۱۰ رقم باشد",','"gender" to "جنسیت (اختیاری)",','"gender" to "جنسیت",',
   ['"err_gender" to "لطفاً جنسیت را انتخاب کنید",',
    '"dup_nid" to "با این کد ملی از قبل عضو شده‌اید — از «ورود به برنامه» وارد شوید",',
    '"dup_phone" to "با این شماره تلفن از قبل عضو شده‌اید — از «ورود به برنامه» وارد شوید",',
    '"dup_checking" to "در حال بررسی…",']),
   'TURKISH':('"err_nid" to "Kimlik numarası 10 haneli olmalı",','"gender" to "Cinsiyet (isteğe bağlı)",','"gender" to "Cinsiyet",',
   ['"err_gender" to "Lütfen cinsiyet seçin",',
    '"dup_nid" to "Bu kimlik numarasıyla zaten üyesiniz — lütfen giriş yapın",',
    '"dup_phone" to "Bu telefon numarasıyla zaten üyesiniz — lütfen giriş yapın",',
    '"dup_checking" to "Kontrol ediliyor…",']),
   'ENGLISH':('"err_nid" to "National ID must be 10 digits",','"gender" to "Gender (optional)",','"gender" to "Gender",',
   ['"err_gender" to "Please select your gender",',
    '"dup_nid" to "You are already registered with this national ID — please sign in",',
    '"dup_phone" to "You are already registered with this phone number — please sign in",',
    '"dup_checking" to "Checking…",']),
   'ARABIC':('"err_nid" to "الرقم الوطني يجب أن يكون 10 أرقام",','"gender" to "الجنس (اختياري)",','"gender" to "الجنس",',
   ['"err_gender" to "يرجى اختيار الجنس",',
    '"dup_nid" to "أنت مسجّل مسبقاً بهذا الرقم الوطني — يرجى تسجيل الدخول",',
    '"dup_phone" to "أنت مسجّل مسبقاً بهذا الرقم الهاتفي — يرجى تسجيل الدخول",',
    '"dup_checking" to "جارٍ التحقق…",'])}
for name,(nid,g_old,g_new,extra) in S.items():
    rep('ui/i18n/Strings.kt',[(nid, nid+''.join('\n    '+e for e in extra)),(g_old,g_new)])

# ---------- 5) Registration screen ----------
rep('ui/screens/RegistrationScreen.kt',[
 ('''    onSubmit: (Applicant, ByteArray?, ByteArray?, Undertaking?) -> Unit,
    onBack: () -> Unit
) {''','''    onSubmit: (Applicant, ByteArray?, ByteArray?, Undertaking?) -> Unit,
    onBack: () -> Unit,
    // v50: بررسی تکراری‌بودن کد ملی/شماره تلفن → (nidTaken, phoneTaken)
    checkUnique: (String, String, (Boolean, Boolean) -> Unit) -> Unit = { _, _, cb -> cb(false, false) }
) {'''),
 ('''    val scroll = rememberScrollState()
''','''    val scroll = rememberScrollState()
    var nidTaken by remember { mutableStateOf(false) }      // v50
    var phoneTaken by remember { mutableStateOf(false) }    // v50
    var checking by remember { mutableStateOf(false) }      // v50
'''),
 ('''            !PhoneUtils.isValid(phoneCountry, phone) -> loc[phoneErrorKey(phoneCountry)]
            else -> null
        }
        2 ->''','''            !PhoneUtils.isValid(phoneCountry, phone) -> loc[phoneErrorKey(phoneCountry)]
            gender != "m" && gender != "f" -> loc["err_gender"]          // v50: الزامی
            else -> null
        }
        2 ->'''),
 ('''                        loading = busy,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            val err = validate(step)
                            error = err
                            if (err == null) {
                                if (step < TOTAL_STEPS) step++ else {''','''                        loading = busy || checking,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (checking) return@PrimaryButton
                            val err = validate(step)
                            error = err
                            if (err == null && step == 1) {
                                // v50: کد ملی یا شماره تکراری → «از قبل عضو شده‌اید»
                                checking = true
                                checkUnique(
                                    FormatUtils.digitsOnly(nationalId, 10),
                                    PhoneUtils.format(phoneCountry, phone).filter { it.isDigit() }
                                ) { n, p ->
                                    checking = false
                                    nidTaken = n
                                    phoneTaken = p
                                    error = when {
                                        n -> loc["dup_nid"]
                                        p -> loc["dup_phone"]
                                        else -> null
                                    }
                                    if (!n && !p) step++
                                }
                            } else if (err == null) {
                                if (step < TOTAL_STEPS) step++ else {'''),
 ('''                                onValueChange = {
                                    nationalId = FormatUtils.digitsOnly(it, 10)
                                },
                                label = loc["national_id"],
                                placeholder = "0079123456",
                                required = true,''','''                                onValueChange = {
                                    nationalId = FormatUtils.digitsOnly(it, 10)
                                    nidTaken = false
                                },
                                label = loc["national_id"],
                                placeholder = "0079123456",
                                required = true,
                                error = if (nidTaken) loc["dup_nid"] else null,'''),
 ('''                            onSelect = { phoneCountry = it; error = null }
                        )
                        Spacer(Modifier.height(12.dp))
                        KbField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = loc["phone"],
                            placeholder = phoneCountry.example.ifEmpty { "123 456 789" },
                            prefix = "+" + phoneCountry.code,
                            required = true,''','''                            onSelect = { phoneCountry = it; error = null; phoneTaken = false }
                        )
                        Spacer(Modifier.height(12.dp))
                        KbField(
                            value = phone,
                            onValueChange = { phone = it; phoneTaken = false },
                            label = loc["phone"],
                            placeholder = phoneCountry.example.ifEmpty { "123 456 789" },
                            prefix = "+" + phoneCountry.code,
                            required = true,
                            error = if (phoneTaken) loc["dup_phone"] else null,'''),
 ('''                        SectionTitle(loc["gender"])''','''                        SectionTitle(loc["gender"] + " *")      // v50: الزامی'''),
 ('''                            SelectChip(
                                text = loc["all"] + " / -",
                                selected = gender == "",
                                onClick = { gender = "" }
                            )
''',''),
])
rep('MainActivity.kt',[
 ('''                        onBack = { nav.popBackStack() }
                    )
                }

                // ---------- بدنه‌ی اصلی (تب‌ها) ----------''','''                        onBack = { nav.popBackStack() },
                        checkUnique = { nid, ph, cb -> vm.checkUnique(nid, ph, cb) }   // v50
                    )
                }

                // ---------- بدنه‌ی اصلی (تب‌ها) ----------'''),
])

# ---------- 6) Repository: uniqueKeys ----------
rep('data/repo/ApplicantRepository.kt',[
 ('''        // ---------- حالت متصل ----------
        return runCatching {
            val user = auth.currentUser ?: auth.signInAnonymously().await().user
            val uid = user?.uid ?: throw IllegalStateException("auth failed")
''','''        // ---------- حالت متصل ----------
        return runCatching {
            val user = auth.currentUser ?: auth.signInAnonymously().await().user
            val uid = user?.uid ?: throw IllegalStateException("auth failed")

            // v50: بررسی دوباره‌ی تکراری‌بودن، پیش از آپلود عکس‌ها
            val (nidTaken, phoneTaken) = checkUnique(applicant.nationalId, applicant.phone)
            if (nidTaken) throw DuplicateException("nid")
            if (phoneTaken) throw DuplicateException("phone")
'''),
 ('''            db.collection(Config.COL_APPLICANTS).document(uid).set(saved.toMap() - "paymentAt").await()
            saved''','''            db.collection(Config.COL_APPLICANTS).document(uid).set(saved.toMap() - "paymentAt").await()
            runCatching { claimKeys(saved) }   // v50: ثبت کد ملی/تلفن (best-effort)
            saved'''),
 ('''    private val ADMIN_OR_PAYMENT_KEYS = setOf(''','''    // ================= v50: یکتا بودن کد ملی و شماره تلفن =================
    // uniqueKeys/{nid_<sha256> | ph_<sha256>} = { owner: uid, kind, createdAt }
    // فقط هش ذخیره می‌شود؛ هر کاربرِ واردشده فقط می‌تواند «وجود داشتن» یک کلید را بپرسد.

    class DuplicateException(val kind: String) : Exception("duplicate:$kind")

    private fun sha256(s: String): String =
        java.security.MessageDigest.getInstance("SHA-256")
            .digest(s.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }

    fun nidKey(nid: String): String? =
        nid.filter { it.isDigit() }.takeIf { it.length == 10 }?.let { "nid_" + sha256("karbama|nid|$it") }

    /** phone = شماره‌ی کامل بین‌المللی (هر قالبی؛ فقط رقم‌ها استفاده می‌شوند) */
    fun phoneKey(phone: String): String? =
        phone.filter { it.isDigit() }.takeIf { it.length >= 7 }?.let { "ph_" + sha256("karbama|ph|$it") }

    /**
     * (nidTaken, phoneTaken). کلیدی «تکراری» است که مال شخص دیگری باشد —
     * یا مال همین حساب باشد ولی این حساب از قبل پروفایل داشته باشد.
     * اگر سرور در دسترس نباشد یا قانون هنوز منتشر نشده باشد → false (ثبت‌نام مسدود نمی‌شود).
     */
    suspend fun checkUnique(nid: String, phone: String): Pair<Boolean, Boolean> {
        val db = Service.db
        val auth = Service.auth
        if (db == null || auth == null) {
            val pd = phone.filter { it.isDigit() }
            val nd = nid.filter { it.isDigit() }
            val all = LocalStore.applicants.value
            return Pair(
                nd.isNotEmpty() && all.any { it.nationalId.filter { c -> c.isDigit() } == nd },
                pd.isNotEmpty() && all.any { it.phone.filter { c -> c.isDigit() } == pd }
            )
        }
        val uid = runCatching {
            (auth.currentUser ?: auth.signInAnonymously().await().user)?.uid
        }.getOrNull() ?: return Pair(false, false)
        val iHaveProfile = runCatching {
            db.collection(Config.COL_APPLICANTS).document(uid).get().await().exists()
        }.getOrDefault(false)
        suspend fun taken(key: String?): Boolean {
            if (key == null) return false
            val doc = runCatching {
                db.collection(COL_UNIQUE).document(key).get().await()
            }.getOrNull() ?: return false
            return doc.exists() && (doc.getString("owner") != uid || iHaveProfile)
        }
        return Pair(taken(nidKey(nid)), taken(phoneKey(phone)))
    }

    /** ثبت کلیدهای کد ملی/تلفن برای این متقاضی (فقط اگر هنوز نیست) */
    suspend fun claimKeys(applicant: Applicant) {
        val db = Service.db ?: return
        val uid = Service.currentUid ?: return
        if (uid != applicant.id) return
        listOf("nid" to nidKey(applicant.nationalId), "phone" to phoneKey(applicant.phone)).forEach { (kind, key) ->
            if (key == null) return@forEach
            runCatching {
                val ref = db.collection(COL_UNIQUE).document(key)
                if (!ref.get().await().exists()) {
                    ref.set(
                        mapOf(
                            "owner" to uid,
                            "kind" to kind,
                            "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                        )
                    ).await()
                }
            }
        }
    }

    /** آزاد کردن کلید شماره‌ی قبلی بعد از تغییر شماره (فقط اگر مال خودم باشد — قانون بررسی می‌کند) */
    suspend fun releasePhoneKey(oldPhone: String) {
        val db = Service.db ?: return
        val key = phoneKey(oldPhone) ?: return
        runCatching { db.collection(COL_UNIQUE).document(key).delete().await() }
    }

    private val COL_UNIQUE = "uniqueKeys"

    private val ADMIN_OR_PAYMENT_KEYS = setOf('''),
])

# ---------- 7) ViewModel ----------
rep('ui/AppViewModel.kt',[
 ('''        val remote = applicantRepo.myProfile(uid)
        if (remote != null) {
            myProfile = remote
            prefs.registered = true
            prefs.saveMyProfile(remote)
        }
    }''','''        val remote = applicantRepo.myProfile(uid)
        if (remote != null) {
            myProfile = remote
            prefs.registered = true
            prefs.saveMyProfile(remote)
            // v50: کارجوهای قبلی هم کد ملی/تلفنشان ثبت شود تا تکراری‌ها شناخته شوند
            runCatching { withContext(Dispatchers.IO) { applicantRepo.claimKeys(remote) } }
        }
    }

    /** v50: آیا کد ملی / شماره تلفن قبلاً ثبت شده؟ → cb(nidTaken, phoneTaken) */
    fun checkUnique(nid: String, phone: String, cb: (Boolean, Boolean) -> Unit) {
        viewModelScope.launch {
            val r = runCatching {
                withContext(Dispatchers.IO) { applicantRepo.checkUnique(nid, phone) }
            }.getOrDefault(Pair(false, false))
            cb(r.first, r.second)
        }
    }'''),
 ('''            }.onFailure {
                message = "⚠️ " + friendlyError(it, "err_register_failed")   // v47
            }''','''            }.onFailure {
                message = if (it is ApplicantRepository.DuplicateException)   // v50
                    "⚠️ " + loc()[if (it.kind == "nid") "dup_nid" else "dup_phone"]
                else "⚠️ " + friendlyError(it, "err_register_failed")   // v47
            }'''),
 ('''    fun saveMyProfile(applicant: Applicant, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            busy = true
            applicantRepo.updateMyProfile(applicant)''','''    fun saveMyProfile(applicant: Applicant, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            busy = true
            // v50: شماره‌ی جدید نباید مال کارجوی دیگری باشد
            val old = myProfile
            val phoneChanged = old != null &&
                old.phone.filter { it.isDigit() } != applicant.phone.filter { it.isDigit() }
            if (phoneChanged) {
                val taken = runCatching {
                    withContext(Dispatchers.IO) { applicantRepo.checkUnique("", applicant.phone) }.second
                }.getOrDefault(false)
                if (taken) {
                    busy = false
                    message = "⚠️ " + loc()["dup_phone"]
                    return@launch
                }
            }
            applicantRepo.updateMyProfile(applicant)
            if (phoneChanged) runCatching {
                withContext(Dispatchers.IO) {
                    applicantRepo.claimKeys(applicant)
                    applicantRepo.releasePhoneKey(old!!.phone)
                }
            }'''),
])
print('v50-a OK')
