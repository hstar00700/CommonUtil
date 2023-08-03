package kr.hstar.commonutil

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * n초 후 해당 작업을 수행 하는 함수
 *
 * @param r 수행 함수
 * @param delayTime 지연 시간
 */
public fun delayRun(r: Runnable, delayTime: Long = 2000L) {
    Handler(Looper.getMainLooper()).postDelayed(r, delayTime)
}

/**
 * 구분자에 의한 값 리스트 획득
 *
 * @param splitKey 구분자
 */
public fun tokenizeValidStr(splitKey: String?): List<String?> {
    return tokenizeValidStr(splitKey, "|")
}

private fun tokenizeValidStr(
    string: String?,
    delimiter: String?
): List<String?> {
    if (string.isNullOrEmpty()) {
        return ArrayList()
    }
    val st = StringTokenizer(string, delimiter)
    val list = ArrayList<String?>()
    while (st.hasMoreElements()) {
        val s = st.nextElement() as String
        if (string.isNotEmpty()) {
            list.add(s)
        }
    }
    return list
}

// 7일 이후 경과된 파일들을 정리
public suspend fun deleteOldLogsV2(path: String, intervalDay: Int = 7) {
    val dir = File(path)
    if (dir.exists()) {
        CoroutineScope(Dispatchers.IO).launch {
            dir.listFiles()?.map { fileItem ->
                async(Dispatchers.IO) {
                    //val createdDay = fileItem.name.substring(8).replace("\\D".toRegex(), "")
                    val createdDay = fileItem.name.split("_")[0]
                    if(checkTimeDiff(createdDay) >= intervalDay) {
                        fileItem.delete()
                    }
                }
            }?.forEach {
                it.await()
            }
        }
    }
}

@SuppressLint("SimpleDateFormat")
private fun checkTimeDiff(oldDay: String): Int {
    return runCatching {
        val today: Date = SimpleDateFormat("yyyyMMdd").parse(getToday()) as Date
        val targetDay: Date = SimpleDateFormat("yyyyMMdd").parse(oldDay) as Date
        val diffSec = (today.time - targetDay.time) / 1000
        val diffDays = diffSec / (24 * 60 * 60)
        diffDays
    }.getOrDefault(0).toInt()
}