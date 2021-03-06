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
package com.instructure.espresso.page

import android.Manifest
import android.app.Activity
import android.support.test.InstrumentationRegistry
import android.support.test.rule.GrantPermissionRule
import com.google.protobuf.MessageLite
import com.instructure.espresso.BuildConfig
import com.instructure.espresso.InstructureActivityTestRule
import com.instructure.espresso.ScreenshotTestRule
import com.instructure.espresso.UiControllerSingleton
import com.instructure.espresso.ditto.DittoConfig
import com.instructure.espresso.ditto.DittoMode
import com.instructure.espresso.ditto.DittoRule
import okreplay.AndroidTapeRoot
import okreplay.DittoResponseMod
import okreplay.MatchRules
import org.junit.Before
import org.junit.ClassRule
import org.junit.Rule
import org.junit.rules.RuleChain
import org.junit.rules.TestRule

abstract class InstructureTest : InstructureTestingContract {

    abstract val activityRule: InstructureActivityTestRule<out Activity>

    abstract val isTesting: Boolean

    private val dittoMode: DittoMode = when (BuildConfig.GLOBAL_DITTO_MODE.toLowerCase()) {
        "play" -> DittoMode.PLAY
        "record" -> DittoMode.RECORD
        "live" -> DittoMode.LIVE
        else -> throw IllegalArgumentException("Invalid ditto mode specified. Valid options are 'play', 'record', or 'live'.")
    }

    private val dittoConfig = DittoConfig(
        globalMode = dittoMode,
        matchRules = arrayOf(MatchRules.uri, MatchRules.method),
        tapeRoot = AndroidTapeRoot(InstrumentationRegistry.getContext(), javaClass)
    )

    @Rule
    override fun chain(): TestRule {
        return RuleChain
            .outerRule(ScreenshotTestRule())
            .around(DittoRule(dittoConfig))
            .around(activityRule)
    }

    @Before
    override fun launchActivity() {
        if (!configChecked) {
            checkBuildConfig()
            configChecked = true
        }
        activityRule.launchActivity(null)
        UiControllerSingleton.get()
    }

    private fun checkBuildConfig() {
        if (!isTesting) throw RuntimeException("Build config must be IS_TESTING! (qaDebug)")
    }

    @Suppress("unused")
    fun InstructureTest.addDittoMod(mod: DittoResponseMod) = DittoConfig.interceptor.addResponseMod(mod)

    inline fun <reified T : MessageLite> mockableSeed(onRecord: () -> T): T {
        return DittoConfig.interceptor.playSeededDataBytes()
            ?.let {
                val method = T::class.java.getMethod("newBuilder")
                val builder = method.invoke(null) as MessageLite.Builder
                builder.mergeFrom(it).build() as T
            } ?: onRecord().also { DittoConfig.interceptor.recordSeededDataBytes(it.toByteArray()) }
    }

    inline fun mockableString(label: String, onRecord: () -> String): String {
        return DittoConfig.interceptor.playTestData(label) ?: onRecord().also { DittoConfig.interceptor.recordTestData(label, it) }
    }

    inline fun mockableDouble(label: String, onRecord: () -> Double): Double {
        return DittoConfig.interceptor.playTestData(label)?.toDoubleOrNull()
                ?: onRecord().also { DittoConfig.interceptor.recordTestData(label, it.toString()) }
    }

    companion object {

        /* Both read & write permission are required for saving screenshots
         * otherwise the code will error with permission denied.
         * Read also required due to a bug specifically with full read/write external storage not being granted
         * https://issuetracker.google.com/issues/64389280 */
        @ClassRule
        @JvmField
        val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        private var configChecked = false

    }

}
