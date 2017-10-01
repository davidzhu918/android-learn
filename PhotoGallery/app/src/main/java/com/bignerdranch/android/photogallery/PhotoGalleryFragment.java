package com.bignerdranch.android.photogallery;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zixiangz on 9/23/17.
 */

public class PhotoGalleryFragment extends Fragment {
    private static final String TAG = "PhotoGalleryFragment";

    private FetchItemTask mFetchItemTask;
    private RecyclerView mPhotoRecyclerView;
    private PhotoAdapter mAdapter;
    private int currentMaxPages;
    private ThumbnailDownloader<PhotoHolder> mThumbnailDownloader;

    public static PhotoGalleryFragment newInstance() {
        return new PhotoGalleryFragment();
    }

    public PhotoGalleryFragment() {
        mAdapter = new PhotoAdapter(new ArrayList<GalleryItem>());
        currentMaxPages = 1;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mFetchItemTask = new FetchItemTask();
        mFetchItemTask.execute(currentMaxPages++);

        Handler responseHandler = new Handler();
        mThumbnailDownloader = new ThumbnailDownloader<>(responseHandler);
        mThumbnailDownloader.setThumbnailDownloadListener(
                new ThumbnailDownloader.ThumbnailDownloadListener<PhotoHolder>() {
            @Override
            public void onThumbnailDownloaded(PhotoHolder photoHolder, Bitmap thumbnail) {
                Drawable drawable = new BitmapDrawable(getResources(), thumbnail);
                photoHolder.bindDrawable(drawable);
            }
        });

        mThumbnailDownloader.start();
        mThumbnailDownloader.getLooper();
        Log.i(TAG, "Background thread started");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_photo_gallery, container, false);
        mPhotoRecyclerView = (RecyclerView) v.findViewById(R.id.photo_recycler_view);
        mPhotoRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        mPhotoRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (!recyclerView.canScrollVertically(1)) {
                    mFetchItemTask = new FetchItemTask();
                    mFetchItemTask.execute(currentMaxPages++);
                    Log.i(TAG, "Reached end of list");
                }
            }
        });
        mPhotoRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mPhotoRecyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                float width  = mPhotoRecyclerView.getMeasuredWidth();
                float item_width = getResources().getDimension(R.dimen.gallery_item_width);
                int columns = Math.max((int) (width / item_width), 1);
                Log.i(TAG, "width: " + width + ", item_width: " + item_width + ", columns: " + columns);
                mPhotoRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), columns));
            }
        });

        if (isAdded()) {
            mPhotoRecyclerView.setAdapter(mAdapter);
        }

        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mThumbnailDownloader.clearQueue();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mThumbnailDownloader.quit();
        Log.i(TAG, "Background thread destroyed");
    }

    private class PhotoHolder extends RecyclerView.ViewHolder {
        private ImageView mItemImageView;

        public PhotoHolder(View itemView) {
            super(itemView);

            mItemImageView = (ImageView) itemView.findViewById(R.id.item_image_view);
        }

        public void bindDrawable(Drawable drawable) {
            mItemImageView.setImageDrawable(drawable);
        }
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {
        private List<GalleryItem> mGalleryItems;

        public PhotoAdapter(List<GalleryItem> galleryItems) {
            mGalleryItems = galleryItems;
        }

        public void appendGalleryItems(List<GalleryItem> galleryItems) {
            mGalleryItems.addAll(galleryItems);
        }

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.list_item_gallery, parent, false);
            PhotoHolder photoHolder = new PhotoHolder(view);
            return photoHolder;
        }

        @Override
        public void onBindViewHolder(PhotoHolder holder, int position) {
            Log.i(TAG, "onBindViewHolder at position: " + position);
            GalleryItem galleryItem = mGalleryItems.get(position);
            // Drawable placeholder = getResources().getDrawable(R.drawable.bill_up_close);
            // holder.bindDrawable(placeholder);
            mThumbnailDownloader.queueThumbnail(holder, galleryItem.getUrl(), false);
            for (int i = 1; i <= Math.min(10, position); i++) {
                GalleryItem prevItem = mGalleryItems.get(position - i);
                mThumbnailDownloader.queueThumbnail(holder, prevItem.getUrl(), true);
            }
            for (int i = 1; i <= Math.min(mGalleryItems.size() - position - 1, 10); i++) {
                GalleryItem postItem = mGalleryItems.get(position + i);
                mThumbnailDownloader.queueThumbnail(holder, postItem.getUrl(), true);
            }
        }

        @Override
        public int getItemCount() {
            return mGalleryItems.size();
        }
    }

    private class FetchItemTask extends AsyncTask<Integer, Void, List<GalleryItem>> {

        @Override
        protected List<GalleryItem> doInBackground(Integer... params) {
            return new FlickrFetchr().fetchItems(params[0]);
        }

        @Override
        protected void onPostExecute(List<GalleryItem> items) {
            if (isAdded()) {
                mAdapter.appendGalleryItems(items);
                mAdapter.notifyDataSetChanged();
            }
        }
    }
}
