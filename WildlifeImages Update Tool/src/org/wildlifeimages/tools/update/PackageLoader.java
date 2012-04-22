package org.wildlifeimages.tools.update;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.zip.ZipInputStream;

import org.wildlifeimages.android.wildlifeimages.Parser.Event;

public interface PackageLoader {

	public ExhibitLoader readPackage(ArrayList<String> files) throws IOException;
	
	public ExhibitLoader readUpdate(ZipInputStream source, ArrayList<String> files) throws IOException;

	public boolean loadNewPackage();
	
	public InputStream getFileInputStream(String filename) throws IOException;

	public boolean isNew();

	public void setNewState(boolean b);
	
	public Event[] loadEvents();
}
