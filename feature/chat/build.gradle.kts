plugins {
    id("echochat.kmp.feature")
}

kotlin {
    android {
        namespace = "com.echochat.feature.chat"
        androidResources.enable = true
    }
}

compose.resources {
    packageOfResClass = "com.echochat.feature.chat.resources"
}
