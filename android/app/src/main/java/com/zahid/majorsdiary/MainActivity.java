package com.zahid.majorsdiary;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Hosts the planner and class portal (the web app). Routines saved there feed the widgets,
 * and joining the class turns on notifications for notes, quizzes and announcements.
 */
public class MainActivity extends Activity {

    static final String EXTRA_TAB = "tab";
    private static final String APP_HOST = "zahid-56.github.io";
    private static final String APP_URL = "https://zahid-56.github.io/Major-s-diary/";
    private static final int PICK_FILE = 42;

    private WebView web;
    private ValueCallback<Uri[]> fileCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.parseColor("#07090F"));
        getWindow().setNavigationBarColor(Color.parseColor("#07090F"));

        web = new WebView(this);
        web.setBackgroundColor(Color.parseColor("#07090F"));
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setDomStorageEnabled(true);
        web.addJavascriptInterface(new Bridge(getApplicationContext()), "MajorsDiary");

        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                if (APP_HOST.equals(uri.getHost())) return false;
                openExternal(uri);   // notes, Drive links, YouTube, APK downloads → browser / matching app
                return true;
            }
        });
        // Enables JS dialogs (confirm/prompt) and the file picker for photos and notes uploads.
        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (fileCallback != null) fileCallback.onReceiveValue(null);
                fileCallback = callback;
                try {
                    startActivityForResult(params.createIntent(), PICK_FILE);
                } catch (Exception e) {
                    fileCallback = null;
                    return false;
                }
                return true;
            }
        });
        web.setDownloadListener((url, userAgent, contentDisposition, mimeType, length) -> openExternal(Uri.parse(url)));

        web.loadUrl(urlFor(getIntent()));
        setContentView(web);

        // Background setup must never stop the app from opening.
        safe(() -> Notify.ensureChannels(this));
        safe(this::askNotificationPermission);
        safe(() -> DiaryWidget.refreshAll(this));
        safe(() -> ReminderReceiver.scheduleNext(this));
        safe(() -> ClassSyncJob.schedule(this));
    }

    static void safe(Runnable task) {
        try {
            task.run();
        } catch (Throwable t) {
            android.util.Log.e("MajorsDiary", "Background setup failed", t);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if ("class".equals(intent.getStringExtra(EXTRA_TAB)) && web != null) {
            web.evaluateJavascript("location.hash='#class'", null);
        }
    }

    private static String urlFor(Intent intent) {
        return "class".equals(intent.getStringExtra(EXTRA_TAB)) ? APP_URL + "#class" : APP_URL;
    }

    private void openExternal(Uri uri) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
        } catch (Exception ignored) {
        }
    }

    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 7);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != PICK_FILE || fileCallback == null) return;
        fileCallback.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
        fileCallback = null;
    }

    @Override
    public void onBackPressed() {
        if (web != null && web.canGoBack()) web.goBack();
        else super.onBackPressed();
    }

    /** Lets the web app talk to the native side. */
    static final class Bridge {
        private final Context context;

        Bridge(Context context) {
            this.context = context;
        }

        @JavascriptInterface
        public void saveRoutine(String json) {
            String value = json == null ? "" : json;
            String old = context.getSharedPreferences(Schedule.PREFS, Context.MODE_PRIVATE).getString(Schedule.KEY_ROUTINE, "");
            if (value.equals(old)) return;
            context.getSharedPreferences(Schedule.PREFS, Context.MODE_PRIVATE)
                    .edit().putString(Schedule.KEY_ROUTINE, value).apply();
            safe(() -> DiaryWidget.routineChanged(context));
            safe(() -> ReminderReceiver.scheduleNext(context));
        }

        @JavascriptInterface
        public void setClassJoined(boolean joined) {
            safe(() -> ClassSyncJob.setJoined(context, joined));
            if (joined) safe(() -> ClassSyncJob.schedule(context));
        }
    }
}
