//
// Copyright (C) 2018-present Instructure, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//



package normal

import api.bitrise.BitriseAppObject
import api.buddybuild.BuddybuildAppObject

data class NormalApp(
        val id: String,
        val name: String
)

private fun validateAppId(app: NormalApp, expectedLength: Int) {
    if (app.id.length != expectedLength) {
        throw RuntimeException("Invalid app identifier ${app.id}. Must be length $expectedLength")
    }
}

interface ToNormalApp {
    fun toNormalApp(): NormalApp {
        return when {
            this is BitriseAppObject -> {
                val result = NormalApp(slug, title)
                validateAppId(result, 16)
                result
            }
            this is BuddybuildAppObject -> {
                val result = NormalApp(_id, app_name)
                validateAppId(result, 24)
                result
            }
            else -> throw RuntimeException("Unable to convert class ${this::class} to normal app")
        }
    }
}
