/*
 * Copyright (C) 2017 - present Instructure, Inc.
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

package com.instructure.canvasapi2.apis;

import android.support.annotation.NonNull;

import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.builders.RestBuilder;
import com.instructure.canvasapi2.builders.RestParams;
import com.instructure.canvasapi2.models.AlertThreshold;
import com.instructure.canvasapi2.models.ObserverAlertThreshold;
import com.instructure.canvasapi2.models.post_models.ObserverAlertThresholdPostBody;
import com.instructure.canvasapi2.models.post_models.ObserverAlertThresholdPostBodyWrapper;
import com.instructure.canvasapi2.utils.APIHelper;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;


public class AlertThresholdAPI {

    interface AlertThresholdInterface {

        @GET("alertthreshold/student/{parentId}/{studentId}")
        Call<List<AlertThreshold>> getAlertThresholdsForStudent(
                @Path("parentId") String parentId,
                @Path("studentId") String studentId);

        @GET("alertthreshold/{parentId}/{thresholdId}")
        Call<AlertThreshold> getAlertThresholdById(
                @Path("parentId") String parentId,
                @Path("thresholdId") long thresholdId);

        @FormUrlEncoded
        @PUT("alertthreshold/{parentId}")
        Call<AlertThreshold> createAlertThreshold(
                @Path("parentId") String parentId,
                @Field("student_id") String studentId,
                @Field("alert_type") String alertType,
                @Field("threshold") String threshold);

        @FormUrlEncoded
        @POST("alertthreshold/{parentId}/{thresholdId}")
        Call<AlertThreshold> updateAlertThreshold(
                @Path("parentId") String parentIdPath,
                @Path("thresholdId") String thresholdIdPath,
                @Field("observer_id") String parentId,
                @Field("threshold_id") String thresholdId,
                @Field("alert_type") String alertType,
                @Field("threshold") String threshold);

        //threshold field is optional
        @FormUrlEncoded
        @PUT("alertthreshold/{parentId}")
        Call<AlertThreshold> createAlertThreshold(
                @Path("parentId") String parentId,
                @Field("student_id") String studentId,
                @Field("alert_type") String alertType);

        //threshold field is optional
        @FormUrlEncoded
        @POST("alertthreshold/{parentId}/{thresholdId}")
        Call<AlertThreshold> updateAlertThreshold(
                @Path("parentId") String parentIdPath,
                @Path("thresholdId") String thresholdIdPath,
                @Field("observer_id") String parentId,
                @Field("threshold_id") String thresholdId,
                @Field("alert_type") String alertType);


        @DELETE("alertthreshold/{parentId}/{thresholdId}")
        Call<ResponseBody> deleteAlertThreshold(
                @Path("parentId") String parentId,
                @Path("thresholdId") String thresholdId);

        @GET("users/self/observer_alert_thresholds")
        Call<List<ObserverAlertThreshold>> getObserverAlertThresholds(
                @Query("student_id") long studentId);

        @GET("users/self/observer_alert_thresholds/{observerAlertThresholdId}")
        Call<ObserverAlertThreshold> getObserverAlertThresholdById(
                @Path("observerAlertThresholdId") long thresholdId);

        @POST("users/self/observer_alert_thresholds")
        Call<ObserverAlertThreshold> createObserverAlertThreshold(
                @Body ObserverAlertThresholdPostBodyWrapper body);

        @FormUrlEncoded
        @PUT("users/self/observer_alert_thresholds/{observerAlertThresholdId}")
        Call<ObserverAlertThreshold> updateObserverAlertThreshold(
                @Path("observerAlertThresholdId") String thresholdIdPath,
                @Field("threshold") String threshold);

        //threshold field is optional
        @FormUrlEncoded
        @PUT("users/self/observer_alert_thresholds")
        Call<ObserverAlertThreshold> createObserverAlertThreshold(
                @Field("student_id") long studentId,
                @Field("alert_type") String alertType);


        @DELETE("users/self/observer_alert_thresholds/{observerAlertThresholdId}")
        Call<ResponseBody> deleteObserverAlertThreshold(
                @Path("observerAlertThresholdId") String thresholdId);

    }

    public static void getAlertThresholdsForStudent(
            @NonNull String airwolfDomain,
            @NonNull RestBuilder adapter,
            @NonNull RestParams params,
            @NonNull String parentId,
            @NonNull String studentId,
            @NonNull StatusCallback<List<AlertThreshold>> callback){

        callback.addCall(adapter.build(AlertThresholdInterface.class, APIHelper.paramsWithDomain(airwolfDomain, params)).getAlertThresholdsForStudent(parentId, studentId)).enqueue(callback);
    }

    public static void getAlertThresholdById(
            @NonNull String airwolfDomain,
            @NonNull RestBuilder adapter,
            @NonNull RestParams params,
            @NonNull String parentId,
            long thresholdId,
            @NonNull StatusCallback<AlertThreshold> callback){

        callback.addCall(adapter.build(AlertThresholdInterface.class, APIHelper.paramsWithDomain(airwolfDomain, params)).getAlertThresholdById(parentId, thresholdId)).enqueue(callback);
    }

    public static void createAlertThreshold(
            @NonNull String airwolfDomain,
            @NonNull RestBuilder adapter,
            @NonNull RestParams params,
            @NonNull String parentId,
            @NonNull String studentId,
            @NonNull String alertType,
            @NonNull String threshold,
            @NonNull StatusCallback<AlertThreshold> callback){

        callback.addCall(adapter.build(AlertThresholdInterface.class, APIHelper.paramsWithDomain(airwolfDomain, params)).createAlertThreshold(parentId, studentId, alertType, threshold)).enqueue(callback);
    }

    public static void updateAlertThreshold(
            @NonNull String airwolfDomain,
            @NonNull RestBuilder adapter,
            @NonNull RestParams params,
            @NonNull String parentId,
            @NonNull String thresholdId,
            @NonNull String alertType,
            @NonNull String threshold,
            @NonNull StatusCallback<AlertThreshold> callback){

        callback.addCall(adapter.build(AlertThresholdInterface.class, APIHelper.paramsWithDomain(airwolfDomain, params)).updateAlertThreshold(parentId, thresholdId, parentId, thresholdId, alertType, threshold)).enqueue(callback);
    }

    public static void createAlertThreshold(
            @NonNull String airwolfDomain,
            @NonNull RestBuilder adapter,
            @NonNull RestParams params,
            @NonNull String parentId,
            @NonNull String studentId,
            @NonNull String alertType,
            @NonNull StatusCallback<AlertThreshold> callback){

        callback.addCall(adapter.build(AlertThresholdInterface.class, APIHelper.paramsWithDomain(airwolfDomain, params)).createAlertThreshold(parentId, studentId, alertType)).enqueue(callback);
    }

    public static void updateAlertThreshold(
            @NonNull String airwolfDomain,
            @NonNull RestBuilder adapter,
            @NonNull RestParams params,
            @NonNull String parentId,
            @NonNull String thresholdId,
            @NonNull String alertType,
            @NonNull StatusCallback<AlertThreshold> callback){

        callback.addCall(adapter.build(AlertThresholdInterface.class, APIHelper.paramsWithDomain(airwolfDomain, params)).updateAlertThreshold(parentId, thresholdId, parentId, thresholdId, alertType)).enqueue(callback);
    }

    public static void deleteAlertThreshold(
            @NonNull String airwolfDomain,
            @NonNull RestBuilder adapter,
            @NonNull RestParams params,
            @NonNull String parentId,
            @NonNull String thresholdId,
            @NonNull StatusCallback<ResponseBody> callback){

        callback.addCall(adapter.build(AlertThresholdInterface.class, APIHelper.paramsWithDomain(airwolfDomain, params)).deleteAlertThreshold(parentId, thresholdId)).enqueue(callback);
    }

    public static void getObserverAlertThresholds(
            @NonNull RestBuilder adapter,
            @NonNull RestParams params,
            long studentId,
            @NonNull StatusCallback<List<ObserverAlertThreshold>> callback){

        callback.addCall(adapter.build(AlertThresholdInterface.class, params).getObserverAlertThresholds(studentId)).enqueue(callback);
    }

    public static void getObserverAlertThresholdById(
            @NonNull RestBuilder adapter,
            @NonNull RestParams params,
            long thresholdId,
            @NonNull StatusCallback<ObserverAlertThreshold> callback){

        callback.addCall(adapter.build(AlertThresholdInterface.class, params).getObserverAlertThresholdById(thresholdId)).enqueue(callback);
    }

    public static void createObserverAlertThreshold(
            @NonNull RestBuilder adapter,
            @NonNull RestParams params,
            @NonNull ObserverAlertThresholdPostBodyWrapper body,
            @NonNull StatusCallback<ObserverAlertThreshold> callback){

        callback.addCall(adapter.build(AlertThresholdInterface.class, params).createObserverAlertThreshold(body)).enqueue(callback);
    }

    public static void updateObserverAlertThreshold(
            @NonNull RestBuilder adapter,
            @NonNull RestParams params,
            @NonNull String thresholdId,
            @NonNull String alertType,
            @NonNull String threshold,
            @NonNull StatusCallback<ObserverAlertThreshold> callback){

        callback.addCall(adapter.build(AlertThresholdInterface.class, params).updateObserverAlertThreshold(thresholdId, threshold)).enqueue(callback);
    }

    public static void createObserverAlertThreshold(
            @NonNull RestBuilder adapter,
            @NonNull RestParams params,
            long studentId,
            @NonNull String alertType,
            @NonNull StatusCallback<ObserverAlertThreshold> callback){

        callback.addCall(adapter.build(AlertThresholdInterface.class, params).createObserverAlertThreshold(studentId, alertType)).enqueue(callback);
    }

    public static void deleteObserverAlertThreshold(
            @NonNull RestBuilder adapter,
            @NonNull RestParams params,
            @NonNull String thresholdId,
            @NonNull StatusCallback<ResponseBody> callback){

        callback.addCall(adapter.build(AlertThresholdInterface.class, params).deleteObserverAlertThreshold(thresholdId)).enqueue(callback);
    }
}
