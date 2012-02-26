package org.wildlifeimages.tools.update;

import java.io.IOException;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class Parser {

	public Parser(XmlPullParser xmlBox, ExhibitInterface handler) throws XmlPullParserException, IOException {
		int eventType;

		eventType = xmlBox.getEventType();

		while (eventType != XmlPullParser.END_DOCUMENT) {
			if(eventType == XmlPullParser.START_TAG) {
				if (xmlBox.getName().equalsIgnoreCase("exhibit")){
					readExhibit(xmlBox, handler);
				}else if(xmlBox.getName().equalsIgnoreCase("group")){
					readGroup(xmlBox, handler);
				}
			}
			eventType = xmlBox.next();
		}
	}

	private void readExhibitTag(XmlPullParser xmlBox, ExhibitDataHolder e, ExhibitInterface handler) throws XmlPullParserException, IOException{
		if (xmlBox.getName().equalsIgnoreCase("content")){
			String url = xmlBox.getAttributeValue(null, "page");
			e.contentNameList.add(xmlBox.getAttributeValue(null, "tag"));
			e.contentValueList.add(url);
		}else if (xmlBox.getName().equalsIgnoreCase("photo")){
			String url = xmlBox.getAttributeValue(null, "page");
			e.photoList.add(url);
		}else if (xmlBox.getName().equalsIgnoreCase("alias")){
			String tmp = xmlBox.getAttributeValue(null, "xpos");
			int xAlias = -1;
			int yAlias = -1;
			if (tmp != null){
				xAlias = Integer.decode(tmp);
			}
			tmp = xmlBox.getAttributeValue(null, "ypos");
			if (tmp != null){
				yAlias = Integer.decode(tmp);
			}
			String aliasName = xmlBox.getAttributeValue(null, "name");
			e.aliasList.add(aliasName);
			e.aliasXList.add(xAlias);
			e.aliasYList.add(yAlias);
		}
	}

	private void readGroupMember(XmlPullParser xmlBox, ArrayList<String> members, ExhibitInterface handler){
		if (xmlBox.getName().equalsIgnoreCase("member")){
			String name = xmlBox.getAttributeValue(null, "exhibit");
			members.add(name);
		}
	}

	private void readGroup(XmlPullParser xmlBox, ExhibitInterface handler) throws XmlPullParserException, IOException{
		int eventType;
		String groupName = xmlBox.getAttributeValue(null, "name");//TODO error check
		ArrayList<String> members = new ArrayList<String>();
		String tmp = xmlBox.getAttributeValue(null, "xpos");
		int xCoord = -1;
		int yCoord = -1;
		if (tmp != null){
			xCoord = Integer.decode(tmp);
		}
		tmp = xmlBox.getAttributeValue(null, "ypos");
		if (tmp != null){
			yCoord = Integer.decode(tmp);
		}		

		eventType = xmlBox.getEventType();
		while (eventType != XmlPullParser.END_DOCUMENT) {
			if(eventType == XmlPullParser.START_TAG) {
				readGroupMember(xmlBox, members, handler);
			}else if(eventType == XmlPullParser.END_TAG){
				if (xmlBox.getName().equalsIgnoreCase("group")){
					break;
				}
			}
			eventType = xmlBox.next();
		}
		handler.addGroup(groupName, members.toArray(new String[0]), xCoord, yCoord);
	}

	private void readExhibit(XmlPullParser xmlBox, ExhibitInterface handler) throws XmlPullParserException, IOException{
		int eventType;

		String name = xmlBox.getAttributeValue(null, "name");//TODO error check

		String tmp = xmlBox.getAttributeValue(null, "xpos");
		int xCoord = -1;
		int yCoord = -1;
		if (tmp != null){
			xCoord = Integer.decode(tmp);
		}
		tmp = xmlBox.getAttributeValue(null, "ypos");
		if (tmp != null){
			yCoord = Integer.decode(tmp);
		}
		String previous = xmlBox.getAttributeValue(null, "previous");
		String next = xmlBox.getAttributeValue(null, "next");
		//TODO null handling

		ExhibitDataHolder e = new ExhibitDataHolder();
		eventType = xmlBox.getEventType();
		while (eventType != XmlPullParser.END_DOCUMENT) {
			if(eventType == XmlPullParser.START_TAG) {
				readExhibitTag(xmlBox, e, handler);
			}else if(eventType == XmlPullParser.END_TAG){
				if (xmlBox.getName().equalsIgnoreCase("exhibit")){
					break;
				}
			}
			eventType = xmlBox.next();
		}
		handler.addExhibit(name, xCoord, yCoord, next, previous, e);
	}

	public class ExhibitDataHolder{
		public ArrayList<String> contentNameList = new ArrayList<String>();
		public ArrayList<String> contentValueList = new ArrayList<String>();
		public ArrayList<String> photoList = new ArrayList<String>();
		public ArrayList<String> aliasList = new ArrayList<String>();
		public ArrayList<Integer> aliasXList = new ArrayList<Integer>();
		public ArrayList<Integer> aliasYList = new ArrayList<Integer>();
	}

	public interface ExhibitInterface {

		public void addGroup(String groupName, String[] data, int x, int y);

		public void addExhibit(String name, int xCoord, int yCoord, String next, String previous, ExhibitDataHolder data);
	}
}
