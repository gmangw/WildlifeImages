package org.wildlifeimages.android.wildlifeimages;

import java.io.IOException;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

/**
 * For playing audio and video.
 * Audio Video Activity.
 * 
 * @author Graham Wilkinson
 * @author Shady Glenn
 * @author Naveen Nanja
 *
 */
public class AVActivity extends WireActivity implements OnCompletionListener{

	private MediaPlayer soundPlayer = null;
	
	private MediaThread updater;

	/**
	 * This will happen when the activity actually starts.
	 * Will grab the latest state of the current AV file and show the AV player.
	 * 
	 * @param a bundle savedState that holds the current state.
	 */
	@Override
	public void onCreate(Bundle bundle){
		super.onCreate(bundle);
		setContentView(R.layout.media_progress_layout);

		String url = getIntent().getStringExtra("URL");

		soundPlayer = playSound(url, ContentManager.getSelf(), getAssets());
		soundPlayer.setOnCompletionListener(this);
		updater = new MediaThread();
		updater.execute(soundPlayer);
	}

	/**
	 * This will play the audio file.
	 * 
	 * @param a String shortUrl that is a short Url to the AV file.
	 * @param a ContentManager contentManager that keeps track of state, cached items, and open connections.
	 * @param an AssetManager assets that lets us know where the items in the assets folder are.
	 */
	public MediaPlayer playSound(String shortUrl, ContentManager contentManager, AssetManager assets){
		MediaPlayer soundPlayer = new MediaPlayer();
		try{
			AssetFileDescriptor fd = contentManager.getFileDescriptor((shortUrl), assets);
			if (fd.getStartOffset() == -1){
				soundPlayer.setDataSource(fd.getFileDescriptor());
			}else{
				soundPlayer.setDataSource(fd.getFileDescriptor(),fd.getStartOffset(),fd.getLength());
			}
			fd.close();
			soundPlayer.prepare();
			soundPlayer.start();			
		} catch (IOException e){
			Log.e(this.getClass().getName(), Log.getStackTraceString(e));
		}
		return soundPlayer;
	}

	/**
	 * Bootstrapper that allows the launching of activities.
	 * So will start the activity for this page.
	 * 
	 * @param a Context context 
	 * @param a String url that has the url to the AV item.
	 * 
	 */
	public static void start(Context context, String url) {
		/* The AVActivity needs to know what the context is, so we add it to the intent here. */
		Intent avIntent = new Intent(context, AVActivity.class);
		
		/* Add the URL we have to the hash table of the intent with the key URL. */
		avIntent.putExtra("URL", url);
		
		/* Start the activity. */
		context.startActivity(avIntent);
	}

	/**
	 * Pauses playing media and starts paused media.
	 * 
	 * @param a View v the button pressed.
	 * 
	 */
	public void mediaPause(View v){
		if (soundPlayer.isPlaying()){
			soundPlayer.pause();
		}else{
			soundPlayer.start();
		}
	}

	/**
	 * Stops the playing media.
	 * 
	 * @param a View v the button pressed.
	 * 
	 */
	public void mediaStop(View v){
		soundPlayer.stop();
		
		/* Cancel the thread showing the progress bar. */
		updater.cancel(true);
		
		/* Exits the activity and returns to the previous page. */
		finish();
	}
	
	/**
	 * On completion of the media stop the media playing.
	 * 
	 * @param a MediaPlayer mp of the player playing the currently selected AV item.
	 * 
	 */
	public void onCompletion(MediaPlayer mp) {
		mediaStop(null);
	}
	
	/**
	 * Runs n the background and periodically gets into the user interface thread and perform tasks on the stop and end.
	 * Has a publish progress that will show the progress of the AV item.
	 */
	public class MediaThread extends AsyncTask<MediaPlayer, Integer, Integer>{

		private MediaPlayer player;

		public MediaThread(){
		}

		@Override
		protected void onPreExecute(){
		}

		@Override
		protected void onPostExecute(Integer i){
		}

		/**
		 * Will publish progress at certain times in the media player.
		 * 
		 * @param a MediaPlayer... params that takes in an array of mysterious size.
		 * 
		 */
		@Override
		protected Integer doInBackground(MediaPlayer... params) {
			player = params[0];
			while(isCancelled() == false){
				if (player.isPlaying()){
					publishProgress(100 * player.getCurrentPosition()/player.getDuration());
				}
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {;
				}
			}
			return 0;
		}

		/**
		 * Will set the progress bar at certain times in the media player.
		 * 
		 * @param an Integer... amount that takes in an array of mysterious size.
		 * 
		 */
		@Override
		protected void onProgressUpdate(Integer... amount) {
			ProgressBar progress = (ProgressBar)findViewById(R.id.media_progress);
			if (progress != null){
				progress.setProgress(amount[0]);
			}
		}
	}
}
