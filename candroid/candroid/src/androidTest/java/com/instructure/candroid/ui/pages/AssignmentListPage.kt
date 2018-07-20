/*
 * Copyright (C) 2018 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */
package com.instructure.candroid.ui.pages

import com.instructure.candroid.R
import com.instructure.espresso.*
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.waitForViewWithText
import com.instructure.soseedy.Assignment

class AssignmentListPage : BasePage(pageResId = R.id.assignmentListPage) {

    private val assignmentListToolbar by OnViewWithId(R.id.toolbar)

    // Only displayed when assignment list is empty
    private val emptyPandaView by WaitForViewWithId(R.id.emptyPandaView, autoAssert = false)

    // Only displayed when there are grading periods
    private val gradingPeriodHeader by WaitForViewWithId(R.id.termSpinnerLayout, autoAssert = false)

    // Only displayed when there are no assignments
    private val emptyText by WaitForViewWithText(R.string.noItemsToDisplayShort, autoAssert = false)

    fun clickAssignment(assignment: Assignment) {
        waitForViewWithText(assignment.name).click()
    }

    fun assertDisplaysNoAssignmentsView() {
        emptyPandaView.assertDisplayed()
        emptyText.assertDisplayed()
    }

    fun assertHasAssignment(assignment: Assignment) {
        waitForViewWithText(assignment.name).assertDisplayed()
    }

    fun assertHasGradingPeriods() {
        gradingPeriodHeader.assertDisplayed()
    }
}
