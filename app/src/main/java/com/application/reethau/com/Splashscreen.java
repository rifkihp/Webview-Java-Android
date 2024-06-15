package com.application.reethau.com;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Splashscreen extends AppCompatActivity {

    Context context;
    String app_ver_no = "";
    String app_ver_name = "";
    version app_ver;
    TextView appVer;

    version getAppVersion(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String version_name = prefs.getString("version_name", "");
        String version_no = prefs.getString("version_no", "");

        if (version_name.length() == 0 && version_no.length() == 0) {
            version_name = BuildConfig.VERSION_NAME;
            version_no = String.valueOf(BuildConfig.VERSION_CODE);

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("version_name", version_name);
            editor.putString("version_no", version_no);
        }

        return new version(version_no, version_name);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);

        context = Splashscreen.this;
        app_ver = getAppVersion(context);
        app_ver_no = app_ver.getNo();
        app_ver_name = app_ver.getNama();
        appVer = findViewById(R.id.app_ver);

        appVer.setText(app_ver_name);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                Intent i = new Intent(context, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);

                finish();
            }
        }, 3000);

    }
}
