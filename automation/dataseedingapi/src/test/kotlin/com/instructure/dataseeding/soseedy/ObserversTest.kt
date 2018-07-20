package com.instructure.dataseeding.soseedy

import com.instructure.dataseeding.InProcessServer
import com.instructure.dataseeding.model.EnrollmentTypes
import com.instructure.soseedy.*
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ObserversTest {
    private val parent: CanvasUser = InProcessServer.userClient.createCanvasUser(CreateCanvasUserRequest.getDefaultInstance())
    private val student: CanvasUser = InProcessServer.userClient.createCanvasUser(CreateCanvasUserRequest.getDefaultInstance())
    private val course: Course = InProcessServer.courseClient.createCourse(CreateCourseRequest.newBuilder().build())

    @Before
    fun setUp() {
        InProcessServer.enrollmentClient.enrollUserInCourse(EnrollUserRequest.newBuilder()
                .setCourseId(course.id)
                .setUserId(student.id)
                .setEnrollmentType(EnrollmentTypes.STUDENT_ENROLLMENT)
                .build())
    }

    @Test
    fun addObserveeWithCredentials() {
        val request = AddObserveeWithCredentialsRequest.newBuilder()
                .setLoginId(student.loginId)
                .setPassword(student.password)
                .setObserveeToken(student.token)
                .setObserverToken(parent.token)
                .build()
        val observee = InProcessServer.observerClient.addObserveeWithCredentials(request)
        assertThat(observee, instanceOf(CanvasUser::class.java))
        assertEquals(student, observee)
    }

    @Test
    fun getObserverAlertThresholds_whenEmpty() {
        InProcessServer.observerClient.addObserveeWithCredentials(AddObserveeWithCredentialsRequest.newBuilder()
                .setLoginId(student.loginId)
                .setPassword(student.password)
                .setObserveeToken(student.token)
                .setObserverToken(parent.token)
                .build())
        val request = GetObserverAlertThresholdsRequest.newBuilder()
                .setToken(parent.token)
                .build()
        val thresholds = InProcessServer.observerClient.getObserverAlertThresholds(request)
        assertThat(thresholds, instanceOf(ObserverAlertThresholds::class.java))
        assertEquals(0, thresholds.thresholdsCount)
    }

    @Test
    fun addObserverAlertThreshold_assignmentGradeLow() {
        InProcessServer.observerClient.addObserveeWithCredentials(AddObserveeWithCredentialsRequest.newBuilder()
                .setLoginId(student.loginId)
                .setPassword(student.password)
                .setObserveeToken(student.token)
                .setObserverToken(parent.token)
                .build())
        val alertType = "assignment_grade_low"
        val request = AddObserverAlertThresholdRequest.newBuilder()
                .setAlertType(alertType)
                .setUserId(student.id)
                .setObserverId(parent.id)
                .setToken(parent.token)
                .build()
        val threshold = InProcessServer.observerClient.addObserverAlertThreshold(request)
        assertThat(threshold, instanceOf(ObserverAlertThreshold::class.java))
        assertEquals(alertType, threshold.alertType)
        assertTrue(threshold.threshold.toDouble() >= 0)
        assertTrue(threshold.threshold.toDouble() <= 100)
        assertEquals("active", threshold.workflowState)
        assertEquals(student.id, threshold.userId)
        assertEquals(parent.id, threshold.observerId)
    }

    fun addObserverAlertThreshold_assignmentGradeHigh() {
        InProcessServer.observerClient.addObserveeWithCredentials(AddObserveeWithCredentialsRequest.newBuilder()
                .setLoginId(student.loginId)
                .setPassword(student.password)
                .setObserveeToken(student.token)
                .setObserverToken(parent.token)
                .build())
        val alertType = "assignment_grade_high"
        val request = AddObserverAlertThresholdRequest.newBuilder()
                .setAlertType(alertType)
                .setUserId(student.id)
                .setObserverId(parent.id)
                .setToken(parent.token)
                .build()
        val threshold = InProcessServer.observerClient.addObserverAlertThreshold(request)
        assertThat(threshold, instanceOf(ObserverAlertThreshold::class.java))
        assertEquals(alertType, threshold.alertType)
        assertTrue(threshold.threshold.toDouble() >= 0)
        assertTrue(threshold.threshold.toDouble() <= 100)
        assertEquals("active", threshold.workflowState)
        assertEquals(student.id, threshold.userId)
        assertEquals(parent.id, threshold.observerId)
    }

    @Test
    fun addObserverAlertThreshold_assignmentMissing() {
        InProcessServer.observerClient.addObserveeWithCredentials(AddObserveeWithCredentialsRequest.newBuilder()
                .setLoginId(student.loginId)
                .setPassword(student.password)
                .setObserveeToken(student.token)
                .setObserverToken(parent.token)
                .build())
        val alertType = "assignment_missing"
        val request = AddObserverAlertThresholdRequest.newBuilder()
                .setAlertType(alertType)
                .setUserId(student.id)
                .setObserverId(parent.id)
                .setToken(parent.token)
                .build()
        val threshold = InProcessServer.observerClient.addObserverAlertThreshold(request)
        assertThat(threshold, instanceOf(ObserverAlertThreshold::class.java))
        assertEquals(alertType, threshold.alertType)
        assertEquals("", threshold.threshold)
        assertEquals("active", threshold.workflowState)
        assertEquals(student.id, threshold.userId)
        assertEquals(parent.id, threshold.observerId)
    }

    @Test
    fun addObserverAlertThreshold_courseGradeLow() {
        InProcessServer.observerClient.addObserveeWithCredentials(AddObserveeWithCredentialsRequest.newBuilder()
                .setLoginId(student.loginId)
                .setPassword(student.password)
                .setObserveeToken(student.token)
                .setObserverToken(parent.token)
                .build())
        val alertType = "course_grade_low"
        val request = AddObserverAlertThresholdRequest.newBuilder()
                .setAlertType(alertType)
                .setUserId(student.id)
                .setObserverId(parent.id)
                .setToken(parent.token)
                .build()
        val threshold = InProcessServer.observerClient.addObserverAlertThreshold(request)
        assertThat(threshold, instanceOf(ObserverAlertThreshold::class.java))
        assertEquals(alertType, threshold.alertType)
        assertTrue(threshold.threshold.toDouble() >= 0)
        assertTrue(threshold.threshold.toDouble() <= 100)
        assertEquals("active", threshold.workflowState)
        assertEquals(student.id, threshold.userId)
        assertEquals(parent.id, threshold.observerId)
    }

    @Test
    fun addObserverAlertThreshold_courseGradeHigh() {
        InProcessServer.observerClient.addObserveeWithCredentials(AddObserveeWithCredentialsRequest.newBuilder()
                .setLoginId(student.loginId)
                .setPassword(student.password)
                .setObserveeToken(student.token)
                .setObserverToken(parent.token)
                .build())
        val alertType = "course_grade_high"
        val request = AddObserverAlertThresholdRequest.newBuilder()
                .setAlertType(alertType)
                .setUserId(student.id)
                .setObserverId(parent.id)
                .setToken(parent.token)
                .build()
        val threshold = InProcessServer.observerClient.addObserverAlertThreshold(request)
        assertThat(threshold, instanceOf(ObserverAlertThreshold::class.java))
        assertEquals(alertType, threshold.alertType)
        assertTrue(threshold.threshold.toDouble() >= 0)
        assertTrue(threshold.threshold.toDouble() <= 100)
        assertEquals("active", threshold.workflowState)
        assertEquals(student.id, threshold.userId)
        assertEquals(parent.id, threshold.observerId)
    }

    @Test
    fun addObserverAlertThreshold_courseAnnouncement() {
        InProcessServer.observerClient.addObserveeWithCredentials(AddObserveeWithCredentialsRequest.newBuilder()
                .setLoginId(student.loginId)
                .setPassword(student.password)
                .setObserveeToken(student.token)
                .setObserverToken(parent.token)
                .build())
        val alertType = "course_announcement"
        val request = AddObserverAlertThresholdRequest.newBuilder()
                .setAlertType(alertType)
                .setUserId(student.id)
                .setObserverId(parent.id)
                .setToken(parent.token)
                .build()
        val threshold = InProcessServer.observerClient.addObserverAlertThreshold(request)
        assertThat(threshold, instanceOf(ObserverAlertThreshold::class.java))
        assertEquals(alertType, threshold.alertType)
        assertEquals("", threshold.threshold)
        assertEquals("active", threshold.workflowState)
        assertEquals(student.id, threshold.userId)
        assertEquals(parent.id, threshold.observerId)
    }

    @Test
    fun addObserverAlertThreshold_institutionAnnouncement() {
        InProcessServer.observerClient.addObserveeWithCredentials(AddObserveeWithCredentialsRequest.newBuilder()
                .setLoginId(student.loginId)
                .setPassword(student.password)
                .setObserveeToken(student.token)
                .setObserverToken(parent.token)
                .build())
        val alertType = "institution_announcement"
        val request = AddObserverAlertThresholdRequest.newBuilder()
                .setAlertType(alertType)
                .setUserId(student.id)
                .setObserverId(parent.id)
                .setToken(parent.token)
                .build()
        val threshold = InProcessServer.observerClient.addObserverAlertThreshold(request)
        assertThat(threshold, instanceOf(ObserverAlertThreshold::class.java))
        assertEquals(alertType, threshold.alertType)
        assertEquals("", threshold.threshold)
        assertEquals("active", threshold.workflowState)
        assertEquals(student.id, threshold.userId)
        assertEquals(parent.id, threshold.observerId)
    }

    @Test
    fun getObserverAlerts_courseAnnouncement() {
        InProcessServer.observerClient.addObserveeWithCredentials(AddObserveeWithCredentialsRequest.newBuilder()
                .setLoginId(student.loginId)
                .setPassword(student.password)
                .setObserveeToken(student.token)
                .setObserverToken(parent.token)
                .build())
        val alertType = "course_announcement"
        val threshold = InProcessServer.observerClient.addObserverAlertThreshold(AddObserverAlertThresholdRequest.newBuilder()
                .setAlertType(alertType)
                .setUserId(student.id)
                .setObserverId(parent.id)
                .setToken(parent.token)
                .build())
        val teacher = InProcessServer.userClient.createCanvasUser(CreateCanvasUserRequest.getDefaultInstance())
        InProcessServer.enrollmentClient.enrollUserInCourse(EnrollUserRequest.newBuilder()
                .setCourseId(course.id)
                .setUserId(teacher.id)
                .setEnrollmentType(EnrollmentTypes.TEACHER_ENROLLMENT)
                .build())
        val announcement = InProcessServer.discussionClient.createAnnouncement(CreateAnnouncementRequest.newBuilder()
                .setCourseId(course.id)
                .setToken(teacher.token)
                .build())
        val request = GetObserverAlertsRequest.newBuilder()
                .setUserId(student.id)
                .setToken(parent.token)
                .build()
        val alerts = InProcessServer.observerClient.getObserverAlerts(request)
        assertThat(alerts, instanceOf(ObserverAlerts::class.java))
        assertEquals(1, alerts.alertsCount)
        val alert = alerts.getAlerts(0)
        assertThat(alert, instanceOf(ObserverAlert::class.java))
        assertEquals(threshold.id, alert.observerAlertThresholdId)
        assertEquals("DiscussionTopic", alert.contextType)
        assertEquals(announcement.id, alert.contextId)
        assertEquals(alertType, alert.alertType)
        assertEquals("unread", alert.workflowState)
        assertEquals("Announcement posted: ${announcement.title}", alert.title)
        assertEquals(student.id, alert.userId)
        assertEquals(parent.id, alert.observerId)
        assertTrue(alert.htmlUrl.endsWith("/courses/${course.id}/discussion_topics/${announcement.id}"))
    }
}
