package com.geofferson.what2wear;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
//import com.google.android.gms.common.api.GoogleApiClient;
//import com.google.android.gms.location.LocationServices;

import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class MainActivity extends FragmentActivity implements
        asyncResponse, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback, com.google.android.gms.location.LocationListener{

    protected getWeatherData mAsyncTask = new getWeatherData(MainActivity.this,MainActivity.this);
    protected data2clothes mData2clothes = new data2clothes();
    protected MapUtility mMapUtility = new MapUtility();
    protected MapFragment mMapFragment;
    protected View mMapView;
    protected GoogleMap mMap;
    protected settings mSettings = new settings();
    protected SharedPreferences mPrefs;
    protected GoogleApiClient mGoogleClient;
    protected Button refreshButton;
    protected Button settingsButton;
    protected Button mapBackBtn;
    protected RadioButton metricBtn;
    protected RadioButton imperialBtn;

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*if (!isInternetWorking()) {
            Toast toast = Toast.makeText(this, "Connect to the interwebs to use WeatherWare.",Toast.LENGTH_LONG);
            toast.show();
            return;
        }*/
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M ) {
            checkPermission();
        }
        mPrefs = getSharedPreferences("myPrefs",Context.MODE_PRIVATE);
        /*refreshButton = (Button) findViewById(R.id.refreshButton);
        settingsButton = (Button) findViewById(R.id.settingsButton);*/
        metricBtn = (RadioButton) findViewById(R.id.metric);
        imperialBtn = (RadioButton) findViewById(R.id.imperial);
        mapBackBtn = (Button) findViewById(R.id.mapBack);
        mMapView = findViewById(R.id.map);
        hideMap();

        /*refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LatLng pos = mSettings.returnLatLng(MainActivity.this);
                mMapUtility.setPointAt(MainActivity.this,pos);
            }
        });

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                //settingsDialoge.show();
                showMap();
            }
        });*/

        mapBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideMap();
            }
        });
        metricBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSettings.saveSettings(MainActivity.this,"metric","");
                goToPrefLoc();
            }
        });
        imperialBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSettings.saveSettings(MainActivity.this,"imperial","");
                goToPrefLoc();
            }
        });

        getCurrentLocation();
    }

    protected void reloadWeatherData () {
        mAsyncTask.initiate(MainActivity.this);
    }

    @Override
    public void onMapReady(final GoogleMap map) {
        mMapUtility.mapReady(map,this);
    }

    protected void getCurrentLocation () {
        if (mGoogleClient == null) {
            Log.d(TAG, "google client is null");
            mGoogleClient = new GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build();
        }
        mMapUtility.setUpMap(this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, Integer.toString(i));
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "connected to google client");

        try {
            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleClient);
            if (location != null) {
                Log.d(TAG, "location not null");
                onLocationChanged(location);
            } else {
                Log.i(TAG, "mLocation is null, starting location updater");
                LocationRequest locReq = new LocationRequest();
                locReq.create();
                locReq.setNumUpdates(1);
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleClient, locReq, this);

                //can now set default location
                goToPrefLoc();
            }
        } catch (SecurityException e) {
            Log.w(TAG, "need location permission:");
            e.printStackTrace();
            Toast toast = Toast.makeText(this, "Enable location services to use WeatherWare.",Toast.LENGTH_LONG);
            toast.show();
            //switch to map to choose location manually
        }
    }

    protected void goToPrefLoc () {
        Float firstLat =mPrefs.getFloat("lat",0);
        Float firstLon = mPrefs.getFloat("lon",0);
        mMapUtility.setPointAt(this,new LatLng(firstLat,firstLon));
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG,"location updated");
        mMapUtility.setPointAt(this,new LatLng(location.getLatitude(),location.getLongitude()));
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "connection to google failed");
    }

    protected void showMap () {
        mMapView.setVisibility(View.VISIBLE);

        //settingsButton.setVisibility(View.GONE);
        //refreshButton.setVisibility(View.GONE);
    }

    public void hideMap () {
        mMapView.setVisibility(View.GONE);
        //settingsButton.setVisibility(View.VISIBLE);
        //refreshButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void processFinish(String output){
        final TextView textField = (TextView) findViewById(R.id.results);
        if (output.equals("failConnect")){
            textField.setText("Network Error :(");
        }
        if (output.equals("failed")){
            Toast toast = Toast.makeText(this, "Location failed to load. Try a different search location (settings -> click on map).",Toast.LENGTH_LONG);
            toast.show();
        }
        else{

            final ImageView imageField = (ImageView) findViewById(R.id.imageViewBG);
            final ListView clothesList = (ListView) findViewById(R.id.clothesList);
            //final TextView itemText = (TextView) findViewById(R.layout.listitem.listText);

            //textField.setText(output);
            String[] weatherInfo = mData2clothes.initializer(MainActivity.this,output);
            String[] clothes = weatherInfo[0].split("-");
            if (weatherInfo[11].equals("black") ){
                final ArrayAdapter arrayAdapter = new ArrayAdapter(this,R.layout.listitem,R.id.listTextBlack,clothes);
                clothesList.setAdapter(arrayAdapter);
            }
            else if (weatherInfo[11].equals("darkBlack") ){
                final ArrayAdapter arrayAdapter = new ArrayAdapter(this,R.layout.listitem,R.id.listTextDarkBlack,clothes);
                clothesList.setAdapter(arrayAdapter);
            }
            else if (weatherInfo[11].equals("grey") ){
                final ArrayAdapter arrayAdapter = new ArrayAdapter(this,R.layout.listitem,R.id.listTextGrey,clothes);
                clothesList.setAdapter(arrayAdapter);
            }
            else if (weatherInfo[11].equals("white") ){
                final ArrayAdapter arrayAdapter = new ArrayAdapter(this,R.layout.listitem,R.id.listTextWhite,clothes);
                clothesList.setAdapter(arrayAdapter);
            }

            //clothesList.setAdapter(arrayAdapter);

            textField.setText("Feels like "+weatherInfo[1]+weatherInfo[2]+" \n"+weatherInfo[3]+"\n"+weatherInfo[4]+", "+weatherInfo[5]);//weatherInfo[0] +
            mSettings.saveSettings(this,"",weatherInfo[4]+", "+weatherInfo[5]);
            textField.setTextColor(Color.parseColor(weatherInfo[10]));
            //itemText.setTextColor(Color.parseColor(weatherInfo[10]));
            int imageId = getResources().getIdentifier(weatherInfo[6], "drawable", getPackageName());
            imageField.setImageResource(imageId);
        }

    }

    public void checkPermission(){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                ){//Can add more as per requirement

            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    123);
        }
    }

    /*public boolean isInternetWorking() {
        boolean success = false;
        try {
            URL url = new URL("https://google.com");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(10000);
            connection.connect();
            success = connection.getResponseCode() == 200;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return success;
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            showMap();
            return true;
        }
        if (id == R.id.menu_refresh) {
            LatLng pos = mSettings.returnLatLng(MainActivity.this);
            mMapUtility.setPointAt(MainActivity.this,pos);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
