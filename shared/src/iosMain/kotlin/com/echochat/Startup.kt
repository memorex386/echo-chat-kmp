package com.echochat

import com.echochat.core.data.CacheImageStore
import kotlinx.coroutines.runBlocking

/**
 * Clears the photos the last run left behind (PHOTO-08). The Swift app calls this once as it
 * starts, as `StartupKt.clearOldPhotos()`: not from MainViewController(), which runs for each
 * window an iPad opens.
 */
fun clearOldPhotos() = runBlocking { CacheImageStore().clear() }
