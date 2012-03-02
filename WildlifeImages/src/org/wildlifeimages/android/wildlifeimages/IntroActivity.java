package org.wildlifeimages.android.wildlifeimages;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * This Android App is intended for visitors of Wildlife Images Rehabilitation and Education Center.
 * Users can find information about each exhibit and find their way around the center.
 * 
 * @author Graham Wilkinson
 * @author Shady Glenn
 * @author Naveen Nanja
 * 	
 */
public class IntroActivity extends WireActivity implements UpdateListener {

	private static final int EXIT_DIALOG = 0;
	
	private int activeHomeId = R.id.intro_sidebar_intro;

	/*
	 * This progress manager will handle the update button when pressed.
	 * It will be what you see when you are updating the app.
	 */
	private ProgressManager updateDialogManager = new ProgressManager();

	/**
	 * Invoked when the Activity is created.
	 * 
	 * @param savedInstanceState a Bundle containing state saved from a previous
	 *        execution, or null if this is a new execution
	 */
	@Override
	protected void onCreate(Bundle savedState) {
		super.onCreate(savedState);		

		/* Create a new content manager if there is none. */
		if (ContentManager.getSelf() == null){
			new ContentManager(this.getCacheDir(), this.getAssets());
		}

		if (savedState == null) { /* Start from scratch if there is no previous state */
			showIntro();
		} else { /* Use saved state info if app just restarted */
			restoreState(savedState);
		}
	}

	/**
	 * Loads the page at intro_url_about.
	 */
	private void showIntro() {
		setContentView(R.layout.intro_layout);

		ExhibitView mExhibitView;
		mExhibitView = (ExhibitView) findViewById(R.id.intro);
		mExhibitView.loadUrl(loadString(R.string.intro_url_about), ContentManager.getSelf());
	}

	/**
	 * Starts the ExhibitListActivity page.
	 */
	private void showList(){
		ExhibitListActivity.start(this);
	}

	/**
	 * Shows the intro and loads the proper sidebar.
	 * 
	 * * @param savedState a Bundle containing state saved from a previous execution.
	 */
	private void restoreState(Bundle savedState){
		activeHomeId = savedState.getInt(loadString(R.string.save_current_home_id));

		showIntro();
		introProcessSidebar(activeHomeId);
	}

	protected Dialog onCreateDialog(int id){
		super.onCreateDialog(id);
		
		return createExitDialog();
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

	/**
	 * Notification that something is about to happen, to give the Activity a
	 * chance to save state.
	 * 
	 * @param outState a Bundle into which this Activity should save its state
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		if (activeHomeId == R.id.intro_sidebar_photos){
			ExhibitView exView = (ExhibitView) findViewById(R.id.intro);
			exView.clear();
		}
		
		outState.putInt(loadString(R.string.save_current_home_id), activeHomeId);

		/* 
		 * Here we are dismissing since if you have a dialog open and rotate the device it
		 * will try to exit the application and we do not want that. 
		 */
		updateDialogManager.dismiss();
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
		case R.id.intro_sidebar_intro:
			showIntro();
			activeHomeId = viewId;
			break;
		case R.id.intro_sidebar_donations:
			((ExhibitView) findViewById(R.id.intro)).loadUrl(loadString(R.string.intro_url_support), ContentManager.getSelf());
			activeHomeId = viewId;
			break;
		case R.id.intro_sidebar_events:
			((ExhibitView) findViewById(R.id.intro)).loadUrl(loadString(R.string.intro_url_events), ContentManager.getSelf());
			activeHomeId = viewId;
			break;
		case R.id.intro_sidebar_photos:
			String[] introPhotoList = getResources().getStringArray(R.array.intro_image_list);
			((ExhibitView) findViewById(R.id.intro)).loadUrlList(introPhotoList, ContentManager.getSelf());
			activeHomeId = viewId;
			break;
		case R.id.intro_sidebar_exhibitlist:
			showList();
			break;
		case R.id.intro_sidebar_map:
			MapActivity.start(this);
			break;
		case R.id.intro_sidebar_update:
			//TODO onCreateDialog()
			final ProgressDialog progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("Getting updated content...");
			//progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progressDialog.setCancelable(true);
			progressDialog.show();

			updateDialogManager.setDialog(progressDialog);

			ContentManager contentManager = ContentManager.getSelf();
			contentManager.clearCache();

			updateDialogManager.registerUpdateListener(this);

			contentManager.startUpdate(updateDialogManager);
			break;
		}
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		
		if (activeHomeId == R.id.intro_sidebar_photos){
			introProcessSidebar(activeHomeId);
		}
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

	public void onUpdateCompleted() {
		ContentManager.getSelf().prepareExhibits(this.getAssets());
	}
}
