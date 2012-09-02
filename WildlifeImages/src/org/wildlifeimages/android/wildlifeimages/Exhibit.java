package org.wildlifeimages.android.wildlifeimages;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;


/**
 * An Exhibit contains all of the content info for one map location.
 * 
 * @author Graham Wilkinson
 * @author Shady Glenn
 * @author Naveen Nanja
 * 	
 */
public class Exhibit {
	public static final String TAG_AUTO = "_";
	public static final String TAG_PHOTOS = "Photos";

	private String name;
	private String currentTag = null;
	private LinkedHashMapRestricted<String, String> contents = new LinkedHashMapRestricted<String, String>();
	private LinkedHashSet<ExhibitPhoto> photos = new LinkedHashSet<ExhibitPhoto>();
	private ArrayList<Alias> aliasList = new ArrayList<Alias>();
	private String next = null;
	private String previous = null;
	private int mapX = -1;
	private int mapY = -1;

	/**
	 * This is the constructor and will give the exhibit a name.
	 * 
	 * @param name The name to give the exhibit.
	 */
	public Exhibit(String name) {
		this.name = name;
	}

	/**
	 * This will get the next exhibit.
	 * 
	 * @return The next exhibit.
	 */
	public String getNext() {
		return this.next;
	}

	/**
	 * This function will set the next exhibit to nextName.
	 * 
	 * @param nextName The name of the next exhibit.
	 */
	public void setNext(String nextName) {
		this.next = nextName;
	}

	/**
	 * This will get the name of the previous exhibit.
	 * 
	 * @return The name of the previous exhibit.
	 */
	public String getPrevious() {
		return previous;
	}

	/**
	 * This will set the previous exhibit to be previous.
	 * 
	 * @param previous The name of the exhibit to set previous to.
	 */
	public void setPrevious(String previous) {
		this.previous = previous;
	}

	/**
	 * Will return and iterator of the different content tags for this exhibit.
	 * 
	 * @return An iterator of the tags for this exhibit.
	 */
	public Iterator<String> getTags() {
		return contents.keySet().iterator();
	}

	/**
	 * Will get the content held by the contentTag for this exhibit.
	 * 
	 * @param contentTag The tag that we want to get content from, so pages in exhibits.
	 * 
	 * @return The content of the specified tag for the exhibit.
	 */
	public String getContent(String contentTag) {
		String c = contents.get(contentTag);
		if (c == null) {
			return contents.entrySet().iterator().next().getValue();
		}else{
			return c;
		}
	}
	
	/**
	 * This will set the content of the contentTag for the current exhibit to be content. 
	 * 
	 * @param contentTag The tag we will be setting.
	 * @param content The string we will be setting the tag to.
	 */
	public void setContent(String contentTag, String content) {
		if (content == null) {
			contents.remove(contentTag);
			return;
		}
		contents.put(contentTag, content);
		if (currentTag == null) {
			currentTag = contentTag;
		}
	}

	/**
	 * This will add the photo with the photo URL to the photos hash table so it can be looked
	 *  up later.  If the image has a caption, this is also added.
	 * 
	 * @param photo The photo to be added.
	 */
	public void addPhoto(ExhibitPhoto photo) {
		photos.add(photo);
	}

	/**
	 * This will return the name of the exhibit.
	 * 
	 * @return The name of the exhibit.
	 */
	public String getName() {
		return name;
	}

	/**
	 * This will set the touch sensitive information for the exhibit as well as where the
	 *  exhibit name is drawn.
	 * 
	 * @param x The x coordinate.
	 * @param y The y coordinate.
	 */
	public void setCoords(int x, int y) {
		mapX = x;
		mapY = y;
	}

	/**
	 * For this exhibit, find the closest alias that is on the map, so find out how close the closest
	 *  alias of this exhibit is.
	 * 
	 * @param x The x coordinate component.
	 * @param y The y coordinate component.
	 * 
	 * @return The distance of the closest alias to the coordinate.
	 */
	public int getMinDistance(int x, int y) {
		float distance = Common.distance(mapX, mapY, x, y);
		for (Alias a : aliasList) {
			float aliasDistance = Common.distance(a.xPos, a.yPos, x, y);
			if (aliasDistance < distance) {
				distance = aliasDistance;
			}
		}
		return (int) distance;
	}

	/**
	 * Return the X component of the exhibit location.
	 * 
	 * @return The X component of the exhibit location.
	 */
	public int getX() {
		return mapX;
	}

	/**
	 * Return the Y component of the exhibit location.
	 * 
	 * @return The Y component of the exhibit location.
	 */
	public int getY() {
		return mapY;
	}

	/**
	 * Gets the current page the user is looking at.
	 * 
	 * @return The current page that the user is looking at.
	 */
	public String getCurrentTag() {
		return currentTag;
	}

	/**
	 * Displays the page contentTag.
	 * 
	 * @param contentTag The page to now display.
	 */
	public void setCurrentTag(String contentTag) {
		currentTag = contentTag;
	}

	/**
	 * If you are checking if 2 exhibits are equal, just want to check if
	 *  they have the same name, so not check if the pointers are just the same. 
	 */
	@Override
	public boolean equals(Object o) {
		if (o != null && o.getClass() == Exhibit.class) {
			if (((Exhibit)o).getName().equals(name)) {
				return true;
			}else{
				return false;
			}
		}else{
			return false;
		}
	}

	/**
	 * Called by hash tables, but we want to look at the name and make a hash off that.
	 */
	@Override
	public int hashCode() {
		return name.hashCode();
	}

	/**
	 * Will put a new alias in the exhibits object, so that it will show up somewhere on the map.
	 * 
	 * @param aliasName The name of the alias.
	 * @param xPos The x coordinate of the alias.
	 * @param yPos The y coordinate of the alias.
	 * @param tag The tag, so page to go to when clicked.
	 */
	public void addAlias(String aliasName, int xPos, int yPos, String tag) {
		for (int i = 0; i < aliasList.size(); i++) {
			if (aliasList.get(i).name.equals(aliasName)) {
				aliasList.set(i, new Alias(aliasName, xPos, yPos, tag));
				return;
			}
		}
		aliasList.add(new Alias(aliasName, xPos, yPos, tag));
	}

	/**
	 * This will return the alias array for the exhibit.
	 * 
	 * @return The alias array for the current exhibit.
	 */
	public Alias[] getAliases() {
		return aliasList.toArray(new Alias[aliasList.size()]);
	}

	/**
	 * Definition of an alias, inner class.
	 */
	public class Alias {
		public final String name;
		public final int xPos;
		public final int yPos;
		public final String tag;

		public Alias(String aliasName, int aliasX, int aliasY, String exhibitTag) {
			name = aliasName;
			xPos = aliasX;
			yPos = aliasY;
			tag = exhibitTag;
		}
	}

	/**
	 * Will return an array of all the photos for the current exhibit.
	 * 
	 * @return The array of all the exhibit's photos.
	 */
	public ExhibitPhoto[] getPhotos() {
		return photos.toArray(new ExhibitPhoto[0]);
	}

	/**
	 * This will return the number of pages in the current exhibit.
	 * 
	 * @return The number of pages in the current exhibit.
	 */
	public int getTagCount() {
		return contents.entrySet().size();
	}

	/**
	 * This will return the index of the current page we are on.
	 * 
	 * @param index The index of the page we are currently on.
	 * @return The index of the page we are currently on.
	 */
	public String getTag(int index) {
		return (String) contents.keySet().toArray()[index];
	}
	
	/**
	 * This will return only the name of the exhibit instead of the entire exhibit
	 *  as a string.
	 */
	@Override
	public String toString() {
		return name;
	}
}
