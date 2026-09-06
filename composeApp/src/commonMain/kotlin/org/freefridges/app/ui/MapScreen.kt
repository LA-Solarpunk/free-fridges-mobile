package org.freefridges.app.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.geojson.Position

/** OpenFreeMap Liberty — full street detail, free, no API key. */
private const val LIBERTY_STYLE_URL = "https://tiles.openfreemap.org/styles/liberty"

private val LOS_ANGELES = Position(longitude = -118.2437, latitude = 34.0522)

@Composable
fun MapScreen(modifier: Modifier = Modifier) {
    val cameraState = rememberCameraState(
        firstPosition = CameraPosition(target = LOS_ANGELES, zoom = 11.0)
    )

    // The default overlay carries the source attribution OpenFreeMap requires; replacing
    // it means rendering attribution ourselves. Fridge pins will go in later as a GeoJSON
    // source + CircleLayer/SymbolLayer passed through MaplibreMap's trailing content lambda.
    MaplibreMap(
        modifier = modifier.fillMaxSize(),
        baseStyle = BaseStyle.Uri(LIBERTY_STYLE_URL),
        cameraState = cameraState
    )
}
