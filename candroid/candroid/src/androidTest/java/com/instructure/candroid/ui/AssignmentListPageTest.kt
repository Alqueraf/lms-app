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
package com.instructure.candroid.ui

import com.instructure.candroid.ui.utils.*
import com.instructure.espresso.ditto.Ditto
import com.instructure.soseedy.Assignment
import com.instructure.soseedy.SubmissionSeed
import com.instructure.soseedy.SubmissionType
import org.junit.Test

class AssignmentListPageTest : StudentTest() {

    @Test
    @Ditto
    override fun displaysPageObjects() {
        getToAssignmentsPage(0)
        assignmentListPage.assertPageObjects()
    }

    @Test
    @Ditto
    fun displaysNoAssignmentsView() {
        getToAssignmentsPage(0)
        assignmentListPage.assertDisplaysNoAssignmentsView()
    }

    @Test
    @Ditto
    fun displaysAssignment() {
        val assignment = getToAssignmentsPage()[0]
        assignmentListPage.assertHasAssignment(assignment)
    }

    private fun getToAssignmentsPage(assignmentCount: Int = 1): MutableList<Assignment> {
        val data = seedData(teachers = 1, students = 1, favoriteCourses = 1)
        val teacher = data.teachersList[0]
        val student = data.studentsList[0]
        val course = data.coursesList[0]
        val assignments = seedAssignments(
            courseId = course.id,
            assignments = assignmentCount,
            teacherToken = teacher.token,
            submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY)
        )

        if (assignmentCount > 0) {
            val submissions = listOf(
                SubmissionSeed.newBuilder()
                    .setSubmissionType(SubmissionType.ONLINE_TEXT_ENTRY)
                    .setAmount(1)
                    .build()
            )

            seedAssignmentSubmission(
                submissionSeeds = submissions,
                assignmentId = assignments.assignmentsList[0].id,
                courseId = course.id,
                studentToken = if (data.studentsList.isEmpty()) "" else data.studentsList[0].token
            )
        }

        tokenLogin(student)
        routeTo("courses/${course.id}/assignments")
        return assignments.assignmentsList
    }

}

