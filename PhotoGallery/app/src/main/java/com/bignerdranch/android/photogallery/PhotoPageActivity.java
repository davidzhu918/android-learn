package com.bignerdranch.android.photogallery;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;

/**
 * Created by zixiangzhu on 11/19/17.
 */

public class PhotoPageActivity extends SingleFragmentActivity {
    public interface BackPressedListener {
        boolean onBackPressed();
    }

    private BackPressedListener backPressedListener;

    public static Intent newIntent(Context context, Uri photoPageUri) {
        Intent i = new Intent(context, PhotoPageActivity.class);
        i.setData(photoPageUri);
        return i;
    }

    @Override
    protected Fragment createFragment() {
        PhotoPageFragment fragment = PhotoPageFragment.newInstance(getIntent().getData());
        backPressedListener = fragment;
        return fragment;
    }

    @Override
    public void onBackPressed() {
        if (!backPressedListener.onBackPressed()) {
            super.onBackPressed();
        }
    }
}
