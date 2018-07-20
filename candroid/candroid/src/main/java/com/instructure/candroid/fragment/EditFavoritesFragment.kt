/*
 * Copyright (C) 2017 - present Instructure, Inc.
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

import android.app.DialogFragment
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.instructure.candroid.R
import com.instructure.candroid.adapter.EditFavoritesRecyclerAdapter
import com.instructure.candroid.interfaces.AdapterToFragmentCallback
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.managers.GroupManager
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.weave.WeaveJob
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.interactions.router.Route
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.isTablet
import com.instructure.pandautils.utils.setupAsBackButton
import kotlinx.android.synthetic.main.fragment_favoriting.*
import kotlinx.android.synthetic.main.recycler_swipe_refresh_layout.*

@PageView(url = "courses")
class EditFavoritesFragment : ParentFragment() {

    private var mRecyclerAdapter: EditFavoritesRecyclerAdapter? = null
    private var mHasChanges = false
    private var mCourseCall: WeaveJob? = null
    private var mGroupCall: WeaveJob? = null

    override fun title(): String = context.getString(R.string.editFavorites)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isTablet) setStyle(DialogFragment.STYLE_NORMAL, R.style.LightStatusBarDialog)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            layoutInflater.inflate(R.layout.fragment_favoriting, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        applyTheme()
        mRecyclerAdapter = EditFavoritesRecyclerAdapter(activity, object : AdapterToFragmentCallback<CanvasComparable<*>> {
            override fun onRefreshFinished() {
                setRefreshing(false)
            }

            override fun onRowClicked(canvasContext: CanvasComparable<*>, position: Int, isOpenDetail: Boolean) {
                mHasChanges = true
                when(canvasContext) {
                    is Course -> updateCourseFavorite(canvasContext)
                    is Group -> updateGroupFavorite(canvasContext)
                }
            }
        })
        configureRecyclerView(view!!, context, mRecyclerAdapter!!, R.id.swipeRefreshLayout, R.id.emptyPandaView, R.id.listView, R.string.no_courses_available)
        listView.isSelectionEnabled = false
    }

    override fun applyTheme() {
        toolbar.setTitle(R.string.editFavorites)
        toolbar.setupAsBackButton(this)
        ViewStyler.themeToolbar(activity, toolbar, Color.WHITE, Color.BLACK, false)
    }

    private fun updateCourseFavorite(course: Course) {
        mCourseCall?.cancel()
        course.isFavorite = !course.isFavorite
        mRecyclerAdapter?.addOrUpdateItem(EditFavoritesRecyclerAdapter.ItemType.COURSE_HEADER,course)
        mCourseCall = tryWeave {
            awaitApi<Favorite> {
                if (course.isFavorite) CourseManager.addCourseToFavorites(course.id, it, true)
                else CourseManager.removeCourseFromFavorites(course.id, it, true)
            }
        } catch {
            course.isFavorite = !course.isFavorite
            mRecyclerAdapter?.addOrUpdateItem(EditFavoritesRecyclerAdapter.ItemType.COURSE_HEADER,course)
        }
    }

    private fun updateGroupFavorite(group: Group) {
        mGroupCall?.cancel()
        group.isFavorite = !group.isFavorite
        mRecyclerAdapter?.addOrUpdateItem(EditFavoritesRecyclerAdapter.ItemType.GROUP_HEADER,group)
        mGroupCall = tryWeave {
            awaitApi<Favorite> {
                if (group.isFavorite) GroupManager.addGroupToFavorites(group.id, it)
                else GroupManager.removeGroupFromFavorites(group.id, it)
            }
        } catch {
            group.isFavorite = !group.isFavorite
            mRecyclerAdapter?.addOrUpdateItem(EditFavoritesRecyclerAdapter.ItemType.GROUP_HEADER,group)
        }
    }

    override fun onStart() {
        super.onStart()
        if (!isTablet && dialog != null) {
            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }
    }

    override fun onStop() {
        super.onStop()
        mCourseCall?.cancel()
        mGroupCall?.cancel()
        mRecyclerAdapter?.cancel()
        if (mHasChanges) {
            val intent = Intent(Const.COURSE_THING_CHANGED)
            intent.putExtras(Bundle().apply { putBoolean(Const.COURSE_FAVORITES, true) })
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
        }
    }

    companion object {

        fun makeRoute() = Route(EditFavoritesFragment::class.java, null)

        fun validRoute(route: Route) = route.primaryClass == EditFavoritesFragment::class.java

        fun newInstance(route: Route): EditFavoritesFragment? {
            if (!validRoute(route)) return null
            return EditFavoritesFragment()
        }

    }
}
