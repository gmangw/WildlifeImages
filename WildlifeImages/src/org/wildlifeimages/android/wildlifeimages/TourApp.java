package org.wildlifeimages.android.wildlifeimages;

import android.app.Activity;
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
    private static final int MENU_EASY = 1;

    private static final int MENU_HARD = 2;

    private static final int MENU_MEDIUM = 3;

    private static final int MENU_SCAN = 4;

    private static final int MENU_CAMERA = 5;

    private static final int MENU_HOME = 6;

    private static final int MENU_MAP = 7;


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
        String summary = "<html><body>All about Wildlife Images!</body></html>";
        mWebView.loadData(summary, "text/html", "US-ASCII");
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
        menu.add(0, MENU_EASY, 0, R.string.menu_easy);
        menu.add(0, MENU_MEDIUM, 0, R.string.menu_medium);
        menu.add(0, MENU_HARD, 0, R.string.menu_hard);

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
            	Toast.makeText(this.getApplicationContext(), "Scan", 1).show(); //TODO
                return true;
            case MENU_CAMERA:
            	Toast.makeText(this.getApplicationContext(), "Camera", 1).show(); //TODO
                return true;
            case MENU_EASY:
            	Toast.makeText(this.getApplicationContext(), "USER\n\nWHY U PUSH THIS", 1).show(); //TODO
                return true;
            case MENU_MEDIUM:
                return true;
            case MENU_HARD:
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
}
