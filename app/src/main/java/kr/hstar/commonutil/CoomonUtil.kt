package kr.hstar.commonutil

import android.os.Handler
import android.os.Looper

/**
 * n초 후 해당 작업을 수행 하는 함수
 *
 * @param r 수행 함수
 * @param delayTime 지연 시간
 */
fun delayRun(r: Runnable, delayTime: Long = 2000L) = Handler(Looper.getMainLooper()).postDelayed(r, delayTime)