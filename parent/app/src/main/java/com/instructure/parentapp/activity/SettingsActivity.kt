/*
 * Copyright (C) 2017 - present  Instructure, Inc.
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

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandarecycler.decorations.SpacesItemDecoration
import com.instructure.pandarecycler.util.UpdatableSortedList
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.setupAsBackButton
import com.instructure.parentapp.R
import com.instructure.parentapp.adapter.SettingsRecyclerAdapter
import com.instructure.parentapp.factorys.SettingsPresenterFactory
import com.instructure.parentapp.holders.SettingsViewHolder
import com.instructure.parentapp.interfaces.AdapterToFragmentCallback
import com.instructure.parentapp.presenters.SettingsPresenter
import com.instructure.parentapp.util.AnalyticUtils
import com.instructure.parentapp.util.ApplicationManager
import com.instructure.parentapp.util.RecyclerViewUtils
import com.instructure.parentapp.util.ViewUtils
import com.instructure.parentapp.viewinterface.SettingsView
import instructure.androidblueprint.PresenterFactory
import instructure.androidblueprint.SyncActivity
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.recycler_swipe_refresh_layout.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import java.util.*
import kotlinx.android.synthetic.main.recycler_swipe_refresh_layout.recyclerView as recyclerview

class SettingsActivity :
    SyncActivity<User, SettingsPresenter, SettingsView, SettingsViewHolder, SettingsRecyclerAdapter>(),
    SettingsView {

    private val recyclerAdapter: SettingsRecyclerAdapter by lazy {
        SettingsRecyclerAdapter(
            this@SettingsActivity,
            presenter,
            AdapterToFragmentCallback { student, _, _ ->
                startActivityForResult(
                    StudentDetailsActivity.createIntent(this@SettingsActivity, student),
                    com.instructure.parentapp.util.Const.STUDENT_DETAILS_REQUEST_CODE
                )
            })
    }

    override fun getList(): UpdatableSortedList<User> = presenter.data

    override fun onCreate(savedInstanceState: Bundle?) {
        setResult(Activity.RESULT_CANCELED)
        // Make the status bar dark blue
        ViewUtils.setStatusBarColor(this, ContextCompat.getColor(this@SettingsActivity, R.color.colorPrimaryDark))

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setupViews()
    }

    private fun setupViews() {
        toolbar.setupAsBackButton { finish() }
        toolbar.setTitle(R.string.manageChildren)
    }

    public override fun getAdapter(): SettingsRecyclerAdapter = recyclerAdapter


    fun addStudent(students: ArrayList<User>?) {
        if (students != null && !students.isEmpty()) {
            setResult(Activity.RESULT_OK)
            adapter.addAll(students)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == com.instructure.parentapp.util.Const.DOMAIN_PICKER_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val students = data.getParcelableArrayListExtra<User>(Const.STUDENT)
            addStudent(students)
        } else if (requestCode == com.instructure.parentapp.util.Const.STUDENT_LOGIN_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val students = data.getParcelableArrayListExtra<User>(Const.STUDENT)
            addStudent(students)
        }
    }

    override fun airwolfDomain(): String {
        return ApiPrefs.airwolfDomain
    }

    override fun parentId(): String {
        return ApplicationManager.getParentId(this@SettingsActivity)
    }

    override fun hasStudent(hasStudent: Boolean) {
        if (!hasStudent) {
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    override fun needToCheckToken() {
        ApplicationManager.checkTokenValidity(this@SettingsActivity)
    }

    //Sync

    override fun onReadySetGo(presenter: SettingsPresenter) {
        recyclerView.adapter = adapter
        getPresenter().loadData(false)
    }

    override fun getPresenterFactory(): PresenterFactory<SettingsPresenter> {
        return SettingsPresenterFactory()
    }

    override fun onPresenterPrepared(presenter: SettingsPresenter) {
        RecyclerViewUtils.buildRecyclerView(
            this@SettingsActivity, adapter,
            presenter, swipeRefreshLayout, recyclerView, emptyPandaView, getString(R.string.noCourses)
        )
        recyclerView.addItemDecoration(SpacesItemDecoration(this@SettingsActivity, R.dimen.med_padding))
        addSwipeToRefresh(swipeRefreshLayout)
        addPagination()
    }

    override fun getRecyclerView(): RecyclerView = recyclerview

    override fun perPageCount() = ApiPrefs.perPageCount

    override fun onRefreshStarted() {
        emptyPandaView.setLoading()
    }

    override fun onRefreshFinished() {
        swipeRefreshLayout.isRefreshing = false
    }

    override fun checkIfEmpty() {
        RecyclerViewUtils.checkIfEmpty(emptyPandaView, recyclerView, swipeRefreshLayout, adapter, presenter.isEmpty)
    }

    companion object {

        @JvmStatic
        fun createIntent(context: Context, userName: String): Intent {
            val intent = Intent(context, SettingsActivity::class.java)
            intent.putExtra(Const.NAME, userName)
            return intent
        }
    }
}
