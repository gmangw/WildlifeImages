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
