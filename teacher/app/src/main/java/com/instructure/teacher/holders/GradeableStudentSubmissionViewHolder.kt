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
package com.instructure.teacher.holders

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.View
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.GradeableStudentSubmission
import com.instructure.canvasapi2.models.GroupAssignee
import com.instructure.canvasapi2.models.StudentAssignee
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.pandautils.utils.*
import com.instructure.teacher.R
import com.instructure.teacher.adapters.StudentContextFragment
import com.instructure.interactions.router.Route
import com.instructure.teacher.router.RouteMatcher
import com.instructure.pandautils.utils.ProfileUtils
import com.instructure.teacher.utils.getResForSubmission
import com.instructure.teacher.utils.iconRes
import com.instructure.teacher.utils.setAnonymousAvatar
import kotlinx.android.synthetic.main.adapter_gradeable_student_submission.view.*
import java.util.*

class GradeableStudentSubmissionViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    companion object {
        val holderResId = R.layout.adapter_gradeable_student_submission
    }

    fun bind(
            context: Context,
            gradeableStudentSubmission: GradeableStudentSubmission,
            assignment: Assignment,
            courseId: Long,
            gradeAnonymously: Boolean,
            callback: (GradeableStudentSubmission) -> Unit
    ) = with(itemView) {

        val assignee = gradeableStudentSubmission.assignee
        when {
            gradeAnonymously -> {
                studentAvatar.setAnonymousAvatar()
                studentName.text = context.getString(R.string.anonymousStudentLabel)
            }
            assignee is StudentAssignee -> {
                ProfileUtils.loadAvatarForUser(studentAvatar, assignee.student.name, assignee.student.avatarUrl)
                studentName.text = assignee.student.name
                studentAvatar.setupAvatarA11y(assignee.name)
                studentAvatar.onClick {
                    val bundle = StudentContextFragment.makeBundle(assignee.id, courseId)
                    RouteMatcher.route(context, Route(StudentContextFragment::class.java, null, bundle))
                }
            }
            assignee is GroupAssignee -> {
                studentAvatar.setImageResource(assignee.iconRes)
                studentName.text = assignee.group.name
            }
        }

        val submission = gradeableStudentSubmission.submission

        //graded text
        if (submission == null || submission.workflowState == "unsubmitted") {
            submissionGrade.text = ""
            submissionGrade.background = null
        } else {
            if (submission.isExcused) {
                submissionGrade.text = context.getString(R.string.submission_status_excused)
                submissionGrade.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14.0f)
                submissionGrade.background = null
                submissionGrade.setTextColor(ContextCompat.getColor(context, R.color.defaultTextDark))
            } else if (submission.isGraded) {
                //This is not ideal... the api returns us the string with lower case first letter.
                //Hopefully the fact that we localize our strings will make this consistent...
                when(submission.grade) {
                    "complete" -> {
                        submissionGrade.text = context.getString(R.string.complete_grade)
                    }
                    "incomplete" -> {
                        submissionGrade.text = context.getString(R.string.incomplete_grade)
                    }
                    else -> {
                        try {
                            if (assignment.gradingType == Assignment.PERCENT_TYPE) {
                                val value: Double = submission.grade?.removeSuffix("%")?.toDouble() as Double
                                submissionGrade.text = NumberHelper.doubleToPercentage(value, 2)
                            } else {
                                submissionGrade.text = NumberHelper.formatDecimal(submission.grade?.toDouble() as Double, 2, true)
                            }
                        } catch(e: NumberFormatException) {
                            // Grade is a letter or text grade
                            submissionGrade.text = submission.grade
                        }
                    }
                }
                submissionGrade.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14.0f)
                submissionGrade.background = null
                submissionGrade.setTextColor(ContextCompat.getColor(context, R.color.defaultTextDark))
            } else {
                submissionGrade.text = ViewStyler.applyKerning(context.getString(R.string.needsGrading).toUpperCase(Locale.getDefault()), .5f)
                submissionGrade.contentDescription = context.getString(R.string.needsGrading)
                submissionGrade.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.0f)
                val submissionGradeDrawable = ContextCompat.getDrawable(context, R.drawable.bg_generic_pill)
                val strokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1f, context.resources.displayMetrics)
                (submissionGradeDrawable as GradientDrawable).setStroke(strokeWidth.toInt(), ThemePrefs.brandColor)
                submissionGrade.background = submissionGradeDrawable
                submissionGrade.setTextColor(ThemePrefs.brandColor)
            }
        }

        // Set submission status

        if (assignment.turnInType != Assignment.TURN_IN_TYPE.ON_PAPER && assignment.turnInType != Assignment.TURN_IN_TYPE.NONE) {
            submissionStatus.setVisible()
        } else {
            submissionStatus.setGone()
        }

        val (stringRes, colorRes) = assignment.getResForSubmission(gradeableStudentSubmission.submission)
        if (stringRes == -1 || colorRes == -1) {
            submissionStatus.setGone()
        } else {
            submissionStatus.setText(stringRes)
            submissionStatus.setTextColor(ContextCompat.getColor(context, colorRes))
        }

        itemView.setOnClickListener { callback(gradeableStudentSubmission) }
    }
}
