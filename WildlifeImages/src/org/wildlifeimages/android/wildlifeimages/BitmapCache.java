package org.wildlifeimages.android.wildlifeimages;

import java.util.HashMap;
import java.util.LinkedList;

import android.graphics.Bitmap;
import android.util.Log;

/**
 * This class will handle cached bitmaps, pretty much as hashtable.
 * 
 * @author Graham Wilkinson
 * @author Shady Glenn
 * @author Naveen Nanja
 * 	
 */	
public class BitmapCache {	
	private static final int SIZE = 96;
	public static final int CACHE_MAX = 8;

	private HashMap<String, Bitmap> cachedBitmaps;
	private HashMap<String, Bitmap> cachedThumbs;
	LinkedList<String> sizeObserver = new LinkedList<String>();

	public BitmapCache(){
		cachedBitmaps = new HashMap<String, Bitmap>();
		cachedThumbs = new HashMap<String, Bitmap>();
	}

	public Bitmap getBitmap(String shortUrl) {
		Log.i(this.getClass().getName(), "Retrieved cached bitmap " + shortUrl);
		return cachedBitmaps.get(shortUrl);
	}

	public Bitmap getThumb(String shortUrl){
		//Log.i(this.getClass().getName(), "Retrieved cached thumb " + shortUrl);
		return cachedThumbs.get(shortUrl);
	}

	public boolean containsBitmap(String shortUrl) {
		return cachedBitmaps.containsKey(shortUrl);
	}

	public boolean containsThumb(String shortUrl) {
		return cachedThumbs.containsKey(shortUrl);
	}

	public void putThumb(String shortUrl, Bitmap bmp) {
		//Log.i(this.getClass().getName(), "Caching thumb " + shortUrl);
		Bitmap thumb;
		int width = bmp.getWidth();
		int height = bmp.getHeight();
		float aspect = 1.0f * width / height;

		if (aspect < 1.0f){
			int newHeight = (int)(SIZE/aspect);
			thumb = Bitmap.createScaledBitmap(bmp, SIZE, newHeight, true);
			thumb = Bitmap.createBitmap(thumb, 0, 0, SIZE, SIZE);
		}else{
			int newWidth = (int)(SIZE*aspect);
			thumb = Bitmap.createScaledBitmap(bmp, newWidth, SIZE, true);
			thumb = Bitmap.createBitmap(thumb, (newWidth - SIZE)/2, 0, SIZE, SIZE);
		}
		cachedThumbs.put(shortUrl, thumb);
	}

	public void putBitmap(String shortUrl, Bitmap bmp) {
		cachedBitmaps.put(shortUrl, bmp);
		sizeObserver.addLast(shortUrl);
		if (sizeObserver.size() > CACHE_MAX){
			removeBitmap(sizeObserver.getFirst());
		}
		Log.w(this.getClass().getName(), "Bitmap cache using " + sizeObserver.size() + "/" + CACHE_MAX);
	}

	public void removeBitmap(String shortUrl) {
		if (cachedBitmaps.containsKey(shortUrl)){
			cachedBitmaps.get(shortUrl).recycle();
			cachedBitmaps.remove(shortUrl);
		}
		sizeObserver.remove(shortUrl);
		//cachedThumbs.remove(shortUrl);
	}
}
