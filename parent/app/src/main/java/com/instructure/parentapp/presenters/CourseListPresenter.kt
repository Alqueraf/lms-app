/*
 * Copyright (C) 2016 - present  Instructure, Inc.
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
package com.instructure.parentapp.presenters

import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.isValid
import com.instructure.canvasapi2.utils.weave.WeaveJob
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.parentapp.viewinterface.CourseListView
import instructure.androidblueprint.SyncPresenter

class CourseListPresenter(val student: User) : SyncPresenter<Course, CourseListView>(Course::class.java) {

    var courseJob: WeaveJob? = null
    var courses: List<Course>? = null

    override fun loadData(forceNetwork: Boolean) {
        if (courses?.isNotEmpty() != true) getCourses(forceNetwork)
    }

    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    private fun getCourses(forceNetwork: Boolean) {
        viewCallback?.let {
            courseJob = tryWeave {
                courses = awaitApi<List<Course>> { CourseManager.getCoursesWithSyllabus(forceNetwork, it) }
                courses?.filter { it.name.isValid() }?.onEach { course ->
                    // We need to set the course enrollment as the student enrollment that we are observing.
                    course.enrollments
                        .find { it.userId == student.id }
                        ?.let { enrollment ->
                            course.enrollments = listOf(enrollment)
                            data.addOrUpdate(course)
                        }
                }
                it.onRefreshFinished()
                it.checkIfEmpty()
            } catch {
                viewCallback?.onRefreshFinished()
                viewCallback?.checkIfEmpty()
            }

        }
    }

    override fun refresh(forceNetwork: Boolean) {
        onRefreshStarted()
        courseJob?.cancel()
        courses = null
        clearData()
        loadData(forceNetwork)
    }

    public override fun compare(o1: Course, o2: Course) = o1.compareTo(o2)

    public override fun areContentsTheSame(oldItem: Course, newItem: Course) = false

    public override fun areItemsTheSame(item1: Course, item2: Course): Boolean {
        return item1.contextId.hashCode() == item2.contextId.hashCode()
    }

    override fun onDestroyed() {
        super.onDestroyed()
        courseJob?.cancel()
    }
}
