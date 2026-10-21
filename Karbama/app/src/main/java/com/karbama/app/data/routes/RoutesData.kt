package com.karbama.app.data.routes

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * v52.0.2 — «مسیریابی»: اطلاعات مکان‌های مهم کشور مقصد کارجو (فرودگاه‌ها، مترو، متروبوس، تراموا، قطار شهری…).
 * داده‌ها داخل خود برنامه‌اند (assets/routes/<کشور>.json) و بدون اینترنت هم دیده می‌شوند.
 * منبع: © OpenStreetMap contributors (ODbL) — ساخته‌شده با kb-tools/routes/build_tr.py
 * کشورهای دیگر در آپدیت‌های بعدی: فقط یک فایل JSON تازه + یک خط در [Destinations.all].
 */
data class RoutePlace(val name: String, val lat: Double, val lon: Double, val code: String = "")

data class RouteLine(
    val kind: String,          // metro | metrobus | tram | rail | funicular
    val ref: String,           // M4, T1, 34, B1 …
    val color: Long,           // 0xFFRRGGBB
    val title: String,         // «Kadıköy – Sabiha Gökçen Havalimanı»
    val stations: List<RoutePlace>
)

data class RouteCity(
    val id: String,
    val names: Map<String, String>,   // tr / fa / en / ar
    val lat: Double,
    val lon: Double,
    val airports: List<RoutePlace>,
    val lines: List<RouteLine>
) {
    fun name(lang: String): String = names[lang] ?: names["tr"] ?: id
}

data class RouteCountry(
    val code: String,
    val updated: String,
    val source: String,
    val cities: List<RouteCity>
)

/** کشورهای مقصدی که کارجو می‌تواند انتخاب کند.
 * دادهٔ مسیریابی آفلاین در این انتشار فقط برای ترکیه بسته‌بندی شده است؛ مقصدهای دیگر
 * برای فرم/پروفایل ذخیره می‌شوند تا دادهٔ مستقلشان در انتشار بعدی افزوده شود.
 */
object Destinations {
    data class Dest(val code: String, val flag: String, val asset: String?, val nameKey: String)

    val all: List<Dest> = listOf(
        Dest("TR", "🇹🇷", "routes/tr.json", "route_country_tr"),
        Dest("AM", "🇦🇲", null, "route_country_am"),
        Dest("IQ", "🇮🇶", null, "route_country_iq"),
        Dest("DE", "🇩🇪", null, "route_country_de")
    )

    const val DEFAULT = "TR"

    fun of(code: String): Dest = all.firstOrNull { it.code == code } ?: all.first()

    /** پروفایل‌های قدیمی که هنوز مقصد ندارند → ترکیه */
    fun normalize(code: String?): String = if (code.isNullOrBlank() || all.none { it.code == code }) DEFAULT else code
}

object RoutesRepo {
    private val cache = HashMap<String, RouteCountry>()

    fun load(context: Context, countryCode: String): RouteCountry? {
        val dest = Destinations.of(countryCode)
        val asset = dest.asset ?: return null
        cache[dest.code]?.let { return it }
        return runCatching {
            val text = context.assets.open(asset).bufferedReader(Charsets.UTF_8).use { it.readText() }
            parse(text).also { cache[dest.code] = it }
        }.getOrNull()
    }

    fun parse(text: String): RouteCountry {
        val o = JSONObject(text)
        val cities = o.getJSONArray("cities")
        return RouteCountry(
            code = o.optString("country"),
            updated = o.optString("updated"),
            source = o.optString("src"),
            cities = List(cities.length()) { i -> city(cities.getJSONObject(i)) }
        )
    }

    private fun city(c: JSONObject): RouteCity {
        val n = c.getJSONObject("n")
        val names = buildMap { n.keys().forEach { k -> put(k, n.getString(k)) } }
        val air = c.optJSONArray("air") ?: JSONArray()
        val lines = c.optJSONArray("lines") ?: JSONArray()
        return RouteCity(
            id = c.getString("id"),
            names = names,
            lat = c.optDouble("lat"),
            lon = c.optDouble("lon"),
            airports = List(air.length()) { i ->
                val a = air.getJSONArray(i)
                RoutePlace(a.getString(0), a.getDouble(2), a.getDouble(3), code = a.getString(1))
            },
            lines = List(lines.length()) { i ->
                val l = lines.getJSONObject(i)
                val st = l.getJSONArray("s")
                RouteLine(
                    kind = l.getString("k"),
                    ref = l.optString("r"),
                    color = parseColor(l.optString("c")),
                    title = l.optString("n"),
                    stations = List(st.length()) { j ->
                        val s = st.getJSONArray(j)
                        RoutePlace(s.getString(0), s.getDouble(1), s.getDouble(2))
                    }
                )
            }
        )
    }

    private fun parseColor(hex: String): Long =
        runCatching { 0xFF000000L or hex.removePrefix("#").toLong(16) }.getOrDefault(0xFF0E9F8EL)
}
