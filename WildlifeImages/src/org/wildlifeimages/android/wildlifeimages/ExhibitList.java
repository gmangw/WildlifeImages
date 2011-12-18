package org.wildlifeimages.android.wildlifeimages;

import java.util.Hashtable;

public class ExhibitList {
	
	private Hashtable<String, Exhibit> exhibitList = new Hashtable<String, Exhibit>();
	
	private Exhibit current = null;
	
	private final String EXHIBITS[][] = {
			{"Alpha", "<html><body>Exhibit Alpha Contents</body></html>", "0", "0"},
			{"Bravo", "<html><body>Exhibit Bravo Contents</body></html>", "0", "0"},
			{"Charlie", "<html><body>Exhibit Charlie Contents</body></html>", "0", "0"},
			{"Delta", "<html><body>Exhibit Delta Contents</body></html>", "0", "0"}
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
}
