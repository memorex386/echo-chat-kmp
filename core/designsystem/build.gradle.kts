plugins {
    id("echochat.kmp.compose")
}

kotlin {
    android {
        namespace = "com.echochat.core.designsystem"
        androidResources.enable = true
    }
}

compose.resources {
    // Feature modules draw these icons, so the generated Res class is public.
    publicResClass = true
    packageOfResClass = "com.echochat.core.designsystem.resources"
}
