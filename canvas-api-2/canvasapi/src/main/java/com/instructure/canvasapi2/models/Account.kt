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

import paperparcel.PaperParcel
import paperparcel.PaperParcelable
import java.util.*

@PaperParcel
data class Account(
        private var id: Long = 0,
        var name: String = ""
) : CanvasModel<Account>(), PaperParcelable {

    override fun getId() = id

    override fun getComparisonDate(): Date? = null

    override fun getComparisonString(): String? = null

    override fun describeContents() = 0

    companion object {
        @Suppress("UNRESOLVED_REFERENCE")
        @JvmField val CREATOR = PaperParcelAccount.CREATOR
    }

}