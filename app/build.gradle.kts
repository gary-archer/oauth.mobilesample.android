// Apply these plugins to build our Kotlin app
plugins {
    alias(libs.plugins.application)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.compose)
    alias(libs.plugins.detekt)
}

android {
    // Build with the latest released Android version
    compileSdk = 36
    namespace = "com.authsamples.finalmobileapp"

    // Support devices from Android 10+
    defaultConfig {
        applicationId = "com.authsamples.finalmobileapp"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        // The scheme value used by AppAuth's RedirectUriReceiverActivity class to receive login responses
        manifestPlaceholders["appAuthRedirectScheme"] = "https"
    }

    // Be explicit about the JVM version
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    // All builds of the app are signed with a self signed key to identify the app for app links
    signingConfigs {
        create("release") {
            storeFile = file("${rootDir}/security/app-keystore.jks")
            storePassword = "android"
            keyAlias = "com.authsamples.finalmobileapp"
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
}

dependencies {

    // Kotlin extensions
    implementation(libs.androidx.core.ktx)

    // Jetpack compose
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))

    // Lifecycle and navigation
    implementation(libs.navigation.ui.ktx)
    implementation(libs.navigation.compose)

    // UI elements
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.material3)

    // The AppAuth library manages OAuth security
    implementation (libs.appauth)

    // Chrome Custom tabs for the login window
    implementation (libs.browser)

    // API requests and JSON
    implementation (libs.okhttp)
    implementation (libs.gson)

    // Resource file reading
    implementation (libs.okio)

    // Event messages
    implementation (libs.eventbus)

    // The plugin to enable code quality checks
    detektPlugins(libs.detekt.rules.ktlint.wrapper)
}

// Code quality configuration
detekt {
    config.setFrom(files("$rootDir/detekt.yml"))
}
