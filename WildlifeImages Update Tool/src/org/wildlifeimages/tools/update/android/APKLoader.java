package org.wildlifeimages.tools.update.android;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.wildlifeimages.android.wildlifeimages.Parser;
import org.wildlifeimages.android.wildlifeimages.Parser.Event;
import org.wildlifeimages.tools.update.ExhibitLoader;
import org.wildlifeimages.tools.update.PackageLoader;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class APKLoader implements PackageLoader{
	private static final String EXHIBITSFILENAME = "exhibits.xml";
	private static final String EVENTSFILENAME = "events.xml";
	private static final String ASSETPATH = "assets/";

	private File apkFile = null;
	private boolean isNew = false;

	private ZipInputStream getPackageStream(){
		if (apkFile != null){
			try {
				return new ZipInputStream(new FileInputStream(apkFile));
			} catch (FileNotFoundException e) {}
		}
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

	public ExhibitLoader readUpdate(ZipInputStream zf, ArrayList<String> modFiles) throws IOException{
		ExhibitLoader loader = null;

		for (ZipEntry item = zf.getNextEntry(); item != null; item = zf.getNextEntry()){
			String zipEntryName = item.getName();
			if (false == item.isDirectory()){
				String shortUrl = zipEntryName;
				modFiles.add(shortUrl);
				if (shortUrl.equals(EXHIBITSFILENAME)){						
					InputStream stream = zf;
					try{
						XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
						XmlPullParser xmlBox = factory.newPullParser();
						BufferedReader in = new BufferedReader(new InputStreamReader(stream));

						xmlBox.setInput(in);
						System.out.println("Creating parser");
						loader = new ExhibitLoader(xmlBox);
					}catch(XmlPullParserException e){
						throw new IOException("Error parsing package." , e);
					}
				}
			}
		}
		zf.close();
		return loader;
	}
	
	public ExhibitLoader readPackage(ArrayList<String> origFiles) throws IOException{
		ZipInputStream zf = getPackageStream();
		ExhibitLoader loader = null;

		for (ZipEntry item = zf.getNextEntry(); item != null; item = zf.getNextEntry()){
			String zipEntryName = item.getName();
			if (false == item.isDirectory() && zipEntryName.startsWith(ASSETPATH)){
				String shortUrl = zipEntryName.substring(ASSETPATH.length());
				origFiles.add(shortUrl);
				if (shortUrl.equals(EXHIBITSFILENAME)){						
					InputStream stream = zf;
					try{
						XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
						XmlPullParser xmlBox = factory.newPullParser();
						BufferedReader in = new BufferedReader(new InputStreamReader(stream));

						xmlBox.setInput(in);
						System.out.println("Creating parser");
						loader = new ExhibitLoader(xmlBox);
					}catch(XmlPullParserException e){
						throw new IOException("Error parsing package." , e);
					}
				}
			}
		}
		zf.close();
		return loader;
	}

	public boolean loadNewPackage(){
		JFileChooser chooser = new JFileChooser("./");
		chooser.setFileFilter(new FileFilter(){
			@Override
			public boolean accept(File f) {
				if (f.isDirectory() || f.getName().endsWith(".apk")){
					return true;
				}
				return false;
			}
			@Override
			public String getDescription() {
				return "Android Applications (*.apk)";
			}
		});
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setDialogTitle("Select package file");
		if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
			if (chooser.getSelectedFile().exists()){
				setFile(chooser.getSelectedFile());
				setNewState(true);
				return true;
			}
		}
		return false;
	}

	public InputStream getFileInputStream(String filename)throws IOException{
		ZipEntry entry;
		ZipInputStream stream = getPackageStream();
		for (entry = stream.getNextEntry(); entry != null; entry = stream.getNextEntry()){
			if (entry.getName().equals(filename)){
				break;
			}
			entry = null;
		}
		if (entry == null){
			throw new IOException("Error: Could not load " + filename);
		}
		return stream;
	}
	
	public Event[] loadEvents(){
		try{
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlPullParser xmlBox = factory.newPullParser();
			InputStream istr = getFileInputStream(ASSETPATH+EVENTSFILENAME);
			BufferedReader in = new BufferedReader(new InputStreamReader(istr), 1024);
			xmlBox.setInput(in);
			return Parser.parseEvents(xmlBox);
		}catch(XmlPullParserException e){
			return new Event[0];
		}catch(IOException e){
			return new Event[0];
		}
	}
}
