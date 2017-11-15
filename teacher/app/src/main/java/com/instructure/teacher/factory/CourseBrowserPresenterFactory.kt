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
package com.instructure.teacher.factory

import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Tab
import com.instructure.teacher.presenters.CourseBrowserPresenter
import instructure.androidblueprint.PresenterFactory

class CourseBrowserPresenterFactory(val course: Course, val filter: (Tab, Int) -> Boolean) : PresenterFactory<CourseBrowserPresenter> {
    override fun create(): CourseBrowserPresenter {
        return CourseBrowserPresenter(course, filter)
    }
}
