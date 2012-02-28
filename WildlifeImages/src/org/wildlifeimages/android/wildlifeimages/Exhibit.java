package org.wildlifeimages.android.wildlifeimages;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

/**
 * An Exhibit contains all of the content info for one map location.
 * 
 * @author Graham Wilkinson 
 * 	
 */
public class Exhibit{

	public static final String TAG_AUTO = "_auto";
	public static final String TAG_PHOTOS = "Photos";

	private String name;
	private String currentTag = null;
	private Hashtable<String, String> contents = new Hashtable<String, String>();
	private ArrayList<String> tagList = new ArrayList<String>();
	private ArrayList<Alias> aliasList = new ArrayList<Alias>();
	private String next = null;
	private String previous = null;
	private int mapX = -1;
	private int mapY = -1;

	public Exhibit(String name){
		this.name = name;
	}

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
			return contents.get(tagList.get(0));
		}else{
			return c;
		}
	}

	public boolean hasContent(String contentTag) {
		String c = contents.get(contentTag);
		if (c == null){
			return false;
		} else {
			return true;
		}
	}

	public void setContent(String contentTag, String content) {
		if (false == contents.containsKey(contentTag)){
			tagList.add(contentTag);
		}
		contents.put(contentTag, content);
		if (currentTag == null){
			currentTag = contentTag;
		}
	}

	public void addPhoto(String content) {
		if (contents.containsKey(Exhibit.TAG_PHOTOS)){
			String previousPhoto = getContent(Exhibit.TAG_PHOTOS);
			setContent(Exhibit.TAG_PHOTOS, previousPhoto + "," + content);
		}else{
			setContent(Exhibit.TAG_PHOTOS, content);
		}
	}

	public String getName() {
		return name;
	}

	public void setCoords(int x, int y){
		mapX = x;
		mapY = y;
	}

	public int getMinDistance(int x, int y){
		float distance = Common.distance(mapX, mapY, x, y);
		for (Alias a : aliasList){
			float aliasDistance = Common.distance(a.xPos, a.yPos, x, y);
			if (aliasDistance < distance){
				distance = aliasDistance;
			}
		}
		return (int)distance;
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


	@Override
	public boolean equals(Object o){
		if (o.getClass() == Exhibit.class){
			if (((Exhibit)o).getName().equals(name)){
				return true;
			}else{
				return false;
			}
		}else{
			return false;
		}
	}
	
	@Override
	public int hashCode(){
		return name.hashCode();
	}

	public void addAlias(String aliasName, int xPos, int yPos) {
		aliasList.add(new Alias(aliasName, xPos, yPos));
	}
	
	public Alias[] getAliases(){
		return aliasList.toArray(new Alias[aliasList.size()]);
	}
	
	public class Alias{
		public final String name;
		public final int xPos;
		public final int yPos;
		
		public Alias(String aliasName, int aliasX, int aliasY){
			name = aliasName;
			xPos = aliasX;
			yPos = aliasY;
		}
	}
	
	public String[] getPhotos(){
		String photos = contents.get(Exhibit.TAG_PHOTOS);
		if (photos != null){
			return photos.split(",");
		}else{
			return new String[0];
		}
	}
	
	public int getTagCount(){
		return tagList.size();
	}
	
	public String getTag(int index){
		return tagList.get(index);
	}
}
