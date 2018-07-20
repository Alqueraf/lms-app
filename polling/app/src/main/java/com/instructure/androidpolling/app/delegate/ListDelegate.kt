/*
 * Copyright (C) 2017 - present  Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.instructure.androidpolling.app.delegate

import android.view.View

/**
 * Generic type I is the type of the model object used to create row views in adapter
 * It must implement Comparable so the adapter can sort the rows
 */
interface ListDelegate<I> {
    fun getRowViewForItem(item: I, convertView: View?, position: Int): View
    fun getViewTypeCount(): Int
    fun getItemViewType(position: Int, item: I): Int
    fun showFirstItem(item: I)
    fun isShowFirstItem(): Boolean
}
