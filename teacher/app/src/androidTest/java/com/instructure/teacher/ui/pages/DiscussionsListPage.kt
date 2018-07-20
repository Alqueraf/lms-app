package com.instructure.teacher.ui.pages

import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.waitForViewWithText
import com.instructure.soseedy.Discussion
import com.instructure.teacher.R

class DiscussionsListPage : BasePage() {

    private val discussionListToolbar by OnViewWithId(R.id.discussionListToolbar)
    private val discussionsFAB by OnViewWithId(R.id.createNewDiscussion)
    private val discussionsRecyclerView by OnViewWithId(R.id.discussionRecyclerView)

    fun clickDiscussion(discussion: Discussion) {
        waitForViewWithText(discussion.title).click()
    }

    fun assertHasDiscussion(discussion: Discussion) {
        waitForViewWithText(discussion.title).assertDisplayed()
    }
}
