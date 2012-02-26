package org.wildlifeimages.tools.update;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.zip.ZipOutputStream;

import org.wildlifeimages.tools.update.ExhibitInfo.Alias;
import org.wildlifeimages.tools.update.Parser.ExhibitDataHolder;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class ExhibitLoader implements Parser.ExhibitInterface{
	private ArrayList<ExhibitInfo> exhibits = new ArrayList<ExhibitInfo>();
	private Hashtable<String, ExhibitGroup> groupList = new Hashtable<String, ExhibitGroup>();

	public ExhibitLoader(XmlPullParser xmlBox) throws XmlPullParserException, IOException{
		new Parser(xmlBox, this);
	}

	public ArrayList<ExhibitInfo> getExhibits(){
		return exhibits;
	}

	public void writeExhibitXML(ZipOutputStream out) {
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

	public class ExhibitGroup{
		public final String[] exhibits;
		public final int xPos;
		public final int yPos;

		public ExhibitGroup(String[] list, int x, int y){
			exhibits = list;
			xPos = x;
			yPos = y;
		}
	}
	
	public String[] getGroupNames(){
		return groupList.keySet().toArray(new String[0]);
	}
	
	public ExhibitGroup getGroup(String name){
		return groupList.get(name);
	}

	@Override
	public void addGroup(String groupName, String[] data, int x, int y) {
		groupList.put(groupName, new ExhibitGroup(data, x, y));
	}

	@Override
	public void addExhibit(String name, int xCoord, int yCoord, String next, String previous, ExhibitDataHolder data) {
		ExhibitInfo e = new ExhibitInfo(name, xCoord, yCoord, previous, next);
		for(int i=0; i<data.contentNameList.size(); i++){
			e.addOrigContent(data.contentNameList.get(i), data.contentValueList.get(i));
		}
		for(String photo : data.photoList){
			e.addPhoto(photo);
		}
		for(int i=0; i<data.aliasList.size(); i++){
			e.addAlias(data.aliasList.get(i), data.aliasXList.get(i), data.aliasYList.get(i));
		}
		exhibits.add(e);
	}
}
