package com.miruken.http.jackson

import com.miruken.api.NamedType
import com.miruken.api.Request
import com.miruken.api.schedule.Concurrent
import com.miruken.http.Message
import com.miruken.http.RawJson
import org.junit.Test
import kotlin.test.assertEquals

class ValidationErrorMappingTest {
    @Test fun `Deserializes message with validation errors`() {
        val message = Message(StockQuote("AAPL", 207.48, StockType.Common))
        val json    = JacksonProvider.mapper.writeValueAsString(message)
        assertEquals("{\"payload\":{\"\$type\":\"StockQuote\",\"symbol\":\"AAPL\",\"value\":207.48,\"type\":0}}", json)
    }
}