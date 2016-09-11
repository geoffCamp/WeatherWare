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
    protected Dialog settingsDialoge;
    protected Dialog referenceDialog;
    protected GoogleApiClient mGoogleClient;
    protected Location mLocation;
    protected Button refreshButton;
    protected Button settingsButton;

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M ) {
            checkPermission();
        }
        mPrefs = getSharedPreferences("myPrefs",Context.MODE_PRIVATE);
        // so they arent looking at nothing while location loads
        // ing context so the class can access getsystemservice
        //Log.i(TAG, "started get weather");


        settingsDialoge = new Dialog(this);//final Dialog settingsDialog = new Dialog(this);
        settingsDialoge.setContentView(R.layout.settings_dialoge);
        settingsDialoge.setTitle("Settings");
        getPrefs(settingsDialoge);

        referenceDialog = new Dialog(this);//final Dialog settingsDialog = new Dialog(this);
        referenceDialog.setContentView(R.layout.reference_dialog);
        referenceDialog.setTitle("Image Reference");

        refreshButton = (Button) findViewById(R.id.refreshButton);
        settingsButton = (Button) findViewById(R.id.settingsButton);
        Button settingsConfirm = (Button) settingsDialoge.findViewById(R.id.settingsConfirm);
        Button settingsCancel = (Button) settingsDialoge.findViewById(R.id.settingsCancel);
        Button settingsRef = (Button) settingsDialoge.findViewById(R.id.refBtn);
        Button referenceCancel = (Button) referenceDialog.findViewById(R.id.refCancel);

        mMapView = findViewById(R.id.map);
        hideMap();

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LatLng pos = mSettings.returnLatLng(MainActivity.this);
                mMapUtility.setPointAt(MainActivity.this,pos);
            }
        });

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                settingsDialoge.show();
                showMap();
            }
        });

        settingsConfirm.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick (View v) {
                String units = "metric";
                String location;
                TextView locationValue;
                RadioButton C;
                RadioButton F;

                locationValue = (EditText) settingsDialoge.findViewById(R.id.location);
                location = locationValue.getText().toString();

                if (location == ""){
                    location = "Guelph,ca";
                }

                C = (RadioButton) settingsDialoge.findViewById(R.id.metric);
                F = (RadioButton) settingsDialoge.findViewById(R.id.imperial);

                if (C.isChecked()){
                    units = "metric";
                }
                else if (F.isChecked()){
                    units = "imperial";
                }

                mSettings.saveSettings(MainActivity.this,units,location);
                settingsDialoge.hide();
                mAsyncTask.initiate(MainActivity.this);
            }

        });

        settingsCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingsDialoge.hide();
                getPrefs(settingsDialoge);
            }
        });

        settingsRef.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingsDialoge.hide();
                referenceDialog.show();
            }
        });

        referenceCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                referenceDialog.hide();
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
            mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleClient);
            if (mLocation != null) {
                Log.d(TAG, "location not null");
                onLocationChanged(mLocation);
            } else {
                Log.i(TAG, "mLocation is null, starting location updater");
                LocationRequest locReq = new LocationRequest();
                locReq.create();
                locReq.setNumUpdates(1);
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleClient, locReq, this);

                //can now set default location
                Float firstLat =mPrefs.getFloat("lat",0);
                Float firstLon = mPrefs.getFloat("lon",0);
                mMapUtility.setPointAt(this,new LatLng(firstLat,firstLon));
            }
        } catch (SecurityException e) {
            Log.w(TAG, "need location permission:");
            e.printStackTrace();
            //switch to map to choose location manually
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG,"location updated");
        mLocation = location;
        mMapUtility.setPointAt(this,new LatLng(mLocation.getLatitude(),mLocation.getLongitude()));
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "connection to google failed");
    }

    protected void getPrefs(Dialog dialog){
        String unitsGlobal = mPrefs.getString("units","metric");
        String locationGlobal = mPrefs.getString("location","Guelph,ca");

        //Log.i(TAG,unitsGlobal);
        //Log.i(TAG,locationGlobal);

        //get pre-existing dialog values
        EditText locationValueGlobal = (EditText) settingsDialoge.findViewById(R.id.location);
        RadioButton Cglobal = (RadioButton) settingsDialoge.findViewById(R.id.metric);
        RadioButton Fglobal = (RadioButton) settingsDialoge.findViewById(R.id.imperial);

        //set dialog values
        locationValueGlobal.setText(locationGlobal);
        if (unitsGlobal.equals("metric")){
            Cglobal.setChecked(true);
        }
        else {
            Fglobal.setChecked(true);
        }
    }

    protected void showMap () {
        mMapView.setVisibility(View.VISIBLE);
        settingsButton.setVisibility(View.GONE);
        refreshButton.setVisibility(View.GONE);
    }

    public void hideMap () {
        mMapView.setVisibility(View.GONE);
        settingsButton.setVisibility(View.VISIBLE);
        refreshButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void processFinish(String output){
        final TextView textField = (TextView) findViewById(R.id.results);
        if (output.equals("failConnect")){
            textField.setText("Network Error :(");
        }
        if (output.equals("failed")){
            Toast toast = Toast.makeText(this, "Location failed to load. Try a different search. (E.g. 'toronto', 'toronto,ca')",Toast.LENGTH_LONG);
            toast.show();
        }
        else{

            final ImageView imageField = (ImageView) findViewById(R.id.imageViewBG);
            final TextView refTitle = (TextView) referenceDialog.findViewById(R.id.refImageName);
            final TextView refAuthor = (TextView) referenceDialog.findViewById(R.id.refAuthor);
            final TextView refURL = (TextView) referenceDialog.findViewById(R.id.refURL);
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
            textField.setTextColor(Color.parseColor(weatherInfo[10]));
            //itemText.setTextColor(Color.parseColor(weatherInfo[10]));
            int imageId = getResources().getIdentifier(weatherInfo[6], "drawable", getPackageName());
            imageField.setImageResource(imageId);
            refTitle.setText(weatherInfo[7]);
            refAuthor.setText("by "+weatherInfo[8]);
            refURL.setText(weatherInfo[9]);
        }

    }

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
            return true;
        }

        return super.onOptionsItemSelected(item);
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
}
