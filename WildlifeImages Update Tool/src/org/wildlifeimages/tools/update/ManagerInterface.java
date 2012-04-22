package org.wildlifeimages.tools.update;

import java.io.File;
import java.io.InputStream;

import org.wildlifeimages.android.wildlifeimages.Parser.Event;

public interface ManagerInterface {

	void addFile();

	void makeChange();

	String[] getOriginalFiles();

	boolean modifiedFileExists(String s);

	String[] getModifiedFileNames();

	File getModifiedFile(String string);

	InputStream getFileInputStream(String string);

	ExhibitLoader getLoader();
	
	Event[] loadEvents();
}
