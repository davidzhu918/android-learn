package com.bignerdranch.android.criminalintent;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by zixiangz on 7/3/17.
 */

public class LargePhotoFragment extends DialogFragment {
    private static final String ARG_PATH = "path";

    private ImageView mImageView;

    public static LargePhotoFragment newInstance(String path) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_PATH, path);

        LargePhotoFragment fragment = new LargePhotoFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String path = (String) getArguments().getSerializable(ARG_PATH);

        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_photo, null);
        mImageView = (ImageView) v.findViewById(R.id.large_photo);
        mImageView.setImageBitmap(BitmapFactory.decodeFile(path));

        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setPositiveButton(android.R.string.ok, null)
                .create();
    }
}
