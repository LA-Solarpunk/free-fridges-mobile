import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    android {
        namespace = "org.freefridges.app.shared"
        compileSdk = 37
        minSdk = 26

        // Off by default for com.android.kotlin.multiplatform.library. Without it
        // `variant.sources.assets` is null, so the Compose resources plugin silently
        // never packages composeResources/ into the AAR and the tab icons don't load.
        androidResources {
            enable = true
        }

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.androidx.navigation.compose)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.maplibre.compose)
            implementation(libs.maplibre.compose.material3)
        }

        androidMain.dependencies {
            // Render backend for MapLibre on Android. OpenGL rather than Vulkan: it is the
            // MapLibre demo app's own default and the safer pick at minSdk 26.
            runtimeOnly(libs.maplibre.compose.runtime.opengl.android)
        }
    }
}

compose.resources {
    publicResClass = false
    packageOfResClass = "org.freefridges.app.generated.resources"
}
