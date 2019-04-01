package com.miruken.http

import com.miruken.callback.*
import okhttp3.Interceptor
import java.time.Duration

class HttpOptions : Options<HttpOptions>() {
    var timeoutSeconds: Long? = null

    var interceptors: Collection<Interceptor>? = null

    override fun mergeInto(other: HttpOptions) {
        if (timeoutSeconds != null && other.timeoutSeconds == null)
            other.timeoutSeconds = timeoutSeconds

        if (interceptors != null) {
            other.interceptors = other.interceptors?.apply {
                this + interceptors!!
            } ?: interceptors
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other is HttpOptions) {
            return timeoutSeconds == other.timeoutSeconds &&
                    ((interceptors === other.interceptors ||
                      interceptors?.equals(other.interceptors) == true))
        }
        return false
    }

    override fun hashCode(): Int {
        val hash = 31 + (timeoutSeconds?.hashCode() ?: 0)
        return 31 * hash + (interceptors?.hashCode() ?: 0)
    }
}

fun Handling.timeout(timeoutSeconds: Long) =
        withOptions(HttpOptions().apply {
            this.timeoutSeconds = timeoutSeconds
        })

fun Handling.timeout(timeout: Duration) =
        withOptions(HttpOptions().apply {
            this.timeoutSeconds = timeout.toMillis() / 1000
        })

fun Handling.withInterceptors(vararg interceptors: Interceptor) =
        withOptions(HttpOptions().apply {
            this.interceptors = interceptors.toList()
        })
