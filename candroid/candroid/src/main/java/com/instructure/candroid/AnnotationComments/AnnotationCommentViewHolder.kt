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
package com.instructure.candroid.AnnotationComments

import android.support.v7.widget.RecyclerView
import android.view.View
import com.instructure.candroid.R
import com.instructure.canvasapi2.models.CanvaDocs.CanvaDocAnnotation
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.pandautils.utils.onClick
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import kotlinx.android.synthetic.main.viewholder_annotation_comment.view.*
import java.util.*

class AnnotationCommentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    companion object {
        val holderRes = R.layout.viewholder_annotation_comment
    }

    fun bind(annotation: CanvaDocAnnotation, canEdit: Boolean, canDelete: Boolean, editCallback: (CanvaDocAnnotation, Int) -> Unit, deleteCallback: (CanvaDocAnnotation, Int) -> Unit) = with(itemView) {
        commentAuthorTextView.text = annotation.userName
        commentDateTextView.text = DateHelper.getMonthDayAtTime(context, DateHelper.stringToDateWithMillis(annotation.createdAt), context.getString(R.string.at))
        commentContentsTextView.text = annotation.contents

        commentEditIcon.setVisible((canEdit || canDelete) && !annotation.deleted)

        if(annotation.deleted) {
            commentRemovedLabel.setVisible()
            val date = DateHelper.getMonthDayAtTime(context, DateHelper.stringToDateWithMillis(annotation.deletedAt), context.getString(R.string.at))
            commentRemovedLabel.text = resources.getString(R.string.removedComment, date, annotation.deletedBy)
        } else {
            commentRemovedLabel.setGone()
        }

        commentEditIcon.onClick {
            val popup = android.support.v7.widget.PopupMenu(context, it, android.view.Gravity.TOP, 0,
                    android.support.v7.appcompat.R.style.Base_Widget_AppCompat_PopupMenu_Overflow)
            popup.inflate(R.menu.menu_edit_annotation_comment)
            if(!canEdit) popup.menu.removeItem(R.id.edit)
            if(!canDelete) popup.menu.removeItem(R.id.delete)
            popup.setOnMenuItemClickListener {
                when(it.itemId) {
                    R.id.edit -> {
                        editCallback(annotation, adapterPosition)
                    }
                    R.id.delete -> { deleteCallback(annotation, adapterPosition) }
                }
                true
            }
            popup.show()
        }
    }
}