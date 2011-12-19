package org.wildlifeimages.android.wildlifeimages;

import java.util.Enumeration;
import java.util.Hashtable;

public class ExhibitList {
	
	private Hashtable<String, Exhibit> exhibitList = new Hashtable<String, Exhibit>();
	
	private Exhibit current = null;
	
	private final String EXHIBITS[][] = {
			{"Alpha", "<html><body>Exhibit Alpha Contents</body></html>", "56", "59"},
			{"Bravo", "<html><body>Exhibit Bravo Contents</body></html>", "38", "56"},
			{"Charlie", "<html><body>Exhibit Charlie Contents</body></html>", "30", "44"},
			{"Delta", "<html><body>Exhibit Delta Contents</body></html>", "46", "27"}
	};
	
	private final String ORDER[][] = {
			{"Alpha", "Bravo"},
			{"Bravo", "Charlie"},
			{"Charlie", "Delta"}
	};
	
	public ExhibitList(){
		Exhibit e;

		for (int i=0; i<EXHIBITS.length; i++){
			e = new Exhibit(EXHIBITS[i][0], EXHIBITS[i][1]);
			e.setCoords(Integer.decode(EXHIBITS[i][2]), Integer.decode(EXHIBITS[i][3]));
			exhibitList.put(e.getName(), e);
		}
		
		for (int i=0; i<ORDER.length; i++){
			exhibitList.get(ORDER[i][0]).setNext(exhibitList.get(ORDER[i][1]));
		}
		
		setCurrent(get("Alpha")); //TODO
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

	public void setCurrent(Exhibit current) {
		this.current = current;
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
