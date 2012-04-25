package org.wildlifeimages.android.wildlifeimages;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class UpdateActivity extends WireActivity{

	/*
	 * This progress manager will handle the update button when pressed.
	 * It will be what you see when you are updating the app.
	 */

	private ContentUpdateTask updateTask = new ContentUpdateTask();

	public void onCreate(Bundle inState){
		super.onCreate(inState);

		setContentView(R.layout.update_layout);

		ContentManager.clearCache();

		updateTask.execute("");
	}

	public void onUpdateCompleted(boolean result) {
		if (result == true){
			setResult(Activity.RESULT_OK);
			ContentManager.prepareExhibits(this.getResources());
			ContentManager.getSVG(this.getResources());
			Toast.makeText(this.getApplicationContext(), loadString(R.string.update_result_success), Toast.LENGTH_SHORT).show();
		}else{
			setResult(Activity.RESULT_CANCELED);
			Toast.makeText(this.getApplicationContext(), loadString(R.string.update_result_failure), Toast.LENGTH_SHORT).show();
		}

		finish();
	}
	
	@Override
	public void onBackPressed(){
		cancelUpdate();
	}

	@Override
	public void onPause(){
		super.onPause(); 
		
		cancelUpdate();
	}
	
	private void cancelUpdate(){
		Toast.makeText(this.getApplicationContext(), loadString(R.string.update_result_cancelled), Toast.LENGTH_SHORT).show();
		setResult(Activity.RESULT_CANCELED);
		updateTask.cancel(true);
		finish();
	}
	
	public class ContentUpdateTask extends AsyncTask<String, Integer, Boolean>{

		@Override
		protected Boolean doInBackground(String... arg0) {

			String url = Common.getZipUrl(loadString(R.string.update_page_url));

			if (url == null){
				return false;
			}else{
				boolean result =  ContentManager.updateCache(this, url);
				if (true == result){
					SharedPreferences preferences = getSharedPreferences(loadString(R.string.update_preferences), Context.MODE_PRIVATE);
					Editor editablePreferences = preferences.edit();
					editablePreferences.putString(loadString(R.string.update_preferences_key_last), url);
					editablePreferences.commit();
				}
				return result;
			}
		}

		public void publish(int progress){
			super.publishProgress(progress);
		}

		@Override
		protected void onPostExecute(Boolean result){
			onUpdateCompleted(result);
		}

		@Override
		protected void onProgressUpdate(Integer... amount) {
			if (amount[0] != -1){
				TextView text = (TextView)findViewById(R.id.update_progress_text);	
				text.setText(amount[0]/1024 + " kb downloaded.");
			}
		}
	}

}

