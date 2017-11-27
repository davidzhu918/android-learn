package com.example.zixiangzhu.draganddraw;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zixiangzhu on 11/26/17.
 */

public class BoxDrawingView extends View {
    private static final String TAG = "BoxDrawingView";

    private Box mCurrentBox;
    private List<Box> mBoxen = new ArrayList<>();
    private Paint mBoxPaint;
    private Paint mBackgroundPaint;

    public BoxDrawingView(Context context) {
        this(context, null);
    }

    public BoxDrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Paint the boxes a nice semitransparent red (ARGB)
        mBoxPaint = new Paint();
        mBoxPaint.setColor(0x22ff0000);

        // Paint the background off-white
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(0xfff8efe0);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        PointF current = new PointF(event.getX(), event.getY());
        String action = "";

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                action = "ACTION_DOWN";
                // Reset drawing state
                mCurrentBox = new Box(current);
                mBoxen.add(mCurrentBox);
                break;
            case MotionEvent.ACTION_MOVE:
                action = "ACTION_MOVE";
                if (mCurrentBox != null) {
                    mCurrentBox.setCurrent(current);
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                action = "ACTION_UP";
                mCurrentBox = null;
                break;
            case MotionEvent.ACTION_CANCEL:
                action = "ACTION_CANCEL";
                mCurrentBox = null;
                break;
        }
        Log.i(TAG, action + " at x = " + current.x + ", y = " + current.y);
        return true;
    }

    @Override
    public void onDraw(Canvas canvas) {
        // Fill the background.
        canvas.drawPaint(mBackgroundPaint);

        for (Box box : mBoxen) {
            float left = Math.min(box.getOrigin().x, box.getCurrent().x);
            float right = Math.max(box.getOrigin().x, box.getCurrent().x);
            float top = Math.min(box.getOrigin().y, box.getCurrent().y);
            float bottom = Math.max(box.getOrigin().y, box.getCurrent().y);

            canvas.drawRect(left, top, right, bottom, mBoxPaint);
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();

        int n = mBoxen.size();
        float[] leftArray = new float[n];
        float[] rightArray = new float[n];
        float[] topArray = new float[n];
        float[] bottomArray = new float[n];

        for (int i = 0; i < n; i++) {
            Box box = mBoxen.get(i);
            leftArray[i] = Math.min(box.getOrigin().x, box.getCurrent().x);
            rightArray[i] = Math.max(box.getOrigin().x, box.getCurrent().x);
            topArray[i] = Math.min(box.getOrigin().y, box.getCurrent().y);
            bottomArray[i] = Math.max(box.getOrigin().y, box.getCurrent().y);
        }
        bundle.putFloatArray("left", leftArray);
        bundle.putFloatArray("right", rightArray);
        bundle.putFloatArray("top", topArray);
        bundle.putFloatArray("bottom", bottomArray);
        bundle.putParcelable("parent", super.onSaveInstanceState());

        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Log.w(TAG, "onRestoreInstanceState");
        Bundle bundle = (Bundle) state;
        super.onRestoreInstanceState(bundle.getParcelable("parent"));

        float[] leftArray = bundle.getFloatArray("left");
        float[] rightArray = bundle.getFloatArray("right");
        float[] topArray = bundle.getFloatArray("top");
        float[] bottomArray = bundle.getFloatArray("bottom");

        Log.w(TAG, "n = " + leftArray.length);
        for (int i = 0; i < leftArray.length; i++) {
            Box box = new Box(new PointF(leftArray[i], topArray[i]));
            box.setCurrent(new PointF(rightArray[i], bottomArray[i]));
            Log.i(TAG, "left = " + leftArray[i] + "; top = " + topArray[i] + "; right = " + rightArray[i] + "; bottom = " + bottomArray[i]);
            mBoxen.add(box);
        }
    }

}
