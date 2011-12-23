package org.wildlifeimages.android.wildlifeimages;

import java.util.Hashtable;

public class Exhibit {
	
	public static final String INTRO_TAG = "Introduction";
	
	public static final String HISTORY_TAG = "History";
	
	public static final String PHOTOS_TAG = "Photos";
	
	public static final String VIDEOS_TAG = "Videos";
	
	public static final String FUNFACTS_TAG = "Fun Facts";
	
	public static final String DIET_TAG = "Diet";
	
	public static final String MAP_TAG = "Map";
	
	private String name;
	
	private Hashtable<String, String> contents = new Hashtable<String, String>();
	
	private Exhibit next = null;
	
	private Exhibit previous = null;
	
	private int mapX = -1;
	
	private int mapY = -1;
	
	public Exhibit getNext() {
		return next;
	}

	public void setNext(Exhibit next) {
		this.next = next;
		if (next != null){
			next.setPrevious(this);
		}
	}
	
	public Exhibit getPrevious() {
		return previous;
	}

	private void setPrevious(Exhibit previous) {
		this.previous = previous;
	}

	public String getContent(String contentTag) {
		String c = contents.get(contentTag);
		if (c == null){
			return "";
		}else{
			return c;
		}
	}

	public void setContent(String contentTag, String content) {
		contents.put(contentTag, content);
	}
	
	public Exhibit(String name, String intro){
		this.name = name;
		contents.put(Exhibit.INTRO_TAG, intro);
	}

	public String getName() {
		return name;
	}
	
	public void setCoords(int x, int y){
		mapX = x;
		mapY = y;
	}
	
	public int getDistance(int x, int y){
		return (int)Math.sqrt(Math.pow(mapX - x, 2) + Math.pow(mapY - y, 2));
	}
	
	public int getX(){
		return mapX;
	}
	
	public int getY(){
		return mapY;
	}
}
