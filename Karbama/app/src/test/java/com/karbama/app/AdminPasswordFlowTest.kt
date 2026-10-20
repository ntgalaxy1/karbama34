package com.karbama.app

import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.LayoutDirection
import com.karbama.app.data.sample.Catalog
import com.karbama.app.ui.i18n.Loc
import com.karbama.app.ui.i18n.LocalLoc
import com.karbama.app.ui.screens.AdminContent
import com.karbama.app.ui.theme.KarbamaTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/**
 * v52: the admin password can be changed from inside the app.
 * Simulates Firebase: without the current password → "need_current" (requires-recent-login);
 * wrong current password → "wrong_current"; correct one → "ok".
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w412dp-h1900dp-mdpi", application = android.app.Application::class)
class AdminPasswordFlowTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    private val isPassword = SemanticsMatcher.keyIsDefined(SemanticsProperties.Password)

    @Test fun changePasswordInsideApp() {
        val loc = Loc("fa")
        val calls = mutableListOf<Pair<String, String?>>()
        rule.mainClock.autoAdvance = false      // blinking cursor never idles under Robolectric
        rule.setContent {
            CompositionLocalProvider(LocalLoc provides loc, LocalLayoutDirection provides LayoutDirection.Rtl) {
                KarbamaTheme {
                    AdminContent(
                        loc = loc, mode = "live", jobs = Catalog.sampleJobs(), applicants = Catalog.sampleApplicants(),
                        onNewJob = {}, onEditJob = {}, onDeleteJob = {}, onOpenApplicant = {},
                        brokerCard = "1234123412341234", brokerHolder = "", onSaveSettings = {},
                        twoFAEnabled = false, twoFAPending = false, twoFASecret = "", twoFARecovery = "", twoFAUri = "",
                        onBegin2FA = {}, onConfirm2FA = { false }, onDisable2FA = {},
                        onSetStatus = { _, _ -> }, onSetFee = { _, _ -> },
                        adsFee = 500L, onExit = {}, initialTab = 2,
                        adminEmail = "admin@karbama.app",
                        onChangeAdminPassword = { newP, cur, cb ->
                            calls += newP to cur
                            cb(
                                when (cur) {
                                    null -> "need_current"
                                    "old-pass1" -> "ok"
                                    else -> "wrong_current"
                                }
                            )
                        }
                    )
                }
            }
        }
        rule.mainClock.advanceTimeBy(800)
        // 1) type a new admin password and press the second «💾 ذخیره» (admin panel section)
        rule.onNode(isPassword and hasSetTextAction()).performScrollTo().performTextInput("newpass1")
        rule.onAllNodesWithText("💾", substring = true)[1].performScrollTo().performClick()
        rule.mainClock.advanceTimeBy(800)
        assertEquals("newpass1" to null, calls.last())
        println("STEP1 save → need_current OK")

        // 2) the dialog asks for the current password
        rule.onNodeWithText(loc["adm_cur_title"]).assertExists()
        val dialogField = rule.onAllNodes(isPassword and hasSetTextAction())
        dialogField.assertCountEquals(2)
        dialogField[1].performTextInput("wrong-pass")
        rule.onNodeWithText("✅ " + loc["adm_cur_ok"]).performClick()
        rule.mainClock.advanceTimeBy(800)
        assertEquals("newpass1" to "wrong-pass", calls.last())
        rule.onNodeWithText("⚠️ " + loc["adm_cur_wrong"]).assertExists()
        println("STEP2 wrong current → error shown OK")

        // 3) correct current password → changed, dialog closes, field cleared
        rule.onAllNodes(isPassword and hasSetTextAction())[1].performTextClearance()
        rule.onAllNodes(isPassword and hasSetTextAction())[1].performTextInput("old-pass1")
        rule.onNodeWithText("✅ " + loc["adm_cur_ok"]).performClick()
        rule.mainClock.advanceTimeBy(800)
        assertEquals("newpass1" to "old-pass1", calls.last())
        rule.onAllNodesWithText(loc["adm_cur_title"]).assertCountEquals(0)
        val txt = rule.onNode(isPassword and hasSetTextAction()).fetchSemanticsNode()
            .config[SemanticsProperties.EditableText].text
        assertEquals("", txt)
        println("STEP3 correct current → changed, dialog closed, field cleared OK (calls=${calls.size})")
    }

    /** screenshot of the «current password» overlay */
    @Test fun dialogOpensWithoutFocus() {
        val loc = Loc("fa")
        rule.setContent {
            CompositionLocalProvider(LocalLoc provides loc, LocalLayoutDirection provides LayoutDirection.Rtl) {
                KarbamaTheme {
                    AdminContent(
                        loc = loc, mode = "live", jobs = Catalog.sampleJobs(), applicants = Catalog.sampleApplicants(),
                        onNewJob = {}, onEditJob = {}, onDeleteJob = {}, onOpenApplicant = {},
                        brokerCard = "1234123412341234", brokerHolder = "", onSaveSettings = {},
                        twoFAEnabled = false, twoFAPending = false, twoFASecret = "", twoFARecovery = "", twoFAUri = "",
                        onBegin2FA = {}, onConfirm2FA = { false }, onDisable2FA = {},
                        onSetStatus = { _, _ -> }, onSetFee = { _, _ -> },
                        adsFee = 500L, onExit = {}, initialTab = 2, adminEmail = "admin@karbama.app",
                        onChangeAdminPassword = { _, _, cb -> cb("need_current") }
                    )
                }
            }
        }
        rule.onAllNodesWithText("💾", substring = true)[1].performScrollTo().performClick()
        println("DIAG clicked")
        rule.onNodeWithText(loc["adm_cur_title"]).assertExists()
        println("DIAG overlay shown OK")
        shotDialog("android-admin-pass-dialog")
    }

    private fun shotDialog(name: String) {
        runCatching {
            val v = rule.activity.window.decorView
            val bmp = Bitmap.createBitmap(v.width, v.height, Bitmap.Config.ARGB_8888)
            rule.runOnUiThread { v.draw(android.graphics.Canvas(bmp)) }
            val out = File(System.getProperty("kbshots.dir", "/home/user/kb-tools/shots"), "$name.png")
            out.outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
            println("SHOT $out ${bmp.width}x${bmp.height}")
        }.onFailure { println("SHOT dialog failed: $it") }
    }
}
