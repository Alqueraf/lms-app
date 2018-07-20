package com.instructure.dataseeding.soseedy

import com.instructure.dataseeding.InProcessServer
import com.instructure.dataseeding.model.EnrollmentTypes
import com.instructure.soseedy.*
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ModulesTest {
    private val course: Course = InProcessServer.courseClient.createCourse(CreateCourseRequest.newBuilder().build())
    private val teacher: CanvasUser = InProcessServer.userClient.createCanvasUser(CreateCanvasUserRequest.getDefaultInstance())

    @Before
    fun setUp() {
        InProcessServer.enrollmentClient.enrollUserInCourse(EnrollUserRequest.newBuilder()
                .setCourseId(course.id)
                .setUserId(teacher.id)
                .setEnrollmentType(EnrollmentTypes.TEACHER_ENROLLMENT)
                .build())
    }

    @Test
    fun createModule() {
        val request = CreateModuleRequest.newBuilder()
                .setCourseId(course.id)
                .setToken(teacher.token)
                .setUnlockAt("")
                .build()
        val module = InProcessServer.moduleClient.createModule(request)
        assertThat(module, instanceOf(Module::class.java))
        assertTrue(module.id >= 1)
        assertTrue(module.name.isNotEmpty())
        assertTrue(module.unlockAt.isEmpty())
    }

    @Test
    fun createModule_withUnlockAt() {
        val date = "2020-01-01"
        val request = CreateModuleRequest.newBuilder()
                .setCourseId(course.id)
                .setToken(teacher.token)
                .setUnlockAt(date)
                .build()
        val module = InProcessServer.moduleClient.createModule(request)
        assertThat(module, instanceOf(Module::class.java))
        assertTrue(module.id >= 1)
        assertTrue(module.name.isNotEmpty())
        assertTrue(module.unlockAt.startsWith(date))
    }

    @Test
    fun publishModule() {
        val request = CreateModuleRequest.newBuilder()
                .setCourseId(course.id)
                .setToken(teacher.token)
                .setUnlockAt("")
                .build()
        var module = InProcessServer.moduleClient.createModule(request)
        assertFalse(module.published)

        val updateRequest = UpdateModuleRequest.newBuilder()
                .setCourseId(course.id)
                .setId(module.id)
                .setPublished(true)
                .setToken(teacher.token)
                .build()
        module = InProcessServer.moduleClient.updateModule(updateRequest)
        assertTrue(module.published)
    }
}
