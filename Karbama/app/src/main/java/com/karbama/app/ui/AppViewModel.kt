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
import com.karbama.app.data.model.Hotel
import com.karbama.app.data.model.EssentialAddress
import com.karbama.app.data.model.JobFilter
import com.karbama.app.data.repo.AdminGate
import com.karbama.app.data.repo.ApplicantRepository
import com.karbama.app.data.repo.JobMediaStore
import com.karbama.app.data.repo.JobRepository
import com.karbama.app.data.repo.HotelRepository
import com.karbama.app.data.repo.EssentialAddressRepository
import com.karbama.app.data.repo.IdentityRepository
import com.karbama.app.data.repo.UserActivityRepository
import com.karbama.app.data.repo.ActivityItem
import com.karbama.app.data.repo.AccountDeletionRepository
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
import kotlinx.coroutines.tasks.await

// v10.0.1 ships Firebase Auth for jobseekers, while the collaborator rollout is
// deliberately deferred. Keep the source paths intact for the later update, but
// prevent an undeployed collaborator backend from becoming an app entry point.
private const val COLLABORATOR_WORKSPACE_ENABLED = false

/**
 * تنها ViewModel برنامه — نگه‌دارنده‌ی وضعیت مشترک بین صفحه‌ها.
 */
class AppViewModel(app: Application) : AndroidViewModel(app) {

    val prefs = AppPrefs(app)

    private val jobRepo = JobRepository()
    private val hotelRepo = HotelRepository()
    private val essentialAddressRepo = EssentialAddressRepository()
    private val applicantRepo = ApplicantRepository()
    private val activityRepo = UserActivityRepository()
    private val identityRepo = IdentityRepository()
    private val accountDeletionRepo = AccountDeletionRepository()

    // ---------- زبان ----------
    var lang by mutableStateOf(prefs.lang ?: LANG_FA)

    /** true = باید اول زبان انتخاب شود */
    val needsLanguagePick: Boolean get() = prefs.lang == null

    fun applyLang(code: String) {
        lang = code
        prefs.lang = code
    }

    // ---------- حالت نمایش (روز / شب / هماهنگ با گوشی) ----------
    var themeMode by mutableStateOf(prefs.themeMode)
        private set

    fun applyThemeMode(mode: String) {
        val safe = if (mode in com.karbama.app.ui.theme.ThemeMode.all) mode
        else com.karbama.app.ui.theme.ThemeMode.SYSTEM
        themeMode = safe
        prefs.themeMode = safe
    }

    fun resolvesDarkTheme(systemDark: Boolean): Boolean = when (themeMode) {
        com.karbama.app.ui.theme.ThemeMode.DARK -> true
        com.karbama.app.ui.theme.ThemeMode.LIGHT -> false
        else -> systemDark
    }

    // ---------- داده‌ها ----------
    val jobs = jobRepo.observeJobs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** فهرست زندهٔ هتل‌های اضافه‌شده توسط مدیر برای تب مسیریابی. */
    val hotels = hotelRepo.observeHotels()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val essentialAddresses = essentialAddressRepo.observeAddresses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun applicantsFlow(live: Boolean): Flow<List<Applicant>> =
        if (live) applicantRepo.observeApplicants() else LocalStore.applicants

    /** Collaborator queries are ownership-filtered so Firestore can enforce the scope. */
    fun collaboratorJobsFlow(): Flow<List<Job>> = collaboratorUid?.let { jobRepo.observeJobsCreatedBy(it) }
        ?: kotlinx.coroutines.flow.flowOf(emptyList())

    fun collaboratorHotelsFlow(): Flow<List<Hotel>> = collaboratorUid?.let { hotelRepo.observeHotelsOwnedBy(it) }
        ?: kotlinx.coroutines.flow.flowOf(emptyList())

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

    /** UID is held only for the active collaborator session; it is never persisted. */
    var collaboratorUid by mutableStateOf<String?>(null)
        private set

    val isCollaborator: Boolean get() = COLLABORATOR_WORKSPACE_ENABLED && collaboratorUid != null

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
        // اگر Firebase تنظیم شده، نقش و پروفایل نشست واقعی را از سرور بازیابی می‌کنیم.
        viewModelScope.launch { restoreSignedInSession() }
        // #29: اگر کاربر روی این دستگاه وارد شده، پسندیده/درخواست‌هایش با سرور هم‌گام می‌شود
        viewModelScope.launch { if (isRegistered) syncUserActivity() }
    }

    private suspend fun restoreSignedInSession() {
        if (!Service.ready) return
        val user = Service.auth?.currentUser ?: return
        if (AdminGate.isAdminAccount()) { adminMode = AdminMode.LIVE; prefs.adminMode = AdminMode.LIVE; return }
        if (runCatching { user.getIdToken(true).await().claims["karbamaRole"] as? String }.getOrNull() == "collaborator") {
            // The collaborator workspace is deferred; do not expose an account
            // whose provisioning backend has intentionally not been deployed.
            identityRepo.signOut()
            collaboratorUid = null
            prefs.clearMyProfile()
            prefs.registered = false
            adminMode = AdminMode.NONE
            prefs.adminMode = AdminMode.NONE
            return
        }
        refreshProfile()
    }

    private suspend fun refreshProfile() {
        if (!Service.ready) return
        val uid = Service.currentUid ?: return
        val remote = applicantRepo.myProfile(uid)
        if (remote != null) {
            collaboratorUid = null
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
    }

    // ---------- ثبت‌نام ----------

    fun submitRegistration(
        applicant: Applicant, password: String, idCardBytes: ByteArray?, bodyPhotoBytes: ByteArray?, onDone: (Applicant) -> Unit
    ) {
        viewModelScope.launch {
            busy = true
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    if (Service.ready) identityRepo.createApplicant(applicant.username, password)
                    applicantRepo.submit(applicant, idCardBytes, bodyPhotoBytes).getOrThrow()
                }
            }
            busy = false
            result.onSuccess { saved ->
                myProfile = saved
                prefs.registered = true
                prefs.saveMyProfile(saved)
                onDone(saved)
            }.onFailure {
                message = if (it is ApplicantRepository.DuplicateException)   // v50
                    "⚠️ " + loc()[if (it.kind == "nid") "dup_nid" else "dup_phone"]
                else "⚠️ " + friendlyError(it, "err_register_failed")   // v47
            }
        }
    }

    fun saveMyProfile(applicant: Applicant, onDone: () -> Unit = {}) {
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
            }
            myProfile = applicant
            prefs.saveMyProfile(applicant)
            busy = false
            onDone()
        }
    }

    /**
     * Starts a manual account/data-deletion process in the connected app.
     * It is intentionally an intake request: actual erasure can require a
     * retention review and server-side Firebase Auth administration.
     */
    fun requestAccountDeletion() {
        if (myProfile == null || !Service.ready) {
            message = "⚠️ " + loc()["delete_account_request_unavailable"]
            return
        }
        viewModelScope.launch {
            busy = true
            val result = withContext(Dispatchers.IO) {
                accountDeletionRepo.requestCurrentApplicantDeletion()
            }
            busy = false
            message = when (result) {
                AccountDeletionRepository.RequestResult.Submitted ->
                    "✅ " + loc()["delete_account_request_sent"]
                AccountDeletionRepository.RequestResult.AlreadySubmitted ->
                    "ℹ️ " + loc()["delete_account_request_pending"]
                is AccountDeletionRepository.RequestResult.Failed ->
                    "⚠️ " + friendlyError(result.error, "delete_account_request_failed")
            }
        }
    }

    fun logout() {
        myProfile = null
        prefs.clearMyProfile()
        prefs.registered = false
        collaboratorUid = null
        identityRepo.signOut()
        adminMode = AdminMode.NONE
        prefs.adminMode = AdminMode.NONE
        // The encrypted remembered username/password intentionally stays on this device.
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
        message = "✅ " + loc()["adm2_saved"]      // v51: مثل toast دمو
    }

    /** v51: «یوزرنیم مدیر» در تنظیمات = ایمیل حساب Firebase مدیرِ واردشده */
    val adminEmail: String get() = AdminGate.currentEmail()

    /**
     * v51: تغییر رمز مدیر — مستقیم در Firebase Auth. رمز در Firestore یا جای دیگری نوشته نمی‌شود؛
     * فقط اگر روی این دستگاه «ورود به خاطر سپرده» (Keystore) برای همین ایمیل بود، آن هم به‌روز می‌شود.
     */
    /** v52: onDone → "ok" | "need_current" (پنجره‌ی پسورد فعلی) | "wrong_current" | "fail" */
    fun changeAdminPassword(newPass: String, currentPass: String? = null, onDone: (String) -> Unit = {}) {
        val l = loc()
        if (newPass.length < 6) {
            message = "⚠️ " + l["adm_pass_short"]
            onDone("fail")
            return
        }
        viewModelScope.launch {
            busy = true
            val r = AdminGate.changePassword(newPass, rememberedPassword = null, currentPassword = currentPass)
            busy = false
            when (r) {
                AdminGate.PwResult.OK -> {
                    message = "✅ " + l["adm_pass_changed"]
                    onDone("ok")
                }
                AdminGate.PwResult.NEED_CURRENT -> onDone("need_current")
                AdminGate.PwResult.WRONG_CURRENT -> { message = "⚠️ " + l["adm_cur_wrong"]; onDone("wrong_current") }
                AdminGate.PwResult.WEAK -> { message = "⚠️ " + l["adm_pass_short"]; onDone("fail") }
                AdminGate.PwResult.NETWORK -> { message = "⚠️ " + l["err_network"]; onDone("fail") }
                AdminGate.PwResult.ERROR -> { message = "⚠️ " + l["adm_pass_failed"]; onDone("fail") }
            }
        }
    }

    /** مدیر همیشه آگهی‌ها را می‌بیند؛ کارجو تنها وقتی مدیر پرداختش را تأیید کرده باشد */
    val adsUnlocked: Boolean
        get() = adminMode != AdminMode.NONE || (myProfile?.payStatus == "paid")

    /** v38: receipt + transaction reference are required before a manual request reaches manager review. */
    fun requestManualAdsPayment(reference: String, receiptBytes: ByteArray) {
        val me = myProfile ?: return
        viewModelScope.launch {
            busy = true
            val result = withContext(Dispatchers.IO) {
                applicantRepo.submitManualPayment(me, reference, receiptBytes)
            }
            busy = false
            result.onSuccess { saved ->
                myProfile = saved
                prefs.saveMyProfile(saved)
                message = "📨 " + saved.payTrackCode
            }.onFailure {
                message = "⚠️ " + friendlyError(it, "err_pay_failed")   // v47
            }
        }
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
        prefs.clearMyProfile()
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

    /** فهرست فعلیِ قابل‌دسترسی در نشست، برای ثبت پسند/درخواست کارجو. */
    private var jobsNow: List<Job> = emptyList()

    /** Called by the root collector; avoids a second, unscoped listener for collaborators. */
    fun recordVisibleJobs(items: List<Job>) {
        jobsNow = items
        jobsLoaded = true
    }

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
     *  • مدیر: فقط ایمیل و رمز حساب Firebase → پنل مدیریت واقعی
     *  • کاربر عادی: نام کاربری (یا شماره تلفن) + رمز خودش → قسمت شخصی
     */
    fun loginUser(user: String, pass: String, onResult: (LoginOutcome) -> Unit) {
        val u = user.trim()
        if (u.isEmpty() || pass.isEmpty()) {
            onResult(LoginOutcome.FAIL)
            return
        }

        // مدیر فقط با ایمیل و رمز حساب Firebase وارد پنل واقعی می‌شود.
        if (AuthUtils.looksLikeEmail(u)) {
            viewModelScope.launch {
                busy = true
                when (AdminGate.signIn(u, pass)) {
                    AdminGate.LoginResult.SUCCESS -> {
                        myProfile = null
                        prefs.clearMyProfile()
                        prefs.registered = false
                        adminMode = AdminMode.LIVE
                        prefs.adminMode = AdminMode.LIVE
                        busy = false
                        onResult(LoginOutcome.ADMIN)
                    }
                    AdminGate.LoginResult.AUTH_REJECTED -> {
                        busy = false
                        onResult(LoginOutcome.ADMIN_AUTH_REJECTED)
                    }
                    AdminGate.LoginResult.NOT_ADMIN -> {
                        busy = false
                        onResult(LoginOutcome.ADMIN_NOT_AUTHORIZED)
                    }
                    AdminGate.LoginResult.NETWORK_ERROR -> {
                        busy = false
                        onResult(LoginOutcome.ADMIN_NETWORK_ERROR)
                    }
                    else -> {
                        busy = false
                        onResult(LoginOutcome.ADMIN_FIREBASE_ERROR)
                    }
                }
            }
            return
        }

        if (!Service.ready) { onResult(LoginOutcome.FAIL); return }
        viewModelScope.launch {
            busy = true
            val signed = runCatching { withContext(Dispatchers.IO) { identityRepo.signIn(u, pass) } }.getOrNull()
            if (signed == null) { busy = false; onResult(LoginOutcome.FAIL); return@launch }
            if (signed.role == IdentityRepository.Role.COLLABORATOR) {
                // Held for the next update: no collaborator flow is available in v10.0.1.
                identityRepo.signOut()
                collaboratorUid = null
                busy = false
                onResult(LoginOutcome.FAIL)
                return@launch
            }
            val profile = withContext(Dispatchers.IO) { applicantRepo.myProfile(signed.user.uid) }
            busy = false
            if (profile != null) { openApplicantSession(profile); onResult(LoginOutcome.USER) }
            else { identityRepo.signOut(); onResult(LoginOutcome.FAIL) }
        }
    }

    private fun openApplicantSession(a: Applicant) {
        myProfile = a; prefs.saveMyProfile(a); prefs.registered = true; collaboratorUid = null; adminMode = AdminMode.NONE; prefs.adminMode = AdminMode.NONE; syncUserActivity()
    }

    fun createCollaborator(username: String, password: String) {
        viewModelScope.launch {
            busy = true
            val result = runCatching { withContext(Dispatchers.IO) { identityRepo.createCollaborator(username, password) } }
            busy = false
            message = if (result.isSuccess) "✅ همکار «${result.getOrThrow().username}» ساخته شد." else "⚠️ " + friendlyError(result.exceptionOrNull() ?: Exception(), "err_register_failed")
        }
    }

    fun collaboratorApplicantsFlow(): Flow<List<Applicant>> = collaboratorUid?.let { applicantRepo.observeApplicantsCreatedBy(it) } ?: kotlinx.coroutines.flow.flowOf(emptyList())

    fun createApplicantForCollaborator(username: String, password: String, firstName: String, lastName: String, nationalId: String, phone: String) {
        if (!isCollaborator) {
            message = "⚠️ دسترسی همکار لازم است."
            return
        }
        viewModelScope.launch {
            busy = true
            val result = withContext(Dispatchers.IO) { runCatching {
                val identity = identityRepo.createApplicantForCollaborator(username, password)
                applicantRepo.submitForCollaborator(Applicant(firstName = firstName.trim(), lastName = lastName.trim(), username = identity.username, nationalId = nationalId.filter { it.isDigit() }, phone = phone.trim(), status = "new", consent = true, lang = lang), identity.uid).getOrThrow()
            } }
            busy = false
            message = if (result.isSuccess) "✅ حساب و پروندهٔ کارجو ساخته شد." else "⚠️ " + friendlyError(result.exceptionOrNull() ?: Exception(), "err_register_failed")
        }
    }

    fun adminAccountLogin(email: String, password: String, onResult: (Boolean, Boolean) -> Unit) {
        viewModelScope.launch {
            busy = true
            val result = AdminGate.signIn(email, password)
            busy = false
            if (result == AdminGate.LoginResult.SUCCESS) {
                adminMode = AdminMode.LIVE
                prefs.adminMode = AdminMode.LIVE
                onResult(true, true)
            } else {
                onResult(false, result != AdminGate.LoginResult.AUTH_REJECTED)
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

    fun saveHotel(hotel: Hotel) {
        viewModelScope.launch {
            busy = true
            val result = hotelRepo.save(hotel)
            busy = false
            message = if (result.isSuccess) "✅ " + hotel.name
            else "⚠️ " + friendlyError(result.exceptionOrNull() ?: IllegalStateException(), "err_register_failed")
        }
    }

    fun deleteHotel(hotel: Hotel) {
        viewModelScope.launch {
            val result = hotelRepo.delete(hotel.id)
            message = if (result.isSuccess) "🗑 " + hotel.name
            else "⚠️ " + friendlyError(result.exceptionOrNull() ?: IllegalStateException(), "err_register_failed")
        }
    }

    fun saveEssentialAddress(address: EssentialAddress) {
        viewModelScope.launch {
            busy = true
            val result = essentialAddressRepo.save(address)
            busy = false
            message = if (result.isSuccess) "✅ " + address.title
            else "⚠️ " + friendlyError(result.exceptionOrNull() ?: IllegalStateException(), "essential_save_failed")
        }
    }

    fun deleteEssentialAddress(address: EssentialAddress) {
        viewModelScope.launch {
            val result = essentialAddressRepo.delete(address.id)
            message = if (result.isSuccess) "🗑 " + address.title
            else "⚠️ " + friendlyError(result.exceptionOrNull() ?: IllegalStateException(), "essential_save_failed")
        }
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
        return l[key] + "\n(\u200E" + code + ")"
    }
}
