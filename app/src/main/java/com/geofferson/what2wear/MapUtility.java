package com.geofferson.what2wear;

import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by Geofferson on 2016-09-10.
 */
public class MapUtility {

    private static final String TAG = MapUtility.class.getSimpleName();

    protected void setUpMap (MainActivity context) {
        try {
            switch (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)) {
                case ConnectionResult.SUCCESS:
                    context.mMapFragment = (MapFragment) context.getFragmentManager().findFragmentById(R.id.map);
                    //Log.d(TAG, "calling getMapAsync");
                    context.mMapFragment.getMapAsync(context);
                    break;
                case ConnectionResult.SERVICE_MISSING:

                    break;
                case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:

                    break;
                default:

            }
        } catch (Exception e) {
            //Log.d(TAG, "map set up failed");
            Toast toast = Toast.makeText(context, "Google Play Services unavailable.",Toast.LENGTH_LONG);
            toast.show();
        }
    }

    protected void mapReady(GoogleMap map, final MainActivity context) {
        //Log.d(TAG, "onMapReady");
        context.mMap = map;

        if (map == null) {
            //Log.d("", "Map Fragment Not Found or no Map in it!!");
        } else {
            context.mGoogleClient.connect();

            context.mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    context.mMap.clear();
                    setPointAt(context,latLng);
                }
            });
            map.setIndoorEnabled(true);
            //map.setMyLocationEnabled(true);
            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
    }

    protected void setPointAt (MainActivity context, LatLng point) {
        context.mSettings.saveCoords(context,point.latitude,point.longitude);
        try {
            context.mMap.addMarker(new MarkerOptions()
                .position(point)
                .title("how dare you"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        context.mMap.moveCamera(CameraUpdateFactory.zoomTo(10));
        context.mMap.animateCamera(
                CameraUpdateFactory.newLatLng(point), 1750, null);
        context.reloadWeatherData();
    }

}
