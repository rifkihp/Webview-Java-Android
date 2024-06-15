package com.application.reethau.com;

import android.content.Context;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

public class JavascriptHandler {

    Context context;


    JavascriptHandler(Context context) {
        this.context = context;
    }
    /**
     *  Key point here is the annotation @JavascriptInterface
     *
     */
    @JavascriptInterface
    public void jsCallback() {
        // Do something
        Toast.makeText(context, "Test OHOT", Toast.LENGTH_SHORT).show();

    }

    @JavascriptInterface
    public void jsCallbackTwo(String dummyData) {
        // Do something
        Toast.makeText(context, dummyData, Toast.LENGTH_SHORT).show();
    }
}