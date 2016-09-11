package com.geofferson.what2wear;

import android.content.Context;
import android.util.Log;

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
                    Log.d(TAG, "calling getMapAsync");
                    context.mMapFragment.getMapAsync(context);
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

    protected void mapReady(GoogleMap map, MainActivity context) {
        Log.d(TAG, "onMapReady");
        context.mMap = map;
        if (map == null) {
            Log.d("", "Map Fragment Not Found or no Map in it!!");
        }
        LatLng pos = context.mSettings.returnLatLng(context);
        try {
            map.addMarker(new MarkerOptions()
                    .position(pos)
                    .title("sugar'n spice").snippet(""));
        } catch (Exception e) {
            e.printStackTrace();
        }

        map.setIndoorEnabled(true);
        //map.setMyLocationEnabled(true);

        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }

    protected void setPointAt (MainActivity context, LatLng point) {
        context.mMap.addMarker(new MarkerOptions()
                .position(point)
                .title("marker"));
        context.mMap.moveCamera(CameraUpdateFactory.zoomTo(5));
        context.mMap.animateCamera(
                CameraUpdateFactory.newLatLng(point), 1750, null);
    }

}
