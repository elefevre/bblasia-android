package com.nilhcem.bblfr.ui.splashscreen;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Pair;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.nilhcem.bblfr.BBLApplication;
import com.nilhcem.bblfr.R;
import com.nilhcem.bblfr.core.prefs.Preferences;
import com.nilhcem.bblfr.jobs.splashscreen.checkdata.CheckDataService;
import com.nilhcem.bblfr.jobs.splashscreen.importdata.ImportService;
import com.nilhcem.bblfr.model.baggers.City;
import com.nilhcem.bblfr.ui.BaseActivity;
import com.nilhcem.bblfr.ui.baggers.cities.CitiesMapActivity;
import com.nilhcem.bblfr.ui.baggers.list.BaggersListActivity;

import java.util.Date;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;
import rx.android.app.AppObservable;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

import static com.nilhcem.bblfr.core.utils.NetworkUtils.isNetworkAvailable;

public class SplashscreenActivity extends BaseActivity {

    // Download data at most once a day (1 * 24 * 60 * 60 * 1000).
    private static final long DOWNLOAD_DATA_INTERVAL = 86_400_000L;

    @Inject Preferences mPrefs;
    @Inject ImportService mImportService;
    @Inject CheckDataService mCheckDataService;

    @InjectView(R.id.splash_logo_container) ViewGroup mLogoContainer;
    @InjectView(R.id.splash_subtitle) TextView mSubtitle;
    @Optional @InjectView(R.id.splash_shimmer_container) ShimmerFrameLayout mShimmerContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BBLApplication.get(this).component().inject(this);
        setContentView(R.layout.splashscreen_activity);
        ButterKnife.inject(this);
        mSubtitle.setText(Html.fromHtml(getString(R.string.splash_subtitle)));
        animateLogo();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (shouldImportData(mPrefs.getLastDownloadDate(), isNetworkAvailable(this))) {
            mSubscription = AppObservable.bindActivity(this, mImportService.importData())
                    .subscribeOn(Schedulers.io())
                    .subscribe(new Action1<Pair<Boolean, Boolean>>() {
                        @Override
                        public void call(Pair<Boolean, Boolean> booleanBooleanPair) {
                            onAfterDataImported(true);
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            onImportError();
                        }
                    });
        } else {
            onAfterDataImported(false);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        ErrorDialogFragment dialog = (ErrorDialogFragment) getSupportFragmentManager().findFragmentByTag(ErrorDialogFragment.TAG);
        if (dialog != null) {
            dialog.dismiss();
        }

        super.onSaveInstanceState(outState);
    }

    /**
     * DO NOT download latest data if it was already downloaded less than a day ago,
     * OR if user has no Internet connection and has already some data.
     */
    private boolean shouldImportData(long lastDownloadDate, boolean internetEnabled) {
        boolean importData = true;

        if (lastDownloadDate > 0) {
            if (internetEnabled) {
                long now = new Date().getTime();
                importData = (now - lastDownloadDate) > DOWNLOAD_DATA_INTERVAL;
            } else {
                importData = false;
            }
        }
        return importData;
    }

    /**
     * Verifies data, then directs to the appropriate activity.
     */
    private void onAfterDataImported(final boolean saveDownloadDate) {
        unsubscribe(mSubscription);
        mSubscription = AppObservable.bindActivity(this, mCheckDataService.checkData(this))
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<Pair<Boolean, City>>() {
                    @Override
                    public void call(Pair<Boolean, City> result) {
                        Boolean hasValidData = result.first;
                        City city = result.second;

                        if (hasValidData) {
                            if (saveDownloadDate) {
                                Timber.d("Keep data download date");
                                mPrefs.setDownloadDate();
                            }
                            startNextActivity(city);
                        } else {
                            onImportError();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        onImportError();
                    }
                });
    }

    private void startNextActivity(City city) {
        Intent intent;
        if (city == null) {
            // First time the application is launched
            intent = CitiesMapActivity.createLaunchIntent(this, false);
        } else {
            // If user has already selected a city, or we found his nearest city
            // via his location, directs the user to the baggers list for this city.
            mPrefs.keepInMemory(city);
            intent = BaggersListActivity.createLaunchIntent(this, city);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    private void onImportError() {
        Timber.w("Network is unreachable or data is corrupted");
        mPrefs.reset();
        ErrorDialogFragment.create(!isNetworkAvailable(this)).show(getSupportFragmentManager(), ErrorDialogFragment.TAG);
    }

    private void animateLogo() {
        Animation localAnimation = AnimationUtils.loadAnimation(this, R.anim.splashscreen_logo);
        mLogoContainer.clearAnimation();
        mLogoContainer.setAnimation(localAnimation);
        mLogoContainer.startAnimation(localAnimation);

        if (mShimmerContainer != null) {
            mShimmerContainer.setDuration(900);
            mShimmerContainer.setBaseAlpha(0.65f);
            mShimmerContainer.startShimmerAnimation();
        }
    }
}
