package com.nilhcem.bblfr.ui.locations;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Pair;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nilhcem.bblfr.BBLApplication;
import com.nilhcem.bblfr.R;
import com.nilhcem.bblfr.core.map.MapUtils;
import com.nilhcem.bblfr.core.utils.IntentUtils;
import com.nilhcem.bblfr.jobs.locations.LocationsService;
import com.nilhcem.bblfr.model.locations.Location;
import com.nilhcem.bblfr.ui.BaseMapActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import rx.Observable;
import rx.android.app.AppObservable;
import rx.functions.Action1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class LocationsMapActivity extends BaseMapActivity {

    private static final float DEFAULT_ZOOM = 11f;

    @Inject LocationsService mLocationsService;

    public static Intent createLaunchIntent(Context context) {
        return new Intent(context, LocationsMapActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BBLApplication.get(this).component().inject(this);
        getSupportActionBar().setTitle(R.string.location_toolbar_title);
    }

    @Override
    protected void loadMap() {
        unsubscribe(mSubscription);
        mSubscription = AppObservable.bindActivity(this,
                Observable.zip(
                        mLocationsService.getLocations(),
                        MapUtils.getGoogleMapObservable(mMapFragment),
                        new Func2<List<Location>, GoogleMap, Pair<List<Location>, GoogleMap>>() {
                            @Override
                            public Pair<List<Location>, GoogleMap> call(List<Location> locations, GoogleMap googleMap) {
                                return Pair.create(locations, googleMap);
                            }
                        }))
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<Pair<List<Location>, GoogleMap>>() {
                    @Override
                    public void call(final Pair<List<Location>, GoogleMap> pair) {
                        pair.second.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                            @Override
                            public void onMapLoaded() {
                                onHostsLoaded(pair.first, pair.second);
                            }
                        });
                    }
                });
    }

    private void onHostsLoaded(List<Location> locations, GoogleMap map) {
        Timber.d("BBL Hosts loaded from DB");
        onMapFinishedLoading();

        // Set the locations in the map.
        List<Marker> markers = new ArrayList<>();
        final Map<Marker, Location> markerLocations = new HashMap<>();
        for (Location location : locations) {
            Marker marker = map.addMarker(new MarkerOptions()
                            .position(MapUtils.gpsToLatLng(location.gps))
                            .icon(BitmapDescriptorFactory.defaultMarker(MapUtils.HUE_DEFAULT))
            );
            markers.add(marker);
            markerLocations.put(marker, location);
        }

        // Custom infowindow.
        map.setInfoWindowAdapter(new LocationsInfoWindowAdapter(this, markerLocations));

        // Open the company's website when clicking on the infowindow.
        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                String website = markerLocations.get(marker).website;
                if (!TextUtils.isEmpty(website)) {
                    IntentUtils.startSiteIntent(LocationsMapActivity.this, website);
                }
            }
        });

        // Zoom the map indicator to user's current position
        MapUtils.moveToCurrentLocation(map, markers, mLocationProvider.getLastKnownLocation(), DEFAULT_ZOOM);
    }
}
