package org.wildlifeimages.android.wildlifeimages;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.VideoView;

public class VideoActivity extends WireActivity {

	private ControlFader thread = new ControlFader();

	@Override
	public void onCreate(Bundle inState){
		super.onCreate(inState);
		setContentView(R.layout.video_layout);

		VideoView videoView = (VideoView) findViewById(R.id.video_view);  

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
	public void onDestroy(){
		super.onDestroy();

		thread.cancel(true);
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
			for (int p : amount){
				SeekBar progress = (SeekBar)findViewById(R.id.media_progress);
				progress.setProgress(p);
			}
		}

		public void onCompletion(MediaPlayer mp) {
			mp.seekTo(0); 
			Button b = (Button)findViewById(R.id.media_pause_button);
			b.setBackgroundResource(R.drawable.play_button);
		}
	}
}