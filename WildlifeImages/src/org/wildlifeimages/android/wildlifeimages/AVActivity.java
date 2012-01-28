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

public class AVActivity extends WireActivity implements OnCompletionListener{

	private MediaPlayer soundPlayer = null;
	
	private MediaThread updater;

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

	public static void start(Context context, String url) {
		Intent avIntent = new Intent(context, AVActivity.class);
		avIntent.putExtra("URL", url);
		context.startActivity(avIntent);
	}

	public void mediaPause(View v){
		if (soundPlayer.isPlaying()){
			soundPlayer.pause();
		}else{
			soundPlayer.start();
		}
	}

	public void mediaStop(View v){
		soundPlayer.stop();
		updater.cancel(true);
		finish();
	}
	
	public void onCompletion(MediaPlayer mp) {
		mediaStop(null);
	}
	
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

		@Override
		protected void onProgressUpdate(Integer... amount) {
			ProgressBar progress = (ProgressBar)findViewById(R.id.media_progress);
			if (progress != null){
				progress.setProgress(amount[0]);
			}
		}
	}
}
