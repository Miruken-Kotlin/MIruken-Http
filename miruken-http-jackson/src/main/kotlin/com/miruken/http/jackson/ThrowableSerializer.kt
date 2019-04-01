package com.miruken.http.jackson

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.ser.std.StdSerializer

object ThrowableSerializer : StdSerializer<Throwable>(
        Throwable::class.java
) {
    override fun serialize(
            value:    Throwable?,
            gen:      JsonGenerator,
            provider: SerializerProvider
    ) {
        if (value == null) {
            gen.writeNull()
        } else {
            gen.writeStartObject()
            gen.writeStringField("message", value.message)
            gen.writeEndObject()
        }
    }
}

object ThrowableDeserializer : StdDeserializer<Throwable>(
        Throwable::class.java
) {
    override fun deserialize(
            parser: JsonParser,
            ctxt:   DeserializationContext
    ): Throwable? {
        val tree = parser.codec.readTree<JsonNode>(parser)
        return tree.get("message")?.textValue()?.let {
            Exception(it)
        } ?: throw JsonMappingException.from(parser,
                "Expected field 'message' was missing")
    }
}