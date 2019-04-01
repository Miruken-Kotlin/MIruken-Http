package com.miruken.http.jackson

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import java.time.LocalDateTime
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField

private val DATETIME_FORMATTER =
        DateTimeFormatterBuilder().appendPattern("yyyy-MM-dd'T'HH:mm:ss")
                .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
                .toFormatter()

object LocalDateTimeSerializer : StdSerializer<LocalDateTime>(
        LocalDateTime::class.java
) {
    override fun serialize(
            value: LocalDateTime,
            gen: JsonGenerator,
            provider: SerializerProvider) {
        gen.writeString(DATETIME_FORMATTER.format(value))
    }
}

object LocalDateTimeDeserializer : StdDeserializer<LocalDateTime>(
        LocalDateTime::class.java
) {
    override fun deserialize(
            parser: JsonParser,
            ctxt: DeserializationContext
    ): LocalDateTime = LocalDateTime.parse(parser.text, DATETIME_FORMATTER)
}
