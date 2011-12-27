package org.wildlifeimages.android.wildlifeimages;

import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
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
	WebView htmlView;
	MultiImageView picView;


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

	public void loadUrl(String url){
		String lower = url.toLowerCase();
		if (lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".png")){
			loadImageUrl(url);
		}else{
			loadHtmlUrl(url);
		}
	}

	public void loadHtmlUrl(String htmlUrl){
		htmlView.loadUrl(htmlUrl);
		htmlView.setVisibility(View.VISIBLE);
		picView.setVisibility(View.INVISIBLE);
	}

	public void loadImageUrl(String imgUrl){
		String[] bml = imgUrl.split(",");
		//bml[0] = imgUrl;
		//bml[1] = "file:///android_asset/ExhibitContents/bobcat.jpg";
		picView.setImageBitmapList(bml);
		picView.setVisibility(View.VISIBLE);
		htmlView.setVisibility(View.INVISIBLE);
	}

}
