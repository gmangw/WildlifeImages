package org.wildlifeimages.android.wildlifeimages;

import java.util.Iterator;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;

/**
 * This class...
 * 
 * @author Graham Wilkinson
 * @author Shady Glenn
 * @author Naveen Nanja
 * 	
 */
public class ExhibitActivity extends WireActivity{

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
		
		//if (savedState == null) { /* Start from scratch if there is no previous state */
		//	showExhibit(exhibitList.getCurrent(), Exhibit.TAG_AUTO);
		//} else { /* Use saved state info if app just restarted */
		//	Exhibit e = exhibitList.get(savedState.getString(loadString(R.string.save_current_exhibit)));
		//	String tag = savedState.getString(loadString(R.string.save_current_exhibit_tag));
		//	showExhibit(e, tag);
		//}
		remakeButtons(exhibitList.getCurrent());
		showExhibit(exhibitList.getCurrent(), Exhibit.TAG_AUTO);
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
			String[] content = e.getContent(e.getCurrentTag());
			exView.loadUrlList(content, ContentManager.getSelf());
		}
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
		buttonList.removeAllViews();
		buttonList.setVisibility(View.GONE);
		int index = 0;
		while (tagList.hasNext()){
			if (null != findViewById(R.id.exhibitframe_land)){
				Button button = makeStyledButton(tagList.next(), buttonList.getContext(), params, listen);

				/* Add each button after the previous one, keeping any xml buttons at the end */
				buttonList.addView(button, index);
				index++;
			} else{
				LinearLayout buttonPair = new LinearLayout(buttonList.getContext());
				buttonPair.setLayoutParams(params);
				buttonPair.setOrientation(LinearLayout.VERTICAL);
				for(int i=0; i<2; i++){
					if (tagList.hasNext()){
						Button button = makeStyledButton(tagList.next(), buttonPair.getContext(), params, listen);
						buttonPair.addView(button);
					} else {
						Button filler = makeStyledButton("", buttonPair.getContext(), params, null);
						filler.setEnabled(false);
						buttonPair.addView(filler);
					}
				}
				/* Add each button after the previous one, keeping any xml buttons at the end */
				buttonList.addView(buttonPair, index);
				index++; 
			}
		}
		buttonList.setVisibility(View.VISIBLE);
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

	/**
	 * Get all of the buttons for a given exhibit and display them.
	 * 
	 * @param a View v that is the current button.
	 */
	public void exhibitProcessSidebar(View v){
		ExhibitList exhibitList = ContentManager.getSelf().getExhibitList();
		switch (v.getId()) {
		case R.id.exhibit_sidebar_map:
			Intent mapIntent = new Intent(getApplicationContext(), MapActivity.class);
			mapIntent.putExtra("ExhibitList", exhibitList);
			this.startActivity(mapIntent);
			break;
		default:
			showExhibit(exhibitList.getCurrent(), ((Button)v).getText().toString());
			break;
		}
	}

	/**
	 * Pass the current outState and pass it up to the parent to overwrite it.s
	 * 
	 * @param a Bundle outState where you will put current state into, overwire the initial function.
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
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
