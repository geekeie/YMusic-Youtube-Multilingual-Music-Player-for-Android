package com.peecock.ymusic.utils

import android.os.Build
import androidx.annotation.RequiresApi
import timber.log.Timber
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

@RequiresApi(Build.VERSION_CODES.O)
fun moveDir(src: Path, dest: Path): Boolean {
    if (src.toFile().isDirectory) {
        for (file in src.toFile().listFiles()!!) {
            moveDir(file.toPath(), dest.resolve(src.relativize(file.toPath())))
        }
    }
    return try {
        Files.move(src, dest, StandardCopyOption.REPLACE_EXISTING)
        true
    } catch (e: IOException) {
        Timber.e(e)
        false
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun copyDir(src: Path, dest: Path) {
    val sources = Files.walk(src).toList()
    for (source in sources) {
        Files.copy(source, dest.resolve(src.relativize(source)),
            StandardCopyOption.REPLACE_EXISTING
        )
    }
}

/*
fun tryMoveDir() {
    val from = File("/path/to/src")
    val to = File("/path/to/dest")
    val success = moveDir(from.toPath(), to.toPath())
    if (success) {
        println("File Moved Successfully")
    } else {
        println("File Moved Failed")
    }
}
 */

/*
fun tryCopyDir() {
    val from = File("/var/kotlin/")
    val to = File("/var/bak/kotlin/")
    try {
        copyDir(from.toPath(), to.toPath())
        println("Copying succeeded.")
    } catch (ex: IOException) {
        ex.printStackTrace()
    }
}
 */