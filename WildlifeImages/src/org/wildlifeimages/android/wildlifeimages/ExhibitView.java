package org.wildlifeimages.android.wildlifeimages;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

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

	public ExhibitView(final Context context, AttributeSet attrs) {
		super(context, attrs);
		htmlView = new WebView(context, attrs);
		htmlView.setVisibility(View.VISIBLE);

		htmlView.getSettings().setJavaScriptEnabled(true);
		htmlView.getSettings().setPluginsEnabled(true);
		htmlView.getSettings().setSupportZoom(true);
		htmlView.getSettings().setBuiltInZoomControls(false);
		htmlView.getSettings().setDefaultZoom(WebSettings.ZoomDensity.CLOSE);
		htmlView.getSettings().setDefaultFontSize(getResources().getInteger(R.integer.content_text_size));

		htmlView.setDownloadListener(this);

		picView = new MultiImageView(context, attrs);
		picView.setBackgroundColor(0xFF000000);
		picView.setVisibility(View.INVISIBLE);

		FrameLayout.LayoutParams paramsLeft = this.generateDefaultLayoutParams();
		paramsLeft.gravity = Gravity.LEFT + Gravity.CENTER_VERTICAL;
		paramsLeft.width = FrameLayout.LayoutParams.WRAP_CONTENT;
		paramsLeft.height = FrameLayout.LayoutParams.WRAP_CONTENT;

		FrameLayout.LayoutParams paramsRight = this.generateDefaultLayoutParams();
		paramsRight.gravity = Gravity.RIGHT + Gravity.CENTER_VERTICAL;
		paramsRight.width = FrameLayout.LayoutParams.WRAP_CONTENT;
		paramsRight.height = FrameLayout.LayoutParams.WRAP_CONTENT;

		this.addView(htmlView);
		this.addView(picView);
	}

	public void loadHtmlUrl(String htmlShortUrl){
		htmlView.loadUrl(ContentManager.getBestUrl(htmlShortUrl));
		htmlView.setVisibility(View.VISIBLE);
		picView.setVisibility(View.INVISIBLE);
	}

	public void loadPhotoList(ExhibitPhoto[] imgShortUrl){
		picView.setImageBitmapList(imgShortUrl);
		picView.setVisibility(View.VISIBLE);;
		htmlView.setVisibility(View.INVISIBLE);
	}

	public void loadData(String data){
		htmlView.loadData(data, "text/html", null);
		htmlView.setVisibility(View.VISIBLE);
		picView.setVisibility(View.INVISIBLE);
	}

	public void onDownloadStart(String clickedUrl, String userAgent, String contentDisposition, String mimetype, long contentLength) {
		Log.d(this.getClass().getName(), "Clicked link with type " + mimetype);
		String url = clickedUrl.replaceAll(ContentManager.ASSET_PREFIX, "");
		Log.w(this.getClass().getName(), "Playing " + url);

		ExhibitPhoto[] shortUrls = ContentManager.getExhibitList().getCurrent().getPhotos();
		String thumbUrl = "";
		if (shortUrls.length > 0){
			thumbUrl = shortUrls[0].shortUrl;
		}
		AudioActivity.start(context, url, thumbUrl);
	}

	public void clear() {
		htmlView.loadUrl("");
		picView.setImageBitmap(null);
	}

	public boolean isCleared() {
		if (htmlView != null && picView != null){
			if(picView.isEmpty()){
				return true;
			}
		}
		return false;
	}
}
