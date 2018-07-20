/*
 * Copyright (C) 2018 - present Instructure, Inc.
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
package com.instructure.parentapp.dialogs

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.instructure.canvasapi2.managers.UserManager
import com.instructure.canvasapi2.models.TermsOfService
import com.instructure.canvasapi2.utils.isValid
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.descendants
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.parentapp.R
import com.instructure.parentapp.activity.InternalWebViewActivity
import kotlinx.android.synthetic.main.dialog_legal.*
import kotlinx.coroutines.experimental.Job

@Suppress("EXPERIMENTAL_FEATURE_WARNING")
class LegalDialog : AppCompatDialogFragment() {

    private var termsJob: Job? = null

    private var html: String = ""

    init {
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return LayoutInflater.from(activity).inflate(R.layout.dialog_legal, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view?.descendants<ImageView>()?.onEach { it.setColorFilter(ThemePrefs.brandColor) }

        // Different institutions can have different terms of service, we need to get them from the api
        termsJob = tryWeave {
            html = awaitApi<TermsOfService> { UserManager.getTermsOfService(it, true) }.content.orEmpty()
            progressBar.setGone()
            itemContainer.setVisible()
            // Hide the item if the institution has set terms and conditions to be "no terms"
            termsOfUse.setVisible(html.isValid())
        } catch {
            itemContainer.setVisible()
        }

        termsOfUse.setOnClickListener {
            val intent = InternalWebViewActivity.createIntent(
                activity,
                "http://www.canvaslms.com/policies/terms-of-use",
                html,
                activity.getString(R.string.termsOfUse),
                false
            )
            activity.startActivity(intent)
            dialog.dismiss()
        }

        privacyPolicy.setOnClickListener {
            val intent = InternalWebViewActivity.createIntent(
                activity,
                "https://www.instructure.com/policies/privacy/",
                activity.getString(R.string.privacyPolicy),
                false
            )
            activity.startActivity(intent)
            dialog.dismiss()
        }

        openSource.setOnClickListener {
            startActivity(Intent(context, OssLicensesMenuActivity::class.java))
            dialog.dismiss()
        }
    }

    override fun onDestroyView() {
        if (retainInstance) dialog?.setDismissMessage(null)
        super.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
        termsJob?.cancel()
    }

    companion object {
        const val TAG = "legalDialog"
    }
}
