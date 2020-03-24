package com.miruken.http.jackson

import com.miruken.api.Try
import com.miruken.api.route.BatchRouter
import com.miruken.api.route.routeTo
import com.miruken.api.schedule.ScheduledResult
import com.miruken.api.send
import com.miruken.callback.NotHandledException
import com.miruken.callback.batch
import com.miruken.callback.policy.HandlerDescriptorFactory
import com.miruken.callback.policy.MutableHandlerDescriptorFactory
import com.miruken.callback.policy.registerDescriptor
import com.miruken.http.HttpRouter
import com.miruken.http.Message
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestName
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HttpRouterTest {
    @Rule
    @JvmField val testName = TestName()

    @Rule
    @JvmField val server = MockWebServer()

    private lateinit var router: HttpRouter

    @Before
    fun setup() {
        HandlerDescriptorFactory.useFactory(
                MutableHandlerDescriptorFactory().apply {
                    registerDescriptor<BatchRouter>()
                    registerDescriptor<HttpRouter>()
                }
        )
        router = HttpRouter(JacksonProvider.retrofitConverter())
    }

    @Test fun `Sends a request and receives a response`() {
        val quote = Message(StockQuote("GOOGL", 1071.49, StockType.Common))
        val json  = JacksonProvider.mapper.writeValueAsString(quote)
        server.enqueue(MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody(json))
        assertAsync(testName) { done ->
            router.send(GetStockQuote("GOOGL")
                    .routeTo(server.url("/").toUrl().toString())) then {
                assertEquals("GOOGL", it.symbol)
                assertEquals(1071.49, it.value)
                done()
            }
        }
    }

    @Test fun `Batches requests and receives a response`() {
        val mixed = Message(ScheduledResult(listOf(
                Try.Success(StockQuote("MSFT", 106.16, StockType.Preferred)),
                Try.error(Exception("Symbol 'KLMN' not found")))))
        val json  = JacksonProvider.mapper.writeValueAsString(mixed)
        server.enqueue(MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody(json))
        val url = server.url("/").toUrl().toString()
        assertAsync(testName) { done ->
            var count = 0
            router.batch { batch ->
                batch.send(GetStockQuote("MSFT").routeTo(url)) then {
                    assertEquals("MSFT", it.symbol)
                    assertEquals(106.16, it.value)
                    assertEquals(StockType.Preferred, it.type)
                    ++count
                }
                batch.send(GetStockQuote("KLMN").routeTo(url)) catch {
                    assertEquals("Symbol 'KLMN' not found", it.message)
                    ++count
                }
            } then {results ->
                assertEquals(1, results.size)
                assertEquals(2, count)
                done()
            }
        }
    }

    @Test fun `Batches single request and receives a response`() {
        val result = Message(ScheduledResult(listOf(
                Try.Success(StockQuote("MSFT", 106.16, StockType.Preferred)))))
        val json   = JacksonProvider.mapper.writeValueAsString(result)
        server.enqueue(MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody(json))
        val url = server.url("/").toUrl().toString()
        assertAsync(testName) { done ->
            var count = 0
            router.batch { batch ->
                batch.send(GetStockQuote("MSFT").routeTo(url)) then {
                    assertEquals("MSFT", it.symbol)
                    assertEquals(106.16, it.value)
                    assertEquals(StockType.Preferred, it.type)
                    ++count
                }
            } then {results ->
                assertEquals(1, results.size)
                assertEquals(1, count)
                done()
            }
        }
    }

    @Test fun `Batches single request and receives a failure`() {
        val result = Message(ScheduledResult(listOf(
                Try.error(Exception("Symbol 'KLMN' not found")))))
        val json   = JacksonProvider.mapper.writeValueAsString(result)
        server.enqueue(MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody(json))
        val url = server.url("/").toUrl().toString()
        assertAsync(testName) { done ->
            var count = 0
            router.batch { batch ->
                batch.send(GetStockQuote("KLMN").routeTo(url)) catch {
                    assertEquals("Symbol 'KLMN' not found", it.message)
                    ++count
                }
            } then {results ->
                assertEquals(1, results.size)
                assertEquals(1, count)
                done()
            }
        }
    }

    @Test fun `Sends single batch as failed response`() {
        server.enqueue(MockResponse().setResponseCode(404))
        val url = server.url("/").toUrl().toString()
        assertAsync(testName) { done ->
            var count = 0
            router.batch { batch ->
                batch.send(GetStockQuote("MSFT").routeTo(url)) catch {
                    ++count
                }
            } catch {
                done()
            }
        }
    }

    @Test fun `Fails response when IO exception`() {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AFTER_REQUEST))
        assertAsync(testName) { done ->
            router.send(GetStockQuote("GOOGL")
                    .routeTo(server.url("/").toUrl().toString())) catch {
                assertTrue(it is IOException)
                done()
            }
        }
    }

    @Test fun `Fails is not routing HTTP`() {
        assertAsync(testName) { done ->
            router.send(GetStockQuote("GOOGL")
                    .routeTo("queue")) catch {
                assertTrue(it is NotHandledException)
                done()
            }
        }
    }
}