package org.wildlifeimages.android.wildlifeimages;

import java.util.HashMap;

import android.graphics.Bitmap;
	
	
public class BitmapCache {
	private HashMap<String, Bitmap> cachedBitmaps;
	
	public BitmapCache(){
		cachedBitmaps = new HashMap<String, Bitmap>();
	}

	public Bitmap get(String shortUrl) {
		return cachedBitmaps.get(shortUrl);
	}

	public boolean containsKey(String shortUrl) {
		return cachedBitmaps.containsKey(shortUrl);
	}

	public void put(String shortUrl, Bitmap bmp) {
		cachedBitmaps.put(shortUrl, bmp);		
	}

	public void remove(String shortUrl) {
		cachedBitmaps.remove(shortUrl);
	}
}
