package com.echochat.core.testing

import io.github.vinceglb.filekit.PlatformFile

/** A file named by [path] for a test photo. Nothing is read from it. */
expect fun testPhotoFile(path: String): PlatformFile
