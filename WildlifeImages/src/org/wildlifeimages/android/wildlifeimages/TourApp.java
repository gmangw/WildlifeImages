package org.wildlifeimages.android.wildlifeimages;

import java.util.List;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This is a simple TourApp activity that houses a single MapView. It
 * demonstrates...
 * <ul>
 * <li>animating by calling invalidate() from draw()
 * <li>loading and drawing resources
 * <li>handling onPause() in an animation
 * </ul>
 */
public class TourApp extends Activity {
	
	private ExhibitList exhibitList = new ExhibitList();
	
	boolean isLandscape = false;
	
    private void mapInit() {
    	// tell system to use the layout defined in our XML file
        setContentView(R.layout.tour_layout);
    	MapView mMapView;
    	// get handles to the MapView from XML
        mMapView = (MapView) findViewById(R.id.map);

        // give the MapView a handle to the TextView used for messages
        mMapView.setTextView((TextView) findViewById(R.id.text));
    	Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	Toast.makeText(v.getContext().getApplicationContext(), R.string.app_name, 1).show();
            }
        });
        button.setVisibility(View.GONE);
    }
    
    private void introInit() {
    	// tell system to use the layout defined in our XML file
        setContentView(R.layout.intro_layout);
    	WebView mWebView;
    	mWebView = (WebView) findViewById(R.id.intro);
        mWebView.loadUrl("file:///android_asset/intro.html");
    }
    
    private void exhibitInit(Exhibit e) {
    	// tell system to use the layout defined in our XML file
    	exhibitList.setCurrent(e);
        setContentView(R.layout.intro_layout);
    	WebView mWebView;
    	mWebView = (WebView) findViewById(R.id.intro);
        mWebView.loadData(e.getContents(), "text/html", null);
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
        
        //TODO
        //menu.findItem(R.integer.MENU_NEXT).setEnabled( exhibitList.getCurrent().getNext() != null );
        //menu.findItem(R.integer.MENU_PREVIOUS).setEnabled( exhibitList.getCurrent().getPrevious() != null );
        
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
            	introInit();
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
            	Exhibit next = exhibitList.getCurrent().getNext();
            	
            	if(next != null){
            		exhibitInit(next);
            	}
                return true;
            case R.integer.MENU_PREVIOUS:
            	Exhibit prev = exhibitList.getCurrent().getPrevious();
            	
            	if(prev != null){
            		exhibitInit(prev);
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // turn off the window's title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        introInit();
        
        if (savedInstanceState == null) {
            // we were just launched: set up a new game
            Log.w(this.getClass().getName(), "SIS is null");
        } else {
            // we are being restored: resume a previous game
            Log.w(this.getClass().getName(), "SIS is nonnull");
        }
    }

    /**
     * Invoked when the Activity loses user focus.
     */
    @Override
    protected void onPause() {
        super.onPause();
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
    			exhibitInit(e);
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
    	
    	isLandscape = config.orientation == Configuration.ORIENTATION_LANDSCAPE;
    }
}
