package com.miruken.http.jackson

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

object OffsetDateTimeSerializer : StdSerializer<OffsetDateTime>(
        OffsetDateTime::class.java
) {
    override fun serialize(
            value:    OffsetDateTime,
            gen:      JsonGenerator,
            provider: SerializerProvider) {
        gen.writeString(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(value))
    }
}

object OffsetDateTimeDeserializer : StdDeserializer<OffsetDateTime>(
        OffsetDateTime::class.java
) {
    override fun deserialize(
            parser: JsonParser,
            ctxt:   DeserializationContext
    ): OffsetDateTime = OffsetDateTime.parse(parser.text, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
}
