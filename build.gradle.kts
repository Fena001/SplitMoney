plugins {
    alias(libs.plugins.android.application) apply false // ✅ This is correct
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.google.gms.google.services) apply false
}