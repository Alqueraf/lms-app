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

package com.instructure.parentapp.holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.instructure.parentapp.R;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Copyright (c) 2016 Instructure. All rights reserved.
 */

public class SettingsViewHolder extends RecyclerView.ViewHolder {

    public TextView name;
    public CircleImageView avatar;

    public SettingsViewHolder(View itemView) {
        super(itemView);
        name = (TextView)itemView.findViewById(R.id.name);
        avatar = (CircleImageView) itemView.findViewById(R.id.avatar);
    }

    public static int holderResId() {
        return R.layout.viewholder_observee;
    }
}
