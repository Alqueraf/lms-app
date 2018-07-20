//
// Copyright (C) 2018-present Instructure, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//



package com.instructure.dataseeding.soseedy

import com.instructure.dataseeding.InProcessServer
import com.instructure.soseedy.SeedDataRequest
import com.instructure.soseedy.SeedParentDataRequest
import com.instructure.soseedy.SeededData
import com.instructure.soseedy.SeededParentData
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThat
import org.junit.Test

class GeneralTest {

    @Test
    fun seedData() {
        val teacherCount = 1
        val studentCount = 2
        val courseCount = 2
        val favoriteCourseCount = 1
        val announcementsCount = 1
        val discussionCount = 2
        val gradingPeriod = true
        val request = SeedDataRequest.newBuilder()
                .setTeachers(teacherCount)
                .setStudents(studentCount)
                .setCourses(courseCount)
                .setFavoriteCourses(favoriteCourseCount)
                .setAnnouncements(announcementsCount)
                .setDiscussions(discussionCount)
                .setGradingPeriods(gradingPeriod)
                .build()
        val response = InProcessServer.generalClient.seedData(request)
        assertThat(response, instanceOf(SeededData::class.java))
        assertEquals(courseCount, response.coursesCount)
        assertEquals(teacherCount * courseCount, response.teachersCount)
        assertEquals(studentCount * courseCount, response.studentsCount)
        assertEquals(favoriteCourseCount, response.favoritesCount)
        assertEquals((teacherCount + studentCount) * courseCount, response.enrollmentsCount)
        assertEquals(announcementsCount, response.announcementsCount)
        assertEquals(discussionCount, response.discussionsCount)
    }

    @Test
    fun seedParentData() {
        val parentCount = 1
        val courseCount = 1
        val studentCount = 3
        val enrollmentCount = (courseCount * studentCount) * 2
        val request = SeedParentDataRequest.newBuilder()
            .setParents(parentCount)
            .setCourses(courseCount)
            .setStudents(studentCount)
            .build()
        val response: SeededParentData = InProcessServer.generalClient.seedParentData(request)
        assertEquals(parentCount, response.parentsCount)
        assertEquals(courseCount, response.coursesCount)
        assertEquals(studentCount, response.studentsCount)
        assertEquals(enrollmentCount, response.enrollmentsCount)
    }

}
