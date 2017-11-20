package com.bignerdranch.android.photogallery.net;

import android.net.Uri;

import com.google.gson.annotations.SerializedName;

/**
 * Created by zixiangz on 9/28/17.
 */
public class Gallery {
    GalleryPhotoCollection photos;

    public class GalleryItem {
        @SerializedName("title")
        private String mCaption;

        @SerializedName("id")
        private String mId;

        @SerializedName("url_s")
        private String mUrl;

        @SerializedName("owner")
        private String mOwner;

        public String getId() {
            return mId;
        }

        public String getUrl() {
            return mUrl;
        }

        public String getOwner() {
            return mOwner;
        }

        public Uri getPhotoPageUri() {
            return Uri.parse("https://www.flickr.com/photos/")
                    .buildUpon()
                    .appendPath(mOwner)
                    .appendPath(mId)
                    .build();
        }

        @Override
        public String toString() {
            return mCaption;
        }

    }

    class GalleryPhotoCollection {
        GalleryItem[] photo;
    }
}
