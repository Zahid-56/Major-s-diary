package com.zahid.majorsdiary;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/** Opens the full planner (the hosted web app). Routines saved there are shared with the widget. */
public class MainActivity extends Activity {

    private static final String APP_URL = "https://zahid-56.github.io/Major-s-diary/";

    private static final int PICK_IMAGE = 42;

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
        web.setWebViewClient(new WebViewClient());
        // Enables JS dialogs (confirm/prompt) and the photo picker for "My photo" backgrounds.
        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (fileCallback != null) fileCallback.onReceiveValue(null);
                fileCallback = callback;
                Intent pick = new Intent(Intent.ACTION_GET_CONTENT).addCategory(Intent.CATEGORY_OPENABLE).setType("image/*");
                try {
                    startActivityForResult(Intent.createChooser(pick, "Choose a photo"), PICK_IMAGE);
                } catch (Exception e) {
                    fileCallback = null;
                    return false;
                }
                return true;
            }
        });
        web.addJavascriptInterface(new Bridge(getApplicationContext()), "MajorsDiary");
        web.loadUrl(APP_URL);
        setContentView(web);

        DiaryWidget.refreshAll(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != PICK_IMAGE || fileCallback == null) return;
        Uri[] result = null;
        if (resultCode == RESULT_OK && data != null && data.getData() != null) result = new Uri[]{data.getData()};
        fileCallback.onReceiveValue(result);
        fileCallback = null;
    }

    @Override
    public void onBackPressed() {
        if (web != null && web.canGoBack()) web.goBack();
        else super.onBackPressed();
    }

    /** Lets the web app hand the user's routine to the widget. */
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
            DiaryWidget.routineChanged(context);
        }
    }
}
