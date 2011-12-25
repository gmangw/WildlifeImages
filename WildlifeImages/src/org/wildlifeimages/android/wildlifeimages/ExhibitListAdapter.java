package org.wildlifeimages.android.wildlifeimages;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class ExhibitListAdapter extends BaseAdapter{
	
	ExhibitList backingList;
	
	public int getCount() {
		return backingList.getCount();
	}

	public Exhibit getItem(int position) {
		return backingList.getExhibitAt(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		return null;
	}

}
