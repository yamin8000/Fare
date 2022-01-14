/*
 *     Services.kt Created by Yamin Siahmargooei at 2021/7/1
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

import com.github.yamin8000.fare.model.*
import com.github.yamin8000.fare.util.CONSTANTS.CITY_ID
import com.github.yamin8000.fare.util.CONSTANTS.DESTINATION
import com.github.yamin8000.fare.util.CONSTANTS.LIMIT
import com.github.yamin8000.fare.util.CONSTANTS.LINE_CODE
import com.github.yamin8000.fare.util.CONSTANTS.ORIGIN
import com.github.yamin8000.fare.util.CONSTANTS.STATE_ID
import retrofit2.Call
import retrofit2.http.*

object APIs {

    //table
    private const val FEEDBACK = "Feedback"
    private const val LICENSE = "License"
    private const val STATE = "State"
    private const val CITY = "City"
    private const val CITY_EXTRA = "CityExtra"
    private const val LINE = "Line"
    private const val PRICE_REFERENCE = "PriceReference"
    private const val LOG = "Log"

    //query
    private const val SELECT = "select"
    private const val ID = "id"
    private const val NAME = "name"

    //advanced queries
    private const val CITY_JOIN = "id,name,county:County(*),state:State(*)"
    private const val LINE_PRICE_JOIN =
        "id,code,origin,destination,has_custom_property_name,price:Price(name,price)"
    const val TOP_CITIES_ID =
        "in.(61,71,88,114,124,173,187,231,283,297,323,366,399,606,679,719,738,761,826,836,922,923,930,1050,1060,1081,1109,1122,1137,1142,1218,1356,1378,1528,1543,1547)"
    private const val COMPACT_LINE_QUERY = "code,origin,destination"

    interface FeedbackAPI {

        @POST(FEEDBACK)
        fun createFeedback(@Body feedback: Feedback): Call<Unit>
    }

    interface LicenseAPI {

        @GET(LICENSE)
        fun getAll(): Call<List<License>>
    }

    interface StateAPI {

        @GET(STATE)
        fun getAll(@Query(value = SELECT) query: String? = null): Call<List<State>>

        @HEAD(STATE)
        fun getCount(): Call<Void>
    }

    interface CityAPI {

        @GET(CITY)
        fun getAll(@Query(SELECT) query: String? = null): Call<List<City>>

        @GET(CITY)
        fun searchCity(
            @Query(NAME) cityName: String? = null,
            @Query(ID) cityId: String? = null,
            @Query(STATE_ID) stateId: String? = null,
            @Query(SELECT) select: String = CITY_JOIN
        ): Call<List<CityJoined>>
    }

    interface LineAPI {

        @GET(LINE)
        fun getAll(@Query(SELECT) query: String? = null): Call<List<Line>>

        @GET(LINE)
        fun getCityLines(
            @Query(CITY_ID) cityId: String? = null,
            @Query(ORIGIN) origin: String? = null,
            @Query(DESTINATION) destination: String? = null,
            @Query(LINE_CODE) lineCode: String? = null,
            @Query(LIMIT) limit: String? = null,
            @Query(SELECT) select: String = LINE_PRICE_JOIN
        ): Call<List<Line>>
    }

    interface PriceReferenceAPI {

        @GET(PRICE_REFERENCE)
        fun getCityReference(@Query(CITY_ID) cityId: String): Call<List<PriceReference>>
    }

    interface CityExtraAPI {

        @GET(CITY_EXTRA)
        fun getCityExtra(@Query(CITY_ID) cityId: String): Call<List<CityExtra>>
    }

    interface CompactLineApi {

        @GET(LINE)
        fun getCityLines(
            @Query(CITY_ID) cityId: String,
            @Query(SELECT) select: String = COMPACT_LINE_QUERY
        ): Call<List<CompactLine>>
    }

    interface LogApi {

        @POST(LOG)
        fun createLog(@Body log: LogModel): Call<Unit>
    }
}