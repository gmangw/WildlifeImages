package org.wildlifeimages.android.wildlifeimages;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;

public abstract class WireActivity extends Activity{
	public String loadString(int resId){
		return getResources().getString(resId);
	}
	
	@Override
	protected void onCreate(Bundle bundle){
		super.onCreate(bundle);
		/* Use saved version of contentManager if activity just restarted */
		ContentManager contentManager = (ContentManager)getLastNonConfigurationInstance();
		if (null == contentManager){
			if (ContentManager.getSelf() == null){
				new ContentManager(this.getCacheDir(), this.getAssets());
			}
		}else{
			ContentManager.setSelf(contentManager);
		}
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
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
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent){
		Common.processActivityResult(this, requestCode, resultCode, intent);
	}
	
	@Override
	public Object onRetainNonConfigurationInstance() {
		final ContentManager data = ContentManager.getSelf();
		return data;
	}
}
