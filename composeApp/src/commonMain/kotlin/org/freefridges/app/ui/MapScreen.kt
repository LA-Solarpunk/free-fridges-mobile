package org.freefridges.app.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.freefridges.app.ui.theme.LocalIsDarkTheme
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.geojson.Position

/** OpenFreeMap Liberty — full street detail, free, no API key. */
private const val LIBERTY_STYLE_URL = "https://tiles.openfreemap.org/styles/liberty"

/**
 * OpenFreeMap Dark — the same OpenMapTiles planet source as Liberty, so it carries the
 * identical attribution (which comes from the shared `/planet` TileJSON, not the style).
 */
private const val DARK_STYLE_URL = "https://tiles.openfreemap.org/styles/dark"

private val LOS_ANGELES = Position(longitude = -118.2437, latitude = 34.0522)

/**
 * [contentWindowInsets] positions the map's own overlay controls (attribution, compass,
 * scale bar). MaplibreMap reads these insets directly in its measure policy — it does not
 * observe `Modifier.padding` or `consumeWindowInsets` — so a caller that has already
 * inset the map away from a system bar must drop that side here or it is applied twice.
 */
@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    contentWindowInsets: WindowInsets = WindowInsets.safeDrawing
) {
    val cameraState = rememberCameraState(
        firstPosition = CameraPosition(target = LOS_ANGELES, zoom = 11.0)
    )
    val styleUrl = if (LocalIsDarkTheme.current) DARK_STYLE_URL else LIBERTY_STYLE_URL

    // The default overlay carries the source attribution OpenFreeMap requires; replacing
    // it means rendering attribution ourselves. Fridge pins will go in later as a GeoJSON
    // source + CircleLayer/SymbolLayer passed through MaplibreMap's trailing content lambda.
    MaplibreMap(
        modifier = modifier.fillMaxSize(),
        baseStyle = BaseStyle.Uri(styleUrl),
        cameraState = cameraState,
        contentWindowInsets = contentWindowInsets
    )
}
