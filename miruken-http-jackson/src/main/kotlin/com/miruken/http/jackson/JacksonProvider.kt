package com.miruken.http.jackson

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.cfg.MapperConfig
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.miruken.callback.Provides
import com.miruken.callback.Singleton
import retrofit2.Converter
import retrofit2.converter.jackson.JacksonConverterFactory

object JacksonProvider {
    @Provides @Singleton
    fun retrofitConverter(): Converter.Factory =
            JacksonConverterFactory.create(mapper)

    @get:Provides @get:Singleton
    val mapper: ObjectMapper get() =
        jacksonObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .enable(SerializationFeature.WRITE_ENUMS_USING_INDEX)
                .setPropertyNamingStrategy(GetterNamingStrategy)
                .registerModule(MirukenApiModule)

    private object GetterNamingStrategy : PropertyNamingStrategy() {
        override fun nameForGetterMethod(
                config:      MapperConfig<*>,
                method:      AnnotatedMethod,
                defaultName: String
        ): String {
            if (method.hasReturnType() && method.name.startsWith("is")) {
                val rawType = method.rawReturnType
                if (rawType === Boolean::class.java ||
                    rawType === Boolean::class.javaObjectType ||
                    rawType === Boolean::class.javaPrimitiveType) {
                    return method.name
                }
            }
            return defaultName
        }
    }
}