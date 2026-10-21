package com.karbama.app

import android.graphics.Bitmap
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.LayoutDirection
import com.karbama.app.data.sample.Catalog
import com.karbama.app.data.model.Hotel
import com.karbama.app.ui.i18n.Loc
import com.karbama.app.ui.i18n.LocalLoc
import com.karbama.app.ui.screens.AdminContent
import com.karbama.app.ui.theme.KarbamaTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/** Renders the admin «Settings» tab to PNG (1 dp = 1 px, 412 wide — same as the demo screenshot). */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w412dp-h1900dp-mdpi", application = android.app.Application::class)
class AdminSettingsShot {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    private fun shot(name: String, twoFA: Boolean, pending: Boolean = false) {
        val loc = Loc("fa")
        val people = Catalog.sampleApplicants()
        rule.setContent {
            CompositionLocalProvider(
                LocalLoc provides loc,
                LocalLayoutDirection provides LayoutDirection.Rtl
            ) {
                KarbamaTheme {
                    AdminContent(
                        loc = loc, mode = "live", jobs = Catalog.sampleJobs(), applicants = people,
                        onNewJob = {}, onEditJob = {}, onDeleteJob = {}, onOpenApplicant = {},
                        brokerCard = "1234123412341234", brokerHolder = "",
                        onSaveSettings = {},
                        twoFAEnabled = twoFA, twoFAPending = pending,
                        twoFASecret = "JBSWY3DPEHPK3PXP", twoFARecovery = "ABCD-1234",
                        twoFAUri = "otpauth://totp/Karbama:admin?secret=JBSWY3DPEHPK3PXP&issuer=Karbama",
                        onBegin2FA = {}, onConfirm2FA = { false }, onDisable2FA = {},
                        onSetStatus = { _, _ -> }, onSetFee = { _, _ -> },
                        adsFee = 500L, onExit = {}, initialTab = 2,
                        adminEmail = "admin@karbama.app"
                    )
                }
            }
        }
        rule.waitForIdle()
        val root = rule.activity.window.decorView
        val bmp = Bitmap.createBitmap(root.width, root.height, Bitmap.Config.ARGB_8888)
        rule.runOnUiThread { root.draw(android.graphics.Canvas(bmp)) }
        val out = File(System.getProperty("kbshots.dir", "/home/user/kb-tools/shots"), "$name.png")
        out.outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
        println("SHOT " + out + " " + bmp.width + "x" + bmp.height)
    }

    @Test fun settings() = shot("android-settings", twoFA = false)
    @Test fun settingsPending() = shot("android-settings-pending", twoFA = false, pending = true)
    @Test fun settingsOn() = shot("android-settings-on", twoFA = true)

    @Test fun settingsShowsHotelManager() {
        val loc = Loc("fa")
        rule.setContent {
            CompositionLocalProvider(LocalLoc provides loc, LocalLayoutDirection provides LayoutDirection.Rtl) {
                KarbamaTheme {
                    AdminContent(
                        loc = loc, mode = "live", jobs = Catalog.sampleJobs(), applicants = Catalog.sampleApplicants(),
                        hotels = listOf(Hotel(id = "hotel-1", cityId = "istanbul", name = "هتل آزمون", address = "Beyoğlu", lat = 41.03, lon = 28.97)),
                        onNewJob = {}, onEditJob = {}, onDeleteJob = {}, onOpenApplicant = {},
                        brokerCard = "1234123412341234", brokerHolder = "", onSaveSettings = {},
                        twoFAEnabled = false, twoFAPending = false, twoFASecret = "", twoFARecovery = "", twoFAUri = "",
                        onBegin2FA = {}, onConfirm2FA = { false }, onDisable2FA = {},
                        onSetStatus = { _, _ -> }, onSetFee = { _, _ -> }, adsFee = 500L,
                        onSaveHotel = {}, onDeleteHotel = {}, onExit = {}, initialTab = 2
                    )
                }
            }
        }
        rule.waitForIdle()
        rule.onNodeWithText("مدیریت هتل‌های مسیریابی").performScrollTo()
        rule.onAllNodesWithText("مدیریت هتل‌های مسیریابی").assertCountEquals(1)
        rule.onAllNodesWithText("هتل آزمون").assertCountEquals(1)
        rule.onAllNodesWithText("افزودن هتل", substring = true).assertCountEquals(1)
        val root = rule.activity.window.decorView
        val bmp = Bitmap.createBitmap(root.width, root.height, Bitmap.Config.ARGB_8888)
        rule.runOnUiThread { root.draw(android.graphics.Canvas(bmp)) }
        val out = File(System.getProperty("kbshots.dir", "/home/user/kb-tools/shots"), "android-settings-hotels.png")
        out.outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
        println("SHOT $out ${bmp.width}x${bmp.height}; hotel manager controls OK")
    }
}
