package org.wildlifeimages.tools.update;

import java.util.Hashtable;

import org.wildlifeimages.android.wildlifeimages.Exhibit;

public class ExhibitInfo extends Exhibit{
	public final String origName;
	public final int origXCoord;
	public final int origYCoord;
	public final String origPrevious;
	public final String origNext;

	private final Hashtable<String, String> origContents = new Hashtable<String, String>();
	
	public ExhibitInfo(String name, int x, int y, String previous, String next){
		super(name);
		origName = name;
		
		this.setCoords(x, y);
		origXCoord = x;
		origYCoord = y;
		
		this.setPrevious(previous);
		origPrevious = previous;
		
		this.setNext(next);
		origNext = next;
	}

	public void addOrigContent(String tag, String content) {
		this.setContent(tag, content);
		origContents.put(tag, content);
	}

	public String getOrigContents(String tag) {
		return origContents.get(tag);
	}
}