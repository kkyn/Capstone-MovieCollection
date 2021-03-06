package com.example.android.fnlprjct;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

public class ImageLoaderHelper {

    private static ImageLoaderHelper sInstance;

    public static ImageLoaderHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new ImageLoaderHelper(context.getApplicationContext());
        }

        return sInstance;
    }

    //int cacheSize = 50 * 1024 * 1024; // 4MiB

    /*private final LruCache<String, Bitmap> mImageCache = new LruCache<String, Bitmap>(cacheSize){
        protected int sizeOf(String key, Bitmap value) {
            return value.getByteCount();
        }
    };*/
    // https://developer.android.com/reference/android/util/LruCache.html
    // https://developer.android.com/topic/performance/graphics/cache-bitmap.html
    private final LruCache<String, Bitmap> mImageCache = new LruCache<String, Bitmap>(10);

    private ImageLoader mImageLoader;

    private ImageLoaderHelper(Context applicationContext) {

        RequestQueue queue = Volley.newRequestQueue(applicationContext);

        ImageLoader.ImageCache imageCache = new ImageLoader.ImageCache() {

            @Override
            public void putBitmap(String key, Bitmap value) {
                mImageCache.put(key, value);
            }

            @Override
            public Bitmap getBitmap(String key) {
                return mImageCache.get(key);
            }
        };
        mImageLoader = new ImageLoader(queue, imageCache);
    }

    public ImageLoader getImageLoader() {
        return mImageLoader;
    }
}
