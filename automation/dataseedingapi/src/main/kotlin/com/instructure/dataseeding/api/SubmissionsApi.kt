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


package com.instructure.dataseeding.api

import com.instructure.dataseeding.model.AssignmentApiModel
import com.instructure.dataseeding.model.CreateSubmissionCommentWrapper
import com.instructure.dataseeding.model.SubmissionApiModel
import com.instructure.dataseeding.model.SubmitCourseAssignmentSubmissionWrapper
import com.instructure.dataseeding.util.CanvasRestAdapter
import com.instructure.dataseeding.util.Randomizer
import com.instructure.soseedy.SubmissionType
import retrofit2.Call
import retrofit2.http.*

object SubmissionsApi {
    interface SubmissionsService {

        @POST("courses/{courseId}/assignments/{assignmentId}/submissions")
        fun submitCourseAssignment(
                @Path("courseId") courseId: Long,
                @Path("assignmentId") assignmentId: Long,
                @Body submitCourseAssignmentSubmission: SubmitCourseAssignmentSubmissionWrapper): Call<SubmissionApiModel>

        @PUT("courses/{courseId}/assignments/{assignmentId}/submissions/self")
        fun commentOnSubmission(
                @Path("courseId") courseId: Long,
                @Path("assignmentId") assignmentId: Long,
                @Body createSubmissionComment: CreateSubmissionCommentWrapper
        ): Call<AssignmentApiModel>

        @GET("courses/{courseId}/assignments/{assignmentId}/submissions/{studentId}")
        fun getSubmission(
                @Path("courseId") courseId: Long,
                @Path("assignmentId") assignmentId: Long,
                @Path("studentId") studentId: Long): Call<SubmissionApiModel>
    }

    private fun submissionsService(token: String): SubmissionsService
            = CanvasRestAdapter.retrofitWithToken(token).create(SubmissionsService::class.java)

    fun submitCourseAssignment(submissionType: SubmissionType,
                               courseId: Long,
                               assignmentId: Long,
                               fileIds: MutableList<Long>,
                               studentToken: String): SubmissionApiModel {

        val submission = Randomizer.randomSubmission(submissionType, fileIds)

        return submissionsService(studentToken)
                .submitCourseAssignment(courseId, assignmentId, SubmitCourseAssignmentSubmissionWrapper(submission))
                .execute()
                .body()!!
    }

    fun commentOnSubmission(studentToken: String,
                            courseId: Long,
                            assignmentId: Long,
                            fileIds: MutableList<Long>): AssignmentApiModel {

        val comment = Randomizer.randomSubmissionComment(fileIds)

        return submissionsService(studentToken)
                .commentOnSubmission(courseId, assignmentId, CreateSubmissionCommentWrapper(comment))
                .execute()
                .body()!!
    }

    fun getSubmission(studentToken: String,
                            courseId: Long,
                            assignmentId: Long,
                            studentId: Long): SubmissionApiModel {
        return submissionsService(studentToken)
                .getSubmission(courseId, assignmentId, studentId)
                .execute()
                .body()!!
    }
}
