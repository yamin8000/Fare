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

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.github.yamin8000.fare.util.SUPABASE.SUPA_BASE_URL
import com.orhanobut.logger.Logger
import retrofit2.*
import retrofit2.converter.moshi.MoshiConverterFactory

class WEB(
    baseUrl : String = SUPA_BASE_URL,
    converterFactory : Converter.Factory = MoshiConverterFactory.create(),
         ) {
    
    private var retrofit = Retrofit.Builder().baseUrl(baseUrl).addConverterFactory(converterFactory).build()
    
    fun <T> getService(clazz : Class<T>) : T = retrofit.create(clazz)
    
    companion object {
        
        /**
         * async callback
         *
         * this callback is aware of context or lifecycle
         *
         * @param T result type
         * @param callback callback lambda
         * @receiver any type of retrofit Call
         * @see Call<T>
         */
        fun <T> Call<T>.async(
            lifeCycleOwner : LifecycleOwner, onSuccess : (T?) -> Unit,
            onFail : (Throwable) -> Unit,
                             ) {
            lifeCycleOwner.lifecycle.addObserver(object : LifecycleObserver {
                @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                fun cancelCall() {
                    this@async.cancel()
                }
            })
            
            this.enqueue(object : Callback<T> {
                override fun onResponse(call : Call<T>, response : Response<T>) = onSuccess(response.body())
                
                override fun onFailure(call : Call<T>, t : Throwable) {
                    if (!isCanceled) onFail(t)
                    else Logger.d("${call.request().url()} is canceled")
                }
            })
        }
        
        fun <T> Call<T>.asyncResponse(
            lifeCycleOwner : LifecycleOwner, onSuccess : (Response<T>) -> Unit,
            onFail : (Throwable) -> Unit,
                                     ) {
            lifeCycleOwner.lifecycle.addObserver(object : LifecycleObserver {
                @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                fun cancelCall() {
                    this@asyncResponse.cancel()
                }
            })
            
            this.enqueue(object : Callback<T> {
                override fun onResponse(call : Call<T>, response : Response<T>) = onSuccess(response)
                
                override fun onFailure(call : Call<T>, t : Throwable) {
                    if (!isCanceled) onFail(t)
                    else Logger.d("${call.request().url()} is canceled")
                }
            })
        }
        
        fun String.likeQuery() = "like.*$this*"
        
        fun String.eqQuery() = "eq.$this"
    }
}