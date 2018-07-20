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

package com.instructure.parentapp.fragments

import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.View

import com.instructure.canvasapi2.apis.AlertAPI
import com.instructure.canvasapi2.models.Alert
import com.instructure.canvasapi2.models.ObserverAlert
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandarecycler.decorations.SpacesItemDecoration
import com.instructure.pandautils.fragments.BaseSyncFragment
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.parentapp.R
import com.instructure.parentapp.activity.StudentViewActivity
import com.instructure.parentapp.adapter.AlertListRecyclerAdapter
import com.instructure.parentapp.factorys.AlertPresenterFactory
import com.instructure.parentapp.holders.AlertViewHolder
import com.instructure.parentapp.interfaces.AdapterToFragmentBadgeCallback
import com.instructure.parentapp.presenters.AlertPresenter
import com.instructure.parentapp.util.AnalyticUtils
import com.instructure.parentapp.util.ApplicationManager
import com.instructure.parentapp.util.ParentPrefs
import com.instructure.parentapp.util.RecyclerViewUtils
import com.instructure.parentapp.util.RouterUtils
import com.instructure.parentapp.viewinterface.AlertView

import instructure.androidblueprint.PresenterFactory
import kotlinx.android.synthetic.main.recycler_swipe_refresh_layout.*
import kotlinx.android.synthetic.main.recycler_swipe_refresh_layout.recyclerView as recycler


class AlertFragment : BaseSyncFragment<ObserverAlert, AlertPresenter, AlertView, AlertViewHolder, AlertListRecyclerAdapter>(), AlertView {

    private var student: User by ParcelableArg(key = Const.STUDENT)
    private var forceNetwork: Boolean = false

    private val mAdapterToFragmentCallback = AdapterToFragmentBadgeCallback<ObserverAlert> { it, position, _ ->
        //open various detail views depending on alert
        //If the alert is a course grade alert, we don't want to route the user
        if (it.alertType != Alert.alertTypeToAPIString(Alert.ALERT_TYPE.COURSE_GRADE_HIGH) && it.alertType != Alert.alertTypeToAPIString(Alert.ALERT_TYPE.COURSE_GRADE_LOW)) {
            AnalyticUtils.trackFlow(AnalyticUtils.ALERT_FLOW, AnalyticUtils.ALERT_ITEM_SELECTED)

            //note: student is only utilized for assignment routes
            val student = (activity as StudentViewActivity).currentStudent

            // if it's an institution announcement we need to construct the url
            if (it.alertType == Alert.alertTypeToAPIString(Alert.ALERT_TYPE.INSTITUTION_ANNOUNCEMENT)) {
                onRefreshStarted()
                var url = ApiPrefs.fullDomain + "/accounts/self/users/" + student.id + "/account_notifications/" + it.contextId
                RouterUtils.routeUrl(activity, url, student, ApiPrefs.domain, true)
                onRefreshFinished()
            } else {
                RouterUtils.routeUrl(activity, it.htmlUrl, student, ApiPrefs.domain, true)
            }
        }

        //the student should be set in the adapter
        presenter.updateAlert(it.id, AlertAPI.ALERT_READ, position)
    }

    val mAdapterItemDismissedCallback = object : AlertListRecyclerAdapter.ItemDismissedInterface {
        override fun itemDismissed(item: ObserverAlert, holder: AlertViewHolder) {
            AnalyticUtils.trackButtonPressed(AnalyticUtils.DISMISS_ALERT)

            adapter.remove(item)

            presenter.updateAlert(item.id, AlertAPI.ALERT_DISMISSED, 0)
            if (!item.isMarkedRead()) {
                presenter.updateUnreadCount()
            }
            // update the alerts, also updates the cache
            presenter.refresh(true)
        }
    }

    fun setColor(color: Int) {
        swipeRefreshLayout.setColorSchemeColors(color, color, color, color)
        emptyPandaView.progressBar.indeterminateDrawable.setColorFilter(color, PorterDuff.Mode.SRC_IN)
    }

    override fun onCreateView(view: View?) = Unit

    override fun layoutResId(): Int {
        return R.layout.fragment_alert
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        addSwipeToRefresh(swipeRefreshLayout)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        student = arguments.getParcelable(Const.STUDENT)
    }

    fun refreshWithStudent(student: User, refresh: Boolean) {
        if (presenter != null) {
            arguments.putParcelable(Const.STUDENT, student)
            presenter.setStudent(student, refresh)
        }
    }

    override fun airwolfDomain(): String {
        return ApiPrefs.airwolfDomain
    }

    override fun parentId(): String {
        return ApplicationManager.getParentId(context)
    }

    override fun onReadySetGo(presenter: AlertPresenter) {
        recyclerView.adapter = adapter
        presenter.loadData(forceNetwork)
        setColor(ParentPrefs.currentColor)
    }

    override fun getPresenterFactory(): PresenterFactory<AlertPresenter> {
        return AlertPresenterFactory(student)
    }

    override fun onPresenterPrepared(presenter: AlertPresenter) {
        RecyclerViewUtils.buildRecyclerView(mRootView, context, adapter,
                presenter, R.id.swipeRefreshLayout, R.id.recyclerView, R.id.emptyPandaView, getString(R.string.noAlerts))
        recyclerView.addItemDecoration(SpacesItemDecoration(context, R.dimen.med_padding))
        addSwipeToRefresh(swipeRefreshLayout)
        addPagination()
    }

    override fun getAdapter(): AlertListRecyclerAdapter {
        if (mAdapter == null) {
            mAdapter = AlertListRecyclerAdapter(activity, presenter, mAdapterToFragmentCallback, mAdapterItemDismissedCallback)
        }
        return mAdapter
    }

    override fun withPagination(): Boolean {
        return true
    }

    override fun getRecyclerView() : RecyclerView = recycler

    override fun perPageCount(): Int {
        return ApiPrefs.perPageCount
    }

    override fun onRefreshStarted() {
        emptyPandaView.setLoading()
    }

    override fun onRefreshFinished() {
        swipeRefreshLayout.isRefreshing = false
    }

    override fun checkIfEmpty() {
        RecyclerViewUtils.checkIfEmpty(emptyPandaView, recyclerView, swipeRefreshLayout, adapter, presenter.isEmpty)
    }

    override fun markPositionAsRead(position: Int) {
        val itemCount = adapter.itemCount

        if (position < itemCount) {
            val alert = adapter.getItemAtPosition(position)
            if (alert != null) {
                alert.workflowState = AlertAPI.ALERT_READ
                adapter.notifyItemChanged(position)
                forceNetwork = true
            }
        }
        presenter.updateUnreadCount()
    }


    override fun onUpdateUnreadCount(unreadCount: Int) {
        if (activity is StudentViewActivity) {
            (activity as StudentViewActivity).updateAlertUnreadCount(unreadCount)
        }
    }

    companion object {

        fun newInstance(student: User): AlertFragment {
            val args = Bundle()
            args.putParcelable(Const.STUDENT, student)
            val fragment = AlertFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
