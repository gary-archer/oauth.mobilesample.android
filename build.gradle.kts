/*
 * The root gradle file brings in plugins which are applied in child scripts
 */
plugins {
    id("com.android.application") version "8.8.1" apply false
    id("org.jetbrains.kotlin.android") version "2.1.10" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.10" apply false
    id("io.gitlab.arturbosch.detekt") version "1.23.7" apply false
}
