package com.example.zixiangzhu.draganddraw;

import android.graphics.PointF;

/**
 * Created by zixiangzhu on 11/26/17.
 */

public class Box {
    private PointF mOrigin;
    private PointF mCurrent;

    public Box(PointF origin) {
        mOrigin = origin;
        mCurrent = origin;
    }

    public PointF getOrigin() {
        return mOrigin;
    }

    public void setOrigin(PointF mOrigin) {
        this.mOrigin = mOrigin;
    }

    public PointF getCurrent() {
        return mCurrent;
    }

    public void setCurrent(PointF mCurrent) {
        this.mCurrent = mCurrent;
    }
}
