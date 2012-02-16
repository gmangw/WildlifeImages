package org.wildlifeimages.tools.update;

import java.util.ArrayList;
import java.util.Hashtable;

public class ExhibitInfo {
	private static final String TAG_PHOTO = "Photos";

	public final String origName;
	public final int origXCoord;
	public final int origYCoord;
	public final String origPrevious;
	public final String origNext;

	private String name = "New Exhibit";
	private int xCoord = 0;
	private int yCoord = 0;
	private String previous = null;
	private String next = null;
	private Hashtable<String, String> contents = new Hashtable<String, String>();
	private Hashtable<String, String> origContents = new Hashtable<String, String>();
	private ArrayList<String> photos = new ArrayList<String>();
	private ArrayList<String> tags = new ArrayList<String>();

	public ExhibitInfo(String name, int x, int y, String previous, String next){
		this.name = name;
		this.origName = name;

		this.xCoord = x;
		this.origXCoord = x;

		this.yCoord = y;
		this.origYCoord = y;

		this.previous = previous;
		this.origPrevious = previous;

		this.next = next;
		this.origNext = next;
	}

	public String[] getPhotos(){
		return photos.toArray(new String[photos.size()]);
	}
	
	public String getContents(String tag){
		return contents.get(tag);
	}

	public String getOrigContents(String tag){
		return origContents.get(tag);
	}

	public String getTag(int index){
		return tags.get(index);
	}

	public int getTagCount(){
		return tags.size();
	}

	public void addContent(String tag, String data){
		if (contents.containsKey(tag) == false){
			tags.add(tag);
		}
		contents.put(tag, data);
	}
	
	public void addOrigContent(String tag, String data){
		if (contents.containsKey(tag) == false){
			tags.add(tag);
			origContents.put(tag, data);
		}
		contents.put(tag, data);
	}



	public void addPhoto(String url){
		if (photos.size() == 0){
			//tags.add(TAG_PHOTO); //TODO
		}
		photos.add(url);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getxCoord() {
		return xCoord;
	}

	public void setxCoord(int xCoord) {
		this.xCoord = xCoord;
	}

	public int getyCoord() {
		return yCoord;
	}

	public void setyCoord(int yCoord) {
		this.yCoord = yCoord;
	}

	public String getPrevious() {
		return previous;
	}

	public void setPrevious(String previous) {
		this.previous = previous;
	}

	public String getNext() {
		return next;
	}

	public void setNext(String next) {
		this.next = next;
	}
}