package com.miruken.http.jackson

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.deser.ContextualDeserializer
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.miruken.api.Try
import com.miruken.api.fold

object TrySerializer : StdSerializer<Try<*,*>>(Try::class.java) {
    override fun serialize(
            value:    Try<*,*>?,
            gen:      JsonGenerator,
            provider: SerializerProvider) {
        if (value == null) {
            gen.writeNull()
        } else {
            gen.writeStartObject()
            value.fold({
                gen.writeBooleanField("isLeft", true)
                gen.writeObjectField("value", it)
            }, {
                gen.writeBooleanField("isLeft", false)
                gen.writeObjectField("value", it)
            })
            gen.writeEndObject()
        }
    }
}

class TryDeserializer : StdDeserializer<Try<*,*>>(Try::class.java),
        ContextualDeserializer {
    private lateinit var _tryType: JavaType

    override fun createContextual(
            ctxt:     DeserializationContext,
            property: BeanProperty?
    ): JsonDeserializer<*> {
        val tryType = ctxt.contextualType ?: property?.type
            ?: error("Unable to determine Try parameters")
        return TryDeserializer().apply { _tryType = tryType }
    }

    override fun deserialize(
            parser: JsonParser,
            ctxt:   DeserializationContext
    ): Try<*,*>? {
        val tree    = parser.codec.readTree<JsonNode>(parser)
        val isError = tree.get("isLeft")?.booleanValue()
                ?: throw JsonMappingException.from(parser,
                        "Expected field 'isLeft' was missing")

        val value   = tree.get("value")
                ?: throw JsonMappingException.from(parser,
                        "Expected field 'value' was missing")

        return when (isError) {
            true -> Try.error(value.traverse(parser.codec)
                    .readValueAs(_tryType.containedType(0).rawClass)
                    as Throwable)
            false -> Try.result(value.traverse(parser.codec)
                    .readValueAs(_tryType.containedType(1).rawClass))
        }
    }
}