package org.wildlifeimages.tools.update;

import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipOutputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class ExhibitParser {
	ArrayList<ExhibitInfo> exhibits = new ArrayList<ExhibitInfo>();

	private void readExhibitTag(XmlPullParser xmlBox, ExhibitInfo e){
		if (xmlBox.getName().equalsIgnoreCase("content")){
			String url = xmlBox.getAttributeValue(null, "page");
			e.addOrigContent(xmlBox.getAttributeValue(null, "tag"), url);
		}else if (xmlBox.getName().equalsIgnoreCase("photo")){
			String url = xmlBox.getAttributeValue(null, "page");
			e.addPhoto(url);
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
			e.addAlias(aliasName, xAlias, yAlias);
			//exhibitList.put(aliasName, e);
		}
	}
	
	private void readExhibit(XmlPullParser xmlBox) throws XmlPullParserException, IOException{
		ExhibitInfo e = null;
		
		String name = xmlBox.getAttributeValue(null, "name"); 
		
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

		e = new ExhibitInfo(name, xCoord, yCoord, previous, next);
		exhibits.add(e);
		
		int eventType = xmlBox.getEventType();
		while (eventType != XmlPullParser.END_DOCUMENT) {
			if(eventType == XmlPullParser.START_TAG) {
				readExhibitTag(xmlBox, e);
			}else if(eventType == XmlPullParser.END_TAG){
				if (xmlBox.getName().equalsIgnoreCase("exhibit")){
					break;
				}
			}
			eventType = xmlBox.next();
		}
	}
	
	public ExhibitParser(XmlPullParser xmlBox) throws XmlPullParserException, IOException{
		int eventType;

		eventType = xmlBox.getEventType();

		while (eventType != XmlPullParser.END_DOCUMENT) {
			if(eventType == XmlPullParser.START_TAG) {
				if (xmlBox.getName().equalsIgnoreCase("exhibit")){
					readExhibit(xmlBox);
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
			StringBuffer sb = new StringBuffer();
			sb.append("<?xml version=\"1.0\"?>\n<exhibit_list>");

			for (ExhibitInfo e : exhibits){
				sb.append("\n\t<exhibit ");
				appendValue(sb, "name", e.getName());
				appendValue(sb, "xpos", e.getxCoord()+"");
				appendValue(sb, "ypos", e.getyCoord()+"");
				appendValue(sb, "previous", e.getPrevious());
				appendValue(sb, "next", e.getNext());
				sb.append(">");
				for (int i=0; i<e.getTagCount(); i++){
					sb.append("\n\t\t<content ");
					appendValue(sb, "tag", e.getTag(i));
					appendValue(sb, "page", e.getContents(e.getTag(i)));
					sb.append("/>");
				}
				for (String photo : e.getPhotos()){
					sb.append("\n\t\t<photo ");
					appendValue(sb, "page", photo);
					sb.append("/>");
				}
				sb.append("\n\t</exhibit> ");
			}

			sb.append("\n</exhibit_list>");

			out.write(sb.toString().getBytes());
		} catch (IOException e) {
		}
	}

	private void appendValue(StringBuffer sb, String name, String value){
		if (value != null){
			sb.append(name);
			sb.append("=\"");
			sb.append(value);
			sb.append("\" ");
		}
	}
}
