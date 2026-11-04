plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.android.gradlePlugin)
    implementation(libs.compose.compiler.gradlePlugin)
    implementation(libs.compose.multiplatform.gradlePlugin)
    implementation(libs.kotlin.gradlePlugin)
    implementation(libs.serialization.gradlePlugin)
}
