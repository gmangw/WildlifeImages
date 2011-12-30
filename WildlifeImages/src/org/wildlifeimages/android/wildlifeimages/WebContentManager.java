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

	public static final String ASSET_PREFIX = "file:///android_asset/";

	private Hashtable<String, String> newUrlMap = new Hashtable<String, String>();

	private File cacheDir;
	private HashMap<String, Bitmap> bmpCache;

	private boolean enabled = true;

	public WebContentManager(File cacheDir){
		this.cacheDir = cacheDir;

		addFileToMap(cacheDir);
		bmpCache = new HashMap<String, Bitmap>();
	}

	public void updateCache(){
		populateCache("aaaaclark0007.jpg");
		populateCache("ExhibitContents/alphaFunFacts.html"); //TODO
	}

	private void populateCache(String filename){
		if (false == newUrlMap.containsKey(filename)){
			try {
				File f  = new File(cacheDir.getAbsolutePath() + "/" + filename);
				if (false == f.getParentFile().exists()){
					if (true == f.getParentFile().mkdir()){ //TODO only generates one higher directory
						Log.d(this.getClass().getName(), "Cache subdirectory created at " + f.getParentFile());
					}else{
						Log.e(this.getClass().getName(), "Cache subdirectory creation failed: " + f.getParentFile());
					}
				}

				byte[] newContent = getWebContent(filename);
				if (null != newContent){
					FileOutputStream fOut = new FileOutputStream(f);
					fOut.write(newContent);
					fOut.close();

					newUrlMap.put(filename, ""); //TODO
					enabled = true;
					Log.d(this.getClass().getName(), "Page cached: " + filename);
				}
				bmpCache.remove(filename);
			} catch (FileNotFoundException e) {
				Log.w(this.getClass().getName(), "Cache disabled due to FileNotFoundException");
				enabled = false;
			} catch (IOException e) {
				Log.w(this.getClass().getName(), "Cache disabled due to IOException");
				enabled = false;
			}
		}
	}

	private byte[] getWebContent(String filename){
		URL url;
		try {
			url = new URL("http://oregonstate.edu/~wilkinsg/wildlifeimages/" + filename);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			Log.i(this.getClass().getName(), conn.getHeaderFields().toString());

			int lengthGuess = conn.getContentLength();
			byte[] buffer = new byte[lengthGuess + 256]; //Extra space added just in case.

			InputStream binaryReader = conn.getInputStream();
			int length = 0;
			int read = 0;
			while (true){
				read = binaryReader.read(buffer, length, buffer.length - length);
				if (read == -1){
					break;
				}else{
					length += read;
				}
			}
			if (length > lengthGuess){
				Log.w(this.getClass().getName(), "Guess was " + lengthGuess + ", actually read " + length);
			}
			byte[] result = new byte[length];
			System.arraycopy(buffer, 0, result, 0, result.length);
			buffer = null;
			binaryReader.close();
			conn.disconnect();
			return result;
		} catch (MalformedURLException e) {
			Log.e(this.getClass().getName(), "Caching of " + filename + " failed with MalformedUrl");
		} catch (IOException e) {
			Log.e(this.getClass().getName(), "Caching of " + filename + " failed with IOException: " + e.getMessage());
		}
		return null;	
	}

	private void addFileToMap(File file){
		if (file.isDirectory()){
			File[] list = file.listFiles();
			for(int i=0; i<list.length; i++){
				addFileToMap(list[i]);
			}
		}else{
			String path = file.getAbsolutePath();
			path = path.replace(cacheDir.getAbsolutePath()+"/", "");
			newUrlMap.put(path, "");
			Log.d(this.getClass().getName(), "Found in cache: " + path);
		}
	}

	public String getBestUrl(String localUrl) {
		if (enabled && newUrlMap.containsKey(localUrl)){
			Log.d(this.getClass().getName(), "Pulled from cache: " + localUrl);
			return cacheDir.toURI() + localUrl;
		}else{
			return "file:///android_asset/" + localUrl;
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

	public void clearCache(){
		/* This function scares me a little */
		File[] list = cacheDir.listFiles();
		for(int i=0; i<list.length; i++){
			recursiveRemove(list[i]);
		}
		newUrlMap.clear();
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
		if (bmpCache.containsKey(shortUrl)){
			Log.i(this.getClass().getName(), "Retrieved cached Bitmap " + shortUrl);
			return bmpCache.get(shortUrl);
		}else{
			Bitmap bmp = getBitmapFromStream(streamAssetOrFile(shortUrl, assets));
			bmpCache.put(shortUrl, bmp);
			Log.i(this.getClass().getName(), "Cached Bitmap " + shortUrl);
			return bmp;
		}
	}
	
	/* http://stackoverflow.com/questions/2752924/android-images-from-assets-folder-in-a-gridview */
	private Bitmap getBitmapFromStream(InputStream istr)
	{
		if (null == istr){
			return null;
		}else{
			Bitmap bitmap = BitmapFactory.decodeStream(istr);
			return bitmap;
		}
	}
}
