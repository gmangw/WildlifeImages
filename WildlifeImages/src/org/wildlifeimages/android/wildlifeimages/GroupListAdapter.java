package org.wildlifeimages.android.wildlifeimages;


import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class GroupListAdapter extends BaseAdapter{
	private final String groupName;
	private final ExhibitList exhibitList = ContentManager.getExhibitList();
	
	public GroupListAdapter(String name){
		groupName = name;
	}
	
	public int getCount() {
		return exhibitList.getGroup(groupName).length;
	}

	public Object getItem(int position) {
		return exhibitList.getGroup(groupName)[position];
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {		
		String exhibitName = exhibitList.getGroup(groupName)[position];
		Exhibit entry = exhibitList.get(exhibitName);
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.list_item_layout, null);
		}
		
		if (entry.getPhotos().length > 0){
			ExhibitPhoto[] photos = entry.getPhotos();
			ImageView thumb = (ImageView)convertView.findViewById(R.id.listitemphoto);
			Bitmap bmp = ContentManager.getBitmapThumb(photos[0].shortUrl, convertView.getContext().getAssets());
			thumb.setImageBitmap(bmp);
		}

		TextView itemLabel = (TextView) convertView.findViewById(R.id.listitemlabel);
		itemLabel.setText(entry.getName());

		return convertView;
	}
}
