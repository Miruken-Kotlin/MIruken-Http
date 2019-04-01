package com.miruken.http.jackson

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer
import com.fasterxml.jackson.databind.jsontype.TypeSerializer
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.miruken.http.RawJson

object RawJsonSerializer : StdSerializer<RawJson>(RawJson::class.java) {
    override fun serialize(
            value:    RawJson?,
            gen:      JsonGenerator,
            provider: SerializerProvider
    ) {
        if (value?.json == null) {
            gen.writeNull()
        } else {
            gen.writeRawValue(value.json)
        }
    }

    override fun serializeWithType(
            value:       RawJson?,
            gen:         JsonGenerator,
            serializers: SerializerProvider,
            typeSer:     TypeSerializer
    ) {
        serialize(value, gen, serializers)
    }
}

object RawJsonDeserializer : StdDeserializer<RawJson>(RawJson::class.java) {
    override fun deserialize(
            parser: JsonParser,
            ctxt:   DeserializationContext
    ) = RawJson(parser.codec.readTree<TreeNode>(parser).toString())

    override fun deserializeWithType(
            parser:           JsonParser,
            ctxt:             DeserializationContext,
            typeDeserializer: TypeDeserializer
    ) = deserialize(parser, ctxt)
}