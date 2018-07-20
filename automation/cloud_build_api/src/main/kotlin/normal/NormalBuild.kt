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



package normal

import api.bitrise.BitriseBuildObject
import api.bitrise.BitriseStatus
import api.buddybuild.BuddybuildBuildObject
import api.buddybuild.BuddybuildStatus
import util.sqlDate
import util.yearWeek
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

data class NormalBuild(
        val buildId: String,
        val buildDate: String,
        val buildYearweek: String,
        val buildQueued: Long, // seconds
        val buildDuration: Long,
        val buildSuccessful: Boolean
)

/*
    returns null when the build status isn't success or failure.

    finished_at - started_on_worker_at is the full build time which includes preparing the build environment.
    Bitrise web UI excludes environment prep time since it's not counted against our build limits
    UI calculates duration as follows: finished_at - environment_prepare_finished_at

    Actual build time is finished_at - started_on_worker_at

    Bitrise:

    "slug": "f54280687495a3da",
    "triggered_at": "2017-10-18T19:53:58Z",
    "started_on_worker_at": "2017-10-18T19:53:58Z",
    "environment_prepare_finished_at": "2017-10-18T19:55:22Z",
    "finished_at": "2017-10-18T20:10:10Z",
    "status_text": "success",
 */
fun BitriseBuildObject.toNormalBuild(): NormalBuild? {
    if (!BitriseStatus.successfulOrFailed(this)) {
        return null
    }

    val creationTime = ZonedDateTime.parse(triggered_at)
    val startTime = ZonedDateTime.parse(started_on_worker_at)
    val endTime = ZonedDateTime.parse(finished_at)
    val buildQueued = ChronoUnit.SECONDS.between(creationTime, startTime)
    val buildDuration = ChronoUnit.SECONDS.between(startTime, endTime)

    val buildId = slug
    val buildDate = ZonedDateTime.parse(triggered_at).toLocalDate()
    val buildYearweek = buildDate.yearWeek()
    val buildSuccessful = status_text == BitriseStatus.success

    return NormalBuild(
            buildId,
            buildDate.sqlDate(),
            buildYearweek,
            buildQueued,
            buildDuration,
            buildSuccessful)
}

/*
    Buddybuild:
    "_id": "59e7d4e18c070900019a50f8",
    "created_at": "2017-10-18T22:25:33.047Z",
    "started_at": "2017-10-18T22:25:55.221Z",
    "finished_at": "2017-10-18T22:34:51.993Z",
    "build_status": "success",

    track only successful & failed builds
*/
fun BuddybuildBuildObject.toNormalBuild(): NormalBuild? {
    if (!BuddybuildStatus.successfulOrFailed(this)) {
        return null
    }

    val creationTime = ZonedDateTime.parse(created_at)
    val startTime = ZonedDateTime.parse(started_at)
    val endTime = ZonedDateTime.parse(finished_at)
    val buildQueued = ChronoUnit.SECONDS.between(creationTime, startTime)
    val buildDuration = ChronoUnit.SECONDS.between(startTime, endTime)

    val buildId = _id
    val buildDate = ZonedDateTime.parse(created_at).toLocalDate()
    val buildYearweek = buildDate.yearWeek()
    val buildSuccessful = build_status == BuddybuildStatus.success

    return NormalBuild(
            buildId,
            buildDate.sqlDate(),
            buildYearweek,
            buildQueued,
            buildDuration,
            buildSuccessful)
}
