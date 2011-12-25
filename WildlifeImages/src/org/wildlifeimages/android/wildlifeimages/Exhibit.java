package org.wildlifeimages.android.wildlifeimages;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

public class Exhibit {
	
	public static final String INTRO_TAG = "Introduction";
	
	public static final String MAP_TAG = "Map"; //TODO
	
	public static final String AUTO_TAG = "_auto";
	
	private String name;
	
	private String currentTag = Exhibit.INTRO_TAG;
	
	private Hashtable<String, String> contents = new Hashtable<String, String>();
	
	private ArrayList<String> tagList = new ArrayList<String>();
	
	private String next = null;
	
	private String previous = null;
	
	private int mapX = -1;
	
	private int mapY = -1;
	
	public String getNext() {
		return this.next;
	}

	public void setNext(String nextName) {
		this.next = nextName;
	}
	
	public String getPrevious() {
		return previous;
	}

	public void setPrevious(String previous) {
		this.previous = previous;
	}

	public Iterator<String> getTags(){
		return tagList.iterator();
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
		tagList.add(contentTag);
		contents.put(contentTag, content);
	}
	
	public Exhibit(String name, String intro){
		this.name = name;
		tagList.add(Exhibit.INTRO_TAG);
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
