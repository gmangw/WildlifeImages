package org.wildlifeimages.android.wildlifeimages;

import java.util.Iterator;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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
        
        TextView itemLabel = (TextView) convertView.findViewById(R.id.listitemlabel);
        itemLabel.setText(entry.getName());
        
        TextView itemInfo = (TextView) convertView.findViewById(R.id.listiteminfo);
        Iterator<String> tagList = entry.getTags();
        String info = "";
        tagList.next();
        while(tagList.hasNext()){
        	info = info.concat(tagList.next());
        	if (tagList.hasNext()){
        		info = info.concat(", ");
        	}
        }
        itemInfo.setText(info);
        
        if (backingList.getCurrent().equals(entry)){
        	itemLabel.setTextColor(0xFF085FFF);
        }

        return convertView;
    }

}
