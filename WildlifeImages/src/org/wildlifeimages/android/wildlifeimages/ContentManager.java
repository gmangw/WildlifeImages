package org.wildlifeimages.android.wildlifeimages;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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

	private HashSet<String> cachedFiles = new HashSet<String>();

	private ExhibitList exhibitList;

	private File cacheDir;
	BitmapCache imgCache;

	private int accessTime = 0;

	private Hashtable<String, Integer> timekeeper = new Hashtable<String, Integer>();

	public static ContentManager getSelf(){
		return self;
	}

	public ContentManager(File cacheDir, AssetManager assets){
		//testBitmapMax(assets);

		self = this;

		this.cacheDir = cacheDir;
		imgCache = new BitmapCache();
		addAllToMap(cacheDir);

		prepareExhibits(assets);
	}

	public void prepareExhibits(AssetManager assets){
		try {
			exhibitList = buildExhibitList(assets);
		} catch (XmlPullParserException e) {
			Log.e(this.getClass().getName(), "XmlPullParserException: " + e.getMessage());
		} catch (IOException e) {
			Log.e(this.getClass().getName(), "IOException: " + e.getMessage());
		}
		cacheThumbs(assets);
	}

	private void cacheThumbs(AssetManager assets){
		for(int i=0; i<exhibitList.getCount(); i++){
			Exhibit entry = exhibitList.getExhibitAt(i);
			if (entry.hasContent("Photos")){
				String[] photos = entry.getContent("Photos").split(",");
				getBitmapThumb(photos[0], assets);
			}
		}
	}

	public void clearCache(){
		File[] list = cacheDir.listFiles();
		for(int i=0; i<list.length; i++){
			FileFetcher.recursiveRemove(list[i]); 
		}
		cachedFiles.clear();
	}

	public String getBestUrl(String shortUrl) {
		timekeeper.put(shortUrl, accessTime++);
		if (cachedFiles.contains(shortUrl)){
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
		String longUrl = getBestUrl(shortUrl);
		try{
			AssetFileDescriptor afd = null;
			if (longUrl.startsWith(ASSET_PREFIX)){
				afd = assets.openFd(shortUrl);
			}else{
				File f = new File(new URI(longUrl));
				ParcelFileDescriptor pfd = ParcelFileDescriptor.open(f, ParcelFileDescriptor.MODE_READ_ONLY);
				afd = new AssetFileDescriptor(pfd, -1, 0);
			}
			timekeeper.put(shortUrl, accessTime++);
			return afd;
		} catch (IOException e){
			return null;
		} catch (URISyntaxException e) {
			return null;
		}

	}

	public Bitmap getBitmap(String shortUrl, AssetManager assets) {	
		timekeeper.put(shortUrl, accessTime++);
		populateBitmap(shortUrl, assets);
		return imgCache.getBitmap(shortUrl);
	}

	public Bitmap getBitmapThumb(String shortUrl, AssetManager assets){
		populateBitmapThumb(shortUrl, assets);
		return imgCache.getThumb(shortUrl);
	}

	private void populateBitmap(String shortUrl, AssetManager assets){
		if (imgCache.containsBitmap(shortUrl)){
		}else{
			Bitmap bmp = BitmapFactory.decodeStream(streamAssetOrFile(shortUrl, assets));
			if (bmp != null){
				imgCache.putBitmap(shortUrl, bmp);
			}
		}
	}

	private void populateBitmapThumb(String shortUrl, AssetManager assets){
		if (imgCache.containsThumb(shortUrl)){
		}else{
			Bitmap bmp = BitmapFactory.decodeStream(streamAssetOrFile(shortUrl, assets));
			if (bmp != null){
				imgCache.putThumb(shortUrl, bmp);
			}
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
			cachedFiles.add(path);
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

	public boolean updateCache(UpdateActivity.ContentUpdater progress, String zipURL){
		try{
			URL url;
			try{
				url = new URL(zipURL);
			}catch(MalformedURLException e){
				return false;
			}
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			InputStream webStream = conn.getInputStream();
			ZipInputStream zipStream = new ZipInputStream(webStream);
			ZipEntry ze = null;

			int length = conn.getContentLength();
			int lengthRead = 0;
			while ((ze = zipStream.getNextEntry()) != null) {
				if (progress.isCancelled() == true){
					break;
				}
				File f  = new File(cacheDir.getAbsolutePath() + "/" + ze.getName());
				Log.d(this.getClass().getName(), "Unzipping " + ze.getName() + " to " + f.getPath());
				try{
					FileFetcher.mkdirForFile(f);
				}catch(IOException e){
					continue;
				}
				FileOutputStream fout = new FileOutputStream(f);

				for (int c = zipStream.read(); c != -1; c = zipStream.read()) {
					fout.write(c);
					lengthRead++;
					if (length != -1){
						progress.publish((int)(100*lengthRead/length));
					}
				}
				imgCache.removeBitmap(ze.getName());
				cachedFiles.add(ze.getName());
				zipStream.closeEntry();
				fout.close();
			}
			zipStream.close();
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	public ExhibitList getExhibitList(){
		return exhibitList;
	}

	private ExhibitList buildExhibitList(AssetManager assetManager) throws XmlPullParserException, IOException{
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		XmlPullParser xmlBox = factory.newPullParser();
		InputStream istr = streamAssetOrFile("exhibits.xml", assetManager);
		BufferedReader in = new BufferedReader(new InputStreamReader(istr));
		xmlBox.setInput(in);
		Log.i(this.getClass().getName(), "Input has been set.");
		return new ExhibitList(xmlBox);
	}

	public static void setSelf(ContentManager contentManager) {
		self = contentManager;
	}
}
