package org.wildlifeimages.android.wildlifeimages;

import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
/**
 * The parent for the other activities allowing for fun helper functions.
 */
public abstract class WireActivity extends Activity{

	public static final int SCAN_DIALOG = 0;

	@Override
	protected void onCreate(Bundle bundle){
		super.onCreate(bundle);
		
		/* Create a new content manager if there is none. */
		if (ContentManager.isInitialized() == false){
			ContentManager.init(this.getFilesDir(), this.getResources());
		}		
	}

	@Override
	protected Dialog onCreateDialog(int id){
		super.onCreateDialog(id);

		if (id == SCAN_DIALOG){
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(loadString(R.string.scan_app_options))
			.setCancelable(false)
			.setPositiveButton(R.string.scan_app_option_yes, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(loadString(R.string.scan_app_url)));
					startActivity(i);
				}
			})
			.setNegativeButton(R.string.scan_app_option_no, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			});
			return builder.create();
		}else{
			return null;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.mainmenu, menu);
		if (this.getClass() == MapActivity.class){
			menu.removeItem(R.id.menu_map);
		}
		if (this.getClass() == IntroActivity.class){
			menu.removeItem(R.id.menu_home);
		}
		if (ContentManager.getExhibitList().getCurrent().getNext() == null){
			menu.findItem(R.id.menu_next).setEnabled(false);
		}
		if (ContentManager.getExhibitList().getCurrent().getPrevious() == null){
			menu.findItem(R.id.menu_previous).setEnabled(false);
		}
		if (false == Common.isIntentAvailable(this, MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)){
			menu.findItem(R.id.menu_camera).setEnabled(false);
			menu.findItem(R.id.menu_scan).setEnabled(false);
		}
		if (this.getClass() != ExhibitActivity.class){
			menu.removeItem(R.id.menu_next);
			menu.removeItem(R.id.menu_previous);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
		case R.id.menu_home:
			if (getClass() != IntroActivity.class){
				IntroActivity.start(this);
			}
			break;
		case R.id.menu_map:
			MapActivity.start(this);
			break;
		case R.id.menu_scan:
			Common.startScan(this);
			break;
		case R.id.menu_camera:
			Common.startCamera(this);
			break;
		case R.id.menu_next:
			Exhibit next = ContentManager.getExhibitList().getNext();

			if(next != null){
				ContentManager.getExhibitList().setCurrent(next, Exhibit.TAG_AUTO);
				ExhibitActivity.start(this);
			}else{
				if (getClass() != ExhibitActivity.class){
					ExhibitActivity.start(this);
				}
			}
			break;
		case R.id.menu_previous:
			Exhibit prev = ContentManager.getExhibitList().getPrevious();

			if(prev != null){
				ContentManager.getExhibitList().setCurrent(prev, Exhibit.TAG_AUTO);
				ExhibitActivity.start(this);
			}else{
				if (getClass() != ExhibitActivity.class){
					ExhibitActivity.start(this);
				}
			}
			break;
		}
		return true;
	}

	/**
	 * Gets called when an activity has been started and a request has been given for when it ends.
	 * 
	 * @param an int requestCode that has the code for what action to do.
	 * @param an int resultCode that has the resulting code from the request.
	 * @param a Intent intent that is the intent used to start the process.	
	 */	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent){
		if (resultCode == Activity.RESULT_OK){
			if (requestCode == loadInt(R.integer.CODE_SCAN_ACTIVITY_REQUEST)) {
				String contents = intent.getStringExtra(loadString(R.string.intent_scan_extra_result));
				String format = intent.getStringExtra(loadString(R.string.intent_scan_extra_result_format));
				if (format.equals(loadString(R.string.intent_result_qr))){
					Common.processBarcodeContents(this, contents);
				}
			}else if (requestCode == loadInt(R.integer.CODE_SCAN_2_ACTIVITY_REQUEST)){
				String contents = intent.getStringExtra(loadString(R.string.intent_scan_2_extra_result));
				Common.processBarcodeContents(this, contents);
			}
		}
	}

	public String loadString(int resId){
		return getResources().getString(resId); 
	}
	
	public int loadInt(int resId){
		return getResources().getInteger(resId); 
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event){
		super.onKeyDown(keyCode, event);
		return Common.onKeyDown(this, keyCode, event);
	}

	public long getBuildTime(){
		try{
			ApplicationInfo ai = getPackageManager().getApplicationInfo(getPackageName(), 0);
			ZipFile zf = new ZipFile(ai.sourceDir);
			ZipEntry ze = zf.getEntry("classes.dex");
			return ze.getTime();
		}catch(IOException e){
			return 0;
		} catch (NameNotFoundException e) {
			return 0;
		}
	}
}
