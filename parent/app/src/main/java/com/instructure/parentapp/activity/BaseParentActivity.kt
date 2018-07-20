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
import android.os.Bundle
import android.widget.Toast
import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.managers.EnrollmentManager
import com.instructure.canvasapi2.managers.UserManager
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.ApiType
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.canvasapi2.utils.LinkHeaders
import com.instructure.pandautils.activities.BaseActivity
import com.instructure.parentapp.R
import com.instructure.parentapp.util.ApplicationManager
import retrofit2.Response

open class BaseParentActivity : BaseActivity() {

    override fun handleError(code: Int, error: String) {
        if (code == 401) {
            ApplicationManager.checkTokenValidity(this@BaseParentActivity)
        }
    }

    override fun unBundle(extras: Bundle) {
    }

    override fun applyThemeAutomagically(): Boolean {
        return false
    }

    companion object {

        fun getReadableRegion(context: Context, regionCode: String): String {
            when (regionCode) {
                "ca-central-1" -> return context.getString(R.string.canada)
                "eu-central-1" -> return context.getString(R.string.ireland)
                "eu-west-1" -> return context.getString(R.string.germany)
                "ap-southeast-1" -> return context.getString(R.string.singapore)
                "ap-southeast-2" -> return context.getString(R.string.australia)
                "us-east-1" -> return context.getString(R.string.theUnitedStates)
                else -> return context.getString(R.string.theUnitedStates)
            }
        }

    }

}
