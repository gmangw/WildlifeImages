package org.wildlifeimages.android.wildlifeimages;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * A collection of {@link Exhibit} instances.
 * 
 * @author Graham Wilkinson 
 * 	
 */
public class ExhibitList implements Parser.ExhibitInterface{

	private Hashtable<String, Exhibit> exhibitList = new Hashtable<String, Exhibit>();

	private ArrayList<String> keyList = new ArrayList<String>();

	Hashtable<String, ExhibitGroup> groupList = new Hashtable<String, ExhibitGroup>();

	private Exhibit current = null;

	private float zoomFactor = 0.75f;
	private float zoomMinimum = 1.00f;
	private float zoomExponent = 1.00f;
	private float[][] anchorPoints = { //TODO move this data somewhere else
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

			{0.50f, 0.00f, 1.90f},
			{0.50f, 0.25f, 2.50f}, 
			{0.50f, 0.50f, 1.30f},
			{0.50f, 0.75f, 1.30f},
			{0.50f, 1.00f, 1.60f},

			{0.75f, 0.00f, 1.90f},
			{0.75f, 0.25f, 2.60f}, 
			{0.75f, 0.50f, 1.60f},
			{0.75f, 0.75f, 1.50f},
			{0.75f, 1.00f, 1.40f},

			{1.00f, 0.00f, 1.90f},
			{1.00f, 0.25f, 1.80f},
			{1.00f, 0.50f, 1.70f},
			{1.00f, 0.75f, 1.60f},
			{1.00f, 1.00f, 1.60f},
	};
	
	public void addGroup(String groupName, String[] data, int x, int y){
		groupList.put(groupName, new ExhibitGroup(data, x, y));
	}
	
	public void addExhibit(String name, int xCoord, int yCoord, String next, String previous, Parser.ExhibitDataHolder data){
		Exhibit e = new Exhibit(name);
		e.setCoords(xCoord, yCoord);
		e.setNext(next);
		e.setPrevious(previous);
		for(int i=0; i<data.contentNameList.size(); i++){
			e.setContent(data.contentNameList.get(i), data.contentValueList.get(i));
		}
		for(String photo : data.photoList){
			e.addPhoto(photo);
		}
		for(int i=0; i<data.aliasList.size(); i++){
			e.addAlias(data.aliasList.get(i), data.aliasXList.get(i), data.aliasYList.get(i));
			exhibitList.put(data.aliasList.get(i), e);
		}
		exhibitList.put(e.getName(), e);
		keyList.add(e.getName());
	}

	public ExhibitList(XmlPullParser xmlBox) throws XmlPullParserException, IOException{
		new Parser(xmlBox, this);
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
}
