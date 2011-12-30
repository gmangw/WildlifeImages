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
	private WebContentManager webManager = null;

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

	public void loadShortUrl(String shortUrl){
		//TODO
	}
	
	public void loadUrl(String url){
		String[] urlList = new String[1];
		urlList[0] = url;
		loadUrlList(urlList);
	}
	
	public void loadUrlList(String[] url){
		String lower = url[0].toLowerCase();
		String[] extensionList = this.getContext().getResources().getStringArray(R.array.image_extensions);
		for (int i=0; i<extensionList.length; i++){
			if (lower.endsWith(extensionList[i])){
				loadImageUrl(url);
				return;
			}
		}
		//Else
		loadHtmlUrl(url[0]);
	}

	public void loadHtmlUrl(String htmlUrl){
		htmlView.loadUrl(htmlUrl);
		htmlView.setVisibility(View.VISIBLE);
		picView.setVisibility(View.INVISIBLE);
	}

	/* Need full urls... */
	public void loadImageUrl(String[] imgUrl){
		picView.setImageBitmapList(imgUrl);
		picView.setVisibility(View.VISIBLE);
		htmlView.setVisibility(View.INVISIBLE);
	}

	public void loadData(String data, String mimeType, String encoding){
		htmlView.loadData(data, mimeType, encoding);
		htmlView.setVisibility(View.VISIBLE);
		picView.setVisibility(View.INVISIBLE);
	}
	
	public void setWebContentManager(WebContentManager m){
		webManager = m;
		picView.setWebContentManager(webManager);
	}

}
