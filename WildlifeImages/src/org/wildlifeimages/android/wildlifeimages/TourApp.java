package org.wildlifeimages.android.wildlifeimages;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.hardware.Camera;
import android.os.Bundle;
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

    private static final int MENU_SCAN = 1;

    private static final int MENU_CAMERA = 2;

    private static final int MENU_HOME = 3;

    private static final int MENU_MAP = 4;

    private Camera mCamera;

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
    
    private void donationInit() {
    	// tell system to use the layout defined in our XML file
        setContentView(R.layout.intro_layout);
    	WebView mWebView;
    	mWebView = (WebView) findViewById(R.id.intro);
        mWebView.loadUrl("file:///android_asset/donate.html");
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
        
        menu.add(0, MENU_HOME, 0, R.string.menu_home);
        menu.add(0, MENU_MAP, 0, R.string.menu_map);
        menu.add(0, MENU_SCAN, 0, R.string.menu_scan);
        menu.add(0, MENU_CAMERA, 0, R.string.menu_camera);
        
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
            case MENU_HOME:
            	introInit();
                return true;
            case MENU_MAP:
            	mapInit();
                return true;
            case MENU_SCAN:
            	boolean scanAvailable = isIntentAvailable(this, "com.google.zxing.client.android.SCAN");
            	
            	if (scanAvailable){
            		mapInit();
            		Intent intent = new Intent("com.google.zxing.client.android.SCAN");
            		intent.putExtra("com.google.zxing.client.android.SCAN.SCAN_MODE", "QR_CODE_MODE");
                    startActivityForResult(intent, 0);
            	} else {
            		Toast.makeText(this.getApplicationContext(), "Install the Barcode Scanner app first.", 1).show();
            	}
                return true;
            case MENU_CAMERA:
            	//Toast.makeText(this.getApplicationContext(), "Camera", 1).show(); //TODO
            	//
            	setContentView(R.layout.camera_layout);
            	CameraDisplay mPreview = (CameraDisplay) findViewById(R.id.cam);
            	//setContentView(new Preview(this));
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
    
    public void takePicture(View view) {
    	((CameraDisplay)findViewById(R.id.cam)).takePicture();
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
    
    /**
     * http://stackoverflow.com/questions/2050263/using-zxing-to-create-an-android-barcode-scanning-app
     */
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                String contents = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
                if (format.equals("QR_CODE")){
                	Toast.makeText(this.getApplicationContext(), contents, 1).show();
                }
            } else if (resultCode == RESULT_CANCELED) {
                //TODO
            }
        }
    }
}
