package com.nilhcem.bblfr.jobs.splashscreen.importdata;

import android.util.Pair;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Func2;

public class ImportService {

    private final ImportBaggers mBaggersService;
    private final ImportLocations mLocationsService;

    @Inject
    public ImportService(ImportBaggers importBaggers, ImportLocations importLocations) {
        mBaggersService = importBaggers;
        mLocationsService = importLocations;
    }

    public Observable<Pair<Boolean, Boolean>> importData() {
        return Observable.zip(
                mBaggersService.importData(),
                mLocationsService.importData(),
                new Func2<Boolean, Boolean, Pair<Boolean, Boolean>>() {
                    @Override
                    public Pair<Boolean, Boolean> call(Boolean res1, Boolean res2) {
                        return Pair.create(res1, res2);
                    }
                });
    }
}
