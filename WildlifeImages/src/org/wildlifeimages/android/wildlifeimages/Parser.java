package org.wildlifeimages.android.wildlifeimages;

import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;

import org.wildlifeimages.android.wildlifeimages.Exhibit.Alias;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class Parser {

	public Parser(XmlPullParser xmlBox, ExhibitInterface handler) throws XmlPullParserException, IOException {
		int eventType;
		if (xmlBox != null){
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
	}

	private void readExhibitTag(XmlPullParser xmlBox, ExhibitDataHolder e, ExhibitInterface handler) throws XmlPullParserException, IOException{
		if (xmlBox.getName().equalsIgnoreCase("content")){
			String url = xmlBox.getAttributeValue(null, "page");
			e.contentNameList.add(xmlBox.getAttributeValue(null, "tag"));
			e.contentValueList.add(url);
		}else if (xmlBox.getName().equalsIgnoreCase("photo")){
			String url = xmlBox.getAttributeValue(null, "page");
			String caption = xmlBox.getAttributeValue(null, "comment");
			e.photoList.add(new ExhibitPhoto(url, caption));
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
			String aliasTag = xmlBox.getAttributeValue(null, "tag");
			if (aliasTag == null){
				aliasTag = Exhibit.TAG_AUTO;
			}
			e.aliasList.add(aliasName);
			e.aliasXList.add(xAlias);
			e.aliasYList.add(yAlias);
			e.aliasTagList.add(aliasTag);
		}
	}

	private void readGroupMember(XmlPullParser xmlBox, ArrayList<String> members, ExhibitInterface handler){
		if (xmlBox.getName().equalsIgnoreCase("member")){
			String name = xmlBox.getAttributeValue(null, "exhibit");
			members.add(name);
		}
	}

	private static String getXMLAttribute(XmlPullParser xmlBox, String name, String defaultString){
		String result = xmlBox.getAttributeValue(null, name);
		if (result == null){
			return defaultString;
		}else{
			return result;
		}
	}

	private void readGroup(XmlPullParser xmlBox, ExhibitInterface handler) throws XmlPullParserException, IOException{
		int eventType;
		String groupName = getXMLAttribute(xmlBox, "name", "Unnamed Group");
		ArrayList<String> members = new ArrayList<String>();
		int xCoord = Integer.decode(getXMLAttribute(xmlBox, "xpos", "-1"));
		int yCoord = Integer.decode(getXMLAttribute(xmlBox, "ypos", "-1"));
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

		String name = getXMLAttribute(xmlBox, "name", "Unnamed Exhibit");
		int xCoord = Integer.decode(getXMLAttribute(xmlBox, "xpos", "-1"));
		int yCoord = Integer.decode(getXMLAttribute(xmlBox, "ypos", "-1"));

		String previous = getXMLAttribute(xmlBox, "previous", null);
		String next = getXMLAttribute(xmlBox, "next", null);

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

	private static void appendValue(StringBuffer sb, String name, String value){
		if (value != null){
			sb.append(name);
			sb.append("=\"");
			sb.append(value);
			sb.append("\" ");
		}
	}

	public static void writeExhibitXML(OutputStream out, ArrayList<? extends Exhibit> exhibits, Hashtable<String, ExhibitGroup> groupList) throws IOException {
		StringBuffer sb = new StringBuffer();
		sb.append("<?xml version=\"1.0\"?>\n<exhibit_list>");

		for (Exhibit e : exhibits){
			sb.append("\n\t<exhibit ");
			appendValue(sb, "name", e.getName());
			appendValue(sb, "xpos", e.getX()+"");
			appendValue(sb, "ypos", e.getY()+"");
			appendValue(sb, "previous", e.getPrevious());
			appendValue(sb, "next", e.getNext());
			sb.append(">");

			String tag;
			for (Iterator<String> iter = e.getTags(); iter.hasNext();){
				tag = iter.next();
				sb.append("\n\t\t<content ");
				appendValue(sb, "tag", tag);
				appendValue(sb, "page", e.getContent(tag));
				sb.append("/>");
			}
			for (ExhibitPhoto photo : e.getPhotos()){
				sb.append("\n\t\t<photo ");
				appendValue(sb, "page", photo.shortUrl);
				appendValue(sb, "comment", photo.getCaption());
				sb.append("/>");
			}
			for (Alias a : e.getAliases()){
				sb.append("\n\t\t<alias ");
				appendValue(sb, "name", a.name);
				appendValue(sb, "xpos", ""+a.xPos);
				appendValue(sb, "ypos", ""+a.yPos);
				sb.append("/>");
			}
			sb.append("\n\t</exhibit> ");
		}
		for (String groupName : groupList.keySet()){
			ExhibitGroup group = groupList.get(groupName);
			sb.append("\n\t<group ");
			appendValue(sb, "name", groupName);
			appendValue(sb, "xpos", ""+group.xPos);
			appendValue(sb, "ypos", ""+group.yPos);
			sb.append(">");
			for (String member : group.exhibits){
				sb.append("\n\t\t<member ");
				appendValue(sb, "exhibit", member);
				sb.append("/>");
			}
			sb.append("\n\t</group> ");
		}

		sb.append("\n</exhibit_list>");
		out.write(sb.toString().getBytes());
	}

	public static void writeEventsXml(OutputStream out, ArrayList<? extends Event> events)throws IOException{
		StringBuffer sb = new StringBuffer();
		sb.append("<?xml version=\"1.0\"?>\n");
		sb.append("<events_list xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n\t");
		sb.append("xsi:noNamespaceSchemaLocation=\"../EventsSchema.xsd\">\n");

		for (Event e : events){
			sb.append("\t<event>\n");

			sb.append("\t\t<title>");
			sb.append(e.getTitle().replace("&", "and"));
			sb.append("</title>\n");

			sb.append("\t\t<startdate>");
			Date d = e.getStartDay();
			Calendar now = Calendar.getInstance();
			now.setTime(d);
			String date = String.format("%1$tY-%1$tm-%1$td", now);
			sb.append(date);
			sb.append("</startdate>\n");

			sb.append("\t\t<enddate>");
			d = e.getEndDay();
			now = Calendar.getInstance();
			now.setTime(d);
			date = String.format("%1$tY-%1$tm-%1$td", now);
			sb.append(date);
			sb.append("</enddate>\n");

			sb.append("\t\t<description>");
			sb.append(e.getDescription().replace("&", "and"));
			sb.append("</description>\n");

			if (e.getImage().length() > 0){
				sb.append("\t\t<image>");
				sb.append(e.getImage());
				sb.append("</image>\n");
			}

			sb.append("\t</event>\n");
		}

		sb.append("</events_list>");
		out.write(sb.toString().getBytes());
	}

	public class ExhibitDataHolder{
		public ArrayList<String> contentNameList = new ArrayList<String>();
		public ArrayList<String> contentValueList = new ArrayList<String>();
		public ArrayList<ExhibitPhoto> photoList = new ArrayList<ExhibitPhoto>();
		public ArrayList<String> aliasList = new ArrayList<String>();
		public ArrayList<Integer> aliasXList = new ArrayList<Integer>();
		public ArrayList<Integer> aliasYList = new ArrayList<Integer>();
		public ArrayList<String> aliasTagList = new ArrayList<String>();
	}

	public interface ExhibitInterface {

		public void addGroup(String groupName, String[] data, int x, int y);

		public void addExhibit(String name, int xCoord, int yCoord, String next, String previous, ExhibitDataHolder data);
	}

	public static Event[] parseEvents(XmlPullParser xmlBox)throws IOException, XmlPullParserException{
		int eventType;
		ArrayList<Event> eventList = new ArrayList<Event>();

		if (xmlBox != null){
			eventType = xmlBox.getEventType();

			while (eventType != XmlPullParser.END_DOCUMENT) {
				if(eventType == XmlPullParser.START_TAG) {
					if (xmlBox.getName().equalsIgnoreCase("event")){
						eventList.add(Parser.readEvent(xmlBox));
					}
				}
				eventType = xmlBox.next();
			}
		}
		return eventList.toArray(new Event[0]);
	}

	public static class Event implements Comparable<Event>{
		private Date startDay = new Date(0);
		private Date endDay = new Date(0);
		private String title = "";
		private String description = "";
		private String imageUrl = "";

		public Date getStartDay() {
			return startDay;
		}
		public void setStartDay(Date startDay) {
			this.startDay = startDay;
			setEndDay(endDay);
		}
		public void setStartDay(String startDay){
			setStartDay(convertXmlToDate(startDay));
		}
		public Date getEndDay() {
			return endDay;
		}
		public void setEndDay(Date endDay) {
			if (endDay.before(startDay)){
				endDay = new Date(startDay.getTime());
			}
			this.endDay = endDay;
		}
		public void setEndDay(String endDay){
			setEndDay(convertXmlToDate(endDay));
		}
		public String getTitle() {
			return title;
		}
		public void setTitle(String title) {
			this.title = title;
		}
		public String getDescription() {
			return description;
		}
		public void setDescription(String description) {
			this.description = description;
		}
		public void setImage(String image){
			imageUrl = image;
		}
		public String getImage(){
			return imageUrl;
		}
		private Date convertXmlToDate(String xmlDate){
			SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
			Date time;
			try {
				time= fmt.parse(xmlDate);
			} catch (ParseException e) {
				time = new Date(0);
			}
			return time;
		}
		public int compareTo(Event other) {
			return this.getStartDay().compareTo(other.getStartDay());
		}
	}

	private static Event readEvent(XmlPullParser xmlBox) throws XmlPullParserException, IOException{
		int eventType;
		Event e = new Event();

		eventType = xmlBox.getEventType();
		while (eventType != XmlPullParser.END_DOCUMENT) {
			if(eventType == XmlPullParser.START_TAG) {
				if (xmlBox.getName().equalsIgnoreCase("title")){
					eventType = xmlBox.next();
					String title = xmlBox.getText();
					e.setTitle(title);
				}else if (xmlBox.getName().equalsIgnoreCase("startdate")){
					eventType = xmlBox.next();
					String date = xmlBox.getText();
					e.setStartDay(date);
				}else if (xmlBox.getName().equalsIgnoreCase("enddate")){
					eventType = xmlBox.next();
					String date = xmlBox.getText();
					e.setEndDay(date);
				}else if (xmlBox.getName().equalsIgnoreCase("description")){
					eventType = xmlBox.next();
					String description = xmlBox.getText();
					e.setDescription(description);
				}else if (xmlBox.getName().equalsIgnoreCase("image")){
					eventType = xmlBox.next();
					String image = xmlBox.getText();
					e.setImage(image);
				}
			}else if(eventType == XmlPullParser.END_TAG){
				if (xmlBox.getName().equalsIgnoreCase("event")){
					break;
				}
			}
			eventType = xmlBox.next();
		}
		return e;
	}

}
