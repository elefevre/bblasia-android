package com.nilhcem.bblfr.jobs.locations;

import com.nilhcem.bblfr.model.locations.Location;
import com.nilhcem.bblfr.model.locations.dao.LocationsDao;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;

public class LocationsService {

    private final LocationsDao mDao;

    @Inject
    public LocationsService(LocationsDao dao) {
        mDao = dao;
    }

    public Observable<List<Location>> getLocations() {
        return Observable.create(new Observable.OnSubscribe<List<Location>>() {
            @Override
            public void call(Subscriber<? super List<Location>> subscriber) {
                subscriber.onNext(mDao.getLocations());
                subscriber.onCompleted();
            }
        });
    }
}
