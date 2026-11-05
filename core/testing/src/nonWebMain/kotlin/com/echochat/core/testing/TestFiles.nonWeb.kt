package com.echochat.core.testing

import io.github.vinceglb.filekit.PlatformFile

actual fun testPhotoFile(path: String): PlatformFile = PlatformFile(path)
