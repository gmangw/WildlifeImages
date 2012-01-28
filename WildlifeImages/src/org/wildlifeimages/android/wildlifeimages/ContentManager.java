package org.wildlifeimages.android.wildlifeimages;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Hashtable;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.ParcelFileDescriptor;
import android.util.Log;

/**
 * This class handles the caching and updating of exhibit content.
 * 
 * @author Graham Wilkinson 
 * 		
 */
public class ContentManager {

	private static ContentManager self = null;
	
	public static final String ASSET_PREFIX = "file:///android_asset/";

	private Hashtable<String, String> cachedFiles = new Hashtable<String, String>();
	
	private ExhibitList exhibitList;

	private File cacheDir;
	BitmapCache imgCache;

	private int accessTime = 0;

	private Hashtable<String, Integer> timekeeper = new Hashtable<String, Integer>();

	public static ContentManager getSelf(){
		return self;
	}
	
	public ContentManager(File cacheDir, AssetManager assets){
		self = this;
		
		this.cacheDir = cacheDir;
		addAllToMap(cacheDir);
		imgCache = new BitmapCache();
		
		exhibitList = buildExhibitList(assets);
	}

	public void startUpdate(ProgressManager progress){
		new ContentUpdater(this).execute(progress);
	}

	public void clearCache(){
		/* This function scares me a little */
		File[] list = cacheDir.listFiles();
		FileFetcher fetch = new FileFetcher();
		for(int i=0; i<list.length; i++){
			fetch.recursiveRemove(list[i]); 
		}
		cachedFiles.clear();
	}

	public String getBestUrl(String shortUrl) {
		timekeeper.put(shortUrl, accessTime++);
		if (cachedFiles.containsKey(shortUrl)){
			Log.d(this.getClass().getName(), "Pulled from cache: " + shortUrl);
			return cacheDir.toURI().toString() + shortUrl;
		}else{
			return "file:///android_asset/" + shortUrl;
		}
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

	public AssetFileDescriptor getFileDescriptor(String shortUrl, AssetManager assets){
		AssetFileDescriptor afd = null;
		String longUrl = getBestUrl(shortUrl);
		try{
			if (longUrl.startsWith(ASSET_PREFIX)){
				afd = assets.openFd(shortUrl);
			}else{
				File f = new File(new URI(longUrl));
				ParcelFileDescriptor pfd = ParcelFileDescriptor.open(f, ParcelFileDescriptor.MODE_READ_ONLY);
				afd = new AssetFileDescriptor(pfd, -1, 0);
			}
			timekeeper.put(shortUrl, accessTime++);
		} catch (IOException e){
			// TODO
		} catch (URISyntaxException e) {
			// TODO
		}
		return afd;
	}

	public Bitmap getBitmap(String shortUrl, AssetManager assets) {	
		timekeeper.put(shortUrl, accessTime++);
		if (imgCache.containsKey(shortUrl)){
			Log.i(this.getClass().getName(), "Retrieved cached Bitmap " + shortUrl);
			return imgCache.get(shortUrl);
		}else{
			Bitmap bmp = BitmapFactory.decodeStream(streamAssetOrFile(shortUrl, assets));
			imgCache.put(shortUrl, bmp); //TODO may want to limit cache size
			Log.i(this.getClass().getName(), "Cached Bitmap " + shortUrl);
			return bmp;
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
	
	private boolean populateCache(String shortUrl, ContentUpdater progress){
		if (false == cachedFiles.containsKey(shortUrl)){
			File f  = new File(cacheDir.getAbsolutePath() + "/" + shortUrl);
			FileFetcher fetch = new FileFetcher();
			try{
				fetch.mkdirForFile(f); //TODO only generates one higher directory 
			}catch(IOException e){
				return false;
			}

			byte[] newContent = fetch.getWebContent(shortUrl, progress);
			if (null != newContent){
				try {
					fetch.writeBytesToFile(newContent, f);

					imgCache.remove(shortUrl);
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
	
	public void updateCache(ContentUpdater progress){
		ArrayList<String> lines = new ArrayList<String>();
		if (populateCache("list.txt", null)){
			try{
				BufferedReader in = new BufferedReader(new InputStreamReader(streamAssetOrFile("list.txt", null)));
				String line;
				do{
					line = in.readLine();
					if (null != line){
						lines.add(line);
					}
				}while(null != line);
			}catch(IOException e){
				Log.w(this.getClass().getName(), "Problem updating from list.txt");
			}
			for (int i=0; i<lines.size(); i++){
				//progress.show();
				populateCache(lines.get(i), progress);
			}
		}
	}
	
	public ExhibitList getExhibitList(){
		return exhibitList;
	}
	
	private ExhibitList buildExhibitList(AssetManager assetManager){
		try{
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlPullParser xmlBox = factory.newPullParser();
			InputStream istr = assetManager.open("exhibits.xml");
			BufferedReader in = new BufferedReader(new InputStreamReader(istr));
			xmlBox.setInput(in);
			Log.i(this.getClass().getName(), "Input has been set.");
			return new ExhibitList(xmlBox);
		}catch(XmlPullParserException e){
			throw(null); //TODO
		} catch (IOException e) {
			throw(null);
		}
	}

	public static void setSelf(ContentManager contentManager) {
		self = contentManager;
	}
}
