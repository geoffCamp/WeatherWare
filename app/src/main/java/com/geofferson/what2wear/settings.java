package com.geofferson.what2wear;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Solid Choice on 15/12/2014.
 */
public class settings {
    private static final String TAG = settings.class.getSimpleName();

    protected void saveSettings (Context context, String units, String location) {
        SharedPreferences.Editor editor = context.getSharedPreferences("myPrefs", context.MODE_PRIVATE).edit();
        editor.putString("units", units);
        editor.putString("location",location);
        editor.commit();
    }

    protected void saveCoords (Context context, Double lat, Double lon) {
        SharedPreferences.Editor editor = context.getSharedPreferences("myPrefs", context.MODE_PRIVATE).edit();
        editor.putFloat("lat", Float.valueOf(String.valueOf(lat)));
        editor.putFloat("lon", Float.valueOf(String.valueOf(lon)));
        editor.commit();
    }

}
