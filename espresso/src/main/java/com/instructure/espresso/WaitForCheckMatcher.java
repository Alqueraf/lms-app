/*
 * Copyright (C) 2017 - present Instructure, Inc.
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

package com.instructure.espresso;

import android.support.test.espresso.UiController;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static android.support.test.espresso.core.internal.deps.guava.base.Preconditions.checkNotNull;

public class WaitForCheckMatcher<T> extends BaseMatcher<T> {
    private static final EspressoLog log = new EspressoLog(WaitForCheckMatcher.class);
    private final Matcher<T> matcher;
    private static final UiController uiController = UiControllerSingleton.get();

    private static final AtomicBoolean waiting = new AtomicBoolean(false);

    public static boolean finishedWaiting() {
        return !waiting.get();
    }

    public WaitForCheckMatcher(Matcher<T> matcher) {
        this.matcher = matcher;
    }

    @Override
    public boolean matches(Object arg) {
        checkNotNull(arg);
        waiting.set(true);

        uiController.loopMainThreadUntilIdle();
        final long waitTime = TimeUnit.SECONDS.toMillis(10);
        final long endTime = System.currentTimeMillis() + waitTime;
        do {
            log.i("waitForCheck matching...");
            try {
                if (matcher.matches(arg)) {
                    waiting.set(false);
                    return true;
                }
            } catch (Exception | Error ignored) {
            }
            uiController.loopMainThreadForAtLeast(100L);
        } while (System.currentTimeMillis() < endTime);

        waiting.set(false);
        return matcher.matches(arg);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("wait ").appendDescriptionOf(matcher);
    }

    // note: due to conflict with Object.wait, we use 'waitFor'
    //       instead of 'wait' to name the static import.

    /**
     * Creates a wait matcher that wraps an existing matcher.
     * The default wait is 10 seconds.
     * <p/>
     * Examples:
     * <pre>
     * onView(withId(...)).check(matches(waitFor(not(isDisplayed()))));
     * onView(withId(...)).check(matches(waitFor(isDisplayed())));
     * </pre>
     *
     * @param matcher the matcher to wrap
     */
    public static <T> Matcher<T> waitFor(Matcher<T> matcher) {
        return new WaitForCheckMatcher<T>(matcher);
    }
}
