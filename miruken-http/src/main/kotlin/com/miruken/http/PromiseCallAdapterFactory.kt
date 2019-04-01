package com.miruken.http

import com.miruken.concurrent.ChildCancelMode
import com.miruken.concurrent.Promise
import retrofit2.*
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

object PromiseCallAdapterFactory : CallAdapter.Factory() {
    override fun get(
            returnType:  Type,
            annotations: Array<out Annotation>,
            retrofit:    Retrofit
    ): CallAdapter<*, *>? {
        if (Promise::class.java != getRawType(returnType)) {
            return null
        }
        if (returnType !is ParameterizedType) {
            error("Promise return type must be parameterized as Promise<Foo> or Promise<out Foo>")
        }
        val responseType = getParameterUpperBound(0, returnType)

        val rawPromiseType = getRawType(responseType)
        return if (rawPromiseType == Response::class.java) {
            if (responseType !is ParameterizedType) {
                error("Response must be parameterized as Response<Foo> or Response<out Foo>")
            }
            ResponseCallAdapter<Any>(getParameterUpperBound(0, responseType))
        } else {
            BodyCallAdapter<Any>(responseType)
        }
    }

    private class BodyCallAdapter<T>(
            private val responseType: Type
    ) : CallAdapter<T, Promise<T>> {

        override fun responseType() = responseType

        override fun adapt(call: Call<T>): Promise<T> {
            return Promise(ChildCancelMode.ANY) { resolve, reject, onCancel ->
                onCancel(call::cancel)

                call.enqueue(object : Callback<T> {
                    override fun onFailure(call: Call<T>, t: Throwable) {
                        reject(t)
                    }

                    override fun onResponse(call: Call<T>, response: Response<T>) {
                        if (response.isSuccessful) {
                            resolve(response.body()!!)
                        } else {
                            reject(HttpException(response))
                        }
                    }
                })
            }
        }
    }

    private class ResponseCallAdapter<T>(
            private val responseType: Type
    ) : CallAdapter<T, Promise<Response<T>>> {

        override fun responseType() = responseType

        override fun adapt(call: Call<T>): Promise<Response<T>> {
            return Promise(ChildCancelMode.ANY) { resolve, reject, onCancel ->
                onCancel(call::cancel)

                call.enqueue(object : Callback<T> {
                    override fun onFailure(call: Call<T>, t: Throwable) {
                      reject(t)
                    }

                    override fun onResponse(call: Call<T>, response: Response<T>) {
                       resolve(response)
                    }
                })
            }
        }
    }
}