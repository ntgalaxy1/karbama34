package com.karbama.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karbama.app.ui.components.*
import com.karbama.app.ui.i18n.Loc
import com.karbama.app.ui.i18n.langFlag
import com.karbama.app.ui.i18n.langLabel
import com.karbama.app.ui.theme.*

/** ۱) انتخاب زبان */
@Composable
fun LanguageScreen(loc: Loc, onPick: (String) -> Unit) {
    SystemBars(darkIcons = false)
    Box(
        Modifier
            .fillMaxSize()
            .background(HeaderBrush)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            BrandLogo(size = 86.dp, onDark = true)
            Spacer(Modifier.height(16.dp))
            Text(
                "کارباما",
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Text(
                "KARBAMA",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.75f),
                letterSpacing = 4.sp
            )
            Spacer(Modifier.height(28.dp))
            Text(
                loc["lang_title"],
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                loc["lang_hint"],
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(20.dp))

            listOf("fa", "tr", "en").forEach { code ->
                LanguageRow(code = code, onClick = { onPick(code) })
                Spacer(Modifier.height(8.dp))
            }

            Spacer(Modifier.height(16.dp))
            Text(
                text = loc["tagline"],
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(   // #28: کم‌رنگ‌تر و کم‌تر
                text = "powered by naser tavakkoli @nt.galaxy",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.42f),
                letterSpacing = 0.3.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun LanguageRow(code: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        color = GlassFill,
        border = BorderStroke(1.dp, GlassStroke),
        shadowElevation = 10.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(langFlag(code), fontSize = 24.sp)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    langLabel(code),
                    style = MaterialTheme.typography.titleMedium,
                    color = Ink
                )
                Text(
                    when (code) {
                        "tr" -> "Türkiye'nin iş ilanları"
                        "en" -> "Job listings in Türkiye"
                        else -> "آگهی‌های استخدام ترکیه"
                    },
                    style = MaterialTheme.typography.bodySmall
                )
            }
            SecondaryArrowForward()
        }
    }
}

/** ۲) خوش‌آمدگویی */
@Composable
fun WelcomeScreen(
    loc: Loc,
    jobsCount: Int,
    peopleCount: Int,
    onStart: () -> Unit,
    onLogin: () -> Unit,
    onLanguage: () -> Unit
) {
    SystemBars(darkIcons = false)
    Column(
        Modifier
            .fillMaxSize()
            .background(Bg)
            .verticalScroll(rememberScrollState())
    ) {
        // هدر
        Column(
            Modifier
                .fillMaxWidth()
                .background(HeaderBrush)
                .statusBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BrandLogo(size = 76.dp, onDark = true)
            Spacer(Modifier.height(12.dp))
            Text(loc["app_name"], fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.White)
            Text(
                loc["app_latin"],
                fontSize = 11.sp,
                letterSpacing = 4.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.8f)
            )
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatBox(jobsCount.toString(), loc["stat_jobs"], Modifier.weight(1f))
                StatBox(peopleCount.toString(), loc["stat_people"], Modifier.weight(1f))
                StatBox("13", loc["stat_cities"], Modifier.weight(1f))
            }
        }

        Column(Modifier.padding(16.dp)) {
            Text(
                loc["welcome_title"],
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(8.dp))
            Text(
                loc["welcome_sub"],
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 26.sp
            )
            Spacer(Modifier.height(16.dp))

            HowRow(Icons.Filled.BusinessCenter, loc["how_1"])
            HowRow(Icons.Filled.Search, loc["how_2"])
            HowRow(Icons.Filled.Send, loc["how_3"])

            Spacer(Modifier.height(16.dp))
            PrimaryButton(text = "🚀 " + loc["start_btn"], onClick = onStart)
            Spacer(Modifier.height(8.dp))
            GhostButton(
                text = "🔑 " + loc["login_title"],
                onClick = onLogin
            )
            Spacer(Modifier.height(16.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onLanguage),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🌐 ", fontSize = 14.sp)
                Text(
                    loc["language"] + ": ",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    langLabel(loc.code),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = BrandDark
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                loc["tagline"],
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun StatBox(value: String, label: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(Color.White.copy(alpha = 0.16f))
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color.White)
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.9f))
        }
    }
}

@Composable
private fun HowRow(icon: ImageVector, text: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(BrandLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = BrandDark, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = Ink2,
            fontWeight = FontWeight.Medium
        )
    }
}
