package org.wildlifeimages.android.wildlifeimages;


import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
	private ExhibitListAdapter adapter;
	/**
	 * This will happen when the activity actually starts.
	 * Will grab the latest state of the current exhibit list and display it.
	 * 
	 * @param a bundle savedState that holds the current state.
	 */
	@Override
	public void onCreate(Bundle bundle){
		super.onCreate(bundle);

		if (false == Common.isAtLeastHoneycomb()){
			requestWindowFeature(Window.FEATURE_NO_TITLE);
		}else{
			setTheme(android.R.style.Theme_Holo_Light);
		}
		
		setContentView(R.layout.list_layout);
		adapter = new ExhibitListAdapter(this);
		if (bundle != null){
			adapter.setGroupFilter(bundle.getString("GroupFilter"));
		}
		setListAdapter(adapter);
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
		ExhibitList exhibitList = ContentManager.getExhibitList();
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
		context.startActivity(listIntent);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.listmenu, menu);
		
		return true;
	}
	
	@Override
	protected void onSaveInstanceState(Bundle bundle){
		bundle.putString("GroupFilter", adapter.getGroupFilter());
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		
		switch (item.getItemId()){
		case android.R.id.home:
			IntroActivity.start(this);
			break;
		case R.id.list_menu_all:
			adapter.setGroupFilter(getResources().getString(R.string.list_menu_all_group));
			break;
		case R.id.list_menu_birds:
			adapter.setGroupFilter(getResources().getString(R.string.list_menu_birds_group));
			break;
		case R.id.list_menu_mammals:
			adapter.setGroupFilter(getResources().getString(R.string.list_menu_mammals_group));
			break;
		case R.id.list_menu_facilities:
			adapter.setGroupFilter(getResources().getString(R.string.list_menu_facilities_group));
			break;
		}
		return true;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event){
		super.onKeyDown(keyCode, event);
		return Common.onKeyDown(this, keyCode, event);
	}
}
