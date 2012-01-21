package org.wildlifeimages.android.wildlifeimages;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.AsyncTask;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

/**
 * A modified FrameLayout containing a {@link WebView} and a {@link MultiImageView}.
 * 
 * @author Graham Wilkinson 
 * 	
 */

public class ExhibitView extends FrameLayout implements DownloadListener{
	private WebView htmlView;
	private MultiImageView picView;
	private Context context = this.getContext();
	private ContentManager lastContentManager;

	public ExhibitView(Context context, AttributeSet attrs) {
		super(context, attrs);
		htmlView = new WebView(context, attrs);
		htmlView.setVisibility(View.VISIBLE);

		htmlView.getSettings().setJavaScriptEnabled(true);
		htmlView.getSettings().setPluginsEnabled(true);

		htmlView.setDownloadListener(this);

		picView = new MultiImageView(context, attrs);
		picView.setBackgroundColor(0xFF000000);
		picView.setVisibility(View.INVISIBLE);

		this.addView(htmlView);
		this.addView(picView);
	}

	public void loadUrl(String shortUrl, ContentManager contentManager){
		String[] urlList = new String[1];
		urlList[0] = shortUrl;
		loadUrlList(urlList, contentManager);
		lastContentManager = contentManager;
	}

	public void loadUrlList(String[] shortUrlList, ContentManager contentManager){
		String lower = shortUrlList[0].toLowerCase();
		String[] extensionList = this.getContext().getResources().getStringArray(R.array.image_extensions);
		lastContentManager = contentManager;
		for (int i=0; i<extensionList.length; i++){
			if (lower.endsWith(extensionList[i])){
				loadImageUrl(shortUrlList, contentManager);
				return;
			}
		}
		//Else
		loadHtmlUrl(shortUrlList[0], contentManager);
	}

	public void loadHtmlUrl(String htmlShortUrl, ContentManager contentManager){
		htmlView.loadUrl(contentManager.getBestUrl(htmlShortUrl));
		htmlView.setVisibility(View.VISIBLE);
		picView.setVisibility(View.INVISIBLE);
		lastContentManager = contentManager;
	}

	public void loadImageUrl(String[] imgShortUrl, ContentManager contentManager){
		picView.setImageBitmapList(imgShortUrl, contentManager);
		picView.setVisibility(View.VISIBLE);
		htmlView.setVisibility(View.INVISIBLE);
		lastContentManager = contentManager;
	}

	public void loadData(String data){
		htmlView.loadData(data, "text/html", null);
		htmlView.setVisibility(View.VISIBLE);
		picView.setVisibility(View.INVISIBLE);
	}

	public void onDownloadStart(String url, String userAgent,
			String contentDisposition, String mimetype, long contentLength) {
		Log.d(this.getClass().getName(), "Clicked link with type " + mimetype); //TODO
		url = url.replaceAll(ContentManager.ASSET_PREFIX, "");
		Log.w(this.getClass().getName(), url);
		if (lastContentManager != null){
			AVManager avManager = new AVManager();
			MediaPlayer soundPlayer = avManager.playSound(url, lastContentManager, context.getAssets());
			new MediaThread(this.getContext()).execute(soundPlayer);
		}
	}

	public class MediaThread extends AsyncTask<MediaPlayer, Integer, Integer> implements OnClickListener, OnCompletionListener{

		private Context context;
		private ProgressDialog progressDialog;
		private MediaPlayer player;
		private boolean finished = false;

		public MediaThread(Context c){
			context = c;
		}

		@Override
		protected void onPreExecute(){
			progressDialog = new ProgressDialog(context);
			progressDialog.setMessage("Playing Audio");
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progressDialog.setCancelable(false);;
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			LinearLayout buttons = (LinearLayout)inflater.inflate(R.layout.media_progress_layout, null);
			for (int i=0; i<buttons.getChildCount(); i++){
				buttons.getChildAt(i).setOnClickListener(this);
			}
			progressDialog.setCustomTitle(buttons);
			progressDialog.show();
		}

		@Override
		protected void onPostExecute(Integer i){
			progressDialog.dismiss();
		}

		@Override
		protected Integer doInBackground(MediaPlayer... params) {
			player = params[0];
			player.setOnCompletionListener(this);
			while(finished == false){
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
			progressDialog.setProgress(amount[0]);
		}

		public void onCompletion(MediaPlayer mp) {
			finished = true;
		}

		public void onClick(View v) {
			if (v.getId() == R.id.media_pause_button){
				if (player.isPlaying()){
					player.pause();
				}else {
					player.start();
				}
			} else if (v.getId() == R.id.media_stop_button){
				player.stop();
			}
		}
	}
}
