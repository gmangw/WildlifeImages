package org.wildlifeimages.tools.update;

import java.io.IOException;
import java.util.ArrayList;
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
					e.addOrigContent(xmlBox.getAttributeValue(null, "tag"), url);
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
