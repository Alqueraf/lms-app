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

package com.instructure.teacher.decorations;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.DimenRes;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class NonuniformSpacesItemDecoration extends RecyclerView.ItemDecoration {

    private @DimenRes int horizontalSpace;
    private @DimenRes int verticalSpace;

    public NonuniformSpacesItemDecoration(Context context, @DimenRes int horizontalSpaceResId, @DimenRes int verticalSpaceResId) {
        this.verticalSpace = context.getResources().getDimensionPixelOffset(verticalSpaceResId);
        this.horizontalSpace = context.getResources().getDimensionPixelOffset(horizontalSpaceResId);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.left = horizontalSpace;
        outRect.right = horizontalSpace;
        outRect.bottom = verticalSpace;

        if(parent.getChildAdapterPosition(view) == 0)
            outRect.top = verticalSpace;
    }
}
