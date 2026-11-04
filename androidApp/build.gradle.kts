plugins {
    id("echochat.android.application")
}

android {
    namespace = "com.echochat"
    defaultConfig {
        applicationId = "com.echochat.kmp"
        versionCode = 1
        versionName = "1.0"
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(project(":core:notifications"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.koin.android)

    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
