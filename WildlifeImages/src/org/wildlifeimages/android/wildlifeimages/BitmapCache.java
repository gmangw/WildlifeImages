package org.wildlifeimages.android.wildlifeimages;

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
	public final int SIZE;
	
	public static final int CACHE_MAX = 8;

	private LinkedHashMapRestricted<String, Bitmap> cachedBitmaps;
	private LinkedHashMapRestricted<String, Bitmap> cachedThumbs;
	LinkedList<String> sizeObserver = new LinkedList<String>();

	/**
	 * This will create the cached bitmaps and thumbs and store them in memory.
	 * 
	 */
	public BitmapCache(int size){
		SIZE = size;
		cachedBitmaps = new LinkedHashMapRestricted<String, Bitmap>();
		cachedThumbs = new LinkedHashMapRestricted<String, Bitmap>();
	}

	/**
	 * This will get the bitmap (full-size photo) from memory, so a picture in memory.
	 * 
	 * @param shortUrl a shortened URL from the assets directory.
	 * 
	 * @return The cached full-size bitmap.
	 * 
	 */
	public Bitmap getBitmap(String shortUrl) {
		Log.i(this.getClass().getName(), "Retrieved cached bitmap " + shortUrl);
		return cachedBitmaps.get(shortUrl);
	}

	/**
	 * This will get the thumbnail from memory, so a picture in memory.
	 * 
	 * @param shortUrl a shortened URL from the assets directory.
	 * 
	 * @return The cached thumbnail bitmap.
	 * 
	 */
	public Bitmap getThumb(String shortUrl){
		//Log.i(this.getClass().getName(), "Retrieved cached thumb " + shortUrl);
		return cachedThumbs.get(shortUrl);
	}

	/**
	 * This will return true if the large photo bitmap is in memory.
	 * Used to check if the large photo is in memory, or else it will be put there.
	 * 
	 * @param shortUrl a shortened URL from the assets directory.
	 * 
	 * @return True if there is a cached full-size bitmap of the image.
	 * 
	 */
	public boolean containsBitmap(String shortUrl) {
		return cachedBitmaps.containsKey(shortUrl);
	}

	/**
	 * This will return true if the thumbnail is in memory.
	 * Used to check if the thumbnail is in memory, or else it will be put there.
	 * 
	 * @param shortUrl a shortened URL from the assets directory.
	 * 
	 * @return True if there is a cached thumbnail bitmap of the image.
	 * 
	 */
	public boolean containsThumb(String shortUrl) {
		return cachedThumbs.containsKey(shortUrl);
	}

	/**
	 * This will put the thumbnail bitmap into memory.
	 * 
	 * @param shortUrl a shortened URL from the assets directory.
	 * @param bmp the bitmap to be stored in memory.
	 * 
	 */
	public void putThumb(String shortUrl, Bitmap bmp) {
		//Log.i(this.getClass().getName(), "Caching thumb " + shortUrl);
		Bitmap thumb;
		int width = bmp.getWidth();
		int height = bmp.getHeight();
		float aspect = 1.0f * width / height;

		if (aspect < 1.0f){
			thumb = Bitmap.createBitmap(bmp, 0, 0, width, width);
			thumb = Bitmap.createScaledBitmap(thumb, SIZE, SIZE, true);
		}else{
			thumb = Bitmap.createBitmap(bmp, (width - height)/2, 0, height, height);
			thumb = Bitmap.createScaledBitmap(thumb, SIZE, SIZE, true);
		}
		cachedThumbs.put(shortUrl, thumb);
	}

	/**
	 * This will store the full-size photo bitmap in memory.
	 * 
	 * @param shortUrl a shortened URL from the assets directory.
	 * @param bmp the bitmap to be stored in memory.
	 * 
	 */
	public void putBitmap(String shortUrl, Bitmap bmp) {
		cachedBitmaps.put(shortUrl, bmp);
		sizeObserver.addLast(shortUrl);
		if (sizeObserver.size() > CACHE_MAX){
			removeBitmap(sizeObserver.getFirst()); //TODO LinkedHashMap instead?
		}
		Log.w(this.getClass().getName(), "Bitmap cache using " + sizeObserver.size() + "/" + CACHE_MAX);
	}

	/**
	 * This will remove the full-size photo bitmap from memory.
	 * 
	 * @param shortUrl a shortened URL from the assets directory.
	 * 
	 */
	public void removeBitmap(String shortUrl) {
		if (cachedBitmaps.containsKey(shortUrl)){
			cachedBitmaps.get(shortUrl).recycle();
			cachedBitmaps.remove(shortUrl);
		}
		sizeObserver.remove(shortUrl);
		
	}
	
	public void removeThumb(String shortUrl){
		cachedThumbs.remove(shortUrl);
	}
	
	public void clear(){
		for (Bitmap b : cachedBitmaps.values()){
			b.recycle();
		}
		for (Bitmap b : cachedThumbs.values()){
			b.recycle();
		}
		cachedBitmaps.clear();
		cachedThumbs.clear();
		sizeObserver.clear();
	}
}
