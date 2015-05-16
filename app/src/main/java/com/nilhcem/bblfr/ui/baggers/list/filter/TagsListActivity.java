package com.nilhcem.bblfr.ui.baggers.list.filter;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.nilhcem.bblfr.R;
import com.nilhcem.bblfr.core.ui.recyclerview.EmptyRecyclerView;
import com.nilhcem.bblfr.core.ui.recyclerview.SimpleDividerItemDecoration;
import com.nilhcem.bblfr.core.utils.CompatibilityUtils;
import com.nilhcem.bblfr.jobs.baggers.BaggersService;
import com.nilhcem.bblfr.model.baggers.City;
import com.nilhcem.bblfr.ui.navigationdrawer.NavigationDrawerActivity;
import com.nilhcem.bblfr.ui.navigationdrawer.NavigationDrawerEntry;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import icepick.Icicle;
import rx.Subscription;
import rx.android.app.AppObservable;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public abstract class TagsListActivity extends NavigationDrawerActivity implements TagsListAdapter.OnFilterChangeListener {

    private static final String EXTRA_CITY = "mCity";

    @Inject protected BaggersService mBaggersService;

    // A way to perform multiple ButterKnife injections on the same instance object.
    FilterDrawerViewHolder mFilterDrawer;

    static class FilterDrawerViewHolder {
        @InjectView(R.id.filter_container) ViewGroup mContainer;
        @InjectView(R.id.filter_drawer_layout) DrawerLayout mLayout;
        @InjectView(R.id.filter_content_frame) FrameLayout mContent;
        @InjectView(R.id.filter_drawer_view) EmptyRecyclerView mRecyclerView;
        @InjectView(R.id.loading_view) ProgressBar mEmptyView;
        @InjectView(R.id.toolbar) Toolbar mToolbar;
    }

    private Subscription mTagsSubscription;
    TagsListAdapter mTagsAdapter;

    @Icicle protected boolean mIsFiltered;
    @Icicle ArrayList<TagsListEntry> mTags;

    protected City mCity;

    protected static Intent createLaunchIntent(@NonNull Context context, @NonNull Class clazz, @NonNull City city) {
        Intent intent = new Intent(context, clazz);
        intent.putExtra(EXTRA_CITY, city);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setDataFromExtra();
        initLayout();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mTags == null) {
            mTagsSubscription = AppObservable.bindActivity(this, mBaggersService.getBaggersTags(mCity.id))
                    .subscribeOn(Schedulers.io())
                    .subscribe(new Action1<List<TagsListEntry>>() {
                        @Override
                        public void call(List<TagsListEntry> tagsListEntries) {
                            Timber.d("Tags loaded from database");
                            mTags = new ArrayList<>(tagsListEntries);
                            mTagsAdapter.updateItems(mTags);
                        }
                    });
        } else {
            mTagsAdapter.updateItems(mTags, false);
        }
    }

    @Override
    protected void onStop() {
        unsubscribe(mTagsSubscription);
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.baggers_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        DrawerLayout drawer = mFilterDrawer.mLayout;

        if (drawer != null) {
            if (item.getItemId() == R.id.action_filter_baggers) {
                if (drawer.isDrawerOpen(GravityCompat.END)) {
                    drawer.closeDrawer(GravityCompat.END);
                } else {
                    drawer.openDrawer(GravityCompat.END);
                }
                return true;
            } else if (item.getItemId() == android.R.id.home && drawer.isDrawerVisible(GravityCompat.END)) {
                drawer.closeDrawer(GravityCompat.END);
                return super.onOptionsItemSelected(item);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mFilterDrawer.mLayout.isDrawerVisible(GravityCompat.END)) {
            mFilterDrawer.mLayout.closeDrawer(GravityCompat.END);
        } else if (mIsFiltered && !isNavigationDrawerVisible()) {
            resetFilter();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onFilterChanged(List<String> selectedTagsIds) {
        mIsFiltered = !selectedTagsIds.isEmpty();
    }

    @Override
    protected void onNavigationDrawerEntryClicked(NavigationDrawerEntry entry) {
        super.onNavigationDrawerEntryClicked(entry);
        if (mIsFiltered) {
            resetFilter();
        }
    }

    @Override
    public void setContentView(int layoutResID) {
        View inflated = LayoutInflater.from(this).inflate(layoutResID, mFilterDrawer.mContent, true);
        ButterKnife.inject(this, inflated);
        super.setContentView(mFilterDrawer.mContainer);
    }

    protected void resetFilter() {
        mIsFiltered = false;
        mTagsAdapter.resetFilter();
    }

    private void initLayout() {
        mFilterDrawer = new FilterDrawerViewHolder();
        View inflated = LayoutInflater.from(this).inflate(R.layout.tags_list_activity, getParentView(), false);
        ButterKnife.inject(mFilterDrawer, inflated);
        setSupportActionBar(mFilterDrawer.mToolbar);

        DrawerLayout drawer = mFilterDrawer.mLayout;
        drawer.setDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                lockNavigationDrawer();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                unlockNavigationDrawer();
            }
        });
        drawer.setDrawerShadow(R.drawable.drawer_shadow_end, GravityCompat.END);

        mFilterDrawer.mEmptyView.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.primary_light), PorterDuff.Mode.SRC_ATOP);

        EmptyRecyclerView recyclerView = mFilterDrawer.mRecyclerView;
        recyclerView.setEmptyView(mFilterDrawer.mEmptyView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(CompatibilityUtils.getDrawable(this, R.drawable.line_divider)));
        mTagsAdapter = new TagsListAdapter(this);
        recyclerView.setAdapter(mTagsAdapter);
    }

    private void setDataFromExtra() {
        mCity = getIntent().getParcelableExtra(EXTRA_CITY);
        if (mCity == null) {
            Timber.e("City must not be null");
        }
    }
}
