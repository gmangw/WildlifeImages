package org.wildlifeimages.android.wildlifeimages;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.CharBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class UpdateActivity extends WireActivity implements OnCancelListener{

	/*
	 * This progress manager will handle the update button when pressed.
	 * It will be what you see when you are updating the app.
	 */
	private static final int UPDATE_DIALOG = 1;

	private boolean cancelled = false;
	
	private Pattern zipNameExpression = Pattern.compile("http://.*?/update_\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\.zip");

	public void onCreate(Bundle inState){
		super.onCreate(inState);
		
		if (inState == null){
			if (Common.isNetworkConnected(this) == true){
				showDialog(UPDATE_DIALOG);
				ContentManager contentManager = ContentManager.getSelf();
				contentManager.clearCache();

				new ContentUpdater(contentManager).execute(this);
			}else{
				Toast.makeText(getApplicationContext(), "Cannot update: No internet connection available.", Toast.LENGTH_SHORT).show();
				finish();
			}
		}else{
			Log.w(this.getClass().getName(), "Update already in progress.");
		}
	}

	public static void start(Activity context) {
		Intent introIntent = new Intent(context, UpdateActivity.class);
		context.startActivityIfNeeded(introIntent, 0);
	}

	protected Dialog onCreateDialog(int id){
		super.onCreateDialog(id);

		final ProgressDialog progressDialog = new ProgressDialog(this);
		progressDialog.setMessage("Getting updated content...");
		progressDialog.show();
		progressDialog.setOnCancelListener(this);

		return progressDialog;
	}

	public void onUpdateCompleted(boolean result) {
		ContentManager.getSelf().prepareExhibits(this.getAssets());
		if (result == true){
			Toast.makeText(this.getApplicationContext(), "Update Complete", Toast.LENGTH_SHORT).show();
		}else{
			Toast.makeText(this.getApplicationContext(), "Update Failed - Check internet connection", Toast.LENGTH_SHORT).show();
		}
		finish();
	}

	public void onCancel(DialogInterface dialog) {
		cancelled = true;
	}

	private String getZipUrl(){
		try{
			URL url = new URL("http://oregonstate.edu/~wilkinsg/wildlifeimages/update.html");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			InputStream webStream = conn.getInputStream();
			InputStreamReader r = new InputStreamReader(webStream);

			CharBuffer chars = CharBuffer.allocate(1024);
			r.read(chars);
			String fileContents = chars.rewind().toString();

			Matcher m = zipNameExpression.matcher(fileContents);
			if (m.find() == true){
				return m.group();
			}else{
				return null;
			}
		} catch(MalformedURLException e){
			return null;
		} catch (IOException e) {
			return null;
		}
	}
	
	public class ContentUpdater extends AsyncTask<UpdateActivity, Integer, Boolean>{

		private UpdateActivity progress;
		private ContentManager content;

		public ContentUpdater(ContentManager manager){
			content = manager;
		}

		@Override
		protected Boolean doInBackground(UpdateActivity... arg0) {
			progress = arg0[0];

			String url = getZipUrl();
			
			SharedPreferences preferences = getSharedPreferences("Update", Context.MODE_PRIVATE);
			String oldUrl = preferences.getString("lastUpdate", "<none>");
			Editor editablePreferences = preferences.edit();
			editablePreferences.putString("lastUpdate", url);
			editablePreferences.commit();
			
			Log.i(this.getClass().getName(), "Grabbing " + url + ", previously grabbed " + oldUrl);
			
			if (url == null){
				return false;
			}else{
				return content.updateCache(this, url);
			}
		}

		@Override
		protected void onPostExecute(Boolean result){
			try{
				dismissDialog(UPDATE_DIALOG);	
			}catch(IllegalArgumentException e){
				Log.e(this.getClass().getName(), "Could not dismiss dialog.");
			}
			progress.onUpdateCompleted(result);
		}

		public void publish(int amount){
			publishProgress(amount);
		}

		@Override
		protected void onCancelled(){
			Log.w(this.getClass().getName(), "onCancelled");
			cancelled = true;
		}

		@Override
		protected void onProgressUpdate(Integer... amount) {
			if (cancelled == true){
				this.cancel(true);
			}
			if (amount[0] == -1){

			}else{
				if (progress != null){
					progress.setProgress(amount[0]);
				}	
			}
		}
	}
	
}

