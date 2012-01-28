package org.wildlifeimages.android.wildlifeimages;

import java.util.ArrayList;
import java.util.Iterator;

import android.app.Activity;
import android.app.AlertDialog;
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
public class IntroActivity extends WireActivity {

	private int activeHomeId = R.id.intro_sidebar_intro;

	private AlertDialog exitDialog;

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

		exitDialog = createExitDialog();

		if (ContentManager.getSelf() == null){
			new ContentManager(this.getCacheDir(), this.getAssets());
		}

		if (savedState == null) { /* Start from scratch if there is no previous state */
			showIntro();
		} else { /* Use saved state info if app just restarted */
			restoreState(savedState);
		}
	}

	private void showIntro() {
		setContentView(R.layout.intro_layout);

		ExhibitView mExhibitView;
		mExhibitView = (ExhibitView) findViewById(R.id.intro);
		mExhibitView.loadUrl(loadString(R.string.intro_url_about), ContentManager.getSelf());
	}

	private void showList(){
		ExhibitListActivity.start(this);
	}

	private void restoreState(Bundle savedState){
		activeHomeId = savedState.getInt(loadString(R.string.save_current_home_id));

		showIntro();
		introProcessSidebar(activeHomeId);
	}

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
		ExhibitList exhibitList = ContentManager.getSelf().getExhibitList();

		outState.putInt(loadString(R.string.save_current_home_id), activeHomeId);

		exitDialog.dismiss();
		updateDialogManager.dismiss();

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

	public void introProcessSidebar(View v) {
		introProcessSidebar(v.getId());
	}

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
		case R.id.intro_sidebar_app:
			final ProgressDialog progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("Looking for updated content...");
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progressDialog.setCancelable(false);
			progressDialog.show();

			updateDialogManager.setDialog(progressDialog);

			ContentManager contentManager = ContentManager.getSelf();
			contentManager.clearCache();

			contentManager.startUpdate(updateDialogManager);

			((ExhibitView) findViewById(R.id.intro)).loadData("Map only scrolls 1 direction currently and doesn't zoom.<br><br>" +
					"QR code scan requires that <a href=\"market://search?q=pname:com.google.zxing.client.android\">Barcode Scanner</a>" +
					" or <a href=\"market://search?q=pname:com.google.android.apps.unveil\">Google Goggles</a> be installed already.<br><br>" +
			"Viewing this page has triggered a cache flush and web update for debug purposes."); //TODO
			activeHomeId = viewId;
			break;
		case R.id.intro_sidebar_exhibitlist:
			showList();
			break;
		case R.id.intro_sidebar_map:
			MapActivity.start(this);
			break;
		}
	}

	@Override
	public void onBackPressed() {
		if (this.isTaskRoot()) {
			exitDialog.show();
		} else {
			finish();
		}
	}

	public static void start(Activity context) {
		Intent introIntent = new Intent(context, IntroActivity.class);
		context.startActivityIfNeeded(introIntent, 0);
	}
}
