/*
 * Copyright (C) 2018 - present Instructure, Inc.
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
package com.instructure.candroid.holders

import android.content.Context
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import com.instructure.candroid.R
import com.instructure.candroid.interfaces.AdapterToFragmentCallback
import com.instructure.canvasapi2.models.CanvasComparable
import com.instructure.canvasapi2.models.Course
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.onClick
import kotlinx.android.synthetic.main.viewholder_edit_favorites.view.*

class EditFavoritesCourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    companion object {
        fun holderResId() = R.layout.viewholder_edit_favorites
    }

    fun bind(context: Context, course: Course, callback: AdapterToFragmentCallback<CanvasComparable<*>>) = with(itemView) {
        val isFavorite = course.isFavorite
        val name = course.name
        val contentDescResId = if (isFavorite) R.string.courseFavorited else R.string.courseNotFavorited
        title.text = name
        star.setImageResource(if (isFavorite) R.drawable.vd_star_filled else R.drawable.vd_star)
        title.contentDescription = context.getString(contentDescResId, name)
        DrawableCompat.setTint(DrawableCompat.wrap(star.drawable), ThemePrefs.brandColor)
        onClick { callback.onRowClicked(course, adapterPosition, !isFavorite) }
    }
}