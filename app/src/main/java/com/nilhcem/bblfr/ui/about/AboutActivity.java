package com.nilhcem.bblfr.ui.about;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nilhcem.bblfr.R;
import com.nilhcem.bblfr.core.utils.IntentUtils;
import com.nilhcem.bblfr.ui.navigationdrawer.NavigationDrawerActivity;
import com.viewpagerindicator.CirclePageIndicator;

import butterknife.InjectView;
import butterknife.OnClick;
import icepick.Icicle;

public class AboutActivity extends NavigationDrawerActivity {

    private static final String ARTICLE_URL = "http://linsolas.github.io/blog/2013/02/09/lancez-vous-dans-les-brown-bag-lunches/";

    @InjectView(R.id.about_layout_container) ViewGroup mLayoutContainer;
    @InjectView(R.id.about_steps_container) ViewGroup mStepsContainer;
    @InjectView(R.id.about_last_step_container) ViewGroup mLastStepContainer;

    @InjectView(R.id.about_viewpager) ViewPager mViewPager;
    @InjectView(R.id.about_viewpager_indicator) CirclePageIndicator mViewPagerIndicator;

    @InjectView(R.id.about_title) TextView mTitle;
    @InjectView(R.id.about_content) TextView mContent;

    private int[] mTitlesIds = AboutPagerStepEntry.getTitles();
    private int[] mContentIds = AboutPagerStepEntry.getContents();

    @Icicle int mCurrentViewPagerItem;

    public static Intent createLaunchIntent(Context context) {
        return new Intent(context, AboutActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_activity);
        getSupportActionBar().setTitle(R.string.about_title);

        mViewPager.setAdapter(new AboutPagerAdapter());
        mViewPagerIndicator.setViewPager(mViewPager);
        mViewPagerIndicator.setOnPageChangeListener(new FadePageListener());

        mLayoutContainer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return mViewPager.onTouchEvent(event);
            }
        });
        mViewPager.setCurrentItem(mCurrentViewPagerItem, false);
    }

    @OnClick(R.id.about_see_article)
    public void onSeeArticleButtonClicked() {
        IntentUtils.startSiteIntent(this, ARTICLE_URL);
    }

    class FadePageListener extends ViewPager.SimpleOnPageChangeListener {

        private int mLastTextPosition = -1;
        private int mMaxPosition = mTitlesIds.length - 1;

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            float alpha = getAlpha(positionOffset);
            if (position < mMaxPosition - 1 || position == mMaxPosition - 1 && Float.floatToRawIntBits(positionOffset) == 0) {
                mStepsContainer.setAlpha(1);
                mLastStepContainer.setVisibility(View.GONE);

                setStepText(positionOffset < 0.5 ? position : position + 1);
                mTitle.setAlpha(alpha);
                mContent.setAlpha(alpha);
            } else {
                mLastStepContainer.setVisibility(View.VISIBLE);

                if (Float.floatToRawIntBits(positionOffset) == 0) {
                    mStepsContainer.setAlpha(0);
                    mLastStepContainer.setAlpha(1);
                } else if (positionOffset < 0.5f) {
                    mStepsContainer.setAlpha(alpha);
                    mLastStepContainer.setVisibility(View.GONE);
                } else {
                    mStepsContainer.setAlpha(0);
                    mLastStepContainer.setAlpha(alpha);
                }
                setStepText(mMaxPosition - 1);
            }
        }

        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
            mCurrentViewPagerItem = position;
        }

        private void setStepText(int position) {
            if (mLastTextPosition != position) {
                mTitle.setText(mTitlesIds[position]);
                mContent.setText(mContentIds[position]);
                mLastTextPosition = position;
            }
        }

        private float getAlpha(float positionOffset) {
            float alpha;
            if (positionOffset < 0.5f) {
                alpha = 1 - positionOffset * 2;
            } else {
                alpha = positionOffset * 2 - 1;
            }
            return alpha;
        }
    }
}
