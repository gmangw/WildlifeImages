package org.wildlifeimages.android.wildlifeimages;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

	public static final String ASSET_PREFIX = "file:///android_asset/"; //TODO

	private Hashtable<String, String> cachedFiles = new Hashtable<String, String>();

	private File cacheDir;
	private HashMap<String, Bitmap> cachedBitmaps;

	private boolean enabled = true;

	public WebContentManager(File cacheDir){
		this.cacheDir = cacheDir;

		addAllToMap(cacheDir);
		cachedBitmaps = new HashMap<String, Bitmap>();
	}

	public void updateCache(){
		populateCache("aaaaclark0007.jpg");
		populateCache("ExhibitContents/alphaFunFacts.html"); //TODO
	}

	public String getBestUrl(String localUrl) {
		if (enabled && cachedFiles.containsKey(localUrl)){
			Log.d(this.getClass().getName(), "Pulled from cache: " + localUrl);
			return cacheDir.toURI().toString() + localUrl;
		}else{
			return "file:///android_asset/" + localUrl;
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
		}catch(IOException e){
			Log.w(this.getClass().getName(), "Asset " + longUrl + " is missing or corrupt.");
		} catch (URISyntaxException e) {
			Log.w(this.getClass().getName(), "Bad url " + longUrl);
		}
		return istr;
	}

	public Bitmap getBitmap(String shortUrl, AssetManager assets) {	
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

	private void populateCache(String shortUrl){
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
				} catch (FileNotFoundException e) {
					Log.w(this.getClass().getName(), "FileNotFoundException while trying to cache " + shortUrl);
				} catch (IOException e) {
					Log.w(this.getClass().getName(), "IOException while trying to cache " + shortUrl);
				}
			}		
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
			Log.i(this.getClass().getName(), conn.getHeaderFields().toString());
			int lengthGuess = conn.getContentLength();
			buffer = new byte[lengthGuess + 256]; //Extra space added just in case.
			
			InputStream binaryReader = conn.getInputStream();			
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
			Log.e(this.getClass().getName(), "Caching of " + shortUrl + " failed with IOException: " + e.getMessage());
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
}
