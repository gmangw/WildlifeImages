package org.wildlifeimages.android.wildlifeimages;

import java.util.HashMap;

import android.graphics.Bitmap;

public class BitmapCache {
	private HashMap<String, Bitmap> cacheMap;
	
	public BitmapCache(){
		cacheMap = new HashMap<String, Bitmap>();
	}
	
	public boolean contains(String shortUrl){
		return cacheMap.containsKey(shortUrl);
	}
	
	public void put(String shortUrl, Bitmap bmp){
		cacheMap.put(shortUrl, bmp);
	}
	
	public Bitmap get(String shortUrl){
		return cacheMap.get(shortUrl);
	}
}
