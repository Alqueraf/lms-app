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

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import com.instructure.candroid.R
import com.instructure.candroid.adapter.AssignmentDateListRecyclerAdapter
import com.instructure.candroid.adapter.TermSpinnerAdapter
import com.instructure.candroid.interfaces.AdapterToAssignmentsCallback
import com.instructure.candroid.router.RouteMatcher
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.GradingPeriod
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.interactions.FragmentInteractions
import com.instructure.interactions.bookmarks.Bookmarkable
import com.instructure.interactions.bookmarks.Bookmarker
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouterParams
import com.instructure.pandautils.utils.*
import kotlinx.android.synthetic.main.assignment_list_layout.*

@PageView(url = "{canvasContext}/assignments")
class AssignmentListFragment : ParentFragment(), Bookmarkable {

    private var canvasContext by ParcelableArg<CanvasContext>(key = Const.CANVAS_CONTEXT)

    private lateinit var recyclerAdapter: AssignmentDateListRecyclerAdapter
    private var termAdapter: TermSpinnerAdapter? = null

    private val allTermsGradingPeriod by lazy {
        GradingPeriod().apply { title = getString(R.string.allGradingPeriods) }
    }

    private val adapterToAssignmentsCallback = object : AdapterToAssignmentsCallback {
        override fun setTermSpinnerState(isEnabled: Boolean) {
            termSpinner?.isEnabled = isEnabled
            termAdapter?.setIsLoading(!isEnabled)
            termAdapter?.notifyDataSetChanged()
        }

        override fun gradingPeriodsFetched(periods: List<GradingPeriod>) {
            setupGradingPeriods(periods)
        }

        override fun onRowClicked(assignment: Assignment, position: Int, isOpenDetail: Boolean) {
            RouteMatcher.route(getContext(), AssignmentFragment.makeRoute(canvasContext, assignment))
        }

        override fun onRefreshFinished() = setRefreshing(false)
    }

    override val bookmark: Bookmarker
        get() = Bookmarker(true, canvasContext)

    override fun title(): String = getString(R.string.assignments)

    override fun getSelectedParamName() = RouterParams.ASSIGNMENT_ID

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.assignment_list_layout, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        recyclerAdapter = AssignmentDateListRecyclerAdapter(
            context,
            canvasContext,
            adapterToAssignmentsCallback
        )

        configureRecyclerView(
            view!!,
            context,
            recyclerAdapter,
            R.id.swipeRefreshLayout,
            R.id.emptyPandaView,
            R.id.listView
        )

        appbar.addOnOffsetChangedListener { _, i ->
            // Workaround for Toolbar not showing with swipe to refresh
            if (i == 0) {
                setRefreshingEnabled(true)
            } else {
                setRefreshingEnabled(false)
            }
        }
    }

    override fun applyTheme() {
        setupToolbarMenu(toolbar)
        toolbar.title = title()
        toolbar.setupAsBackButton(this)
        ViewStyler.themeToolbar(activity, toolbar, canvasContext)
    }
    
    private fun setupGradingPeriods(periods: List<GradingPeriod>) {
        val adapter = TermSpinnerAdapter(
            context,
            android.R.layout.simple_spinner_dropdown_item,
            periods + allTermsGradingPeriod
        )
        termAdapter = adapter
        termSpinner.adapter = adapter
        termSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View?, i: Int, l: Long) {
                if (adapter.getItem(i)!!.title == getString(R.string.allGradingPeriods)) {
                    recyclerAdapter.loadAssignment()
                } else {
                    recyclerAdapter.loadAssignmentsForGradingPeriod(adapter.getItem(i)!!.id, true)
                    termSpinner.isEnabled = false
                    adapter.setIsLoading(true)
                    adapter.notifyDataSetChanged()
                }
                recyclerAdapter.setCurrentGradingPeriod(adapter.getItem(i))
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {}
        }

        // If we have a "current" grading period select it
        if (recyclerAdapter.currentGradingPeriod != null) {
            val position = adapter.getPositionForId(recyclerAdapter.currentGradingPeriod?.id ?: 0)
            if (position != -1) {
                termSpinner.setSelection(position)
            } else {
                toast(R.string.errorOccurred)
            }
        }

        termSpinnerLayout.setVisible()
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        configureRecyclerView(
            view!!,
            context,
            recyclerAdapter,
            R.id.swipeRefreshLayout,
            R.id.emptyPandaView,
            R.id.listView
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        recyclerAdapter.cancel()
    }

    companion object {

        @JvmStatic
        fun makeRoute(canvasContext: CanvasContext) = Route(AssignmentListFragment::class.java, canvasContext)

        @JvmStatic
        fun validateRoute(route: Route) = route.canvasContext?.isCourse == true

        @JvmStatic
        fun newInstance(route: Route): AssignmentListFragment? {
            if (!validateRoute(route)) return null
            return AssignmentListFragment().withArgs(route.canvasContext!!.makeBundle())
        }
    }

}
