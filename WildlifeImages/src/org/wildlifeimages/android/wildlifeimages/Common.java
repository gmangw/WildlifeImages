package org.wildlifeimages.android.wildlifeimages;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.CharBuffer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.MediaStore;
import android.util.FloatMath;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

/**
 * All the static functions that are not start are kept here.
 * 
 * @author Graham Wilkinson
 * @author Shady Glenn
 * @author Naveen Nanja
 * 	
 */	
public class Common {
	private static final Pattern zipNameExpression = Pattern.compile("http://.*?/update_\\d{12}\\.zip");
	public static final Pattern imageExtensionExpression = Pattern.compile(".+(.jpg|.jpeg|.bmp|.png|.gif)");
	/**
	 * Indicates whether the specified action can be used as an intent. This
	 * method queries the package manager for installed packages that can
	 * respond to an intent with the specified action. If no suitable package is
	 * found, this method returns false.
	 * 
	 * http://developer.android.com/resources/articles/can-i-use-this-intent.html
	 *
	 * @param context The application's environment.
	 * @param action The Intent action to check for availability.
	 *
	 * @return True if an Intent with the specified action can be sent and
	 *         responded to, false otherwise.
	 */
	public static boolean isIntentAvailable(Context context, String action) {
		if (context == null){
			Log.e(Common.class.getName(), "Context given to isIntentAvailable() was null.");
			return false;
		}else{
			final PackageManager packageManager = context.getPackageManager();
			final Intent intent = new Intent(action);
			List<ResolveInfo> list =
				packageManager.queryIntentActivities(intent,
						PackageManager.MATCH_DEFAULT_ONLY);
			return list.size() > 0;
		}
	}/* CommonInstrumentationTest */

	static void processBarcodeContents(WireActivity context, String contents){
		String potentialKey = null;
		String prefix = context.loadString(R.string.qr_prefix);

		if(contents.length() > prefix.length() && contents.substring(0, prefix.length()).equals(prefix)){
			potentialKey = contents.substring(prefix.length());
		}

		if (potentialKey != null){
			ExhibitList exhibitList = ContentManager.getExhibitList();
			if (true == exhibitList.containsKey(potentialKey)){
				ExhibitActivity.start(context, potentialKey);
			}
		}else{
			Toast.makeText(context.getApplicationContext(), context.loadString(R.string.qr_unknown), Toast.LENGTH_SHORT).show();
			Log.w(Common.class.getName(), "Unrecognized QR code " + contents);
		}
	}/* CommonUnitTest */

	/**
	 * Checks if you have an application to scan a QR code and launch it if you have one.
	 * 
	 * @param context The activity to serve as the parent of the new activity
	 */
	public static void startScan(WireActivity context){
		boolean scanAvailable = Common.isIntentAvailable(context, context.loadString(R.string.intent_action_scan));
		boolean scan2Available = Common.isIntentAvailable(context, context.loadString(R.string.intent_action_scan_2));

		if (scanAvailable){
			Intent intent = new Intent(context.loadString(R.string.intent_action_scan));
			intent.putExtra(context.loadString(R.string.intent_extra_scan_mode), context.loadString(R.string.intent_qr_mode));
			context.startActivityForResult(intent, context.loadInt(R.integer.CODE_SCAN_ACTIVITY_REQUEST));
		}else if (scan2Available){
			Intent intent = new Intent(context.loadString(R.string.intent_action_scan_2));
			context.startActivityForResult(intent, context.loadInt(R.integer.CODE_SCAN_2_ACTIVITY_REQUEST));
		}else {
			context.showDialog(WireActivity.SCAN_DIALOG);
		}
	}/* CommonUnitTest */

	/**
	 * Starts the camera application to take a picture.
	 * 
	 * @param an Activity context that has the information about the current activity.
	 * @param a URI imageUri that contains the location of where the image will be stored. 
	 */
	public static void startCamera(Activity context){
		Intent intent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
		context.startActivity(intent);
	}/* CommonUnitTest */

	/**
	 * This will check if the image URL passed in has an acceptable image format.
	 * 
	 * @param a String url containing the URL of the supposed image.
	 * @param a String[] extensionlist that is an array of the acceptable extensions.
	 */
	public static boolean isImageUrl(String url){
		String lower = url.toLowerCase();
		return imageExtensionExpression.matcher(lower).matches();
	}/* CommonInstrumentationTest */

	public static float distance(float x1, float y1, float x2, float y2){
		return (FloatMath.sqrt((x2-x1)*(x2-x1) + (y2-y1)*(y2-y1)));
	}/* CommonInstrumentationTest */

	public static float clamp(float value, float min, float max){
		if (max < min){
			float temp = min;
			min = max;
			max = temp;
		}
		if (value < min){
			value = min;
		}else if (value > max){
			value = max;
		}
		return value;
	}/* CommonInstrumentationTest */

	//http://en.wikipedia.org/wiki/Smoothstep
	public static float smoothStep(float edge0, float edge1, float x){
		x = clamp((x - edge0)/(edge1 - edge0), 0, 1);
		return x*x*x*(x*(x*6 - 15) + 10);
	}/* CommonInstrumentationTest */

	public static void recursiveRemove(File f){
		if (f.isDirectory()){
			File[] list = f.listFiles();
			for(int i=0; i<list.length; i++){
				recursiveRemove(list[i]);
				if (list[i].isDirectory()){
					list[i].delete();
				}
			}
		}else{
			f.delete();
		}
	}/* CommonInstrumentationTest */

	public static void mkdirForFile(File file) throws IOException{
		if (file.getParentFile().exists()){
			return;
		}else{
			mkdirForFile(file.getParentFile());
			if (true == file.getParentFile().mkdir()){ 
				return;
			}else{
				throw(new IOException("Cache subdirectory creation failed: " + file.getParentFile()));
			}
		}
	}/* CommonInstrumentationTest */

	public static void writeBytesToFile(byte[] content, File f) throws IOException{
		FileOutputStream fOut = new FileOutputStream(f);
		fOut.write(content);
		fOut.close();
	}/* CommonInstrumentationTest */

	public static String getZipUrl(String page){
		try{
			URL url = new URL(page);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			InputStream webStream = conn.getInputStream();
			InputStreamReader r = new InputStreamReader(webStream);

			CharBuffer chars = CharBuffer.allocate(1024);
			r.read(chars);
			String fileContents = chars.rewind().toString();

			Matcher m = zipNameExpression.matcher(fileContents);
			if (m.find() == true){
				return m.group();
			}else{
				return null;
			}
		} catch(MalformedURLException e){
			return null;
		} catch (IOException e) {
			return null;
		}
	}/* CommonInstrumentationTest */
	
	public static boolean isAtLeastHoneycomb(){
		return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB;
	}/* CommonInstrumentationTest */

	public static boolean isNetworkConnected(Context context){
		ConnectivityManager manager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netData = manager.getActiveNetworkInfo();
		if (netData != null && netData.isConnectedOrConnecting() == true){
			return true;
		}else{
			return false;
		}
	}/* CommonInstrumentationTest */

	public static boolean onKeyDown(Activity context, int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_CAMERA){
			Common.startCamera(context);
			return true;
		}else if (keyCode == KeyEvent.KEYCODE_BACK){
			context.onBackPressed();
			return true;
		}else{
			return false;
		}
	}/* CommonUnitTest */
	
	public static Date getUpdateTime(String url){
		//update_201204041839.zip
		SimpleDateFormat fmt = new SimpleDateFormat();
		fmt.applyPattern("yyyyMMddHHmm");
		
		Date time;
		try {
			time= fmt.parse(url.substring(url.length()-16, url.length()-4));
		} catch (ParseException e) {
			time = new Date(0);
		}
		return time;
	}/* CommonInstrumentationTest */
}
