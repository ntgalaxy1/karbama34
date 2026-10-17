package com.karbama.app.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.karbama.app.data.Service
import com.karbama.app.data.local.AppPrefs
import com.karbama.app.data.local.LocalStore
import com.karbama.app.data.model.AdminMode
import com.karbama.app.data.model.Applicant
import com.karbama.app.data.model.Job
import com.karbama.app.data.model.JobFilter
import com.karbama.app.data.repo.AdminGate
import com.karbama.app.data.repo.ApplicantRepository
import com.karbama.app.data.repo.JobMediaStore
import com.karbama.app.data.repo.JobRepository
import com.karbama.app.data.repo.UserActivityRepository
import com.karbama.app.data.repo.ActivityItem
import com.karbama.app.data.Config
import com.karbama.app.ui.screens.LoginOutcome
import com.karbama.app.util.TotpUtil
import com.karbama.app.util.AuthUtils
import com.karbama.app.data.sample.Catalog
import com.karbama.app.ui.i18n.LANG_FA
import com.karbama.app.ui.i18n.Loc
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * تنها ViewModel برنامه — نگه‌دارنده‌ی وضعیت مشترک بین صفحه‌ها.
 */
class AppViewModel(app: Application) : AndroidViewModel(app) {

    val prefs = AppPrefs(app)

    private val jobRepo = JobRepository()
    private val applicantRepo = ApplicantRepository()
    private val activityRepo = UserActivityRepository()

    // ---------- زبان ----------
    var lang by mutableStateOf(prefs.lang ?: LANG_FA)

    /** true = باید اول زبان انتخاب شود */
    val needsLanguagePick: Boolean get() = prefs.lang == null

    fun applyLang(code: String) {
        lang = code
        prefs.lang = code
    }

    // ---------- داده‌ها ----------
    val jobs = jobRepo.observeJobs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun applicantsFlow(live: Boolean): Flow<List<Applicant>> =
        if (live) applicantRepo.observeApplicants() else LocalStore.applicants

    // ---------- وضعیت کاربر ----------
    var myProfile by mutableStateOf<Applicant?>(prefs.myProfile())
        private set

    var appliedIds by mutableStateOf(prefs.appliedIds())
        private set

    // #28: آگهی‌های پسندیده‌ی کاربر (روی خود گوشی ذخیره می‌شود)
    var likedIds by mutableStateOf(prefs.likedIds())
        private set

    var adminMode by mutableStateOf(prefs.adminMode)
        private set

    var busy by mutableStateOf(false)
        private set

    var message by mutableStateOf<String?>(null)

    /** آیا اولین پاسخ فهرست آگهی‌ها رسیده است؟ (برای نشان‌دادن حالت بارگذاری) */
    var jobsLoaded by mutableStateOf(false)
        private set

    val isRegistered: Boolean get() = myProfile != null || prefs.registered

    val currentUid: String? get() = myProfile?.uid ?: Service.currentUid

    init {
        LocalStore.init(prefs)
        // اگر Firebase تنظیم شده، پروفایل واقعی را از سرور می‌خوانیم
        viewModelScope.launch { refreshProfile() }
        viewModelScope.launch {
            jobs.collect { jobsLoaded = true; jobsNow = it }
        }
        // #29: اگر کاربر روی این دستگاه وارد شده، پسندیده/درخواست‌هایش با سرور هم‌گام می‌شود
        viewModelScope.launch { if (isRegistered) syncUserActivity() }
    }

    private suspend fun refreshProfile() {
        if (!Service.ready) return
        val uid = Service.currentUid ?: return
        val remote = applicantRepo.myProfile(uid)
        if (remote != null) {
            myProfile = remote
            prefs.registered = true
            prefs.saveMyProfile(remote)
        }
    }

    // ---------- ثبت‌نام ----------

    fun submitRegistration(
        applicant: Applicant,
        idCardBytes: ByteArray?,
        bodyPhotoBytes: ByteArray?,
        onDone: (Applicant) -> Unit
    ) {
        viewModelScope.launch {
            busy = true
            val result = withContext(Dispatchers.IO) {
                applicantRepo.submit(applicant, idCardBytes, bodyPhotoBytes)
            }
            busy = false
            result.onSuccess { saved ->
                myProfile = saved
                prefs.registered = true
                prefs.saveMyProfile(saved)
                onDone(saved)
            }.onFailure {
                message = "خطا: " + (it.message ?: "ثبت‌نام انجام نشد")
            }
        }
    }

    fun saveMyProfile(applicant: Applicant, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            busy = true
            applicantRepo.updateMyProfile(applicant)
            myProfile = applicant
            prefs.saveMyProfile(applicant)
            busy = false
            onDone()
        }
    }

    fun logout() {
        myProfile = null
        prefs.registered = false
        adminMode = AdminMode.NONE
        prefs.adminMode = AdminMode.NONE
    }

    /** خروج از حساب مدیر و پایان نشست */
    fun logoutAdmin() = logout()

    // ---------- تنظیمات کارگزار (برای متن تعهدنامه) ----------

    /** مبلغ پیش‌فرض تعهدنامه — ثابت است و در تنظیمات پنل تغییر نمی‌کند
     *  (متقاضی هنگام پر کردن برگه واردش می‌کند و مدیر هم می‌تواند جداگانه تغییرش بدهد) */
    val brokerFee: Long get() = Config.DEFAULT_FEE

    // ---------- #34: هزینه‌ی نمایش آگهی‌ها ----------

    var adsFee by mutableStateOf(prefs.adsFee)
        private set

    /** مبلغ «هزینه نمایش آگهی‌ها» را ذخیره می‌کند (فقط از پنل مدیریت) */
    fun saveAdsFee(value: Long) {
        val safe = if (value > 0L) value else Config.DEFAULT_ADS_FEE
        adsFee = safe
        prefs.adsFee = safe
        message = "✅ " + safe
    }

    /** مدیر همیشه آگهی‌ها را می‌بیند؛ کارجو تنها وقتی مدیر پرداختش را تأیید کرده باشد */
    val adsUnlocked: Boolean
        get() = adminMode != AdminMode.NONE || (myProfile?.payStatus == "paid")

    /** «واریز کردم» — کارجو درخواست بررسی پرداخت را می‌فرستد (منتظر تأیید مدیر) */
    fun requestAdsPayment() {
        val me = myProfile ?: return
        val updated = me.copy(payStatus = "pending", payAt = System.currentTimeMillis())
        myProfile = updated
        prefs.saveMyProfile(updated)
        viewModelScope.launch { applicantRepo.setPayment(updated, "pending") }
        message = "📨 " + updated.payTrackCode
    }

    /** مدیر: تأیید (paid) یا لغو ("" ) پرداخت یک متقاضی */
    fun setApplicantPayment(applicant: com.karbama.app.data.model.Applicant, status: String) {
        viewModelScope.launch {
            applicantRepo.setPayment(applicant, status)
            message = if (status == "paid") "✅ " + applicant.fullName
            else "↩︎ " + applicant.fullName
        }
    }

    /** کارجو: خواندن دوباره‌ی وضعیت پرداخت (وقتی مدیر تأیید می‌کند) */
    fun refreshMyPayment(onDone: (String) -> Unit = {}) {
        viewModelScope.launch {
            val me = myProfile ?: return@launch
            val uid = Service.currentUid
            val remote = if (Service.ready && uid != null) applicantRepo.myProfile(uid) else null
            val fresh = remote ?: LocalStore.applicants.value.firstOrNull { it.id == me.id }
            if (fresh != null) {
                myProfile = fresh
                prefs.saveMyProfile(fresh)
                onDone(fresh.payStatus)
            } else onDone(me.payStatus)
        }
    }

    var brokerCard by mutableStateOf(prefs.brokerCard)
        private set

    var brokerHolder by mutableStateOf(prefs.brokerHolder)
        private set

    fun saveBrokerSettings(card: String, holder: String) {
        brokerCard = card
        brokerHolder = holder
        prefs.brokerCard = card
        prefs.brokerHolder = holder
    }

    // ---------- یوزرنیم و پسورد مدیر (قابل تغییر از تنظیمات پنل) ----------

    var adminUsername by mutableStateOf(prefs.adminUsername)
        private set

    var adminPassword by mutableStateOf(prefs.adminPassword)
        private set

    /** ذخیره‌ی یوزرنیم و پسورد تازه‌ی مدیر */
    fun saveAdminCredentials(user: String, pass: String) {
        adminUsername = user.trim()
        adminPassword = pass
        prefs.adminUsername = adminUsername
        prefs.adminPassword = adminPassword
    }

    /** آیا این یوزرنیم/پسورد، حساب مدیرِ همین دستگاه است؟ */
    fun adminCredentialsMatch(user: String, pass: String): Boolean =
        pass.isNotEmpty() && user.trim().equals(adminUsername, ignoreCase = true) && pass == adminPassword

    // ---------- ورود دومرحله‌ای (2FA / TOTP) ----------

    var twoFactorEnabled by mutableStateOf(prefs.twoFactorEnabled)
        private set

    var twoFactorSecret by mutableStateOf(prefs.twoFactorSecret)
        private set

    var twoFactorRecovery by mutableStateOf(prefs.twoFactorRecovery)
        private set

    /** کلید ساخته شده ولی هنوز با کد ۶ رقمی تأیید نشده است */
    var twoFactorPending by mutableStateOf(false)
        private set

    val twoFactorUri: String get() = TotpUtil.otpauthUri(twoFactorSecret)

    /** کلید تازه می‌سازد و برمی‌گرداند؛ تا تأیید کد، 2FA روشن نمی‌شود */
    fun beginTwoFactorSetup(): String {
        val secret = TotpUtil.newSecret()
        twoFactorSecret = secret
        twoFactorRecovery = TotpUtil.newRecoveryCode()
        twoFactorPending = true
        prefs.twoFactorSecret = secret
        prefs.twoFactorRecovery = twoFactorRecovery
        prefs.twoFactorEnabled = false
        twoFactorEnabled = false
        return secret
    }

    /** کد ۶ رقمی را بررسی و 2FA را فعال می‌کند */
    fun confirmTwoFactor(code: String): Boolean {
        if (!TotpUtil.verify(twoFactorSecret, code)) return false
        twoFactorEnabled = true
        twoFactorPending = false
        prefs.twoFactorEnabled = true
        return true
    }

    fun cancelTwoFactorSetup() {
        twoFactorPending = false
        twoFactorEnabled = false
        twoFactorSecret = ""
        twoFactorRecovery = ""
        prefs.twoFactorEnabled = false
        prefs.twoFactorSecret = ""
        prefs.twoFactorRecovery = ""
    }

    /** خاموش‌کردن 2FA */
    fun disableTwoFactor() = cancelTwoFactorSetup()

    /** بررسی کد ورود دومرحله‌ای — کد ۶ رقمی برنامه یا کد بازیابی (یک‌بارمصرف) */
    fun verifyTwoFactor(input: String): Boolean {
        val clean = input.trim()
        if (clean.isEmpty()) return false
        if (twoFactorRecovery.isNotEmpty() && clean.equals(twoFactorRecovery, ignoreCase = true)) {
            disableTwoFactor()   // کد بازیابی یک‌بارمصرف است و بعد از استفاده 2FA خاموش می‌شود
            return true
        }
        return TotpUtil.verify(twoFactorSecret, clean)
    }

    /** بعد از تأیید کد دومرحله‌ای، ورود مدیر را کامل می‌کند */
    fun completeAdminLogin() {
        myProfile = null
        prefs.registered = false
        viewModelScope.launch {
            val m = AdminGate.currentMode()
            adminMode = m
            prefs.adminMode = m
        }
    }

    /** ثبت تعهدنامه‌ی امضاشده در پروفایل کاربر */
    fun saveUndertaking(undertaking: com.karbama.app.data.model.Undertaking) {
        val me = myProfile ?: return
        val updated = me.copy(undertaking = undertaking)
        myProfile = updated
        prefs.saveMyProfile(updated)
        viewModelScope.launch { applicantRepo.updateMyProfile(updated) }
    }

    // ---------- درخواست‌ها ----------

    fun applyToJob(jobId: String) {
        prefs.addApplied(jobId)
        appliedIds = prefs.appliedIds()
        // #29: همان درخواست در Firestore هم ثبت می‌شود تا مدیر ببیند و روی دستگاه دیگر هم بماند
        val me = myProfile?.id ?: prefs.myApplicantId.takeIf { it.isNotBlank() }
        val job = jobsNow.firstOrNull { it.id == jobId }
        if (me != null && job != null) {
            viewModelScope.launch { activityRepo.apply(me, job) }
        }
    }

    /** #28/#29: پسندیدن / برداشتن از پسندیده — محلی + سرور. خروجی: آیا الان پسندیده است؟ */
    fun toggleLike(jobId: String): Boolean {
        val liked = prefs.toggleLiked(jobId)
        likedIds = prefs.likedIds()
        val me = myProfile?.id ?: prefs.myApplicantId.takeIf { it.isNotBlank() }
        val job = jobsNow.firstOrNull { it.id == jobId }
        if (me != null && job != null) {
            viewModelScope.launch {
                if (liked) activityRepo.like(me, job) else activityRepo.unlike(me, jobId)
            }
        }
        return liked
    }

    // ---------- #29: فعالیت متقاضی برای پنل مدیر ----------

    /** فهرست زنده‌ی آگهی‌ها (برای پیدا کردن آگهی هنگام ثبت در سرور) */
    private var jobsNow: List<Job> = emptyList()

    /** پسندیده‌ها و درخواست‌های یک متقاضی را زنده می‌خواند (فقط در حالت متصل) */
    fun watchApplicantActivity(applicantId: String): Flow<Pair<List<ActivityItem>, List<ActivityItem>>> =
        kotlinx.coroutines.flow.combine(
            activityRepo.observeLikes(applicantId),
            activityRepo.observeApplications(applicantId)
        ) { a, b -> a to b }

    /** هم‌گام‌سازی پسندیده/درخواست‌های محلی با سرور (پس از ورود یا ثبت‌نام) */
    fun syncUserActivity() {
        val me = myProfile?.id ?: prefs.myApplicantId.takeIf { it.isNotBlank() } ?: return
        if (!Service.ready) return
        viewModelScope.launch {
            val (liked, applied) = activityRepo.mergeIds(me, prefs.likedIds(), prefs.appliedIds())
            prefs.setLikedIds(liked)
            prefs.setAppliedIds(applied)
            likedIds = prefs.likedIds()
            appliedIds = prefs.appliedIds()
        }
    }

    // ---------- فیلترها ----------

    var filter by mutableStateOf(JobFilter())

    fun applyFilter(f: JobFilter) {
        filter = f
    }

    fun filteredJobs(list: List<Job>): List<Job> {
        val f = filter
        var out = list.filter { it.active }
        if (f.city.isNotEmpty()) out = out.filter { it.city == f.city }
        if (f.district.isNotEmpty()) out = out.filter { it.district == f.district }
        if (f.contract.isNotEmpty()) out = out.filter { it.contract == f.contract }
        if (f.query.isNotBlank()) {
            val q = f.query.trim().lowercase()
            out = out.filter { job ->
                val names = Catalog.title(job.titleKey)?.names?.values?.joinToString(" ") ?: ""
                names.lowercase().contains(q) ||
                    job.titleCustom.lowercase().contains(q) ||
                    job.company.lowercase().contains(q) ||
                    job.city.lowercase().contains(q) ||
                    job.district.lowercase().contains(q)
            }
        }
        out = when (f.sort) {
            "salary" -> out.sortedByDescending { it.salary }
            "salaryLow" -> out.sortedBy { it.salary }
            else -> out.sortedByDescending { it.publishedAt }
        }
        return out
    }

    // ---------- پنل مدیریت ----------

    /**
     * ورود به برنامه — یکی برای همه:
     *  • مدیر: نام کاربری مدیر + رمز، یا ایمیل حساب Firebase + رمز → پنل مدیریت
     *  • کاربر عادی: نام کاربری (یا شماره تلفن) + رمز خودش → قسمت شخصی
     */
    fun loginUser(user: String, pass: String, onResult: (LoginOutcome) -> Unit) {
        val u = user.trim()
        if (u.isEmpty() || pass.isEmpty()) {
            onResult(LoginOutcome.FAIL)
            return
        }

        // ۱) مدیر با نام کاربری و رمز داخلی
        if (adminCredentialsMatch(u, pass)) {
            myProfile = null
            prefs.registered = false
            if (twoFactorEnabled) {
                // مرحله‌ی دوم: کد ۶ رقمی برنامه‌ی Authenticator
                onResult(LoginOutcome.ADMIN_2FA)
                return
            }
            viewModelScope.launch {
                val m = AdminGate.currentMode()
                adminMode = m
                prefs.adminMode = m
                onResult(LoginOutcome.ADMIN)
            }
            return
        }

        // ۲) مدیر با ایمیل حساب Firebase
        if (AuthUtils.looksLikeEmail(u)) {
            viewModelScope.launch {
                busy = true
                val res = AdminGate.signIn(u, pass)
                busy = false
                if (res.getOrDefault(false)) {
                    myProfile = null
                    prefs.registered = false
                    adminMode = AdminMode.LIVE
                    prefs.adminMode = AdminMode.LIVE
                    onResult(LoginOutcome.ADMIN)
                } else {
                    onResult(LoginOutcome.FAIL)
                }
            }
            return
        }

        // ۳) کاربر عادی
        if (Service.ready) {
            viewModelScope.launch {
                busy = true
                val remote = runCatching { applicantRepo.findByUsername(u) }.getOrNull()
                busy = false
                if (remote != null && remote.passwordHash.isNotEmpty() &&
                    remote.passwordHash == AuthUtils.hash(pass)
                ) {
                    openSession(remote)
                    onResult(LoginOutcome.USER)
                } else {
                    onResult(LoginOutcome.FAIL)
                }
            }
        } else {
            val local = LocalStore.findByUsername(u)
            if (local != null && local.passwordHash.isNotEmpty() &&
                local.passwordHash == AuthUtils.hash(pass)
            ) {
                openSession(local)
                onResult(LoginOutcome.USER)
            } else {
                onResult(LoginOutcome.FAIL)
            }
        }
    }

    /** باز کردن قسمت شخصی کاربر */
    private fun openSession(a: Applicant) {
        myProfile = a
        prefs.saveMyProfile(a)
        prefs.registered = true
        adminMode = AdminMode.NONE
        prefs.adminMode = AdminMode.NONE
            syncUserActivity()
    }

    fun adminPasswordLogin(password: String, onResult: (Boolean) -> Unit) {
        if (password != adminPassword && !AdminGate.passwordIsCorrect(password)) {
            onResult(false)
            return
        }
        viewModelScope.launch {
            val mode = AdminGate.currentMode()
            adminMode = mode
            prefs.adminMode = mode
            onResult(true)
        }
    }

    fun adminAccountLogin(email: String, password: String, onResult: (Boolean, Boolean) -> Unit) {
        viewModelScope.launch {
            busy = true
            val res = AdminGate.signIn(email, password)
            busy = false
            val isAdmin = res.getOrDefault(false)
            if (isAdmin) {
                adminMode = AdminMode.LIVE
                prefs.adminMode = AdminMode.LIVE
                onResult(true, true)
            } else {
                onResult(false, res.isSuccess)
            }
        }
    }

    fun exitAdminPanel() {
        adminMode = AdminMode.NONE
        prefs.adminMode = AdminMode.NONE
        AdminGate.signOut()
    }

    fun saveJob(job: Job, onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            busy = true
            // عکس/ویدیویی که مدیر از گوشی انتخاب کرده، اول آپلود (یا در حالت نمایشی کپی) می‌شود
            val key = job.id.ifEmpty { "new_" + System.currentTimeMillis() }
            val prepared = job.copy(media = JobMediaStore.prepare(key, job.media))
            val res = jobRepo.saveJob(prepared)
            busy = false
            onDone(res.isSuccess)
        }
    }

    fun deleteJob(id: String) {
        viewModelScope.launch { jobRepo.deleteJob(id) }
    }

    fun setApplicantStatus(id: String, status: String) {
        viewModelScope.launch { applicantRepo.setStatus(id, status) }
    }

    /** تغییر مبلغ تعهدنامه‌ی یک متقاضی (از پنل مدیریت) */
    fun setApplicantFee(applicant: com.karbama.app.data.model.Applicant, fee: Long) {
        viewModelScope.launch {
            applicantRepo.setFee(applicant, fee)
            message = "✅ " + applicant.fullName
        }
    }

    fun seedSampleJobs(onDone: (Int) -> Unit) {
        viewModelScope.launch {
            busy = true
            val count = jobRepo.seedSampleJobs().getOrDefault(0)
            busy = false
            onDone(count)
        }
    }

    fun resetDemoData() {
        LocalStore.reset()
    }

    fun loc(): Loc = Loc(lang)
}
