package com.karbama.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karbama.app.data.model.Applicant
import com.karbama.app.data.model.Hotel
import com.karbama.app.data.routes.Destinations
import com.karbama.app.data.routes.RouteCity
import com.karbama.app.data.routes.RouteLine
import com.karbama.app.data.routes.RoutePlace
import com.karbama.app.data.routes.RoutesRepo
import com.karbama.app.data.routes.TurkeyHospitals
import com.karbama.app.ui.components.BrandLogo
import com.karbama.app.ui.components.EmptyState
import com.karbama.app.ui.components.KbField
import com.karbama.app.ui.components.OpenStreetMapHotelsDialog
import com.karbama.app.ui.components.OpenStreetMapPoint
import com.karbama.app.ui.components.PickerField
import com.karbama.app.ui.components.SectionCard
import com.karbama.app.ui.components.SectionTitle
import com.karbama.app.ui.components.SystemBars
import com.karbama.app.ui.i18n.Loc
import com.karbama.app.ui.theme.*

/**
 * v52.0.2 — تب پایین «مسیریابی» برای کارجوی ثبت‌نام‌شده.
 * همه‌ی محتوا درون APK است: اطلاعات کشور مقصد حتی بدون اینترنت قابل خواندن است.
 * تنها دکمهٔ «نقشه» برنامهٔ نقشه/مرورگر گوشی را با مختصات همان مکان باز می‌کند.
 */
@Composable
fun RouteContent(loc: Loc, applicant: Applicant?, hotels: List<Hotel> = emptyList()) {
    SystemBars(darkIcons = false)
    val context = LocalContext.current
    val countryCode = Destinations.normalize(applicant?.destinationCountry)
    val country = remember(countryCode) { RoutesRepo.load(context, countryCode) }
    val dest = Destinations.of(countryCode)

    if (applicant == null) {
        Column(Modifier.fillMaxSize().background(Bg)) {
            RouteHeader(loc, dest.flag + " " + loc["route_title"], loc["route_subtitle"])
            EmptyState("🧭", loc["route_need_register"], loc["route_need_register_hint"])
        }
        return
    }
    if (dest.asset == null) {
        Column(Modifier.fillMaxSize().background(Bg)) {
            RouteHeader(loc, dest.flag + " " + loc["route_title"], loc.f("route_for_country", loc[dest.nameKey]))
            EmptyState("🧭", loc["route_country_pending"], loc.f("route_country_pending_hint", loc[dest.nameKey]))
        }
        return
    }
    if (country == null) {
        Column(Modifier.fillMaxSize().background(Bg)) {
            RouteHeader(loc, dest.flag + " " + loc["route_title"], loc["route_subtitle"])
            EmptyState("⚠️", loc["route_data_error"], loc["route_data_error_hint"])
        }
        return
    }

    var query by remember(countryCode) { mutableStateOf("") }
    val initialCity = remember(country) {
        country.cities.firstOrNull { it.id == "istanbul" }?.id ?: country.cities.firstOrNull()?.id.orEmpty()
    }
    var cityId by remember(countryCode) { mutableStateOf(initialCity) }
    val city = country.cities.firstOrNull { it.id == cityId } ?: country.cities.first()
    val cityOptions = remember(country, loc.code) {
        country.cities.map { it.id to it.name(loc.code) }
    }

    Column(Modifier.fillMaxSize().background(Bg)) {
        RouteHeader(
            loc = loc,
            title = dest.flag + " " + loc["route_title"],
            subtitle = loc.f("route_for_country", countryName(countryCode, loc))
        )
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Surface(
                color = BrandLight,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, Brand.copy(alpha = .22f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("🗺️", fontSize = 26.sp)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(loc["route_offline_title"], style = MaterialTheme.typography.labelLarge, color = BrandDark, fontWeight = FontWeight.ExtraBold)
                        Text(loc.f("route_offline_hint", loc.n(country.cities.size), loc.n(country.cities.sumOf { it.airports.size }), loc.n(country.cities.sumOf { it.lines.size })), style = MaterialTheme.typography.bodySmall, color = Ink2, lineHeight = 18.sp)
                    }
                }
            }
            Spacer(Modifier.height(14.dp))

            PickerField(
                label = loc["route_city"],
                value = city.name(loc.code),
                placeholder = loc["route_choose_city"],
                options = cityOptions,
                onSelect = { cityId = it; query = "" },
                searchable = true,
                searchPlaceholder = loc["route_city_search"],
                matcher = { id, q ->
                    country.cities.firstOrNull { it.id == id }?.let { c ->
                        c.names.values.any { it.contains(q, ignoreCase = true) }
                    } == true
                }
            )
            Spacer(Modifier.height(12.dp))
            KbField(
                value = query,
                onValueChange = { query = it },
                label = loc["route_search"],
                placeholder = "🔎 " + loc["route_search_hint"]
            )
            Spacer(Modifier.height(14.dp))

            RouteCityContent(
                loc = loc,
                city = city,
                hotels = hotels,
                query = query,
                onOpenPlace = { openMap(context, it) }
            )

            Spacer(Modifier.height(14.dp))
            Text(
                loc.f("route_source", country.source, country.updated),
                style = MaterialTheme.typography.bodySmall,
                color = Ink3,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun RouteHeader(loc: Loc, title: String, subtitle: String) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(BrandBrush)
            .statusBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BrandLogo(size = 40.dp, onDark = true, modifier = Modifier.size(40.dp))
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = Color.White)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = .91f))
            }
            Text("🧭", fontSize = 25.sp)
        }
    }
}

@Composable
private fun RouteCityContent(
    loc: Loc,
    city: RouteCity,
    hotels: List<Hotel>,
    query: String,
    onOpenPlace: (RoutePlace) -> Unit
) {
    val q = query.trim()
    val airports = city.airports.filter { q.isBlank() || it.name.contains(q, true) || it.code.contains(q, true) }
    val lines = city.lines.filter { line ->
        q.isBlank() || line.ref.contains(q, true) || line.title.contains(q, true) || line.stations.any { it.name.contains(q, true) }
    }
    // فهرست بیمارستان‌ها با دادهٔ OSM داخل APK است؛ هتل‌ها را مدیر در Firestore مدیریت می‌کند.
    val hospitals = TurkeyHospitals.forCity(city.id).filter { q.isBlank() || it.name.contains(q, true) }
    val cityHotels = hotels.asSequence()
        .filter { it.active && it.countryCode == "TR" && it.cityId.equals(city.id, ignoreCase = true) }
        .filter { q.isBlank() || it.name.contains(q, true) || it.address.contains(q, true) }
        .map { RoutePlace(it.name, it.lat, it.lon, it.address) }
        .toList()
    var showHotelMap by remember(city.id) { mutableStateOf(false) }

    SectionCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                SectionTitle("📍 " + city.name(loc.code))
                Spacer(Modifier.height(8.dp))
                Text(
                    loc.f("route_city_summary", loc.n(city.airports.size), loc.n(city.lines.size)),
                    style = MaterialTheme.typography.bodySmall,
                    color = Ink3
                )
            }
            MiniMapButton(loc, RoutePlace(city.name(loc.code), city.lat, city.lon), onOpenPlace)
        }

        if (airports.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            RouteSectionLabel("✈️", loc["route_airports"])
            Spacer(Modifier.height(8.dp))
            airports.forEachIndexed { index, p ->
                PlaceRow(p, code = p.code, loc = loc, onOpenPlace = onOpenPlace)
                if (index != airports.lastIndex) HorizontalDivider(color = Line)
            }
        }

        if (lines.isNotEmpty()) {
            Spacer(Modifier.height(18.dp))
            RouteSectionLabel("🚇", loc["route_network"])
            Spacer(Modifier.height(8.dp))
            lines.forEach { line ->
                RouteLineCard(line = line, loc = loc, query = q, onOpenPlace = onOpenPlace)
                Spacer(Modifier.height(8.dp))
            }
        }

        if (hospitals.isNotEmpty()) {
            Spacer(Modifier.height(18.dp))
            RouteSectionLabel("🏥", loc["route_hospitals"])
            Spacer(Modifier.height(8.dp))
            hospitals.forEachIndexed { index, place ->
                PlaceRow(place, code = "", loc = loc, icon = "⚕", onOpenPlace = onOpenPlace)
                if (index != hospitals.lastIndex) HorizontalDivider(color = Line)
            }
        }

        Spacer(Modifier.height(18.dp))
        RouteSectionLabel("🏨", loc["route_hotels"])
        Spacer(Modifier.height(8.dp))
        if (cityHotels.isEmpty()) {
            Text(loc["route_hotels_empty"], style = MaterialTheme.typography.bodySmall, color = Ink3)
        } else {
            TextButton(onClick = { showHotelMap = true }, contentPadding = PaddingValues(vertical = 2.dp)) {
                Icon(Icons.Filled.LocationOn, contentDescription = null, tint = Brand, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text(loc["hotel_show_map"], color = BrandDark, fontWeight = FontWeight.Bold)
            }
            cityHotels.forEachIndexed { index, place ->
                PlaceRow(place, code = place.code, loc = loc, icon = "🏨", onOpenPlace = onOpenPlace)
                if (index != cityHotels.lastIndex) HorizontalDivider(color = Line)
            }
        }

        if (airports.isEmpty() && lines.isEmpty() && hospitals.isEmpty() && cityHotels.isEmpty()) {
            Spacer(Modifier.height(12.dp))
            Text(loc["route_no_result"], style = MaterialTheme.typography.bodyMedium, color = Ink3)
        }
    }

    if (showHotelMap) {
        OpenStreetMapHotelsDialog(
            loc = loc,
            title = loc["hotel_map_title"],
            hotels = cityHotels.mapIndexed { index, place ->
                OpenStreetMapPoint(
                    id = "${city.id}-$index-${place.name}",
                    title = place.name,
                    subtitle = place.code,
                    lat = place.lat,
                    lon = place.lon
                )
            },
            fallbackLatitude = city.lat,
            fallbackLongitude = city.lon,
            onDismiss = { showHotelMap = false }
        )
    }
}

@Composable
private fun RouteSectionLabel(icon: String, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(icon, fontSize = 18.sp)
        Spacer(Modifier.width(7.dp))
        Text(text, style = MaterialTheme.typography.labelLarge, color = Ink2, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun PlaceRow(place: RoutePlace, code: String, loc: Loc, icon: String = "✈", onOpenPlace: (RoutePlace) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(30.dp).clip(CircleShape).background(AccentLight), contentAlignment = Alignment.Center) {
            Text(icon, fontSize = 15.sp)
        }
        Spacer(Modifier.width(9.dp))
        Column(Modifier.weight(1f)) {
            Text(place.name, style = MaterialTheme.typography.bodyMedium, color = Ink, fontWeight = FontWeight.Bold)
            Text(
                (if (code.isNotBlank()) "$code · " else "") + place.lat.toGeoText() + ", " + place.lon.toGeoText(),
                style = MaterialTheme.typography.bodySmall.copy(textDirection = TextDirection.Ltr), color = Ink3
            )
        }
        MiniMapButton(loc, place, onOpenPlace)
    }
}

@Composable
private fun RouteLineCard(line: RouteLine, loc: Loc, query: String, onOpenPlace: (RoutePlace) -> Unit) {
    var expanded by remember(line.kind, line.ref, line.title) { mutableStateOf(query.isNotBlank() && line.stations.any { it.name.contains(query, true) }) }
    val color = Color(line.color)
    Surface(
        shape = RoundedCornerShape(15.dp),
        color = FieldBg,
        border = BorderStroke(1.dp, Line),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 12.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(color),
                    contentAlignment = Alignment.Center
                ) {
                    Text(line.ref.ifBlank { routeKindShort(line.kind) }, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
                }
                Spacer(Modifier.width(9.dp))
                Column(Modifier.weight(1f)) {
                    Text(routeKind(loc, line.kind), style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.ExtraBold)
                    Text(line.title, style = MaterialTheme.typography.bodyMedium, color = Ink, fontWeight = FontWeight.Bold, maxLines = if (expanded) 3 else 1, overflow = TextOverflow.Ellipsis)
                }
                Text(if (expanded) "⌃" else "⌄", color = Ink3, fontSize = 20.sp)
            }
            if (expanded) {
                HorizontalDivider(color = Line)
                Row(Modifier.padding(start = 12.dp, end = 8.dp, top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(loc.f("route_station_count", loc.n(line.stations.size)), style = MaterialTheme.typography.bodySmall, color = Ink3, modifier = Modifier.weight(1f))
                    MiniMapButton(loc, line.stations.first(), onOpenPlace)
                }
                line.stations.forEachIndexed { index, p ->
                    val mark = when (index) {
                        0 -> "●"
                        line.stations.lastIndex -> "■"
                        else -> "•"
                    }
                    Row(
                        Modifier.fillMaxWidth().padding(start = 13.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(16.dp)) {
                            Text(mark, color = color, fontSize = if (index == 0 || index == line.stations.lastIndex) 13.sp else 16.sp, lineHeight = 16.sp)
                            if (index != line.stations.lastIndex) Box(Modifier.width(2.dp).height(15.dp).background(color.copy(alpha = .35f)))
                        }
                        Spacer(Modifier.width(7.dp))
                        Column(Modifier.weight(1f)) {
                            Text(p.name, style = MaterialTheme.typography.bodySmall, color = Ink, fontWeight = if (index == 0 || index == line.stations.lastIndex) FontWeight.Bold else FontWeight.Normal)
                            Text(p.lat.toGeoText() + ", " + p.lon.toGeoText(), style = MaterialTheme.typography.labelSmall.copy(textDirection = TextDirection.Ltr), color = Ink3)
                        }
                        IconButton(onClick = { onOpenPlace(p) }, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Filled.LocationOn, contentDescription = loc["route_open_map"], tint = Brand, modifier = Modifier.size(19.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniMapButton(loc: Loc, place: RoutePlace, onOpenPlace: (RoutePlace) -> Unit) {
    TextButton(onClick = { onOpenPlace(place) }, contentPadding = PaddingValues(horizontal = 7.dp, vertical = 3.dp)) {
        Icon(Icons.Filled.LocationOn, contentDescription = null, tint = Brand, modifier = Modifier.size(17.dp))
        Spacer(Modifier.width(2.dp))
        Text(loc["route_map"], style = MaterialTheme.typography.labelSmall, color = BrandDark, fontWeight = FontWeight.Bold)
    }
}

private fun routeKind(loc: Loc, k: String): String = loc["route_kind_$k"]
private fun routeKindShort(k: String): String = when (k) { "metrobus" -> "MB"; "funicular" -> "F"; else -> k.take(1).uppercase() }
private fun Double.toGeoText(): String = "%.5f".format(java.util.Locale.US, this)

private fun countryName(code: String, loc: Loc): String = loc[Destinations.of(code).nameKey]

private fun openMap(context: android.content.Context, p: RoutePlace) {
    val label = Uri.encode(p.name)
    val uri = Uri.parse("geo:${p.lat},${p.lon}?q=${p.lat},${p.lon}($label)")
    val intent = Intent(Intent.ACTION_VIEW, uri)
    runCatching { context.startActivity(intent) }.getOrElse {
        val web = Uri.parse("https://www.openstreetmap.org/?mlat=${p.lat}&mlon=${p.lon}#map=16/${p.lat}/${p.lon}")
        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, web)) }
    }
}
