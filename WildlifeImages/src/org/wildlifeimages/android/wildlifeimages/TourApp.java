package org.wildlifeimages.android.wildlifeimages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Spannable;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class TourApp extends Activity {

	public ExhibitList exhibitList;

	private boolean isLandscape = false;

	private int activeId;
	
	private int activeHomeId = R.id.intro_sidebar_intro;

	private void mapInit() {
		// tell system to use the layout defined in our XML file
		if (isLandscape){
			setActiveView(R.layout.tour_layout);
		}else{
			setActiveView(R.layout.tour_layout_vertical);
		}
		MapView mMapView;
		// get handles to the MapView from XML
		mMapView = (MapView) findViewById(R.id.map);

		mMapView.setExhibitList(exhibitList);
		mMapView.setParent(this);
	}

	private void introInit() {
		// tell system to use the layout defined in our XML file
		if (isLandscape){
			setActiveView(R.layout.intro_layout);
		}else{
			setActiveView(R.layout.intro_layout_vertical);
		}
		WebView mWebView;
		mWebView = (WebView) findViewById(R.id.intro);
		mWebView.loadUrl("file:///android_asset/intro.html");
	}

	public void listInit(){
		setActiveView(R.layout.list_layout);
		ListView list = (ListView)findViewById(R.id.exhibitlist);
		ArrayList<String> tempList = new ArrayList<String>();
		Iterator<String> placeNameIter = exhibitList.keys();
		while(placeNameIter.hasNext()){
			Exhibit e = exhibitList.get(placeNameIter.next());
			tempList.add(e.getName());
		}
		String[] tempArray = tempList.toArray(new String[0]);
		Arrays.sort(tempArray);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, tempArray);
		ExhibitListAdapter exhibitAdapter = new ExhibitListAdapter(this, exhibitList);
		list.setAdapter(exhibitAdapter);
		list.setOnItemClickListener(new ItemClickHandler());
	}

	public void setActiveView(int layoutResID){
		setContentView(layoutResID);
		activeId = layoutResID;
	}

	public void exhibitSwitch(Exhibit e, String contentTag) {
		Exhibit previous = exhibitList.getCurrent();
		exhibitList.setCurrent(e, contentTag);
		
		/* If not viewing any exhibit or the exhibit is not the one currently open */
		if ((WebView) findViewById(R.id.exhibit) == null || false == previous.equals(e)){
			exhibitList.setCurrent(e, contentTag);
			if (isLandscape){
				setActiveView(R.layout.exhibit_layout);
			}else{
				setActiveView(R.layout.exhibit_layout_vertical);
			}
			Iterator<String> tagList = e.getTags();
			LinearLayout buttonList = (LinearLayout)findViewById(R.id.exhibit_sidebar_linear);
			
			int index = 0;
			while (tagList.hasNext()){
				Button button = new Button(buttonList.getContext());
				String label = tagList.next();
				button.setText(label);
				LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
				button.setLayoutParams(params);
				button.setOnClickListener(new OnClickListener(){
					public void onClick(View v) {
						exhibitProcessSidebar(v);
					}});
				/* Add each button after the previous one, keeping map at the end */
				buttonList.addView(button, index);
				index++; 
			}
		}
		WebView mWebView;
		mWebView = (WebView) findViewById(R.id.exhibit);
		//mWebView.loadData(e.getContent(contentTag), "text/html", null);
		mWebView.loadUrl(e.getContent(e.getCurrentTag()));
	}

	/**
	 * Invoked during init to give the Activity a chance to set up its Menu.
	 * 
	 * @param menu the Menu to which entries may be added
	 * @return true
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(0, R.integer.MENU_HOME, 0, R.string.menu_home);
		menu.add(0, R.integer.MENU_MAP, 0, R.string.menu_map);
		menu.add(0, R.integer.MENU_SCAN, 0, R.string.menu_scan);
		menu.add(0, R.integer.MENU_CAMERA, 0, R.string.menu_camera);
		menu.add(0, R.integer.MENU_PREVIOUS, 0, R.string.menu_previous);
		menu.add(0, R.integer.MENU_NEXT, 0, R.string.menu_next);

		return true;
	}

	/**
	 * Invoked when the user selects an item from the Menu.
	 * 
	 * @param item the Menu entry which was selected
	 * @return true if the Menu item was legit (and we consumed it), false
	 *         otherwise
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.integer.MENU_HOME:
			if(activeId == R.layout.intro_layout || activeId == R.layout.intro_layout_vertical){
				introProcessSidebar(R.id.intro_sidebar_intro);
			}else{
				introInit();
				introProcessSidebar(activeHomeId);
			}
			return true;
		case R.integer.MENU_MAP:
			mapInit();
			return true;
		case R.integer.MENU_SCAN:
			boolean scanAvailable = isIntentAvailable(this, "com.google.zxing.client.android.SCAN");

			if (scanAvailable){
				//mapInit();
				Intent intent = new Intent("com.google.zxing.client.android.SCAN");
				intent.putExtra("com.google.zxing.client.android.SCAN.SCAN_MODE", "QR_CODE_MODE");
				startActivityForResult(intent, R.integer.CODE_SCAN_ACTIVITY_REQUEST);
			} else {
				Toast.makeText(this.getApplicationContext(), "Install the Barcode Scanner app first.", 1).show();
			}
			return true;
		case R.integer.MENU_CAMERA:
			/* http://achorniy.wordpress.com/2010/04/26/howto-launch-android-camera-using-intents/ */
			//define the file-name to save photo taken by Camera activity
			String fileName = "new-photo-name.jpg";
			Uri imageUri;
			//create parameters for Intent with filename
			ContentValues values = new ContentValues();
			values.put(MediaStore.Images.Media.TITLE, fileName);
			values.put(MediaStore.Images.Media.DESCRIPTION,"Image capture by camera");
			//imageUri is the current activity attribute, define and save it for later usage (also in onSaveInstanceState)
			imageUri = getContentResolver().insert(
					MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
			//create new Intent
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
			intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
			startActivityForResult(intent, R.integer.CAPTURE_IMAGE_ACTIVITY_REQUEST);

			return true;
		case R.integer.MENU_NEXT:
			Exhibit next = exhibitList.getNext();

			if(next != null){
				exhibitSwitch(next, Exhibit.TAG_AUTO);
			}else{
				exhibitSwitch(exhibitList.getCurrent(), Exhibit.TAG_AUTO);
			}
			return true;
		case R.integer.MENU_PREVIOUS:
			Exhibit prev = exhibitList.getPrevious();

			if(prev != null){
				exhibitSwitch(prev, Exhibit.TAG_AUTO);
			}
			return true;
		}

		return false;
	}

	/**
	 * Invoked when the Activity is created.
	 * 
	 * @param savedInstanceState a Bundle containing state saved from a previous
	 *        execution, or null if this is a new execution
	 */
	@Override
	protected void onCreate(Bundle savedState) {
		super.onCreate(savedState);

		// turn off the window's title bar
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		Display screen = this.getWindowManager().getDefaultDisplay();
		if (screen.getWidth() > screen.getHeight()){
			isLandscape = true;
		}else{
			isLandscape = false;
		}		

		exhibitList = new ExhibitList();

		if (savedState == null) {
			// we were just launched: set up a new instance
			Log.w(this.getClass().getName(), "SIS is null");
			introInit();
		} else {
			// we are being restored: resume a previous instance
			Log.w(this.getClass().getName(), "SIS is nonnull");
			Exhibit saved = exhibitList.get(savedState.getString(getResources().getString(R.string.save_current_exhibit)));
			
			exhibitList.setCurrent(saved, Exhibit.TAG_AUTO);
			
			ArrayList<String> activeTagList = savedState.getStringArrayList(getResources().getString(R.string.save_current_exhibit_tag));
			ArrayList<String> exhibitNames = savedState.getStringArrayList(getResources().getString(R.string.save_current_exhibit_names));
			
			//while(nameList.hasNext()){
			for(int i=0; i<exhibitNames.size(); i++){
				Exhibit e = exhibitList.get(exhibitNames.get(i));
				if (e != null){
					e.setCurrentTag(activeTagList.get(i));
				}
			}
			
			activeHomeId = savedState.getInt(getResources().getString(R.string.save_current_home_id));
			activeId = savedState.getInt(getResources().getString(R.string.save_current_page));
			switch(activeId){
			case R.layout.list_layout:
				listInit();
				break;

			case R.layout.tour_layout:
			case R.layout.tour_layout_vertical:
				mapInit();
				break;

			case R.layout.exhibit_layout:
			case R.layout.exhibit_layout_vertical:
				exhibitSwitch(saved, Exhibit.TAG_AUTO);
				break;

			case R.layout.intro_layout:
			case R.layout.intro_layout_vertical:
			default:
				introInit();
				introProcessSidebar(activeHomeId);
				break;
			}

		}
	}

	/**
	 * Invoked when the Activity loses user focus.
	 */
	@Override
	protected void onPause() {
		super.onPause();
		//TODO
	}

	/**
	 * Notification that something is about to happen, to give the Activity a
	 * chance to save state.
	 * 
	 * @param outState a Bundle into which this Activity should save its state
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Log.w(this.getClass().getName(), "SIS called");
		outState.putString(getResources().getString(R.string.save_current_exhibit), exhibitList.getCurrent().getName());

		outState.putInt(getResources().getString(R.string.save_current_page), activeId);
		outState.putInt(getResources().getString(R.string.save_current_home_id), activeHomeId);
		
		Iterator<String> keyList = exhibitList.keys();
		ArrayList<String> currentExhibitList = new ArrayList<String>();
		ArrayList<String> currentTagList = new ArrayList<String>();
		while(keyList.hasNext()){
			String exhibitName = keyList.next();
			currentExhibitList.add(exhibitName);
			currentTagList.add(exhibitList.get(exhibitName).getCurrentTag());
		}
		outState.putStringArrayList(getResources().getString(R.string.save_current_exhibit_names), currentExhibitList);
		outState.putStringArrayList(getResources().getString(R.string.save_current_exhibit_tag), currentTagList);
	}

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

	private boolean processResultQR(String textQR){
		String prefix = this.getResources().getString(R.string.qr_prefix);
		textQR.substring(0, prefix.length());
		if(textQR.substring(0, prefix.length()).equals(prefix)){
			String potential_key = textQR.substring(prefix.length());
			if (exhibitList.containsKey(potential_key)){
				Exhibit e = exhibitList.get(potential_key);
				exhibitSwitch(e, Exhibit.TAG_AUTO); //TODO add tag and app url to qr code
				return true;
			}else{
				return false;
			}
		}else{
			return false;
		}
	}

	/**
	 * http://stackoverflow.com/questions/2050263/using-zxing-to-create-an-android-barcode-scanning-app
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == R.integer.CODE_SCAN_ACTIVITY_REQUEST) {
			if (resultCode == RESULT_OK) {
				String contents = intent.getStringExtra("SCAN_RESULT");
				String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
				if (format.equals("QR_CODE")){
					if (false == processResultQR(contents)){
						Toast.makeText(this.getApplicationContext(), "Code not recognized: " + contents, 1).show();
					}
				}
			} else if (resultCode == RESULT_CANCELED) {
			}
		} else if (requestCode == R.integer.CAPTURE_IMAGE_ACTIVITY_REQUEST) {
			if (resultCode == RESULT_OK) {
				//use imageUri here to access the image
				//TODO
			} else if (resultCode == RESULT_CANCELED) {
				Toast.makeText(this, "Picture was not taken", Toast.LENGTH_SHORT);
			} else {
				Toast.makeText(this, "Picture was not taken", Toast.LENGTH_SHORT);
			}
		}
	}

	@Override
	public void onConfigurationChanged(Configuration config){
		super.onConfigurationChanged(config);
	}

	public void introProcessSidebar(View v){
		introProcessSidebar(v.getId());
	}
	
	public void introProcessSidebar(int viewId){
		switch (viewId) {
		case R.id.intro_sidebar_intro:
			introInit();
			activeHomeId = viewId;
			break;
		case R.id.intro_sidebar_donations:
			((WebView) findViewById(R.id.intro)).loadUrl("file:///android_asset/donate.html");
			activeHomeId = viewId;
			break;
		case R.id.intro_sidebar_events:
			((WebView) findViewById(R.id.intro)).loadUrl("file:///android_asset/events.html");
			activeHomeId = viewId;
			break;
		case R.id.intro_sidebar_photos:
			((WebView) findViewById(R.id.intro)).loadUrl("file:///android_asset/photos.html");
			activeHomeId = viewId;
			break;
		case R.id.intro_sidebar_app:
			((WebView) findViewById(R.id.intro)).loadData("Map only scrolls 1 direction currently and doesn't zoom.<br><br>" +
					"QR code scan requires that Barcode Scanner or Google Goggles be installed already.<br><br>" +
					"The camera will generate duplicate photos, and leave garbage files if you cancel it.",
					"text/html", null);
			activeHomeId = viewId;
			break;
		case R.id.intro_sidebar_exhibitlist:
			listInit();
			break;
		case R.id.intro_sidebar_map:
			mapInit();
			break;
		}
	}

	public void exhibitProcessSidebar(View v){
		switch (v.getId()) {
		case R.id.exhibit_sidebar_map:
			mapInit();
			break;
		default:
			exhibitSwitch(exhibitList.getCurrent(), ((Button)v).getText().toString());
			break;
		}
	}

	/* http://stackoverflow.com/questions/2257963/android-how-to-show-dialog-to-confirm-user-wishes-to-exit-activity */
	@Override
	public void onBackPressed() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		final TourApp me = this;
		builder.setMessage("Are you sure you want to exit?")
		.setCancelable(false)
		.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				me.finish();
			}
		})
		.setNegativeButton("No", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();

	}

	public class ItemClickHandler implements AdapterView.OnItemClickListener{

		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			Exhibit e = (Exhibit)parent.getItemAtPosition(position);
			exhibitSwitch(e, Exhibit.TAG_AUTO);
		}

	}
}
