/*
 * The root gradle file brings in plugins which are applied in child scripts
 */
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.21" apply false
    id("io.gitlab.arturbosch.detekt") version "1.23.4" apply false
}
