/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
package com.instructure.teacher.adapters

import android.content.Context
import android.support.annotation.ArrayRes
import android.support.annotation.LayoutRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.instructure.teacher.R
import kotlinx.android.synthetic.main.spinner_long_text.view.*

class LongNameArrayAdapter(context: Context, res: Int, textViewRes: Int, private val objects: List<CharSequence>) :
        ArrayAdapter<CharSequence>(context, res, textViewRes, objects) {

    override
    fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.spinner_long_text, parent, false)

        view.title.text = objects[position]
        return view
    }

    companion object {
        fun createFromResource(context: Context,
                               @ArrayRes textArrayResId: Int, @LayoutRes textViewResId: Int): ArrayAdapter<CharSequence> {
            val strings = context.resources.getTextArray(textArrayResId)
            return LongNameArrayAdapter(context, textViewResId, 0, strings.toMutableList())
        }
    }
}