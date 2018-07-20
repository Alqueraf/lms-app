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
import com.instructure.candroid.R
import com.instructure.candroid.adapter.TodoListRecyclerAdapter
import com.instructure.candroid.interfaces.NotificationAdapterToFragmentCallback
import com.instructure.candroid.router.RouteMatcher
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.ToDo
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.interactions.router.Route
import com.instructure.pandautils.utils.*
import kotlinx.android.synthetic.main.fragment_list_todo.*
import kotlinx.android.synthetic.main.recycler_swipe_refresh_layout.*

@PageView
class ToDoListFragment : ParentFragment() {

    private var canvasContext by ParcelableArg<CanvasContext>(key = Const.CANVAS_CONTEXT)
    
    private lateinit var mRecyclerAdapter: TodoListRecyclerAdapter
    
    private var mAdapterToFragmentCallback: NotificationAdapterToFragmentCallback<ToDo> = object : NotificationAdapterToFragmentCallback<ToDo> {
        override fun onRowClicked(todo: ToDo, position: Int, isOpenDetail: Boolean) {
            mRecyclerAdapter.selectedPosition = position
            onRowClick(todo)
        }

        override fun onRefreshFinished() {
            setRefreshing(false)
            editOptions.setGone()
        }

        override fun onShowEditView(isVisible: Boolean) {
            editOptions.setVisible(isVisible)
        }

        override fun onShowErrorCrouton(message: Int) = Unit
    }

    override fun title(): String = getString(R.string.Todo)

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return layoutInflater.inflate(R.layout.fragment_list_todo, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        mRecyclerAdapter = TodoListRecyclerAdapter(context, canvasContext, mAdapterToFragmentCallback)
        configureRecyclerView(
            view!!,
            context,
            mRecyclerAdapter,
            R.id.swipeRefreshLayout,
            R.id.emptyPandaView,
            R.id.listView
        )
        
        listView.isSelectionEnabled = false

        confirmButton.text = getString(R.string.markAsDone)
        confirmButton.setOnClickListener { mRecyclerAdapter.confirmButtonClicked() }

        cancelButton.setText(R.string.cancel)
        cancelButton.setOnClickListener { mRecyclerAdapter.cancelButtonClicked() }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        toolbar.title = title()
        navigation?.attachNavigationDrawer(this, toolbar)
    }

    override fun applyTheme() {
        setupToolbarMenu(toolbar)
        ViewStyler.themeToolbar(activity, toolbar, ThemePrefs.primaryColor, ThemePrefs.primaryTextColor)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        configureRecyclerView(
            view!!,
            context,
            mRecyclerAdapter,
            R.id.swipeRefreshLayout,
            R.id.emptyPandaView,
            R.id.listView
        )
    }

    private fun onRowClick(toDo: ToDo?) {
        if (toDo?.assignment != null) {
            // Launch assignment details fragment.
            RouteMatcher.route(context, AssignmentFragment.makeRoute(toDo.canvasContext, toDo.assignment))
        } else if (toDo?.scheduleItem != null) {
            // It's a Calendar event from the Upcoming API.
            RouteMatcher.route(context, CalendarEventFragment.makeRoute(toDo.canvasContext, toDo.scheduleItem))
        }
    }

    companion object {

        fun makeRoute(canvasContext: CanvasContext): Route {
            return Route(ToDoListFragment::class.java, canvasContext, Bundle())
        }

        private fun validateRoute(route: Route) = route.canvasContext != null

        fun newInstance(route: Route): ToDoListFragment? {
            if (!validateRoute(route)) return null
            return ToDoListFragment().withArgs(route.canvasContext!!.makeBundle())
        }

    }
}
