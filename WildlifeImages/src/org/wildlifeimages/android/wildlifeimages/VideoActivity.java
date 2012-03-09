package org.wildlifeimages.android.wildlifeimages;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.VideoView;

public class VideoActivity extends WireActivity implements OnCompletionListener {

	private ControlFader thread = new ControlFader();

	@Override
	public void onCreate(Bundle inState){
		//TODO mediaController
		super.onCreate(inState);
		setContentView(R.layout.video_layout);

		VideoView videoView = (VideoView) findViewById(R.id.video_view);  
		videoView.setOnCompletionListener(this);

		int id = getIntent().getIntExtra("ID", -1);
		Uri pathToVideo = Uri.parse("android.resource://org.wildlifeimages.android.wildlifeimages/" + id);  
	
		videoView.setVideoURI(pathToVideo);  

		videoView.setMediaController(new MediaController(this));
		
		videoView.requestFocus();
		if (inState == null){
			Button b = (Button)findViewById(R.id.media_pause_button);
			b.setBackgroundResource(R.drawable.pause_button);
			videoView.start();
		}

		thread.execute(-1);
	}

	public static void start(Context context, int id) {
		Intent videoIntent = new Intent(context, VideoActivity.class);

		videoIntent.putExtra("ID", id);

		/* Start the activity. */
		context.startActivity(videoIntent);
	}

	@Override
	public void onResume(){
		super.onResume();

		Button b = (Button)findViewById(R.id.media_pause_button);
		VideoView videoView = (VideoView) findViewById(R.id.video_view);
		if (videoView.isPlaying()){
			b.setBackgroundResource(R.drawable.pause_button);
		}else{
			b.setBackgroundResource(R.drawable.play_button);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event){
		LinearLayout controls = (LinearLayout) findViewById(R.id.video_controls);  
		if (controls != null){
			return false;
		}
		if (controls.getVisibility() == View.INVISIBLE){
			Animation fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
			fadeInAnimation.setAnimationListener(new FadeInListener());
			controls.startAnimation(fadeInAnimation);
			return true;
		}else{
			thread.setCountdown(20);
			return true;
		}
	}

	public void mediaPause(View v){
		Button b = (Button)findViewById(R.id.media_pause_button);


		VideoView videoView = (VideoView) findViewById(R.id.video_view);
		if (videoView.isPlaying()){
			videoView.pause();
			thread.setCountdown(-1);
			b.setBackgroundResource(R.drawable.play_button);
		}else{
			videoView.start();
			thread.setCountdown(20);
			b.setBackgroundResource(R.drawable.pause_button);
		}
	}

	public void mediaStop(View v){
		thread.setCountdown(-1);
		VideoView videoView = (VideoView) findViewById(R.id.video_view);
		videoView.seekTo(videoView.getDuration());
		Button b = (Button)findViewById(R.id.media_pause_button);
		b.setBackgroundResource(R.drawable.play_button);
	}

	private void fadeOut(){
		LinearLayout controls = (LinearLayout) findViewById(R.id.video_controls);  
		Animation fadeOutAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_out);
		controls.startAnimation(fadeOutAnimation);
		fadeOutAnimation.setAnimationListener(new FadeOutListener());
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		
		thread.cancel(true);
	}

	private class FadeInListener implements AnimationListener{

		public void onAnimationEnd(Animation animation) {
		}

		public void onAnimationRepeat(Animation animation) {
		}

		public void onAnimationStart(Animation animation) {
			LinearLayout controls = (LinearLayout) findViewById(R.id.video_controls);  
			controls.setVisibility(View.VISIBLE);
		}

	}

	private class FadeOutListener implements AnimationListener{

		public void onAnimationEnd(Animation animation) {
			LinearLayout controls = (LinearLayout) findViewById(R.id.video_controls);  
			controls.setVisibility(View.INVISIBLE);
		}

		public void onAnimationRepeat(Animation animation) {
		}

		public void onAnimationStart(Animation animation) {
		}

	}

	public class ControlFader extends AsyncTask<Integer, Integer, Boolean>{

		private int countdown = -1;

		@Override
		protected Boolean doInBackground(Integer... arg0) {
			countdown = arg0[0];
			VideoView videoView = (VideoView) findViewById(R.id.video_view);
			while (isCancelled() == false){
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
				publishProgress(100*videoView.getCurrentPosition()/videoView.getDuration());
				if (countdown > 0){
					countdown--;
					if (countdown == 0){
						publishProgress(-1);
					}
				}
			}
			return true;
		}

		public void setCountdown(int time){
			countdown = time;
		}

		@Override
		protected void onPostExecute(Boolean result){

		}

		@Override
		protected void onCancelled(){

		}

		@Override
		protected void onProgressUpdate(Integer... amount) {
			for (int p : amount)
				if (p == -1){
					fadeOut();
				}else{
					SeekBar progress = (SeekBar)findViewById(R.id.media_progress);
					progress.setProgress(p);
				}
		}
	}

	public void onCompletion(MediaPlayer mp) {
		mp.seekTo(0); 
		Button b = (Button)findViewById(R.id.media_pause_button);
		b.setBackgroundResource(R.drawable.play_button);
	}
}
