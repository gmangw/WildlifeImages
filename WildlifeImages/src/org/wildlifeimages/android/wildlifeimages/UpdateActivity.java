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
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class UpdateActivity extends WireActivity implements UpdateListener{

	/*
	 * This progress manager will handle the update button when pressed.
	 * It will be what you see when you are updating the app.
	 */
	private ProgressManager updateDialogManager = new ProgressManager();

	private static final int UPDATE_DIALOG = 1;

	public void onCreate(Bundle inState){
		super.onCreate(inState);

		if (inState == null){
			if (isNetworkConnected() == true){
				showDialog(UPDATE_DIALOG);
				ContentManager contentManager = ContentManager.getSelf();
				contentManager.clearCache();

				updateDialogManager.registerUpdateListener(this);
				
				new ContentUpdater(contentManager).execute(updateDialogManager);
			}else{
				Toast.makeText(getApplicationContext(), "Cannot update: No internet connection available.", Toast.LENGTH_SHORT).show();
				finish();
			}
		}else{
			Log.w(this.getClass().getName(), "Update already in progress.");
		}
	}

	public boolean isNetworkConnected(){
		ConnectivityManager manager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netData = manager.getActiveNetworkInfo();
		if (netData != null && netData.isConnectedOrConnecting() == true){
			return true;
		}else{
			return false;
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

		updateDialogManager.setDialog(progressDialog);

		return progressDialog;
	}

	public void onUpdateCompleted(boolean result) {
		ContentManager.getSelf().prepareExhibits(this.getAssets());
		if (result == true){
			Toast.makeText(this.getApplicationContext(), "Update Complete", Toast.LENGTH_SHORT).show();
		}else{
			Toast.makeText(this.getApplicationContext(), "Update Failed", Toast.LENGTH_SHORT).show();
		}
		finish();
	}


	public class ContentUpdater extends AsyncTask<ProgressManager, Integer, Boolean>{

		ProgressManager progress;
		ContentManager content;
		String label = "";

		public ContentUpdater(ContentManager manager){
			content = manager;
		}

		@Override
		protected Boolean doInBackground(ProgressManager... arg0) {
			progress = arg0[0];

			String url = getZipUrl();
			Log.i(this.getClass().getName(), "Grabbing " + url);
			if (url == null){
				return false;
			}else{
				return content.updateCache(this, url);
			}
		}

		@Override
		protected void onPostExecute(Boolean result){
			progress.dismiss(result);	
			content = null;
			progress = null;
		}

		public void publish(int amount){
			publishProgress(amount);
		}

		@Override
		protected void onCancelled(){
			Log.w(this.getClass().getName(), "onCancelled");
			progress.reset();
		}

		@Override
		protected void onProgressUpdate(Integer... amount) {
			if (progress.isCancelled()){
				this.cancel(true);
			}
			if (amount[0] == -1){
				progress.setText(label);
			}else{
				if (progress != null){
					progress.setProgress(amount[0]);
				}	
			}
		}

		private String getZipUrl(){
			Pattern zipNameExpression = Pattern.compile("http://.*?\\.zip");
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
	}

}

