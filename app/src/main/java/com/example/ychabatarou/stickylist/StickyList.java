package com.example.ychabatarou.stickylist;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.example.ychabatarou.stickylist.viewlifecycle.DispatchListener;

/**
 * Created by ychabatarou on 7.10.15.
 */
public class StickyList extends ListView {

    private final static String TAG = "StickyList";
    private DispatchListener mDispatchListener;
    private int[] mItemOffsetY;
    private int mHeight;

    public StickyList(Context context) {
        super(context);
    }

    public StickyList(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        mDispatchListener.onDispatchDrawEvent(canvas);
    }

    public void setOnDispatchListener(DispatchListener stickyListFrame) {
        mDispatchListener = stickyListFrame;
    }

    public void computeScrollY() {
        mHeight = 0;
        int mItemCount = getAdapter().getCount();
        if (mItemOffsetY == null) {
            mItemOffsetY = new int[mItemCount];
        }
        for (int i = 0; i < mItemCount; ++i) {
            View view = getAdapter().getView(i, null, this);
            view.measure(
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            mItemOffsetY[i] = mHeight;
            mHeight += view.getMeasuredHeight();
        }
    }


}
