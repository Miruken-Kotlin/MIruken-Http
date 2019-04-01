package com.miruken.http

import com.miruken.concurrent.Promise
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy.DISCONNECT_AFTER_REQUEST
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestName
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PromiseCallAdapterFactoryApiTest {
    @Rule
    @JvmField val testName = TestName()

    @Rule
    @JvmField val server = MockWebServer()

    private lateinit var service: Service

    @Before
    fun setup() {
        val retrofit = Retrofit.Builder()
                .baseUrl(server.url("/"))
                .addConverterFactory(StringConverterFactory())
                .addCallAdapterFactory(PromiseCallAdapterFactory)
                .build()
        service = retrofit.create(Service::class.java)
    }

    interface Service {
        @GET("/") fun body(): Promise<String>
        @GET("/") fun response(): Promise<Response<String>>
    }

    @Test fun `Gets a successful promise`() {
        server.enqueue(MockResponse().setBody("Hi"))
        assertAsync(testName) { done ->
            service.body() then {
                assertEquals("Hi", it)
                done()
            }
        }
    }

    @Test fun `Gets a failed promise`() {
        server.enqueue(MockResponse().setResponseCode(404))
        assertAsync(testName) { done ->
            service.body() catch {
                assertEquals("HTTP 404 Client Error", it.message)
                done()
            }
        }
    }

    @Test fun `Gets a failed promise if IO exception`() {
        assertAsync(testName) { done ->
            server.enqueue(MockResponse().setSocketPolicy(DISCONNECT_AFTER_REQUEST))
            service.body() catch {
                assertTrue(it is IOException)
                done()
            }
        }
    }

    @Test fun `Gets a successful wrapped promise`() {
        server.enqueue(MockResponse().setBody("Hi"))
        assertAsync(testName) { done ->
            service.response() then {
                assertTrue(it.isSuccessful)
                assertEquals("Hi", it.body())
                done()
            }
        }
    }


    @Test fun `Gets a failed wrapped promise`() {
        server.enqueue(MockResponse().setResponseCode(404).setBody("Hi"))
        assertAsync(testName) { done ->
            service.response() then {
                assertFalse(it.isSuccessful)
                assertEquals(404, it.code())
                assertEquals("Hi", it.errorBody()!!.string())
                done()
            }
        }
    }

    @Test fun `Gets a failed wrapped promise if IO exception`() {
        assertAsync(testName) { done ->
            server.enqueue(MockResponse().setSocketPolicy(DISCONNECT_AFTER_REQUEST))
            service.response() catch {
                assertTrue(it is IOException)
                done()
            }
        }
    }
}