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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.instructure.candroid.R
import com.instructure.candroid.adapter.ModuleListRecyclerAdapter
import com.instructure.candroid.events.ModuleUpdatedEvent
import com.instructure.candroid.interfaces.ModuleAdapterToFragmentCallback
import com.instructure.candroid.router.RouteMatcher

import com.instructure.candroid.util.ModuleProgressionUtility
import com.instructure.candroid.util.ModuleUtility
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.interactions.bookmarks.Bookmarkable
import com.instructure.interactions.bookmarks.Bookmarker
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouterParams
import com.instructure.pandautils.utils.*
import com.instructure.pandautils.utils.ViewStyler
import kotlinx.android.synthetic.main.fragment_module_list.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

import java.util.ArrayList

@PageView(url = "modules")
class ModuleListFragment : ParentFragment(), Bookmarkable {
    private var canvasContext: CanvasContext by ParcelableArg(key = Const.CANVAS_CONTEXT)

    private lateinit var recyclerAdapter: ModuleListRecyclerAdapter

    val tabId: String
        get() = Tab.MODULES_ID

    //region Fragment Lifecycle Overrides

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View?
            = layoutInflater.inflate(R.layout.fragment_module_list, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setupViews()
    }

    //endregion

    //region Fragment Interaction Overrides
    override fun title(): String = getString(R.string.modules)

    override fun applyTheme() {
        toolbar.title = title()
        toolbar.setupAsBackButton(this)
        setupToolbarMenu(toolbar)
        ViewStyler.themeToolbar(activity, toolbar, canvasContext)
    }

    //endregion

    override val bookmark: Bookmarker
        get() = Bookmarker(true, canvasContext)

    override fun getSelectedParamName(): String = RouterParams.MODULE_ID

    fun setupViews() {
        recyclerAdapter = ModuleListRecyclerAdapter(canvasContext as Course, -1, context, canvasContext, object : ModuleAdapterToFragmentCallback {
            override fun onRowClicked(moduleObject: ModuleObject, moduleItem: ModuleItem, position: Int, isOpenDetail: Boolean) {
                if (moduleItem.type != null && moduleItem.type == ModuleObject.STATE.unlock_requirements.toString()) {
                    return
                }

                // Don't do anything with headers if the user selects it
                if (moduleItem.type != null && moduleItem.type == ModuleItem.TYPE.SubHeader.toString()) {
                    return
                }

                val isLocked = ModuleUtility.isGroupLocked(moduleObject)
                if (isLocked) {
                    return
                }

                // Remove all the subheaders and stuff.
                val groups = recyclerAdapter.groups

                val moduleItemsArray = groups.indices.mapTo(ArrayList<ArrayList<ModuleItem>>()) { recyclerAdapter.getItems(groups[it]) }
                val moduleHelper = ModuleProgressionUtility.prepareModulesForCourseProgression(context, moduleItem.id, groups, moduleItemsArray)

                RouteMatcher.route(context, CourseModuleProgressionFragment.makeRoute(groups,
                        moduleHelper.strippedModuleItems,
                        canvasContext,
                        moduleHelper.newGroupPosition,
                        moduleHelper.newChildPosition))
            }

            override fun onRefreshFinished() {
                setRefreshing(false)
            }
        })
        configureRecyclerView(view!!, context, recyclerAdapter, R.id.swipeRefreshLayout, R.id.emptyPandaView, R.id.listView)
    }

    fun notifyOfItemChanged(`object`: ModuleObject?, item: ModuleItem?) {
        if (item == null || `object` == null) {
            return
        }

        recyclerAdapter.addOrUpdateItem(`object`, item)
    }

    fun refreshModuleList() = recyclerAdapter.updateMasteryPathItems()

    /**
     * Update the list without clearing the data or collapsing headers. Used to update possibly updated
     * items (like a page that has now been viewed)
     */
    private fun updateList(moduleObject: ModuleObject) = recyclerAdapter.updateWithoutResettingViews(moduleObject)


    // region Bus Events
    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onModuleUpdated(event: ModuleUpdatedEvent) {
        event.once(javaClass.simpleName, {
            updateList(it)
            recyclerAdapter.notifyDataSetChanged()
        })
    }
    // endregion

    companion object {

        fun newInstance(route: Route) =
                if (validateRoute(route)) {
                    ModuleListFragment().apply {
                        arguments = route.canvasContext!!.makeBundle(route.arguments)
                    }
                } else null

        fun makeRoute(canvasContext: CanvasContext?) =
                Route(ModuleListFragment::class.java, canvasContext)

        private fun validateRoute(route: Route) = route.canvasContext != null
    }
}
