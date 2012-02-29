package org.wildlifeimages.android.wildlifeimages;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.provider.MediaStore;
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
		final PackageManager packageManager = context.getPackageManager();
		final Intent intent = new Intent(action);
		List<ResolveInfo> list =
			packageManager.queryIntentActivities(intent,
					PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
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
		if (requestCode == R.integer.CODE_SCAN_ACTIVITY_REQUEST && resultCode == Activity.RESULT_OK) {
			String contents = intent.getStringExtra(context.loadString(R.string.intent_extra_result));
			String format = intent.getStringExtra(context.loadString(R.string.intent_extra_result_format));
			if (format.equals(context.loadString(R.string.intent_result_qr))){
				String potentialKey = Common.processResultQR(context, contents);
				if (potentialKey != null){
					ExhibitList exhibitList = ContentManager.getSelf().getExhibitList();
					if (true == exhibitList.containsKey(potentialKey)){
						exhibitList.setCurrent(potentialKey, Exhibit.TAG_AUTO);
						ExhibitActivity.start(context);
					}
				}else{
					Toast.makeText(context, "Unfamiliar QR code "+ contents, Toast.LENGTH_SHORT);
				}
			}
		}
	}

	/**
	 * Will process the string gotten from a QR code.
	 * 
	 * @param a WireActivity context that contains the info about the WireActivity, so our main page.
	 * @param a String textQR that contains the text returned from the QR scanner.
	 */	
	private static String processResultQR(WireActivity context, String textQR){
		String prefix = context.loadString(R.string.qr_prefix);
		textQR.substring(0, prefix.length());
		if(textQR.substring(0, prefix.length()).equals(prefix)){
			String potential_key = textQR.substring(prefix.length());
			return potential_key;
		}else{
			return null;
		}
	}

	/**
	 * Checks if you have an application to scan a QR code and launch it if you have one.
	 * 
	 * @param a WireActivity context that contains the info about the WireActivity, so our main page.
	 */
	public static void startScan(WireActivity context){
		boolean scanAvailable = Common.isIntentAvailable(context, context.loadString(R.string.intent_action_scan));

		if (scanAvailable){
			Intent intent = new Intent(context.loadString(R.string.intent_action_scan));
			intent.putExtra(context.loadString(R.string.intent_extra_scan_mode), context.loadString(R.string.intent_qr_mode));
			context.startActivityForResult(intent, R.integer.CODE_SCAN_ACTIVITY_REQUEST);
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
		case R.integer.MENU_HOME:
			IntroActivity.start(context);
			break;
		case R.integer.MENU_MAP:
			MapActivity.start(context);
			break;
		case R.integer.MENU_SCAN:
			boolean scanAvailable = Common.isIntentAvailable(context, context.loadString(R.string.intent_action_scan));

			if (scanAvailable){
				Intent intent = new Intent(context.loadString(R.string.intent_action_scan));
				intent.putExtra(context.loadString(R.string.intent_extra_scan_mode), context.loadString(R.string.intent_qr_mode));
				context.startActivityForResult(intent, R.integer.CODE_SCAN_ACTIVITY_REQUEST);
			} else {
				context.scanDialog.show();
			}
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
	public static boolean isImageUrl(String url, String[] extensionList){
		String lower = url.toLowerCase();
		for (int i=0; i<extensionList.length; i++){
			if (lower.endsWith(extensionList[i])){
				return true;
			}
		}
		return false;
	}

	public static float distance(float x1, float y1, float x2, float y2){
		return (float)(Math.sqrt(Math.pow(x2-x1, 2) + Math.pow(y2-y1, 2)));
	}

	public static float clamp(float value, float min, float max){
		if (value < min){
			value = min;
		}else if (value > max){
			value = max;
		}
		return value;
	}

	//http://en.wikipedia.org/wiki/Smoothstep
	public static float smoothStep(float edge0, float edge1, float x){
		// Scale, and clamp x to 0..1 range
		x = clamp((x - edge0)/(edge1 - edge0), 0, 1);
		// Evaluate polynomial
		return x*x*x*(x*(x*6 - 15) + 10);
	}
}
