package org.wildlifeimages.android.wildlifeimages;

import java.util.Enumeration;
import java.util.Hashtable;

public class ExhibitList {

	private Hashtable<String, Exhibit> exhibitList = new Hashtable<String, Exhibit>();
	
	private Exhibit current = null;
	
	private final String EXHIBITS[][] = {
		{"Alpha", "56", "59",
			"Introduction", "file:///android_asset/ExhibitContents/alphaIntro.html", 
			"History", "file:///android_asset/ExhibitContents/alphaHistory.html",
			"Photos", "file:///android_asset/ExhibitContents/alphaPhotos.html",
			"Videos", "file:///android_asset/ExhibitContents/alphaVideos.html",
			"Fun Facts", "file:///android_asset/ExhibitContents/alphaFunFacts.html",
			"Diet", "file:///android_asset/ExhibitContents/alphaDiet.html"},
		{"Bravo", "38", "56",
			"Introduction", "file:///android_asset/ExhibitContents/bravoIntro.html", 
			"History", "file:///android_asset/ExhibitContents/alphaHistory.html",
			"Photos", "file:///android_asset/ExhibitContents/badger.jpg",
			"Videos", "file:///android_asset/ExhibitContents/alphaVideos.html",
			"Fun Facts", "file:///android_asset/ExhibitContents/alphaFunFacts.html",
			"Diet", "file:///android_asset/ExhibitContents/alphaDiet.html"},
		{"Charlie", "30", "44",
			"Introduction", "file:///android_asset/ExhibitContents/charlieIntro.html", 
			"History", "file:///android_asset/ExhibitContents/alphaHistory.html",
			"Photos", "file:///android_asset/ExhibitContents/wolf.jpg",
			"Videos", "file:///android_asset/ExhibitContents/alphaVideos.html",
			"Fun Facts", "file:///android_asset/ExhibitContents/alphaFunFacts.html",
			"Diet", "file:///android_asset/ExhibitContents/alphaDiet.html"},
		{"Delta", "46", "27", 
			"Introduction", "file:///android_asset/ExhibitContents/deltaIntro.html", 
			"History", "file:///android_asset/ExhibitContents/alphaHistory.html",
			"Photos", "file:///android_asset/ExhibitContents/bobcat.jpg",
			"Videos", "file:///android_asset/ExhibitContents/alphaVideos.html",
			"Fun Facts", "file:///android_asset/ExhibitContents/alphaFunFacts.html",
			"Diet", "file:///android_asset/ExhibitContents/alphaDiet.html"},
	};
	
	private final String ORDER[][] = {
			{"Alpha", "Bravo"},
			{"Bravo", "Charlie"},
			{"Charlie", "Delta"}
	};
	
	public ExhibitList(){
		Exhibit e;

		for (int i=0; i<EXHIBITS.length; i++){
			e = new Exhibit(EXHIBITS[i][0], EXHIBITS[i][4]);
			for(int k=5; k<EXHIBITS[i].length; k+=2){
				e.setContent(EXHIBITS[i][k], EXHIBITS[i][k+1]);
			}
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
			if (Exhibit.AUTO_TAG != contentTag){
				current.setCurrentTag(contentTag);
			}
		}
	}
	
	public void setCurrent(String name, String contentTag) {
		Exhibit e = this.get(name);
		setCurrent(e, contentTag);
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
