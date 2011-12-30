package org.wildlifeimages.android.wildlifeimages;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Stack;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;
import android.webkit.WebView;

/**
 * A collection of {@link Exhibit} instances.
 * 
 * @author Graham Wilkinson 
 * 	
 */
public class ExhibitList {

	private Hashtable<String, Exhibit> exhibitList = new Hashtable<String, Exhibit>();

	private ArrayList<String> keyList = new ArrayList<String>();

	private Exhibit current = null;

	private XmlPullParser xmlBox;

	public ExhibitList(XmlPullParser parser) throws XmlPullParserException, IOException{
		xmlBox = parser;

		int eventType;

		eventType = xmlBox.getEventType();

		Exhibit e = null;

		while (eventType != XmlPullParser.END_DOCUMENT) {

			if(eventType == XmlPullParser.START_DOCUMENT) {

			} else if(eventType == XmlPullParser.START_TAG) {
				if (xmlBox.getName().equalsIgnoreCase("exhibit")){
					String name = xmlBox.getAttributeValue(null, "name");
					String introduction = xmlBox.getAttributeValue(null, "intro");
					int xCoord = Integer.decode(xmlBox.getAttributeValue(null, "xpos"));
					int yCoord = Integer.decode(xmlBox.getAttributeValue(null, "ypos"));
					String previous = xmlBox.getAttributeValue(null, "previous");
					String next = xmlBox.getAttributeValue(null, "next");

					e = new Exhibit(name, introduction);
					e.setCoords(xCoord, yCoord);
					e.setNext(next);
					e.setPrevious(previous);
					exhibitList.put(e.getName(), e);
					keyList.add(e.getName());
				}else if (xmlBox.getName().equalsIgnoreCase("content")){
					e.setContent(xmlBox.getAttributeValue(null, "tag"), xmlBox.getAttributeValue(null, "page"));
				}
			} else if(eventType == XmlPullParser.END_TAG) {

			} else if(eventType == XmlPullParser.TEXT) {
				//xmlBox.getText();
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
		return exhibitList.get(potential_key);
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
}
