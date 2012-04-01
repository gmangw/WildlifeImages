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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.FloatMath;
import android.util.Log;
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
	}

	/**
	 * Gets called when an activity has been started and a request has been given for when it ends.
	 * 
	 * @param a WireActivity context that contains the info about the WireActivity, so our main page.
	 * @param an int requestCode that has the code for what action to do.
	 * @param an int resultCode that has the resulting code from the request.
	 * @param a Intent intent that is the intent used to start the process.	
	 */	
	public static void processActivityResult(WireActivity context, int requestCode, int resultCode, Intent intent){
		if (resultCode == Activity.RESULT_OK){
			if (requestCode == R.integer.CODE_SCAN_ACTIVITY_REQUEST) {
				String contents = intent.getStringExtra(context.loadString(R.string.intent_scan_extra_result));
				String format = intent.getStringExtra(context.loadString(R.string.intent_scan_extra_result_format));
				if (format.equals(context.loadString(R.string.intent_result_qr))){
					processBarcodeContents(context, contents);
				}
			}else if (requestCode == R.integer.CODE_SCAN_2_ACTIVITY_REQUEST){
				String contents = intent.getStringExtra(context.loadString(R.string.intent_scan_2_extra_result));
				processBarcodeContents(context, contents);
			}
		}
	}

	private static void processBarcodeContents(WireActivity context, String contents){
		String potentialKey = null;
		String prefix = context.loadString(R.string.qr_prefix);

		if(contents.length() > prefix.length() && contents.substring(0, prefix.length()).equals(prefix)){
			potentialKey = contents.substring(prefix.length());
		}

		if (potentialKey != null){
			ExhibitList exhibitList = ContentManager.getSelf().getExhibitList();
			if (true == exhibitList.containsKey(potentialKey)){
				ExhibitActivity.start(context, potentialKey);
			}
		}else{
			Toast.makeText(context.getApplicationContext(), context.loadString(R.string.qr_unknown), Toast.LENGTH_SHORT).show();
			Log.w(Common.class.getName(), "Unrecognized QR code " + contents);
		}
	}

	/**
	 * Checks if you have an application to scan a QR code and launch it if you have one.
	 * 
	 * @param a WireActivity context that contains the info about the WireActivity, so our main page.
	 */
	public static void startScan(WireActivity context){
		boolean scanAvailable = Common.isIntentAvailable(context, context.loadString(R.string.intent_action_scan));
		boolean scan2Available = Common.isIntentAvailable(context, context.loadString(R.string.intent_action_scan_2));

		if (scanAvailable){
			Intent intent = new Intent(context.loadString(R.string.intent_action_scan));
			intent.putExtra(context.loadString(R.string.intent_extra_scan_mode), context.loadString(R.string.intent_qr_mode));
			context.startActivityForResult(intent, R.integer.CODE_SCAN_ACTIVITY_REQUEST);
		}else if (scan2Available){
			Intent intent = new Intent(context.loadString(R.string.intent_action_scan_2));
			context.startActivityForResult(intent, R.integer.CODE_SCAN_2_ACTIVITY_REQUEST);
		}else {
			context.showDialog(WireActivity.SCAN_DIALOG);
		}
	}

	/**
	 * Starts the camera application to take a picture.
	 * 
	 * @param an Activity context that has the information about the current activity.
	 * @param a URI imageUri that contains the location of where the image will be stored. 
	 */
	public static void startCamera(Activity context, Uri imageUri){
		Intent intent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
		context.startActivity(intent);
	}

	/**
	 * Will ask the user if you don't have a QR scanner if you would like to download one.
	 * 
	 * @param a WireActivity context that contains the info about the WireActivity, so our main page.
	 */
	public static AlertDialog createScanDialog(final WireActivity context){
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(context.loadString(R.string.scan_app_options))
		.setCancelable(false)
		.setPositiveButton(R.string.scan_app_option_yes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(context.loadString(R.string.scan_app_url)));
				context.startActivity(i);
			}
		})
		.setNegativeButton(R.string.scan_app_option_no, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		return builder.create();
	}

	/**
	 * Tries to match the id of the menu button pressed with one of the cases, so home or map, etc...
	 * Launches the appropriate activity according to the case found from the id.
	 * 
	 * @param a WireActivity context that contains the info about the WireActivity, so our main page.
	 * @param an int id containing the id of the view, so a button or an item.
	 * @param an ExhibitList exhibits that contains the current list of exhibits.
	 */
	public static void menuItemProcess(WireActivity context, int id, ExhibitList exhibits){
		switch (id) {
		case android.R.id.home:
		case R.integer.MENU_HOME:
			IntroActivity.start(context);
			break;
		case R.integer.MENU_MAP:
			MapActivity.start(context);
			break;
		case R.integer.MENU_SCAN:
			startScan(context);
			break;
		case R.integer.MENU_CAMERA:
			Common.startCamera(context, null);
			break;
		case R.integer.MENU_NEXT:
			Exhibit next = exhibits.getNext();

			if(next != null){
				exhibits.setCurrent(next, Exhibit.TAG_AUTO);
				ExhibitActivity.start(context);
			}else{
				if (context.getClass() != ExhibitActivity.class){
					ExhibitActivity.start(context);
				}
			}
			break;
		case R.integer.MENU_PREVIOUS:
			Exhibit prev = exhibits.getPrevious();

			if(prev != null){
				exhibits.setCurrent(prev, Exhibit.TAG_AUTO);
				ExhibitActivity.start(context);
			}else{
				if (context.getClass() != ExhibitActivity.class){
					ExhibitActivity.start(context);
				}
			}
			break;
		}
	}

	/**
	 * This will check if the image URL passed in has an acceptable image format.
	 * 
	 * @param a String url containing the URL of the supposed image.
	 * @param a String[] extensionlist that is an array of the acceptable extensions.
	 */
	public static boolean isImageUrl(String url){
		String lower = url.toLowerCase();
		return imageExtensionExpression.matcher(lower).matches();
	}

	public static float distance(float x1, float y1, float x2, float y2){
		return (FloatMath.sqrt((x2-x1)*(x2-x1) + (y2-y1)*(y2-y1)));
	}

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
	}

	//http://en.wikipedia.org/wiki/Smoothstep
	public static float smoothStep(float edge0, float edge1, float x){
		x = clamp((x - edge0)/(edge1 - edge0), 0, 1);
		return x*x*x*(x*(x*6 - 15) + 10);
	}

	public static void recursiveRemove(File f){
		if (f.isDirectory()){
			File[] list = f.listFiles();
			for(int i=0; i<list.length; i++){
				recursiveRemove(list[i]);
			}
		}else{
			f.delete();
		}
	}

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
	}

	public static void writeBytesToFile(byte[] content, File f) throws IOException{
		FileOutputStream fOut = new FileOutputStream(f);
		fOut.write(content);
		fOut.close();
	}

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
	}

	public static boolean isNetworkConnected(Context context){
		ConnectivityManager manager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netData = manager.getActiveNetworkInfo();
		if (netData != null && netData.isConnectedOrConnecting() == true){
			return true;
		}else{
			return false;
		}
	}
}
