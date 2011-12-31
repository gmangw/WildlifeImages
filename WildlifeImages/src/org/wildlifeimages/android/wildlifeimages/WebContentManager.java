package org.wildlifeimages.android.wildlifeimages;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Hashtable;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

/**
 * This class handles the caching and updating of exhibit content.
 * 
 * @author Graham Wilkinson 
 * 		
 */
public class WebContentManager {

	public static final String ASSET_PREFIX = "file:///android_asset/";

	private Hashtable<String, String> cachedFiles = new Hashtable<String, String>();

	private File cacheDir;
	private HashMap<String, Bitmap> cachedBitmaps;

	private boolean enabled = true;
	
	private int accessTime = 0;
	
	private Hashtable<String, Integer> timekeeper = new Hashtable<String, Integer>();

	public WebContentManager(File cacheDir){
		this.cacheDir = cacheDir;

		addAllToMap(cacheDir);
		cachedBitmaps = new HashMap<String, Bitmap>();
	}

	public void updateCache(){
		if (populateCache("list.txt")){
			try{
				BufferedReader in = new BufferedReader(new InputStreamReader(streamAssetOrFile("list.txt", null)));
				while(true){
					String line = in.readLine();
					if (null == line){
						break;
					}else{
						populateCache(line);
					}
				}
			}catch(IOException e){
				Log.w(this.getClass().getName(), "Problem updating from list.txt");
			}
		}
	}

	public String getBestUrl(String shortUrl) {
		timekeeper.put(shortUrl, accessTime++);
		if (enabled && cachedFiles.containsKey(shortUrl)){
			Log.d(this.getClass().getName(), "Pulled from cache: " + shortUrl);
			return cacheDir.toURI().toString() + shortUrl;
		}else{
			return "file:///android_asset/" + shortUrl;
		}
	}

	public void clearCache(){
		/* This function scares me a little */
		File[] list = cacheDir.listFiles();
		for(int i=0; i<list.length; i++){
			recursiveRemove(list[i]);
		}
		cachedFiles.clear();
	}

	public InputStream streamAssetOrFile(String shortUrl, AssetManager assets) {
		InputStream istr = null;
		String longUrl = getBestUrl(shortUrl);
		try{
			if (longUrl.startsWith(ASSET_PREFIX)){
				istr = assets.open(shortUrl);
			}else{
				File f = new File(new URI(longUrl));
				istr = new FileInputStream(f);
			}
			timekeeper.put(shortUrl, accessTime++);
		}catch(IOException e){
			Log.w(this.getClass().getName(), "Asset " + longUrl + " is missing or corrupt.");
		} catch (URISyntaxException e) {
			Log.w(this.getClass().getName(), "Bad url " + longUrl);
		}
		return istr;
	}

	public Bitmap getBitmap(String shortUrl, AssetManager assets) {	
		timekeeper.put(shortUrl, accessTime++);
		if (cachedBitmaps.containsKey(shortUrl)){
			Log.i(this.getClass().getName(), "Retrieved cached Bitmap " + shortUrl);
			return cachedBitmaps.get(shortUrl);
		}else{
			Bitmap bmp = BitmapFactory.decodeStream(streamAssetOrFile(shortUrl, assets));
			cachedBitmaps.put(shortUrl, bmp); //TODO may want to limit cache size
			Log.i(this.getClass().getName(), "Cached Bitmap " + shortUrl);
			return bmp;
		}
	}

	private boolean populateCache(String shortUrl){
		if (false == cachedFiles.containsKey(shortUrl)){
			File f  = new File(cacheDir.getAbsolutePath() + "/" + shortUrl);
			mkdirForFile(f); //TODO only generates one higher directory

			byte[] newContent = getWebContent(shortUrl);
			if (null != newContent){
				try {
					FileOutputStream fOut = new FileOutputStream(f);
					fOut.write(newContent);
					fOut.close();

					cachedBitmaps.remove(shortUrl);
					cachedFiles.put(shortUrl, ""); //TODO
					Log.d(this.getClass().getName(), "File cached: " + shortUrl);
					return true;
				} catch (FileNotFoundException e) {
					Log.w(this.getClass().getName(), "FileNotFoundException while trying to cache " + shortUrl);
					return false;
				} catch (IOException e) {
					Log.w(this.getClass().getName(), "IOException while trying to cache " + shortUrl);
					return false;
				}
			}else{
				return false;
			}
		}else{
			return true;
		}
	}

	private byte[] getWebContent(String shortUrl){
		URL url;
		try {
			url = new URL("http://oregonstate.edu/~wilkinsg/wildlifeimages/" + shortUrl);
		}catch(MalformedURLException e){
			Log.e(this.getClass().getName(), "Caching of " + shortUrl + " failed with MalformedUrlException.");
			return null;
		}
		int length = 0;
		int read = 0;
		byte[] buffer = null;
		try {
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			InputStream binaryReader = conn.getInputStream();
			int lengthGuess = conn.getContentLength();
			if (lengthGuess == -1){
				lengthGuess = 32768;
			}
			buffer = new byte[lengthGuess + 256]; //Extra space added just in case.
			Log.i(this.getClass().getName(), conn.getHeaderFields().toString());	
			while (true){
				read = binaryReader.read(buffer, length, buffer.length - length);
				if (read == -1){
					break;
				}else{
					length += read;
				}
			}
			binaryReader.close();
			conn.disconnect();
			if (length > lengthGuess){
				Log.w(this.getClass().getName(), "Guess was " + lengthGuess + ", actually read " + length);
			}
		} catch (IOException e) {
			Log.w(this.getClass().getName(), "Caching of " + shortUrl + " failed with IOException: " + e.getMessage());
		}

		if (length > 0){
			byte[] result = new byte[length];
			System.arraycopy(buffer, 0, result, 0, result.length);
			return result;
		}else{
			return null;
		}	
	}

	private void addAllToMap(File file){
		if (file.isDirectory()){
			File[] list = file.listFiles();
			for(int i=0; i<list.length; i++){
				addAllToMap(list[i]);
			}
		}else{
			String path = file.getAbsolutePath();
			path = path.replace(cacheDir.getAbsolutePath()+"/", "");
			cachedFiles.put(path, "");
			Log.d(this.getClass().getName(), "Found in cache: " + path);
		}
	}

	private void mkdirForFile(File file){
		if (false == file.getParentFile().exists()){
			if (true == file.getParentFile().mkdir()){ 
				Log.d(this.getClass().getName(), "Cache subdirectory created at " + file.getParentFile());
			}else{
				Log.e(this.getClass().getName(), "Cache subdirectory creation failed: " + file.getParentFile());
			}
		}
	}

	private void recursiveRemove(File f){
		if (f.isDirectory()){
			File[] list = f.listFiles();
			for(int i=0; i<list.length; i++){
				recursiveRemove(list[i]);
			}
		}else{
			f.delete();
			Log.d(this.getClass().getName(), "Removed cache file " + f.getName());
		}
	}
	
	public int getMostRecentIndex(String[] shortUrlList){
		int resultIndex = 0;
		int mostRecent = 0;
		for(int i=0; i<shortUrlList.length; i++){
			Integer time = timekeeper.get(shortUrlList[i]);
			if (time != null && time > mostRecent){
				mostRecent = time;
				resultIndex = i;
			}
		}
		return resultIndex;
	}
}
