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
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.larvalabs.svgandroid.SVG;
import com.larvalabs.svgandroid.SVGParser;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.Resources;
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
	public static final String ASSET_PREFIX = "file:///android_asset/";

	private static HashSet<String> cachedFiles = null;

	private static ExhibitList exhibitList = null;

	private static File filesDir = null;
	private static BitmapCache imgCache = null;
	
	private static boolean timeKeeperEnabled = true;

	private static int accessTime = 0;

	private static HashTableRestricted<String, Integer> timekeeper = null;
	
	private static SVG svg = null;
	
	/**
	 * Constructor that builds our content manager.
	 * 
	 * @param cacheDir the directory that holds the cached items.
	 * @param assets all the stuff in the assets folder that we need.
	 * 
	 */
	public static void init(File files, Resources resources){
		cachedFiles = new HashSet<String>();
		imgCache = new BitmapCache();
		timeKeeperEnabled = true;
		accessTime = 0;
		timekeeper = new HashTableRestricted<String, Integer>();
		
		filesDir = files;
		addAllToMap(filesDir);

		prepareExhibits(resources);
	}
	
	public static boolean isInitialized(){
		return (filesDir != null);
	}

	public static synchronized SVG getSVG(Resources resources){
		if (svg == null){
			svg = SVGParser.getSVGFromResource(resources, R.raw.map);
		}
		return svg;
	}
	
	/**
	 * Create the exhibit list and cache the thumbs.
	 * 
	 * @param assets all the stuff in the assets folder that we need.
	 * 
	 */
	public static void prepareExhibits(Resources resources){
		try {
			exhibitList = buildExhibitList(resources.getAssets());
		} catch (XmlPullParserException e) {
			Log.e(ContentManager.class.getName(), "XmlPullParserException: " + e.getMessage());
		} catch (IOException e) {
			Log.e(ContentManager.class.getName(), "IOException: " + e.getMessage());
		}
		svg = null;
		cacheThumbs(resources.getAssets());
	}

	/**
	 * Will go through the exhibit list and get the bitmap for each thumb, then cache it.
	 * 
	 * @param assets all the stuff in the assets folder that we need.
	 * 
	 */
	private static void cacheThumbs(AssetManager assets){
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
	public static void clearCache(){
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
	public static String getBestUrl(String shortUrl) {
		putTime(shortUrl);
		if (cachedFiles.contains(shortUrl)){
			Log.d(ContentManager.class.getName(), "Pulled from cache: " + shortUrl);
			return filesDir.toURI().toString() + shortUrl;
		}else{
			return "file:///android_asset/" + shortUrl;
		}
	}
	
	public static void setTimeKeeperEnabled(boolean enabled){
		timeKeeperEnabled = enabled;
	}

	private static void putTime(String shortUrl){
		if (timeKeeperEnabled == true){
			timekeeper.put(shortUrl, accessTime++);
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
	public static InputStream streamAssetOrFile(String shortUrl, AssetManager assets) { 
		InputStream istr = null;
		String longUrl = getBestUrl(shortUrl);
		try{
			if (longUrl.startsWith(ASSET_PREFIX)){
				istr = assets.open(shortUrl);
			}else{
				File f = new File(new URI(longUrl));
				istr = new FileInputStream(f);
			}
			putTime(shortUrl);
		}catch(IOException e){
			Log.w(ContentManager.class.getName(), "Asset " + longUrl + " is missing or corrupt.");
		} catch (URISyntaxException e) {
			Log.w(ContentManager.class.getName(), "Bad url " + longUrl);
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
	public static AssetFileDescriptor getFileDescriptor(String shortUrl, AssetManager assets){
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
			putTime(shortUrl);
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
	public static Bitmap getBitmap(String shortUrl, AssetManager assets) {	
		putTime(shortUrl);
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
	public static Bitmap getBitmapThumb(String shortUrl, AssetManager assets){
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
	private static void populateBitmap(String shortUrl, AssetManager assets){
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
	private static void populateBitmapThumb(String shortUrl, AssetManager assets){
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
	private static void addAllToMap(File file){
		if (file.isDirectory()){
			File[] list = file.listFiles();
			for(int i=0; i<list.length; i++){
				addAllToMap(list[i]);
			}
		}else{
			String path = file.getAbsolutePath();
			path = path.replace(filesDir.getAbsolutePath()+"/", "");
			cachedFiles.add(path);
			Log.d(ContentManager.class.getName(), "Found in cache: " + path);
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
	public static int getMostRecentPhoto(ExhibitPhoto[] shortUrlList){
		int resultIndex = 0;
		int mostRecent = 0;
		for(int i=0; i<shortUrlList.length; i++){
			Integer time = timekeeper.get(shortUrlList[i].shortUrl);
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
	public static boolean updateCache(UpdateActivity.ContentUpdater progress, String zipURL){
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
				
				Log.d(ContentManager.class.getName(), "Unzipping " + ze.getName() + " to " + f2.getPath());
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
					Log.e(ContentManager.class.getName(), "Could not rename the .part file for " +ze.getName() + " in data.");
					result = false;
				}
				imgCache.removeBitmap(ze.getName());
				imgCache.removeThumb(ze.getName());
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
	public static ExhibitList getExhibitList(){
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
	private static ExhibitList buildExhibitList(AssetManager assetManager) throws XmlPullParserException, IOException{
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		XmlPullParser xmlBox = factory.newPullParser();
		InputStream istr = streamAssetOrFile("exhibits.xml", assetManager);
		BufferedReader in = new BufferedReader(new InputStreamReader(istr), 1024);
		xmlBox.setInput(in);
		return new ExhibitList(xmlBox);
	}

	public static void finish() {
		filesDir = null;
		timekeeper = null;
		cachedFiles = null;
		exhibitList = null;
		
		imgCache.clear();
		imgCache = null;
	}
}
