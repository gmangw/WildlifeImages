package org.wildlifeimages.android.wildlifeimages;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Gallery;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

public class EventsActivity extends WireActivity{

	@Override protected void onCreate(Bundle bundle){
		super.onCreate(bundle);

		setContentView(R.layout.events_layout);
		Gallery gallery = (Gallery)findViewById(R.id.events_view);
		gallery.setSpacing(0);
		gallery.setAdapter(new EventAdapter());

		if (bundle == null){
			new AsyncTask<Integer, Integer, Integer>(){
				@Override
				protected Integer doInBackground(Integer... arg0) {
					try {
						Thread.sleep(50);
					} catch (InterruptedException e1) {
					}
					for (int i=0; i<arg0[0]; i++)
						try {
							Thread.sleep(100);
							publishProgress(0);
						} catch (InterruptedException e) {
						}

						return 0;
				}
				@Override
				protected void onProgressUpdate(Integer... results){
					Gallery gallery = (Gallery)findViewById(R.id.events_view);
					gallery.onKeyDown(KeyEvent.KEYCODE_DPAD_RIGHT, null);
				}
			}.execute(0);
			gallery.setSelection(0);
		}

	}

	public static void start(Activity context) {
		Intent eventsIntent = new Intent(context, EventsActivity.class);
		context.startActivity(eventsIntent);
	}
	
	private class EventAdapter implements SpinnerAdapter{
		public EventAdapter(){
		}
		
		public int getCount() {
			return 10;
		}
		public Object getItem(int position) {
			return null;
		}
		public long getItemId(int position) {
			return position;
		}
		public int getItemViewType(int position) {
			return 0;
		}
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.event_item_layout, null);
			}
			convertView.setLayoutParams(new Gallery.LayoutParams(300, 300));
			TextView itemLabel = (TextView) convertView.findViewById(R.id.event_item_name);
			itemLabel.setText("Item " + position);
			itemLabel = (TextView) convertView.findViewById(R.id.event_item_description);
			itemLabel.setText("Item " + position + " is a large item with lots of event text. blah blah blah. So much text that it runs onto multiple lines, showing off ellipsizing.");

			convertView.setBackgroundResource(R.drawable.android_button);

			return convertView;
		}
		public int getViewTypeCount() {
			return 1;
		}
		public boolean hasStableIds() {
			return false;
		}
		public boolean isEmpty() {
			return false;
		}
		public void registerDataSetObserver(DataSetObserver observer) {
		}
		public void unregisterDataSetObserver(DataSetObserver observer) {
		}
		public View getDropDownView(int position, View convertView, ViewGroup parent) {
			return null;
		}
	}

}
