package org.wildlifeimages.android.wildlifeimages;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

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

		
		return content.updateCache(this);
	}

	@Override
	protected void onPostExecute(Boolean result){
		if (result){
			progress.setText("Update Complete");
		}else{
			progress.setText("Update Failed");
		}
		progress.dismiss();	
		content = null;
		progress = null;
	}

	public void publish(int amount){
		publishProgress(amount);
	}

	public void setText(String text){
		label = text;
		publishProgress(-1);
	}

	public void show(){
		progress.show();
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
}
