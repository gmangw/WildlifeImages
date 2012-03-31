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
 * @author Shady Glenn
 * @author Naveen Nanja
 * 		
 */
public class ContentManager {

	private static ContentManager self = null;

	public static final String ASSET_PREFIX = "file:///android_asset/";

	private HashSet<String> cachedFiles = new HashSet<String>();

	private ExhibitList exhibitList;

	private final File filesDir;
	BitmapCache imgCache;

	private int accessTime = 0;

	private Hashtable<String, Integer> timekeeper = new Hashtable<String, Integer>();

	/**
	 * This will return the instance that is currently alive.
	 * 
	 */
	public static ContentManager getSelf(){
		return self;
	}

	/**
	 * Constructor that builds our content manager.
	 * 
	 * @param cacheDir the directory that holds the cached items.
	 * @param assets all the stuff in the assets folder that we need.
	 * 
	 */
	public ContentManager(File filesDir, AssetManager assets){
		//testBitmapMax(assets);

		self = this;

		this.filesDir = filesDir;
		imgCache = new BitmapCache();
		addAllToMap(filesDir);

		prepareExhibits(assets);
	}

	/**
	 * Create the exhibit list and cache the thumbs.
	 * 
	 * @param assets all the stuff in the assets folder that we need.
	 * 
	 */
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

	/**
	 * Will go through the exhibit list and get the bitmap for each thumb, then cache it.
	 * 
	 * @param assets all the stuff in the assets folder that we need.
	 * 
	 */
	private void cacheThumbs(AssetManager assets){
		for(int i=0; i<exhibitList.getCount(); i++){
			Exhibit entry = exhibitList.getExhibitAt(i);
			if (entry.hasContent("Photos")){
				String[] photos = entry.getContent("Photos").split(",");
				getBitmapThumb(photos[0], assets);
			}
		}
	}

	/**
	 * This will clear the cache.
	 * 
	 */
	public void clearCache(){
		File[] list = filesDir.listFiles();
		for(int i=0; i<list.length; i++){
			Common.recursiveRemove(list[i]); 
		}
		cachedFiles.clear();
	}

	/**
	 * This will give us the correct URL to the file, so if cached one exists from an update grab that or grab the original.
	 * Used for web things.
	 * 
	 * @param shortUrl a shortened URL from the assets directory.
	 * 
	 * @return The correct URL for the most up-to-date file.
	 * 
	 */
	public String getBestUrl(String shortUrl) {
		timekeeper.put(shortUrl, accessTime++);
		if (cachedFiles.contains(shortUrl)){
			Log.d(this.getClass().getName(), "Pulled from cache: " + shortUrl);
			return filesDir.toURI().toString() + shortUrl;
		}else{
			return "file:///android_asset/" + shortUrl;
		}
	}

	/**
	 * Will get the input stream from the shortURL, useful for images.
	 * Used for streaming data.
	 * 
	 * @param shortUrl a shortened URL from the assets directory.
	 * @param assets all the stuff in the assets folder that we need.
	 * 
	 * @return An input string of the current file.
	 * 
	 */
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

	/**
	 * Another way of getting access to a file, but using a file descriptor.
	 * Audio uses this file access style.
	 * 
	 * @param shortUrl a shortened URL from the assets directory.
	 * @param assets all the stuff in the assets folder that we need.
	 * 
	 * @return A file descriptor of the file or null on fail.
	 * 
	 */
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

	/**
	 * This will call many functions and get the fullsize bitmap image from memory.
	 * 
	 * @param shortUrl a shortened URL from the assets directory.
	 * @param assets all the stuff in the assets folder that we need.
	 * 
	 * @return The fullsize image bitmap.
	 * 
	 */
	public Bitmap getBitmap(String shortUrl, AssetManager assets) {	
		timekeeper.put(shortUrl, accessTime++);
		populateBitmap(shortUrl, assets);
		return imgCache.getBitmap(shortUrl);
	}

	/**
	 * This will call the function to populate the thumbnail bitmap and return the bitmap once populated.
	 * 
	 * @param shortUrl a shortened URL from the assets directory.
	 * @param assets all the stuff in the assets folder that we need.
	 * 
	 * @return The thumbnail bitmap.
	 * 
	 */
	public Bitmap getBitmapThumb(String shortUrl, AssetManager assets){
		populateBitmapThumb(shortUrl, assets);
		return imgCache.getThumb(shortUrl);
	}

	/**
	 * This will convert an image into a fullsize bitmap and store it in memory
	 * 
	 * @param shortUrl a shortened URL from the assets directory.
	 * @param assets all the stuff in the assets folder that we need.
	 * 
	 */
	private void populateBitmap(String shortUrl, AssetManager assets){
		if (imgCache.containsBitmap(shortUrl)){
		}else{
			Bitmap bmp = BitmapFactory.decodeStream(streamAssetOrFile(shortUrl, assets));
			if (bmp != null){
				imgCache.putBitmap(shortUrl, bmp);
			}
		}
	}

	/**
	 * This will convert an image into a bitmap thumb and store it in memory.
	 * 
	 * @param shortUrl a shortened URL from the assets directory.
	 * @param assets all the stuff in the assets folder that we need.
	 * 
	 */
	private void populateBitmapThumb(String shortUrl, AssetManager assets){
		if (imgCache.containsThumb(shortUrl)){
		}else{
			Bitmap bmp = BitmapFactory.decodeStream(streamAssetOrFile(shortUrl, assets));
			if (bmp != null){
				imgCache.putThumb(shortUrl, bmp);
			}
		}
	}

	/**
	 * This will look at everything in that folder and let itself know that these files exist.
	 * Used to populate the cache list of tiles.
	 * 
	 * @param file a directory which will have all of its files and subdirectories and their files added to the list.
	 * 
	 */
	private void addAllToMap(File file){
		if (file.isDirectory()){
			File[] list = file.listFiles();
			for(int i=0; i<list.length; i++){
				addAllToMap(list[i]);
			}
		}else{
			String path = file.getAbsolutePath();
			path = path.replace(filesDir.getAbsolutePath()+"/", "");
			cachedFiles.add(path);
			Log.d(this.getClass().getName(), "Found in cache: " + path);
		}
	}

	/**
	 * This will get the most recently accessed URL in the shortUrlList you pass in.
	 * 
	 * @param shortUrlList a list of all the shortURLs, so the list of all the most recent URLs recently viewed. 
	 * 
	 * @return The most recent URL in the shortUrlList
	 * 
	 */
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

	/**
	 * This function will update the cache.
	 * 
	 * @param progress thread doing the updating, can communicate if it wants to stop updating.
	 * @param zipURL URL location of the zip file used for updating.
	 * 
	 * @return True if the entire update completed successfully.
	 * 
	 */
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

			boolean result = true;
			int lengthRead = 0;
			while ((ze = zipStream.getNextEntry()) != null) {
				progress.publish(0);
				if (progress.isCancelled()){
					zipStream.close();
					return false;
				}
				if (progress.isCancelled() == true){
					break;
				}
				File f2 = new File(filesDir, ze.getName() + ".part");
				String outputFilename2 = filesDir.getAbsolutePath() + "/" + ze.getName();
				
				Log.d(this.getClass().getName(), "Unzipping " + ze.getName() + " to " + f2.getPath());
				try{
					Common.mkdirForFile(f2);
				}catch(IOException e){
					result = false;
					continue;
				}
				FileOutputStream fout2 = new FileOutputStream(f2);

				byte[] buffer = new byte[4];
				for(int read = zipStream.read(buffer); read != -1; read = zipStream.read(buffer)) {
					fout2.write(buffer, 0, read);
					lengthRead++;
				}
				fout2.close();
				zipStream.closeEntry();
				boolean renameResult2 = f2.renameTo(new File(outputFilename2));
				if (false == renameResult2){
					Log.e(this.getClass().getName(), "Could not rename the .part file for " +ze.getName() + " in data.");
					result = false;
				}
				imgCache.removeBitmap(ze.getName()); //TODO make sure thumbs update
				cachedFiles.add(ze.getName());
			}
			zipStream.close();
			return result;
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * This will return the exhibit list.
	 * 
	 * @return The exhibit list.
	 * 
	 */
	public ExhibitList getExhibitList(){
		return exhibitList;
	}

	/**
	 * This will build the current exhibit list.
	 * 
	 * @param assetManager an assetManager giving us access to the assets.
	 * 
	 * @return A current exhibit list.
	 * 
	 * @throws XmlPullParserException when XML parsing fails.
	 * @throws IOException if SD card read fails, could be a corrupt file system.
	 * 
	 */
	private ExhibitList buildExhibitList(AssetManager assetManager) throws XmlPullParserException, IOException{
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		XmlPullParser xmlBox = factory.newPullParser();
		InputStream istr = streamAssetOrFile("exhibits.xml", assetManager);
		BufferedReader in = new BufferedReader(new InputStreamReader(istr), 1024);
		xmlBox.setInput(in);
		return new ExhibitList(xmlBox);
	}

	/**
	 * This will set self to be the current content manager.
	 * 
	 * @param contentManager the contentManger to set self to.
	 * 
	 */
	public static void setSelf(ContentManager contentManager) {
		self = contentManager;
	}
}
