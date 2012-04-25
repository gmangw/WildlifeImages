package org.wildlifeimages.android.wildlifeimages;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
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
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.wildlifeimages.android.wildlifeimages.UpdateActivity.ContentUpdateTask;
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
	public static final String TEMP_FILE_EXTENSION = ".part";
	
	private static final int UPDATE_BUFFER_SIZE = 128;
	private static final int UPDATE_STREAM_BUFFER_SIZE = 2048;
	private static final int STREAM_END = -1;

	private static HashSet<String> cachedFiles = null;

	private static ExhibitList exhibitList = null;

	private static File filesDir = null;
	private static BitmapCache imgCache = null;

	private static boolean timeKeeperEnabled = true;

	private static int accessTime = 0;

	private static LinkedHashMapRestricted<String, Integer> timekeeper = null;

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
		imgCache = new BitmapCache(resources.getInteger(R.integer.thumbnail_size));
		timeKeeperEnabled = true;
		accessTime = 0;
		timekeeper = new LinkedHashMapRestricted<String, Integer>();

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

	/**
	 * This function will set the timekeeper enabled value to the boolean value passed in.
	 * 
	 * @param enabled a boolean value that the variable timeKeeperEnabled will be set to.
	 * 
	 */
	public static void setTimeKeeperEnabled(boolean enabled){
		timeKeeperEnabled = enabled;
	}

	/**
	 * This function will set the time for the the current shortUrl.
	 * Used for the timekeeper, which tells when things were most recently accessed, so used
	 * when you come back to say a photo screen and want to know what photo to display.
	 * 
	 * @param shortUrl a shortened URL from the assets directory.
	 * 
	 */
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
		return new BufferedInputStream(istr);
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
		if (false == imgCache.containsThumb(shortUrl)){
			populateBitmapThumb(shortUrl, assets);
		}
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
	private static synchronized void populateBitmapThumb(String shortUrl, AssetManager assets){
		Bitmap bmp = BitmapFactory.decodeStream(streamAssetOrFile(shortUrl, assets));
		if (bmp != null){
			imgCache.putThumb(shortUrl, bmp);
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
	 * Stream an update file from the server, unzipping the contents into the app's storage directory
	 * and preparing them for use.
	 * 
	 * @param progress AsyncTask calling this function, queried to verify that the update was not cancelled.
	 * @param zipURL Web address of the update package file.
	 * 
	 * @return true if the entire update completed successfully, false otherwise.
	 * 
	 */
	public static boolean updateCache(ContentUpdateTask progress, String zipURL){
		ArrayList<File> downloadedFiles = new ArrayList<File>(); /* Files saved to disk */
		ArrayList<String> updatedFileNames = new ArrayList<String>(); /* Short URLs of saved files */
		byte[] buffer = new byte[UPDATE_BUFFER_SIZE]; /* Space to hold chunks of downloaded files */
		boolean updateResult = true; /* Track whether any errors have occurred */

		try{
			URL url = new URL(zipURL); /* Location of update package zip file */
			ZipEntry entry = null; /* Current file within update package */

			/* Request to access the update package over the network */
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			BufferedInputStream webStream = new BufferedInputStream(conn.getInputStream(), UPDATE_STREAM_BUFFER_SIZE);
			ZipInputStream zipStream = new ZipInputStream(webStream);

			int lengthRead = 0; /* Number of bytes downloaded */

			/* Download and process each new file */
			for (entry = zipStream.getNextEntry(); entry != null; entry = zipStream.getNextEntry()) {
				if (true == progress.isCancelled()){
					/* Cancel gracefully */
					zipStream.close();
					updateResult = false;
					break;
				}

				/* Temporary location to save the downloaded file */
				File outputFile = new File(filesDir, entry.getName() + TEMP_FILE_EXTENSION);

				Log.d(ContentManager.class.getName(), "Unzipping " + entry.getName() + " to " + outputFile.getPath());

				/* Ensure that a folder exists to hold the file's relative path */
				Common.mkdirForFile(outputFile);

				/* Open the file for writing, using a buffered stream for efficientcy */ 
				BufferedOutputStream outputFileStream = new BufferedOutputStream(new FileOutputStream(outputFile));

				/* Record the file's relative path and actual location for later processing */
				downloadedFiles.add(outputFile);
				updatedFileNames.add(entry.getName());

				/* Read the entire file as the package downloads, buffer.length bytes at a time */
				for(int read = zipStream.read(buffer); read != STREAM_END; read = zipStream.read(buffer)) {
					outputFileStream.write(buffer, 0, read);
					lengthRead += read;
					progress.publish(lengthRead); /* Show the user how much has been downloaded */
				}

				/* Clean up */
				outputFileStream.close();
				zipStream.closeEntry();
			}

			/* Close the network connection */
			zipStream.close();
		} catch(MalformedURLException e){
			/* Error converting string to URL, this should never occur */
			updateResult = false;
			Log.e(ContentManager.class.getName(), "Update failed due to malformed URL");
		} catch (IOException e) {
			/* Error reading from network (likely) or writing to disk (unlikely) */
			updateResult = false;
			Log.d(ContentManager.class.getName(), "Update failed due to I/O Exception");
		}

		if (true == updateResult){
			/* Process each successfully downloaded file */
			for (int i=0; i<downloadedFiles.size(); i++){
				String entryName = updatedFileNames.get(i);
				String outputFilename = filesDir.getAbsolutePath() + File.separator + entryName;

				/* Move the file to its final location */
				File finalOutputFile = new File(outputFilename);
				if (finalOutputFile.exists()){
					finalOutputFile.delete();
				}
				boolean renameResult = downloadedFiles.get(i).renameTo(finalOutputFile);

				if (true == renameResult){
					/* Register the file for use and remove references to previous versions */
					imgCache.removeBitmap(entryName);
					imgCache.removeThumb(entryName);
					cachedFiles.add(entryName);
				}else{
					/* renameTo failed for an unknown reason */
					updateResult = false;
					Log.e(ContentManager.class.getName(), "Updated file " + entryName +" could not be renamed.");
				}
			}
		}else{
			/* Remove partial update contents in case of error */
			for (File tmp : downloadedFiles){
				tmp.delete();
			}
		}

		return updateResult;
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

	/**
	 * This will be called at the end and will set the variables used to null.
	 * It will also clear and null the image cache.
	 * 
	 */
	public static void finish() {
		filesDir = null;
		timekeeper = null;
		cachedFiles = null;
		exhibitList = null;

		imgCache.clear();
		imgCache = null;
	}
}
