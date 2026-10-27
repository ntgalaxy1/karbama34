package com.karbama.app

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.LocalLayoutDirection
import com.karbama.app.data.model.EssentialAddressSamples
import com.karbama.app.ui.i18n.Loc
import com.karbama.app.ui.i18n.LocalLoc
import com.karbama.app.ui.screens.EssentialAddressesContent
import com.karbama.app.ui.theme.KarbamaTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * v10.0.5 regression guard: a new or offline live collection must still show
 * the approved, bundled official directory instead of an empty screen.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w412dp-h1900dp-mdpi", application = android.app.Application::class)
class EssentialAddressDataTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test fun bundledDirectoryHasEightActiveValidatedRecords() {
        val records = EssentialAddressSamples.default()
        assertEquals(8, records.size)
        assertEquals(8, records.map { it.id }.toSet().size)
        assertTrue(records.all {
            it.countryCode == "TR" &&
                it.active &&
                it.cityId.length >= 2 &&
                it.title.length >= 2 &&
                it.phone.isNotBlank() &&
                it.category in setOf("immigration", "health", "legal", "transport", "other")
        })
        assertTrue(records.all { (it.lat == null) == (it.lon == null) })
        val migrationOffice = records.first { it.id == "official_istanbul_migration_office" }
        assertEquals(41.020687, migrationOffice.lat!!, 0.000001)
        assertEquals(28.935617, migrationOffice.lon!!, 0.000001)
        assertTrue(records.any { it.phone == "112" })
        assertTrue(records.any { it.phone == "157" })
        assertTrue(records.any { it.phone == "183" })
        println("ESSENTIAL directory: ${records.size} approved bundled records OK")
    }

    @Test fun essentialScreenRendersBundledDirectoryInsteadOfEmptyState() {
        val loc = Loc("fa")
        rule.setContent {
            CompositionLocalProvider(LocalLoc provides loc, LocalLayoutDirection provides LayoutDirection.Rtl) {
                KarbamaTheme { EssentialAddressesContent(loc, EssentialAddressSamples.default()) }
            }
        }
        rule.waitForIdle()
        rule.onAllNodesWithText("آدرس‌های ضروری ترکیه").assertCountEquals(1)
        rule.onAllNodesWithText("اداره مهاجرت استان استانبول").assertCountEquals(1)
        rule.onAllNodesWithText("نشانی پیدا نشد").assertCountEquals(0)
        println("ESSENTIAL UI: approved directory visible without Firestore documents OK")
    }
}
