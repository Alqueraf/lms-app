package com.instructure.parentapp.asynctask;

import android.content.Intent;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.instructure.canvasapi2.CanvasRestAdapter;
import com.instructure.canvasapi2.builders.RestBuilder;
import com.instructure.canvasapi2.utils.ApiPrefs;
import com.instructure.canvasapi2.utils.ContextKeeper;
import com.instructure.canvasapi2.utils.FileUtils;
import com.instructure.canvasapi2.utils.MasqueradeHelper;
import com.instructure.loginapi.login.tasks.SwitchUsersTask;
import com.instructure.pandautils.utils.ThemePrefs;
import com.instructure.pandautils.utils.Utils;
import com.instructure.parentapp.activity.LoginActivity;
import com.instructure.parentapp.util.ParentPrefs;

import java.io.File;
import java.io.IOException;

import okhttp3.OkHttpClient;

public class SwitchUsersAsyncTask extends SwitchUsersTask {

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

        RestBuilder.clearCacheDirectory();
        safeClear();
    }

    @Override
    protected void cleanupMasquerading() {
        MasqueradeHelper.stopMasquerading();
        //remove the cached stuff for masqueraded user
        File masqueradeCacheDir = new File(ContextKeeper.getAppContext().getFilesDir(), "cache_masquerade");
        //need to delete the contents of the internal cache folder so previous user's results don't show up on incorrect user
        FileUtils.deleteAllFilesInDirectory(masqueradeCacheDir);
    }

    @Override
    protected void refreshWidgets() {
        //No widgets in parent app
    }

    @Override
    protected void clearTheme() {
        ThemePrefs.INSTANCE.clearPrefs();
    }

    @Override
    protected void startLoginFlow() {
        Intent intent = LoginActivity.Companion.createIntent(ContextKeeper.appContext);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        ContextKeeper.appContext.startActivity(intent);
    }

    private void safeClear() {
        ApiPrefs.clearAllData();
        ParentPrefs.INSTANCE.clearPrefs();
        File exCacheDir = Utils.getAttachmentsDirectory(ContextKeeper.getAppContext());
        File cacheDir = new File(ContextKeeper.getAppContext().getFilesDir(), "cache");
        //need to delete the contents of the internal/external cache folder so previous user's results don't show up on incorrect user
        FileUtils.deleteAllFilesInDirectory(cacheDir);
        FileUtils.deleteAllFilesInDirectory(exCacheDir);
    }
}
