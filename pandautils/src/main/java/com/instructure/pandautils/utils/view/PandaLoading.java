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
 *
 */

package com.instructure.pandautils.utils.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.instructure.pandautils.R;


public class PandaLoading extends ImageView {

    public PandaLoading(Context context) {
        super(context);
        setupPandaLoading();
    }

    public PandaLoading(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupPandaLoading();
    }

    public PandaLoading(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setupPandaLoading();
    }

    @TargetApi(21)
    public PandaLoading(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setupPandaLoading();
    }

    private void setupPandaLoading() {
        setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.panda_loading));
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        // Start the animation (looped playback by default).
        AnimationDrawable frameAnimation = (AnimationDrawable) getBackground();
        frameAnimation.start();
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
    }
}
