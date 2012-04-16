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
	private String groupFilter = "";
	private final StringBuilder sb = new StringBuilder();

	public ExhibitListAdapter(Context context){
	}

	public int getCount() {
		ExhibitList list = ContentManager.getExhibitList();
		String[] groupExhibits = list.getGroup(groupFilter);
		if (groupExhibits.length > 0){
			return groupExhibits.length;
		}else{
			return list.getCount();
		}
	}

	public Exhibit getItem(int position) {
		ExhibitList list = ContentManager.getExhibitList();
		String[] groupExhibits = list.getGroup(groupFilter);
		if (groupExhibits.length > 0){
			return list.get(groupExhibits[position]);
		}else{
			return list.getExhibitAt(position);
		}
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup viewGroup) {
		Exhibit entry = getItem(position);
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) viewGroup.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
		TextView itemDataLabel = (TextView)convertView.findViewById(R.id.list_item_info_label);
		if (itemDataLabel != null){
			for (int i=0; i<entry.getTagCount(); i++){
				//if (false == entry.getTag(i).equals(Exhibit.TAG_PHOTOS)){
					sb.append(entry.getTag(i));
					sb.append(" | ");
				//}
			}
			sb.append("Photos");
			itemDataLabel.setText(sb.toString());
			sb.setLength(0);
		}

		return convertView;
	}

	public void setGroupFilter(String filter){
		if (groupFilter.equals(filter) == false){
			groupFilter = filter;
			this.notifyDataSetChanged();
		}
	}

	public String getGroupFilter(){
		return groupFilter;
	}

}
