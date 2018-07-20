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
import com.instructure.candroid.adapter.QuizListRecyclerAdapter
import com.instructure.candroid.interfaces.AdapterToFragmentCallback
import com.instructure.candroid.router.RouteMatcher
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Quiz
import com.instructure.canvasapi2.models.QuizQuestion.QUESTION_TYPE.*
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.interactions.bookmarks.Bookmarkable
import com.instructure.interactions.bookmarks.Bookmarker
import com.instructure.interactions.router.Route
import com.instructure.pandautils.utils.*
import kotlinx.android.synthetic.main.quiz_list_layout.*

@PageView(url = "{canvasContext}/quizzes")
class QuizListFragment : ParentFragment(), Bookmarkable {

    private var canvasContext by ParcelableArg<CanvasContext>(key = Const.CANVAS_CONTEXT)

    private var mRecyclerAdapter: QuizListRecyclerAdapter? = null

    private var mAdapterToFragmentCallback: AdapterToFragmentCallback<Quiz> = object : AdapterToFragmentCallback<Quiz> {
        override fun onRowClicked(quiz: Quiz, position: Int, isOpenDetail: Boolean) {
            rowClick(quiz)
        }

        override fun onRefreshFinished() {
            setRefreshing(false)
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return layoutInflater.inflate(R.layout.quiz_list_layout, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        mRecyclerAdapter = QuizListRecyclerAdapter(context, canvasContext, mAdapterToFragmentCallback)
        configureRecyclerView(
            view!!,
            context,
            mRecyclerAdapter!!,
            R.id.swipeRefreshLayout,
            R.id.emptyPandaView,
            R.id.listView
        )
    }

    override fun applyTheme() {
        setupToolbarMenu(toolbar)
        toolbar.title = title()
        toolbar.setupAsBackButton(this)
        ViewStyler.themeToolbar(activity, toolbar, canvasContext)
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        configureRecyclerView(
            view!!,
            context,
            mRecyclerAdapter!!,
            R.id.swipeRefreshLayout,
            R.id.emptyPandaView,
            R.id.listView
        )
    }

    override fun title(): String = getString(R.string.quizzes)

    override val bookmark: Bookmarker
        get() = Bookmarker(true, canvasContext)

    private fun rowClick(quiz: Quiz) {
        val navigation = navigation
        if (navigation != null) {
            /* Determine if we support the quiz question types. If not, just show the questions in a WebView.
             if the quiz has an access code we don't currently support that natively on the app, so send them
             to a WebView. Also, we currently don't support one quiz question at a time quizzes. */
            if (!isNativeQuiz(canvasContext, quiz)) {
                // Log to GA, track if they're a teacher (because teachers currently always get the non native quiz)
                RouteMatcher.route(context, BasicQuizViewFragment.makeRoute(canvasContext, quiz, quiz.url))
            } else {
                RouteMatcher.route(context, QuizStartFragment.makeRoute(canvasContext, quiz))
            }
        }
    }

    companion object {

        // Currently supports TRUE_FALSE, ESSAY, SHORT_ANSWER, MULTI_CHOICE
        private val unsupportedTypes = listOf(CALCULATED, FILL_IN_MULTIPLE_BLANKS, UNKNOWN)

        private fun containsUnsupportedQuestionType(quiz: Quiz): Boolean {
            // Loop through all question types. If there is one we don't support or the list is null/empty, return true
            return quiz.parsedQuestionTypes?.takeIf { it.isNotEmpty() }?.any { it in unsupportedTypes } ?: true
        }

        @JvmStatic
        fun isNativeQuiz(canvasContext: CanvasContext, quiz: Quiz): Boolean {
            return !(containsUnsupportedQuestionType(quiz) || quiz.isHasAccessCode || quiz.oneQuestionAtATime || canvasContext is Course && canvasContext.isTeacher)
        }

        fun makeRoute(canvasContext: CanvasContext): Route {
            return Route(QuizListFragment::class.java, canvasContext, Bundle())
        }

        private fun validateRoute(route: Route) = route.canvasContext != null

        fun newInstance(route: Route): QuizListFragment? {
            if (!validateRoute(route)) return null
            return QuizListFragment().withArgs(route.canvasContext!!.makeBundle())
        }
    }
}
