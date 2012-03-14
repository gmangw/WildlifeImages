package org.wildlifeimages.android.wildlifeimages;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
/**
 * The parent for the other activities allowing for fun helper functions.
 */
public abstract class WireActivity extends Activity{

	AlertDialog scanDialog;

	@Override
	protected void onCreate(Bundle bundle){
		super.onCreate(bundle);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		scanDialog = Common.createScanDialog(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.mainmenu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Common.menuItemProcess(this, item.getItemId(), ContentManager.getSelf().getExhibitList());
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

	String loadString(int resId){
		return getResources().getString(resId); 
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		scanDialog.dismiss();
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
