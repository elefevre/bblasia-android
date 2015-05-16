package com.nilhcem.bblfr.jobs.splashscreen.checkdata;

import android.content.Context;
import android.location.Location;
import android.util.Pair;

import com.nilhcem.bblfr.core.map.LocationProvider;
import com.nilhcem.bblfr.core.prefs.Preferences;
import com.nilhcem.bblfr.model.baggers.City;
import com.nilhcem.bblfr.model.baggers.dao.BaggersDao;
import com.nilhcem.bblfr.model.baggers.dao.CitiesDao;
import com.nilhcem.bblfr.model.locations.dao.LocationsDao;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func2;
import timber.log.Timber;

public class CheckDataService {

    private final Preferences mPrefs;
    private final CitiesDao mCitiesDao;
    private final BaggersDao mBaggersDao;
    private final LocationsDao mLocationsDao;
    private final LocationProvider mProvider;

    @Inject
    public CheckDataService(Preferences prefs, CitiesDao citiesDao, BaggersDao baggersDao, LocationsDao locationsDao, LocationProvider locationProvider) {
        mPrefs = prefs;
        mCitiesDao = citiesDao;
        mBaggersDao = baggersDao;
        mLocationsDao = locationsDao;
        mProvider = locationProvider;
    }

    public Observable<Pair<Boolean, City>> checkData(Context context) {
        return Observable.zip(
                checkDatabase(),
                getFavoriteCity(context),
                new Func2<Boolean, City, Pair<Boolean, City>>() {
                    @Override
                    public Pair<Boolean, City> call(Boolean checkDb, City city) {
                        return Pair.create(checkDb, city);
                    }
                });
    }

    private Observable<Boolean> checkDatabase() {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                subscriber.onNext(mCitiesDao.hasData() && mBaggersDao.hasData() && mLocationsDao.hasData());
                subscriber.onCompleted();
            }
        });
    }

    private Observable<City> getFavoriteCity(final Context context) {
        return Observable.create(new Observable.OnSubscribe<City>() {
            @Override
            public void call(Subscriber<? super City> subscriber) {
                mProvider.initSync(context);
                City city = null;

                Pair<String, String> latLng = mPrefs.getFavoriteCityLatLng();
                if (latLng != null) {
                    Timber.d("Favorite city found");
                    city = mCitiesDao.getCityByLatLng(latLng.first, latLng.second);
                }

                if (city == null) {
                    Timber.d("Try to find the closest city");
                    city = findClosestCity(mProvider.getLastKnownLocation(), mCitiesDao.getCities());
                    if (city != null) {
                        mPrefs.setFavoriteCity(city);
                    }
                }
                subscriber.onNext(city);
                subscriber.onCompleted();
            }
        });
    }

    City findClosestCity(Location location, List<City> cities) {
        City closestCity = null;
        float closestDistance = Float.MAX_VALUE;

        if (location != null && cities != null) {
            for (City city : cities) {
                float[] results = new float[1];
                Location.distanceBetween(city.lat, city.lng, location.getLatitude(), location.getLongitude(), results);
                if (results[0] < closestDistance) {
                    closestCity = city;
                    closestDistance = results[0];
                }
            }
        }
        return closestCity;
    }
}
