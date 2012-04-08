package org.wildlifeimages.android.wildlifeimages;

import java.util.Iterator;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
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

	private Exhibit activityCurrentExhibit = null;

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

		ExhibitList exhibitList = ContentManager.getExhibitList();

		if (savedState == null) { /* Start from scratch if there is no previous state */
			String name = getIntent().getStringExtra("Exhibit");
			if (name != null){
				exhibitList.setCurrent(name, Exhibit.TAG_AUTO);
			}

			remakeButtons(exhibitList.getCurrent());
			showExhibit(exhibitList.getCurrent(), Exhibit.TAG_AUTO);
		} else { /* Use saved state info if app just restarted */
			String wasShowing = savedState.getString(loadString(R.string.save_current_exhibit));
			Exhibit e = exhibitList.get(wasShowing);
			String tag = savedState.getString(loadString(R.string.save_current_exhibit_tag));
			remakeButtons(e);
			showExhibit(e, tag);
		}
	}

	/**
	 * This will display the given exhibit.
	 * 
	 * @param an Exhibit e that is the exhibit to be shown.
	 * @param a String contentTag which is the name of the button selected.
	 */
	public void showExhibit(Exhibit e, String contentTag) {
		ExhibitList exhibitList = ContentManager.getExhibitList();

		exhibitList.setCurrent(e, contentTag);

		ExhibitView exView = (ExhibitView) findViewById(R.id.exhibit);
		String[] content = e.getContent(e.getCurrentTag()).split(",");
		exView.loadUrlList(content);

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
		String[] urlList = e.getContent(Exhibit.TAG_PHOTOS).split(",");
		String shortUrl = urlList[0];
		Bitmap b = ContentManager.getBitmapThumb(shortUrl, getAssets());
		photoButton.setImageBitmap(b);
		if (Common.isAtLeastHoneycomb()){

			LinearLayout container = (LinearLayout)photoButton.getParent();
			container.setBackgroundResource(R.drawable.android_button);
			container.removeAllViews();
			container.addView(photoButton);
			photoButton.setBackgroundColor(0);

			for (int i=1; i<Math.min(urlList.length, 3); i++){
				LayoutInflater inflater = LayoutInflater.from(this);
				ImageButton button = (ImageButton)inflater.inflate(R.layout.exhibit_photo_button, null);
				button.setLayoutParams(photoButton.getLayoutParams());
				shortUrl = e.getContent(Exhibit.TAG_PHOTOS).split(",")[i];
				ContentManager.setTimeKeeperEnabled(false);
				b = ContentManager.getBitmapThumb(shortUrl, getAssets());
				ContentManager.setTimeKeeperEnabled(true);
				button.setImageBitmap(b);
				button.setBackgroundColor(0);
				container.addView(button);
			}
		}
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
		ExhibitList exhibitList = ContentManager.getExhibitList();
		switch (v.getId()) {
		case R.id.exhibit_photo_button:
			if (Common.isAtLeastHoneycomb()){
				LinearLayout photoButtons = (LinearLayout)findViewById(R.id.exhibit_photo_button_layout);
				for (int i=0; i<photoButtons.getChildCount(); i++){
					if (photoButtons.getChildAt(i).equals(v)){
						String content = exhibitList.getCurrent().getContent(Exhibit.TAG_PHOTOS).split(",")[i];
						ContentManager.getBitmap(content, getAssets());
					}
				}
			}
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
	protected void onRestart(){
		super.onResume();
		remakeButtons(activityCurrentExhibit);
		showExhibit(activityCurrentExhibit, Exhibit.TAG_AUTO);
		//TODO not called after screen unlock
	}

	/**
	 * Bootstrapper that allows the launching of activities.
	 * So will start the activity for this page.
	 * 
	 * @param an Activity called context which takes whatever to be passed when starting this page.
	 */
	public static void start(Activity context) {
		Intent exhibitIntent = new Intent(context, ExhibitActivity.class);
		context.startActivity(exhibitIntent);
	}

	public static void start(Activity context, Exhibit e){
		Intent exhibitIntent = new Intent(context, ExhibitActivity.class);
		exhibitIntent.putExtra("Exhibit", e.getName());
		context.startActivity(exhibitIntent);
	}

	public static void start(Activity context, String s){
		Intent exhibitIntent = new Intent(context, ExhibitActivity.class);
		exhibitIntent.putExtra("Exhibit", s);
		context.startActivity(exhibitIntent);
	}
}
