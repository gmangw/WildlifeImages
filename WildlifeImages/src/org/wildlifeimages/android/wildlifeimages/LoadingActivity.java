package org.wildlifeimages.android.wildlifeimages;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.PixelFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ProgressBar;

public class LoadingActivity extends WireActivity{

	private ThumbLoader thumbLoad = null;

	@Override
	public void onCreate(Bundle inState){
		super.onCreate(inState);
		getWindow().setFormat(PixelFormat.RGBA_8888);
		setContentView(R.layout.loading_layout);
	}

	protected void onStart(){
		super.onStart();

		thumbLoad = new ThumbLoader();
		thumbLoad.execute(getAssets());
	}

	private class ThumbLoader extends AsyncTask<AssetManager, Integer, Integer>{
		@Override
		protected void onPreExecute(){
			ProgressBar progress = (ProgressBar)findViewById(R.id.loading_progress);
			progress.setMax(ContentManager.getExhibitList().getCount());
		}
		@Override
		protected Integer doInBackground(AssetManager... assets){
			for(int i=0; i<ContentManager.getExhibitList().getCount() && isCancelled () == false; i++){
				Exhibit entry = ContentManager.getExhibitList().getExhibitAt(i);
				if (entry.getPhotos().length > 0){
					String photo = entry.getPhotos()[0].shortUrl;
					ContentManager.getBitmapThumb(photo, assets[0]);
				}
				publishProgress(i);
			}
			return 0;
		}
		@Override
		protected void onPostExecute(Integer result){
			thumbLoad = null;
			finish();
		}
		@Override
		protected void onProgressUpdate(Integer... count){
			ProgressBar progress = (ProgressBar)findViewById(R.id.loading_progress);
			for (Integer i : count){
				progress.setProgress(i);
			}
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (thumbLoad != null){
			thumbLoad.cancel(true);
		}
	}

	public static void start(Activity context) {
		Intent loadingIntent = new Intent(context, LoadingActivity.class);
		context.startActivity(loadingIntent);
	}
}
