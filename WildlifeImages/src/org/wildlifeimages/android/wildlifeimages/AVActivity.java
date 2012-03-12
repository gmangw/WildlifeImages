package org.wildlifeimages.android.wildlifeimages;

import java.io.IOException;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

/**
 * For playing audio and video.
 * Audio Video Activity.
 * 
 * @author Graham Wilkinson
 * @author Shady Glenn
 * @author Naveen Nanja
 *
 */
public class AVActivity extends WireActivity implements OnCompletionListener, OnPreparedListener{

	private MediaPlayer soundPlayer = null;
	private MediaController mController;

	private Handler handler = new Handler();

	//private MediaThread updater;

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

		String imageUrl = getIntent().getStringExtra("Image");
		ImageView v = (ImageView)findViewById(R.id.audio_image);
		v.setImageBitmap(ContentManager.getSelf().getBitmapThumb(imageUrl, getAssets()));

		Object instance = getLastNonConfigurationInstance();
		if (instance == null){
			String url = getIntent().getStringExtra("URL");
			soundPlayer = playSound(url, ContentManager.getSelf(), getAssets());
			mController = new MediaController(this);
			soundPlayer.setOnCompletionListener(this); //TODO remove
			soundPlayer.setOnPreparedListener(this);
			//updater = new MediaThread();
			//updater.execute(soundPlayer);
		}else{
			soundPlayer = (MediaPlayer)instance;
			mController = new MediaController(this);
			onPrepared(soundPlayer);
			if (bundle != null){
				if (true == bundle.getBoolean("Playing")){
					soundPlayer.start();
				}
			}
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		mController.hide();
		//soundPlayer.stop();
		//soundPlayer.release();//TODO figure out when to call
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putBoolean("Playing", soundPlayer.isPlaying());
	}

	@Override
	public Object onRetainNonConfigurationInstance(){
		return soundPlayer;
	}

	private class MediaPlayerController implements MediaController.MediaPlayerControl{
		private final MediaPlayer mPlayer;
		public MediaPlayerController(MediaPlayer player){
			mPlayer = player;
		}
		public boolean canPause() {
			return true;
		}
		public boolean canSeekBackward() {
			return true;
		}
		public boolean canSeekForward() {
			return true;
		}
		public int getBufferPercentage() {
			return 0;
		}
		public int getCurrentPosition() {
			return mPlayer.getCurrentPosition();
		}
		public int getDuration() {
			return mPlayer.getDuration();
		}
		public boolean isPlaying() {
			return mPlayer.isPlaying();
		}
		public void pause() {
			mPlayer.pause();
		}
		public void seekTo(int pos) {
			mPlayer.seekTo(pos);
		}
		public void start() {
			mPlayer.start();
		}
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
	public static void start(Context context, String url, String imageUrl) {
		/* The AVActivity needs to know what the context is, so we add it to the intent here. */
		Intent avIntent = new Intent(context, AVActivity.class);

		/* Add the URL we have to the hash table of the intent with the key URL. */
		avIntent.putExtra("URL", url);
		avIntent.putExtra("Image", imageUrl);

		/* Start the activity. */
		context.startActivity(avIntent);
	}

	@Override
	protected void onPause(){
		super.onPause();
		Button b = (Button)findViewById(R.id.media_pause_button);
		if (soundPlayer.isPlaying()){
			soundPlayer.pause();
			b.setBackgroundResource(R.drawable.play_button);
		}
		//updater.cancel(true);
	}

	@Override
	public void onBackPressed(){
		finish();
	}

	/**
	 * On completion of the media stop the media playing.
	 * 
	 * @param a MediaPlayer mp of the player playing the currently selected AV item.
	 * 
	 */
	public void onCompletion(MediaPlayer mp) {
		//TODO
	}

	public void onPrepared(MediaPlayer mediaPlayer) {
		mController.setMediaPlayer(new MediaPlayerController(soundPlayer));
		mController.setAnchorView(findViewById(R.id.av_layout_frame));

		handler.post(new Runnable() {
			public void run() {
				mController.setEnabled(true);
				mController.show(0);
			}
		});
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		mController.show(0);
		return false;
	}

	/**
	 * Runs n the background and periodically gets into the user interface thread and perform tasks on the stop and end.
	 * Has a publish progress that will show the progress of the AV item.
	 */
	public class MediaThread extends AsyncTask<MediaPlayer, Integer, Integer> implements OnSeekBarChangeListener{

		private boolean allowProgress = true;

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
		 * 
		 * @param an Integer... amount that takes in an array of mysterious size.
		 * 
		 */
		@Override
		protected void onProgressUpdate(Integer... amount) {
			SeekBar progress = (SeekBar)findViewById(R.id.media_progress);
			if (allowProgress == true){
				progress.setProgress(amount[0]);
			}
		}

		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			if (fromUser){
				soundPlayer.seekTo(progress*soundPlayer.getDuration()/100);
			}
		}

		public void onStartTrackingTouch(SeekBar seekBar) {
			allowProgress = false;
		}

		public void onStopTrackingTouch(SeekBar seekBar) {	
			allowProgress = true;
		}
	}
}
