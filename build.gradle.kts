/*
 * The root gradle file brings in plugins which are applied in child scripts
 */
plugins {
    id("com.android.application") version "8.1.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
    id("org.jetbrains.kotlin.kapt") version "1.9.0" apply false
    id("io.gitlab.arturbosch.detekt") version "1.23.1" apply false
}
