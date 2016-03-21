package org.wordpress.android.ui.plans;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import org.wordpress.android.R;
import org.wordpress.android.util.AniUtils;
import org.wordpress.android.widgets.WPViewPager;

import java.util.ArrayList;
import java.util.List;

/**
 * post-purchase "on-boarding" experience
 */
public class PlanPostPurchaseActivity extends AppCompatActivity {

    private ViewPager mViewPager;
    private PageAdapter mPageAdapter;
    private TextView mTxtSkip;
    private TextView mTxtNext;
    private ViewGroup mIndicatorContainerView;

    private int mPrevPageNumber = 0;
    private static final int NUM_PAGES = 4;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.plan_post_purchase_activity);

        mTxtSkip = (TextView) findViewById(R.id.text_skip);
        mTxtNext = (TextView) findViewById(R.id.text_next);
        mIndicatorContainerView = (ViewGroup) findViewById(R.id.layout_indicator_container);

        mViewPager = (WPViewPager) findViewById(R.id.viewpager);
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (position != mPrevPageNumber) {
                    updateIndicator(position);
                    updateIndicator(mPrevPageNumber);
                }
                updateButtons();
                mPrevPageNumber = position;
            }
        });
        mViewPager.setAdapter(getPageAdapter());

        mTxtSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mTxtNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoNextPage();
            }
        });

        for (int i = 0; i < NUM_PAGES; i++) {
            getIndicator(i).setOnClickListener(mIndicatorClickListener);
        }
    }

    private PageAdapter getPageAdapter() {
        if (mPageAdapter == null) {
            List<Fragment> fragments = new ArrayList<>();
            for (int i = 0; i < NUM_PAGES; i++) {
                fragments.add(PlanPostPurchaseFragment.newInstance(i));
            }

            FragmentManager fm = getFragmentManager();
            mPageAdapter = new PageAdapter(fm, fragments);
        }
        return mPageAdapter;
    }

    private int getCurrentPage() {
        return mViewPager.getCurrentItem();
    }

    private boolean isLastPage() {
        return getCurrentPage() == NUM_PAGES - 1;
    }

    private void gotoNextPage() {
        if (isLastPage()) {
            finish();
        } else {
            gotoPage(getCurrentPage() + 1);
        }
    }

    private void gotoPage(int pageNumber) {
        mViewPager.setCurrentItem(pageNumber, true);
    }

    private void updateButtons() {
        if (isLastPage()) {
            mTxtNext.setText(R.string.button_done);
            if (mTxtSkip.getVisibility() == View.VISIBLE) {
                AniUtils.fadeOut(mTxtSkip, AniUtils.Duration.MEDIUM);
            }
        } else {
            mTxtNext.setText(R.string.button_next);
            if (mTxtSkip.getVisibility() != View.VISIBLE) {
                AniUtils.fadeIn(mTxtSkip, AniUtils.Duration.MEDIUM);
            }
        }
    }

    private ImageView getIndicator(int pageNumber) {
        @IdRes int resId;
        switch (pageNumber) {
            case 0:
                resId = R.id.image_indicator_0;
                break;
            case 1:
                resId = R.id.image_indicator_1;
                break;
            case 2:
                resId = R.id.image_indicator_2;
                break;
            case 3:
                resId = R.id.image_indicator_3;
                break;
            default:
                throw new IllegalArgumentException("Invalid indicator page number");
        }
        return (ImageView) mIndicatorContainerView.findViewById(resId);
    }

    private void updateIndicator(int pageNumber) {
        boolean isSelected = (pageNumber == getCurrentPage());
        final ImageView indicator = getIndicator(pageNumber);
        final @DrawableRes int backgroundRes =
                isSelected ? R.drawable.indicator_circle_selected : R.drawable.indicator_circle_unselected;

        if (isSelected) {
            // scale it out, change the background, then scale it back in
            PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 0.25f);
            PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 0.25f);
            ObjectAnimator anim = ObjectAnimator.ofPropertyValuesHolder(indicator, scaleX, scaleY);
            anim.setDuration(150);
            anim.setInterpolator(new AccelerateInterpolator());
            anim.setRepeatCount(1);
            anim.setRepeatMode(ValueAnimator.REVERSE);
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationRepeat(Animator animation) {
                    indicator.setBackgroundResource(backgroundRes);
                }
            });
            anim.start();
        } else {
            indicator.setBackgroundResource(backgroundRes);
        }
    }

    private final View.OnClickListener mIndicatorClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.image_indicator_0) {
                gotoPage(0);
            } else if (id == R.id.image_indicator_1) {
                gotoPage(1);
            } else if (id == R.id.image_indicator_2) {
                gotoPage(2);
            } else if (id == R.id.image_indicator_3) {
                gotoPage(3);
            }
        }
    };

    private class PageAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragments;

        PageAdapter(FragmentManager fm, List<Fragment> fragments) {
            super(fm);
            mFragments = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }
    }
}
