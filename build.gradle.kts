/*
 * The root gradle file brings in plugins which are applied in child scripts
 */
plugins {
    alias(libs.plugins.application) apply false
    alias(libs.plugins.kotlin) apply false
    alias(libs.plugins.compose) apply false
    alias(libs.plugins.detekt) apply false
}
