package org.wildlifeimages.android.wildlifeimages;

public class ExhibitPhoto {
	public final String shortUrl;
	private String caption = null;
	
	public ExhibitPhoto(String shortUrl, String caption){
		this.shortUrl = shortUrl;
		this.caption = caption;
	}
	
	public void setCaption(String caption){
		this.caption = caption;
	}
	
	public String getCaption(){
		return caption;
	}
}
