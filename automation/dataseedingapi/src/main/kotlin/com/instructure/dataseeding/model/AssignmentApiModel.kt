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



package com.instructure.dataseeding.model

import com.google.gson.annotations.SerializedName

data class CreateAssignmentWrapper(
        val assignment: CreateAssignment
)

data class CreateAssignment(
        val name: String,
        val description: String? = null,
        val published: Boolean = true,
        @SerializedName("lock_at")
        val lockAt: String? = null,
        @SerializedName("unlock_at")
        val unlockAt: String? = null,
        @SerializedName("due_at")
        val dueAt: String? = null,
        @SerializedName("submission_types")
        val submissionTypes: List<String>? = null,
        @SerializedName("group_category_id")
        val groupCategoryId: Long? = null
)

data class AssignmentApiModel (
        val id: Long,
        val name: String,
        val description: String? = null,
        val published: Boolean = true,
        @SerializedName("lock_at")
        var lockAt: String? = null,
        @SerializedName("unlock_at")
        var unlockAt: String? = null,
        @SerializedName("due_at")
        var dueAt: String? = null,
        @SerializedName("submission_comments")
        val submissionComments: List<SubmissionCommentApiModel>? = null,
        @SerializedName("group_category_id")
        val groupCategoryId: Long? = null
)

// region AssignmentOverrides

data class CreateAssignmentOverrideForStudentsWrapper(
        @SerializedName("assignment_override")
        val assignmentOverride: CreateAssignmentOverrideForStudents
)

data class CreateAssignmentOverrideForStudents(
        val title: String,
        @SerializedName("student_ids")
        val studentIds: List<Long>? = null,
        @SerializedName("group_id")
        val groupId: Long? = null,
        @SerializedName("course_section_id")
        val courseSectionId: Long? = null,
        @SerializedName("due_at")
        val dueAt: String? = null,
        @SerializedName("unlock_at")
        val unlockAt: String? = null,
        @SerializedName("lock_at")
        val lockAt: String? = null
)

data class AssignmentOverrideApiModel (
        val id: Long,
        @SerializedName("assignment_id")
        val assignmentId: Long,
        @SerializedName("student_ids")
        val studentIds: List<Long>? = null,
        @SerializedName("group_id")
        val groupId: Long? = null,
        @SerializedName("course_section_id")
        val courseSectionId: Long? = null,
        val title: String,
        @SerializedName("due_at")
        val dueAt: String? = null,
        @SerializedName("unlock_at")
        val unlockAt: String? = null,
        @SerializedName("lock_at")
        val lockAt: String? = null
)

// endregion
