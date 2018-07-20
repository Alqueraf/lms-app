/*
 * Copyright (C) 2016 - present  Instructure, Inc.
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

package com.instructure.parentapp.binders

import android.content.Context
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.view.View

import com.instructure.canvasapi2.models.Alert
import com.instructure.canvasapi2.models.ObserverAlert
import com.instructure.canvasapi2.utils.APIHelper
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.Utils
import com.instructure.pandautils.utils.setVisible
import com.instructure.parentapp.BuildConfig
import com.instructure.parentapp.R
import com.instructure.parentapp.adapter.AlertListRecyclerAdapter
import com.instructure.parentapp.holders.AlertViewHolder
import com.instructure.parentapp.interfaces.AdapterToFragmentBadgeCallback

class AlertBinder : BaseBinder() {
    companion object {

        fun bind(
                context: Context,
                item: ObserverAlert,
                holder: AlertViewHolder,
                adapterToFragmentCallback: AdapterToFragmentBadgeCallback<ObserverAlert>,
                itemDismissedInterface: AlertListRecyclerAdapter.ItemDismissedInterface) {

            holder.title.text = item.title
            Utils.testSafeContentDescription(holder.title,
                    String.format(context.getString(R.string.alert_title_content_desc), holder.adapterPosition),
                    holder.title.text.toString(),
                    BuildConfig.IS_TESTING)

            holder.date.text = DateHelper.getMonthDayAtTime(context, APIHelper.stringToDate(item.date), R.string.at)

            holder.closeButton.setOnClickListener { itemDismissedInterface.itemDismissed(item, holder) }

            holder.itemView.setOnClickListener { adapterToFragmentCallback.onRowClicked(item, holder.adapterPosition, false) }

            when (Alert.getAlertTypeFromString(item.alertType)) {
                Alert.ALERT_TYPE.COURSE_ANNOUNCEMENT, Alert.ALERT_TYPE.INSTITUTION_ANNOUNCEMENT -> {
                    holder.icon.setImageDrawable(ColorKeeper.getColoredDrawable(context, R.drawable.vd_info_alert_icon, context.resources.getColor(R.color.colorPrimary)))
                    holder.alertType.setTextColor(context.resources.getColor(R.color.colorPrimary))
                }
                Alert.ALERT_TYPE.ASSIGNMENT_GRADE_HIGH, Alert.ALERT_TYPE.COURSE_GRADE_HIGH -> {
                    holder.icon.setImageDrawable(ColorKeeper.getColoredDrawable(context, R.drawable.vd_info_alert_icon, context.resources.getColor(R.color.alertGreen)))
                    holder.alertType.setTextColor(context.resources.getColor(R.color.alertGreen))
                }
                Alert.ALERT_TYPE.COURSE_GRADE_LOW, Alert.ALERT_TYPE.ASSIGNMENT_GRADE_LOW -> {
                    holder.icon.setImageDrawable(ColorKeeper.getColoredDrawable(context, R.drawable.vd_warning_alert_icon, context.resources.getColor(R.color.alertOrange)))
                    holder.alertType.setTextColor(context.resources.getColor(R.color.alertOrange))
                }
                Alert.ALERT_TYPE.ASSIGNMENT_MISSING -> {
                    holder.icon.setImageDrawable(ColorKeeper.getColoredDrawable(context, R.drawable.vd_warning_alert_icon, context.resources.getColor(R.color.alertRed)))
                    holder.alertType.setTextColor(context.resources.getColor(R.color.alertRed))
                }
            }
            holder.alertType.text = getAlertTypeString(context, Alert.getAlertTypeFromString(item.alertType)!!)

            holder.unreadMark.setVisible(!item.isMarkedRead())
        }

        private fun getAlertTypeString(context: Context, type: Alert.ALERT_TYPE): String {
            when (type) {
                Alert.ALERT_TYPE.ASSIGNMENT_GRADE_HIGH, Alert.ALERT_TYPE.ASSIGNMENT_GRADE_LOW -> return context.getString(R.string.assignmentGrade)
                Alert.ALERT_TYPE.COURSE_GRADE_HIGH, Alert.ALERT_TYPE.COURSE_GRADE_LOW -> return context.getString(R.string.courseGrade)
                Alert.ALERT_TYPE.ASSIGNMENT_MISSING -> return context.getString(R.string.assignmentMissing)
                Alert.ALERT_TYPE.INSTITUTION_ANNOUNCEMENT -> return context.getString(R.string.institutionAnnouncement)
                Alert.ALERT_TYPE.COURSE_ANNOUNCEMENT -> return context.getString(R.string.courseAnnouncement)
            }
        }
    }
}
