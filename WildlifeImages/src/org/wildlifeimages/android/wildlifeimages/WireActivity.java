package org.wildlifeimages.android.wildlifeimages;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
/**
 * The parent for the other activities allowing for fun helper functions.
 */
public abstract class WireActivity extends Activity{
	
	public static final int SCAN_DIALOG = 0;

	@Override
	protected void onCreate(Bundle bundle){
		super.onCreate(bundle);

		/* Create a new content manager if there is none. */
		if (ContentManager.getSelf() == null){
			new ContentManager(this.getFilesDir(), this.getAssets());
		}
		
		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB){
			requestWindowFeature(Window.FEATURE_NO_TITLE);
		}
	}
	
	@Override
	protected Dialog onCreateDialog(int id){
		super.onCreateDialog(id);
		
		if (id == SCAN_DIALOG){
			return Common.createScanDialog(this);
		}else{
			return null;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.mainmenu, menu);
		if (this.getClass().equals(MapActivity.class)){
			menu.removeItem(R.integer.MENU_MAP);
		}
		if (this.getClass().equals(IntroActivity.class)){
			menu.removeItem(R.integer.MENU_HOME);
		}
		if (ContentManager.getSelf().getExhibitList().getCurrent().getNext() == null){
			menu.findItem(R.integer.MENU_NEXT).setEnabled(false);
		}
		if (ContentManager.getSelf().getExhibitList().getCurrent().getPrevious() == null){
			menu.findItem(R.integer.MENU_PREVIOUS).setEnabled(false);
		}
		if (false == Common.isIntentAvailable(this, MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)){
			menu.findItem(R.integer.MENU_CAMERA).setEnabled(false);
			menu.findItem(R.integer.MENU_SCAN).setEnabled(false);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Common.menuItemProcess(this, item.getItemId(), ContentManager.getSelf().getExhibitList());
		//TODO add menu bar for 3.0+ devices
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
		Common.processActivityResult(this, requestCode, resultCode, intent);
	}

	public String loadString(int resId){
		return getResources().getString(resId); 
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event){
		super.onKeyDown(keyCode, event);
		if (keyCode == KeyEvent.KEYCODE_CAMERA){
			Common.startCamera(this, null);
			return true;
		}else if (keyCode == KeyEvent.KEYCODE_BACK){
			onBackPressed();
			return true;
		}else{
			return false;
		}
	}
}
