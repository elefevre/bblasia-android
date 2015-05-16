package com.nilhcem.bblfr.jobs.baggers;

import android.content.Context;
import android.support.annotation.NonNull;

import com.nilhcem.bblfr.model.baggers.Bagger;
import com.nilhcem.bblfr.model.baggers.City;
import com.nilhcem.bblfr.model.baggers.Tag;
import com.nilhcem.bblfr.model.baggers.dao.BaggersDao;
import com.nilhcem.bblfr.model.baggers.dao.CitiesDao;
import com.nilhcem.bblfr.ui.baggers.list.BaggersListEntry;
import com.nilhcem.bblfr.ui.baggers.list.filter.TagsListEntry;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;

public class BaggersService {

    private final CitiesDao mCitiesDao;
    private final BaggersDao mBaggersDao;

    @Inject
    public BaggersService(CitiesDao citiesDao, BaggersDao baggersDao) {
        mCitiesDao = citiesDao;
        mBaggersDao = baggersDao;
    }

    public Observable<List<BaggersListEntry>> getBaggers(@NonNull final Context context, @NonNull final Long cityId, @NonNull final List<String> selectedTags) {
        return Observable.create(new Observable.OnSubscribe<List<BaggersListEntry>>() {
            @Override
            public void call(Subscriber<? super List<BaggersListEntry>> subscriber) {
                List<BaggersListEntry> entries = new ArrayList<>();
                List<Bagger> baggers = mBaggersDao.getBaggers(cityId, selectedTags);

                for (Bagger bagger : baggers) {
                    entries.add(new BaggersListEntry(context, bagger));
                }
                subscriber.onNext(entries);
                subscriber.onCompleted();
            }
        });
    }

    public Observable<List<TagsListEntry>> getBaggersTags(@NonNull final Long cityId) {
        return Observable.create(new Observable.OnSubscribe<List<TagsListEntry>>() {
            @Override
            public void call(Subscriber<? super List<TagsListEntry>> subscriber) {
                List<TagsListEntry> entries = new ArrayList<>();
                List<Tag> tags = mBaggersDao.getBaggersTags(cityId);

                for (Tag tag : tags) {
                    entries.add(new TagsListEntry(tag));
                }
                subscriber.onNext(entries);
                subscriber.onCompleted();
            }
        });
    }

    public Observable<List<City>> getBaggersCities() {
        return Observable.create(new Observable.OnSubscribe<List<City>>() {
            @Override
            public void call(Subscriber<? super List<City>> subscriber) {
                subscriber.onNext(mCitiesDao.getCities());
                subscriber.onCompleted();
            }
        });
    }
}
