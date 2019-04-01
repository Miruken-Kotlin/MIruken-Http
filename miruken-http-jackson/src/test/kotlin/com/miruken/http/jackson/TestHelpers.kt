package com.miruken.http.jackson

import org.junit.rules.TestName
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.reflect.KFunction
import kotlin.reflect.full.declaredFunctions
import kotlin.test.assertTrue

private const val DEFAULT_TIMEOUT = 5000L /* 5 seconds */

fun <T> T.testAsync(
        timeoutMs: Long = DEFAULT_TIMEOUT,
        block: T.(()->Unit) -> Unit
): Boolean {
    val done  = CountDownLatch(1)
    try {
        block { done.countDown() }
    } catch (t: Throwable) {
        done.countDown()
        throw t
    }
    return done.await(timeoutMs, TimeUnit.MILLISECONDS)
}

fun <T> T.assertAsync(
        timeoutMs: Long = DEFAULT_TIMEOUT,
        block: T.(()->Unit) -> Unit
) {
    assertTrue(testAsync(timeoutMs, block),
            "Test timed out after $timeoutMs milliseconds")
}

fun <T> T.assertAsync(
        testName: TestName,
        timeoutMs: Long = DEFAULT_TIMEOUT,
        block: T.(()->Unit) -> Unit
) {
    assertTrue(testAsync(timeoutMs, block),
            "Test '${testName.methodName}' timed out after $timeoutMs milliseconds")
}

inline fun <reified T: Any> getMethod(name:String): KFunction<*>? =
        T::class.declaredFunctions.first { it.name == name}