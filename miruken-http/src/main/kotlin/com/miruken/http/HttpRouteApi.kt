package com.miruken.http

import com.miruken.concurrent.Promise
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url
import java.net.URI

interface HttpRouteApi {
    @POST
    fun process(@Url url: URI, @Body message: Message): Promise<Response<Message>>
}