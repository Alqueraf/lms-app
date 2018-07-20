/*
 * Copyright (C) 2018 - present Instructure, Inc.
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
 */

package com.instructure.canvasapi2.models

import com.google.gson.annotations.SerializedName
import paperparcel.PaperParcel
import paperparcel.PaperParcelable
import java.util.*

@PaperParcel
data class ObserverAlert(
        private var id: Long = 0,
        var title: String? = null,
        @SerializedName("user_id")
        var userId: Long = 0,
        @SerializedName("observer_id")
        var observerId: Long = 0,
        @SerializedName("observer_alert_threshold_id")
        var observerAlertThresholdId: Long = 0,
        @SerializedName("alert_type")
        var alertType: String? = null,
        @SerializedName("context_type")
        var contextType: String? = null,
        @SerializedName("context_id")
        var contextId: String? = null,
        @SerializedName("workflow_state")
        var workflowState: String? = null,
        @SerializedName("html_url")
        var htmlUrl: String? = null,
        @SerializedName("action_date")
        var date: String? = null
) : CanvasModel<ObserverAlert>(), PaperParcelable {

    override fun getComparisonDate(): Date? = null
    override fun getComparisonString() = title
    override fun getId() = id
    override fun describeContents() = 0

    fun isMarkedRead() : Boolean {
        return workflowState == "read"
    }

    companion object {
        @Suppress("unresolved_reference")
        @JvmField val CREATOR = PaperParcelObserverAlert.CREATOR
    }
}
