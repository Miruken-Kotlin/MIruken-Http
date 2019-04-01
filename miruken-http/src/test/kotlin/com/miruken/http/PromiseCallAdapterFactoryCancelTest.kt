@file:Suppress("UNCHECKED_CAST")

package com.miruken.http

import com.miruken.concurrent.Promise
import com.miruken.concurrent.PromiseState
import com.miruken.typeOf
import org.junit.Test
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PromiseCallAdapterFactoryCancelTest {
    private val factory  = PromiseCallAdapterFactory
    private val retrofit = Retrofit.Builder()
            .baseUrl("http://example.com")
            .callFactory { TODO() }
            .build()

    @Test fun `Cancel ignored on success`() {
        val promiseString = typeOf<Promise<String>>().type
        val adapter = factory.get(promiseString, emptyArray(), retrofit)!!
                as CallAdapter<String, Promise<String>>
        val call    = CompletableCall<String>()
        val promise = adapter.adapt(call)
        call.complete("hey")
        assertFalse(call.isCanceled)
        assertEquals(PromiseState.FULFILLED, promise.state)
    }

    @Test fun `Cancel ignored on failure`() {
        val promiseString = typeOf<Promise<String>>().type
        val adapter = factory.get(promiseString, emptyArray(), retrofit)!!
                as CallAdapter<String, Promise<String>>
        val call    = CompletableCall<String>()
        val promise = adapter.adapt(call)
        call.completeWithException(IOException())
        assertFalse(call.isCanceled)
        assertEquals(PromiseState.REJECTED, promise.state)
    }

    @Test fun `Cancels call when promise cancelled`() {
        val promiseString = typeOf<Promise<String>>().type
        val adapter = factory.get(promiseString, emptyArray(), retrofit)!!
                as CallAdapter<String, Promise<String>>
        val call    = CompletableCall<String>()
        val promise = adapter.adapt(call)
        assertFalse(call.isCanceled)
        promise.cancel()
        assertTrue(call.isCanceled)
    }
}