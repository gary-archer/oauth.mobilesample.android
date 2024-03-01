/*
 * The root gradle file brings in plugins which are applied in child scripts
 */
plugins {
    id("com.android.application") version "8.2.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    id("io.gitlab.arturbosch.detekt") version "1.23.5" apply false
}
