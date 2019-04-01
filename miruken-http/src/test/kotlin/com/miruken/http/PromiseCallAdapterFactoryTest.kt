package com.miruken.http

import com.miruken.concurrent.Promise
import com.miruken.typeOf
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Response
import retrofit2.Retrofit
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PromiseCallAdapterFactoryTest {
    @Rule
    @JvmField val server = MockWebServer()

    private val factory = PromiseCallAdapterFactory

    private lateinit var retrofit: Retrofit

    @Before
    fun setup() {
        retrofit = Retrofit.Builder()
                .baseUrl(server.url("/"))
                .addConverterFactory(StringConverterFactory())
                .addCallAdapterFactory(factory)
                .build()
    }

    @Test fun `Creates property response type`() {
        val bodyClass = typeOf<Promise<String>>().type
        assertEquals(String::class.java,
                factory.get(bodyClass, emptyArray(), retrofit)
                !!.responseType())
        val bodyGeneric = typeOf<Promise<List<String>>>().type
        assertEquals(typeOf<List<String>>().type,
                factory.get(bodyGeneric, emptyArray(), retrofit)
                !!.responseType())
        val responseClass = typeOf<Promise<Response<String>>>().type
        assertEquals(String::class.java,
                factory.get(responseClass, emptyArray(), retrofit)
                !!.responseType())

    }

    @Test fun `Adapter not created if not a promise return`() {
        val adapter = factory.get(String::class.java, emptyArray(), retrofit)
        assertNull(adapter)
    }
}