/*
 * Copyright (C) 2016 - present Instructure, Inc.
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

package com.instructure.candroid.fragment

import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.instructure.candroid.R
import com.instructure.candroid.events.ModuleUpdatedEvent
import com.instructure.candroid.events.post
import com.instructure.candroid.util.FileDownloadJobIntentService
import com.instructure.candroid.util.StringUtilities
import com.instructure.canvasapi2.managers.FileFolderManager
import com.instructure.canvasapi2.managers.ModuleManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.FileFolder
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.Logger
import com.instructure.canvasapi2.utils.pageview.BeforePageView
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.pageview.PageViewUrlParam
import com.instructure.canvasapi2.utils.pageview.PageViewUrlQuery
import com.instructure.canvasapi2.utils.weave.*
import com.instructure.interactions.router.Route
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.*
import com.instructure.pandautils.utils.PermissionUtils
import com.instructure.pandautils.utils.ViewStyler
import kotlinx.android.synthetic.main.fragment_file_details.*

import java.util.Date

import okhttp3.ResponseBody

@PageView(url = "{canvasContext}/files/{fileId}")
class FileDetailsFragment : ParentFragment() {

    private var canvasContext by ParcelableArg<CanvasContext>(key = Const.CANVAS_CONTEXT)

    private var moduleObject by ParcelableArg<ModuleObject>(key = Const.MODULE_OBJECT)
    private var itemId: Long by LongArg(key = Const.ITEM_ID)

    private var file: FileFolder? = null
    private var fileUrl: String by StringArg(key = Const.FILE_URL)

    private var fileFolderJob: WeaveJob? = null
    private var markAsReadJob: WeaveJob? = null

    private val fileId: Long
        get() = file!!.id

    private val moduleItemId: Long?
        get() = this.getModuleItemId()

    @PageViewUrlParam(name = "fileId")
    private fun getFileIdValue(): Long = fileId

    @PageViewUrlQuery(name = "module_item_id")
    private fun getModuleIdValue(): Long? = moduleItemId

    @BeforePageView
    private fun setPageViewReady() {}

    override fun title(): String {
        return if (file != null && file!!.lockInfo == null) file!!.displayName ?: getString(R.string.file) else getString(R.string.file)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View?
            = inflater?.inflate(R.layout.fragment_file_details, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        getFileFolder()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        fileFolderJob?.cancel()
        markAsReadJob?.cancel()
    }

    override fun applyTheme() {
        setupToolbarMenu(toolbar)
        toolbar.setupAsBackButton(this)
        ViewStyler.themeToolbar(activity, toolbar, canvasContext)
        ViewStyler.themeButton(openButton)
        ViewStyler.themeButton(downloadButton)
    }

    private fun setupTextViews() {
        fileName.text = file?.displayName
        fileType.text = file?.contentType
    }

    private fun setupClickListeners() {
        openButton.setOnClickListener {
            file?.let {
                openMedia(it.contentType, it.url, it.displayName, canvasContext)
                markAsRead()
            }
        }

        downloadButton.setOnClickListener {
            if (PermissionUtils.hasPermissions(activity, PermissionUtils.WRITE_EXTERNAL_STORAGE)) {
                downloadFile()
            } else {
                requestPermissions(PermissionUtils.makeArray(PermissionUtils.WRITE_EXTERNAL_STORAGE), PermissionUtils.WRITE_FILE_PERMISSION_REQUEST_CODE)
            }
        }
    }

    override fun onMediaLoadingStarted() {
        fileLoadingProgressBar?.setVisible()
    }

    override fun onMediaLoadingComplete() {
        fileLoadingProgressBar?.setGone()
    }

    private fun downloadFile() {
        FileDownloadJobIntentService.scheduleDownloadJob(context, file)
        markAsRead()
    }

    private fun markAsRead() {
        //Mark the module as read
        markAsReadJob = tryWeave {
            awaitApi<ResponseBody> { ModuleManager.markModuleItemAsRead(canvasContext, moduleObject.id, itemId, it) }
            ModuleUpdatedEvent(moduleObject).post()
        } catch {
            Logger.e("Error marking module item as read. " + it.message)
        }
    }

    @Suppress("deprecation")
    private fun getFileFolder() {
        fileFolderJob = tryWeave {
            val response = awaitApiResponse<FileFolder> { FileFolderManager.getFileFolderFromURL(fileUrl, it) }
            //set up everything else now, we should have a file
            file = response.body()

            file?.let {
                if (it.lockInfo != null) {
                    //file is locked
                    fileIcon.setImageResource(R.drawable.vd_lock)
                    openButton.visibility = View.GONE
                    downloadButton.visibility = View.GONE
                    fileType.visibility = View.INVISIBLE
                    var lockedMessage = ""

                    if (it.lockInfo?.lockedModuleName != null) {
                        lockedMessage = "<p>" + String.format(activity.getString(R.string.lockedFileDesc), "<b>" + it.lockInfo!!.lockedModuleName + "</b>") + "</p>"
                    }
                    if (it.lockInfo?.modulePrerequisiteNames?.size ?: 0 > 0) {
                        //we only want to add this text if there are module completion requirements
                        lockedMessage += activity.getString(R.string.mustComplete) + "<br>"
                        //textViews can't display <ul> and <li> tags, so we need to use "&#8226; " instead
                        for (i in 0 until it.lockInfo!!.modulePrerequisiteNames.size) {
                            lockedMessage += "&#8226; " + it.lockInfo!!.modulePrerequisiteNames[i]  //"&#8226; "
                        }
                        lockedMessage += "<br><br>"
                    }

                    //check to see if there is an unlocked date
                    if (it.lockInfo!!.unlockAt != null && it.lockInfo!!.unlockAt!!.after(Date())) {
                        lockedMessage += DateHelper.createPrefixedDateTimeString(activity, activity.getString(R.string.unlockedAt) + "<br>&#8226; ", it.lockInfo!!.unlockAt)
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) fileName.text = StringUtilities.simplifyHTML(Html.fromHtml(lockedMessage, Html.FROM_HTML_MODE_LEGACY))
                    else fileName.text = StringUtilities.simplifyHTML(Html.fromHtml(lockedMessage))
                } else {
                    setupTextViews()
                    setupClickListeners()
                    // if the file has a thumbnail then show it. Make it a little bigger since the thumbnail size is pretty small
                    if (!TextUtils.isEmpty(it.thumbnailUrl)) {

                        fileIcon.layoutParams.apply {
                            height = activity.DP(230).toInt()
                            width = height
                        }

                        Glide.with(activity).load(it.thumbnailUrl).apply(RequestOptions().fitCenter()).into(fileIcon)
                    }
                }
                setPageViewReady()
            }
            toolbar.title = title()
        } catch {
            Logger.e("Error getting file folder " + it.message)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PermissionUtils.WRITE_FILE_PERMISSION_REQUEST_CODE) {
            if (PermissionUtils.permissionGranted(permissions, grantResults, PermissionUtils.WRITE_EXTERNAL_STORAGE)) {
                downloadFile()
            }
        }
    }

    companion object {

        @JvmStatic
        fun makeRoute(canvasContext: CanvasContext, fileUrl: String): Route {
            val bundle = Bundle().apply { putString(Const.FILE_URL, fileUrl) }
            return Route(null, FileDetailsFragment::class.java, canvasContext, bundle)
        }

        @JvmStatic
        fun makeRoute(canvasContext: CanvasContext, moduleObject: ModuleObject, itemId: Long, fileUrl: String): Route {
            val bundle = Bundle().apply {
                putString(Const.FILE_URL, fileUrl)
                putParcelable(Const.MODULE_OBJECT, moduleObject)
                putLong(Const.ITEM_ID, itemId)
            }
            return Route(null, FileDetailsFragment::class.java, canvasContext, bundle)
        }

        private fun validateRoute(route: Route): Boolean {
            return route.canvasContext != null && route.arguments.containsKey(Const.FILE_URL)
        }

        @JvmStatic
        fun newInstance(route: Route): FileDetailsFragment? {
            if (!validateRoute(route)) return null
            return FileDetailsFragment().withArgs(route.canvasContext!!.makeBundle(route.arguments))
        }
    }
}
