/*
 *     Fare: find Iran's cities taxi fares
 *     CachePolicy.kt Created by Yamin Siahmargooei at 2021/7/31
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

@file:Suppress("unused")

package com.github.yamin8000.fare2.cache

import java.time.LocalDateTime

typealias Policy = (LocalDateTime, LocalDateTime) -> Boolean

object CachePolicy {

    val DailyCache: Policy = { current, last -> current.minusDays(1).isAfter(last) }
    val WeeklyCache: Policy = { current, last -> current.minusDays(7).isAfter(last) }
    val MonthlyCache: Policy = { current, last -> current.minusMonths(1).isAfter(last) }
}