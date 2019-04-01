package com.miruken.http.jackson

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.miruken.api.NamedType

object VoidNamedType: NamedType {
    override val typeName: String =
            "System.Threading.Tasks.VoidTaskResult,mscorlib"
}

object VoidNamedTypeDeserializer : StdDeserializer<VoidNamedType>(
        VoidNamedType::class.java
) {
    override fun deserialize(
            parser: JsonParser,
            ctxt:   DeserializationContext
    ): VoidNamedType? = null
}