package org.wildlifeimages.android.wildlifeimages;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * This adapter uses an {@link ExhibitList} to show {@link Exhibit} info within a {@linkplain android.widget.ListView ListView}. 
 * 
 * @author Graham Wilkinson 
 * 	
 */
public class ExhibitListAdapter extends BaseAdapter{

	ExhibitList backingList;
	Context context;

	public ExhibitListAdapter(Context context, ExhibitList list){
		backingList = list;
		this.context = context;
	}

	public int getCount() {
		return backingList.getCount();
	}

	public Exhibit getItem(int position) {
		return backingList.getExhibitAt(position);
	}

	public long getItemId(int position) {
		return position;
	}

	/* http://techdroid.kbeanie.com/2009/07/custom-listview-for-android.html */
	public View getView(int position, View convertView, ViewGroup viewGroup) {
		Exhibit entry = backingList.getExhibitAt(position);
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.list_item_layout, null);
		}

		if (entry.hasContent(Exhibit.TAG_PHOTOS)){
			String[] photos = entry.getContent(Exhibit.TAG_PHOTOS).split(",");
			ImageView thumb = (ImageView)convertView.findViewById(R.id.listitemphoto);
			Bitmap bmp = ContentManager.getSelf().getBitmapThumb(photos[0], convertView.getContext().getAssets());
			thumb.setImageBitmap(bmp);
		}

		TextView itemLabel = (TextView) convertView.findViewById(R.id.listitemlabel);
		itemLabel.setText(entry.getName());

		itemLabel.setTextColor(0xFFFFFFFF);
		if (backingList.getCurrent().equals(entry)){
			itemLabel.setTextColor(0xFF085FFF);
		}

		return convertView;
	}

}
