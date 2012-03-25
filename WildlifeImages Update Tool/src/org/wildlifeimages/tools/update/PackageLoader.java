package org.wildlifeimages.tools.update;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipInputStream;

public interface PackageLoader {

	ZipInputStream getPackageStream();

	ExhibitLoader readPackage(ArrayList<String> files) throws IOException;

	public boolean loadNewPackage();
}
