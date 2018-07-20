/*
 * Copyright (C) 2016 - present Instructure, Inc.
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

package com.instructure.candroid.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import android.support.v7.widget.AppCompatEditText
import android.view.LayoutInflater
import android.widget.TextView

import com.instructure.candroid.R
import com.instructure.canvasapi2.models.Assignment
import com.instructure.pandautils.utils.*
import kotlin.properties.Delegates

class WhatIfDialogStyled : DialogFragment() {

    init {
        retainInstance = true
    }

    private var callback: (Double?, Double) -> Unit by Delegates.notNull()
    private var assignment: Assignment by ParcelableArg()
    private var courseColor: Int by IntArg()

    private var currentScoreView: AppCompatEditText? = null

    interface WhatIfDialogCallback {
        fun onClick(assignment: Assignment, position: Int)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
                .setTitle(activity.getString(R.string.whatIfDialogText))
                .setPositiveButton(R.string.done) { _, _ ->
                    try {
                        val whatIfText = currentScoreView?.text.toString()
                        callback(if(whatIfText.isBlank()) null else whatIfText.toDouble(), assignment.pointsPossible)
                    } catch (e: Throwable) {
                        callback(null, assignment.pointsPossible)
                    }
                    dismissAllowingStateLoss()
                }
                .setNegativeButton(R.string.cancel) { _, _ -> dismissAllowingStateLoss() }

        @SuppressLint("InflateParams")
        val view = LayoutInflater.from(activity).inflate(R.layout.dialog_what_if, null)
        view.findViewById<TextView>(R.id.totalScore)?.text = assignment.pointsPossible.toString()
        currentScoreView = view.findViewById(R.id.currentScore)
        builder.setView(view)

        val dialog = builder.create()
        dialog.setOnShowListener {
            if (courseColor != 0) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(courseColor)
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(courseColor)
            }
        }

        dialog.setCanceledOnTouchOutside(true)

        return dialog
    }

    override fun onDestroyView() {
        val dialog = dialog
        // handles https://code.google.com/p/android/issues/detail?id=17423
        if (dialog != null && retainInstance) dialog.setDismissMessage(null)
        super.onDestroyView()
    }

    companion object {
        fun show(fragmentManager: FragmentManager, assignment: Assignment, courseColor: Int, callback: (Double?, Double) -> Unit) {
            (fragmentManager.findFragmentByTag(WhatIfDialogStyled::class.java.simpleName) as? WhatIfDialogStyled)?.dismissAllowingStateLoss()

            WhatIfDialogStyled().apply {
                this.assignment = assignment
                this.courseColor = courseColor
                this.callback = callback
            }.show(fragmentManager, WhatIfDialogStyled::class.java.simpleName)
        }
    }
}
