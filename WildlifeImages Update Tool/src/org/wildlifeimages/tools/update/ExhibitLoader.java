package org.wildlifeimages.tools.update;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.zip.ZipOutputStream;

import org.wildlifeimages.android.wildlifeimages.ExhibitGroup;
import org.wildlifeimages.android.wildlifeimages.Parser;
import org.wildlifeimages.android.wildlifeimages.Parser.ExhibitDataHolder;
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
	
	public void removeGroup(String groupName){
		groupList.remove(groupName);
	}
	
	public void removeGroupExhibit(String exhibitName, String groupName){
		ExhibitGroup group = groupList.get(groupName);
		ArrayList<String> newExhibits = new ArrayList<String>();
		for (String s : group.exhibits){
			if (s.equals(exhibitName) == false){
				newExhibits.add(s);
			}
		}
		ExhibitGroup newGroup = new ExhibitGroup(newExhibits.toArray(new String[0]), group.xPos, group.yPos);
		groupList.put(groupName, newGroup);
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

	public void writeXML(ZipOutputStream out) throws IOException {
		Parser.writeExhibitXML(out, exhibits, groupList);
	}

	public void removeExhibit(String name) {
		for (int i=0; i<exhibits.size(); i++){
			if (exhibits.get(i).getName().equals(name)){
				exhibits.remove(i);
			}
		}
		
	}
	
	
}