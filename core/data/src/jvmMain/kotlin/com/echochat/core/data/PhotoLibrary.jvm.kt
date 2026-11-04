package com.echochat.core.data

// Desktop has no photo library. It saves through a Save dialog instead, with saveCopy.
internal actual suspend fun saveToPhotoLibrary(path: String, name: String): Boolean = false
