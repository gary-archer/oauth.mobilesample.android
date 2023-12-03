/*
 * Apply these plugins to build our Kotlin app
 */
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
    id("io.gitlab.arturbosch.detekt")
}

android {
    // Build with the latest non beta SDK
    compileSdk = 34
    namespace = "com.authsamples.basicmobileapp"

    // Support devices from Android 8.0+
    defaultConfig {
        applicationId = "com.authsamples.basicmobileapp"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        // The scheme value used by AppAuth's RedirectUriReceiverActivity class to receive login responses
        manifestPlaceholders["appAuthRedirectScheme"] = "https"
    }

    // Be explicit about the JVM version to avoid certain types of warning or error
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }

    // All builds of the app are signed with a self signed key to identify the app for app links
    signingConfigs {
        create("release") {
            storeFile = file("${rootDir}/security/app-keystore.jks")
            storePassword = "android"
            keyAlias = "com.authsamples.basicmobileapp"
            keyPassword = "android"
        }
    }

    // Sign both debug and release builds
    buildTypes {
        getByName("debug") {
            signingConfig = signingConfigs.getByName("release")
            isDebuggable = true
        }
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // Enable features needed to build Jetpack compose
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }
}

// Dependency versions
object VERSION {
    const val kotlin_extensions = "1.12.0"
    const val compose = "1.8.0"
    const val compose_bom = "2023.09.02"
    const val compose_ui = "1.5.3"
    const val material3 = "1.1.2"
    const val navigation = "2.7.4"
    const val appauth = "0.11.1"
    const val browser = "1.6.0"
    const val okhttp = "4.11.0"
    const val gson = "2.10.1"
    const val okio = "3.4.0"
    const val eventbus = "3.3.1"
    const val detekt = "1.23.1"
}

dependencies {

    // Kotlin extensions
    implementation("androidx.core:core-ktx:${VERSION.kotlin_extensions}")

    // Jetpack compose
    implementation("androidx.activity:activity-compose:${VERSION.compose}")
    implementation(platform("androidx.compose:compose-bom:${VERSION.compose_bom}"))

    // Lifecycle and navigation
    implementation("androidx.navigation:navigation-ui-ktx:${VERSION.navigation}")
    implementation("androidx.navigation:navigation-compose:${VERSION.navigation}")

    // UI elements
    implementation("androidx.compose.ui:ui:${VERSION.compose_ui}")
    implementation("androidx.compose.ui:ui-graphics:${VERSION.compose_ui}")
    implementation("androidx.compose.material3:material3:${VERSION.material3}")

    // The AppAuth library manages OAuth security
    implementation ("net.openid:appauth:${VERSION.appauth}")

    // Chrome Custom tabs for the login window
    implementation ("androidx.browser:browser:${VERSION.browser}")

    // API requests and JSON
    implementation ("com.squareup.okhttp3:okhttp:${VERSION.okhttp}")
    implementation ("com.google.code.gson:gson:${VERSION.gson}")

    // Resource file reading
    implementation ("com.squareup.okio:okio:${VERSION.okio}")

    // Event messages
    implementation ("org.greenrobot:eventbus:${VERSION.eventbus}")

    // The plugin to enable code quality checks
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:${VERSION.detekt}")
}

// Code quality configuration
detekt {
    config.setFrom(files("$rootDir/detekt.yml"))
}

