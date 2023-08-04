package kr.hstar.commonutil

import kotlinx.coroutines.CoroutineExceptionHandler

public fun baseExceptionHandler(callback: ((String) -> Unit)? = null): CoroutineExceptionHandler = CoroutineExceptionHandler { _, e ->
    val error = runCatching { e.message ?: "Uncaught Error" }.getOrDefault("Error")
    callback?.invoke(error)
}