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

package com.instructure.parentapp.asynctask;

import android.content.Intent;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.Toast;

import com.instructure.canvasapi2.CanvasRestAdapter;
import com.instructure.canvasapi2.builders.RestBuilder;
import com.instructure.canvasapi2.managers.OAuthManager;
import com.instructure.canvasapi2.utils.APIHelper;
import com.instructure.canvasapi2.utils.ApiPrefs;
import com.instructure.canvasapi2.utils.ContextKeeper;
import com.instructure.canvasapi2.utils.FileUtils;
import com.instructure.canvasapi2.utils.MasqueradeHelper;
import com.instructure.loginapi.login.tasks.LogoutTask;
import com.instructure.pandautils.utils.FilePrefs;
import com.instructure.pandautils.utils.ThemePrefs;
import com.instructure.pandautils.utils.Utils;
import com.instructure.parentapp.R;
import com.instructure.parentapp.activity.LoginActivity;
import com.instructure.parentapp.util.ParentPrefs;

import java.io.File;
import java.io.IOException;

import okhttp3.OkHttpClient;

public class LogoutAsyncTask extends LogoutTask {

    @Override
    protected void onLogoutFailed() {
        Toast.makeText(ContextKeeper.appContext, R.string.noDataConnection, Toast.LENGTH_SHORT).show();
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void clearCookies() {
        CookieSyncManager.createInstance(ContextKeeper.appContext);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();
    }

    @Override
    protected void clearCache() {
        OkHttpClient client = CanvasRestAdapter.getClient();
        if(client != null) {
            try {
                client.cache().evictAll();
            } catch (IOException e) {/* Do Nothing */}
        }

        File exCacheDir = Utils.getAttachmentsDirectory(ContextKeeper.getAppContext());
        FileUtils.deleteAllFilesInDirectory(exCacheDir);
        RestBuilder.clearCacheDirectory();
        ApiPrefs.clearAllData();
        ParentPrefs.INSTANCE.clearPrefs();
        FilePrefs.INSTANCE.clearPrefs();
    }

    @Override
    protected void cleanupMasquerading() {
        MasqueradeHelper.stopMasquerading();
    }

    @Override
    protected boolean logout() {
        if(APIHelper.hasNetworkConnection()) {
            if (!ApiPrefs.getToken().isEmpty()) {
                //Delete token from server. Fire and forget.
                OAuthManager.deleteToken();
                return true;
            }
            return true;
        }
        return false;
    }

    @Override
    protected void refreshWidgets() {
        // No widgets in parent app
    }

    @Override
    protected void clearTheme() {
        ThemePrefs.INSTANCE.clearPrefs();
    }

    @Override
    protected void startLoginFlow() {
        Intent intent = LoginActivity.Companion.createIntent(ContextKeeper.getAppContext());


        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);

        ContextKeeper.getAppContext().startActivity(intent);
    }

}
