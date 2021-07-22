/*
 *     UniqueItem.kt Created by Yamin Siahmargooei at 2021/7/1
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

package com.github.yamin8000.fare.ui.recyclerview.other

/*
 * lint check/compiler is complaining that
 * delegated members are hiding supertype members
 * and they should be overridden properly and explicitly
 * but to prevent boilerplate code
 * they are delegated to BaseRecyclerViewAdapter
 * so supertype members are made hidden deliberately
 */
interface UniqueItem {
    
    fun getItemCount() : Int
    
    fun getItemId(position : Int) : Long
    
    fun getItemViewType(position : Int) : Int
}