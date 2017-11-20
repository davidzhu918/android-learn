package com.bignerdranch.android.photogallery;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import com.bignerdranch.android.photogallery.net.FlickrFetchr;
import com.bignerdranch.android.photogallery.net.Gallery.GalleryItem;
import com.bignerdranch.android.photogallery.net.ThumbnailDownloader;
import com.bignerdranch.android.photogallery.service.PollService;
import com.bignerdranch.android.photogallery.service.PollServiceFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zixiangz on 9/23/17.
 */

public class PhotoGalleryFragment extends VisibleFragment {
    private static final String TAG = "PhotoGalleryFragment";

    private RecyclerView mPhotoRecyclerView;
    private PhotoAdapter mAdapter;
    private int currentMaxPages;
    private ThumbnailDownloader<PhotoHolder> mThumbnailDownloader;
    private final PollService pollService;

    public static PhotoGalleryFragment newInstance() {
        return new PhotoGalleryFragment();
    }

    public PhotoGalleryFragment() {
        mAdapter = new PhotoAdapter(new ArrayList<GalleryItem>());
        currentMaxPages = 1;
        pollService = PollServiceFactory.getPollService();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        updateItems(1);

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
                    updateItems(currentMaxPages++);
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

    @Override
    public void onCreateOptionsMenu(final Menu menu, MenuInflater menuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater);
        menuInflater.inflate(R.menu.fragment_photo_gallery, menu);

        final MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d(TAG, "QueryTextSubmit: " + query);
                Context context = getActivity();
                // Save search query string before executing search task.
                QueryPreferences.setStoredQuery(context, query);
                updateItems(1);
                // Hide soft keyboard.
                InputMethodManager imm = (InputMethodManager) context
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
                // Collapse search view.
                searchView.onActionViewCollapsed();
                // Scroll to top on new query
                if (isAdded()) {
                    mPhotoRecyclerView.scrollToPosition(0);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d(TAG, "QueryTextChange: " + newText);
                return false;
            }
        });

        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = QueryPreferences.getStoredQuery(getActivity());
                searchView.setQuery(query, false);
            }
        });

        MenuItem toggleItem = menu.findItem(R.id.menu_item_toggle_polling);
        if (pollService.isServiceOn(getActivity())) {
            toggleItem.setTitle(R.string.stop_polling);
        } else {
            toggleItem.setTitle(R.string.start_polling);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_clear:
                // clear search query string before executing search task.
                QueryPreferences.setStoredQuery(getActivity(), null);
                updateItems(1);
                return true;
            case R.id.menu_item_toggle_polling:
                boolean shouldStartAlarm = !pollService.isServiceOn(getActivity());
                pollService.setServiceJob(getActivity(), shouldStartAlarm);
                getActivity().invalidateOptionsMenu(); // update toolbar option menu.
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateItems(int page) {
        String query = QueryPreferences.getStoredQuery(getActivity());
        Log.d(TAG, "updateItems: query preferences = " + query);
        new FetchItemTask(query).execute(page);
    }

    private class PhotoHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener {
        private ImageView mItemImageView;
        private GalleryItem mGalleryItem;

        public PhotoHolder(View itemView) {
            super(itemView);

            mItemImageView = (ImageView) itemView.findViewById(R.id.item_image_view);
            itemView.setOnClickListener(this);
        }

        public void bindDrawable(Drawable drawable) {
            mItemImageView.setImageDrawable(drawable);
        }

        public void bindGalleryItem(GalleryItem galleryItem) {
            mGalleryItem = galleryItem;
        }

        @Override
        public void onClick(View v) {
            Intent i = PhotoPageActivity
                    .newIntent(getActivity(), mGalleryItem.getPhotoPageUri());
            startActivity(i);
        }
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {
        private List<GalleryItem> mGalleryItems;

        public PhotoAdapter(List<GalleryItem> galleryItems) {
            mGalleryItems = galleryItems;
        }

        public void updateGalleryItems(List<GalleryItem> galleryItems) {
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
            GalleryItem galleryItem = mGalleryItems.get(position);
            Drawable placeholder = getResources().getDrawable(R.drawable.bill_up_close);
            holder.bindDrawable(placeholder);
            holder.bindGalleryItem(galleryItem);

            mThumbnailDownloader.queueThumbnail(holder, galleryItem.getUrl(), false);

//            for (int i = 1; i <= Math.min(10, position); i++) {
//                GalleryItem prevItem = mGalleryItems.get(position - i);
//                mThumbnailDownloader.queueThumbnail(holder, prevItem.getUrl(), true);
//            }
//            for (int i = 1; i <= Math.min(mGalleryItems.size() - position - 1, 10); i++) {
//                GalleryItem postItem = mGalleryItems.get(position + i);
//                mThumbnailDownloader.queueThumbnail(holder, postItem.getUrl(), true);
//            }
        }

        @Override
        public int getItemCount() {
            return mGalleryItems.size();
        }
    }

    private class FetchItemTask extends AsyncTask<Integer, Void, FetchItemResult> {
        private String mQuery;

        public FetchItemTask(String query) {
            mQuery = query;
        }

        @Override
        protected FetchItemResult doInBackground(Integer... params) {
            int page = params[0];
            if (mQuery == null) {
                return new FetchItemResult(new FlickrFetchr().fetchRecentPhotos(page), page != 1);
            } else {
                return new FetchItemResult(new FlickrFetchr().searchPhotos(mQuery, page), page != 1);
            }
        }

        @Override
        protected void onPostExecute(FetchItemResult result) {
            if (isAdded()) {
                if (result.isAppend) {
                    mAdapter.appendGalleryItems(result.items);
                } else {
                    mAdapter.updateGalleryItems(result.items);
                }
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    private class FetchItemResult {
        List<GalleryItem> items;
        boolean isAppend;

        public FetchItemResult(List<GalleryItem> items, boolean isAppend) {
            this.items = new ArrayList<>();
            this.items.addAll(items);
            this.isAppend = isAppend;
        }
    }
}
