package com.miruken.http.jackson

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreType
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.DatabindContext
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.type.TypeFactory
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import com.miruken.TypeReference
import com.miruken.api.NamedType
import com.miruken.api.Request
import com.miruken.api.Try
import com.miruken.http.RawJson
import java.lang.reflect.Type
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.allSupertypes
import kotlin.reflect.full.companionObjectInstance

object MirukenApiModule : SimpleModule() {
    init {
        setMixInAnnotation(
                KType::class.java,
                IgnoreMixin::class.java)

        setMixInAnnotation(
                TypeReference::class.java,
                IgnoreMixin::class.java)

        setMixInAnnotation(
                NamedType::class.java,
                NamedTypeMixin::class.java)

        addSerializer(LocalDateTime::class.java, LocalDateTimeSerializer)
        addDeserializer(LocalDateTime::class.java, LocalDateTimeDeserializer)

        addSerializer(OffsetDateTime::class.java, OffsetDateTimeSerializer)
        addDeserializer(OffsetDateTime::class.java, OffsetDateTimeDeserializer)

        addSerializer(Throwable::class.java, ThrowableSerializer)
        addDeserializer(Throwable::class.java, ThrowableDeserializer)

        addSerializer(Try::class.java, TrySerializer)
        addDeserializer(Try::class.java, TryDeserializer())

        addDeserializer(VoidNamedType::class.java, VoidNamedTypeDeserializer)

        addSerializer(RawJson::class.java, RawJsonSerializer)
        addDeserializer(RawJson::class.java, RawJsonDeserializer)

        register(VoidNamedType)
    }

    @JsonIgnoreType
    interface IgnoreMixin

    @JsonTypeInfo(
            use      = JsonTypeInfo.Id.NAME,
            include  = JsonTypeInfo.As.PROPERTY,
            property = "\$type")
    @JsonTypeIdResolver(NamedTypeIdResolver::class)
    interface NamedTypeMixin {
        @get:JsonIgnore
        val typeName: String
    }

    object NamedTypeIdResolver : TypeIdResolverBase() {
        override fun idFromValue(value: Any) =
                idFromValueAndType(value, value::class.java)

        override fun idFromValueAndType(
                value:         Any,
                suggestedType: Class<*>?
        ): String? {
            val typeName = (value as? NamedType)?.typeName
            if (!typeName.isNullOrBlank()) {
                registerResponseTypeId(value)
                return typeName
            }
            return suggestedType?.let { typeToIdMapping[it] } ?:
            error("${value::class} requires a valid typeName")
        }

        override fun typeFromId(
                context: DatabindContext,
                id:      String
        ) = idToTypeMapping[canonicalTypeId(id)]

        override fun getMechanism() = JsonTypeInfo.Id.NAME

        private fun registerResponseTypeId(value: Any) {
            (value::class.allSupertypes.firstOrNull {
                it.classifier == Request::class
            }) ?.arguments?.first()?.type?.also { responseType ->
                (responseType.classifier as? KClass<*>)?.also { nt ->
                    val companion = nt.companionObjectInstance as? NamedType
                    companion?.typeName?.takeUnless { it.isBlank() }?.also {
                        idToTypeMapping.computeIfAbsent(canonicalTypeId(it)) { typeName ->
                            val javaType = nt.java
                            typeToIdMapping[nt.java] = typeName
                            TypeFactory.defaultInstance().constructType(javaType)
                        }
                    }
                }
            }
        }
    }

    inline fun <reified T: NamedType> register(namedType: T) {
        jacksonTypeRef<T>().type.let {
            (it as? Class<*>)?.enclosingClass ?: it
        }?.also {
            register(namedType.typeName, it)
        }
    }

    inline fun <reified T: Any> register(typeId: String) =
            register(typeId, jacksonTypeRef<T>().type)

    fun register(typeId: String, type: Type) {
        require(typeId.isNotBlank()) {
            "Type Identifier for $type cannot be empty"
        }
        canonicalTypeId(typeId).also {
            idToTypeMapping[it] = TypeFactory.defaultInstance()
                    .constructType(type)
            typeToIdMapping[type] = it
        }
    }

    private fun canonicalTypeId(typeId: String) =
            typeId.replace(allSpaces, "")
}

private val idToTypeMapping = ConcurrentHashMap<String, JavaType>()
private val typeToIdMapping = ConcurrentHashMap<Type, String>()
private val allSpaces       = "\\s".toRegex()