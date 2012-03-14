package org.wildlifeimages.android.wildlifeimages;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class PhotosActivity extends WireActivity {

	@Override public void onCreate(Bundle inState){
		super.onCreate(inState);
		setContentView(R.layout.photos_layout);
		if (true == getIntent().getBooleanExtra("showEvents", false)){
			((ExhibitView) findViewById(R.id.photos_view)).loadUrl(loadString(R.string.intro_url_events), ContentManager.getSelf());
		}else{
			String[] introPhotoList = getResources().getStringArray(R.array.intro_image_list);
			((ExhibitView) findViewById(R.id.photos_view)).loadUrlList(introPhotoList, ContentManager.getSelf());
		}
	}

	@Override
	protected void onResume(){
		super.onResume();
	
		ExhibitView exView = (ExhibitView) findViewById(R.id.photos_view);
		String[] introPhotoList = getResources().getStringArray(R.array.intro_image_list);
		exView.loadUrlList(introPhotoList, ContentManager.getSelf());
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		ExhibitView exView = (ExhibitView) findViewById(R.id.photos_view);
		exView.clear();
	}
	
	public static void start(Activity context, boolean showEvents) {
		Intent photosIntent = new Intent(context, PhotosActivity.class);
		photosIntent.putExtra("showEvents", showEvents);
		context.startActivity(photosIntent);
	}
}
