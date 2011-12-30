package org.wildlifeimages.android.wildlifeimages;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
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
	//private WebContentManager webManager = null;

	public ExhibitView(Context context, AttributeSet attrs) {
		super(context, attrs);
		htmlView = new WebView(context, attrs);
		htmlView.setVisibility(View.VISIBLE);

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

	public void loadData(String data, String mimeType, String encoding){
		htmlView.loadData(data, mimeType, encoding);
		htmlView.setVisibility(View.VISIBLE);
		picView.setVisibility(View.INVISIBLE);
	}
	
	/*public void setWebContentManager(WebContentManager m){
		webManager = m;
		picView.setWebContentManager(webManager);
	}*/

}
