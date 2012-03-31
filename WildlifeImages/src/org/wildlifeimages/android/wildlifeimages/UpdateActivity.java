package org.wildlifeimages.android.wildlifeimages;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.content.SharedPreferences.Editor;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class UpdateActivity extends WireActivity implements OnCancelListener{

	/*
	 * This progress manager will handle the update button when pressed.
	 * It will be what you see when you are updating the app.
	 */
	private static final int UPDATE_DIALOG = WireActivity.SCAN_DIALOG+1;

	private boolean cancelled = false;

	public void onCreate(Bundle inState){
		super.onCreate(inState);

		if (inState == null){
			showDialog(UPDATE_DIALOG);
			ContentManager contentManager = ContentManager.getSelf();
			contentManager.clearCache();

			Intent i = getIntent();
			new ContentUpdater().execute(i.getStringExtra(""));//TODO
		}else{
			Log.w(this.getClass().getName(), "Update already in progress.");
		}
	}
	
	protected Dialog onCreateDialog(int id){
		Dialog parent = super.onCreateDialog(id);

		switch(id){
		case UPDATE_DIALOG:
			final ProgressDialog progressDialog = new ProgressDialog(this);
			progressDialog.setMessage(loadString(R.string.update_dialog_message));
			progressDialog.show();
			progressDialog.setOnCancelListener(this);
			return progressDialog;
		default:
			return parent;
		}
	}

	public void onUpdateCompleted(boolean result) {
		ContentManager.getSelf().prepareExhibits(this.getAssets());
		if (result == true){
			setResult(Activity.RESULT_OK);
			Toast.makeText(this.getApplicationContext(), loadString(R.string.update_result_success), Toast.LENGTH_SHORT).show();
		}else{
			ContentManager.getSelf().clearCache();
			setResult(Activity.RESULT_CANCELED);
			Toast.makeText(this.getApplicationContext(), loadString(R.string.update_result_failure), Toast.LENGTH_SHORT).show();
		}
		finish();
	}

	public void onCancel(DialogInterface dialog) {
		cancelled = true;
	}

	public class ContentUpdater extends AsyncTask<String, Integer, Boolean>{

		@Override
		protected Boolean doInBackground(String... arg0) {

			String url = Common.getZipUrl(loadString(R.string.update_page_url));

			if (url == null){
				return false;
			}else{
				boolean result =  ContentManager.getSelf().updateCache(this, url);
				if (true == result){
					SharedPreferences preferences = getSharedPreferences(loadString(R.string.update_preferences), Context.MODE_PRIVATE);
					Editor editablePreferences = preferences.edit();
					editablePreferences.putString(loadString(R.string.update_preferences_key_last), url);
					editablePreferences.commit();
				}
				return result;
			}
		}

		@Override
		protected void onPostExecute(Boolean result){
			try{
				dismissDialog(UPDATE_DIALOG);	
			}catch(IllegalArgumentException e){
				Log.e(this.getClass().getName(), "Could not dismiss dialog.");
			}
			onUpdateCompleted(result);
		}

		public void publish(int amount){
			publishProgress(amount);
		}

		@Override
		protected void onCancelled(){
			setResult(Activity.RESULT_CANCELED);
			finish();
		}

		@Override
		protected void onProgressUpdate(Integer... amount) {
			if (cancelled == true){
				this.cancel(true);
			}
			if (amount[0] == -1){

			}else{
				setProgress(amount[0]);	
			}
		}
	}

}

