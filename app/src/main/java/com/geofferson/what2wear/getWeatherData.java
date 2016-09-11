package com.geofferson.what2wear;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Solid Choice on 18/11/2014.
 */
public class getWeatherData extends AsyncTask<Object, Void, JSONObject> {
    protected Context context;
    protected asyncResponse delegate;//(asyncResponse) new getWeatherData();//null

    //set the delegate variable to the main activity context so the interface knows where to send the result. The context was set when the getWeatherData object was made in main activity.

    protected getWeatherData(asyncResponse delegate, Context context) {
        this.context = context;
        this.delegate = delegate;
    }

    private static final String TAG = getWeatherData.class.getSimpleName();

    @Override
    protected JSONObject doInBackground(Object... arg0){
        SharedPreferences prefs = context.getSharedPreferences("myPrefs",Context.MODE_PRIVATE);
        String location = prefs.getString("location","Guelph,ca");
        Float lat = prefs.getFloat("lat",0);
        Float lon = prefs.getFloat("lon",0);
        //Log.i(TAG, location);
        //String URLvar = context.getResources().getString(R.string.baseURL)+"q="+location+"&units=metric&appid="+context.getResources().getString(R.string.weatherKey);
        String URLvar = context.getResources().getString(R.string.baseURL)+"lat="+lat+"&lon="+lon+"&units=metric&appid="+context.getResources().getString(R.string.weatherKey);
        //Log.d(TAG, URLvar);
        int responseCode = -1;
        JSONObject jsonResponse = null;
        try {
            URL weatherURL = new URL(URLvar);
            HttpURLConnection connection = (HttpURLConnection) weatherURL.openConnection();
            connection.connect();
            responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK){

                InputStream inputStream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder responseData = new StringBuilder();
                String tmp = "";
                while((tmp=reader.readLine()) != null){
                    responseData.append(tmp);
                }

                jsonResponse = new JSONObject(responseData.toString());
                //Log.i(TAG, responseData.toString());
            } else {
                Log.i(TAG, "weather query gave bad response code");
            }
        }
        catch(MalformedURLException e){ //if url object fails do this
            Log.e(TAG, "MalFormedException caught: ", e);
        }
        catch(IOException e ) {
            Log.e(TAG, "IOException caught: ", e);
        }
        catch(Exception e) { //Exception is a class. generic exception for all other errors
            Log.e(TAG, "Exception caught: ", e);
        }

        if (jsonResponse == null) {
            Toast toast = Toast.makeText(context, "WeatherWare failed to establish connection. Please check connection status.",Toast.LENGTH_LONG);
            toast.show();
            try {
                jsonResponse = new JSONObject("{\"cod\":\"191919\"}");
            } catch (Exception e) {}
        }
        return jsonResponse;
    }

    @Override
    protected void onPostExecute(JSONObject result) {
        try {
            Log.d(TAG,result.toString());
            int code = Integer.parseInt(result.getString("cod"));
            if(code != 200){
                String output = "failed";
                delegate.processFinish(output);
            }
            else if (code == 191919) {
                String output = "failConnect";
                delegate.processFinish(output);
            }
            else {
                String output = result.toString();//getString("weather");
                delegate.processFinish(output);
            }
        } catch (JSONException e) {

            e.printStackTrace();
        }


    }

    private boolean networkAvailable(Context context) {
        boolean result = false;

        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()){
            result = true;
        }

        return result;
    }

    public void initiate(Context context) {

        if (networkAvailable(context)) {
            new getWeatherData(delegate,context).execute();
        }

    }

}
