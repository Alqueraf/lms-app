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
 */
package com.instructure.espresso

import android.os.Bundle
import android.support.test.espresso.IdlingRegistry
import android.support.test.espresso.IdlingResource
import android.support.test.runner.AndroidJUnitRunner
import android.support.test.runner.MonitoringInstrumentation
import com.instructure.canvasapi2.CanvasRestAdapter
import com.instructure.espresso.ditto.DittoConfig
import com.jakewharton.espresso.OkHttp3IdlingResource

@Suppress("unused")
class InstructureRunner : AndroidJUnitRunner() {

    private lateinit var resource: IdlingResource

    override fun onStart() {
        val client = DittoConfig.setupClient(CanvasRestAdapter.getOkHttpClient())
        CanvasRestAdapter.setClient(client)
        resource = OkHttp3IdlingResource.create("okhttp", client)
        IdlingRegistry.getInstance().register(resource)
        super.onStart()
    }

    override fun finish(resultCode: Int, results: Bundle) {
        IdlingRegistry.getInstance().unregister(resource)
        super.finish(resultCode, results)
    }

    companion object {

        private const val START_ACTIVITY_TIMEOUT_SECONDS = 120

        init {
            try {
                // private static final int START_ACTIVITY_TIMEOUT_SECONDS = 45;
                // https://android.googlesource.com/platform/frameworks/testing/+/7a552ffc0bce492a7b87755490f3df7490dc357c/support/src/android/support/test/runner/MonitoringInstrumentation.java#78
                val field = MonitoringInstrumentation::class.java.getDeclaredField("START_ACTIVITY_TIMEOUT_SECONDS")
                field.isAccessible = true
                field.set(null, START_ACTIVITY_TIMEOUT_SECONDS)
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }

    }

}
