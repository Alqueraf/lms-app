/*
 * Copyright (C) 2016 - present Instructure, Inc.
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
package com.instructure.candroid.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import com.instructure.candroid.R
import com.instructure.candroid.router.RouteMatcher
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.LockInfo
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.interactions.router.Route
import com.instructure.pandautils.utils.*
import com.instructure.pandautils.views.CanvasWebView
import kotlinx.android.synthetic.main.fragment_assignment_basic.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

class AssignmentBasicFragment : ParentFragment() {

    private var canvasContext: CanvasContext by ParcelableArg(key = Const.CANVAS_CONTEXT)
    private var assignment: Assignment by ParcelableArg()

    //region Fragment Lifecycle Overrides
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return layoutInflater.inflate(R.layout.fragment_assignment_basic, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupViews()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onResume() {
        super.onResume()
        assignmentWebView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        assignmentWebView?.onPause()
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }
    //endregion

    //region Setup
    private fun setupViews() {
        if (assignment.dueAt != null) {
            dueDate.text = getString(R.string.dueAtTime, DateHelper.getDateTimeString(activity, assignment.dueAt))
        } else {
            dueDateWrapper.setGone()
        }

        assignmentWebView.addVideoClient(activity)
        assignmentWebView.canvasEmbeddedWebViewCallback = object : CanvasWebView.CanvasEmbeddedWebViewCallback {
            override fun launchInternalWebViewFragment(url: String) {
                // Create and add the InternalWebviewFragment to deal with the link they clicked
                val route = InternalWebviewFragment.makeRoute(url, "", false, "")
                val internalWebviewFragment = InternalWebviewFragment.newInstance(route)
//                internalWebviewFragment.arguments = InternalWebviewFragment.makeRoute(url, "", false, "")

                val ft = activity.supportFragmentManager.beginTransaction()
                ft.setCustomAnimations(R.anim.slide_in_from_bottom, android.R.anim.fade_out, R.anim.none, R.anim.slide_out_to_bottom)
                ft.add(R.id.fullscreen, internalWebviewFragment, internalWebviewFragment?.javaClass?.name)
                ft.addToBackStack(internalWebviewFragment?.javaClass?.name)
                ft.commitAllowingStateLoss()
            }

            override fun shouldLaunchInternalWebViewFragment(url: String): Boolean = true
        }

        assignmentWebView.canvasWebViewClientCallback = object : CanvasWebView.CanvasWebViewClientCallback {
            override fun openMediaFromWebView(mime: String, url: String, filename: String) {}
            override fun onPageStartedCallback(webView: WebView, url: String) {}
            override fun onPageFinishedCallback(webView: WebView, url: String) {}

            override fun canRouteInternallyDelegate(url: String): Boolean = RouteMatcher.canRouteInternally(activity, url, ApiPrefs.domain, false)

            override fun routeInternallyCallback(url: String) {
                RouteMatcher.canRouteInternally(activity, url, ApiPrefs.domain, true)
            }
        }

        // Assignment description can be null
        var description = when {
            assignment.isLocked -> getLockedInfoHTML(assignment.lockInfo, activity, R.string.lockedAssignmentDesc)
            assignment.lockAt?.before(Calendar.getInstance(Locale.getDefault()).time) == true ->
                // If an assignment has an available from and until field and it has expired (the current date is after "until" it will have a lock explanation,
                // but no lock info because it isn't locked as part of a module
                assignment.lockExplanation
            else -> assignment.description
        }

        if (description.isNullOrBlank() || description == "null") {
            description = "<p>" + getString(R.string.noDescription) + "</p>"
        }

        assignmentWebView.formatHTML(description, assignment.name)
    }
    //endregion

    //region Bus Events
    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onBackStackChangedEvent(event: OnBackStackChangedEvent) {
        event.get { clazz ->
            if (clazz?.isAssignableFrom(AssignmentBasicFragment::class.java) == true) {
                assignmentWebView?.onResume()
            } else {
                assignmentWebView?.onPause()
            }
        }
    }
    //endregion

    //region Fragment Interaction Overrides

    override fun applyTheme() {
        toolbar.let {
            it.title = assignment.name ?: ""
            it.setupAsBackButton(this)
            ViewStyler.themeToolbar(activity, it, canvasContext)
        }
    }

    override fun title(): String = assignment.name ?: ""
    //endregion

    //region Parent Fragment Overrides
    override fun handleBackPressed(): Boolean = assignmentWebView?.handleGoBack() ?: super.handleBackPressed()
    //endregion

    private fun getLockedInfoHTML(lockInfo: LockInfo, context: Context, explanationFirstLine: Int): String {
        /*
            Note: if the html that this is going in isn't based on html_wrapper.html (it will have something
            like -- String html = CanvasAPI.getAssetsFile(getSherlockActivity(), "html_wrapper.html");) this will
            not look as good. The blue button will just be a link.
         */
        // Get the locked message and make the module name bold
        var lockedMessage = ""

        if (lockInfo.lockedModuleName != null) {
            lockedMessage = "<p>" + String.format(context.getString(explanationFirstLine), "<b>" + lockInfo.lockedModuleName + "</b>") + "</p>"
        }

        if (lockInfo.modulePrerequisiteNames.size > 0) {
            // We only want to add this text if there are module completion requirements
            lockedMessage += context.getString(R.string.mustComplete) + "<ul>"
            for (i in 0 until lockInfo.modulePrerequisiteNames.size) {
                lockedMessage += "<li>" + lockInfo.modulePrerequisiteNames[i] + "</li>"  // "&#8226; "
            }
            lockedMessage += "</ul>"
        }

        // Check to see if there is an unlocked date
        if (lockInfo.unlockAt?.after(Date()) == true) {
            val unlocked = DateHelper.getDateTimeString(context, lockInfo.unlockAt)
            // If there is an unlock date but no module then the assignment is locked
            if (lockInfo.contextModule == null) {
                lockedMessage = "<p>" + context.getString(R.string.lockedAssignmentNotModule) + "</p>"
            }
            lockedMessage += context.getString(R.string.unlockedAt) + "<ul><li>" + unlocked + "</li></ul>"
        }

        return lockedMessage
    }

    companion object {
        fun makeRoute(canvasContext: CanvasContext, assignment: Assignment): Route {
            val bundle = Bundle().apply { putParcelable(Const.ASSIGNMENT, assignment) }
            return Route(AssignmentBasicFragment::class.java, canvasContext, bundle)
        }

        fun newInstance(route: Route): AssignmentBasicFragment? {
            if (!validRoute(route)) return null
            return AssignmentBasicFragment().withArgs(route.argsWithContext)
        }

        private fun validRoute(route: Route) = route.canvasContext != null && route.arguments.containsKey(Const.ASSIGNMENT)
    }
}
