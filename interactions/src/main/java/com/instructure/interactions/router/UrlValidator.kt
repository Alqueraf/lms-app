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
package com.instructure.interactions.router

import android.net.Uri

class UrlValidator(var url: String, userDomain: String) {

    var isHostForLoggedInUser = false
        private set

    var isValid = false
        private set

    val uri: Uri?
        get() {
            if(url.contains("//canvas-student//")) url = url.replace("canvas-student//", "")
            if(url.contains("//canvas-teacher//")) url = url.replace("canvas-teacher//", "")
            if(url.contains("//canvas-parent//")) url = url.replace("canvas-parent//", "")
            return Uri.parse(url)
        }

    init {
        if (uri != null) {
            isValid = true
            val host = uri!!.host
            isHostForLoggedInUser = isLoggedInUserHost(host, userDomain)
        }
    }

    private fun isLoggedInUserHost(host: String, userDomain: String?): Boolean {
        // Assumes user is already signed in (InterwebsToApplication does a signin check)
        return userDomain != null && userDomain == host
    }
}
