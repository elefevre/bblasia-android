package com.nilhcem.bblfr;

import android.app.Application;
import android.content.Context;

import com.nilhcem.bblfr.core.db.Database;
import com.nilhcem.bblfr.core.log.ReleaseTree;

import dagger.Module;
import dagger.ObjectGraph;
import timber.log.Timber;

public class BBLApplication extends Application {

    private ObjectGraph mObjectGraph;

    public static BBLApplication get(Context context) {
        return (BBLApplication) context.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initLogger();
        initObjectGraph();
        Database.init(this);
    }

    private void initLogger() {
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new ReleaseTree());
        }
    }

    private void initObjectGraph() {
        mObjectGraph = ObjectGraph.create(getModules());
    }

    Object[] getModules() {
        return new Object[]{new BBLModule(this)};
    }

    public void inject(Object target) {
        mObjectGraph.inject(target);
    }
}
