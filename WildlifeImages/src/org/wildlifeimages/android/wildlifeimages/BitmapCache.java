package org.wildlifeimages.android.wildlifeimages;

import java.util.HashMap;

import android.graphics.Bitmap;
	
/**
 * This class will handle cached bitmaps, pretty much as hashtable.
 * 
 * @author Graham Wilkinson
 * @author Shady Glenn
 * @author Naveen Nanja
 * 	
 */	
public class BitmapCache {
	private static final int thumbSize = 96;
	
	private HashMap<String, Bitmap> cachedBitmaps;
	private HashMap<String, Bitmap> cachedThumbs;
	
	public BitmapCache(){
		cachedBitmaps = new HashMap<String, Bitmap>();
		cachedThumbs = new HashMap<String, Bitmap>();
	}

	public Bitmap get(String shortUrl) {
		return cachedBitmaps.get(shortUrl);
	}
	
	public Bitmap getThumb(String shortUrl){
		return cachedThumbs.get(shortUrl);
	}

	public boolean containsKey(String shortUrl) {
		return cachedBitmaps.containsKey(shortUrl);
	}

	public void put(String shortUrl, Bitmap bmp) {
		cachedBitmaps.put(shortUrl, bmp);
		
		Bitmap thumb;
		float aspect = 1.0f * bmp.getWidth() / bmp.getHeight();
		
		
		if (aspect < 1.0f){
			thumb = Bitmap.createScaledBitmap(bmp, thumbSize, (int)(thumbSize/aspect), true);
			thumb = Bitmap.createBitmap(thumb, 0, 0, thumbSize, thumbSize);
		}else{
			thumb = Bitmap.createScaledBitmap(bmp, (int)(thumbSize*aspect), thumbSize, true);
			thumb = Bitmap.createBitmap(thumb, 0, 0, thumbSize, thumbSize);
		}
		cachedThumbs.put(shortUrl, thumb);
	}

	public void remove(String shortUrl) {
		cachedBitmaps.remove(shortUrl);
		cachedThumbs.remove(shortUrl);
	}
}
