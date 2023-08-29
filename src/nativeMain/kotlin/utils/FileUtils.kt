package utils

import okio.FileSystem
import okio.Path
import okio.buffer
import okio.use

fun readFile(path: Path): String {
    FileSystem.SYSTEM.source(path).use { fileSource ->
        var result = ""
        fileSource.buffer().use { bufferedFileSource ->
            while (true) {
                val line = bufferedFileSource.readUtf8Line() ?: break
                result += line
            }
        }
        return result
    }
}

