package org.wildlifeimages.android.wildlifeimages;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;

/**
 * This Android App is intended for visitors of Wildlife Images Rehabilitation and Education Center.
 * Users can find information about each exhibit and find their way around the center.
 * 
 * @author Graham Wilkinson
 * @author Shady Glenn
 * @author Naveen Nanja
 * 	
 */
public class IntroActivity extends WireActivity{

	private static final int EXIT_DIALOG = WireActivity.SCAN_DIALOG+1;

	private static final int UPDATE_DIALOG = EXIT_DIALOG+1;
	
	private static final int LOADING_DIALOG = UPDATE_DIALOG+1;

	private static final int NETWORK_ERROR = 1;

	//private int activeHomeId = R.id.intro_sidebar_intro;

	/**
	 * Invoked when the Activity is created.
	 * 
	 * @param savedInstanceState a Bundle containing state saved from a previous
	 *        execution, or null if this is a new execution
	 */
	@Override
	protected void onCreate(Bundle savedState) {
		super.onCreate(savedState);		
		getWindow().setFormat(PixelFormat.RGBA_8888);
		
		if (savedState == null) { /* Start from scratch if there is no previous state */
			showIntro();
			if (isTaskRoot()){
				new UpdateChecker().execute(this);
				new SVGLoader().execute(getResources());
			}
		} else { /* Use saved state info if app just restarted */
			restoreState(savedState);
		}
	}

	/**
	 * Loads the page at intro_url_about.
	 */
	private void showIntro() {
		setContentView(R.layout.splash_layout);

		//ExhibitView mExhibitView;
		//mExhibitView = (ExhibitView) findViewById(R.id.intro);
		//mExhibitView.loadUrl(loadString(R.string.intro_url_about), ContentManager.getSelf());
	}

	/**
	 * Shows the intro and loads the proper sidebar.
	 * 
	 * * @param savedState a Bundle containing state saved from a previous execution.
	 */
	private void restoreState(Bundle savedState){
		//activeHomeId = savedState.getInt(loadString(R.string.save_current_home_id));
		showIntro();

		Button b = (Button)findViewById(R.id.update_status);
		if (savedState.getBoolean("UpdateStatus") == true){
			b.setVisibility(View.VISIBLE);
		}
		//introProcessSidebar(activeHomeId);
	}
	
	public void updateClicked(View v){
		showDialog(UPDATE_DIALOG); //TODO
	}

	protected Dialog onCreateDialog(int id){
		Dialog parent = super.onCreateDialog(id);

		switch(id){
		case EXIT_DIALOG:
			return createExitDialog();
		case UPDATE_DIALOG:
			return createUpdateDialog();
		case LOADING_DIALOG:
			return createLoadingDialog();
		default: 
			return parent;
		}
	}

	/**
	 * Create the exit dialog box with yes/no option.
	 */
	private AlertDialog createExitDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		final IntroActivity me = this;
		builder.setMessage(loadString(R.string.exit_question))
		.setCancelable(false)
		.setPositiveButton(loadString(R.string.exit_option_yes),
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				ContentManager.finish();
				me.finish();
			}
		})
		.setNegativeButton(R.string.exit_option_no,
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		return builder.create();
	}
	
	private AlertDialog createLoadingDialog() {
		ProgressDialog dialog = new ProgressDialog(this);
		dialog.setIndeterminate(true);
		dialog.setCancelable(false);
		dialog.setMessage(loadString(R.string.loading_dialog_message));
		return dialog;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent){
		super.onActivityResult(requestCode, resultCode, intent);
		
		if (requestCode == R.integer.UPDATE_ACTIVITY_REQUEST && resultCode == Activity.RESULT_OK){
			Button b = (Button)findViewById(R.id.update_status);
			b.setVisibility(View.INVISIBLE);
		}
	}
	
	private AlertDialog createUpdateDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		final IntroActivity me = this;
		builder.setMessage(loadString(R.string.update_available_message))
		.setCancelable(true)
		.setPositiveButton(loadString(R.string.update_available_message_yes),
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				Intent introIntent = new Intent(me, UpdateActivity.class);
				me.startActivityForResult(introIntent, R.integer.UPDATE_ACTIVITY_REQUEST);
			}
		})
		.setNegativeButton(loadString(R.string.update_available_message_no),
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		return builder.create();
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
		Button b = (Button)findViewById(R.id.update_status);
		outState.putBoolean("UpdateStatus", b.getVisibility() == View.VISIBLE);
	}

	/**
	 * Helper function for introProcessSidebar.
	 * 
	 * @param a View v
	 */
	public void introProcessSidebar(View v) {
		introProcessSidebar(v.getId());
	}

	/**
	 * If you hit a button, process that button and do the specific action of that button.
	 * 
	 * @param int viewId, the Id of the view.
	 */
	private void introProcessSidebar(int viewId){
		switch (viewId) {
		case R.id.intro_sidebar_events:
			//EventsActivity.start(this);
			PhotosActivity.start(this, true);
			break;
		case R.id.intro_sidebar_photos:
			PhotosActivity.start(this, false);
			break;
		case R.id.intro_sidebar_exhibitlist:
			ExhibitListActivity.start(this);
			break;
		case R.id.intro_sidebar_map:
			MapActivity.start(this);
			break;
		}
	}

	@Override
	protected void onResume(){
		super.onResume();
	}


	/**
	 * Will show the exit dialog if we are in the main part of the program.
	 * Else it will call the finish function which will move us back to the previous page.
	 */
	@Override
	public void onBackPressed() {
		if (this.isTaskRoot()) {
			showDialog(EXIT_DIALOG);
		} else {
			finish();
		}
	}

	/**
	 * Bootstrapper that allows the launching of activities.
	 * So will start the activity for this page.
	 * 
	 * @param an Activity called context which takes whatever to be passed when starting this page.
	 */
	public static void start(Activity context) {
		Intent introIntent = new Intent(context, IntroActivity.class);
		context.startActivityIfNeeded(introIntent, 0);
	}
	
	private class SVGLoader extends AsyncTask<Resources, Integer, Integer>{
		@Override
		protected void onPreExecute(){
			//showDialog(LOADING_DIALOG);
		}
		
		@Override
		protected Integer doInBackground(Resources... arg0) {
			ContentManager.getSVG(arg0[0]);
			return null;
		}
		
		@Override
		protected void onPostExecute(Integer result){
			//dismissDialog(LOADING_DIALOG);
		}
	}

	private class UpdateChecker extends AsyncTask<IntroActivity, Integer, String>{

		@Override
		protected String doInBackground(IntroActivity... arg0) {			
			if (Common.isNetworkConnected(getApplicationContext()) == true){
				String url = Common.getZipUrl(loadString(R.string.update_page_url));

				SharedPreferences preferences = getSharedPreferences(loadString(R.string.update_preferences), Context.MODE_PRIVATE);
				String oldUrl = preferences.getString(loadString(R.string.update_preferences_key_last), "<none>");
				if (url == null){
					Log.d(this.getClass().getName(), "Update failed due to missing zip url.");
					publishProgress(NETWORK_ERROR);
					return null;
				}
				if (true == oldUrl.equals(url)){
					return null;
				}
				//update_201204041839
				SimpleDateFormat fmt = new SimpleDateFormat();
				fmt.applyPattern("yyyyMMddHHmm");
				
				Date time;
				try {
					time= fmt.parse(url.substring(url.length()-16, url.length()-4));
				} catch (ParseException e) {
					time = null;
				}
				
				if (time.getTime() < getBuildTime()){
					Log.d(this.getClass().getName(), "The current update file is older than the application.");
					return null;
				}

				Log.i(this.getClass().getName(), "Grabbing " + url + ", previously grabbed " + oldUrl);
				return url;
			}else{
				Log.d(this.getClass().getName(), "Update failed due to unavailable network.");
				publishProgress(NETWORK_ERROR);
				return null;
			}
		}

		@Override
		protected void onPostExecute(String result){
			if (result != null){
				Button b = (Button)findViewById(R.id.update_status);
				b.setVisibility(View.VISIBLE);
			}
		}

		@Override
		protected void onProgressUpdate(Integer... amount) {
			for (int i : amount){
				switch(i){
				case NETWORK_ERROR:
					//Button b = (Button)findViewById(R.id.update_status);
					//b.setText("Cannot check for updates");
					//b.setVisibility(View.VISIBLE);
					//TODO
				}
			}
		}
	}
}
