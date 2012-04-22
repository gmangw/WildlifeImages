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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

/**
 * For playing audio.
 * 
 * @author Graham Wilkinson
 * @author Shady Glenn
 * @author Naveen Nanja
 *
 */
public class AudioActivity extends WireActivity implements OnCompletionListener{

	private MediaPlayer soundPlayer = null;

	private MediaThread updater;

	private boolean isPlaying = false;

	/**
	 * This will happen when the activity actually starts.
	 * Will grab the latest state of the current AV file and show the AV player.
	 * 
	 * @param bundle a savedState that holds the current state.
	 * 
	 */
	@Override
	public void onCreate(Bundle bundle){
		super.onCreate(bundle);
		setContentView(R.layout.media_progress_layout);

		String imageUrl = getIntent().getStringExtra("Image");
		if (imageUrl != null){
			ImageView v = (ImageView)findViewById(R.id.audio_image);
			v.setImageBitmap(ContentManager.getBitmapThumb(imageUrl, getAssets()));
		}

		Object instance = getLastNonConfigurationInstance();
		if (instance == null){
			String url = getIntent().getStringExtra("URL");
			if (url == null){
				url = "";
			}
			soundPlayer = playSound(url, getAssets());
			if (soundPlayer == null){
				soundPlayer = new MediaPlayer();
			}
			soundPlayer.setOnCompletionListener(this);
			updater = new MediaThread();
			updater.execute(soundPlayer);
		}else{
			soundPlayer = (MediaPlayer)instance;
			updater = new MediaThread();
			updater.execute(soundPlayer);
			mediaPause(null);
			if (bundle != null){
				if (false == bundle.getBoolean("Playing")){
					mediaPause(null);
				}
			}
		}

		SeekBar progress = (SeekBar)findViewById(R.id.media_progress);
		progress.setOnSeekBarChangeListener(updater);
	}

	//Override comment sufficient.
	@Override
	public void onRestart(){
		super.onRestart();
		updater = new MediaThread();
		updater.execute(soundPlayer);
		if (isPlaying == true){
			mediaPause(null);
		}
	}

	//Override comment sufficient.
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putBoolean("Playing", soundPlayer.isPlaying());
	}

	//Override comment sufficient.
	@Override
	public Object onRetainNonConfigurationInstance(){
		return soundPlayer;
	}

	/**
	 * This will play the audio file.
	 * 
	 * @param shortUrl that is a short URL to the AV file.
	 * @param contentManager that keeps track of state, cached items, and open connections.
	 * @param assets that lets us know where the items in the assets folder are.
	 * 
	 */
	public MediaPlayer playSound(String shortUrl, AssetManager assets){
		MediaPlayer soundPlayer = new MediaPlayer();
		try{
			AssetFileDescriptor fd = ContentManager.getFileDescriptor((shortUrl), assets);
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
			return null;
		} catch (NullPointerException e){
			Log.e(this.getClass().getName(), Log.getStackTraceString(e));
			return null;
		}
		return soundPlayer;
	}

	/**
	 * Bootstrapper that allows the launching of activities.
	 * So will start the activity for this page.
	 * 
	 * @param context the activity that is calling this function, place to launch from.
	 * @param url that has the URL to the AV item.
	 * @param imageUrl the URL of the image.
	 * 
	 */
	public static void start(Context context, String url, String imageUrl) {
		/* The AVActivity needs to know what the context is, so we add it to the intent here. */
		Intent avIntent = new Intent(context, AudioActivity.class);

		/* Add the URL we have to the hash table of the intent with the key URL. */
		avIntent.putExtra("URL", url);
		avIntent.putExtra("Image", imageUrl);

		/* Start the activity. */
		context.startActivity(avIntent);
	}

	//Override comment sufficient.
	@Override
	protected void onPause(){
		/* Called when activity is paused: when return to home screen, or another activity starts, or when rotate. */
		super.onPause();
		Button b = (Button)findViewById(R.id.media_pause_button);
		if (soundPlayer.isPlaying()){
			soundPlayer.pause();
			b.setBackgroundResource(R.drawable.play_button);
			isPlaying = true;
		}else{
			isPlaying = false;
		}
		updater.cancel(true);
	}

	//Override comment sufficient.
	@Override
	public void onBackPressed(){
		/* Calls mediaStop to stop the media ad return to the previous page. */
		mediaStop(null);
	}

	/**
	 * Pauses playing media and starts paused media.
	 * 
	 * @param v a View of the button pressed.
	 * 
	 */
	public void mediaPause(View v){
		Button b = (Button)findViewById(R.id.media_pause_button);
		if (soundPlayer.isPlaying()){
			soundPlayer.pause();
			b.setBackgroundResource(R.drawable.play_button);
		}else{
			soundPlayer.start();
			b.setBackgroundResource(R.drawable.pause_button);
		}
	}

	/**
	 * Stops the playing media and returns to the previous page.
	 * 
	 * @param v a View of the button pressed.
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
	 * On completion of the media, stop the media playing.
	 * 
	 * @param mp a MediaPlayer of the player playing the currently selected AV item.
	 * 
	 */
	public void onCompletion(MediaPlayer mp) {
		mediaStop(null);
	}

	/**
	 * Runs n the background and periodically gets into the user interface thread and perform tasks on the stop and end.
	 * Has a publish progress that will show the progress of the AV item.
	 * 
	 */
	public class MediaThread extends AsyncTask<MediaPlayer, Integer, Integer> implements OnSeekBarChangeListener{

		private boolean allowProgress = true;

		/**
		 * Will publish progress at certain times in the media player.
		 * 
		 * @param params a MediaPlayer... (list of media players) that takes in an array of mysterious size.
		 * 
		 */
		@Override
		protected Integer doInBackground(MediaPlayer... params) {
			MediaPlayer player = params[0];
			while(isCancelled() == false){
				if (player.isPlaying()){
					publishProgress(100 * player.getCurrentPosition()/player.getDuration());
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {;
				}
			}
			return 0;
		}

		/**
		 * Will set the progress bar at certain times in the media player.
		 * Called every time when publish progress is called, will update draggable thumb location while playing.
		 * 
		 * @param amount an Integer... (integer list) that takes in an array of mysterious size.
		 * 
		 */
		@Override
		protected void onProgressUpdate(Integer... amount) {
			SeekBar progress = (SeekBar)findViewById(R.id.media_progress);
			if (allowProgress == true){
				progress.setProgress(amount[0]);
			}
		}

		/**
		 * Will seek to the selected location in the audio.
		 * 
		 * @param seekBar a SeekBar that allows the user to select a specific location in the audio.
		 * @param progress an int showing the amount of progress of the track.
		 * @param fromUser whether the user actually changed track position or if the program did.
		 * 
		 */
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			if (fromUser){
				soundPlayer.seekTo(progress*soundPlayer.getDuration()/100);
			}
		}

		/**
		 * Allows the user to move the draggable thumb without trying to reset to current position.
		 * Starts tracking movement and disables progress while tracking.
		 * 
		 * @param seekBar a SeekBar that allows the user to select a specific location in the audio.
		 * 
		 */
		public void onStartTrackingTouch(SeekBar seekBar) {
			allowProgress = false;
		}

		/**
		 * Allows the user to move the draggable thumb without trying to reset to current position.
		 * Stops tracking movement and enables progress after tracking complete.
		 * 
		 * @param seekBar a SeekBar that allows the user to select a specific location in the audio.
		 * 
		 */
		public void onStopTrackingTouch(SeekBar seekBar) {	
			allowProgress = true;
		}
	}
}