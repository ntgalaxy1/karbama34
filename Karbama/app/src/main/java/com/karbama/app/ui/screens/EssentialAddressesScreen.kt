package com.karbama.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karbama.app.data.model.EssentialAddress
import com.karbama.app.data.model.EssentialAddressCategory
import com.karbama.app.ui.components.EmptyState
import com.karbama.app.ui.components.HorizontalChipRow
import com.karbama.app.ui.components.SectionCard
import com.karbama.app.ui.components.SelectChip
import com.karbama.app.ui.components.SystemBars
import com.karbama.app.ui.i18n.Loc
import com.karbama.app.ui.theme.Bg
import com.karbama.app.ui.theme.Brand
import com.karbama.app.ui.theme.BrandBrush
import com.karbama.app.ui.theme.BrandDark
import com.karbama.app.ui.theme.BrandLight
import com.karbama.app.ui.theme.CardBg
import com.karbama.app.ui.theme.FieldBg
import com.karbama.app.ui.theme.Ink
import com.karbama.app.ui.theme.Ink2
import com.karbama.app.ui.theme.Ink3
import com.karbama.app.ui.theme.Line
import com.karbama.app.ui.theme.RCard
import com.karbama.app.ui.theme.RField

/** v10.0.4 — آدرس‌ها و تماس‌های ضروری ترکیه. */
@Composable
fun EssentialAddressesContent(loc: Loc, addresses: List<EssentialAddress>) {
    SystemBars(darkIcons = false)
    var query by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf(EssentialAddressCategory.ALL) }
    var cityId by rememberSaveable { mutableStateOf("all") }
    val cities = remember(addresses) {
        listOf("all") + addresses.map { it.cityId }.filter { it.isNotBlank() }.distinct().sorted()
    }
    val results = remember(addresses, query, category, cityId) {
        val needle = query.trim().lowercase()
        addresses.filter { item ->
            item.active &&
                (category == EssentialAddressCategory.ALL || item.category == category) &&
                (cityId == "all" || item.cityId == cityId) &&
                (needle.isBlank() || listOf(item.title, item.titleTr, item.address, item.phone, item.description)
                    .any { it.lowercase().contains(needle) })
        }
    }

    Column(Modifier.fillMaxSize().background(Bg)) {
        Box(
            Modifier.fillMaxWidth().clip(RCard).background(BrandBrush)
                .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 20.dp)
        ) {
            Column {
                Text(loc["essential_title"], style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(4.dp))
                Text(loc["essential_subtitle"], style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = .86f))
            }
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                OutlinedTextField(
                    value = query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                    placeholder = { Text(loc["essential_search"], color = Ink3) },
                    leadingIcon = { Icon(Icons.Filled.Search, null, tint = Ink3) }, shape = RField,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = FieldBg, unfocusedContainerColor = FieldBg,
                        focusedBorderColor = Brand, unfocusedBorderColor = Line,
                        focusedTextColor = Ink, unfocusedTextColor = Ink, cursorColor = Brand
                    )
                )
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(loc["essential_filter_category"], style = MaterialTheme.typography.labelMedium, color = Ink2)
                    HorizontalChipRow {
                        SelectChip(
                            text = loc["essential_all"],
                            selected = category == EssentialAddressCategory.ALL,
                            onClick = { category = EssentialAddressCategory.ALL }
                        )
                        EssentialAddressCategory.all.forEach { key ->
                            SelectChip(
                                text = loc["essential_category_$key"],
                                selected = category == key,
                                onClick = { category = key }
                            )
                        }
                    }
                    Text(loc["essential_filter_city"], style = MaterialTheme.typography.labelMedium, color = Ink2)
                    HorizontalChipRow {
                        cities.forEach { city ->
                            SelectChip(
                                text = cityLabel(city, loc),
                                selected = cityId == city,
                                onClick = { cityId = city }
                            )
                        }
                    }
                }
            }
            if (results.isEmpty()) item { EmptyState("📍", loc["essential_empty"], loc["essential_empty_hint"]) }
            else items(results, key = { it.id }) { EssentialAddressCard(loc, it) }
        }
    }
}

@Composable
private fun EssentialAddressCard(loc: Loc, address: EssentialAddress) {
    val context = LocalContext.current
    SectionCard {
        Row(verticalAlignment = Alignment.Top) {
            Box(Modifier.size(42.dp).clip(RField).background(BrandLight), contentAlignment = Alignment.Center) {
                Text(categoryEmoji(address.category), fontSize = 20.sp)
            }
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Text(address.title, style = MaterialTheme.typography.titleSmall, color = Ink)
                if (address.titleTr.isNotBlank()) Text(address.titleTr, style = MaterialTheme.typography.bodySmall, color = Ink3)
            }
        }
        if (address.address.isNotBlank()) { Spacer(Modifier.height(12.dp)); DetailLine("📍", address.address) }
        if (address.hours.isNotBlank()) { Spacer(Modifier.height(7.dp)); DetailLine("◷", address.hours) }
        if (address.description.isNotBlank()) { Spacer(Modifier.height(10.dp)); Text(address.description, style = MaterialTheme.typography.bodySmall, color = Ink2) }
        if (address.phone.isNotBlank() || (address.lat != null && address.lon != null)) {
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (address.phone.isNotBlank()) AssistChip(
                    onClick = { runCatching { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${address.phone}"))) } },
                    label = { Text(address.phone, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    leadingIcon = { Icon(Icons.Filled.Call, null, modifier = Modifier.size(16.dp)) },
                    colors = AssistChipDefaults.assistChipColors(containerColor = BrandLight, labelColor = BrandDark, leadingIconContentColor = BrandDark)
                )
                if (address.lat != null && address.lon != null) AssistChip(
                    onClick = {
                        val uri = Uri.parse("geo:${address.lat},${address.lon}?q=${address.lat},${address.lon}")
                        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
                    },
                    label = { Text(loc["essential_map"]) },
                    leadingIcon = { Icon(Icons.Filled.LocationOn, null, modifier = Modifier.size(16.dp)) },
                    colors = AssistChipDefaults.assistChipColors(containerColor = CardBg, labelColor = Ink2, leadingIconContentColor = Brand)
                )
            }
        }
    }
}

@Composable
private fun DetailLine(icon: String, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(icon, fontSize = 14.sp); Spacer(Modifier.size(7.dp)); Text(text, style = MaterialTheme.typography.bodySmall, color = Ink2)
    }
}

private fun categoryEmoji(category: String): String = when (category) {
    EssentialAddressCategory.IMMIGRATION -> "🛂"
    EssentialAddressCategory.HEALTH -> "🏥"
    EssentialAddressCategory.LEGAL -> "⚖️"
    EssentialAddressCategory.TRANSPORT -> "🚌"
    else -> "📍"
}

private fun cityLabel(cityId: String, loc: Loc): String = when (cityId) {
    "all" -> loc["essential_all_cities"]
    "nationwide" -> loc["essential_nationwide"]
    else -> cityId.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}
