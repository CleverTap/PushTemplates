package com.clevertap.pushtemplates;

import android.graphics.Bitmap;
import android.util.LruCache;

class ImageCache {
    private static final int MIN_CACHE_SIZE = 1024 * 10; // 10mb minimum (in KB)
    private final static int maxMemory = (int) (Runtime.getRuntime().maxMemory())/1024;
    private final static int cacheSize = Math.max((maxMemory / 32), MIN_CACHE_SIZE);

    private static LruCache<String, Bitmap> mMemoryCache;

    static void init(){
        synchronized (ImageCache.class) {
            if(mMemoryCache == null) {
                PTLog.verbose("CTInAppNotification.ImageCache: init with max device memory: " + String.valueOf(maxMemory) + "KB and allocated cache size: " + String.valueOf(cacheSize) + "KB");
                try {
                    mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
                        @Override
                        protected int sizeOf(String key, Bitmap bitmap) {
                            // The cache size will be measured in kilobytes rather than
                            // number of items.
                            int size = getImageSizeInKB(bitmap);
                            PTLog.verbose( "CTInAppNotification.ImageCache: have image of size: "+size + "KB for key: " + key);
                            return size;
                        }
                    };
                } catch (Throwable t) {
                    PTLog.verbose( "CTInAppNotification.ImageCache: unable to initialize cache: ", t.getCause());
                }
            }
        }
    }

    public static int getImageSizeInKB(Bitmap bitmap) {
        return bitmap.getByteCount() / 1024;
    }

    public static int getAvailableMemory() {
        synchronized (ImageCache.class) {
            return mMemoryCache == null ? 0 : cacheSize - mMemoryCache.size();
        }
    }

    public static boolean isEmpty() {
        synchronized (ImageCache.class) {
            return mMemoryCache.size() <= 0;
        }
    }

    public static void cleanup() {
        synchronized (ImageCache.class) {
            if (isEmpty()) {
                PTLog.verbose( "CTInAppNotification.ImageCache: cache is empty, removing it");
                mMemoryCache = null;
            }
        }
    }

    static boolean addBitmap(String key, Bitmap bitmap) {

        if(mMemoryCache==null) return false;

        if (getBitmap(key) == null) {
            synchronized (ImageCache.class) {
                int imageSize = getImageSizeInKB(bitmap);
                int available = getAvailableMemory();
                PTLog.verbose( "CTInAppNotification.ImageCache: image size: "+ imageSize +"KB. Available mem: "+available+ "KB.");
                if (imageSize > getAvailableMemory()) {
                    PTLog.verbose( "CTInAppNotification.ImageCache: insufficient memory to add image: " + key);
                    return false;
                }
                mMemoryCache.put(key, bitmap);
                PTLog.verbose( "CTInAppNotification.ImageCache: added image for key: " + key);
            }
        }
        return true;
    }

    static Bitmap getBitmap(String key) {
        synchronized (ImageCache.class) {
            if(key!=null)
                return mMemoryCache == null ? null : mMemoryCache.get(key);
            else
                return null;
        }
    }

    static void removeBitmap(String key) {
        synchronized (ImageCache.class) {
            if (mMemoryCache == null) return;
            mMemoryCache.remove(key);
            PTLog.verbose( "CTInAppNotification.LruImageCache: removed image for key: " + key);
            cleanup();
        }
    }
}
