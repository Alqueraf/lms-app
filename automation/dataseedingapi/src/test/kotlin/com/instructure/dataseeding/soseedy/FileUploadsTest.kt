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

import com.google.protobuf.ByteString
import com.instructure.dataseeding.InProcessServer
import com.instructure.dataseeding.model.EnrollmentTypes
import com.instructure.dataseeding.util.Randomizer
import com.instructure.soseedy.*
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test
import java.io.File
import java.io.FileWriter

@Suppress("TestFunctionName")
class FileUploadsTest {
    private val files = InProcessServer.fileClient
    private val course = InProcessServer.courseClient.createCourse(CreateCourseRequest.newBuilder().build())
    private val student = InProcessServer.userClient.createCanvasUser(CreateCanvasUserRequest.getDefaultInstance())
    private val teacher = InProcessServer.userClient.createCanvasUser(CreateCanvasUserRequest.getDefaultInstance())

    private fun createAssignment(): Assignment {
        InProcessServer.enrollmentClient.enrollUserInCourse(EnrollUserRequest.newBuilder()
                .setCourseId(course.id)
                .setUserId(teacher.id)
                .setEnrollmentType(EnrollmentTypes.TEACHER_ENROLLMENT)
                .build())
        InProcessServer.enrollmentClient.enrollUserInCourse(EnrollUserRequest.newBuilder()
                .setCourseId(course.id)
                .setUserId(student.id)
                .setEnrollmentType(EnrollmentTypes.STUDENT_ENROLLMENT)
                .build())

        val request = CreateAssignmentRequest.newBuilder()
                .setCourseId(course.id)
                .setWithDescription(false)
                .setLockAt("")
                .setUnlockAt("")
                .setDueAt("")
                .addAllSubmissionTypes(listOf(SubmissionType.ONLINE_UPLOAD))
                .setTeacherToken(teacher.token)
                .build()
        return InProcessServer.assignmentClient.createAssignment(request)
    }

    private fun randomFile(): File {
        val tmpDir = System.getProperty("java.io.tmpdir")

        val file = File(
                Randomizer.randomTextFileName(tmpDir))
                .apply { createNewFile() }

        FileWriter(file, true).apply {
            write(Randomizer.randomTextFileContents())
            flush()
            close()
        }

        return file
    }

    @Test
    fun UploadFile() {
        val assignment = createAssignment()
        val file = randomFile()

        val uploadRequest = UploadFileRequest.newBuilder()
                .setCourseId(course.id)
                .setAssignmentId(assignment.id)
                .setToken(student.token)
                .setFileName(file.name)
                .setFile(ByteString.copyFrom(file.readBytes()))
                .setUploadType(FileUploadType.ASSIGNMENT_SUBMISSION)
                .build()
        val response = files.uploadFile(uploadRequest)
        assertThat(response.displayName, `is`(file.name))
        assertThat(response.fileName, `is`(file.name))
    }
}
