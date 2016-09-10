package com.geofferson.what2wear;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
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
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
//import com.google.android.gms.common.api.GoogleApiClient;
//import com.google.android.gms.location.LocationServices;

import org.json.JSONObject;

public class MainActivity extends FragmentActivity implements
        asyncResponse, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback{

    protected getWeatherData mAsyncTask = new getWeatherData(MainActivity.this,MainActivity.this);
    protected data2clothes mData2clothes = new data2clothes();
    protected MapFragment mMapFragment;
    protected settings mSettings = new settings();
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

        mAsyncTask.initiate(MainActivity.this);//passing context so the class can access getsystemservice
        //Log.i(TAG, "started get weather");

        getCurrentLocation();
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

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAsyncTask.initiate(MainActivity.this);
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

        setUpMap();
    }

    private void setUpMap () {
        try {
            switch (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)) {
                case ConnectionResult.SUCCESS:
                    if (mMapFragment != null) {
                        Log.d(TAG, "calling getMapAsync");
                        mMapFragment.getMapAsync(this);
                    }
                    break;
                case ConnectionResult.SERVICE_MISSING:

                    break;
                case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:

                    break;
                default:

            }
        } catch (Exception e) {
            Log.d(TAG, "map set up failed");
        }
    }

    @Override
    public void onMapReady(final GoogleMap map) {

        Log.d(TAG, "onMapReady");
        if (map == null) {
            Log.d("", "Map Fragment Not Found or no Map in it!!");
        }
        LatLng CENTER = mSettings.returnLatLng(this);
        try {
            map.addMarker(new MarkerOptions()
                    .position(CENTER)
                    .title("sugar'n spice").snippet(""));
        } catch (Exception e) {
            e.printStackTrace();
        }

        map.setIndoorEnabled(true);
        //map.setMyLocationEnabled(true);
        map.moveCamera(CameraUpdateFactory.zoomTo(5));
        if (CENTER != null) {
            map.animateCamera(
                    CameraUpdateFactory.newLatLng(CENTER), 1750,
                    null);
        }
        // add circle
        //CircleOptions circle = new CircleOptions();
        //circle.center(CENTER).fillColor(Color.BLUE).radius(10);
        //map.addCircle(circle);
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
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
        mGoogleClient.connect();
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
                mSettings.saveCoords(this,mLocation.getLatitude(),mLocation.getLongitude());
            } else {
                Log.i(TAG, "mLocation is null, redirect to map to choose location manually");
            }
        } catch (SecurityException e) {
            Log.w(TAG, "need location permission");
            //switch to map to choose location manually
        }

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "connection to google failed");
    }

    protected void getPrefs(Dialog dialog){
        SharedPreferences prefs = getSharedPreferences("myPrefs",Context.MODE_PRIVATE);
        String unitsGlobal = prefs.getString("units","metric");
        String locationGlobal = prefs.getString("location","Guelph,ca");

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
        //getFragmentManager().beginTransaction().add(R.id.main,mMapFragment).commit();
        settingsButton.setVisibility(View.GONE);
        refreshButton.setVisibility(View.GONE);
    }

    public void hideMap () {
        //getFragmentManager().beginTransaction().remove(mMapFragment).commit();
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

}
