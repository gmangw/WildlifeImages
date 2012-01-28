package org.wildlifeimages.android.wildlifeimages;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

public class ExhibitListActivity extends ListActivity {

	@Override
	public void onCreate(Bundle bundle){
		super.onCreate(bundle);

		setContentView(R.layout.list_layout);
		setListAdapter(new ExhibitListAdapter(this, ContentManager.getSelf().getExhibitList()));
	}

	@Override
	protected void onListItemClick(ListView list, View v, int position, long id){
		ExhibitList exhibitList = ContentManager.getSelf().getExhibitList();
		Exhibit e = (Exhibit)list.getItemAtPosition(position);
		exhibitList.setCurrent(e, Exhibit.TAG_AUTO);
		ExhibitActivity.start(this);
	}

	public static void start(Activity context) {
		Intent listIntent = new Intent(context, ExhibitListActivity.class);
		context.startActivityIfNeeded(listIntent, 0);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent){

	}
}
