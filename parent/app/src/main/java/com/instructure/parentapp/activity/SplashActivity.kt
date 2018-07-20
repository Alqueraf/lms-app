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

package com.instructure.parentapp.activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.util.SortedList
import android.text.TextUtils
import android.widget.Toast
import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.managers.EnrollmentManager
import com.instructure.canvasapi2.managers.UserManager
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.*
import com.instructure.canvasapi2.utils.weave.*
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.setGone
import com.instructure.parentapp.R
import com.instructure.parentapp.R.id.canvasLoadingView
import com.instructure.parentapp.asynctask.LogoutAsyncTask
import com.instructure.parentapp.fragments.NotAParentFragment
import com.instructure.parentapp.util.ParentPrefs
import kotlinx.android.synthetic.main.activity_splash.*
import kotlinx.coroutines.experimental.Job
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import retrofit2.Call
import retrofit2.Response

class SplashActivity : AppCompatActivity() {

    private var checkSignedInJob: Job? = null
    private var checkRegionJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        setContentView(R.layout.activity_splash)

        when {
            ApiPrefs.token.isNotBlank() -> checkSignedIn() // They have a token
            else -> navigateLoginLandingPage() // They have no token or airwolf domain
        }
    }

    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    private fun checkSignedIn() {
        checkSignedInJob = tryWeave {
            //Now get it from the new place. This will be the true token whether they signed into dev/retrofit or the old way.
            val token = ApiPrefs.token
            ApiPrefs.protocol = "https"

            if (ApiPrefs.airwolfDomain.isNotEmpty()) {
                // They are coming from an airwolf login. Log them out and make the airwolf domain is empty for next time
                ApiPrefs.airwolfDomain = ""
                LogoutAsyncTask().execute()
                return@tryWeave
            }

            if (ParentPrefs.isObserver == null) {
                ParentPrefs.isObserver = awaitApi<List<Course>> { CourseManager.getCoursesWithEnrollmentTypeAllStates(true, it, "observer") }.isNotEmpty()
            }

            if (ApiPrefs.canBecomeUser == null) {
                if (ApiPrefs.domain.startsWith("siteadmin", true)) {
                    ApiPrefs.canBecomeUser = true
                } else try {
                    val roles = awaitApi<List<AccountRole>> { UserManager.getSelfAccountRoles(true, it) }
                    ApiPrefs.canBecomeUser = roles.any { it.permissions["become_user"]?.enabled == true }
                } catch (e: StatusCallbackError) {
                    if (e.response?.code() == 401) ApiPrefs.canBecomeUser = false
                }
            }

            if (ParentPrefs.isObserver == false && ApiPrefs.canBecomeUser != true) {
                canvasLoadingView.setGone()
                supportFragmentManager.beginTransaction()
                    .add(R.id.splashActivityRootView, NotAParentFragment(), NotAParentFragment::class.java.simpleName)
                    .commit()
                return@tryWeave
            }

            if (token.isNotBlank()) {
                EnrollmentManager.getObserveeEnrollments(true, object: StatusCallback<List<Enrollment>>() {
                    override fun onResponse(response: Response<List<Enrollment>>, linkHeaders: LinkHeaders, type: ApiType) {
                        super.onResponse(response, linkHeaders, type)
                        if (response.body() != null) {
                            response.body()?.let {
                                // Use hash set to prevent duplicates
                                val students = it.filter{ it.observedUser != null}.map { it.observedUser }.toHashSet()
                                // The user must either have students or be able to masquerade in order to enter the app
                                if (!students.isEmpty() || ApiPrefs.canBecomeUser == true) {
                                    val intent = StudentViewActivity.createIntent(ContextKeeper.appContext, students.toList())
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)
                                    finish()
                                } else {
                                    getStudents()
                                }
                            }
                        }
                    }

                    override fun onFail(call: Call<List<Enrollment>>?, error: Throwable, response: Response<*>?) {
                        super.onFail(call, error, response)
                        // The api call failed, if they're unauthorized log them out
                        // Their token may have changed or something on Airwolf could have changed. Either way, just make
                        // them start over
                        if(response?.code() == 401 && !TextUtils.isEmpty(ApiPrefs.token)) {
                            LogoutAsyncTask().execute()
                        }
                    }
                })
            }
        } catch {
            Logger.e(it.message)
            Logger.e(it.stackTrace.toString())
        }
    }
    
    /**
     * No active enrollments, but might still have students (if courses haven't been published yet)
     */
    private fun getStudents() {
        tryWeave {
            val users = awaitApi<List<User>> { UserManager.getObservees(it, true) }
            val intent = StudentViewActivity.createIntent(ContextKeeper.appContext, users)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        } catch {

        }
    }

    private fun navigateLoginLandingPage() {
        startActivity(LoginActivity.createIntent(ContextKeeper.appContext))
        finish()
    }

    companion object {

        @JvmStatic fun createIntent(context: Context): Intent {
            val intent = Intent(context, SplashActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            return intent
        }

        @JvmStatic fun createIntent(context: Context, showMessage: Boolean, message: String): Intent {
            val intent = Intent(context, SplashActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra(Const.SHOW_MESSAGE, showMessage)
            intent.putExtra(Const.MESSAGE_TO_USER, message)
            intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            return intent
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        checkSignedInJob?.cancel()
        checkRegionJob?.cancel()
    }
}
