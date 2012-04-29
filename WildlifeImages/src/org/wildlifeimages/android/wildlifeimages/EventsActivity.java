package org.wildlifeimages.android.wildlifeimages;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.wildlifeimages.android.wildlifeimages.Parser.Event;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class EventsActivity extends WireActivity{

	private int screenWidth = 300;
	private int eventWidth = 300;
	private int screenHeight = 300;
	private int eventHeight = 300;

	public static final int PHOTO_DIALOG = WireActivity.SCAN_DIALOG + 1;

	@Override 
	protected void onCreate(Bundle bundle){
		super.onCreate(bundle);

		screenWidth = getWindowManager().getDefaultDisplay().getWidth();
		screenHeight = getWindowManager().getDefaultDisplay().getHeight();
		eventWidth = 5*screenWidth/(6 * (1+screenWidth/1024));
		eventHeight = 2*screenHeight/3;

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
				TextView date = (TextView)findViewById(R.id.events_date);
				Event e = (Event)adapterView.getSelectedItem();
				Calendar start = Calendar.getInstance();
				start.setTime(e.getStartDay());
				if (e.getStartDay().compareTo(e.getEndDay()) == 0){
					date.setText(String.format("%1$tb %1$td", start));
				}else{
					Calendar end = Calendar.getInstance();
					end.setTime(e.getEndDay());
					date.setText(String.format("%1$tB %1$td - %2$tB %2$td", start, end));
				}
				TextView title = (TextView)findViewById(R.id.events_title);
				title.setText(String.format(loadString(R.string.events_layout_text) + " - %1$tY", start));
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
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog){
		super.onPrepareDialog(id, dialog);

		ImageView i = new ImageView(this);
		Gallery gallery = (Gallery)findViewById(R.id.events_view);
		Event e = (Event)gallery.getSelectedItem();
		i.setImageBitmap(ContentManager.getBitmap(e.getImage(), getAssets()));
		((AlertDialog)dialog).setView(i);
	}

	@Override
	protected Dialog onCreateDialog(int id){
		Dialog dialog = super.onCreateDialog(id);

		if (id == PHOTO_DIALOG){
			ImageView i = new ImageView(this);
			Gallery gallery = (Gallery)findViewById(R.id.events_view);
			Event e = (Event)gallery.getSelectedItem();
			i.setImageBitmap(ContentManager.getBitmap(e.getImage(), getAssets()));
			//dialog.setContentView(i);

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setView(i);
			dialog = builder.create();
			dialog.setCanceledOnTouchOutside(true);
			return dialog;
		}
		return dialog;
	}

	public void showEventImage(View v){
		Gallery gallery = (Gallery)findViewById(R.id.events_view);
		Event e = (Event)gallery.getSelectedItem();
		if (e.getImage().length() > 0){
			showDialog(PHOTO_DIALOG);
		}
	}

	public static void start(Activity context) {
		Intent eventsIntent = new Intent(context, EventsActivity.class);
		context.startActivity(eventsIntent);
	}

	private class EventAdapter implements SpinnerAdapter, ListAdapter{
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
			convertView.setLayoutParams(new Gallery.LayoutParams(eventWidth, (screenHeight > 1024) ? eventHeight : LayoutParams.FILL_PARENT));
			Event e = list[position];
			TextView itemLabel = (TextView) convertView.findViewById(R.id.event_item_name);
			itemLabel.setText(e.getTitle());
			itemLabel = (TextView) convertView.findViewById(R.id.event_item_description);
			itemLabel.setText(e.getDescription());

			ImageButton b = (ImageButton)convertView.findViewById(R.id.event_image);
			if (b != null){
				if (false == e.getImage().equals("")){
					b.setImageBitmap(ContentManager.getBitmapThumb(e.getImage(), getAssets()));
					itemLabel.setBackgroundColor(getResources().getColor(android.R.color.background_light) - 0x44000000);
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

		public boolean areAllItemsEnabled() {
			return true;
		}

		public boolean isEnabled(int position) {
			return true;
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

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (super.onOptionsItemSelected(item) == false){
			switch (item.getItemId()) {
			case R.id.menu_event:
				Gallery gallery = (Gallery)findViewById(R.id.events_view);
				addEventToCalendar((Event)gallery.getSelectedItem());
				return true;
			}
			return false;
		}else{
			return true;
		}
	}

	private void addEventToCalendar(Event event) {

		if (Common.isAtLeastICS() == true){
			Intent intent = new Intent(Intent.ACTION_INSERT);
			intent.setType("vnd.android.cursor.item/event");
			intent.putExtra("title", event.getTitle());
			intent.putExtra("description", event.getDescription());
			intent.putExtra("eventLocation", "Wildlife Images");
			intent.putExtra("beginTime", event.getStartDay());
			intent.putExtra("endTime", event.getEndDay());
			intent.putExtra("allDay", true);
			try {
				startActivity(intent);
			} catch (Exception e) {
				Toast.makeText(this.getApplicationContext(), "Sorry, no compatible calendar was found!", Toast.LENGTH_LONG).show();
			}
		}else if (Common.isAtLeastHoneycomb() == false){
			Intent intent = new Intent(Intent.ACTION_EDIT);
			intent.setType("vnd.android.cursor.item/event");
			intent.putExtra("title", event.getTitle());
			intent.putExtra("description", event.getDescription());
			intent.putExtra("eventLocation", "Wildlife Images");
			intent.putExtra("beginTime", event.getStartDay());
			intent.putExtra("endTime", event.getEndDay());
			intent.putExtra("allDay", true);
			try {
				startActivity(intent);
			} catch (Exception e) {
				Toast.makeText(this.getApplicationContext(), "Sorry, no compatible calendar was found!", Toast.LENGTH_LONG).show();
			}
		}else{
			Intent intent = new Intent(Intent.ACTION_EDIT);
			intent.setType("vnd.android.cursor.item/event");
			intent.putExtra("title", event.getTitle());
			intent.putExtra("description", event.getDescription());
			intent.putExtra("eventLocation", "Wildlife Images");
			intent.putExtra("beginTime", event.getStartDay());
			intent.putExtra("endTime", event.getEndDay());
			intent.putExtra("allDay", true);
			try {
				startActivity(intent);
			} catch (Exception e) {
				Toast.makeText(this.getApplicationContext(), "Sorry, no compatible calendar was found!", Toast.LENGTH_LONG).show();
			}
		}
	}

}
