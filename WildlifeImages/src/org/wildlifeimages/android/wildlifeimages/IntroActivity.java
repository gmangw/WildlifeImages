package org.wildlifeimages.android.wildlifeimages;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
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

		if (savedState == null) { /* Start from scratch if there is no previous state */
			showIntro();
			//new UpdateChecker().execute(this);
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
		String updateStatus = savedState.getString("UpdateStatus");
		if (updateStatus.length() > 0){
			b.setText(updateStatus);
			b.setVisibility(View.VISIBLE);
		}
		//introProcessSidebar(activeHomeId);
	}

	protected Dialog onCreateDialog(int id){
		Dialog parent = super.onCreateDialog(id);

		switch(id){
		case EXIT_DIALOG:
			return createExitDialog();
		case UPDATE_DIALOG:
			return createUpdateDialog();
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

	private AlertDialog createUpdateDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		final IntroActivity me = this;
		builder.setMessage(loadString(R.string.update_available_message))
		.setCancelable(true)
		.setPositiveButton(loadString(R.string.update_available_message_yes),
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				UpdateActivity.start(me);
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

		//if (activeHomeId == R.id.intro_sidebar_photos){
		//	ExhibitView exView = (ExhibitView) findViewById(R.id.intro);
		//	exView.clear();
		//}

		//outState.putInt(loadString(R.string.save_current_home_id), activeHomeId);
		Button b = (Button)findViewById(R.id.update_status);
		outState.putString("UpdateStatus", b.getText().toString());
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
		/*case R.id.intro_sidebar_intro:
			showIntro();
			activeHomeId = viewId;
			break;
		case R.id.intro_sidebar_donations:
			((ExhibitView) findViewById(R.id.intro)).loadUrl(loadString(R.string.intro_url_support), ContentManager.getSelf());
			activeHomeId = viewId;
			setContentView(R.layout.splash_layout);
			break;*/
		case R.id.intro_sidebar_events:
			//((ExhibitView) findViewById(R.id.intro)).loadUrl(loadString(R.string.intro_url_events), ContentManager.getSelf());
			//activeHomeId = viewId;
			PhotosActivity.start(this, true);
			break;
		case R.id.intro_sidebar_photos:
			//String[] introPhotoList = getResources().getStringArray(R.array.intro_image_list);
			//((ExhibitView) findViewById(R.id.intro)).loadUrlList(introPhotoList, ContentManager.getSelf());
			//activeHomeId = viewId;
			PhotosActivity.start(this, false);
			break;
		case R.id.intro_sidebar_exhibitlist:
			ExhibitListActivity.start(this);
			break;
		case R.id.intro_sidebar_map:
			MapActivity.start(this);
			break;
			//case R.id.intro_sidebar_video:
			//	VideoActivity.start(this, R.raw.video);
			//	break;
		}
	}

	@Override
	protected void onResume(){
		super.onResume();

		//if (activeHomeId == R.id.intro_sidebar_photos){
		//	introProcessSidebar(activeHomeId);
		//}
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

	public class UpdateChecker extends AsyncTask<IntroActivity, Integer, String>{

		@Override
		protected String doInBackground(IntroActivity... arg0) {
			if (Common.isNetworkConnected(getApplicationContext()) == true){
				String url = Common.getZipUrl(loadString(R.string.update_page_url));

				SharedPreferences preferences = getSharedPreferences(loadString(R.string.update_preferences), Context.MODE_PRIVATE);
				String oldUrl = preferences.getString(loadString(R.string.update_preferences_key_last), "<none>");
				if (true == oldUrl.equals(url)){
					return null;
				}
				if (url == null){
					Log.d(this.getClass().getName(), "Update failed due to missing zip url.");
					publishProgress(NETWORK_ERROR);
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
				b.setText("Update Available");
				b.setOnClickListener(new OnClickListener(){
					public void onClick(View v) {
						showDialog(UPDATE_DIALOG); //TODO
					}
				});
				b.setVisibility(View.VISIBLE);
			}
		}

		@Override
		protected void onProgressUpdate(Integer... amount) {
			for (int i : amount){
				switch(i){
				case NETWORK_ERROR:
					Button b = (Button)findViewById(R.id.update_status);
					b.setText("Cannot check for updates");
					b.setVisibility(View.VISIBLE);
				}
			}
		}
	}
}
