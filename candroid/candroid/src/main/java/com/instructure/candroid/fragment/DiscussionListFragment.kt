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

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.instructure.candroid.R
import com.instructure.candroid.activity.NavigationActivity
import com.instructure.candroid.adapter.DiscussionListRecyclerAdapter
import com.instructure.candroid.events.DiscussionCreatedEvent
import com.instructure.candroid.events.DiscussionTopicHeaderDeletedEvent
import com.instructure.candroid.events.DiscussionTopicHeaderEvent
import com.instructure.candroid.events.DiscussionUpdatedEvent
import com.instructure.candroid.router.RouteMatcher
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.managers.GroupManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.utils.Logger
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.interactions.bookmarks.Bookmarkable
import com.instructure.interactions.bookmarks.Bookmarker
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouterParams
import com.instructure.pandautils.utils.*
import kotlinx.android.synthetic.main.course_discussion_topic.*
import kotlinx.coroutines.experimental.Job
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@PageView(url = "{canvasContext}/discussion_topics")
open class DiscussionListFragment : ParentFragment(), Bookmarkable {

    protected var canvasContext: CanvasContext by ParcelableArg(key = Const.CANVAS_CONTEXT)

    private lateinit var recyclerAdapter: DiscussionListRecyclerAdapter

    private var linearLayoutManager = LinearLayoutManager(context)
    private lateinit var discussionRecyclerView: RecyclerView
    private lateinit var rootView: View
    private var permissionJob: Job? = null
    private var canPost: Boolean = false

    protected open val isAnnouncement: Boolean
        get() = false

    //region Fragment Lifecycle Overrides
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkForPermission()
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        rootView = layoutInflater.inflate(R.layout.course_discussion_topic, container, false)
        recyclerAdapter = DiscussionListRecyclerAdapter(context, canvasContext, !isAnnouncement, object: DiscussionListRecyclerAdapter.AdapterToDiscussionsCallback{
            override fun onRowClicked(model: DiscussionTopicHeader, position: Int, isOpenDetail: Boolean) {
                // If the discussion/announcement hasn't been published take them back to the publish screen
                RouteMatcher.route(activity, DiscussionDetailsFragment.makeRoute(canvasContext, model))
            }

            override fun onRefreshFinished() {
                setRefreshing(false)
                // Show the FAB.
                if(canPost) {
                    createNewDiscussion?.show()
                }
            }

            override fun onRefreshStarted() {
                setRefreshing(true)
                // Hide the FAB.
                if(canPost) {
                    createNewDiscussion?.hide()
                }
            }

            override fun discussionOverflow(group: String?, discussionTopicHeader: DiscussionTopicHeader) {
                if(group != null) {
                    // TODO - Blocked by COMMS-868
//                    DiscussionsMoveToDialog.show(fragmentManager, group, discussionTopicHeader, { newGroup ->
//                        recyclerAdapter.requestMoveDiscussionTopicToGroup(newGroup, group, discussionTopicHeader)
//                    })
                }
            }

            override fun askToDeleteDiscussion(discussionTopicHeader: DiscussionTopicHeader) {
                AlertDialog.Builder(context)
                        .setTitle(R.string.utils_discussionsDeleteTitle)
                        .setMessage(R.string.utils_discussionsDeleteMessage)
                        .setPositiveButton(R.string.delete, { _, _ ->
                            recyclerAdapter.deleteDiscussionTopicHeader(discussionTopicHeader)
                        })
                        .setNegativeButton(R.string.cancel, { _, _ -> })
                        .showThemed()
            }
        })

        discussionRecyclerView = configureRecyclerView(rootView, context, recyclerAdapter, R.id.swipeRefreshLayout, R.id.emptyPandaView, R.id.discussionRecyclerView)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL

        discussionRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if(canPost) {
                    if (dy > 0 && createNewDiscussion.visibility == View.VISIBLE) {
                        createNewDiscussion?.hide()
                    } else if (dy < 0 && createNewDiscussion.visibility != View.VISIBLE) {
                        createNewDiscussion?.show()
                    }
                }
            }
        })

        return rootView
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        createNewDiscussion.setGone()
        createNewDiscussion.backgroundTintList = ViewStyler.makeColorStateListForButton()
        createNewDiscussion.setImageDrawable(ColorUtils.colorIt(ThemePrefs.buttonTextColor, createNewDiscussion.drawable))
        createNewDiscussion.onClickWithRequireNetwork {
            if(isAnnouncement) {
                val route = CreateAnnouncementFragment.makeRoute(canvasContext, null)
                RouteMatcher.route(activity, route)
            } else {
                val route = CreateDiscussionFragment.makeRoute(canvasContext)
                RouteMatcher.route(activity, route)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        permissionJob?.cancel()
    }
    //endregion

    override val bookmark: Bookmarker
        get() = Bookmarker(true, canvasContext)

    //region Fragment Interaction Overrides

    override fun applyTheme() {
        setupToolbarMenu(discussionListToolbar)
        discussionListToolbar.title = title()
        discussionListToolbar.setupAsBackButton(this)
        ViewStyler.themeToolbar(activity, discussionListToolbar, canvasContext)
    }

    override fun title(): String = getString(R.string.discussion)
    //endregion

    //region Parent Fragment Overrides
    override fun getSelectedParamName(): String = RouterParams.MESSAGE_ID

    //endregion

    private fun checkForPermission() {
        permissionJob = tryWeave {
            val permission = if(canvasContext.isCourse) {
                awaitApi<Course> { CourseManager.getCourse(
                        canvasContext.id,
                        it,
                        true
                )}.permissions
            } else {
                awaitApi<Group> { GroupManager.getDetailedGroup(
                        canvasContext.id,
                        it,
                        true
                )}.permissions
            }

            this@DiscussionListFragment.canvasContext.permissions = permission
            canPost = if(isAnnouncement) {
                permission?.canCreateAnnouncement ?: false
            } else {
                permission?.canCreateDiscussionTopic ?: false
            }
            if(canPost) {
                createNewDiscussion?.show()
            }
        } catch {
            Logger.e("Error getting permissions for discussion permissions. " + it.message)
            createNewDiscussion?.hide()
        }
    }

    //region Bus Events
    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onDiscussionUpdated(event: DiscussionUpdatedEvent) {
        event.once(javaClass.simpleName) {
            recyclerAdapter.refresh()
        }
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onDiscussionTopicHeaderDeleted(event: DiscussionTopicHeaderDeletedEvent) {
        event.get {
            // TODO - COMMS-868
        }
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onDiscussionTopicCountChange(event: DiscussionTopicHeaderEvent) {
        event.get {
            // Gets written over on phones - added also to {@link #onRefreshFinished()}
            when {
                it.isPinned -> {
                    recyclerAdapter.addOrUpdateItem(DiscussionListRecyclerAdapter.PINNED, it)
                }
                it.isLocked -> {
                    recyclerAdapter.addOrUpdateItem(DiscussionListRecyclerAdapter.CLOSED_FOR_COMMENTS, it)
                }
                else -> {
                    recyclerAdapter.addOrUpdateItem(DiscussionListRecyclerAdapter.UNPINNED, it)
                }
            }
        }
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onDiscussionCreated(event: DiscussionCreatedEvent) {
        event.once(javaClass.simpleName) {
            recyclerAdapter.refresh()
        }
    }
    //endregion

    companion object {
        fun newInstance(route: Route) =
                if (validateRoute(route)) {
                    DiscussionListFragment().apply {
                        arguments = route.canvasContext!!.makeBundle(route.arguments)
                    }
                } else null

        fun makeRoute(canvasContext: CanvasContext?) =
                Route(DiscussionListFragment::class.java, canvasContext)

        private fun validateRoute(route: Route) = route.canvasContext != null
    }
}
