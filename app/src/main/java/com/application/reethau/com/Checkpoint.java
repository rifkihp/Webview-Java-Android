package com.application.reethau.com;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import androidx.core.app.NotificationCompat;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.List;

public class Checkpoint {

    private Context context;
    private String csrfToken  = "";
    private String csrfCookis = "";
    private String checkoutUrl = "https://pos.gurita.my.id/home";
    private String base_url   ="https://pos.gurita.my.id/get-notifications";
    //private String base_url   ="http://192.168.1.9/pushnotif_reethau/store/unread.php";

    private String myResponse = "{\"pengaduan\":[{\"id\":\"pRj6p\",\"date\":\"2 jam yang lalu\",\"acronym\":\"TD\",\"type\":\"message\",\"is_read\":false,\"title\":\"Permintaan Topup Deposit\",\"content\":\"<p class=\\\"title\\\"><b>Topup deposit melalui Bank BRI<\\/b><\\/p>\\n<p>berhasil<\\/p>\\n<table width=\\\"100%\\\">\\n    <tr>\\n        <td>Kode Deposit<\\/td>\\n        <td><b>DEP253<\\/b><\\/td>\\n    <\\/tr>\\n    <tr>\\n        <td>Jumlah Transfer<\\/td>\\n        <td>Rp. 10.025<\\/td>\\n    <\\/tr>\\n    <tr>\\n        <td>Nomor Rekening Tujuan<\\/td>\\n        <td>BRI 755101003529536 a\\/n HENDY<\\/td>\\n    <\\/tr>\\n<\\/table>\\n\\n\"}]}";

    public Checkpoint(Context context) {
        this.context = context;
    }

    public void doCheckpoint() {
        if(getTime()>=30) {
            okHttp_home();
        } else {
            okHttp();
        }
    }

    private void okHttp() {
        setTime(getTime()+1);
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "");
        Request request = new Request.Builder()
                .url(base_url)
                .method("GET", null)
                .addHeader("Accept", "application/json, text/plain, */*")
                .addHeader("x-csrf-token", getToken())
                .addHeader("Cookie", getCookies())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {

            }

            @Override
            public void onResponse(Response response) throws IOException {
                myResponse = response.body().string();

                Log.e("PARDEDE COUNT", "=========  " + getTime());
                Log.i("PARDEDE RESPONSE", myResponse);

                try {
                    JSONObject jsonObject = new JSONObject(myResponse);
                    JSONArray data_pengaduan  = jsonObject.isNull("pengaduan")?null:jsonObject.getJSONArray("pengaduan");
                    JSONArray data_pembayaran = jsonObject.isNull("pembayaran")?null:jsonObject.getJSONArray("pembayaran");
                    if(data_pengaduan!=null && data_pembayaran!=null) {
                        DatabaseHandler dh = new DatabaseHandler(context);
                        if(data_pengaduan.length()==0 && data_pembayaran.length()==0) {
                            dh.deleteData();
                        } else {
                            for (int i = 0; i < data_pengaduan.length(); i++) {
                                JSONObject rec = data_pengaduan.getJSONObject(i);

                                String kode = rec.getString("id");
                                String tanggal = rec.getString("date");
                                //String acronym = rec.getString("acronym");
                                String tipe = rec.getString("jenis");
                                //String is_read = rec.getString("is_read");
                                String title = rec.getString("title");
                                String content = rec.getString("body");

                                Log.e("PARPAR kode", kode + " " + title);
                                if(!dh.checkData(kode)) {
                                    dh.insertData(kode);
                                    pushNotification(kode, tanggal, title, content);
                                }
                            }

                            for (int i = 0; i < data_pembayaran.length(); i++) {
                                JSONObject rec = data_pembayaran.getJSONObject(i);

                                String kode = rec.getString("id");
                                String tanggal = rec.getString("date");
                                //String acronym = rec.getString("acronym");
                                String tipe = rec.getString("jenis");
                                //String is_read = rec.getString("is_read");
                                String title = rec.getString("title");
                                String content = rec.getString("body");

                                Log.e("PARPAR kode", kode + " " + title);
                                if(!dh.checkData(kode)) {
                                    dh.insertData(kode);
                                    pushNotification(kode, tanggal, title, content);
                                }
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    private void okHttp_home() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(checkoutUrl)
                .method("GET", null)
                .addHeader("Cookie", getCookies())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {

            }

            @Override
            public void onResponse(Response response) throws IOException {
                String myResponse = response.body().string();
                //Log.i("PARDEDE RESPONSE", myResponse);

                Document doc = Jsoup.parse(myResponse);
                Elements links = doc.select("meta[name=csrf-token]");

                csrfToken = links.attr("content");
                Log.i("PARDEDE TOKEN ZR", csrfToken);
                if(!csrfToken.equalsIgnoreCase("")) setToken();

                List<String> _listCsrfCookis = response.headers().values("Set-Cookie");
                csrfCookis = "";
                for (String _csrfCookis:_listCsrfCookis) {
                    //_csrfCookis = (_csrfCookis.split(";"))[0];
                    //Log.i("PARDEDE COOKIES", _csrfCookis);
                    csrfCookis+=_csrfCookis+"; ";
                }
                Log.i("PARDEDE ALL COOKIES", csrfCookis);
                if(!csrfCookis.equalsIgnoreCase("")) setCookies();

                setTime(0);
            }
        });
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

    private void setTime(int timecount) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("timecount", timecount);
        editor.commit();
    }

    private int getTime() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt("timecount", 0);
    }

    private void pushNotification(String kode, String tanggal, String title, String detail) {
        Intent i = new Intent(context, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.putExtra("kode", kode);
        i.putExtra("default_url", "http://pdam.reethau.com/pengaduan");

        String GROUP_KEY_NOTIFICATION = "com.application.reethau.com.STAGING_NOTIFICATION";
        String ANDROID_CHANNEL_ID     = "com.application.reethau.com.ANDROID";
        String ANDROID_CHANNEL_NAME   = "STAGING CHANNEL";

        PendingIntent pIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), i, 0);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(ANDROID_CHANNEL_ID, ANDROID_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(notificationChannel);

            builder = new NotificationCompat.Builder(context, notificationChannel.getId());
        } else {
            builder = new NotificationCompat.Builder(context);
        }
        builder = builder
                .setAutoCancel(true)
                .setContentIntent(pIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(tanggal)
                .setContentText(detail)
                .setTicker(title)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setGroup(GROUP_KEY_NOTIFICATION);

        Notification n = builder.build();
        n.defaults |= Notification.DEFAULT_SOUND;
        n.defaults |= Notification.DEFAULT_VIBRATE;

        NotificationID nid = new NotificationID();
        notificationManager.notify(nid.getID(), n);
    }
}
