package kr.hstar.commonutil

import kotlinx.coroutines.*
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL

public suspend fun deleteInPathFolder(targetDir: File) {
    CoroutineScope(Dispatchers.IO).launch {
        if (targetDir.exists()) {
            val files = targetDir.listFiles()
            files?.map {
                async {
                    //Logger.w("Delete - File : ${it.name}")
                    it.delete()
                }
            }?.forEach { it.await() }
        }
    }
}

/**
 * URL 경로의 파일을 다운받아 경로 파일로 저장
 */
public suspend fun saveFileFromUrl(url: String?, pathFile: String?) {
    coroutineScope {
        var inputStream: BufferedInputStream? = null
        var fileOutputStream: FileOutputStream? = null
        launch(Dispatchers.IO) {
            runCatching {
                inputStream = BufferedInputStream(URL(url).openStream())
                fileOutputStream = FileOutputStream(pathFile)
                val dataBuffer = ByteArray(1024)
                var bytesRead: Int
                while (inputStream!!.read(dataBuffer, 0, 1024).also { bytesRead = it } != -1) {
                    fileOutputStream!!.write(dataBuffer, 0, bytesRead)
                }
                inputStream!!.close()
                fileOutputStream!!.close()
            }.also {
                try {
                    inputStream?.close()
                    fileOutputStream?.close()
                } catch (_: Exception) {
                }
            }
        }
    }
}

/**
 * 지정된 파일 삭제
 */
public fun deleteFile(pathFile: String): Boolean {
    return runCatching {
        val file = File(pathFile)
        if (file.isFile) file.delete()
        true
    }.getOrDefault(false)
}

