package com.miruken.http

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

internal class StringConverterFactory : Converter.Factory() {
    override fun requestBodyConverter(
            type:                 Type,
            parameterAnnotations: Array<Annotation>,
            methodAnnotations:    Array<Annotation>?,
            retrofit:             Retrofit
    ): Converter<*, RequestBody> =
            Converter<String, RequestBody> {
                value -> value.toRequestBody("text/plain".toMediaTypeOrNull())
            }

    override fun responseBodyConverter(
            type:        Type,
            annotations: Array<Annotation>,
            retrofit:    Retrofit
    ): Converter<ResponseBody, *> =
            Converter<ResponseBody, String> {
                value -> value.string()
            }
}