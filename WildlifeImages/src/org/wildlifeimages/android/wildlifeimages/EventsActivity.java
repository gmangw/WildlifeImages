package org.wildlifeimages.android.wildlifeimages;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Date;

import org.wildlifeimages.android.wildlifeimages.Parser.Event;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class EventsActivity extends WireActivity{

	@Override protected void onCreate(Bundle bundle){
		super.onCreate(bundle);

		setContentView(R.layout.events_layout);
		final Gallery gallery = (Gallery)findViewById(R.id.events_view);
		EventAdapter adapter = new EventAdapter(loadEvents());
		gallery.setAdapter(adapter);

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

		final SeekBar seekBar = (SeekBar)findViewById(R.id.events_seekbar);
		gallery.setOnItemSelectedListener(new OnItemSelectedListener(){
			public void onItemSelected(AdapterView<?> adapterView, View view, int index, long arg3) {
				seekBar.setProgress(index);
				TextView month = (TextView)findViewById(R.id.events_month);
				TextView year = (TextView)findViewById(R.id.events_year);
				Event e = (Event)adapterView.getSelectedItem();
				Calendar now = Calendar.getInstance();
				now.setTime(e.getStartDay());
				month.setText(String.format("%1$tB", now));
				year.setText(String.format("%1$tY", now));
			}
			public void onNothingSelected(AdapterView<?> arg0) {
				seekBar.setProgress(0);
			}
		});

		seekBar.setMax(gallery.getCount()-1);
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				gallery.setSelection(progress);
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		});

		Event e = new Event();
		e.setTitle("Sample Event");
		e.setDescription("This event takes place at wildlife images");
		Date d = new Date();
		e.setStartDay(d);
		e.setEndDay(d);
		//addEventToCalendar(e);
	}

	public static void start(Activity context) {
		Intent eventsIntent = new Intent(context, EventsActivity.class);
		context.startActivity(eventsIntent);
	}

	private class EventAdapter implements SpinnerAdapter{
		private Event[] list;
		public EventAdapter(Event[] events){
			list = events;
		}

		public int getCount() {
			return list.length;
		}
		public Object getItem(int position) {
			return list[position];
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
			int width = 4*getWindowManager().getDefaultDisplay().getWidth()/5;
			convertView.setLayoutParams(new Gallery.LayoutParams(width, LayoutParams.FILL_PARENT));
			Event e = list[position];
			TextView itemLabel = (TextView) convertView.findViewById(R.id.event_item_name);
			itemLabel.setText(e.getTitle());
			itemLabel = (TextView) convertView.findViewById(R.id.event_item_description);
			itemLabel.setText(e.getDescription());

			ImageView b = (ImageView)convertView.findViewById(R.id.event_image);
			if (b != null){
				if (e.getImage() != null && false == e.getImage().equals("")){
					b.setImageBitmap(ContentManager.getBitmapThumb(e.getImage(), getAssets()));
					itemLabel.setBackgroundColor(getResources().getColor(android.R.color.background_light) - 0x55000000);
				}else{
					b.setImageResource(0);
				}
			}

			convertView.setBackgroundResource(R.drawable.event_border);

			return convertView;
		}
		public int getViewTypeCount() {
			return 1;
		}
		public boolean hasStableIds() {
			return true;
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

	private Event[] loadEvents(){
		try{
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlPullParser xmlBox = factory.newPullParser();
			InputStream istr = ContentManager.streamAssetOrFile("events.xml", getAssets());
			BufferedReader in = new BufferedReader(new InputStreamReader(istr), 1024);
			xmlBox.setInput(in);
			return Parser.parseEvents(xmlBox);
		}catch(XmlPullParserException e){
			return new Event[0];
		}catch(IOException e){
			return new Event[0];
		}
	}

	public void addEventToCalendar(View v){
		Gallery gallery = (Gallery)findViewById(R.id.events_view);
		addEventToCalendar((Event)gallery.getSelectedItem());
	}

	private void addEventToCalendar(Event event) {
		Intent l_intent = new Intent(Intent.ACTION_EDIT);
		l_intent.setType("vnd.android.cursor.item/event");
		l_intent.putExtra("title", event.getTitle());
		l_intent.putExtra("description", event.getDescription());
		l_intent.putExtra("eventLocation", "Wildlife Images");
		l_intent.putExtra("beginTime", event.getStartDay());
		l_intent.putExtra("endTime", event.getEndDay());
		l_intent.putExtra("allDay", true);
		try {
			startActivity(l_intent);
		} catch (Exception e) {
			Toast.makeText(this.getApplicationContext(), "Sorry, no compatible calendar was found!", Toast.LENGTH_LONG).show();
		}
	}

}
