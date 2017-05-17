/*
 * Copyright (C) 2016 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.instructure.canvasapi2.tests;

import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.models.RubricCriterionAssessment;
import com.instructure.canvasapi2.models.Submission;
import com.instructure.canvasapi2.utils.ApiType;
import com.instructure.canvasapi2.utils.LinkHeaders;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;


public class SubmissionManager_Test {
    public static void getStudentSubmissionsForCourse(long studentId, long courseId, StatusCallback<List<Submission>> callback) {
        //TODO:
        Response response = new Response.Builder()
                .request(new Request.Builder().url("https://test.com").build())
                .code(200)
                .message("todo")
                .protocol(Protocol.HTTP_1_0)
                .body(ResponseBody.create(MediaType.parse("application/json"), "todo".getBytes()))
                .addHeader("content-type", "application/json")
                .build();

        List<Submission> submissions = new ArrayList<>();

        retrofit2.Response<List<Submission>> response1 = retrofit2.Response.success(submissions, response);
        callback.onResponse(response1, new LinkHeaders(), ApiType.CACHE);
    }

    public static void updateRubricAssessment(long courseId,
                                              long assignmentId,
                                              long studentId,
                                              Map<String, RubricCriterionAssessment> assessmentMap,
                                              StatusCallback<Submission> callback) {
        //TODO:
        Response response = new Response.Builder()
                .request(new Request.Builder().url("https://test.com").build())
                .code(200)
                .message("todo")
                .protocol(Protocol.HTTP_1_0)
                .body(ResponseBody.create(MediaType.parse("application/json"), "todo".getBytes()))
                .addHeader("content-type", "application/json")
                .build();

        Submission submission = new Submission();

        retrofit2.Response<Submission> response1 = retrofit2.Response.success(submission, response);
        callback.onResponse(response1, new LinkHeaders(), ApiType.CACHE);
    }

    public static void postSubmissionGrade(long courseId, long assignmentId, long userId, String score, StatusCallback<ResponseBody> callback) {
        //TODO:
        Response response = new Response.Builder()
                .request(new Request.Builder().url("https://test.com").build())
                .code(200)
                .message("todo")
                .protocol(Protocol.HTTP_1_0)
                .body(ResponseBody.create(MediaType.parse("application/json"), "todo".getBytes()))
                .addHeader("content-type", "application/json")
                .build();
    }
}
