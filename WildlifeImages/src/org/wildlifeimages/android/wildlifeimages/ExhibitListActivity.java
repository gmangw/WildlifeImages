package org.wildlifeimages.android.wildlifeimages;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ListView;

/**
 * This class will handle the exhibit list page.
 * 
 * @author Graham Wilkinson
 * @author Shady Glenn
 * @author Naveen Nanja
 * 	
 */
public class ExhibitListActivity extends ListActivity {

	/**
	 * This will happen when the activity actually starts.
	 * Will grab the latest state of the current exhibit list and display it.
	 * 
	 * @param a bundle savedState that holds the current state.
	 */
	@Override
	public void onCreate(Bundle bundle){
		super.onCreate(bundle);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.list_layout);
		setListAdapter(new ExhibitListAdapter(this, ContentManager.getSelf().getExhibitList()));
	}

	/**
	 * This will happen when the activity actually starts.
	 * Will grab the latest state of the current exhibit and call showExhibit to display it.
	 * 
	 * @param a ListeView list of the items in the exhibit list.
	 * @param a View v containing the list item clicked on.
	 * @param an int position containing the location in the list.
	 * @param a long id containing the id of the clicked on item in the list.
	 */
	@Override
	protected void onListItemClick(ListView list, View v, int position, long id){
		ExhibitList exhibitList = ContentManager.getSelf().getExhibitList();
		Exhibit e = (Exhibit)list.getItemAtPosition(position);
		exhibitList.setCurrent(e, Exhibit.TAG_AUTO);
		ExhibitActivity.start(this);
	}

	/**
	 * Bootstrapper that allows the launching of activities.
	 * So will start the activity for this page.
	 * 
	 * @param an Activity called context which takes whatever to be passed when starting this page.
	 */
	public static void start(Activity context) {
		Intent listIntent = new Intent(context, ExhibitListActivity.class);
		context.startActivityIfNeeded(listIntent, 0);
	}
}
