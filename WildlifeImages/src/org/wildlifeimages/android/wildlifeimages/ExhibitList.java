package org.wildlifeimages.android.wildlifeimages;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/**
 * A collection of {@link Exhibit} instances.
 * 
 * @author Graham Wilkinson 
 * 	
 */
public class ExhibitList implements Parcelable{

	private Hashtable<String, Exhibit> exhibitList = new Hashtable<String, Exhibit>();

	private ArrayList<String> keyList = new ArrayList<String>();

	private Exhibit current = null;

	private float zoomFactor = 0.75f;
	private float zoomMinimum = 1.00f;
	private float zoomExponent = 1.00f;
	private float[][] anchorPoints = { 
		{0.00f, 0.00f, 1.30f},
		{0.00f, 0.25f, 2.50f}, 
		{0.00f, 0.50f, 1.00f},
		{0.00f, 0.75f, 1.30f},
		{0.00f, 1.00f, 1.30f},

		{0.25f, 0.00f, 1.30f},
		{0.25f, 0.25f, 2.50f}, 
		{0.25f, 0.50f, 1.00f},
		{0.25f, 0.75f, 1.30f},
		{0.25f, 1.00f, 1.30f}, 

		{0.50f, 0.00f, 1.30f},
		{0.50f, 0.25f, 2.50f}, 
		{0.50f, 0.50f, 1.00f},
		{0.50f, 0.75f, 1.30f},
		{0.50f, 1.00f, 1.30f},

		{0.75f, 0.00f, 1.30f},
		{0.75f, 0.25f, 2.50f}, 
		{0.75f, 0.50f, 1.00f},
		{0.75f, 0.75f, 1.30f},
		{0.75f, 1.00f, 1.30f},

		{1.00f, 0.00f, 1.30f},
		{1.00f, 0.25f, 1.15f},
		{1.00f, 0.50f, 1.00f},
		{1.00f, 0.75f, 1.00f},
		{1.00f, 1.00f, 1.00f},
	};

	public static final Parcelable.Creator<ExhibitList> CREATOR = new Parcelable.Creator<ExhibitList>() {
		public ExhibitList createFromParcel(Parcel in) {
			return new ExhibitList(in);
		}

		public ExhibitList[] newArray(int size) {
			return new ExhibitList[size];
		}
	};

	private ExhibitList(Parcel in) {
		Log.w(this.getClass().getName(), "Reading current");
		current = in.readParcelable(Exhibit.class.getClassLoader());
		String[] keyArray = new String[in.readInt()];
		Log.w(this.getClass().getName(), "Reading keys");
		in.readStringArray(keyArray);
		Log.w(this.getClass().getName(), "Reading values");
		Parcelable[] values = in.readParcelableArray(Exhibit.class.getClassLoader());
		for(int i=0; i<values.length; i++){
			exhibitList.put(keyArray[i], (Exhibit)values[i]);
			keyList.add(keyArray[i]);
		}
	}

	public ExhibitList(XmlPullParser xmlBox) throws XmlPullParserException, IOException{

		int eventType;

		eventType = xmlBox.getEventType();

		Exhibit e = null;

		while (eventType != XmlPullParser.END_DOCUMENT) {
			if(eventType == XmlPullParser.START_TAG) {
				if (xmlBox.getName().equalsIgnoreCase("exhibit")){
					String name = xmlBox.getAttributeValue(null, "name"); 
					String introduction = xmlBox.getAttributeValue(null, "intro");
					int xCoord = Integer.decode(xmlBox.getAttributeValue(null, "xpos"));
					int yCoord = Integer.decode(xmlBox.getAttributeValue(null, "ypos"));
					String previous = xmlBox.getAttributeValue(null, "previous");
					String next = xmlBox.getAttributeValue(null, "next");
					//TODO null handling
					e = new Exhibit(name, introduction);
					e.setCoords(xCoord, yCoord);
					e.setNext(next);
					e.setPrevious(previous);
					exhibitList.put(e.getName(), e);
					keyList.add(e.getName());
				}else if (xmlBox.getName().equalsIgnoreCase("content")){
					String urlList = xmlBox.getAttributeValue(null, "page");
					String[] content = urlList.split(",");
					e.setContent(xmlBox.getAttributeValue(null, "tag"), content);
				}
			}
			eventType = xmlBox.next();
		}
	}

	public Iterator<String> keys(){
		return keyList.iterator();
	}

	public boolean containsKey(String potential_key) {
		return exhibitList.containsKey(potential_key);
	}

	public Exhibit get(String potential_key) {
		if (null == potential_key){
			return null;
		}else{
			return exhibitList.get(potential_key);
		}
	}

	public void setCurrent(Exhibit current, String contentTag) {
		if (current != null){
			this.current = current;
			if (Exhibit.TAG_AUTO != contentTag){
				current.setCurrentTag(contentTag);
			}
		}
	}

	public void setCurrent(String name, String contentTag) {
		Exhibit e = this.get(name);
		setCurrent(e, contentTag);
	}

	public Exhibit getCurrent() {
		if (current == null){
			return get(keyList.get(0));
		}else{
			return current;
		}
	}

	public Exhibit findNearest(int percentHoriz, int percentVert) {
		Enumeration<Exhibit> list = exhibitList.elements();

		int minDistance = 100000;
		Exhibit closest = null;

		while(list.hasMoreElements()){
			Exhibit e = list.nextElement();
			int d = e.getDistance(percentHoriz, percentVert);
			if(d < minDistance){
				minDistance = d;
				closest = e;
			}
		}
		return closest;
	}

	public Exhibit getNext() {
		if (current == null){
			return get(keyList.get(0));
		}else{
			return get(current.getNext());
		}
	}

	public Exhibit getPrevious() {
		if (current == null){
			return get(keyList.get(0));
		}else{
			return get(current.getPrevious());
		}
	}

	public int getCount() {
		return exhibitList.size();
	}

	public Exhibit getExhibitAt(int position) {
		return get(keyList.get(position));
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel out, int flags) {

		Exhibit[] valueArray = new Exhibit[keyList.size()];
		for( int i=0; i<valueArray.length; i++){
			valueArray[i] = get(keyList.get(i));
		}

		out.writeParcelable(current, 0);
		out.writeInt(valueArray.length);
		out.writeStringArray(keyList.toArray(new String[keyList.size()]));
		out.writeParcelableArray(valueArray, 0);

	}

	public float[][] getAnchorPoints() {
		return anchorPoints;
	}

	public float getScale(float xFraction, float yFraction) {
		float newScale = 0.0f;

		for (int i=0; i<anchorPoints.length; i++){
			float distance = (float)Math.pow(Common.distance(xFraction, yFraction, anchorPoints[i][0], anchorPoints[i][1]), zoomExponent);
			float zoomCandidate = (anchorPoints[i][2]+zoomMinimum)-(zoomMinimum + (anchorPoints[i][2]-zoomMinimum)*(float)Common.smoothStep(0f, 0.75f, distance));
			newScale = Math.max(newScale, zoomCandidate);
		}

		return zoomFactor * newScale;
	}

	public void setZoomFactor(float zoomFactor) {
		this.zoomFactor = zoomFactor;
	}

	public void setZoomMinimum(float zoomMinimum) {
		this.zoomMinimum = zoomMinimum;
	}

	public void setZoomExponent(float zoomExponent) {
		this.zoomExponent = zoomExponent;
	}
}
