package org.wildlifeimages.android.wildlifeimages;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.FrameLayout;

/**
 * A modified FrameLayout containing a {@link WebView} and a {@link MultiImageView}.
 * 
 * @author Graham Wilkinson 
 * 	
 */

public class ExhibitView extends FrameLayout{
	private WebView htmlView;
	private MultiImageView picView;
	private Context context = this.getContext();

	public ExhibitView(Context context, AttributeSet attrs) {
		super(context, attrs);
		htmlView = new WebView(context, attrs);
		htmlView.setVisibility(View.VISIBLE);

		htmlView.getSettings().setJavaScriptEnabled(true);
		htmlView.getSettings().setPluginsEnabled(true);

		htmlView.setDownloadListener(new SoundLinkListener());

		picView = new MultiImageView(context, attrs);
		picView.setBackgroundColor(0xFF000000);
		picView.setVisibility(View.INVISIBLE);

		this.addView(htmlView);
		this.addView(picView);
	}

	public void loadUrl(String shortUrl, WebContentManager webManager){
		String[] urlList = new String[1];
		urlList[0] = shortUrl;
		loadUrlList(urlList, webManager);
	}

	public void loadUrlList(String[] shortUrlList, WebContentManager webManager){
		String lower = shortUrlList[0].toLowerCase();
		String[] extensionList = this.getContext().getResources().getStringArray(R.array.image_extensions);
		for (int i=0; i<extensionList.length; i++){
			if (lower.endsWith(extensionList[i])){
				loadImageUrl(shortUrlList, webManager);
				return;
			}
		}
		//Else
		loadHtmlUrl(shortUrlList[0], webManager);
	}

	public void loadHtmlUrl(String htmlShortUrl, WebContentManager webManager){
		htmlView.loadUrl(webManager.getBestUrl(htmlShortUrl));
		htmlView.setVisibility(View.VISIBLE);
		picView.setVisibility(View.INVISIBLE);
	}

	public void loadImageUrl(String[] imgShortUrl, WebContentManager webManager){
		picView.setImageBitmapList(imgShortUrl, webManager);
		picView.setVisibility(View.VISIBLE);
		htmlView.setVisibility(View.INVISIBLE);
	}

	public void loadData(String data){
		htmlView.loadData(data, "text/html", null);
		htmlView.setVisibility(View.VISIBLE);
		picView.setVisibility(View.INVISIBLE);
	}

	private class SoundLinkListener implements DownloadListener{
		public void onDownloadStart(String url, String userAgent,
				String contentDisposition, String mimetype, long contentLength) {
			Log.w(this.getClass().getName(), "Got a download request for " + url);

			MediaPlayer soundPlayer = new MediaPlayer();
			try{
				AssetFileDescriptor fd = context.getAssets().openFd("ExhibitContents/test.mp3");
				soundPlayer.setDataSource(fd.getFileDescriptor(),fd.getStartOffset(),fd.getLength());
				soundPlayer.prepare();
				soundPlayer.start();
			} catch (IOException e){
				Log.e(this.getClass().getName(), Log.getStackTraceString(e));
			}
		}
	}
}
