package com.miruken.http.jackson

import com.miruken.api.NamedType
import com.miruken.api.Request
import com.miruken.api.schedule.Concurrent
import com.miruken.http.Message
import com.miruken.http.RawJson
import org.junit.Test
import kotlin.test.assertEquals

class MessageTest {
    @Test fun `Serializes message with payload`() {
        val message = Message(StockQuote("AAPL", 207.48, StockType.Common))
        val json    = JacksonProvider.mapper.writeValueAsString(message)
        assertEquals("{\"payload\":{\"\$type\":\"StockQuote\",\"symbol\":\"AAPL\",\"value\":207.48,\"type\":0}}", json)
    }

    @Test fun `Serializes message with empty payload`() {
        val message = Message()
        val json    = JacksonProvider.mapper.writeValueAsString(message)
        assertEquals("{}", json)
    }

    @Test fun `Serializes concurrent request of messages`() {
        val message = Message(Concurrent(listOf(
                GetStockQuote("AAPL"), GetStockQuote("GOOGL"))))
        val json    = JacksonProvider.mapper.writeValueAsString(message)
        assertEquals("{\"payload\":{\"\$type\":\"Miruken.Api.Schedule.Concurrent,Miruken\",\"requests\":[{\"\$type\":\"GetStockQuote\",\"symbol\":\"AAPL\"},{\"\$type\":\"GetStockQuote\",\"symbol\":\"GOOGL\"}]}}", json)
    }

    @Test fun `Serializes message with raw json`() {
        val message = Message(RawJson("{\"\$type\":\"StockQuote\",\"symbol\":\"AAPL\",\"value\":207.48,\"type\":0}"))
        val json    = JacksonProvider.mapper.writeValueAsString(message)
        assertEquals("{\"payload\":{\"\$type\":\"StockQuote\",\"symbol\":\"AAPL\",\"value\":207.48,\"type\":0}}", json)
    }

    @Test fun `Serializes message with is property`() {
        val message = Message(GetStockQuote("AAPL", isPercentage = true))
        val json    = JacksonProvider.mapper.writeValueAsString(message)
        assertEquals("{\"payload\":{\"\$type\":\"StockQuote\",\"symbol\":\"AAPL\",\"isPercentage\":true}}", json)
    }

    data class StockQuote(
            val symbol: String,
            val value:  Double,
            override val typeName: String = StockQuote.typeName
    ): NamedType {
        companion object : NamedType {
            override val typeName = "StockQuote"
        }
    }

    data class GetStockQuote(
            val symbol: String,
            override val typeName: String = GetStockQuote.typeName
    ): Request<StockQuote> {
        companion object : NamedType {
            override val typeName = "GetStockQuote"
        }
    }
}