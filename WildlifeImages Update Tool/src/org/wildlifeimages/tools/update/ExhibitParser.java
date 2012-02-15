package org.wildlifeimages.tools.update;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.zip.ZipOutputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class ExhibitParser {
	ArrayList<ExhibitInfo> exhibits = new ArrayList<ExhibitInfo>();

	public ExhibitParser(XmlPullParser xmlBox) throws XmlPullParserException, IOException{
		int eventType;

		ExhibitInfo e = null;

		eventType = xmlBox.getEventType();

		while (eventType != XmlPullParser.END_DOCUMENT) {
			if(eventType == XmlPullParser.START_TAG) {
				if (xmlBox.getName().equalsIgnoreCase("exhibit")){
					String name = xmlBox.getAttributeValue(null, "name"); 
					int xCoord = Integer.decode(xmlBox.getAttributeValue(null, "xpos"));
					int yCoord = Integer.decode(xmlBox.getAttributeValue(null, "ypos"));
					String previous = xmlBox.getAttributeValue(null, "previous");
					String next = xmlBox.getAttributeValue(null, "next");

					e = new ExhibitInfo(name, xCoord, yCoord, previous, next);
					exhibits.add(e);
				}else if (xmlBox.getName().equalsIgnoreCase("content")){
					String url = xmlBox.getAttributeValue(null, "page");
					e.addContent(xmlBox.getAttributeValue(null, "tag"), url);
				}else if (xmlBox.getName().equalsIgnoreCase("photo")){
					String url = xmlBox.getAttributeValue(null, "page");
					e.addPhoto(url);
				}
			}
			eventType = xmlBox.next();
		}
	}

	public ArrayList<ExhibitInfo> getExhibits(){
		return exhibits;
	}
	
	public void writeExhibits(ZipOutputStream out) {
		try {
			out.write("<?xml version=\"1.0\"?><exhibit_list>".getBytes());
			
			for (ExhibitInfo e : exhibits){
				
			}
			
			out.write("</exhibit_list>".getBytes());
		} catch (IOException e) {
		}
	}

	public class ExhibitInfo {
		private static final String TAG_PHOTO = "Photos";

		public final String origName;
		public final int origXCoord;
		public final int origYCoord;
		public final String origPrevious;
		public final String origNext;
		
		private String name = "New Exhibit";
		private int xCoord = 0;
		private int yCoord = 0;
		private String previous = null;
		private String next = null;
		private Hashtable<String, String> contents = new Hashtable<String, String>();
		private Hashtable<String, String> origContents = new Hashtable<String, String>();
		private ArrayList<String> photos = new ArrayList<String>();
		private ArrayList<String> tags = new ArrayList<String>();

		public ExhibitInfo(String name, int x, int y, String previous, String next){
			this.name = name;
			this.origName = name;
			
			this.xCoord = x;
			this.origXCoord = x;
			
			this.yCoord = y;
			this.origYCoord = y;
			
			this.previous = previous;
			this.origPrevious = previous;
			
			this.next = next;
			this.origNext = next;
		}

		public String getContents(String tag){
			return contents.get(tag);
		}
		
		public String getOrigContents(String tag){
			return origContents.get(tag);
		}

		public String getTag(int index){
			return tags.get(index);
		}

		public int getTagCount(){
			return tags.size();
		}

		public void addContent(String tag, String data){
			if (contents.containsKey(tag) == false){
				tags.add(tag);
				origContents.put(tag, data);
			}
			contents.put(tag, data);
		}
		
		

		public void addPhoto(String url){
			if (photos.size() == 0){
				//tags.add(TAG_PHOTO); //TODO
			}
			photos.add(url);
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getxCoord() {
			return xCoord;
		}

		public void setxCoord(int xCoord) {
			this.xCoord = xCoord;
		}

		public int getyCoord() {
			return yCoord;
		}

		public void setyCoord(int yCoord) {
			this.yCoord = yCoord;
		}

		public String getPrevious() {
			return previous;
		}

		public void setPrevious(String previous) {
			this.previous = previous;
		}

		public String getNext() {
			return next;
		}

		public void setNext(String next) {
			this.next = next;
		}
	}
}
