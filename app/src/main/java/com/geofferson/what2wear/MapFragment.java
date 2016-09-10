package com.geofferson.what2wear;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by Geofferson on 2016-09-10.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = MapFragment.class.getSimpleName();
    private MapView mapView;
    private GoogleMap map;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.map_view, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mapView = (MapView) getView().findViewById(R.id.mapView);
        setUpMap();

        Button backButton = (Button) getView().findViewById(R.id.mapBack);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).hideMap();
            }
        });
    }

    private void setUpMap () {
        try {
            MapsInitializer.initialize(getActivity());
            switch (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getActivity())) {
            case ConnectionResult.SUCCESS:
                // Toast.makeText(getActivity(), "SUCCESS", Toast.LENGTH_SHORT)
                // .show();

                // Gets to GoogleMap from the MapView and does initialization
                // stuff
                if (mapView != null) {
                    Log.d(TAG, "calling getMapAsync");
                    mapView.getMapAsync(this);
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
        this.map = map;
        Log.d(TAG, "onMapReady");
        if (map == null) {

            Log.d("", "Map Fragment Not Found or no Map in it!!");

        }
        LatLng CENTER = new settings().returnLatLng(getActivity());
        map.clear();
        try {
            map.addMarker(new MarkerOptions().position(CENTER)
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
}
