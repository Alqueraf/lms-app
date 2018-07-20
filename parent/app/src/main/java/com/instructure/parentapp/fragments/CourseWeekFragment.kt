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
package com.instructure.parentapp.fragments

import android.os.Bundle
import android.view.View
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.User
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.parentapp.R
import kotlinx.android.synthetic.main.fragment_course_week.*

class CourseWeekFragment : WeekFragment() {

    private var course: Course by ParcelableArg(key = Const.COURSE)
    private var student: User by ParcelableArg(key = Const.STUDENT)

    override fun layoutResId(): Int {
        return R.layout.fragment_course_week
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbarTitle.text = course.name
        if (course.syllabusBody == null) {
            toolbarRightIcon.visibility = View.GONE
        }
        toolbarRightIcon.setOnClickListener {
            val fragment = CourseSyllabusFragment.newInstance(course, student)

            val ft = activity.supportFragmentManager.beginTransaction()
            ft.setCustomAnimations(R.anim.slide_from_bottom, android.R.anim.fade_out, R.anim.none, R.anim.slide_to_bottom)
            ft.add(R.id.fullscreen, fragment, fragment.javaClass.name)
            ft.addToBackStack(fragment.javaClass.name)
            ft.commitAllowingStateLoss()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        course = arguments.getParcelable(Const.COURSE)
        student = arguments.getParcelable(Const.STUDENT)
    }

    companion object {

        fun newInstance(user: User, course: Course): CourseWeekFragment {
            val args = Bundle()
            args.putParcelable(Const.STUDENT, user)
            args.putParcelable(Const.COURSE, course)
            val fragment = CourseWeekFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
