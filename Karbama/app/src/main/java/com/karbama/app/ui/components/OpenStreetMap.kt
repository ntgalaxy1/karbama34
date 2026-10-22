package com.karbama.app.ui.components

import android.view.ViewGroup
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import com.karbama.app.ui.i18n.Loc
import com.karbama.app.ui.theme.Brand
import com.karbama.app.ui.theme.BrandDark
import com.karbama.app.ui.theme.CardBg
import com.karbama.app.ui.theme.Ink2
import com.karbama.app.ui.theme.Ink3
import com.karbama.app.ui.theme.Line
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

/** A coordinate chosen or displayed on the embedded OpenStreetMap map. */
data class OpenStreetMapPoint(
    val id: String,
    val title: String,
    val subtitle: String = "",
    val lat: Double,
    val lon: Double
)

private fun validMapCoordinate(lat: Double, lon: Double): Boolean =
    lat in -90.0..90.0 && lon in -180.0..180.0

private fun MapView.prepareOpenStreetMap(context: android.content.Context, center: GeoPoint, zoom: Double) {
    // Identifying the application is required by the standard OpenStreetMap tile policy.
    // Keep the optional tile cache in the app cache; this avoids storage permissions.
    val appContext = context.applicationContext
    val config = Configuration.getInstance()
    val cacheBase = java.io.File(appContext.cacheDir, "osmdroid")
    val tileCache = java.io.File(cacheBase, "tiles")
    cacheBase.mkdirs()
    tileCache.mkdirs()
    config.osmdroidBasePath = cacheBase
    config.osmdroidTileCache = tileCache
    config.userAgentValue = appContext.packageName
    setTileSource(TileSourceFactory.MAPNIK)
    setMultiTouchControls(true)
    zoomController.setVisibility(CustomZoomButtonsController.Visibility.ALWAYS)
    controller.setZoom(zoom)
    controller.setCenter(center)
}

private fun MapView.replaceMarkers(points: List<OpenStreetMapPoint>) {
    overlays.removeAll { it is Marker }
    points.filter { validMapCoordinate(it.lat, it.lon) }.forEach { point ->
        overlays.add(Marker(this).apply {
            position = GeoPoint(point.lat, point.lon)
            title = point.title
            snippet = point.subtitle
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        })
    }
    invalidate()
}

/**
 * Full-screen-enough map for an administrator to select a hotel location in the app.
 * Map tiles are requested from OpenStreetMap while the device is online; no Google Maps
 * key or billing account is used.
 */
@Composable
fun OpenStreetMapPickerDialog(
    loc: Loc,
    initialLatitude: Double,
    initialLongitude: Double,
    onDismiss: () -> Unit,
    onConfirm: (latitude: Double, longitude: Double) -> Unit
) {
    val fallback = GeoPoint(39.0, 35.0) // Türkiye, used only before a city is selected.
    var selected by remember(initialLatitude, initialLongitude) {
        mutableStateOf(
            if (validMapCoordinate(initialLatitude, initialLongitude)) GeoPoint(initialLatitude, initialLongitude) else fallback
        )
    }
    var mapView by remember { mutableStateOf<MapView?>(null) }

    DisposableEffect(mapView) {
        mapView?.onResume()
        onDispose { mapView?.onPause() }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.96f).fillMaxHeight(0.86f),
            shape = RoundedCornerShape(24.dp),
            color = CardBg,
            border = BorderStroke(1.dp, Line)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(loc["hotel_pick_map"], style = MaterialTheme.typography.titleLarge, color = BrandDark)
                Spacer(Modifier.height(4.dp))
                Text(loc["hotel_pick_map_hint"], style = MaterialTheme.typography.bodySmall, color = Ink2)
                Spacer(Modifier.height(12.dp))
                Box(Modifier.fillMaxWidth().weight(1f)) {
                    AndroidView(
                        modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                        factory = { context ->
                            MapView(context).also { map ->
                                map.layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                map.prepareOpenStreetMap(context, selected, 14.5)
                                map.overlays.add(
                                    MapEventsOverlay(object : MapEventsReceiver {
                                        override fun singleTapConfirmedHelper(point: GeoPoint): Boolean {
                                            selected = point
                                            return true
                                        }

                                        override fun longPressHelper(point: GeoPoint): Boolean = false
                                    })
                                )
                                mapView = map
                            }
                        },
                        update = { map ->
                            map.replaceMarkers(
                                listOf(
                                    OpenStreetMapPoint(
                                        id = "selected",
                                        title = loc["hotel_selected_location"],
                                        lat = selected.latitude,
                                        lon = selected.longitude
                                    )
                                )
                            )
                        }
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    "%.6f, %.6f".format(java.util.Locale.US, selected.latitude, selected.longitude),
                    style = MaterialTheme.typography.labelLarge,
                    color = Ink2
                )
                Text(loc["hotel_map_attribution"], style = MaterialTheme.typography.labelSmall, color = Ink3)
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(onClick = onDismiss) { Text(loc["del_no"], color = Ink2) }
                    Spacer(Modifier.width(8.dp))
                    ElevatedButton(
                        onClick = { onConfirm(selected.latitude, selected.longitude) },
                        colors = ButtonDefaults.elevatedButtonColors(containerColor = Brand, contentColor = androidx.compose.ui.graphics.Color.White)
                    ) {
                        Icon(Icons.Filled.LocationOn, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text(loc["hotel_use_location"])
                    }
                }
            }
        }
    }
}

/** Displays saved hotel markers together without leaving the application. */
@Composable
fun OpenStreetMapHotelsDialog(
    loc: Loc,
    title: String,
    hotels: List<OpenStreetMapPoint>,
    fallbackLatitude: Double,
    fallbackLongitude: Double,
    onDismiss: () -> Unit
) {
    val usableHotels = remember(hotels) { hotels.filter { validMapCoordinate(it.lat, it.lon) } }
    val center = remember(usableHotels, fallbackLatitude, fallbackLongitude) {
        if (usableHotels.isEmpty()) {
            GeoPoint(fallbackLatitude, fallbackLongitude)
        } else {
            GeoPoint(
                usableHotels.map { it.lat }.average(),
                usableHotels.map { it.lon }.average()
            )
        }
    }
    var mapView by remember { mutableStateOf<MapView?>(null) }

    DisposableEffect(mapView) {
        mapView?.onResume()
        onDispose { mapView?.onPause() }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.96f).fillMaxHeight(0.86f),
            shape = RoundedCornerShape(24.dp),
            color = CardBg,
            border = BorderStroke(1.dp, Line)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(title, style = MaterialTheme.typography.titleLarge, color = BrandDark)
                Spacer(Modifier.height(4.dp))
                Text(loc["hotel_show_map_hint"], style = MaterialTheme.typography.bodySmall, color = Ink2)
                Spacer(Modifier.height(12.dp))
                AndroidView(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    factory = { context ->
                        MapView(context).also { map ->
                            map.layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            map.prepareOpenStreetMap(context, center, if (usableHotels.size > 1) 12.0 else 15.0)
                            mapView = map
                        }
                    },
                    update = { map -> map.replaceMarkers(usableHotels) }
                )
                Spacer(Modifier.height(10.dp))
                Text(loc["hotel_map_attribution"], style = MaterialTheme.typography.labelSmall, color = Ink3)
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss) { Text(loc["close"], color = Ink2) }
                }
            }
        }
    }
}
