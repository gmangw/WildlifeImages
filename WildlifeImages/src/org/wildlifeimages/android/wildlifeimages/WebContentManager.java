package org.wildlifeimages.android.wildlifeimages;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class WebContentManager {

	private Hashtable<String, String> newUrlMap = new Hashtable<String, String>();
	
	private Context context;
	
	private boolean enabled;
	
	public WebContentManager(Context context){
		this.context = context;
		File cache = context.getCacheDir();
		try {
			FileOutputStream fOut = new FileOutputStream(cache.getAbsolutePath() + "/events.html");
			String s = "EVENTS HERE";
			fOut.write(s.getBytes());
			fOut.close();
			newUrlMap.put("events.html", ""); //TODO
			enabled = true;
		} catch (FileNotFoundException e) {
			Log.w(this.getClass().getName(), "Cache disabled due to FileNotFound");
			enabled = false;
		} catch (IOException e) {
			Log.w(this.getClass().getName(), "Cache disabled due to IOException");
			enabled = false;
		}
	}

	public String getUrl(String localUrl) {
		if (enabled && newUrlMap.containsKey(localUrl)){
			Log.w(this.getClass().getName(), "Pulled from cache: " + localUrl);
			//return "file:///Android/data/org.wildlifeimages.android.wildlifeimages/cache/" + localUrl;
			return context.getCacheDir().toURI() + localUrl;
		}else{
			return "file:///android_asset/" + localUrl;
		}
	}
}
