package org.wildlifeimages.android.wildlifeimages;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Hashtable;

import android.content.Context;
import android.util.Log;

public class WebContentManager {

	private Hashtable<String, String> newUrlMap = new Hashtable<String, String>();

	private Context context;

	private boolean enabled = true;

	public WebContentManager(Context context){
		this.context = context;
		File cache = context.getCacheDir();

		addFileToMap(cache);
	}
	
	public void updateCache(){
		populateCache("ExhibitContents/alphaFunFacts.html");
	}

	private void populateCache(String filename){
		if (false == newUrlMap.containsKey(filename)){
			try {
				File f  = new File(context.getCacheDir().getAbsolutePath() + "/" + filename);
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
		String s = ""; //TODO StringBuilder
		try {
			url = new URL("http://oregonstate.edu/~wilkinsg/wildlifeimages/" + filename);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			//Log.i(this.getClass().getName(), "" + new Date(conn.getLastModified()));
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(conn.getInputStream()));
			String line = reader.readLine();
			while (line != null){
				s = s.concat(line);
				line = reader.readLine();
			}
			reader.close();
			conn.disconnect();
			return s.getBytes();
		} catch (MalformedURLException e) {
			Log.w(this.getClass().getName(), "Caching of " + filename + " failed with MalformedUrl");
		} catch (IOException e) {
			Log.w(this.getClass().getName(), "Caching of " + filename + " failed with IOException");
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
			path = path.replace(context.getCacheDir().getAbsolutePath()+"/", "");
			newUrlMap.put(path, "");//TODO
			Log.d(this.getClass().getName(), "Found in cache: " + path);
		}
	}

	public String getUrl(String localUrl) {
		if (enabled && newUrlMap.containsKey(localUrl)){
			Log.d(this.getClass().getName(), "Pulled from cache: " + localUrl);
			return context.getCacheDir().toURI() + localUrl;
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
		File cache = context.getCacheDir();
		File[] list = cache.listFiles();
		for(int i=0; i<list.length; i++){
			recursiveRemove(list[i]);
		}
		newUrlMap.clear();
	}
}
