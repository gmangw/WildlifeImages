package org.wildlifeimages.android.wildlifeimages;

import java.net.URI;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.provider.MediaStore;

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

	public static void processActivityResult(WireActivity context, int requestCode, int resultCode, Intent intent){
		if (requestCode == R.integer.CODE_SCAN_ACTIVITY_REQUEST && resultCode == Activity.RESULT_OK) {
			String contents = intent.getStringExtra(context.loadString(R.string.intent_extra_result));
			String format = intent.getStringExtra(context.loadString(R.string.intent_extra_result_format));
			if (format.equals(context.loadString(R.string.intent_result_qr))){
				String potential_key = Common.processResultQR(context, contents);
				ExhibitList exhibitList = ContentManager.getSelf().getExhibitList();
				if (true == exhibitList.containsKey(potential_key)){
					exhibitList.setCurrent(potential_key, Exhibit.TAG_AUTO);
					ExhibitActivity.start(context);
				}
			}
		} else if (requestCode == R.integer.CAPTURE_IMAGE_ACTIVITY_REQUEST && resultCode == Activity.RESULT_OK) {	
			//ImageView v = new ImageView(this);
			//v.setImageURI(imageUri); //TODO
			//setContentView(v);
		}
	}

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

	public static void startScan(WireActivity context){
		boolean scanAvailable = Common.isIntentAvailable(context, context.loadString(R.string.intent_action_scan));

		if (scanAvailable){
			Intent intent = new Intent(context.loadString(R.string.intent_action_scan));
			intent.putExtra(context.loadString(R.string.intent_extra_scan_mode), context.loadString(R.string.intent_qr_mode));
			context.startActivityForResult(intent, R.integer.CODE_SCAN_ACTIVITY_REQUEST);
		}
	}

	public static void startCamera(Activity context, Uri imageUri){
		//String fileName = "file://mnt/sdcard/pic.jpg"; //TODO filename
		//imageUri = Uri.parse(fileName);
		//Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); //TODO check availability first

		Intent intent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA); //TODO if we don't want image back
		//intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
		//intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
		context.startActivity(intent);
	}

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
				//scanDialog.show(); TODO
			}
			break;
		case R.integer.MENU_CAMERA:
			/* http://achorniy.wordpress.com/2010/04/26/howto-launch-android-camera-using-intents/ */
			Common.startCamera(context, null);//ScanActivity.start(context);

			break;
		case R.integer.MENU_NEXT:
			Exhibit next = exhibits.getNext();

			if(next != null){
				exhibits.setCurrent(next, Exhibit.TAG_AUTO);
			}
			ExhibitActivity.start(context);
			break;
		case R.integer.MENU_PREVIOUS:
			Exhibit prev = exhibits.getPrevious();

			if(prev != null){
				exhibits.setCurrent(prev, Exhibit.TAG_AUTO);
			}
			ExhibitActivity.start(context);
			break;
		}
	}
}
