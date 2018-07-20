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
import com.instructure.canvasapi2.apis.AlertAPI;
import com.instructure.canvasapi2.builders.RestBuilder;
import com.instructure.canvasapi2.builders.RestParams;
import com.instructure.canvasapi2.models.Alert;
import com.instructure.canvasapi2.models.ObserverAlert;

import java.util.List;

import okhttp3.ResponseBody;


public class AlertManager extends BaseManager {

    private static boolean mTesting = false;

    public static void getAlertsAirwolf(String airwolfDomain, String parentId, String studentId, boolean forceNetwork, StatusCallback<List<Alert>> callback) {
        if (isTesting() || mTesting) {
            //TODO:
        } else {
            RestBuilder adapter = new RestBuilder(callback);
            RestParams params = new RestParams.Builder()
                    .withPerPageQueryParam(true)
                    .withDomain(airwolfDomain)
                    .withForceReadFromNetwork(forceNetwork)
                    .withAPIVersion("")
                    .build();

            AlertAPI.getAlertsAirwolf(parentId, studentId, adapter, callback, params);
        }
    }

    public static void markAlertAsDismissed(String airwolfDomain, String parentId, String alertId, StatusCallback<ResponseBody> callback) {
        if (isTesting() || mTesting) {
            //TODO:
        } else {
            RestBuilder adapter = new RestBuilder(callback);
            RestParams params = new RestParams.Builder()
                    .withPerPageQueryParam(false)
                    .withDomain(airwolfDomain)
                    .withAPIVersion("")
                    .build();

            AlertAPI.markAlertAsDismissed(parentId, alertId, adapter, callback, params);
        }
    }

    public static void markAlertAsRead(String airwolfDomain, String parentId, String alertId, StatusCallback<ResponseBody> callback) {
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

            AlertAPI.markAlertAsRead(parentId, alertId, adapter, callback, params);
        }
    }

    public static void getObserverAlerts(long studentId, boolean forceNetwork, StatusCallback<List<ObserverAlert>> callback) {
        if (isTesting() || mTesting) {
            //TODO:
        } else {
            RestBuilder adapter = new RestBuilder(callback);
            RestParams params = new RestParams.Builder()
                    .withPerPageQueryParam(true)
                    .withForceReadFromNetwork(forceNetwork)
                    .build();

            AlertAPI.getObserverAlerts(studentId, adapter, callback, params);
        }
    }

    public static void updateObserverAlert(long alertId, String workflowState, StatusCallback<ObserverAlert> callback) {
        if (isTesting() || mTesting) {
            //TODO:
        } else {
            RestBuilder adapter = new RestBuilder(callback);
            RestParams params = new RestParams.Builder()
                    .withPerPageQueryParam(false)
                    .build();

            AlertAPI.updateObserverAlert(alertId, workflowState, adapter, callback, params);
        }
    }
}
