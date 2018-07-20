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



package api.buddybuild

import normal.toNormalBuild
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito
import util.dateToZonedTime

class BuddybuildAppsTest {

    @Test
    fun getApps() {
        val result = BuddybuildApps.getApps()
        assertThat(result.size, Matchers.greaterThan(1))
    }

    @Test
    fun getBuilds() {
        val mockApp = Mockito.mock(BuddybuildAppObject::class.java)
        Mockito.`when`(mockApp._id).thenReturn("58b0b2116096900100863eb8")

        val result = BuddybuildApps.getBuilds(mockApp, dateToZonedTime("2017-10-30"))
        assertThat(result.size, Matchers.greaterThan(10))
    }

    @Test
    fun getTestResults() {
        val buildId = "5a5f987b0c043b00017c5eaa"
        val results = BuddybuildApps.getTestResults(buildId)
        assertEquals(buildId, results.build_id)
        assertTrue(results.tests.isNotEmpty())
        results.tests.forEach {
            assertTrue(it.run.isNotEmpty())
            assertTrue(it.target.isNotEmpty())
            assertTrue(it.suite.isNotEmpty())
            assertTrue(it.test.isNotEmpty())
            assertTrue(it.status.isNotEmpty())
        }
    }

    @Test
    fun buddybuildBuildObjectToNormalBuild() {
        val created_at = "2017-10-31T21:26:04.323Z"
        val started_at = "2017-10-31T21:30:16.872Z"
        val finished_at = "2017-10-31T21:43:44.615Z"
        val _id = "59f8ea6fffd03c00010d9699"
        val build_status = "success"

        val mockData = Mockito.mock(BuddybuildBuildObject::class.java)
        Mockito.`when`(mockData.created_at).thenReturn(created_at)
        Mockito.`when`(mockData.started_at).thenReturn(started_at)
        Mockito.`when`(mockData.finished_at).thenReturn(finished_at)
        Mockito.`when`(mockData._id).thenReturn(_id)
        Mockito.`when`(mockData.build_status).thenReturn(build_status)

        val normalBuild = mockData.toNormalBuild()!!
        assertThat(normalBuild.buildId, Matchers.`is`(_id))
        assertThat(normalBuild.buildDate, Matchers.`is`("2017-10-31"))
        assertThat(normalBuild.buildYearweek, Matchers.`is`("2017 44"))
        assertThat(normalBuild.buildQueued, Matchers.`is`(252L))
        assertThat(normalBuild.buildDuration, Matchers.`is`(807L))
        assertThat(normalBuild.buildSuccessful, Matchers.`is`(true))
    }
}
