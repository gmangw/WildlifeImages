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
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import android.widget.Toast;

/**
 * This Android App is intended for visitors to Wildlife Images Rehabilitation and Education Center.
 * Users can find information about each exhibit and find their way around the center.
 * 
 * @author Graham Wilkinson
 * @author Shady Glenn
 * @author Naveen Nanja
 * 	
 */
public class TourApp extends Activity {

	public ExhibitList exhibitList;

	private boolean isLandscape = false;

	private int activeId;

	private int activeHomeId = R.id.intro_sidebar_intro;

	private WebContentManager webManager;
	
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
		ExhibitView mWebView;
		mWebView = (ExhibitView) findViewById(R.id.intro);
		mWebView.loadUrl(getBestUrl(loadString(R.string.intro_url_about)));
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
		ExhibitListAdapter exhibitAdapter = new ExhibitListAdapter(this, exhibitList);
		list.setAdapter(exhibitAdapter);
		list.setOnItemClickListener(new ItemClickHandler());
	}

	public void setActiveView(int layoutResID){
		setContentView(layoutResID);
		activeId = layoutResID;
	}

	private Button makeStyledButton(String label, Context c, LayoutParams params, OnClickListener listen){
		Button button = new Button(c);
		button.setText(label);
		button.setLayoutParams(params);
		button.setOnClickListener(listen);
		button.setTextSize(16);
		button.setPadding(10,8,10,8);
		button.setMinEms(5);
		//button.setFocusable(true); //TODO focusable elements for keyboard nav?
		button.setBackgroundResource(R.drawable.android_button);
		return button;
	}
	
	public void exhibitSwitch(Exhibit e, String contentTag) {
		boolean remakeButtons = false;
		Exhibit previous = exhibitList.getCurrent();
		String previousTag = previous.getCurrentTag();
		
		exhibitList.setCurrent(e, contentTag);
		
		/* If not viewing any exhibit or the exhibit is not the one currently open */
		if ((ExhibitView) findViewById(R.id.exhibit) == null){
			if (isLandscape){
				setActiveView(R.layout.exhibit_layout);
			}else{
				setActiveView(R.layout.exhibit_layout_vertical);
			}
			remakeButtons = true;
		}
		remakeButtons = remakeButtons || false == previous.equals(e);
		if(remakeButtons){
			Iterator<String> tagList = e.getTags();
			LinearLayout buttonList = (LinearLayout)findViewById(R.id.exhibit_sidebar_linear);
			
			buttonList.removeAllViews();
			
			int index = 0;
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT, 1);
			OnClickListener listen = new OnClickListener(){
				public void onClick(View v) {
					exhibitProcessSidebar(v);
				}};
			while (tagList.hasNext()){
				if (isLandscape){
					Button button = makeStyledButton(tagList.next(), buttonList.getContext(), params, listen);
					
					/* Add each button after the previous one, keeping map at the end */
					buttonList.addView(button, index);
					index++; 
				}
				else{
					LinearLayout buttonPair = new LinearLayout(buttonList.getContext());
					buttonPair.setLayoutParams(params);
					buttonPair.setOrientation(LinearLayout.VERTICAL);
					for(int i=0; i<2; i++){
						if (tagList.hasNext()){
							Button button = makeStyledButton(tagList.next(), buttonPair.getContext(), params, listen);
							buttonPair.addView(button);
						}
						else{
							Button filler = makeStyledButton("", buttonPair.getContext(), params, null);
							filler.setEnabled(false);
							buttonPair.addView(filler);
						}
					}

					/* Add each button after the previous one, keeping map at the end */
					buttonList.addView(buttonPair, index);
					index++; 
				}
			}
		}
		if (remakeButtons || previousTag != contentTag){
			ExhibitView exView;
			exView = (ExhibitView) findViewById(R.id.exhibit);
			//mWebView.loadData(e.getContent(contentTag), "text/html", null);
			exView.loadUrl(getBestUrl(e.getContent(e.getCurrentTag())));
		}
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
		boolean scanAvailable = isIntentAvailable(this, loadString(R.string.intent_action_scan));
		
		if(isLandscape){
			menu.add(0, R.integer.MENU_HOME, 0, R.string.menu_home);
			menu.add(0, R.integer.MENU_MAP, 0, R.string.menu_map);
			menu.add(0, R.integer.MENU_SCAN, 0, R.string.menu_scan);
			menu.add(0, R.integer.MENU_CAMERA, 0, R.string.menu_camera);
			menu.add(0, R.integer.MENU_PREVIOUS, 0, R.string.menu_previous);
			menu.add(0, R.integer.MENU_NEXT, 0, R.string.menu_next);
		}else{
			menu.add(0, R.integer.MENU_HOME, 0, R.string.menu_home);
			menu.add(0, R.integer.MENU_SCAN, 0, R.string.menu_scan);
			menu.add(0, R.integer.MENU_MAP, 0, R.string.menu_map);
			menu.add(0, R.integer.MENU_PREVIOUS, 0, R.string.menu_previous);
			menu.add(0, R.integer.MENU_CAMERA, 0, R.string.menu_camera);
			menu.add(0, R.integer.MENU_NEXT, 0, R.string.menu_next);
		}
		
		if (false == scanAvailable){
			menu.findItem(R.integer.MENU_SCAN).setEnabled(false);
		}

		return true;
	}

	private String loadString(int resId){
		return getResources().getString(resId);
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
			boolean scanAvailable = isIntentAvailable(this, loadString(R.string.intent_action_scan));

			if (scanAvailable){
				//mapInit();
				Intent intent = new Intent(loadString(R.string.intent_action_scan));
				intent.putExtra(loadString(R.string.intent_extra_scan_mode), loadString(R.string.intent_qr_mode));
				startActivityForResult(intent, R.integer.CODE_SCAN_ACTIVITY_REQUEST);
			} else {
				Toast.makeText(this.getApplicationContext(), loadString(R.string.scan_app_missing), 1).show();
				//TODO
			}
			return true;
		case R.integer.MENU_CAMERA:
			/* http://achorniy.wordpress.com/2010/04/26/howto-launch-android-camera-using-intents/ */
			//define the file-name to save photo taken by Camera activity
			String fileName = "file://mnt/sdcard/pic.jpg"; //TODO filename
			Uri imageUri = Uri.parse(fileName);
			//create new Intent
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			//Intent intent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA); TODO if we don't want image back
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
		
		/* Use saved version of webManager if we restarted due to something trivial */
		webManager = (WebContentManager)getLastNonConfigurationInstance();
		if (null == webManager){
			webManager = new WebContentManager(this.getApplicationContext().getCacheDir());
		}
		
		if (savedState == null) {
			introInit();
		} else {
			Exhibit saved = exhibitList.get(savedState.getString(loadString(R.string.save_current_exhibit)));

			ArrayList<String> activeTagList = savedState.getStringArrayList(loadString(R.string.save_current_exhibit_tag));
			ArrayList<String> exhibitNames = savedState.getStringArrayList(loadString(R.string.save_current_exhibit_names));

			//while(nameList.hasNext()){
			for(int i=0; i<exhibitNames.size(); i++){
				Exhibit e = exhibitList.get(exhibitNames.get(i));
				if (e != null){
					e.setCurrentTag(activeTagList.get(i));
				}
			}

			activeHomeId = savedState.getInt(loadString(R.string.save_current_home_id));
			activeId = savedState.getInt(loadString(R.string.save_current_page));
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
		outState.putString(loadString(R.string.save_current_exhibit), exhibitList.getCurrent().getName());

		outState.putInt(loadString(R.string.save_current_page), activeId);
		outState.putInt(loadString(R.string.save_current_home_id), activeHomeId);

		Iterator<String> keyList = exhibitList.keys();
		ArrayList<String> currentExhibitList = new ArrayList<String>();
		ArrayList<String> currentTagList = new ArrayList<String>();
		while(keyList.hasNext()){
			String exhibitName = keyList.next();
			currentExhibitList.add(exhibitName);
			currentTagList.add(exhibitList.get(exhibitName).getCurrentTag());
		}
		outState.putStringArrayList(loadString(R.string.save_current_exhibit_names), currentExhibitList);
		outState.putStringArrayList(loadString(R.string.save_current_exhibit_tag), currentTagList);
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
		String prefix = loadString(R.string.qr_prefix);
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
				String contents = intent.getStringExtra(loadString(R.string.intent_extra_result));
				String format = intent.getStringExtra(loadString(R.string.intent_extra_result_format));
				if (format.equals(loadString(R.string.intent_result_qr))){
					if (false == processResultQR(contents)){
						Toast.makeText(this.getApplicationContext(), loadString(R.string.invalid_qr_result) + contents, 1).show();
					}
				}
			} else if (resultCode == RESULT_CANCELED) {
			}
		} else if (requestCode == R.integer.CAPTURE_IMAGE_ACTIVITY_REQUEST) {
			if (resultCode == RESULT_OK) {
				//use imageUri here to access the image
				//TODO
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

	
	public String getBestUrl(String localUrl){
		String[] list = localUrl.split(",");
		StringBuffer result = new StringBuffer();
		for (int i=0; i<list.length; i++){
			result.append(webManager.getUrl(list[i]));
			if (i+1 < list.length){
				result.append(",");
			}
		}
		return result.toString();
	}
	
	public void introProcessSidebar(int viewId){
		switch (viewId) {
		case R.id.intro_sidebar_intro:
			introInit();
			activeHomeId = viewId;
			break;
		case R.id.intro_sidebar_donations:
			((ExhibitView) findViewById(R.id.intro)).loadUrl(getBestUrl(loadString(R.string.intro_url_support)));
			activeHomeId = viewId;
			break;
		case R.id.intro_sidebar_events:
			((ExhibitView) findViewById(R.id.intro)).loadUrl(getBestUrl(loadString(R.string.intro_url_events)));
			activeHomeId = viewId;
			break;
		case R.id.intro_sidebar_photos:
			String[] introPhotoList = getResources().getStringArray(R.array.intro_image_list);
			String urlList = "";
			for(int i=0; i<introPhotoList.length; i++){
				urlList = urlList.concat(getBestUrl(introPhotoList[i]));
				if (i+1 < introPhotoList.length){
					urlList = urlList.concat(",");
				}
			}
			((ExhibitView) findViewById(R.id.intro)).loadUrl(urlList);
			activeHomeId = viewId;
			break;
		case R.id.intro_sidebar_app:
			((ExhibitView) findViewById(R.id.intro)).loadData("Map only scrolls 1 direction currently and doesn't zoom.<br><br>" +
					"QR code scan requires that <a href=\"market://search?q=pname:com.google.zxing.client.android\">Barcode Scanner</a>" +
					" or <a href=\"market://search?q=pname:com.google.android.apps.unveil\">Google Goggles</a> be installed already.<br><br>" +
					"The camera will leave a pic at the filesystem root, does nothing with it.<br><br>" +
					"Viewing this page has triggered a cache flush and web update for debug purposes.",
					"text/html", null); //TODO
			activeHomeId = viewId;
			webManager.clearCache();
			webManager.updateCache();
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
		builder.setMessage(loadString(R.string.exit_question))
		.setCancelable(false)
		.setPositiveButton(loadString(R.string.exit_option_yes), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				me.finish();
			}
		})
		.setNegativeButton(R.string.exit_option_no, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();

	}

	@Override
	public Object onRetainNonConfigurationInstance() {
	    final WebContentManager data = webManager;
	    return data;
	}
	
	public class ItemClickHandler implements AdapterView.OnItemClickListener{

		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			Exhibit e = (Exhibit)parent.getItemAtPosition(position);
			exhibitSwitch(e, Exhibit.TAG_AUTO);
		}
	}
}
