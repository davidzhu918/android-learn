package com.example.zixiangzhu.draganddraw;

import android.content.Context;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

/**
 * Created by zixiangzhu on 12/7/17.
 */

public class CustomBehavior extends BottomSheetBehavior<FrameLayout> {

    private final static String TAG = "CustomBehavior";

    public CustomBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, FrameLayout child, View dependency) {
        return dependency instanceof BoxDrawingView;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, FrameLayout child, View dependency) {
        int[] dependencyLocation = new int[2];
        int[] childLocation = new int[2];

        Log.w(TAG, "Dependent layout changed: ");
        return false;
    }

}
