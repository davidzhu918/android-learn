package com.bignerdranch.android.photogallery.net;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.util.LruCache;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by zixiangz on 9/30/17.
 */

public class ThumbnailDownloader<T> extends HandlerThread {
    private static final String TAG = "ThumbnailDownloader";
    private static final int MESSAGE_DOWNLOAD = 0;
    private static final int MESSAGE_PREFETCH = 1;

    private final LruCache<String, Bitmap> mLruCache = new LruCache<>(100);
    private final ConcurrentMap<T, String> mRequestMap = new ConcurrentHashMap<>();

    private boolean mHasQuit = false;
    private Handler mRequestHandler;
    private Handler mResponseHandler;
    private ThumbnailDownloadListener mThumbnailDownloadListener;

    public interface ThumbnailDownloadListener<T> {
        void onThumbnailDownloaded(T target, Bitmap thumbnail);
    }

    public void setThumbnailDownloadListener(ThumbnailDownloadListener<T> listener) {
        mThumbnailDownloadListener = listener;
    }

    public ThumbnailDownloader(Handler responseHandler) {
        super(TAG);
        mResponseHandler = responseHandler;
    }

    @Override
    protected void onLooperPrepared() {
        mRequestHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                T target = (T) msg.obj;
                switch (msg.what) {
                    case MESSAGE_DOWNLOAD:
                        Log.i(TAG, "Got a download request for URL: " + mRequestMap.get(target));
                        handleRequest(target, true);
                        break;
                    case MESSAGE_PREFETCH:
                        Log.i(TAG, "Got a prefetch request for URL: " + mRequestMap.get(target));
                        handleRequest(target, false);
                        break;
                    default:
                        break;
                }
            }
        };
    }

    @Override
    public boolean quit() {
        mHasQuit = true;
        return super.quit();
    }

    public void queueThumbnail(T target, String url, boolean isPrefetch) {
        Log.i(TAG, "Got a URL: " + url);

        if (url == null) {
            mRequestMap.remove(target);
        } else {
            mRequestMap.put(target, url);
            if (isPrefetch) {
                mRequestHandler.obtainMessage(MESSAGE_PREFETCH, target).sendToTarget();
            } else {
                mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD, target).sendToTarget();
            }
        }
    }

    public void clearQueue() {
        mRequestHandler.removeMessages(MESSAGE_DOWNLOAD);
        mRequestMap.clear();
    }

    private void handleRequest(final T target, boolean notifyThumbnailDownloadedListener) {
        try {
            final String url = mRequestMap.get(target);
            if (url == null) {
                return;
            }

            // Get Bitmap image from cache; if image is not in cache, fetch image using given url.
            final Bitmap bitmap;
            if (mLruCache.get(url) != null) {
                bitmap = mLruCache.get(url);
                Log.i(TAG, "Bitmap fetched");
            } else {
                byte[] bitmapBytes = new FlickrFetchr().getUrlBytes(url);
                bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
                mLruCache.put(url, bitmap);
                Log.i(TAG, "Bitmap created");
            }

//            byte[] bitmapBytes = new FlickrFetchr().getUrlBytes(url);
//            bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
//            mLruCache.put(url, bitmap);
//            Log.i(TAG, "Bitmap created");

            if (notifyThumbnailDownloadedListener) {
                mResponseHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mRequestMap.get(target) != url || mHasQuit) {
                            return;
                        }
                        mRequestMap.remove(target);
                        mThumbnailDownloadListener.onThumbnailDownloaded(target, bitmap);
                    }
                });
            }
        } catch (IOException ioe) {
            Log.e(TAG, "Error downloading image", ioe);
        }
    }

}
