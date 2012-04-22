package org.wildlifeimages.android.wildlifeimages;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class PhotosActivity extends WireActivity {

	private boolean showingEvents;

	@Override public void onCreate(Bundle inState){
		super.onCreate(inState);
		setContentView(R.layout.photos_layout);

		if (inState == null){
			ArrayList<ExhibitPhoto> list = new ArrayList<ExhibitPhoto>();
			for (int i=0; i<ContentManager.getExhibitList().getCount(); i++){
				for (ExhibitPhoto p : ContentManager.getExhibitList().getExhibitAt(i).getPhotos()){
					list.add(p);
				}
			}
			ContentManager.getBitmap(list.get(0).shortUrl, getAssets());
		}
		
		if (true == getIntent().getBooleanExtra("showEvents", false)){
			((ExhibitView) findViewById(R.id.photos_view)).loadHtmlUrl(loadString(R.string.intro_url_events));
			showingEvents = true;
		}else{
			showingEvents = false;
		}
	}

	@Override
	protected void onResume(){
		super.onResume();

		ExhibitView exView = (ExhibitView) findViewById(R.id.photos_view);
		if (showingEvents == false){
			ArrayList<ExhibitPhoto> list = new ArrayList<ExhibitPhoto>();
			for (int i=0; i<ContentManager.getExhibitList().getCount(); i++){
				for (ExhibitPhoto p : ContentManager.getExhibitList().getExhibitAt(i).getPhotos()){
					list.add(p);
				}
			}
			exView.loadPhotoList(list.toArray(new ExhibitPhoto[0]));
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (showingEvents == false){
			ExhibitView exView = (ExhibitView) findViewById(R.id.photos_view);
			exView.clear();
		}
	}

	public static void start(Activity context, boolean showEvents) {
		Intent photosIntent = new Intent(context, PhotosActivity.class);
		photosIntent.putExtra("showEvents", showEvents);
		context.startActivity(photosIntent);
	}
}
