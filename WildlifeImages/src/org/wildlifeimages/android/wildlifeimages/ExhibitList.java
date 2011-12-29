package org.wildlifeimages.android.wildlifeimages;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

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
	
	private final String EXHIBITS[][] = {
		{"Alpha", "56", "59", "", "Bravo",
			"Introduction", "ExhibitContents/alphaIntro.html", 
			"History", "ExhibitContents/alphaHistory.html",
			"Photos", "ExhibitContents/hawk.jpg,ExhibitContents/eagle.jpg",
			"Videos", "ExhibitContents/alphaVideos.html",
			"Fun Facts", "ExhibitContents/alphaFunFacts.html",
			"Diet", "ExhibitContents/alphaDiet.html",
			"Spare", "ExhibitContents/alphaDiet.html"},
		{"Bravo", "38", "56", "Alpha", "Charlie",
			"Introduction", "ExhibitContents/bravoIntro.html", 
			"History", "ExhibitContents/alphaHistory.html",
			"Photos", "ExhibitContents/badger.jpg,ExhibitContents/bobcat.jpg",
			"Habitat", "ExhibitContents/alphaDiet.html",
			"Fun Facts", "ExhibitContents/alphaFunFacts.html",
			"Streaming Video", "ExhibitContents/bravoVideos.html"},
		{"Charlie", "30", "44", "Bravo", "Delta",
			"Introduction", "ExhibitContents/charlieIntro.html", 
			"Diet", "ExhibitContents/alphaDiet.html",
			"Photos", "ExhibitContents/wolf.jpg",
			"Videos", "ExhibitContents/alphaVideos.html",
			"Fun Facts", "ExhibitContents/alphaFunFacts.html",
			"Family Tree", "ExhibitContents/alphaHistory.html"},
		{"Delta", "46", "27", "Charlie", "",
			"Introduction", "ExhibitContents/deltaIntro.html", 
			"History", "ExhibitContents/alphaHistory.html",
			"Photos", "ExhibitContents/bobcat.jpg",
			"Behavior", "ExhibitContents/alphaDiet.html",
			"Fun Facts", "ExhibitContents/alphaFunFacts.html",
			"New Home", "ExhibitContents/alphaVideos.html"},
	};
	
	public ExhibitList(){
		Exhibit e;

		for (int i=0; i<EXHIBITS.length; i++){
			e = new Exhibit(EXHIBITS[i][0], EXHIBITS[i][6]);
			for(int k=7; k<EXHIBITS[i].length; k+=2){
				e.setContent(EXHIBITS[i][k], EXHIBITS[i][k+1]);
			}
			e.setCoords(Integer.decode(EXHIBITS[i][1]), Integer.decode(EXHIBITS[i][2]));
			e.setPrevious(EXHIBITS[i][3]);
			e.setNext(EXHIBITS[i][4]);
			exhibitList.put(e.getName(), e);
			keyList.add(e.getName());
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
