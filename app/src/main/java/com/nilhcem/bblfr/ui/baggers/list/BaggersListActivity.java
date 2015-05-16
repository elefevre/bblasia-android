package com.nilhcem.bblfr.ui.baggers.list;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

import com.nilhcem.bblfr.BBLApplication;
import com.nilhcem.bblfr.R;
import com.nilhcem.bblfr.core.ui.recyclerview.EmptyRecyclerView;
import com.nilhcem.bblfr.core.utils.NetworkUtils;
import com.nilhcem.bblfr.model.baggers.City;
import com.nilhcem.bblfr.ui.baggers.list.filter.TagsListActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.InjectView;
import icepick.Icicle;
import rx.android.app.AppObservable;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class BaggersListActivity extends TagsListActivity {

    @InjectView(R.id.baggers_list_recycler_view) EmptyRecyclerView mRecyclerView;
    @InjectView(R.id.loading_view) View mEmptyView;

    private BaggersListAdapter mAdapter;
    @Icicle ArrayList<BaggersListEntry> mBaggers;

    public static Intent createLaunchIntent(@NonNull Context context, City city) {
        return TagsListActivity.createLaunchIntent(context, BaggersListActivity.class, city);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BBLApplication.get(this).component().inject(this);

        setContentView(R.layout.baggers_list_activity);
        setToolbarTitle(0);
        initRecyclerView();

        if (savedInstanceState != null) {
            updateAdapter(mBaggers);
        }
    }

    @Override
    public void onFilterChanged(List<String> selectedTags) {
        super.onFilterChanged(selectedTags);
        unsubscribe(mSubscription);
        mAdapter.updateItems(null);
        mRecyclerView.scrollToPosition(0);

        mSubscription = AppObservable.bindActivity(this, mBaggersService.getBaggers(this, mCity.id, selectedTags))
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<List<BaggersListEntry>>() {
                    @Override
                    public void call(List<BaggersListEntry> baggersListEntries) {
                        Timber.d("Baggers loaded from DB");
                        mBaggers = new ArrayList<>(baggersListEntries);
                        updateAdapter(baggersListEntries);
                    }
                });
    }

    @Override
    protected void resetFilter() {
        super.resetFilter();
        setToolbarTitle(0);
    }

    void setToolbarTitle(int nbBaggers) {
        String title;

        if (!mIsFiltered) {
            title = getString(R.string.baggers_list_toolbar_title, mCity.name);
        } else if (nbBaggers == 1) {
            title = getString(R.string.baggers_list_toolbar_title_one, mCity.name);
        } else {
            title = getString(R.string.baggers_list_toolbar_title_many, nbBaggers, mCity.name);
        }
        getSupportActionBar().setTitle(title);
    }

    private void initRecyclerView() {
        mRecyclerView.setEmptyView(mEmptyView);

        if (getResources().getBoolean(R.bool.baggers_list_staggered_mode)) {
            mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        } else {
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        }
        mAdapter = new BaggersListAdapter();
        mRecyclerView.setAdapter(mAdapter);
    }

    private void updateAdapter(List<BaggersListEntry> baggers) {
        if (baggers != null && !baggers.isEmpty()) {
            mAdapter.updateItems(baggers, NetworkUtils.getAbsoluteUrl(mCity.picture), true);
            setToolbarTitle(baggers.size());
        }
    }
}
