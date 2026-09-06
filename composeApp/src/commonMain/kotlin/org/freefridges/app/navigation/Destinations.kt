package org.freefridges.app.navigation

import kotlinx.serialization.Serializable

/**
 * Marker for every navigable destination in the app.
 *
 * Navigation's type-safe route API takes `Any` and looks the serializer up at runtime, so
 * a route object that forgot `@Serializable` (or its `composable<T>` registration) would
 * otherwise compile fine and only blow up when the user taps the tab. Requiring this
 * interface keeps [TopLevelDestination] and friends to routes declared here.
 */
sealed interface AppRoute

@Serializable
data object MapRoute : AppRoute

@Serializable
data object FridgesRoute : AppRoute

@Serializable
data object DebugRoute : AppRoute
