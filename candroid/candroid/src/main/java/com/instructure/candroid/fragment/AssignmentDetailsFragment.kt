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

import android.graphics.Typeface
import android.os.Bundle
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.webkit.WebView
import android.widget.TextView
import com.instructure.candroid.R
import com.instructure.candroid.router.RouteMatcher
import com.instructure.candroid.util.Const
import com.instructure.candroid.util.LockInfoHTMLHelper
import com.instructure.candroid.view.ViewUtils
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.pageview.BeforePageView
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.pageview.PageViewUrlParam
import com.instructure.canvasapi2.utils.pageview.PageViewUrlQuery
import com.instructure.interactions.bookmarks.Bookmarkable
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouterParams
import com.instructure.pandautils.utils.*
import com.instructure.pandautils.views.CanvasWebView
import kotlinx.android.synthetic.main.assignment_details_header.*
import kotlinx.android.synthetic.main.fragment_assignment_details.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

@PageView(url = "{canvasContext}/assignments/{assignmentId}")
class AssignmentDetailsFragment : ParentFragment() {

    // Bundle Args
    private var canvasContext: CanvasContext by ParcelableArg(key = Const.CANVAS_CONTEXT)

    // Keep assignment logic within populateAssignmentDetails method, otherwise assignment could be null
    private var assignment: Assignment? = null

    //region Fragment Lifecycle Overrides
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            layoutInflater.inflate(R.layout.fragment_assignment_details, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        canvasWebView.addVideoClient(activity)
        setListeners()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this);
    }

    override fun onResume() {
        super.onResume()
        canvasWebView.onResume()
    }

    override fun onPause() {
        super.onPause()
        canvasWebView.onPause()
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this);
    }
    //endregion

    //region Fragment Interaction Overrides
    override fun title(): String = if (assignment != null) assignment!!.name else getString(R.string.assignments)

    override fun applyTheme() {}

    //endregion

    override fun handleBackPressed(): Boolean = canvasWebView.handleGoBack()

    //endregion

    //region Setup

    @BeforePageView
    fun setupAssignment(assignment: Assignment) {
        this.assignment = assignment
        populateAssignmentDetails(this.assignment)
    }
    /**
     * @param assignment The assignment
     * @param isWithinAnotherCallback See note above
     * @param isCached See note above
     */

    private fun setListeners() {
        notificationTextDismiss.setOnClickListener {
            val fadeOut = AnimationUtils.loadAnimation(context, R.anim.fade_out)
            fadeOut.fillAfter = true
            fadeOut.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {}

                override fun onAnimationEnd(animation: Animation) {
                    notificationTextContainer.visibility = View.GONE
                }

                override fun onAnimationRepeat(animation: Animation) {}
            })
            notificationTextContainer.startAnimation(fadeOut)
        }

        canvasWebView.canvasWebViewClientCallback = object : CanvasWebView.CanvasWebViewClientCallback {
            override fun openMediaFromWebView(mime: String, url: String, filename: String) {
                openMedia(mime, url, filename, canvasContext)
            }

            override fun onPageFinishedCallback(webView: WebView, url: String) {}

            override fun onPageStartedCallback(webView: WebView, url: String) {}

            override fun canRouteInternallyDelegate(url: String): Boolean {
                return RouteMatcher.canRouteInternally(activity, url, ApiPrefs.domain, false)
            }

            override fun routeInternallyCallback(url: String) {
                RouteMatcher.canRouteInternally(activity, url, ApiPrefs.domain, true)
            }
        }

        canvasWebView.canvasEmbeddedWebViewCallback = object : CanvasWebView.CanvasEmbeddedWebViewCallback {
            override fun launchInternalWebViewFragment(url: String) {
                InternalWebviewFragment.loadInternalWebView(activity, InternalWebviewFragment.makeRoute(canvasContext, url, false))
            }

            override fun shouldLaunchInternalWebViewFragment(url: String): Boolean = true
        }
    }

    /**
     * Updates each view with its corresponding assignment data.
     * @param assignment
     */
    private fun populateAssignmentDetails(assignment: Assignment?) {
        // Make sure we have all of the data.
        assignment ?: return

        textViewAssignmentTitle.text = assignment.name

        // Due Date
        if (assignment.dueAt != null) {
            val dueDate = DateHelper.createPrefixedDateTimeString(context, R.string.assignmentDue, assignment.dueAt)
            textViewDueDate.visibility = View.VISIBLE
            textViewDueDate.setTypeface(null, Typeface.ITALIC)
            textViewDueDate.text = dueDate

        } else {
            textViewDueDate.visibility = View.GONE
        }

        // Submission Type
        if (assignment.submissionTypes.contains(Assignment.SUBMISSION_TYPE.NONE)) {
            textViewSubmissionDate.visibility = View.INVISIBLE
        } else {
            textViewSubmissionDate.visibility = View.VISIBLE
            if (assignment.submission != null) {
                updateSubmissionDate(assignment.submission.submittedAt)
            }
        }
        pointsPossible.text = "${assignment.pointsPossible}"

        populateWebView(assignment)

        // This check is to prevent the context from becoming null when assignment items are
        // clicked rapidly in the notification list.
        if (context != null) {
            if (assignment.gradingType != null) {
                gradingType!!.text = Assignment.gradingTypeToPrettyPrintString(assignment.gradingType, context)
            } else {
                gradingType!!.visibility = View.INVISIBLE
            }

            val assignmentTurnInType = assignment.turnInType

            if (assignmentTurnInType != null) {
                submissionTypeSelected.text = Assignment.turnInTypeToPrettyPrintString(assignmentTurnInType, context)
            }


            // Make sure there are no children views
            onlineSubmissionTypes.removeAllViews()

            if (assignmentTurnInType == Assignment.TURN_IN_TYPE.ONLINE) {
                for (submissionType in assignment.submissionTypes) {
                    val submissionTypeTextView = TextView(context)
                    submissionTypeTextView.setPadding(0, ViewUtils.convertDipsToPixels(5f, context).toInt(), 0, 0)

                    submissionTypeTextView.text = Assignment.submissionTypeToPrettyPrintString(submissionType, context)

                    onlineSubmissionTypes.addView(submissionTypeTextView)
                }
            }
        }
    }

    private fun populateWebView(assignment: Assignment) {
        var description: String?
        description = if (assignment.isLocked) {
            LockInfoHTMLHelper.getLockedInfoHTML(assignment.lockInfo, activity, R.string.lockedAssignmentDesc, R.string.lockedAssignmentDescLine2)
        } else if (assignment.lockAt != null && assignment.lockAt!!.before(Calendar.getInstance(Locale.getDefault()).time)) {
            // If an assignment has an available from and until field and it has expired (the current date is after "until" it will have a lock explanation,
            // but no lock info because it isn't locked as part of a module
            assignment.lockExplanation
        } else {
            assignment.description
        }

        if (description == null || description == "null" || description == "") {
            description = "<p>" + getString(R.string.noDescription) + "</p>"
        }
        canvasWebView.formatHTML(description, assignment.name)
    }

    fun setAssignmentWithNotification(assignment: Assignment?, msg: String?) {
        assignment ?: return

        var message = msg

        if (message != null) {
            message = message.trim { it <= ' ' }
        }

        if (!TextUtils.isEmpty(message)) {
            // get rid of "________________________________________ You received this email..." text
            val index = message!!.indexOf("________________________________________")
            if (index > 0) {
                message = message.substring(0, index)
            }

            notificationText.text = message.trim { it <= ' ' }
            notificationText.movementMethod = LinkMovementMethod.getInstance()
            notificationText.setVisible()
            notificationTextContainer.setVisible()
        }
    }
    //endregion

    //region Functionality
    fun updateSubmissionDate(submissionDate: Date?) {
        var submitDate = getString(R.string.assignmentLastSubmission) + ": " + getString(R.string.assignmentNoSubmission)
        if (submissionDate != null) {
            submitDate = DateHelper.createPrefixedDateTimeString(context, R.string.assignmentLastSubmission, submissionDate)
        }
        textViewSubmissionDate.text = submitDate
    }
    //endregion

    //region Bus Events
    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onBackStackChangedEvent(event: OnBackStackChangedEvent) {
        event.get { clazz ->
            if (clazz?.isAssignableFrom(AssignmentDetailsFragment::class.java) == true) {
                canvasWebView.onResume()
            } else {
                canvasWebView.onPause()
            }
        }
    }
    //endregion

    @PageViewUrlParam("assignmentId")
    private fun getAssignmentId() = assignment?.id ?: 0

    @PageViewUrlQuery("module_item_id")
    private fun pageViewModuleItemId() = getModuleItemId()

    companion object {
        val tabTitle: Int
            get() = R.string.assignmentTabDetails

        fun makeRoute(canvasContext: CanvasContext): Route = Route(null, canvasContext, Bundle())

        fun newInstance(route: Route) = if (validRoute(route)) {
            AssignmentDetailsFragment().apply {
                arguments = route.canvasContext!!.makeBundle(route.arguments)
                canvasContext = route.canvasContext!!
            }
        } else null

        private fun validRoute(route: Route) = route.canvasContext != null

    }
}
