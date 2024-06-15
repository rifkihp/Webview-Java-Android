package com.application.reethau.com;

import android.Manifest;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.application.reethau.com.data.RestApi;
import com.application.reethau.com.data.RetroFit;
import com.application.reethau.com.databinding.ActivityMainBinding;

import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.URLUtil;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import im.delight.android.webview.AdvancedWebView;

public class MainActivity extends AppCompatActivity implements AdvancedWebView.Listener {


    private ActivityMainBinding binding;

    //private SwipeRefreshLayout swipeRefreshLayout;
    private AdvancedWebView webView;
    private ProgressBar loading;
    private LinearLayout retry;
    private Button btnReload;

    private String TAG = "AKUANSIAGFEJAYA";

    private String registrationId = "";
    private String baseUrl    = "https://bisakerjaremote.com/mindfulday";

    private String csrfToken  = "";
    private String csrfCookis = "";
    private String nama = "";
    //private String kode;

    int count_close = 1;
    int current_click = 0;

    Handler mHandlerClose = new Handler();
    public Runnable mUpdateClose = new Runnable() {
        public void run() {
            mHandlerClose.removeCallbacks(this);
            current_click = 0;
        }
    };

    /*Handler mHandlerFirebaseInit = new Handler();
    public Runnable mUpdateFirebaseInit = new Runnable() {
        public void run() {
            mHandlerFirebaseInit.removeCallbacks(this);
            FirebaseInitial();
        }
    };

    Handler mHandlerGcmRegId = new Handler();
    public Runnable mUpdateGcmRegId = new Runnable() {

        public void run() {
            mHandlerGcmRegId.removeCallbacks(this);
            setGcm_regId();
        }
    };

    Handler mHandlerGcmUnregId = new Handler();
    public Runnable mUpdateGcmUnregId = new Runnable() {
        public void run() {
            mHandlerGcmUnregId.removeCallbacks(this);
            unsetGcm_regId();
        }
    };*/

    public static String printKeyHash(Context context) {
        PackageInfo packageInfo;
        String key = null;
        try {
            //getting application package name, as defined in manifest
            String packageName = context.getApplicationContext().getPackageName();

            //Retriving package info
            packageInfo = context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
            Log.e("Package Name=", context.getApplicationContext().getPackageName());

            for (Signature signature : packageInfo.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                key = new String(Base64.encode(md.digest(), 0));
                // String key = new String(Base64.encodeBytes(md.digest()));
                Log.e("Key Hash=", key);
            }
        } catch (PackageManager.NameNotFoundException e1) {
            Log.e("Name not found", e1.toString());
        } catch (NoSuchAlgorithmException e) {
            Log.e("No such an algorithm", e.toString());
        } catch (Exception e) {
            Log.e("Exception", e.toString());
        }
        return key;
    }

    //region FOR GENERATE KEY-HASH...
    public static void getKeyHash(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.e("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Context context;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();

        context = MainActivity.this;
        //new DatabaseHandler(context).createTable();
        //printKeyHash(context);

        if (android.os.Build.VERSION.SDK_INT > 8) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        if (Build.VERSION.SDK_INT >= 23) {
            insertDummyContactWrapper();
        }

        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor("#000000"));
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        webView   =  binding.webview;
        loading   =  binding.pgbarLoading;
        retry     =  binding.loadMask;
        btnReload =  binding.btnReload;

        btnReload.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                webView.loadUrl(baseUrl);
            }
        });

        /*swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);
                webView.loadUrl(checkoutUrl);
            }
        });*/

        if(savedInstanceState==null) {
            //kode = getIntent().getStringExtra("kode");
            //String default_url = getIntent().getStringExtra("default_url");
            //checkoutUrl = (default_url==null?checkoutUrl:default_url);//+(kode==null?"":"/"+kode);
        }

        webView.clearCache(true);
        CookieSyncManager.createInstance(context);
        CookieManager.getInstance().setAcceptCookie(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        } else {
            CookieSyncManager.createInstance(context).startSync();
            CookieManager.getInstance().removeAllCookie();
            CookieManager.getInstance().removeSessionCookie();
            CookieSyncManager.createInstance(context).stopSync();
            CookieSyncManager.createInstance(context).sync();
        }
        CookieManager.getInstance().removeSessionCookie();

        /*boolean islogin = checkoutUrl.indexOf("/login")>0;
        if(!islogin) {
            String cookies = getCookies();
            String[] temp = cookies.split(";");
            for (String ar1 : temp) {
                CookieManager.getInstance().setCookie(checkoutUrl, ar1);
            }
            CookieSyncManager.createInstance(context).sync();
        }*/

        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setLoadsImagesAutomatically(true);

        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setSupportMultipleWindows(false);


        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new MyJavaScriptInterface(), "HtmlViewer");

        /*webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setUseWideViewPort(true);*/

        webView.setCookiesEnabled(true);

        /*webView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
                    //Log.e("BACK", "DO BACK");
                    webView.goBack();
                    return true;
                }

                return false;
            }
        });*/

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                Log.i("PARDEDE URUL befload", url);

                /*if (kode!=null) {
                    kode = null;
                    webView.loadUrl("https://wepay.id/prabayar/pulsa");

                    return false;
                }*/

                /*if(url.indexOf("/login")>0) {
                    Intent i = new Intent(context, MainActivity.class);
                    i.putExtra("default_url", loginUrl);
                    startActivity(i);
                    finish();

                    return false;
                }*/

                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                baseUrl = url;
                loading.setVisibility(View.GONE);
                Log.i("PARDEDE URUL", url);

                if(url.indexOf("/login")>0) {
                    //unsetGcm_regId();
                } else {
                    //FirebaseInitial();
                }

                csrfCookis = CookieManager.getInstance().getCookie(url);
                //Log.i("PARPAR COOKIES", csrfCookis);
                setCookies();

                /*webView.loadUrl("javascript:window.HtmlViewer.showHTML" +
                        "('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');"
                );*/

            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                view.loadUrl("about:blank");
                webView.loadUrl("file:///android_asset/error_con.html");
            }

            @Override
            public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
                if (url.contains("chrome-error")) {
                    Intent intent = new Intent(context, MainActivity.class);
                    startActivity(intent);
                }
                super.doUpdateVisitedHistory(view, url, isReload);
            }

        });

        webView.setDownloadListener(new DownloadListener() {

            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimeType, long contentLength) {

                nama = URLUtil.guessFileName(url, contentDisposition, mimeType);

                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.setMimeType(mimeType);
                String cookies = CookieManager.getInstance().getCookie(url);
                request.addRequestHeader("cookie", cookies);
                request.addRequestHeader("User-Agent", userAgent);
                request.setDescription("Downloading file...");
                request.setTitle(nama);
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, nama);
                DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                dm.enqueue(request);
                Toast.makeText(getApplicationContext(), "Downloading File", Toast.LENGTH_LONG).show();


            }
        });

        webView.addJavascriptInterface(new JavascriptHandler(context), "Android");
        webView.loadUrl(baseUrl);
    }

    public void showPdf() {
        //try {



            //String folderPath = Environment.DIRECTORY_DOWNLOADS + File.separator + nama;
            //File file = new File(Environment.DIRECTORY_DOWNLOADS, nama);//name here is the name of any string you want to pass to the method
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), nama);
            if(file.exists()) {
                String extension = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf(".")).toLowerCase();
                if(extension.equalsIgnoreCase(".pdf")) {
                    displaypdf(file);
                    //Toast.makeText(getApplicationContext(), extension, Toast.LENGTH_LONG).show();
                    /*startActivity(
                            PdfViewerActivity.Companion.launchPdfFromPath(           //PdfViewerActivity.Companion.launchPdfFromUrl(..   :: incase of JAVA
                                    context,
                                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + nama,                                // PDF URL in String format
                                    nama,                        // PDF Name/Title in String format
                                    "",                  // If nothing specific, Put "" it will save to Downloads
                                    true,
                                    false// This param is true by defualt.
                            )
                    );*/

                } else {

                    // Get URI and MIME type of file
                    Uri uri = Uri.fromFile(file);
                    String mime = getContentResolver().getType(uri);

                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setDataAndType(uri, mime);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(intent);
                }
            }




        //} catch (Exception e) {
            //e.printStackTrace();
        //}
    }

    public void displaypdf(File file) {

        Toast.makeText(context, file.toString() , Toast.LENGTH_LONG).show();
        if(file.exists()) {
            Intent target = new Intent(Intent.ACTION_VIEW);
            target.setDataAndType(Uri.fromFile(file), "application/pdf");
            target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            target.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION );

            Intent intent = Intent.createChooser(target, "Open File");
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                // Instruct the user to install a PDF reader here, or something
            }
        }
        else
            Toast.makeText(getApplicationContext(), "File path is incorrect." , Toast.LENGTH_LONG).show();
    }

    /*private void FirebaseInitial() {
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if (!task.isSuccessful()) {
                    Log.w(TAG, "getInstanceId failed", task.getException());
                    //mHandlerFirebaseInit.postDelayed(mUpdateFirebaseInit, 3000);

                    return;
                }

                registrationId = getString(R.string.msg_token_fmt, task.getResult().getToken());
                setGcm_regId();
            }
        });
    }*/

    /*private void setGcm_regId() {
        RestApi api = RetroFit.getInstanceRetrofit();
        Call<ResponseRegistrasiRegId> registrasiRegIdCall = api.postRegistrasiRegId(registrationId);
        registrasiRegIdCall.enqueue(new Callback<ResponseRegistrasiRegId>() {
            @Override
            public void onResponse(@NonNull Call<ResponseRegistrasiRegId> call, @NonNull Response<ResponseRegistrasiRegId> response) {
                boolean success = Objects.requireNonNull(response.body()).getSuccess();
                if(!success) {
                    mHandlerGcmRegId.postDelayed(mUpdateGcmRegId, 3000);

                    return;
                }

                setFcmRegId();
            }
            @Override
            public void onFailure(@NonNull Call<ResponseRegistrasiRegId> call, @NonNull Throwable t) {
                mHandlerGcmRegId.postDelayed(mUpdateGcmRegId, 3000);
            }
        });
    }*/

    /*private void unsetGcm_regId() {
        registrationId = getFcmRegId();

        RestApi api = RetroFit.getInstanceRetrofit();
        Call<ResponseRegistrasiRegId> unregistrasiRegIdCall = api.postUnregistrasiRegId(registrationId);
        unregistrasiRegIdCall.enqueue(new Callback<ResponseRegistrasiRegId>() {
            @Override
            public void onResponse(@NonNull Call<ResponseRegistrasiRegId> call, @NonNull Response<ResponseRegistrasiRegId> response) {
                boolean success = Objects.requireNonNull(response.body()).getSuccess();
                if(!success) {
                    mHandlerGcmUnregId.postDelayed(mUpdateGcmUnregId, 3000);
                    return;
                }

                registrationId = "";
                setFcmRegId();
            }
            @Override
            public void onFailure(@NonNull Call<ResponseRegistrasiRegId> call, @NonNull Throwable t) {
                mHandlerGcmUnregId.postDelayed(mUpdateGcmUnregId, 3000);
            }
        });
    }*/

    /*private void okHttp(String str_url) {

        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "");
        Request request = new Request.Builder()
                .url(str_url)
                .method("POST", body)
                .addHeader("Accept", "application/json, text/plain")
                .addHeader("x-csrf-token", getToken())
                .addHeader("Cookie", getCookies())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {

            }

            @Override
            public void onResponse(Response response) throws IOException {
                String myResponse = response.body().string();
                Log.i("PARPAR RESPONSE", myResponse);
            }
        });

    }

    private void okHttpGetUserInfo() {

        String str_url = "https://wepay.id/api/v1/account";
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "");
        Request request = new Request.Builder()
                .url(str_url)
                .method("GET", null)
                .addHeader("Accept", "application/json, text/plain")
                .addHeader("x-csrf-token", getToken())
                .addHeader("Content-Type", "application/json")
                .addHeader("Cookie", getCookies())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {

            }

            @Override
            public void onResponse(Response response) throws IOException {
                String myResponse = response.body().string();
                Log.i("PARPAR RESPONSE", myResponse);
            }
        });

    }*/


    private void setCookies() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("csrfCookis", csrfCookis);
        editor.commit();
    }

    private String getCookies() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString("csrfCookis", csrfCookis);
    }

    private void setToken() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("csrfToken", csrfToken);
        editor.commit();
    }

    private String getToken() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString("csrfToken", csrfToken);
    }

    private void setFcmRegId() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("fcmRegId", registrationId);
        editor.commit();
    }

    private String getFcmRegId() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString("fcmRegId", registrationId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        webView.onResume();

        IntentFilter intentFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        registerReceiver(broadcast, intentFilter);
    }

    @Override
    protected void onPause() {
        webView.onPause();
        super.onPause();

        try {
            mHandlerClose.removeCallbacks(mUpdateClose);
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*try {
            mHandlerFirebaseInit.removeCallbacks(mUpdateFirebaseInit);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            mHandlerGcmRegId.removeCallbacks(mUpdateGcmRegId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            mHandlerGcmUnregId.removeCallbacks(mUpdateGcmUnregId);
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

    @Override
    protected void onDestroy() {
        webView.onDestroy();
        super.onDestroy();

        try {
            mHandlerClose.removeCallbacks(mUpdateClose);
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*try {
            mHandlerFirebaseInit.removeCallbacks(mUpdateFirebaseInit);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            mHandlerGcmRegId.removeCallbacks(mUpdateGcmRegId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            mHandlerGcmUnregId.removeCallbacks(mUpdateGcmUnregId);
        } catch (Exception e) {
            e.printStackTrace();
        }*/

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        webView.onActivityResult(requestCode, resultCode, intent);
        // ...
    }


    @Override
    public boolean onSupportNavigateUp() {

        return super.onSupportNavigateUp();
    }

    @Override
    public void onPageStarted(String url, Bitmap favicon) {

    }

    @Override
    public void onPageFinished(String url) {

    }

    @Override
    public void onPageError(int errorCode, String description, String failingUrl) {

    }

    @Override
    public void onDownloadRequested(String url, String suggestedFilename, String mimeType, long contentLength, String contentDisposition, String userAgent) {

    }

    @Override
    public void onExternalPageRequest(String url) {

    }

    /*private void closeHandler() {
        try {
            unregisterReceiver(mHandleLoadMainPage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final BroadcastReceiver mHandleLoadMainPage = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            webView.loadUrl(checkoutUrl);
        }
    };*/

    class MyJavaScriptInterface {
        @JavascriptInterface
        @SuppressWarnings("unused")
        public void showHTML(String htmlcode) {
            Document doc = Jsoup.parse(htmlcode);
            Elements links = doc.select("meta[name=csrf-token]");

            csrfToken = links.attr("content");
            Log.i("PARPAR TOKEN", csrfToken);
            setToken();
        }
    }

    public Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            mHandlerClose.removeCallbacks(this);
            current_click = 0;
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (current_click != count_close) {
            current_click++;
            Toast.makeText(context, "Tekan dua kali untuk keluar.", Toast.LENGTH_SHORT).show();
            mHandlerClose.postDelayed(mUpdateTimeTask, 1000);
            return false;
        }

        return super.onKeyDown(keyCode, event);
    }

    BroadcastReceiver broadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            showPdf();
        }
    };

    final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;
    private void insertDummyContactWrapper() {
        List<String> permissionsNeeded = new ArrayList<>();
        final List<String> permissionsList = new ArrayList<>();

        if (!addPermission(permissionsList, android.Manifest.permission.WRITE_EXTERNAL_STORAGE))
            permissionsNeeded.add("WRITE_EXTERNAL_STORAGE");
        if (!addPermission(permissionsList, android.Manifest.permission.READ_EXTERNAL_STORAGE))
            permissionsNeeded.add("READ_EXTERNAL_STORAGE");

        if (permissionsList.size() > 0) {
            if (permissionsNeeded.size() > 0) {
                // Need Rationale
                String message = "You need to grant access to " + permissionsNeeded.get(0);
                for (int i = 1; i < permissionsNeeded.size(); i++)
                    message = message + ", " + permissionsNeeded.get(i);

                //showMessageOKCancel(message, new DialogInterface.OnClickListener() {
                //@Override
                //public void onClick(DialogInterface dialog, int which) {*/
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(permissionsList.toArray(new String[permissionsList.size()]), REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
                }
                //}
                //});
                return;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(permissionsList.toArray(new String[permissionsList.size()]), REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            }
            return;
        }
    }

    private boolean addPermission(List<String> permissionsList, String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsList.add(permission);
                // Check for Rationale Option
                if (!shouldShowRequestPermissionRationale(permission))
                    return false;
            }
        }
        return true;
    }

    int REQUEST_STORAGE_PERMISSION = 100;

    public void requestStoragePermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                requestStoragePermission();
            }
        }

        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                Map<String, Integer> perms = new HashMap<String, Integer>();
                perms.put(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(android.Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);

                // Fill with results
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);
                // Check for ACCESS_FINE_LOCATION
                if (perms.get(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                        && perms.get(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                ) {
                    //&& perms.get(android.Manifest.permission.FLASHLIGHT) == PackageManager.PERMISSION_GRANTED
                    // All Permissions Granted
                    Intent i = new Intent(context, MainActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                    finish();

                } else {
                    // Permission Denied
                    Toast.makeText(context, "Some Permission is Denied", Toast.LENGTH_SHORT).show();
                }
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}