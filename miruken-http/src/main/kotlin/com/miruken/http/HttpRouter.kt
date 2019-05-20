@file:Suppress("unused")

package com.miruken.http

import com.miruken.api.route.Routed
import com.miruken.api.route.Routes
import com.miruken.callback.*
import com.miruken.concurrent.Promise
import com.miruken.concurrent.timeout
import com.miruken.map.map
import okhttp3.OkHttpClient
import retrofit2.Converter
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import java.net.URI
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

@Routes("http", "https")
class HttpRouter
    @Provides @Singleton
    constructor(private val converter: Converter.Factory) : Handler() {

    private val _httpClient = OkHttpClient.Builder().build()
    private val _apis = ConcurrentHashMap<HttpOptions, HttpRouteApi>()
    private val _errorConverter = converter.responseBodyConverter(
            Message::class.java, Message::class.java.annotations,
            Retrofit.Builder().baseUrl("http://localhost/")
                    .build())

    @Handles
    fun route(routed: Routed, command: Command, composer: Handling): Promise<*> {
        val payload = routed.message
        val url     = getResourceUrl(routed, command)
        val options = composer.getOptions(HttpOptions())?.also {
            DEFAULT_HTTP_OPTIONS.mergeInto(it)
        } ?: DEFAULT_HTTP_OPTIONS
        val api = getHttpRouteApi(options)
        return (api.process(url, Message(payload)) then {
            if (it.isSuccessful) {
                it.body()?.payload
            } else {
                handleError(it, composer)
            }
        }).timeout(options.timeoutSeconds!! * 1000)
    }

    private fun getHttpRouteApi(options: HttpOptions) =
        _apis.getOrPut(options) {
            val timeout = options.timeoutSeconds!!
            val builder = _httpClient.newBuilder()
                    .connectTimeout(timeout, TimeUnit.SECONDS)
                    .readTimeout(timeout, TimeUnit.SECONDS)
                    .writeTimeout(timeout, TimeUnit.SECONDS).also {
                        options.interceptors?.fold(it) { b, i ->
                            b.addInterceptor(i)
                        }
                    }
            Retrofit.Builder()
                    .client(builder.build())
                    .addCallAdapterFactory(PromiseCallAdapterFactory)
                    .addConverterFactory(converter)
                    .baseUrl("http://localhost/")
                    .build()
                    .create(HttpRouteApi::class.java)
        }

    private fun handleError(response: Response<Message>, composer: Handling): Nothing {
        val errorBody = response.errorBody()
        if (_errorConverter != null && errorBody != null) {
            try {
                val message = _errorConverter.convert(errorBody) as? Message
                message?.payload
            } catch (t: Throwable) {
                null
            }?.also { mapping ->
               composer.bestEffort.map<Throwable>(
                       mapping, format = Throwable::class)?.also {
                   throw it
               }
            }
        }
        throw HttpException(response)
    }

    private fun getResourceUrl(routed: Routed, command: Command) =
            URI(routed.route).resolve(when (command.many) {
                true -> "publish"
                else -> "process"
            })

    companion object {
        private val DEFAULT_HTTP_OPTIONS = HttpOptions().apply {
            timeoutSeconds = 3
        }
    }
}