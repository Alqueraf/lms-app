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
 */
package com.instructure.teacher.ui.utils

import android.support.test.espresso.Espresso
import android.support.test.espresso.ViewInteraction
import android.support.test.espresso.matcher.ViewMatchers
import android.view.View
import com.instructure.espresso.ViewInteractionDelegate
import com.instructure.teacher.R
import org.hamcrest.Matcher
import org.hamcrest.Matchers

/**
 *  The toolbar's title text view's resource id is the same as the course text view in course cards.
 *  Use this to narrow the matcher to the toolbar itself.
 */
class WaitForToolbarTitle(val text: Int, autoAssert: Boolean = true) : ViewInteractionDelegate(autoAssert) {
    override fun onProvide(matcher: Matcher<View>): ViewInteraction = Espresso.onView(matcher)
    override fun getMatcher(): Matcher<View> {
        return Matchers.allOf(ViewMatchers.withText(text), ViewMatchers.withParent(ViewMatchers.withId(R.id.toolbar)))
    }
}
