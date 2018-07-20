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

package com.instructure.canvasapi2.managers;

import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.apis.AlertThresholdAPI;
import com.instructure.canvasapi2.builders.RestBuilder;
import com.instructure.canvasapi2.builders.RestParams;
import com.instructure.canvasapi2.models.AlertThreshold;
import com.instructure.canvasapi2.models.ObserverAlertThreshold;
import com.instructure.canvasapi2.models.post_models.ObserverAlertThresholdPostBody;
import com.instructure.canvasapi2.models.post_models.ObserverAlertThresholdPostBodyWrapper;

import java.util.List;

import okhttp3.ResponseBody;


public class AlertThresholdManager extends BaseManager {

    private static boolean mTesting = false;

    public static void createAlertThreshold(String airwolfDomain, String parentId, String studentId, String alertType, StatusCallback<AlertThreshold> callback) {
        if (isTesting() || mTesting) {
            //TODO:
        } else {
            RestBuilder adapter = new RestBuilder(callback);
            RestParams params = new RestParams.Builder()
                    .withShouldIgnoreToken(false)
                    .withPerPageQueryParam(false)
                    .withDomain(airwolfDomain)
                    .withAPIVersion("")
                    .build();

            AlertThresholdAPI.createAlertThreshold(airwolfDomain, adapter, params, parentId, studentId, alertType, callback);
        }
    }

    public static void createAlertThreshold(String airwolfDomain, String parentId, String studentId, String alertType, String threshold, StatusCallback<AlertThreshold> callback) {
        if (isTesting() || mTesting) {
            //TODO:
        } else {
            RestBuilder adapter = new RestBuilder(callback);
            RestParams params = new RestParams.Builder()
                    .withShouldIgnoreToken(false)
                    .withPerPageQueryParam(false)
                    .withDomain(airwolfDomain)
                    .withAPIVersion("")
                    .build();

            AlertThresholdAPI.createAlertThreshold(airwolfDomain, adapter, params, parentId, studentId, alertType, threshold, callback);
        }
    }

    public static void getAlertThresholdsForStudent(String airwolfDomain, String parentId, String studentId, StatusCallback<List<AlertThreshold>> callback) {
        if (isTesting() || mTesting) {
            //TODO:
        } else {
            RestBuilder adapter = new RestBuilder(callback);
            RestParams params = new RestParams.Builder()
                    .withShouldIgnoreToken(false)
                    .withPerPageQueryParam(false)
                    .withDomain(airwolfDomain)
                    .withAPIVersion("")
                    .withForceReadFromNetwork(true)
                    .build();

            AlertThresholdAPI.getAlertThresholdsForStudent(airwolfDomain, adapter, params, parentId, studentId, callback);
        }
    }

    public static void updateAlertThreshold(String airwolfDomain, String parentId, String thresholdId, String alertType, String threshold, StatusCallback<AlertThreshold> callback) {
        if (isTesting() || mTesting) {
            //TODO:
        } else {
            RestBuilder adapter = new RestBuilder(callback);
            RestParams params = new RestParams.Builder()
                    .withShouldIgnoreToken(false)
                    .withPerPageQueryParam(false)
                    .withDomain(airwolfDomain)
                    .withAPIVersion("")
                    .build();

            AlertThresholdAPI.updateAlertThreshold(airwolfDomain, adapter, params, parentId, thresholdId, alertType, threshold, callback);
        }
    }

    public static void updateAlertThreshold(String airwolfDomain, String parentId, String thresholdId, String alertType, StatusCallback<AlertThreshold> callback) {
        if (isTesting() || mTesting) {
            //TODO:
        } else {
            RestBuilder adapter = new RestBuilder(callback);
            RestParams params = new RestParams.Builder()
                    .withShouldIgnoreToken(false)
                    .withPerPageQueryParam(false)
                    .withDomain(airwolfDomain)
                    .withAPIVersion("")
                    .build();

            AlertThresholdAPI.updateAlertThreshold(airwolfDomain, adapter, params, parentId, thresholdId, alertType, callback);
        }
    }

    public static void deleteAlertThreshold(String airwolfDomain, String parentId, String thresholdId, StatusCallback<ResponseBody> callback) {
        if (isTesting() || mTesting) {
            //TODO:
        } else {
            RestBuilder adapter = new RestBuilder(callback);
            RestParams params = new RestParams.Builder()
                    .withShouldIgnoreToken(false)
                    .withPerPageQueryParam(false)
                    .withDomain(airwolfDomain)
                    .withAPIVersion("")
                    .build();

            AlertThresholdAPI.deleteAlertThreshold(airwolfDomain, adapter, params, parentId, thresholdId, callback);
        }
    }

    public static void getObserverAlertThresholds(long studentId, StatusCallback<List<ObserverAlertThreshold>> callback) {
        if (isTesting() || mTesting) {
            //TODO:
        } else {
            RestBuilder adapter = new RestBuilder(callback);
            RestParams params = new RestParams.Builder()
                    .withShouldIgnoreToken(false)
                    .withPerPageQueryParam(false)
                    .withForceReadFromNetwork(true)
                    .build();

            AlertThresholdAPI.getObserverAlertThresholds(adapter, params, studentId, callback);
        }
    }

    public static void updateObserverAlertThreshold(String thresholdId, String alertType, String threshold, StatusCallback<ObserverAlertThreshold> callback) {
        if (isTesting() || mTesting) {
            //TODO:
        } else {
            RestBuilder adapter = new RestBuilder(callback);
            RestParams params = new RestParams.Builder()
                    .withShouldIgnoreToken(false)
                    .withPerPageQueryParam(false)
                    .build();

            AlertThresholdAPI.updateObserverAlertThreshold(adapter, params, thresholdId, alertType, threshold, callback);
        }
    }


    public static void deleteObserverAlertThreshold(String thresholdId, StatusCallback<ResponseBody> callback) {
        if (isTesting() || mTesting) {
            //TODO:
        } else {
            RestBuilder adapter = new RestBuilder(callback);
            RestParams params = new RestParams.Builder()
                    .withShouldIgnoreToken(false)
                    .withPerPageQueryParam(false)
                    .build();

            AlertThresholdAPI.deleteObserverAlertThreshold(adapter, params, thresholdId, callback);
        }
    }

    public static void createObserverAlertThreshold(long studentId, String alertType, StatusCallback<ObserverAlertThreshold> callback) {
        if (isTesting() || mTesting) {
            //TODO:
        } else {
            RestBuilder adapter = new RestBuilder(callback);
            RestParams params = new RestParams.Builder()
                    .withShouldIgnoreToken(false)
                    .withPerPageQueryParam(false)
                    .build();

            ObserverAlertThresholdPostBody body = new ObserverAlertThresholdPostBody(studentId, alertType, null);
            ObserverAlertThresholdPostBodyWrapper wrapper = new ObserverAlertThresholdPostBodyWrapper();
            wrapper.setObserver_alert_threshold(body);

            AlertThresholdAPI.createObserverAlertThreshold(adapter, params, wrapper, callback);
        }
    }

    public static void createObserverAlertThreshold(long studentId, String alertType, String threshold, StatusCallback<ObserverAlertThreshold> callback) {
        if (isTesting() || mTesting) {
            //TODO:
        } else {
            RestBuilder adapter = new RestBuilder(callback);
            RestParams params = new RestParams.Builder()
                    .withShouldIgnoreToken(false)
                    .withPerPageQueryParam(false)
                    .build();

            ObserverAlertThresholdPostBody body = new ObserverAlertThresholdPostBody(studentId, alertType, Integer.parseInt(threshold));
            ObserverAlertThresholdPostBodyWrapper wrapper = new ObserverAlertThresholdPostBodyWrapper();
            wrapper.setObserver_alert_threshold(body);
            AlertThresholdAPI.createObserverAlertThreshold(adapter, params, wrapper, callback);
        }
    }
}
