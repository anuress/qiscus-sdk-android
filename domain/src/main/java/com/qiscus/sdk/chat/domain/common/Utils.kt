package com.qiscus.sdk.chat.domain.common

import java.util.*
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * Created on : August 18, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
private val random = Random()
private val symbols = '0'.rangeTo('9').toMutableList().apply {
    addAll('a'.rangeTo('z'))
    addAll('A'.rangeTo('Z'))
    toList()
}

private val taskExecutor = ScheduledThreadPoolExecutor(5)

fun randomString(length: Int = 64): String {
    val buf = CharArray(length)
    for (i in buf.indices) {
        buf[i] = symbols[random.nextInt(symbols.size)]
    }
    return String(buf)
}

fun generateUniqueId(): String {
    return "${System.currentTimeMillis()}_${randomString()}"
}

fun runOnBackgroundThread(runnable: Runnable, delay: Long = 0): ScheduledFuture<*> {
    return taskExecutor.schedule(runnable, delay, TimeUnit.MILLISECONDS)
}

fun scheduleOnBackgroundThread(runnable: Runnable, delay: Long): ScheduledFuture<*> {
     return taskExecutor.scheduleWithFixedDelay(runnable, 0, delay, TimeUnit.MILLISECONDS)
}