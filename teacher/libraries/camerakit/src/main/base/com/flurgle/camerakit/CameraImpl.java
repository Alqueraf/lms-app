/*
MIT License

Copyright (c) 2016 WonderKiln

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package com.flurgle.camerakit;

import android.support.annotation.Nullable;

abstract class CameraImpl {

    protected final CameraListener mCameraListener;
    protected final PreviewImpl mPreview;

    CameraImpl(CameraListener callback, PreviewImpl preview) {
        mCameraListener = callback;
        mPreview = preview;
    }

    abstract void start();
    abstract void stop();

    abstract void setDisplayOrientation(int displayOrientation, int deviceOrientation);

    abstract void setFacing(@Facing int facing);
    abstract void setFlash(@Flash int flash);
    abstract void setFocus(@Focus int focus);
    abstract void setMethod(@Method int method);
    abstract void setZoom(@Zoom int zoom);
    abstract void setVideoQuality(@VideoQuality int videoQuality);

    abstract void captureImage();
    abstract void startVideo();
    abstract void endVideo();

    @Nullable abstract Size getCaptureResolution();
    @Nullable abstract Size getPreviewResolution();
    abstract boolean isCameraOpened();
    abstract boolean frontCameraOnly();

    @Nullable
    abstract CameraProperties getCameraProperties();

}
