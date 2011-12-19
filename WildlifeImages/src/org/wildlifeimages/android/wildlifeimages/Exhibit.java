package org.wildlifeimages.android.wildlifeimages;

public class Exhibit {
	
	private String name;
	
	private String contents;
	
	private Exhibit next = null;
	
	private Exhibit previous = null;
	
	private int mapX = -1;
	
	private int mapY = -1;
	
	public Exhibit getNext() {
		return next;
	}

	public void setNext(Exhibit next) {
		this.next = next;
		if (next != null){
			next.setPrevious(this);
		}
	}
	
	public Exhibit getPrevious() {
		return previous;
	}

	private void setPrevious(Exhibit previous) {
		this.previous = previous;
	}

	public String getContents() {
		return contents;
	}

	public void setContents(String contents) {
		this.contents = contents;
	}
	
	public Exhibit(String name, String contents){
		this.name = name;
		this.contents = contents;
	}

	public String getName() {
		return name;
	}
	
	public void setCoords(int x, int y){
		mapX = x;
		mapY = y;
	}
	
	public int getDistance(int x, int y){
		return (int)Math.sqrt(Math.pow(mapX - x, 2) + Math.pow(mapY - y, 2));
	}
	
	public int getX(){
		return mapX;
	}
	
	public int getY(){
		return mapY;
	}
}
