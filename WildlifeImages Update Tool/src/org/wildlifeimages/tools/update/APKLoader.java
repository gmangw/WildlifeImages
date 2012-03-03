package org.wildlifeimages.tools.update;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.zip.ZipInputStream;

public class APKLoader {
	private File apkFile = null;
	private boolean isNew = false;

	public ZipInputStream getAPKStream(){
		if (apkFile != null){
			try {
				System.out.println("Loading from " + apkFile.getPath());
				return new ZipInputStream(new FileInputStream(apkFile));
			} catch (FileNotFoundException e) {}
		}
		System.out.println("Loading from resource");
		return new ZipInputStream(getClass().getResourceAsStream("/resources/WildlifeImages.apk"));
	}

	public void setFile(File file) {
		apkFile = file;
	}

	public File getFile() {
		return apkFile;
	}
	
	public void setNewState(boolean validity){
		isNew = validity;
	}
	
	public boolean isNew(){
		return isNew;
	}
}
