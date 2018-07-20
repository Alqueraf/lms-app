/*
 * Copyright (C) 2018 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
@file:Suppress("unused")

package com.instructure.teacher.ui.pages

import com.instructure.espresso.WaitForViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.assertHasText
import com.instructure.espresso.page.*
import com.instructure.soseedy.CanvasUser
import com.instructure.soseedy.Course
import com.instructure.teacher.R

class StudentContextPage : BasePage(R.id.studentContextPage) {

    private val toolbar by WaitForViewWithId(R.id.toolbar)
    private val messageButton by WaitForViewWithId(R.id.messageButton)
    private val studentName by WaitForViewWithId(R.id.studentNameView)
    private val studentEmail by WaitForViewWithId(R.id.studentEmailView)
    private val courseName by WaitForViewWithId(R.id.courseNameView)
    private val sectionName by WaitForViewWithId(R.id.sectionNameView)
    private val lastActivity by WaitForViewWithId(R.id.lastActivityView)

    fun assertDisplaysStudentInfo(student: CanvasUser) {
        waitForView(withParent(R.id.toolbar) + withText(student.name)).assertDisplayed()
        studentName.assertHasText(student.name)
        studentEmail.assertHasText(student.loginId)
    }

    fun assertDisplaysCourseInfo(course: Course) {
        courseName.assertHasText(course.name)
    }

}
