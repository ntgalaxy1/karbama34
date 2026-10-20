package com.karbama.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.karbama.app.data.model.AdminMode
import com.karbama.app.data.model.Job
import com.karbama.app.ui.AppViewModel
import com.karbama.app.ui.i18n.Loc
import com.karbama.app.ui.i18n.LocalLoc
import com.karbama.app.ui.components.brandBackdrop
import com.karbama.app.ui.components.EmptyState
import com.karbama.app.ui.components.SystemBars
import com.karbama.app.ui.components.TopBar
import com.karbama.app.ui.screens.*
import com.karbama.app.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: AppViewModel = viewModel()
            val loc = remember(vm.lang) { Loc(vm.lang) }

            CompositionLocalProvider(
                LocalLoc provides loc,
                LocalLayoutDirection provides if (loc.rtl) LayoutDirection.Rtl else LayoutDirection.Ltr
            ) {
                KarbamaTheme {
                    AppRoot(vm, loc)
                }
            }
        }
    }
}

private object Routes {
    const val LANGUAGE = "language"
    const val WELCOME = "welcome"
    const val REGISTER = "register"
    const val MAIN = "main"
    const val JOB = "job/{id}"
    const val LOGIN = "login"
    const val JOB_NEW = "job_edit"
    const val JOB_EDIT = "job_edit/{id}"
    const val APPLICANT = "applicant/{id}"
    const val EDIT_PROFILE = "edit_profile"
    const val PAY = "pay"                      // #35: صفحه‌ی پرداخت هزینه‌ی نمایش آگهی‌ها
    const val LANGUAGE_PICK = "language_pick"

    fun job(id: String) = "job/$id"
    fun jobEdit(id: String) = "job_edit/$id"
    fun applicant(id: String) = "applicant/$id"
}

@Composable
fun AppRoot(vm: AppViewModel, loc: Loc) {
    val nav = rememberNavController()
    val jobs by vm.jobs.collectAsState()
    val snackbarHost = remember { SnackbarHostState() }

    LaunchedEffect(vm.message) {
        val msg = vm.message
        if (msg != null) {
            snackbarHost.showSnackbar(msg)
            vm.message = null
        }
    }

    val start = when {
        vm.needsLanguagePick -> Routes.LANGUAGE
        vm.isRegistered -> Routes.MAIN
        else -> Routes.WELCOME
    }

    Scaffold(
        containerColor = Bg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHost) }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).brandBackdrop()) {
            NavHost(navController = nav, startDestination = start) {

                // ---------- انتخاب زبان ----------
                composable(Routes.LANGUAGE) {
                    LanguageScreen(loc = loc) { code ->
                        vm.applyLang(code)
                        val next = if (vm.isRegistered) Routes.MAIN else Routes.WELCOME
                        nav.navigate(next) {
                            popUpTo(Routes.LANGUAGE) { inclusive = true }
                        }
                    }
                }

                composable(Routes.LANGUAGE_PICK) {
                    LanguagePickerScreen(
                        loc = loc,
                        onPick = { vm.applyLang(it) },
                        onBack = { nav.popBackStack() }
                    )
                }

                // ---------- خوش‌آمدگویی ----------
                composable(Routes.WELCOME) {
                    WelcomeScreen(
                        loc = loc,
                        jobsCount = jobs.size,
                        peopleCount = if (vm.adminMode == AdminMode.LIVE) 0 else 6,
                        onStart = { nav.navigate(Routes.REGISTER) },
                        onLogin = { nav.navigate(Routes.LOGIN) },
                        onLanguage = { nav.navigate(Routes.LANGUAGE_PICK) }
                    )
                }

                // ---------- ثبت‌نام ----------
                composable(Routes.REGISTER) {
                    RegistrationScreen(
                        loc = loc,
                        busy = vm.busy,
                        brokerFee = vm.brokerFee,
                        brokerCard = vm.brokerCard,
                        brokerHolder = vm.brokerHolder,
                        onSubmit = { applicant, idBytes, bodyBytes, undertaking ->
                            vm.submitRegistration(applicant.copy(undertaking = undertaking), idBytes, bodyBytes) {
                                // صفحه‌ی «ثبت‌نام با موفقیت» حذف شده؛ مستقیم می‌رویم به آگهی‌ها
                                vm.message = loc["successToast"]
                                nav.navigate(Routes.MAIN) {
                                    popUpTo(nav.graph.id) { inclusive = true }
                                }
                            }
                        },
                        onBack = { nav.popBackStack() }
                    )
                }

                // ---------- بدنه‌ی اصلی (تب‌ها) ----------
                composable(Routes.MAIN) {
                    MainShell(
                        vm = vm,
                        loc = loc,
                        jobs = jobs,
                        onOpenJob = { nav.navigate(Routes.job(it.id)) },
                        onOpenLanguage = { nav.navigate(Routes.LANGUAGE_PICK) },
                        onOpenEditProfile = { nav.navigate(Routes.EDIT_PROFILE) },
                        onNewJob = { nav.navigate(Routes.JOB_NEW) },
                        onEditJob = { nav.navigate(Routes.jobEdit(it.id)) },
                        onOpenApplicant = { nav.navigate(Routes.applicant(it.id)) },
                        onOpenRegister = { nav.navigate(Routes.REGISTER) },
                        onOpenPay = { nav.navigate(Routes.PAY) },   // #35
                        onLoggedOut = {
                            nav.navigate(Routes.LOGIN) {
                                popUpTo(Routes.MAIN) { inclusive = true }
                            }
                        }
                    )
                }

                // ---------- جزئیات آگهی ----------
                composable(Routes.JOB) { entry ->
                    val id = entry.arguments?.getString("id") ?: ""
                    val job = jobs.firstOrNull { it.id == id }
                    if (!vm.adsUnlocked) {
                        // #35: تا تأیید پرداخت، به‌جای آگهی → صفحه‌ی پرداخت
                        PaymentScreen(
                            loc = loc,
                            applicant = vm.myProfile,
                            adsFee = vm.adsFee,
                            brokerCard = vm.brokerCard,
                            brokerHolder = vm.brokerHolder,
                            onRequestPay = { reference, receipt -> vm.requestManualAdsPayment(reference, receipt) },
                            onRecheck = { vm.refreshMyPayment() },
                            onDone = { nav.popBackStack() },
                            onBack = { nav.popBackStack() }
                        )
                    } else if (job != null) {
                        JobDetailScreen(
                            job = job,
                            loc = loc,
                            applied = vm.appliedIds.contains(job.id),
                            canApply = vm.isRegistered,
                            liked = vm.likedIds.contains(job.id),                  // #28
                            onLike = { vm.toggleLike(job.id) },                    // #28
                            onApply = { vm.applyToJob(job.id) },
                            onBack = { nav.popBackStack() }
                        )
                    } else {
                        MissingItemScreen(loc, onBack = { nav.popBackStack() })
                    }
                }

                // ---------- ورود به برنامه (کاربر و مدیر) ----------
                composable(Routes.LOGIN) {
                    val savedLogin = remember { vm.prefs.rememberedLogin() }
                    LoginScreen(
                        loc = loc,
                        busy = vm.busy,
                        savedUsername = savedLogin?.username.orEmpty(),
                        savedPassword = savedLogin?.password.orEmpty(),
                        onLogin = { user, pass, cb ->
                            vm.loginUser(user, pass) { outcome ->
                                cb(outcome)
                                when (outcome) {
                                    // مرحله‌ی دوم (کد 2FA) در همان صفحه پرسیده می‌شود
                                    LoginOutcome.ADMIN_2FA,
                                    LoginOutcome.FAIL,
                                    LoginOutcome.ADMIN_AUTH_REJECTED,
                                    LoginOutcome.ADMIN_NOT_AUTHORIZED,
                                    LoginOutcome.ADMIN_NETWORK_ERROR,
                                    LoginOutcome.ADMIN_FIREBASE_ERROR -> Unit
                                    else -> nav.navigate(Routes.MAIN) {
                                        popUpTo(Routes.LOGIN) { inclusive = true }
                                    }
                                }
                            }
                        },
                        onTwoFactor = { code, cb ->
                            val ok = vm.verifyTwoFactor(code)
                            cb(ok)
                            if (ok) {
                                vm.completeAdminLogin()
                                nav.navigate(Routes.MAIN) {
                                    popUpTo(Routes.LOGIN) { inclusive = true }
                                }
                            }
                        },
                        onRegister = { nav.navigate(Routes.REGISTER) },
                        onBack = { nav.popBackStack() }
                    )
                }

                // ---------- ویرایش/ایجاد آگهی ----------
                composable(Routes.JOB_NEW) {
                    JobEditorScreen(
                        loc = loc,
                        existing = null,
                        busy = vm.busy,
                        onSave = { job ->
                            vm.saveJob(job) { nav.popBackStack() }
                        },
                        onBack = { nav.popBackStack() }
                    )
                }

                composable(Routes.JOB_EDIT) { entry ->
                    val id = entry.arguments?.getString("id") ?: ""
                    val job = jobs.firstOrNull { it.id == id }
                    JobEditorScreen(
                        loc = loc,
                        existing = job,
                        busy = vm.busy,
                        onSave = { updated -> vm.saveJob(updated) { nav.popBackStack() } },
                        onBack = { nav.popBackStack() }
                    )
                }

                // ---------- پروفایل متقاضی (مدیر) ----------
                composable(Routes.APPLICANT) { entry ->
                    val id = entry.arguments?.getString("id") ?: ""
                    val live = vm.adminMode == AdminMode.LIVE
                    val list by remember(id, live) { vm.applicantsFlow(live) }
                        .collectAsState(initial = emptyList())
                    val person = list.firstOrNull { it.id == id }
                    // #29: پسندیده‌ها و درخواست‌های همین متقاضی — زنده از سرور
                    val activity by remember(id) { vm.watchApplicantActivity(id) }
                        .collectAsState(
                            initial = emptyList<com.karbama.app.data.repo.ActivityItem>() to
                                emptyList<com.karbama.app.data.repo.ActivityItem>()
                        )
                    if (person != null) {
                        ApplicantDetailScreen(
                            applicant = person,
                            loc = loc,
                            brokerFee = vm.brokerFee,
                            brokerCard = vm.brokerCard,
                            brokerHolder = vm.brokerHolder,
                            onSetStatus = { vm.setApplicantStatus(person.id, it) },
                            onSetFee = { vm.setApplicantFee(person, it) },
                            adsFee = vm.adsFee,                                       // #34
                            onSetPayment = { st -> vm.setApplicantPayment(person, st) },  // #34
                            onBack = { nav.popBackStack() },
                            likedItems = activity.first,
                            appliedItems = activity.second,
                            activityLive = live
                        )
                    } else {
                        MissingItemScreen(loc, onBack = { nav.popBackStack() })
                    }
                }

                // ---------- #35: صفحه‌ی پرداخت هزینه‌ی نمایش آگهی‌ها ----------
                composable(Routes.PAY) {
                    PaymentScreen(
                        loc = loc,
                        applicant = vm.myProfile,
                        adsFee = vm.adsFee,
                        brokerCard = vm.brokerCard,
                        brokerHolder = vm.brokerHolder,
                        onRequestPay = { reference, receipt -> vm.requestManualAdsPayment(reference, receipt) },
                        onRecheck = { vm.refreshMyPayment() },
                        onDone = { nav.popBackStack() },
                        onBack = { nav.popBackStack() }
                    )
                }

                // ---------- ویرایش پروفایل ----------
                composable(Routes.EDIT_PROFILE) {
                    val me = vm.myProfile
                    if (me != null) {
                        EditProfileScreen(
                            loc = loc,
                            applicant = me,
                            busy = vm.busy,
                            onSave = { vm.saveMyProfile(it) { nav.popBackStack() } },
                            onBack = { nav.popBackStack() }
                        )
                    } else {
                        MissingItemScreen(loc, onBack = { nav.popBackStack() })
                    }
                }
            }
        }
    }
}

/** بدنه‌ی برنامه با سه تب: آگهی‌ها، پروفایل، مدیریت */
@Composable
private fun MainShell(
    vm: AppViewModel,
    loc: Loc,
    jobs: List<Job>,
    onOpenJob: (Job) -> Unit,
    onOpenLanguage: () -> Unit,
    onOpenEditProfile: () -> Unit,
    onNewJob: () -> Unit,
    onEditJob: (Job) -> Unit,
    onOpenApplicant: (com.karbama.app.data.model.Applicant) -> Unit,
    onOpenRegister: () -> Unit,
    onOpenPay: () -> Unit,                     // #35
    onLoggedOut: () -> Unit
) {
    var tab by remember { mutableIntStateOf(0) }
    val isAdmin = vm.adminMode != AdminMode.NONE
    // #34: تا هزینه‌ی نمایش آگهی‌ها پرداخت و توسط مدیر تأیید نشود، هیچ آگهی‌ای نمایش داده نمی‌شود
    val adsUnlocked = vm.adsUnlocked

    val applicantsFlow = remember(vm.adminMode) {
        vm.applicantsFlow(vm.adminMode == AdminMode.LIVE)
    }
    val applicants by applicantsFlow.collectAsState(initial = emptyList())

    val visibleJobs = remember(jobs, vm.filter) { vm.filteredJobs(jobs) }
    val loading = !vm.jobsLoaded

    Scaffold(
        containerColor = Bg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        // مدیر که با یوزرنیم و پسورد خودش وارد می‌شود، پنل را تمام‌صفحه می‌بیند:
        // تب‌های پایین «آگهی‌ها» و «پروفایل من» نمایش داده نمی‌شوند.
        bottomBar = {
            if (!isAdmin) {
                // نوار پایین شناور و مدرن
                Surface(
                    color = GlassBar,
                    shape = RoundedCornerShape(22.dp),
                    border = BorderStroke(1.dp, GlassStroke),
                    shadowElevation = 16.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                ) {
                NavigationBar(
                    containerColor = Color.Transparent,
                    tonalElevation = 0.dp
                ) {
                    NavigationBarItem(
                        selected = tab == 0,
                        onClick = { tab = 0 },
                        icon = {
                            Text(if (adsUnlocked) "🏠" else "🔒", fontSize = 20.sp)   // v46: ایموجی دمو
                        },
                        label = {
                            Text(
                                if (adsUnlocked) loc["tab_jobs"] else loc["pay_locked_tab"],
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = navItemColors()
                    )
                    NavigationBarItem(
                        selected = tab == 1,
                        onClick = { tab = 1 },
                        icon = { Text("👤", fontSize = 20.sp) },
                        label = {
                            Text(loc["tab_profile"], style = MaterialTheme.typography.labelSmall)
                        },
                        colors = navItemColors()
                    )
                    // «ارتباط با ما» — کنار تب پروفایل، فقط برای کاربران عادی (مدیر پنل تمام‌صفحه دارد)
                    NavigationBarItem(
                        selected = tab == 2,
                        onClick = { tab = 2 },
                        icon = { Text("☎️", fontSize = 20.sp) },
                        label = {
                            Text(loc["tab_contact"], style = MaterialTheme.typography.labelSmall)
                        },
                        colors = navItemColors()
                    )
                }
                }
            }
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isAdmin -> AdminContent(
                    loc = loc,
                    mode = vm.adminMode,
                    jobs = jobs,
                    applicants = applicants,
                    onNewJob = onNewJob,
                    onEditJob = onEditJob,
                    onDeleteJob = { vm.deleteJob(it.id) },   // #26: تأیید داخل خود پنل مدیریت پرسیده می‌شود
                    onOpenApplicant = onOpenApplicant,
                    brokerCard = vm.brokerCard,
                    brokerHolder = vm.brokerHolder,
                    onSaveSettings = { card ->
                        vm.saveBrokerSettings(card, vm.brokerHolder)
                    },
                    twoFAEnabled = vm.twoFactorEnabled,
                    twoFAPending = vm.twoFactorPending,
                    twoFASecret = vm.twoFactorSecret,
                    twoFARecovery = vm.twoFactorRecovery,
                    twoFAUri = vm.twoFactorUri,
                    onBegin2FA = { vm.beginTwoFactorSetup() },
                    onConfirm2FA = { code -> vm.confirmTwoFactor(code) },
                    onDisable2FA = { vm.disableTwoFactor() },
                    onSetStatus = { id, st -> vm.setApplicantStatus(id, st) },
                    onSetFee = { person, fee -> vm.setApplicantFee(person, fee) },
                    adsFee = vm.adsFee,                                            // #34
                    onSaveAdsFee = { v -> vm.saveAdsFee(v) },                      // #34
                    onSetPayment = { person, st -> vm.setApplicantPayment(person, st) },  // #34
                    onExit = {
                        // خروج مدیر → برگشت به صفحه‌ی ورود
                        vm.exitAdminPanel()
                        onLoggedOut()
                    }
                )

                // #35: صفحه‌ی اصلی همان صفحه‌ی قبلی است؛ فقط بخش آگهی‌ها
                // تا تأیید پرداخت با پنل «نیاز به پرداخت» پوشیده می‌شود.
                tab == 0 -> HomeContent(
                    loc = loc,
                    me = vm.myProfile,
                    adsLocked = !adsUnlocked,
                    adsFee = vm.adsFee,
                    brokerCard = vm.brokerCard,
                    onGoPay = onOpenPay,
                    userName = vm.myProfile?.firstName ?: "",
                    jobs = visibleJobs,
                    filter = vm.filter,
                    appliedIds = vm.appliedIds,
                    likedIds = vm.likedIds,                  // #28
                    loading = loading,
                    onFilterChange = { vm.applyFilter(it) },
                    onOpenJob = onOpenJob,
                    onLanguage = onOpenLanguage
                )


                tab == 1 -> ProfileContent(
                    loc = loc,
                    applicant = vm.myProfile,
                    appliedCount = vm.appliedIds.size,
                    adminMode = vm.adminMode,
                    onRegister = onOpenRegister,
                    onEdit = onOpenEditProfile,
                    brokerFee = vm.brokerFee,
                    brokerCard = vm.brokerCard,
                    brokerHolder = vm.brokerHolder,
                    onLanguage = { vm.applyLang(it) },
                    onLogout = {
                        vm.logout()
                        tab = 0
                        onLoggedOut()
                    },
                    // #28: آگهی‌های پسندیده‌ی همین کاربر
                    likedJobs = jobs.filter { vm.likedIds.contains(it.id) },
                    onOpenJob = onOpenJob,
                    onUnlike = { vm.toggleLike(it.id) },
                    adsFee = vm.adsFee,            // #34
                    adsLocked = !adsUnlocked,      // #34
                    onGoPay = onOpenPay            // #35: رفتن به صفحه‌ی پرداخت
                )

                tab == 2 -> ContactContent(loc = loc)

            }
        }
    }

}

@Composable
private fun navItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = BrandDark,
    selectedTextColor = BrandDark,
    indicatorColor = BrandLight,
    unselectedIconColor = Ink3,
    unselectedTextColor = Ink3
)

@Composable
private fun MissingItemScreen(loc: Loc, onBack: () -> Unit) {
    SystemBars(darkIcons = true)
    Scaffold(
        containerColor = Bg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = { TopBar(title = loc["app_name"], onBack = onBack) }
    ) { p ->
        Box(Modifier.fillMaxSize().padding(p)) {
            EmptyState(
                emoji = "🤔",
                title = loc["load_error"],
                actionLabel = loc["retry"],
                onAction = onBack
            )
        }
    }
}
