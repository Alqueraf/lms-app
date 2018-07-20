package com.instructure.canvasapi2.pact.canvas

import au.com.dius.pact.consumer.MockServer
import au.com.dius.pact.consumer.dsl.PactDslJsonBody
import au.com.dius.pact.consumer.dsl.PactDslWithProvider
import au.com.dius.pact.model.RequestResponsePact
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.weave.awaitApi
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.experimental.runBlocking

class GetCourse : CanvasPact() {

    private val expectedCourse = Course()

    override fun createPact(builder: PactDslWithProvider): RequestResponsePact {
        expectedCourse.id = 1
        expectedCourse.name = "Some Course"
        expectedCourse.courseCode = "SC"

        val body = PactDslJsonBody()
                .integerType("id", 1)
                .stringType("name", "Some Course")
                .stringType("course_code", "SC")
                .asBody()
        return builder
                .given("a student in a course")
                .uponReceiving("a get request for the course")
                .method("GET")
                .path("/api/v1/courses/1")
                .query("include[]=term&include[]=permissions&include[]=license&include[]=is_public&include[]=needs_grading_count&include[]=course_image")
                .headers(mapOf(Pair("Authorization", "Bearer some_token")))
                .willRespondWith()
                .status(200)
                .body(body)
                .toPact()
    }

    override fun runTest(mockServer: MockServer) {
        runBlocking {
            val response = awaitApi<Course> {
                val adapter = RestBuilder(it)
                CourseAPI.getCourse(1, adapter, it, getParams(mockServer))
            }

            assertEquals(expectedCourse.id, response.id)
            assertEquals(expectedCourse.name, response.name)
            assertEquals(expectedCourse.courseCode, response.courseCode)
        }
    }
}
