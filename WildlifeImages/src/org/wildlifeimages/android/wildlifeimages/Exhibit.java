package org.wildlifeimages.android.wildlifeimages;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/**
 * An Exhibit contains all of the content info for one map location.
 * 
 * @author Graham Wilkinson 
 * 	
 */
public class Exhibit implements Parcelable{

	public static final String TAG_AUTO = "_auto";

	public static final String TAG_PHOTOS = "Photos";

	private String name;

	private String currentTag = null;

	private Hashtable<String, String> contents = new Hashtable<String, String>();

	private ArrayList<String> tagList = new ArrayList<String>();

	private String next = null;

	private String previous = null;

	private int mapX = -1;

	private int mapY = -1;

	public static final Parcelable.Creator<Exhibit> CREATOR = new Parcelable.Creator<Exhibit>() {
		public Exhibit createFromParcel(Parcel in) {
			return new Exhibit(in); //TODO
		}

		public Exhibit[] newArray(int size) {
			return new Exhibit[size];
		}
	};

	private Exhibit(Parcel in) {
		Log.w(this.getClass().getName(), "Reading data");
		name = in.readString();
		currentTag = in.readString();
		next = in.readString();
		previous = in.readString();
		mapX = in.readInt();
		mapY = in.readInt();

		String[] tagArray = new String[in.readInt()];
		Log.w(this.getClass().getName(), "Reading tagArray");
		in.readStringArray(tagArray);

		for (int i=0; i<tagArray.length; i++){
			String contentArray = null;
			Log.w(this.getClass().getName(), "Reading contentArray");
			contentArray = in.readString();
			contents.put(tagArray[i], contentArray);
			tagList.add(tagArray[i]);
		}
	}

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

	public void setPhoto(String content) {
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

	public int describeContents() {
		return 0;
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

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(name);
		dest.writeString(currentTag);
		dest.writeString(next);
		dest.writeString(previous);
		dest.writeInt(mapX);
		dest.writeInt(mapY);

		dest.writeInt(tagList.size());
		dest.writeStringArray(tagList.toArray(new String[tagList.size()]));

		for(int i=0; i<tagList.size(); i++){
			String contentsArray = contents.get(tagList.get(i));
			dest.writeString(contentsArray);
		}
	}
}
