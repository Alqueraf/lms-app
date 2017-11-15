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
 */
package com.instructure.teacher.fragments

import android.content.Context
import android.net.Uri
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.MenuItem
import android.view.View
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.APIHelper
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.fragments.BaseSyncFragment
import com.instructure.pandautils.utils.*
import com.instructure.teacher.R
import com.instructure.teacher.adapters.CoursesAdapter
import com.instructure.teacher.decorations.VerticalGridSpacingDecoration
import com.instructure.teacher.dialog.NoInternetConnectionDialog
import com.instructure.teacher.events.CourseUpdatedEvent
import com.instructure.teacher.factory.CoursesPresenterFactory
import com.instructure.teacher.holders.CoursesViewHolder
import com.instructure.teacher.presenters.CoursesPresenter
import com.instructure.teacher.utils.*
import com.instructure.teacher.utils.AppType
import com.instructure.teacher.viewinterface.CoursesView
import kotlinx.android.synthetic.main.fragment_courses.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class CoursesFragment : BaseSyncFragment<Course, CoursesPresenter, CoursesView, CoursesViewHolder, CoursesAdapter>(), CoursesView {

    // The user type, used when filtering the course list
    private var mAppType: AppType by SerializableArg(default = AppType.TEACHER)

    lateinit private var mGridLayoutManager: GridLayoutManager
    lateinit private var mDecorator: VerticalGridSpacingDecoration

    // Activity callbacks
    private var mCourseListCallback: CourseListCallback? = null
    private var mCourseBrowserCallback: AllCoursesFragment.CourseBrowserCallback? = null
    private var mNeedToForceNetwork = false

    override fun layoutResId() = R.layout.fragment_courses
    override fun getList() = presenter.data
    override fun getRecyclerView(): RecyclerView = courseRecyclerView
    override fun perPageCount() = ApiPrefs.perPageCount
    override fun withPagination() = false

    override fun getPresenterFactory() = CoursesPresenterFactory {
        when (mAppType) {
            AppType.TEACHER -> it.isTeacher || it.isTA || it.isDesigner
            AppType.STUDENT -> it.isStudent
            AppType.PARENT -> it.isObserver
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is CourseListCallback) mCourseListCallback = context
        if (context is AllCoursesFragment.CourseBrowserCallback) mCourseBrowserCallback = context
    }

    override fun onDetach() {
        super.onDetach()
        mCourseListCallback = null
        mCourseBrowserCallback = null
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onCreateView(view: View) {
        val spanSize = context.resources.getInteger(R.integer.course_list_span_count)
        mGridLayoutManager = GridLayoutManager(context, spanSize)
        mDecorator = VerticalGridSpacingDecoration(context, mGridLayoutManager, true, headerSpacingResId = R.dimen.course_header_spacing)
    }

    override fun onPresenterPrepared(presenter: CoursesPresenter) {
        swipeRefreshLayout.setOnRefreshListener {
            if (!Utils.isNetworkAvailable(context)) {
                swipeRefreshLayout.isRefreshing = false
            } else {
                presenter.refresh(true)
            }
        }

        courseRecyclerView.layoutManager = mGridLayoutManager
        courseRecyclerView.removeItemDecoration(mDecorator) // Remove existing decorator
        courseRecyclerView.addItemDecoration(mDecorator)
        addSwipeToRefresh(swipeRefreshLayout)

        // Set up RecyclerView padding
        val padding = resources.getDimensionPixelSize(R.dimen.course_list_padding)
        val paddingTop = resources.getDimensionPixelSize(R.dimen.course_list_top_padding)
        courseRecyclerView.setPaddingRelative(padding, paddingTop, padding, padding)
        courseRecyclerView.clipToPadding = false

        emptyCoursesView.onClickAddCourses { editFavorites() }

        setupHeader()
    }

    override fun onReadySetGo(presenter: CoursesPresenter) {
        setupToolbar()
        if(courseRecyclerView.adapter == null) {
            courseRecyclerView.adapter = adapter
        }
        presenter.loadData(mNeedToForceNetwork)
        mNeedToForceNetwork  = false
    }

    private fun editFavorites() {
        if(APIHelper.hasNetworkConnection()) {
            mCourseListCallback?.onShowEditFavoritesList()
        } else {
            NoInternetConnectionDialog.show(fragmentManager)
        }
    }

    private fun setupHeader() {
        courseLabel.setTextColor(ThemePrefs.fontColor)
        seeAllTextView.setTextColor(ThemePrefs.buttonColor)
        seeAllTextView.setOnClickListener { mCourseListCallback?.onShowAllCoursesList() }
    }

    private fun setupToolbar() {
        titleTextView.adoptToolbarStyle(toolbar)
        logoImageView.loadUri(Uri.parse(ThemePrefs.logoUrl), R.mipmap.canvas_logo_white)
        toolbar.setupMenu(R.menu.courses_fragment, menuItemCallback)
        ViewStyler.themeToolbar(activity, toolbar, ThemePrefs.primaryColor, ThemePrefs.primaryTextColor)
        toolbar.requestAccessibilityFocus()
    }

    val menuItemCallback: (MenuItem) -> Unit = { item ->
        when (item.itemId) {
            R.id.menu_edit_favorite_courses -> editFavorites()
        }
    }

    override fun getAdapter(): CoursesAdapter {
        if (mAdapter == null) {
            mAdapter = CoursesAdapter(activity, presenter, mCourseBrowserCallback)
        }
        return mAdapter
    }

    override fun onRefreshStarted() {
        //this prevents two loading spinners from happening during pull to refresh
        if(!swipeRefreshLayout.isRefreshing) {
            emptyCoursesView.visibility  = View.VISIBLE
        }
        emptyCoursesView.setLoading()
    }

    override fun onRefreshFinished() {
        swipeRefreshLayout.isRefreshing = false
    }

    override fun checkIfEmpty() {
        RecyclerViewUtils.checkIfEmpty(emptyCoursesView, courseRecyclerView, swipeRefreshLayout, adapter, presenter.isEmpty)
    }

    interface CourseListCallback {
        fun onShowAllCoursesList()
        fun onShowEditFavoritesList()
    }

    companion object {
        @JvmStatic
        fun getInstance(type: AppType) = CoursesFragment().apply { mAppType = type }
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onCourseEdited(event: CourseUpdatedEvent) {
        event.once(javaClass.simpleName) {
            // need to set a flag here. Because we use the event bus in the fragment instead of the presenter for unit testing purposes,
            // when we come back to this fragment it will go through the life cycle events again and the cached data will immediately
            // overwrite the data from the network if we refresh the presenter from here.
            if (isResumed) {
                presenter.loadData(true)
            } else {
                mNeedToForceNetwork = true
            }
        }
    }
}
