package com.zahid.majorsdiary;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/** Opens the full weekly planner (the hosted web app). */
public class MainActivity extends Activity {

    private static final String APP_URL = "https://zahid-56.github.io/Major-s-diary/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.parseColor("#07090F"));
        getWindow().setNavigationBarColor(Color.parseColor("#07090F"));

        WebView web = new WebView(this);
        web.setBackgroundColor(Color.parseColor("#07090F"));
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setDomStorageEnabled(true);
        web.setWebViewClient(new WebViewClient());
        web.loadUrl(APP_URL);
        setContentView(web);

        DiaryWidget.refreshAll(this);
    }
}
