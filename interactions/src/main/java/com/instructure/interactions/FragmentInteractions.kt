package com.instructure.interactions

import android.support.v4.app.Fragment

interface FragmentInteractions {

    val navigation: Navigation?

    fun title(): String
    fun applyTheme()
    fun getFragment(): Fragment?
}