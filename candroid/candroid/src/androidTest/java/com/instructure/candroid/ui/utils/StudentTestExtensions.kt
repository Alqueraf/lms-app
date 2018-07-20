/*
 * Copyright (C) 2018 - present Instructure, Inc.
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
@file:Suppress("unused")

package com.instructure.candroid.ui.utils

import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.support.test.InstrumentationRegistry
import com.google.protobuf.ByteString
import com.instructure.candroid.router.RouteMatcher
import com.instructure.canvasapi2.models.User
import com.instructure.dataseeding.InProcessServer
import com.instructure.dataseeding.model.EnrollmentTypes
import com.instructure.dataseeding.util.CanvasRestAdapter
import com.instructure.dataseeding.util.Randomizer
import com.instructure.interactions.router.Route
import com.instructure.soseedy.*
import java.io.File
import java.io.FileWriter

fun StudentTest.enterDomain(enrollmentType: String = EnrollmentTypes.STUDENT_ENROLLMENT): CanvasUser {
    val user = InProcessServer.userClient.createCanvasUser(CreateCanvasUserRequest.getDefaultInstance())
    val course = InProcessServer.courseClient.createCourse(CreateCourseRequest.getDefaultInstance())
    val enrollment = InProcessServer.enrollmentClient.enrollUserInCourse(
        EnrollUserRequest.newBuilder()
            .setCourseId(course.id)
            .setUserId(user.id)
            .setEnrollmentType(enrollmentType)
            .build()
    )
    loginFindSchoolPage.enterDomain(user.domain)
    return user
}

fun StudentTest.slowLogIn(enrollmentType: String = EnrollmentTypes.STUDENT_ENROLLMENT): CanvasUser {
    loginLandingPage.clickFindMySchoolButton()
    val user = enterDomain(enrollmentType)
    loginFindSchoolPage.clickToolbarNextMenuItem()
    loginSignInPage.loginAs(user)
    return user
}

fun StudentTest.seedData(
    teachers: Int = 0,
    courses: Int = 0,
    students: Int = 0,
    favoriteCourses: Int = 0,
    announcements: Int = 0,
    discussions: Int = 0,
    gradingPeriods: Boolean = false): SeededData {

    val request = SeedDataRequest.newBuilder()
        .setTeachers(teachers)
        .setCourses(courses)
        .setStudents(students)
        .setFavoriteCourses(favoriteCourses)
        .setAnnouncements(announcements)
        .setDiscussions(discussions)
        .setGradingPeriods(gradingPeriods)
        .build()

    return mockableSeed { InProcessServer.generalClient.seedData(request) }
}

fun StudentTest.seedAssignments(
    courseId: Long,
    assignments: Int = 1,
    withDescription: Boolean = false,
    lockAt: String = "",
    unlockAt: String = "",
    dueAt: String = "",
    submissionTypes: List<SubmissionType> = emptyList(),
    teacherToken: String): Assignments {

    val request = SeedAssignmentRequest.newBuilder()
        .setCourseId(courseId)
        .setAssignments(assignments)
        .setWithDescription(withDescription)
        .setLockAt(lockAt)
        .setUnlockAt(unlockAt)
        .setDueAt(dueAt)
        .addAllSubmissionTypes(submissionTypes)
        .setTeacherToken(teacherToken)
        .build()

    return mockableSeed { InProcessServer.assignmentClient.seedAssignments(request) }
}

fun StudentTest.tokenLogin(user: CanvasUser) {
    activityRule.runOnUiThread {
        activityRule.activity.loginWithToken(
            user.token,
            user.domain,
            User().apply {
                id = user.id
                name = user.name
                shortName = user.shortName
                avatarUrl = user.avatarUrl
            }
        )
    }
    dashboardPage.assertPageObjects()
}

fun StudentTest.routeTo(route: String) {
    val url = "canvas-student://${CanvasRestAdapter.canvasDomain}/$route"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    InstrumentationRegistry.getInstrumentation().targetContext.startActivity(intent)
}

fun StudentTest.routeTo(route: Route) {
    RouteMatcher.route(InstrumentationRegistry.getInstrumentation().targetContext, route)
}

fun StudentTest.seedAssignmentSubmission(
    submissionSeeds: List<SubmissionSeed>,
    assignmentId: Long,
    courseId: Long,
    studentToken: String,
    commentSeeds: List<CommentSeed> = kotlin.collections.emptyList()
): SeededCourseAssignmentSubmissions {

    // Upload one submission file for each submission seed
    val seedsWithAttachments = submissionSeeds.map {
        SubmissionSeed.newBuilder(it)
            .addAttachments(
                when (it.submissionType) {
                    SubmissionType.ONLINE_UPLOAD -> uploadTextFile(courseId, assignmentId, studentToken,
                        FileUploadType.ASSIGNMENT_SUBMISSION
                    )
                    else -> Attachment.getDefaultInstance() // Not handled right now
                }
            )
            .build()
    }

    // Upload comment files
    val seedsWithComments = commentSeeds.map {
        val fileAttachments: MutableList<Attachment> = kotlin.collections.mutableListOf()

        for (i in 0..it.amount) {
            if (it.fileType != FileType.NONE) {
                fileAttachments.add(when (it.fileType) {
                    FileType.PDF -> kotlin.TODO()
                    FileType.TEXT -> uploadTextFile(courseId, assignmentId, studentToken,
                        FileUploadType.COMMENT_ATTACHMENT
                    )
                    else -> throw RuntimeException("Unknown file type passed into StudentTest.seedAssignmentSubmission") // Unknown type
                })
            }
        }
        CommentSeed.newBuilder()
            .addAllAttachments(fileAttachments)
            .build()
    }

    // Seed the submissions
    val submissionRequest = SeedAssignmentSubmissionRequest.newBuilder()
        .setAssignmentId(assignmentId)
        .setCourseId(courseId)
        .setStudentToken(studentToken)
        .addAllSubmissionSeeds(seedsWithAttachments)
        .addAllCommentSeeds(seedsWithComments)
        .build()

    return mockableSeed { InProcessServer.assignmentClient.seedAssignmentSubmission(submissionRequest) }
}

fun StudentTest.uploadTextFile(courseId: Long, assignmentId: Long, token: String, fileUploadType: FileUploadType): Attachment {

    // Create the file
    val file = File(
        Randomizer.randomTextFileName(Environment.getExternalStorageDirectory().absolutePath))
        .apply { createNewFile() }

    // Add contents to file
    FileWriter(file, true).apply {
        write(Randomizer.randomTextFileContents())
        flush()
        close()
    }

    // Start the Canvas file upload process
    val uploadRequest = UploadFileRequest.newBuilder()
        .setCourseId(courseId)
        .setAssignmentId(assignmentId)
        .setToken(token)
        .setFileName(file.name)
        .setFile(ByteString.copyFrom(file.readBytes()))
        .setUploadType(fileUploadType)
        .build()

    return mockableSeed { InProcessServer.fileClient.uploadFile(uploadRequest) }
}
