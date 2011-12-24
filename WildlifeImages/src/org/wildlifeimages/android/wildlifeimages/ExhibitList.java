package org.wildlifeimages.android.wildlifeimages;

import java.util.Enumeration;
import java.util.Hashtable;

public class ExhibitList {

	private Hashtable<String, Exhibit> exhibitList = new Hashtable<String, Exhibit>();
	
	private Exhibit current = null;
	
	private final String EXHIBITS[][] = {
			{"Alpha", "56", "59",
				"file:///android_asset/ExhibitContents/alphaIntro.html", 
				"file:///android_asset/ExhibitContents/alphaHistory.html",
				"file:///android_asset/ExhibitContents/alphaPhotos.html",
				"file:///android_asset/ExhibitContents/alphaVideos.html",
				"file:///android_asset/ExhibitContents/alphaFunFacts.html",
				"file:///android_asset/ExhibitContents/alphaDiet.html"},
			{"Bravo", "38", "56",
				"file:///android_asset/ExhibitContents/bravoIntro.html", 
				"file:///android_asset/ExhibitContents/alphaHistory.html",
				"file:///android_asset/ExhibitContents/badger.jpg",
				"file:///android_asset/ExhibitContents/alphaVideos.html",
				"file:///android_asset/ExhibitContents/alphaFunFacts.html",
				"file:///android_asset/ExhibitContents/alphaDiet.html"},
			{"Charlie", "30", "44",
				"file:///android_asset/ExhibitContents/charlieIntro.html", 
				"file:///android_asset/ExhibitContents/alphaHistory.html",
				"file:///android_asset/ExhibitContents/wolf.jpg",
				"file:///android_asset/ExhibitContents/alphaVideos.html",
				"file:///android_asset/ExhibitContents/alphaFunFacts.html",
				"file:///android_asset/ExhibitContents/alphaDiet.html"},
			{"Delta", "46", "27", 
				"file:///android_asset/ExhibitContents/deltaIntro.html", 
				"file:///android_asset/ExhibitContents/alphaHistory.html",
				"file:///android_asset/ExhibitContents/bobcat.jpg",
				"file:///android_asset/ExhibitContents/alphaVideos.html",
				"file:///android_asset/ExhibitContents/alphaFunFacts.html",
				"file:///android_asset/ExhibitContents/alphaDiet.html"}
	};
	
	private final String ORDER[][] = {
			{"Alpha", "Bravo"},
			{"Bravo", "Charlie"},
			{"Charlie", "Delta"}
	};
	
	public ExhibitList(){
		Exhibit e;

		for (int i=0; i<EXHIBITS.length; i++){
			e = new Exhibit(EXHIBITS[i][0], EXHIBITS[i][3]);
			e.setContent(Exhibit.HISTORY_TAG, EXHIBITS[i][4]);
			e.setContent(Exhibit.PHOTOS_TAG, EXHIBITS[i][5]);
			e.setContent(Exhibit.VIDEOS_TAG, EXHIBITS[i][6]);
			e.setContent(Exhibit.FUNFACTS_TAG, EXHIBITS[i][7]);
			e.setContent(Exhibit.DIET_TAG, EXHIBITS[i][8]);
			e.setCoords(Integer.decode(EXHIBITS[i][1]), Integer.decode(EXHIBITS[i][2]));
			exhibitList.put(e.getName(), e);
		}
		
		for (int i=0; i<ORDER.length; i++){
			exhibitList.get(ORDER[i][0]).setNext(exhibitList.get(ORDER[i][1]));
		}
		
		setCurrent(exhibitList.elements().nextElement(), Exhibit.INTRO_TAG);
	}

	public Enumeration<String> keys(){
		return exhibitList.keys();
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
			current.setCurrentTag(contentTag);
		}
	}
	
	public void setCurrent(String name, String contentTag) {
		Exhibit e = this.get(name);
		if (e != null){
			current = e;
			e.setCurrentTag(contentTag);
		}
	}

	public Exhibit getCurrent() {
		return current;
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
	
	public Enumeration<Exhibit> elements(){
		return exhibitList.elements();
	}
}
