package com.karbama.app.data.local

import com.karbama.app.data.model.Applicant
import com.karbama.app.data.model.Job
import com.karbama.app.data.sample.Catalog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * «حالت نمایشی»: همه‌ی آگهی‌ها و متقاضیان روی خود گوشی نگه‌داری می‌شوند.
 * وقتی Firebase تنظیم شود، این کلاس دیگر استفاده نمی‌شود.
 */
object LocalStore {

    private var prefs: AppPrefs? = null

    val jobs = MutableStateFlow<List<Job>>(emptyList())
    val applicants = MutableStateFlow<List<Applicant>>(emptyList())

    fun init(p: AppPrefs) {
        prefs = p
        jobs.value = p.localJobs() ?: Catalog.sampleJobs().also { p.saveLocalJobs(it) }
        applicants.value = p.localApplicants() ?: Catalog.sampleApplicants().also { p.saveLocalApplicants(it) }
    }

    // ---------- آگهی‌ها ----------

    fun upsertJob(job: Job) {
        val list = jobs.value.toMutableList()
        val i = list.indexOfFirst { it.id == job.id }
        if (i >= 0) list[i] = job else list.add(job)
        jobs.value = list
        prefs?.saveLocalJobs(list)
    }

    fun deleteJob(id: String) {
        val list = jobs.value.filterNot { it.id == id }
        jobs.value = list
        prefs?.saveLocalJobs(list)
    }

    // ---------- متقاضیان ----------

    fun upsertApplicant(a: Applicant) {
        val list = applicants.value.toMutableList()
        val i = list.indexOfFirst { it.id == a.id }
        if (i >= 0) list[i] = a else list.add(0, a)
        applicants.value = list
        prefs?.saveLocalApplicants(list)
    }

    fun setStatus(id: String, status: String) {
        applicants.value.firstOrNull { it.id == id }?.let {
            upsertApplicant(it.copy(status = status))
        }
    }

    /** #34: تغییر وضعیت پرداخت هزینه‌ی نمایش آگهی‌ها (کارجو: pending — مدیر: paid/خالی) */
    fun setPayment(id: String, status: String, at: Long) {
        applicants.value.firstOrNull { it.id == id }?.let {
            upsertApplicant(it.copy(payStatus = status, payAt = at))
        }
    }

    /** تغییر مبلغ تعهدنامه‌ی یک متقاضی خاص (فقط مدیر) */
    fun setFee(id: String, fee: Long) {
        applicants.value.firstOrNull { it.id == id }?.let { p ->
            val old = p.undertaking
            val und = (
                old ?: com.karbama.app.data.model.Undertaking(
                    signed = true,
                    date = com.karbama.app.util.FormatUtils.todayIso()
                )
            ).copy(fee = fee)
            upsertApplicant(p.copy(undertaking = und))
        }
    }

    /** یافتن متقاضی با نام کاربری، شماره تلفن یا شناسه (برای ورود) */
    fun findByUsername(user: String): Applicant? {
        val u = user.trim().lowercase()
        val dg = u.filter { it.isDigit() }
        return applicants.value.firstOrNull { a ->
            val byName = a.username.trim().isNotEmpty() && a.username.trim().lowercase() == u
            val byPhone = dg.length > 6 && (
                com.karbama.app.util.AuthUtils.digits(a.phone) == dg ||
                    com.karbama.app.util.AuthUtils.digits(a.phone).endsWith(dg)
                )
            byName || byPhone
        }
    }

    fun reset() {
        jobs.value = Catalog.sampleJobs().also { prefs?.saveLocalJobs(it) }
        applicants.value = Catalog.sampleApplicants().also { prefs?.saveLocalApplicants(it) }
    }
}
