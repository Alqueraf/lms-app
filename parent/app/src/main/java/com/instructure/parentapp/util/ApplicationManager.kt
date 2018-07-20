/*
 * Copyright (C) 2016 - present  Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.instructure.parentapp.util

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.text.TextUtils
import android.util.Log
import android.webkit.WebView

import com.crashlytics.android.Crashlytics
import com.instructure.canvasapi2.AppManager
import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.managers.OAuthManager
import com.instructure.canvasapi2.managers.UserManager
import com.instructure.canvasapi2.models.CanvasErrorCode
import com.instructure.canvasapi2.models.Student
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.APIHelper
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.Prefs
import com.instructure.parentapp.BuildConfig
import com.instructure.parentapp.R
import com.instructure.parentapp.activity.BaseParentActivity
import com.instructure.parentapp.asynctask.LogoutAsyncTask
import java.util.UUID

import io.fabric.sdk.android.Fabric
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import retrofit2.Call
import retrofit2.Response

class ApplicationManager : AppManager() {

    override fun onCreate() {
        // Set preferences to create a pre-logged-in state. This should only be used for the 'robo' app flavor.
        if (BuildConfig.IS_ROBO_TEST) RoboTesting.setAppStatePrefs(this)

        super.onCreate()

        EventBus.getDefault().register(this)

        Fabric.with(this, Crashlytics())

        // there appears to be a bug when the user is installing/updating the android webview stuff.
        // http://code.google.com/p/android/issues/detail?id=175124
        try {
            WebView.setWebContentsDebuggingEnabled(true)
        } catch (e: Exception) {
            Log.d("ParentApp", "Exception trying to setWebContentsDebuggingEnabled")
        }

        val pref = this.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        // If we don't have one, generate one.
        if (!pref.contains("APID")) {
            val uuid = UUID.randomUUID().toString()

            val editor = pref.edit()
            editor.putString("APID", uuid)
            editor.apply()
        }
    }

    @Suppress("unused")
    @Subscribe
    fun canvasErrorCodeEvent(event: CanvasErrorCode) {
        if (event.code == 401) {
            UserManager.getSelf(true, object : StatusCallback<User>() {
                override fun onFail(callResponse: Call<User>?, error: Throwable, response: Response<*>?) {
                    LogoutAsyncTask().execute()
                }
            })
        }
    }

    companion object {
        val PREF_NAME = "android_parent_SP"
        val PREF_FILE_NAME = "android_parent_SP"
        val MULTI_SIGN_IN_PREF_NAME = "multipleSignInAndroidParentSP"
        val OTHER_SIGNED_IN_USERS_PREF_NAME = "otherSignedInUsersAndroidParentSP"
        val PREF_NAME_PREVIOUS_DOMAINS = "android_parent_SP_previous_domains"


        fun getParentId(context: Context): String {
            val prefs = Prefs(context, com.instructure.parentapp.util.Const.CANVAS_PARENT_SP)
            return prefs.load(Const.ID, "")
        }

        fun checkTokenValidity(activity: Activity) {
            // Try to get the students to verify that the parent's token is okay, if we get another 401 log them out.
            val prefs = Prefs(activity, activity.getString(R.string.app_name_parent))
            val parentId = prefs.load(Const.ID, "")
            // We want to refresh cache so the main activity can load quickly with accurate information
            if (!TextUtils.isEmpty(parentId)) {
                UserManager.getStudentsForParentAirwolf(ApiPrefs.airwolfDomain, parentId, object : StatusCallback<List<Student>>() {
                    override fun onFail(call: Call<List<Student>>?, error: Throwable, response: Response<*>?) {
                        // Check to see if they're already logged out
                        if (!TextUtils.isEmpty(ApiPrefs.token)) {
                            LogoutAsyncTask().execute()
                        }
                    }
                })
            }
        }
    }
}
