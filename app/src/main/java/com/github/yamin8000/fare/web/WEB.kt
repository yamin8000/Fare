/*
 *     WEB.kt Created by Yamin Siahmargooei at 2021/7/1
 *     Fare: find Iran's cities taxi fares
 *     This file is part of Fare.
 *     Copyright (C) 2021  Yamin Siahmargooei
 *
 *     Fare is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Fare is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Fare.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.github.yamin8000.fare.web

import androidx.lifecycle.*
import com.github.yamin8000.fare.util.SUPABASE
import com.orhanobut.logger.Logger
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import okhttp3.OkHttpClient
import retrofit2.*
import retrofit2.converter.moshi.MoshiConverterFactory

object WEB {

    val retrofit: Retrofit by lazy(LazyThreadSafetyMode.NONE) { createRetrofit() }

    private fun createRetrofit(): Retrofit {
        val okhttpClient = OkHttpClient.Builder().addInterceptor {
            return@addInterceptor it.proceed(
                it.request().newBuilder()
                    .addHeader(SUPABASE.APIKEY, SUPABASE.SUPA_BASE_KEY).build()
            )
        }.build()

        return Retrofit.Builder()
            .client(okhttpClient)
            .baseUrl(SUPABASE.SUPA_BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    /**
     * get service of given api/interface
     *
     * @param T type/class/interface of api
     * @return service for that api
     */
    inline fun <reified T> getAPI(): T = retrofit.create(T::class.java)


    /**
     * async callback
     *
     * this callback is aware of context or lifecycle
     *
     * @param T result type
     * @param lifeCycleOwner lifecycle owner of this request
     * @param onSuccess callback when request is successful
     * @param onFail callback when request is failed
     */
    inline fun <reified T> Call<T>.async(
        lifeCycleOwner: LifecycleOwner, crossinline onSuccess: (T) -> Unit,
        crossinline onFail: (Throwable) -> Unit,
    ) {
        lifeCycleOwner.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_DESTROY) {
                    this@async.cancel()
                }
            }
        })

        this.enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) {
                val body = response.body()
                if (body != null) onSuccess(body)
                else {
                    /**
                     * beware in this project we know that.
                     * all of our api interface methods return a list,
                     * so this is safe, casting listOf<T> as T,
                     * because we know T is actually List<*> everytime in here,
                     * anyway to prevent ClassCastException,
                     * we also double check T type,
                     * so if T is not a list onSuccess callback is never called when response body is null
                     */
                    if (T::class.java == listOf<T>()::class.java) onSuccess(listOf<T>() as T)
                    onFail(IllegalStateException("Body is not a list!"))
                }
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                if (!isCanceled) onFail(t)
                else Logger.d("${call.request().url()} is canceled")
            }
        })
    }

    /**
     * async callback which pass Response class instead of only data
     *
     * this is useful when you want to know http headers or http codes
     *
     * this callback is aware of context or lifecycle
     *
     * @param T result type
     * @param lifeCycleOwner lifecycle owner of this request
     * @param onSuccess callback when request is successful
     * @param onFail callback when request is failed
     */
    fun <T> Call<T>.asyncResponse(
        lifeCycleOwner: LifecycleOwner, onSuccess: (Response<T>) -> Unit,
        onFail: (Throwable) -> Unit,
    ) {
        lifeCycleOwner.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_DESTROY) {
                    this@asyncResponse.cancel()
                }
            }
        })

        this.enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) = onSuccess(response)

            override fun onFailure(call: Call<T>, t: Throwable) {
                if (!isCanceled) onFail(t)
                else Logger.d("${call.request().url()} is canceled")
            }
        })
    }

    /**
     * generic method for converting List of type T json array String
     *
     * @param T type of data
     * @return json array string
     */
    inline fun <reified T> List<T>.toJsonArray(): String {
        val moshi = Moshi.Builder().build()
        val type = Types.newParameterizedType(List::class.java, T::class.java)
        val jsonAdapter = moshi.adapter<List<T>>(type)
        return jsonAdapter.toJson(this)
    }

    /**
     * generic method for converting json array string to List of objects
     *
     * @param T type of data
     * @return List of type T
     */
    inline fun <reified T> String.fromJsonArray(): List<T>? {
        return if (this.isNotBlank()) {
            val moshi = Moshi.Builder().build()
            val type = Types.newParameterizedType(List::class.java, T::class.java)
            val jsonAdapter = moshi.adapter<List<T>>(type)
            jsonAdapter.fromJson(this)
        } else null
    }

    /**
     * Like query is used for supabase/postgrest requests
     * that are added to query parameter
     *
     */
    fun String.likeQuery() = "like.*$this*"

    /**
     * Eq query is used for supabase/postgrest requests
     * that are added to query parameter
     *
     */
    fun String.eqQuery() = "eq.$this"
}