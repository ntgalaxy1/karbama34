package com.karbama.app

import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.LayoutDirection
import com.karbama.app.data.model.Applicant
import com.karbama.app.data.routes.Destinations
import com.karbama.app.data.routes.RoutesRepo
import com.karbama.app.data.routes.TurkeyHospitals
import com.karbama.app.ui.i18n.Loc
import com.karbama.app.ui.i18n.LocalLoc
import com.karbama.app.ui.screens.RouteContent
import com.karbama.app.ui.theme.KarbamaTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.RuntimeEnvironment
import java.io.File

/**
 * v52.0.2 routing data safety-net.  The app carries OSM-derived Turkish destination
 * data locally; this proves the APK asset contains the agreed country-wide dataset,
 * not an empty/online-only placeholder.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w412dp-h1900dp-mdpi", application = android.app.Application::class)
class RoutingDataTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test fun turkeyAssetHasCountryWideTransportAndAirportData() {
        val tr = RoutesRepo.load(RuntimeEnvironment.getApplication(), "TR")!!
        assertEquals("TR", tr.code)
        assertEquals(51, tr.cities.size)
        assertEquals(51, tr.cities.sumOf { it.airports.size })
        assertEquals(66, tr.cities.sumOf { it.lines.size })
        assertEquals(1202, tr.cities.sumOf { c -> c.lines.sumOf { it.stations.size } })

        val istanbul = tr.cities.first { it.id == "istanbul" }
        assertTrue(istanbul.airports.any { it.code == "IST" && it.name.contains("İstanbul") })
        assertTrue(istanbul.airports.any { it.code == "SAW" && it.name.contains("Sabiha") })
        assertTrue(istanbul.lines.any { it.ref == "M4" && it.stations.any { s -> s.name.contains("Sabiha Gökçen") } })
        assertTrue(istanbul.lines.any { it.ref == "34" && it.stations.any { s -> s.name.contains("Söğütlüçeşme") } })
        println("ROUTES data: cities=${tr.cities.size}, airports=${tr.cities.sumOf { it.airports.size }}, lines=${tr.cities.sumOf { it.lines.size }}, stations=${tr.cities.sumOf { c -> c.lines.sumOf { it.stations.size } }} OK")
    }

    @Test fun destinationChoicesRetainTurkeyOnlyOfflineRouting() {
        assertEquals("TR", Destinations.normalize(null))
        assertEquals("TR", Destinations.normalize(""))
        assertEquals("TR", Destinations.normalize("TR"))
        assertEquals("AM", Destinations.normalize("AM"))
        assertEquals("IQ", Destinations.normalize("IQ"))
        assertEquals("DE", Destinations.normalize("DE"))
        assertEquals(listOf("TR", "AM", "IQ", "DE"), Destinations.all.map { it.code })
        assertEquals("routes/tr.json", Destinations.of("TR").asset)
        assertEquals(null, Destinations.of("AM").asset)
        assertEquals(null, Destinations.of("IQ").asset)
        assertEquals(null, Destinations.of("DE").asset)
        assertEquals(null, RoutesRepo.load(RuntimeEnvironment.getApplication(), "DE"))
        println("ROUTES destinations: TR/AM/IQ/DE selectable; offline asset remains TR only OK")
    }

    @Test fun nonTurkeyDestinationExplainsWhyOfflineRouteDataIsNotShown() {
        val loc = Loc("fa")
        rule.setContent {
            CompositionLocalProvider(LocalLoc provides loc, LocalLayoutDirection provides LayoutDirection.Rtl) {
                KarbamaTheme { RouteContent(loc, Applicant(firstName = "علی", destinationCountry = "DE")) }
            }
        }
        rule.waitForIdle()
        rule.onAllNodesWithText("راهنمای مسیر هنوز آماده نیست").assertCountEquals(1)
        rule.onAllNodesWithText("مقصد شما آلمان است.", substring = true).assertCountEquals(1)
        println("ROUTES UI: Germany clearly reports that offline data is pending OK")
    }

    @Test fun bundledHospitalsHaveNamedMappablePlacesForEveryTurkeyCity() {
        val tr = RoutesRepo.load(RuntimeEnvironment.getApplication(), "TR")!!
        val places = tr.cities.flatMap { city -> TurkeyHospitals.forCity(city.id) }
        assertEquals(tr.cities.size, tr.cities.count { TurkeyHospitals.forCity(it.id).isNotEmpty() })
        assertTrue(places.size >= 250)
        assertTrue(places.all { it.name.isNotBlank() && it.lat in -90.0..90.0 && it.lon in -180.0..180.0 })
        println("ROUTES hospitals: cities=${tr.cities.size}, places=${places.size} bundled from OSM OK")
    }

    @Test fun routeTabRendersIstanbulAndExpandsAnAirportMetroLine() {
        val loc = Loc("fa")
        rule.setContent {
            CompositionLocalProvider(LocalLoc provides loc, LocalLayoutDirection provides LayoutDirection.Rtl) {
                KarbamaTheme { RouteContent(loc, Applicant(firstName = "علی", destinationCountry = "TR")) }
            }
        }
        rule.waitForIdle()
        rule.onAllNodesWithText("مسیریابی", substring = true).assertCountEquals(1)
        rule.onAllNodesWithText("فرودگاه‌ها").assertCountEquals(1)
        rule.onAllNodesWithText("بیمارستان‌ها").assertCountEquals(1)
        rule.onAllNodesWithText("هتل‌ها").assertCountEquals(1)
        rule.onAllNodesWithText("هتلی برای این شهر ثبت نشده است.", substring = true).assertCountEquals(1)
        rule.onNodeWithText("Kadıköy – Sabiha Gökçen Havalimanı").performClick()
        rule.waitForIdle()
        rule.onAllNodesWithText("Sabiha Gökçen Havalimanı").assertCountEquals(1)
        shot("android-routing-istanbul")
        println("ROUTES UI: Istanbul, airports and M4 expanded OK")
    }

    private fun shot(name: String) {
        val view = rule.activity.window.decorView
        val bmp = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        rule.runOnUiThread { view.draw(android.graphics.Canvas(bmp)) }
        val out = File(System.getProperty("kbshots.dir", "/home/user/kb-tools/shots"), "$name.png")
        out.outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
        println("SHOT $out ${bmp.width}x${bmp.height}")
    }
}
