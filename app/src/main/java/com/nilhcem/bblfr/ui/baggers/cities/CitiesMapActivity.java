package com.nilhcem.bblfr.ui.baggers.cities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Pair;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nilhcem.bblfr.BBLApplication;
import com.nilhcem.bblfr.R;
import com.nilhcem.bblfr.core.map.MapUtils;
import com.nilhcem.bblfr.core.utils.AppUtils;
import com.nilhcem.bblfr.core.utils.NetworkUtils;
import com.nilhcem.bblfr.jobs.baggers.BaggersService;
import com.nilhcem.bblfr.model.baggers.City;
import com.nilhcem.bblfr.ui.BaseMapActivity;
import com.nilhcem.bblfr.ui.baggers.cities.fallback.CitiesFallbackActivity;
import com.nilhcem.bblfr.ui.baggers.list.BaggersListActivity;

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

public class CitiesMapActivity extends BaseMapActivity {

    private static final float DEFAULT_ZOOM = 10f;

    @Inject BaggersService mBaggersService;

    public static Intent createLaunchIntent(@NonNull Context context, boolean withNavigationDrawer) {
        Intent intent = new Intent(context,
                NetworkUtils.isNetworkAvailable(context) && AppUtils.hasGooglePlayServices(context)
                        ? CitiesMapActivity.class : CitiesFallbackActivity.class);
        intent.putExtra(EXTRA_DISABLE_DRAWER, !withNavigationDrawer);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BBLApplication.get(this).component().inject(this);
        getSupportActionBar().setTitle(R.string.baggers_map_toolbar_title);
    }

    @Override
    protected void loadMap() {
        mSubscription = AppObservable.bindActivity(this,
                Observable.zip(
                        mBaggersService.getBaggersCities(),
                        MapUtils.getGoogleMapObservable(mMapFragment),
                        new Func2<List<City>, GoogleMap, Pair<List<City>, GoogleMap>>() {
                            @Override
                            public Pair<List<City>, GoogleMap> call(List<City> cities, GoogleMap googleMap) {
                                return Pair.create(cities, googleMap);
                            }
                        })
                        .subscribeOn(Schedulers.io()))
                .subscribe(new Action1<Pair<List<City>, GoogleMap>>() {
                    @Override
                    public void call(final Pair<List<City>, GoogleMap> pair) {
                        pair.second.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                            @Override
                            public void onMapLoaded() {
                                onCitiesLoaded(pair.first, pair.second);
                            }
                        });
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Timber.e(throwable, "Error getting baggers cities");
                        Toast.makeText(CitiesMapActivity.this, R.string.baggers_map_error, Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    private void onCitiesLoaded(List<City> cities, GoogleMap map) {
        Timber.d("BBL Cities loaded from DB");
        onMapFinishedLoading();

        // Set the locations in the map.
        List<Marker> markers = new ArrayList<>();
        final Map<Marker, City> markerCities = new HashMap<>();
        for (City city : cities) {
            Marker marker = map.addMarker(new MarkerOptions()
                            .position(new LatLng(city.lat, city.lng))
                            .title(city.name)
                            .icon(BitmapDescriptorFactory.defaultMarker(MapUtils.HUE_DEFAULT))
            );
            markers.add(marker);
            markerCities.put(marker, city);
        }

        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Timber.d("City selected");
                City city = markerCities.get(marker);
                mPrefs.setFavoriteCity(city);
                startActivity(BaggersListActivity.createLaunchIntent(CitiesMapActivity.this, city));
                return true;
            }
        });

        // Zoom the map indicator to user's current position
        MapUtils.moveToCurrentLocation(map, markers, mLocationProvider.getLastKnownLocation(), DEFAULT_ZOOM);
    }
}
