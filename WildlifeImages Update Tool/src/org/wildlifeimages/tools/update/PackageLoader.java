package org.wildlifeimages.tools.update;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public interface PackageLoader {

	ExhibitLoader readPackage(ArrayList<String> files) throws IOException;

	public boolean loadNewPackage();
	
	public InputStream getFileInputStream(String filename) throws IOException;

	public boolean isNew();

	public void setNewState(boolean b);
}
