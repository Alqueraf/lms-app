package com.instructure.candroid.factory

import com.instructure.candroid.presenters.ViewPdfFragmentPresenter
import instructure.androidblueprint.PresenterFactory

class ViewPdfFragmentPresenterFactory(val pdfUrl : String) : PresenterFactory<ViewPdfFragmentPresenter> {
    override fun create() = ViewPdfFragmentPresenter(pdfUrl)
}