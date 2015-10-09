package com.example.ychabatarou.stickylist;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListAdapter;

import com.example.ychabatarou.stickylist.viewlifecycle.DispatchListener;

/**
 * Created by ychabatarou on 7.10.15.
 */
public class StickyListFrame extends FrameLayout implements DispatchListener {

    private final static String TAG = "StickyListFrame";
    ;


    private final StickyList mList;

    private boolean mIsScrollingUp;

    private View mStickyHeader;
    private final View mListHeader;


    /* --- Delegates --- */
    private AbsListView.OnScrollListener mOnScrollListenerDelegate;


    int mListHeaderH;

    public StickyListFrame(Context context, AttributeSet attrs) {
        super(context, attrs);
        mList = new StickyList(context);
        mList.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mList.setDivider(null);
        mList.setDividerHeight(0);

        LayoutInflater from = LayoutInflater.from(context);
        mStickyHeader = from.inflate(R.layout.sticky_header, null);
        mListHeader = from.inflate(R.layout.full_header, mList, false);
        mList.setOnScrollListener(new ListScrollListener());
        mList.setOnDispatchListener(this);


        Resources r = getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, r.getDisplayMetrics());

        mListHeaderH = (int) px;
        addView(mList);

    }

    public void setListAdapter(ListAdapter adapter) {
        mList.setAdapter(adapter);
        ;
        mList.addHeaderView(mListHeader);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        mList.layout(0, 0, mList.getMeasuredWidth(), getHeight());
        if (mStickyHeader != null) {
            MarginLayoutParams lp = (MarginLayoutParams) mStickyHeader.getLayoutParams();
            int headerTop = lp.topMargin;
            mStickyHeader.layout(0, headerTop, mStickyHeader.getMeasuredWidth()
                    + 0, headerTop + mStickyHeader.getMeasuredHeight());
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mStickyHeader != null) {
            ensureHeaderHasCorrectLayoutParams(mStickyHeader);
            measureHeader(mStickyHeader);
        }
    }


    private void measureHeader(View header) {
        if (header != null) {
            final int width = getMeasuredWidth() - 0 - 0;
            final int parentWidthMeasureSpec = MeasureSpec.makeMeasureSpec(
                    width, MeasureSpec.EXACTLY);
            final int parentHeightMeasureSpec = MeasureSpec.makeMeasureSpec(0,
                    MeasureSpec.UNSPECIFIED);
            measureChild(header, parentWidthMeasureSpec,
                    parentHeightMeasureSpec);
        }
    }

    private void ensureHeaderHasCorrectLayoutParams(View header) {
        ViewGroup.LayoutParams lp = header.getLayoutParams();
        if (lp == null) {
            lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            header.setLayoutParams(lp);
        } else if (lp.height == LayoutParams.MATCH_PARENT || lp.width == LayoutParams.WRAP_CONTENT) {
            lp.height = mListHeaderH;
            lp.width = LayoutParams.MATCH_PARENT;
            header.setLayoutParams(lp);
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        // Only draw the list here.
        // The header should be drawn right after the lists children are drawn.
        // This is done so that the header is above the list items
        // but below the list decorators (scroll bars etc).
        if (mList.getVisibility() == VISIBLE || mList.getAnimation() != null) {
            drawChild(canvas, mList, 0);
        }
    }


    private class ListScrollListener implements AbsListView.OnScrollListener {

        private int mLastFirstVisibleItem;
        private float mTranslationY = -100;

        // Speed //
        private int previousFirstVisibleItem = 0;
        private long previousEventTime = 0;
        private double scrollSpeed = 0;

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {
            final int adapterCount = mList.getAdapter() == null ? 0 : mList.getAdapter().getCount();

            if (mLastFirstVisibleItem < firstVisibleItem) {
                mIsScrollingUp = false;
            }
            if (mLastFirstVisibleItem > firstVisibleItem) {
                mIsScrollingUp = true;
            }

            if (previousFirstVisibleItem != firstVisibleItem){
                long currTime = System.currentTimeMillis();
                long timeToScrollOneElement = currTime - previousEventTime;
                scrollSpeed = ((double)1/timeToScrollOneElement)*1000;
                previousFirstVisibleItem = firstVisibleItem;
                previousEventTime = currTime;
            }

            mLastFirstVisibleItem = firstVisibleItem;

            if (mOnScrollListenerDelegate != null) {
                mOnScrollListenerDelegate.onScroll(view, firstVisibleItem,
                        visibleItemCount, totalItemCount);
            }


            final int headerViewCount = mList.getHeaderViewsCount();
            int headerPosition = firstVisibleItem - headerViewCount;

            final boolean doesListHaveChildren = mList.getChildCount() != 0;
            final boolean isFirstViewBelowTop = doesListHaveChildren
                    && mList.getFirstVisiblePosition() == 0
                    && mList.getChildAt(0).getTop() >= 0;
            final boolean isHeaderPositionOutsideAdapterRange = headerPosition > adapterCount - 1
                    || headerPosition < 0;

            if (!isFirstViewBelowTop && mIsScrollingUp) {
                if (mStickyHeader == null) {
                    mStickyHeader = LayoutInflater.from(getContext()).inflate(R.layout.sticky_header, null);
                    addView(mStickyHeader);
                    measureHeader(mStickyHeader);
                    mStickyHeader.setTranslationY(mTranslationY);
                } else {
                    if (isHeaderPositionOutsideAdapterRange) {
                        mTranslationY -= scrollSpeed;
                        if(mTranslationY <= -50){
                            mStickyHeader.setVisibility(View.GONE);
                        }
                    }

                    if (mTranslationY <= 0 && !isHeaderPositionOutsideAdapterRange) {
                        mTranslationY += scrollSpeed;
                        if(mTranslationY > 0){
                            mTranslationY = 0;
                        }
                    }
                    mStickyHeader.setTranslationY(mTranslationY);
                }
            } else {
                mTranslationY = -100;
                clearHeader();
            }
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }
    }

    @Override
    public void onDispatchDrawEvent(Canvas canvas) {
        if (mStickyHeader != null) {
            drawChild(canvas, mStickyHeader, 0);
        }
    }

    private void clearHeader() {
        if (mStickyHeader != null) {
            removeView(mStickyHeader);
            mStickyHeader = null;
        }
    }


    public void goTopListView(){
        if(mList!=null){
            mList.smoothScrollToPosition(0);
        }
    }
}
