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
    // Build with latest non beta SDK
    compileSdk = 34
    namespace = "com.authsamples.basicmobileapp"

    // Support devices from Android 6.0+
    defaultConfig {
        applicationId = "com.authsamples.basicmobileapp"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        // The scheme value used by AppAuth's RedirectUriReceiverActivity class to receive login responses
        manifestPlaceholders["appAuthRedirectScheme"] = "https"
    }

    // Be explicit about the JVM version to avoid certain types of warning or error
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
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

    // Enable newer Jetpack binding features
    buildFeatures {
        dataBinding = true
        viewBinding = true
    }
}

// Dependency versions
object VERSION {
    const val coroutines = "1.7.3"
    const val lifecycle_extensions = "2.2.0"
    const val navigation = "2.6.0"
    const val appauth = "0.11.1"
    const val browser = "1.5.0"
    const val okhttp = "4.11.0"
    const val gson = "2.10.1"
    const val okio = "3.4.0"
    const val eventbus = "3.3.1"
}

dependencies {

    // Kotlin async support
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${VERSION.coroutines}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${VERSION.coroutines}")

    // View model support
    implementation("androidx.lifecycle:lifecycle-extensions:${VERSION.lifecycle_extensions}")

    // Navigation for Single Activity Apps
    implementation("androidx.navigation:navigation-fragment-ktx:${VERSION.navigation}")
    implementation("androidx.navigation:navigation-ui-ktx:${VERSION.navigation}")

    // The AppAuth library manages OAuth security
    implementation ("net.openid:appauth:${VERSION.appauth}")

    // Include Chrome Custom tabs for the login window
    implementation ("androidx.browser:browser:${VERSION.browser}")

    // API requests
    implementation ("com.squareup.okhttp3:okhttp:${VERSION.okhttp}")

    // JSON serialization
    implementation ("com.google.code.gson:gson:${VERSION.gson}")

    // Resource file reading
    implementation ("com.squareup.okio:okio:${VERSION.okio}")

    // Event messages
    implementation ("org.greenrobot:eventbus:${VERSION.eventbus}")

    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.1")
}

detekt {
    config.setFrom(files("$rootDir/detekt.yml"))
}

