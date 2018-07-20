/*
 * Copyright (C) 2018 - present  Instructure, Inc.
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
package com.instructure.candroid.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.instructure.candroid.R
import com.instructure.candroid.adapter.InboxRecipientAdapter
import com.instructure.candroid.events.ChooseRecipientsEvent
import com.instructure.candroid.interfaces.AdapterToFragmentCallback
import com.instructure.candroid.router.RouteMatcher
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Page
import com.instructure.canvasapi2.models.Recipient
import com.instructure.interactions.router.Route
import com.instructure.pandautils.utils.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import org.greenrobot.eventbus.EventBus
import java.util.*

class InboxRecipientsFragment : ParentFragment() {

    override fun title(): String = getString(R.string.selectRecipients)

    override fun applyTheme() {
        (view?.findViewById<View>(R.id.menu_done) as? TextView)?.setTextColor(ThemePrefs.buttonColor)
        ViewStyler.themeToolbarBottomSheet(activity, isTablet, toolbar, Color.BLACK, false)
    }

    private val adapter: InboxRecipientAdapter by lazy {
        InboxRecipientAdapter(
            context,
            arguments.getParcelable(Const.CANVAS_CONTEXT),
            arguments.getParcelableArrayList<Recipient>(RECIPIENT_LIST).toHashSet(),
                 object : AdapterToFragmentCallback<Recipient> {
                     override fun onRowClicked(page: Recipient, position: Int, isOpenDetail: Boolean) { }

                     override fun onRefreshFinished() {
                         setRefreshing(false)
                     }
                 }
        )
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_inbox_recipients, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        configureRecyclerView(view!!, context, adapter, R.id.swipeRefreshLayout, R.id.emptyPandaView, R.id.listView)
    }

    private fun setupToolbar() {
        toolbar.setupAsBackButton(this)
        toolbar.title = title()
        toolbar.inflateMenu(R.menu.menu_done_text)
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_done -> {
                    // Send the recipient list back to the message
                    EventBus.getDefault().postSticky(ChooseRecipientsEvent(adapter.getRecipients(), null))
                    // Clear the back stack because we want to go back to the message, not the previous screen
                    adapter.clearBackStack()
                    activity.onBackPressed()
                }
            }
            false
        }
    }

    override fun handleBackPressed() = adapter.popBackStack()

    companion object {

        private const val RECIPIENT_LIST = "recipient_list"

        fun makeRoute(canvasContext: CanvasContext, addedRecipients: List<Recipient>): Route {
            val bundle = Bundle().apply { putParcelableArrayList(RECIPIENT_LIST, ArrayList(addedRecipients)) }
            return Route(InboxRecipientsFragment::class.java, canvasContext, bundle)
        }

        private fun validateRoute(route: Route): Boolean {
            return route.canvasContext != null && route.arguments.containsKey(RECIPIENT_LIST)
        }

        fun newInstance(route: Route): InboxRecipientsFragment? {
            if (!validateRoute(route)) return null
            return InboxRecipientsFragment().withArgs(route.canvasContext!!.makeBundle(route.arguments))
        }
    }
}
