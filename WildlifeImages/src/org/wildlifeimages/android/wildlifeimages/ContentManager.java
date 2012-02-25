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
import java.util.ArrayList;
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
		//testBitmapMax(assets);

		self = this;

		this.cacheDir = cacheDir;
		imgCache = new BitmapCache();
		addAllToMap(cacheDir);

		prepareExhibits(assets);
	}

	private void testBitmapMax(AssetManager assets){
		Bitmap[] list = new Bitmap[4000];
		int i;
		for(i=0; i<list.length; i++){
			try{
				InputStream stream = assets.open("ExhibitContents/Badger/Badger-Boogie-1.jpg");
				list[i] = BitmapFactory.decodeStream(stream);
				stream.close();
			}catch(OutOfMemoryError e){
				break;
			} catch (IOException e) {
				Log.e(this.getClass().getName(), "Failed to load");
			}
			if (i % 100 == 0){
				Log.e(this.getClass().getName(), "Loaded " + i + " bitmaps");
			}
		}
		Log.e(this.getClass().getName(), "Loaded " + i + " bitmaps");
		for (int k = 0; k<i; k++){
			list[k].recycle();
		}
	}

	public void prepareExhibits(AssetManager assets){
		exhibitList = buildExhibitList(assets);
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

	public void startUpdate(ProgressManager progress){
		new ContentUpdater(this).execute(progress);
	}

	public void clearCache(){
		/* This function scares me a little */
		//TODO check that this does not crash when cache is empty
		File[] list = cacheDir.listFiles();
		for(int i=0; i<list.length; i++){
			FileFetcher.recursiveRemove(list[i]); 
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
				imgCache.putBitmap(shortUrl, bmp); //TODO may want to limit cache size
			}
		}
	}

	private void populateBitmapThumb(String shortUrl, AssetManager assets){
		if (imgCache.containsThumb(shortUrl)){
		}else{
			Bitmap bmp = BitmapFactory.decodeStream(streamAssetOrFile(shortUrl, assets));
			if (bmp != null){
				imgCache.putThumb(shortUrl, bmp); //TODO may want to limit cache size
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
			try{
				FileFetcher.mkdirForFile(f);
			}catch(IOException e){
				return false;
			}

			byte[] newContent = FileFetcher.getWebContent(shortUrl, progress);
			if (null != newContent){
				try {
					FileFetcher.writeBytesToFile(newContent, f);

					imgCache.removeBitmap(shortUrl);
					cachedFiles.put(shortUrl, ""); //TODO
					Log.d(this.getClass().getName(), "File cached: " + shortUrl);
					return true;
				} catch (FileNotFoundException e) {
					Log.w(this.getClass().getName(), "FileNotFoundException while trying to cache " + shortUrl);
					return true;
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

	public boolean updateCache(ContentUpdater progress){
		ArrayList<String> lines = new ArrayList<String>();

		try{
			URL url = new URL("http://oregonstate.edu/~wilkinsg/wildlifeimages/" + "update.zip");
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
					//TODO
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
				cachedFiles.put(ze.getName(), ""); //TODO
				zipStream.closeEntry();
				fout.close();
			}
			zipStream.close();
			return true;
		}catch(MalformedURLException e){
			//TODO
			return false;
		} catch (IOException e) {
			// TODO
			return false;
		}
	}

	public ExhibitList getExhibitList(){
		return exhibitList;
	}

	private ExhibitList buildExhibitList(AssetManager assetManager){
		try{
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlPullParser xmlBox = factory.newPullParser();
			InputStream istr = streamAssetOrFile("exhibits.xml", assetManager);
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
