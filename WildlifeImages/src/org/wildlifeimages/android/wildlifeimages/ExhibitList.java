package org.wildlifeimages.android.wildlifeimages;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;

/**
 * A collection of {@link Exhibit} instances.
 * 
 * @author Graham Wilkinson 
 * 	
 */
public class ExhibitList{

	private Hashtable<String, Exhibit> exhibitList = new Hashtable<String, Exhibit>();

	private ArrayList<String> keyList = new ArrayList<String>();

	Hashtable<String, ExhibitGroup> groupList = new Hashtable<String, ExhibitGroup>();

	private Exhibit current = null;

	private float zoomFactor = 0.75f;
	private float zoomMinimum = 1.00f;
	private float zoomExponent = 1.00f;
	private float[][] anchorPoints = { 
			{0.00f, 0.00f, 1.90f},
			{0.00f, 0.25f, 2.50f}, 
			{0.00f, 0.50f, 1.80f},
			{0.00f, 0.75f, 1.70f},
			{0.00f, 1.00f, 1.60f},

			{0.25f, 0.00f, 2.00f},
			{0.25f, 0.25f, 3.00f}, 
			{0.25f, 0.50f, 1.80f},
			{0.25f, 0.75f, 1.70f},
			{0.25f, 1.00f, 1.60f}, 

			{0.50f, 0.00f, 1.60f},
			{0.50f, 0.25f, 2.50f}, 
			{0.50f, 0.50f, 1.30f},
			{0.50f, 0.75f, 1.30f},
			{0.50f, 1.00f, 1.60f},

			{0.75f, 0.00f, 1.60f},
			{0.75f, 0.25f, 2.60f}, 
			{0.75f, 0.50f, 1.60f},
			{0.75f, 0.75f, 1.50f},
			{0.75f, 1.00f, 1.40f},

			{1.00f, 0.00f, 1.60f},
			{1.00f, 0.25f, 1.60f},
			{1.00f, 0.50f, 1.60f},
			{1.00f, 0.75f, 1.60f},
			{1.00f, 1.00f, 1.60f},
	};

	private void readExhibitTag(XmlPullParser xmlBox, Exhibit e) throws XmlPullParserException, IOException{
		if (xmlBox.getName().equalsIgnoreCase("content")){
			String url = xmlBox.getAttributeValue(null, "page");
			e.setContent(xmlBox.getAttributeValue(null, "tag"), url);
		}else if (xmlBox.getName().equalsIgnoreCase("photo")){
			String url = xmlBox.getAttributeValue(null, "page");
			e.addPhoto(url);
		}else if (xmlBox.getName().equalsIgnoreCase("alias")){
			String tmp = xmlBox.getAttributeValue(null, "xpos");
			int xAlias = -1;
			int yAlias = -1;
			if (tmp != null){
				xAlias = Integer.decode(tmp);
			}
			tmp = xmlBox.getAttributeValue(null, "ypos");
			if (tmp != null){
				yAlias = Integer.decode(tmp);
			}
			String aliasName = xmlBox.getAttributeValue(null, "name");
			e.addAlias(aliasName, xAlias, yAlias);
			exhibitList.put(aliasName, e);
		}
	}

	private void readGroupMember(XmlPullParser xmlBox, ArrayList<String> members){
		if (xmlBox.getName().equalsIgnoreCase("member")){
			String name = xmlBox.getAttributeValue(null, "exhibit");
			members.add(name);
		}
	}
	
	private void readGroup(XmlPullParser xmlBox) throws XmlPullParserException, IOException{
		int eventType;
		String groupName = xmlBox.getAttributeValue(null, "name");//TODO error check
		ArrayList<String> members = new ArrayList<String>();
		String tmp = xmlBox.getAttributeValue(null, "xpos");
		int xCoord = -1;
		int yCoord = -1;
		if (tmp != null){
			xCoord = Integer.decode(tmp);
		}
		tmp = xmlBox.getAttributeValue(null, "ypos");
		if (tmp != null){
			yCoord = Integer.decode(tmp);
		}		
		
		eventType = xmlBox.getEventType();
		while (eventType != XmlPullParser.END_DOCUMENT) {
			if(eventType == XmlPullParser.START_TAG) {
				readGroupMember(xmlBox, members);
			}else if(eventType == XmlPullParser.END_TAG){
				if (xmlBox.getName().equalsIgnoreCase("group")){
					break;
				}
			}
			eventType = xmlBox.next();
		}
		addGroup(groupName, members.toArray(new String[0]), xCoord, yCoord);
	}
	
	private void readExhibit(XmlPullParser xmlBox) throws XmlPullParserException, IOException{
		int eventType;
		Exhibit e = null;
		String name = xmlBox.getAttributeValue(null, "name");//TODO error check

		String tmp = xmlBox.getAttributeValue(null, "xpos");
		int xCoord = -1;
		int yCoord = -1;
		if (tmp != null){
			xCoord = Integer.decode(tmp);
		}
		tmp = xmlBox.getAttributeValue(null, "ypos");
		if (tmp != null){
			yCoord = Integer.decode(tmp);
		}
		String previous = xmlBox.getAttributeValue(null, "previous");
		String next = xmlBox.getAttributeValue(null, "next");
		//TODO null handling
		e = new Exhibit(name);
		e.setCoords(xCoord, yCoord);
		e.setNext(next);
		e.setPrevious(previous);
		exhibitList.put(e.getName(), e);
		keyList.add(e.getName());

		eventType = xmlBox.getEventType();
		while (eventType != XmlPullParser.END_DOCUMENT) {
			if(eventType == XmlPullParser.START_TAG) {
				readExhibitTag(xmlBox, e);
			}else if(eventType == XmlPullParser.END_TAG){
				if (xmlBox.getName().equalsIgnoreCase("exhibit")){
					break;
				}
			}
			eventType = xmlBox.next();
		}
	}

	public ExhibitList(XmlPullParser xmlBox) throws XmlPullParserException, IOException{

		int eventType;

		eventType = xmlBox.getEventType();

		while (eventType != XmlPullParser.END_DOCUMENT) {
			if(eventType == XmlPullParser.START_TAG) {
				if (xmlBox.getName().equalsIgnoreCase("exhibit")){
					readExhibit(xmlBox);
				}else if(xmlBox.getName().equalsIgnoreCase("group")){
					readGroup(xmlBox);
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
		return keyList.size();
	}

	public Exhibit getExhibitAt(int position) {
		return get(keyList.get(position));
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

		//Log.i(this.getClass().getName(), ""+newScale);
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

	public String[] getGroupNames(){
		return groupList.keySet().toArray(new String[0]);
	}

	public void addGroup(String groupName, String[] data, int x, int y){
		groupList.put(groupName, new ExhibitGroup(data, x, y));
	}

	public int getGroupX(String groupName){
		if (groupList.containsKey(groupName)){
			return groupList.get(groupName).xPos;
		}else{
			return -1;
		}
	}

	public int getGroupY(String groupName){
		if (groupList.containsKey(groupName)){
			return groupList.get(groupName).yPos;
		}else{
			return -1;
		}
	}

	public String[] getGroup(String groupName) {
		if (groupList.containsKey(groupName)){
			return groupList.get(groupName).exhibits;
		}else{
			return new String[0];
		}
	}

	public class ExhibitGroup{
		public final String[] exhibits;
		public final int xPos;
		public final int yPos;

		public ExhibitGroup(String[] list, int x, int y){
			exhibits = list;
			xPos = x;
			yPos = y;
		}
	}
}
