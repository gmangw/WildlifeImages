package org.wildlifeimages.android.wildlifeimages;

import java.util.Enumeration;
import java.util.Hashtable;

public class Exhibit {
	
	public static final String INTRO_TAG = "Introduction";
	
	public static final String MAP_TAG = "Map"; //TODO
	
	public static final String AUTO_TAG = "_auto";
	
	private String name;
	
	private String currentTag = Exhibit.INTRO_TAG;
	
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

	public Enumeration<String> getTags(){
		return contents.keys();
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

	public String getCurrentTag() {
		return currentTag;
	}

	public void setCurrentTag(String contentTag) {
		currentTag = contentTag;
	}
}
