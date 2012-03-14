package org.wildlifeimages.android.wildlifeimages;

import java.util.Iterator;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

/**
 * This class will handle the exhibit page.
 * 
 * @author Graham Wilkinson
 * @author Shady Glenn
 * @author Naveen Nanja
 * 	
 */
public class ExhibitActivity extends WireActivity{

	private Exhibit activityCurrentExhibit;

	/**
	 * This will happen when the activity actually starts.
	 * Will grab the latest state of the current exhibit and call showExhibit to display it.
	 * 
	 * @param a bundle savedState that holds the current state.
	 */
	@Override
	public void onCreate(Bundle savedState) {
		super.onCreate(savedState);

		setContentView(R.layout.exhibit_layout);

		ExhibitList exhibitList = ContentManager.getSelf().getExhibitList();

		if (savedState == null) { /* Start from scratch if there is no previous state */
			remakeButtons(exhibitList.getCurrent());
			showExhibit(exhibitList.getCurrent(), Exhibit.TAG_AUTO);
		} else { /* Use saved state info if app just restarted */
			Exhibit e = exhibitList.get(savedState.getString(loadString(R.string.save_current_exhibit)));
			String tag = savedState.getString(loadString(R.string.save_current_exhibit_tag));
			remakeButtons(e);
			showExhibit(e, tag);
		}

	}

	public void processSlider(View v){
		Log.d(this.getClass().getName(), "Slid");
	}

	/**
	 * This will display the given exhibit.
	 * 
	 * @param an Exhibit e that is the exhibit to be shown.
	 * @param a String contentTag which is the name of the button selected.
	 */
	public void showExhibit(Exhibit e, String contentTag) {
		boolean needRemakeButtons = false;
		ExhibitList exhibitList = ContentManager.getSelf().getExhibitList();
		Exhibit previous = exhibitList.getCurrent();
		String previousTag = previous.getCurrentTag();

		exhibitList.setCurrent(e, contentTag);

		if(false == previous.equals(e)){
			remakeButtons(e);
		}
		if (needRemakeButtons || previousTag != contentTag){
			ExhibitView exView;
			exView = (ExhibitView) findViewById(R.id.exhibit);
			String[] content = e.getContent(e.getCurrentTag()).split(",");
			exView.loadUrlList(content, ContentManager.getSelf());
		}
		activityCurrentExhibit = e;
	}

	/**
	 * Get all of the buttons for a given exhibit and display them.
	 * 
	 * @param an Exhibit e that is the exhibit to be shown.
	 */
	private void remakeButtons(Exhibit e){
		Iterator<String> tagList = e.getTags();
		LinearLayout buttonList = (LinearLayout)findViewById(R.id.exhibit_sidebar_linear);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT, 1);
		OnClickListener listen = new OnClickListener(){
			public void onClick(View v) {
				exhibitProcessSidebar(v);
			}
		};
		for(int i=buttonList.getChildCount()-1; i>=0; i--){
			View child = buttonList.getChildAt(i);
			if (child.getId() != R.id.exhibit_photo_button_layout){
				buttonList.removeView(child);
			}
		}
		while (tagList.hasNext()){
			if (null != findViewById(R.id.exhibitframe_land)){
				String tag = tagList.next();
				Button button = makeStyledButton(tag, buttonList.getContext(), params, listen);
				buttonList.addView(button);
			}else{
				LinearLayout buttonPair = new LinearLayout(buttonList.getContext());
				buttonPair.setLayoutParams(params);
				buttonPair.setOrientation(LinearLayout.VERTICAL);
				for(int i=0; i<3; i++){
					if (tagList.hasNext()){
						String tag = tagList.next();

						Button button = makeStyledButton(tag, buttonPair.getContext(), params, listen);
						buttonPair.addView(button);
					} else {
						Button filler = makeStyledButton("", buttonPair.getContext(), params, null);
						filler.setEnabled(false);
						buttonPair.addView(filler);
					}
				}
				buttonList.addView(buttonPair); 
			}
		}
		ImageButton photoButton = (ImageButton)findViewById(R.id.exhibit_photo_button);
		String shortUrl = e.getContent(Exhibit.TAG_PHOTOS).split(",")[0];
		Bitmap b = ContentManager.getSelf().getBitmapThumb(shortUrl, getAssets());
		photoButton.setImageBitmap(b);
	}

	/**
	 * Get all of the buttons for a given exhibit and display them.
	 * 
	 * @param a String label that is the label of the button.
	 * @param a Context c that is the thing that the button is going to sit in.
	 * @param LayoutParams params that contains the layout parameters.
	 * @param an OnClickListener listen that listens for clicks.
	 */
	private Button makeStyledButton(String label, Context c, LayoutParams params, OnClickListener listen){
		LayoutInflater inflater = LayoutInflater.from(this);
		Button button = (Button)inflater.inflate(R.layout.button_template, null);
		button.setLayoutParams(params);
		button.setText(label);
		button.setOnClickListener(listen);
		return button;
	}

	/**
	 * Get all of the buttons for a given exhibit and display them.
	 * 
	 * @param a View v that is the current button.
	 */
	public void exhibitProcessSidebar(View v){
		ExhibitList exhibitList = ContentManager.getSelf().getExhibitList();
		switch (v.getId()) {
		case R.id.exhibit_photo_button:
			showExhibit(exhibitList.getCurrent(), Exhibit.TAG_PHOTOS);
			break;
		case R.id.exhibit_home_button:
			IntroActivity.start(this);
			break;
		default:
			showExhibit(exhibitList.getCurrent(), ((Button)v).getText().toString());
			break;
		}
	}

	/**
	 * Pass the current outState and pass it up to the parent to overwrite it.
	 * 
	 * @param a Bundle outState to store current state data.
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		ExhibitView exView = (ExhibitView) findViewById(R.id.exhibit);
		exView.clear();
		outState.putString(loadString(R.string.save_current_exhibit), activityCurrentExhibit.getName());
		outState.putString(loadString(R.string.save_current_exhibit_tag), activityCurrentExhibit.getCurrentTag());
	}

	@Override
	protected void onResume(){
		super.onResume();

		showExhibit(activityCurrentExhibit, Exhibit.TAG_AUTO);
	}

	/**
	 * Bootstrapper that allows the launching of activities.
	 * So will start the activity for this page.
	 * 
	 * @param an Activity called context which takes whatever to be passed when starting this page.
	 */
	public static void start(Activity context) {
		Intent exhibitIntent = new Intent(context, ExhibitActivity.class);
		//exhibitIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		context.startActivityIfNeeded(exhibitIntent, 0);
	}
}
