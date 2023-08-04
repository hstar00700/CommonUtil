@file:Suppress("unused")

package kr.hstar.commonutil

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import java.util.*

public fun <T : Any> Single<T>.withThread(): Single<T> = subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
public fun <T : Any> BehaviorSubject<T>.withThread(): io.reactivex.rxjava3.core.Observable<T> = subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
public fun <T : Any> PublishSubject<T>.withThread(): io.reactivex.rxjava3.core.Observable<T> = subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

private fun <T> Flow<T>.throttleFirstV(periodMillis: Long) : Flow<T> {
    require(periodMillis > 0) { "periodMillis should be positive" }
    return flow {
        var lastTime = 0L
        collect { value ->
            val currentTime = System.currentTimeMillis()
            if(currentTime - lastTime >= periodMillis) {
                lastTime = currentTime
                emit(value)
            }
        }
    }
}

@Suppress("UNCHECKED_CAST")
public fun <T> Flow<T>.throttleLast(periodMillis: Long) : Flow<T> {
    return channelFlow {
        var lastValue: T?
        var timer: Timer? = null
        onCompletion { timer?.cancel() }
        collect { data ->
            lastValue = data

            timer?.let {

            } ?: run {
                timer = Timer()
                timer?.scheduleAtFixedRate(object : TimerTask() {
                    override fun run() {
                        val value = lastValue
                        lastValue = null
                        value?.let {
                            launch { send(value as T) }
                        } ?: run { timer?.cancel(); timer = null }
                    }
                }, 0, periodMillis)
            }
        }
    }
}

@ExperimentalCoroutinesApi
private fun View.clickFlow(): Flow<Unit> = callbackFlow {
    setOnClickListener {
        trySend(Unit).isSuccess
    }
    awaitClose { setOnClickListener(null) }
}

@ExperimentalCoroutinesApi
public fun View.throttleClick(
    scope: CoroutineScope,
    duration: Long = 1000L,
    callBack: () -> Unit
) {
    clickFlow()
        .throttleFirstV(duration)
        .onEach { callBack.invoke() }
        .launchIn(scope)
}

public fun EditText.textChangesToFlow(): Flow<CharSequence?> {
    return callbackFlow {
        val listener = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = Unit
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
                trySend(text)
            }
        }
        addTextChangedListener(listener)
        // 콜백이 사라질때 실행, 리스너 제거
        awaitClose { removeTextChangedListener(listener) }
    }.onStart {
        // event 방출
        emit(text)
    }
}


public fun ActivityResultCaller.registerResult(
    resultCode: Int = Activity.RESULT_OK,
    action: (Intent?) -> Unit,
) {
    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == resultCode) action(it.data)
    }
}

public fun BroadcastReceiver.goAsync(block: suspend CoroutineScope.() -> Unit) {
    val pendingResult = goAsync()
    CoroutineScope(SupervisorJob()).launch(Dispatchers.IO) {
        try {
            block()
        } finally {
            pendingResult.finish()
        }
    }
}

public fun LifecycleOwner.repeatOnState(
    state: Lifecycle.State = Lifecycle.State.RESUMED,
    block: suspend CoroutineScope.() -> Unit
) {
    lifecycleScope.launch {
        lifecycle.repeatOnLifecycle(state, block)
    }
}

@Suppress("DEPRECATION")
public fun Activity.getPackageVersionName(): String {
    return runCatching {
        val pi = packageManager.getPackageInfo(packageName, 0)
        pi.versionName
    }.getOrDefault("N/A")
}

/**
 * 애플리케이션 패키지의 버전 코드 획득
 */
@Suppress("DEPRECATION", "unused")
public fun Activity.getPackageVersionCode(): Int {
    return runCatching {
        val pi = this.packageManager.getPackageInfo(this.packageName, 0)
        return pi.versionCode
    }.getOrDefault(0)
}