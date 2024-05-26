/*
 * Apply these plugins to build our Kotlin app
 */
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
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
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
}

// Dependency versions
object VERSION {
    const val KOTLIN_EXTENSIONS = "1.13.1"
    const val COMPOSE = "1.9.0"
    const val COMPOSE_BOM = "2024.05.00"
    const val COMPOSE_UI = "1.6.7"
    const val MATERIAL3 = "1.2.1"
    const val NAVIGATION = "2.7.7"
    const val APPAUTH = "0.11.1"
    const val BROWSER = "1.8.0"
    const val OKHTTP = "4.12.0"
    const val GSON = "2.10.1"
    const val OKIO = "3.6.0"
    const val EVENTBUS = "3.3.1"
    const val DETEKT = "1.23.1"
}

dependencies {

    // Kotlin extensions
    implementation("androidx.core:core-ktx:${VERSION.KOTLIN_EXTENSIONS}")

    // Jetpack compose
    implementation("androidx.activity:activity-compose:${VERSION.COMPOSE}")
    implementation(platform("androidx.compose:compose-bom:${VERSION.COMPOSE_BOM}"))

    // Lifecycle and navigation
    implementation("androidx.navigation:navigation-ui-ktx:${VERSION.NAVIGATION}")
    implementation("androidx.navigation:navigation-compose:${VERSION.NAVIGATION}")

    // UI elements
    implementation("androidx.compose.ui:ui:${VERSION.COMPOSE_UI}")
    implementation("androidx.compose.ui:ui-graphics:${VERSION.COMPOSE_UI}")
    implementation("androidx.compose.material3:material3:${VERSION.MATERIAL3}")

    // The AppAuth library manages OAuth security
    implementation ("net.openid:appauth:${VERSION.APPAUTH}")

    // Chrome Custom tabs for the login window
    implementation ("androidx.browser:browser:${VERSION.BROWSER}")

    // API requests and JSON
    implementation ("com.squareup.okhttp3:okhttp:${VERSION.OKHTTP}")
    implementation ("com.google.code.gson:gson:${VERSION.GSON}")

    // Resource file reading
    implementation ("com.squareup.okio:okio:${VERSION.OKIO}")

    // Event messages
    implementation ("org.greenrobot:eventbus:${VERSION.EVENTBUS}")

    // The plugin to enable code quality checks
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:${VERSION.DETEKT}")
}

// Code quality configuration
detekt {
    config.setFrom(files("$rootDir/detekt.yml"))
}

