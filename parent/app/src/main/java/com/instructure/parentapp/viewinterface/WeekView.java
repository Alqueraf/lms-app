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

package com.instructure.parentapp.viewinterface;

import com.instructure.canvasapi2.models.ScheduleItem;
import com.instructure.parentapp.models.WeekHeaderItem;

import java.util.ArrayList;
import java.util.GregorianCalendar;

import instructure.androidblueprint.SyncExpandableManager;
import instructure.androidblueprint.SyncManager;

/**
 * Copyright (c) 2016 Instructure. All rights reserved.
 */

public interface WeekView extends SyncExpandableManager<WeekHeaderItem, ScheduleItem> {
    void updateWeekText(ArrayList<GregorianCalendar> dates);
    String airwolfDomain();
}
