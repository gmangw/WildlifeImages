package org.wildlifeimages.tools.update;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;

import org.apache.batik.swing.JSVGCanvas;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;


public class ZipManager extends JFrame implements ActionListener{

	private static final long serialVersionUID = 1L;

	private static final Pattern localPathPattern = Pattern.compile("[a-zA-Z0-9_\\-\\.]+(/[a-zA-Z0-9_\\-\\.]+)*");
	private static final Pattern exhibitNamePattern = Pattern.compile("[a-zA-Z0-9_'?,]*");
	private static final Pattern newFileNamePattern = Pattern.compile("[a-zA-Z0-9\\.]+(/[a-zA-Z0-9\\.])*");

	private final String EXHIBITSFILENAME = "exhibits.xml";

	private static final String assetPath = "assets/";

	final Hashtable<String, File> modifiedFiles = new Hashtable<String, File>();

	final JSVGCanvas map;

	final Dimension mapDimension = new Dimension();

	ExhibitLoader exhibitParser = null;

	private final APKLoader apkLoader;

	String[] originalFiles;
	ExhibitInfo currentExhibit;
	private String currentTag;

	final ComponentHolder c;

	public ZipManager(APKLoader loader){
		apkLoader = loader;
		map = JMapPanel.getMapCanvas(mapDimension, apkLoader.getAPKStream());
		this.setTitle("Update Tool");
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		modifiedFiles.clear();

		try {
			ArrayList<String> files = new ArrayList<String>();
			exhibitParser = this.readAPK(apkLoader.getAPKStream(), files);
			originalFiles = files.toArray(new String[files.size()]);
		} catch (XmlPullParserException e) {
			showError("Could not load APK file.");
			c = null;
			return;
		}

		currentExhibit = exhibitParser.getExhibits().get(0);

		c = new ComponentHolder(this);
		c.init();

		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Exhibit Stuff", c.mainPanel);
		tabbedPane.addTab("Map", c.mapPanel);
		tabbedPane.addTab("Groups and Aliases", c.groupPanel);

		this.setSize(720, 640);
		this.setLayout(new GridLayout(1,1));
		this.add(tabbedPane);
	}

	void makeChange(){
		this.setTitle("Update Tool*");
	}

	void selectExhibit(){
		currentExhibit = exhibitParser.getExhibits().get(c.exhibitNameList.getSelectedIndex());

		c.contentListModel.notifyChange();
		c.exhibitPhotosModel.notifyChange();
		c.contentList.setSelectionInterval(0, 0);
		for (ListSelectionListener l : c.contentList.getListSelectionListeners()){
			l.valueChanged(new ListSelectionEvent(c.contentList, 0, 0, false));
		}
		c.exhibitXCoordField.getModel().setValue(currentExhibit.getX());
		c.exhibitXCoordOrig.setText("X coordinate: (was " + currentExhibit.origXCoord + ")");
		c.exhibitYCoordField.getModel().setValue(currentExhibit.getY());
		c.exhibitYCoordOrig.setText("Y coordinate: (was " + currentExhibit.origYCoord + ")");

		c.exhibitNextDropdown.setSelectedItem(currentExhibit.getNext());
		if (currentExhibit.getNext() != null){
			c.exhibitNextOrig.setText("Next: (was " + currentExhibit.origNext+")");
		}else{
			c.exhibitPreviousOrig.setText("Next:");
		}
		c.exhibitPreviousDropdown.setSelectedItem(currentExhibit.getPrevious());
		if (currentExhibit.getPrevious() != null){
			c.exhibitPreviousOrig.setText("Previous: (was " + currentExhibit.origPrevious+")");
		}else{
			c.exhibitPreviousOrig.setText("Previous:");
		}

		c.exhibitPhotosList.setSelectionInterval(0, 0);
		selectPhoto();
	}

	void selectPhoto(){
		int index = c.exhibitPhotosList.getSelectedIndex();
		String shortUrl = currentExhibit.getPhotos()[index];

		if (modifiedFiles.containsKey(shortUrl)){
			c.exhibitPhotosImage.setImage(modifiedFiles.get(shortUrl));
		}else{
			c.exhibitPhotosImage.setImage(shortUrl, apkLoader.getAPKStream());
		}
	}

	void selectContent(){
		currentTag = (String)c.contentList.getSelectedValue();
		ExhibitInfo e = currentExhibit;
		String tag = (String)currentTag;
		String data = e.getContent(tag);
		c.exhibitContentLabel.setText(e.getOrigContents(tag));
		c.newContentDropdown.setSelectedItem(data);
	}

	public void addFile(String filename, File newFile){
		modifiedFiles.put(filename, newFile);
		c.modifiedFilesListModel.notifyChange();
		c.newContentDropdown.addItem(filename);
		makeChange();
	}

	public ExhibitLoader readAPK(ZipInputStream zf, ArrayList<String> files) throws XmlPullParserException{
		ExhibitLoader loader = null;
		try {
			for (ZipEntry item = zf.getNextEntry(); item != null; item = zf.getNextEntry()){
				String zipEntryName = (item).getName();
				if (false == item.isDirectory() && zipEntryName.startsWith(assetPath)){
					String shortUrl = zipEntryName.substring(assetPath.length());
					files.add(shortUrl);
					if (shortUrl.equals(EXHIBITSFILENAME)){						
						InputStream stream = zf;

						XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
						XmlPullParser xmlBox = factory.newPullParser();
						BufferedReader in = new BufferedReader(new InputStreamReader(stream));

						xmlBox.setInput(in);
						System.out.println("Creating parser");
						loader = new ExhibitLoader(xmlBox);
					}
				}
			}
			zf.close();
			return loader;
		} catch (IOException e) {
			showError("Error: Could not load APK file.");
			return null;
		}
	}

	public void showError(String message){
		JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
	}

	public void saveFile(File outFile){
		try {
			ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outFile));

			Set<String> keys = modifiedFiles.keySet();
			for (String key : keys){
				try {
					BufferedInputStream in = new BufferedInputStream(new FileInputStream(modifiedFiles.get(key)));
					byte[] buffer = new byte[1];
					out.putNextEntry(new ZipEntry(key));
					while(in.read(buffer) > 0){
						out.write(buffer);
					}
					in.close();
					out.closeEntry();
				}catch(FileNotFoundException e){
					showError("Error: Cannot read new file " + modifiedFiles.get(key));
				}
			}
			out.putNextEntry(new ZipEntry("exhibits.xml"));

			exhibitParser.writeXML(out);

			out.closeEntry();

			out.close();

			for(ExhibitInfo e : exhibitParser.getExhibits()){
				File output = new File(e.getName()+".png");
				CreateQR.writeExhibitQR(e.getName(), output);
			}

			this.setTitle("Update Tool");
		} catch (IOException e) {
			showError("Unable to save update file.");
		}
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getSource().equals(c.saveButton)){
			JFileChooser chooser = new JFileChooser("../");
			chooser.setAcceptAllFileFilterUsed(false);
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.setDialogTitle("Select output zip file name.");
			if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION){
				Calendar now = Calendar.getInstance();
				String filename = String.format("update_%1$tY%1$tm%1$td%1$tH%1$tM.zip", now);
				File f = new File(chooser.getSelectedFile(), filename);
				System.out.println(f.getPath());
				this.saveFile(f);
			}
		}else if (event.getSource().equals(c.newFileButton)){
			JFileChooser chooser = new JFileChooser("../");
			chooser.setDialogTitle("Select new file to add.");
			if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
				if (chooser.getSelectedFile().exists()){
					this.saveFile(chooser.getSelectedFile());
					String newName = JOptionPane.showInputDialog("Local name for new file (such as ExhibitContents/bear.html):");
					if (newName != null){
						if (newFileNamePattern.matcher(newName).matches()){
							addFile(newName, chooser.getSelectedFile());
						}else{
							showError("Error: invalid filename.");
						}
					}
				}
			}
		}else if (event.getSource().equals(c.newContentDropdown)){
			currentExhibit.setContent(currentTag, (String)c.newContentDropdown.getSelectedItem());
			if (false == currentExhibit.getContent(currentTag).equals(currentExhibit.getOrigContents(currentTag))){
				makeChange();
			}
		}else if (event.getSource().equals(c.exhibitPreviousDropdown)){
			String prev = (String)c.exhibitPreviousDropdown.getSelectedItem();
			if (prev != null){
				currentExhibit.setPrevious(prev);
				if (false == currentExhibit.origPrevious.equals(prev)){
					makeChange();
				}
			}
		}else if (event.getSource().equals(c.exhibitNextDropdown)){
			String next = (String)c.exhibitNextDropdown.getSelectedItem();
			if (next != null){
				currentExhibit.setNext(next);
				if (false == currentExhibit.origNext.equals(next)){
					makeChange();
				}
			}
		}else if (event.getSource().equals(c.newTagButton)){
			String newName = JOptionPane.showInputDialog("Name of new content:");
			if (newName != null && localPathPattern.matcher(newName).matches()){
				currentExhibit.setContent(newName, originalFiles[0]);
				c.contentListModel.notifyChange();
				makeChange();
			}else{
				System.out.println("Invalid tag name " + newName);
			}
		}else if (event.getSource().equals(c.newExhibitButton)){
			String newName = JOptionPane.showInputDialog("Name of new exhibit:");
			if (newName != null && exhibitNamePattern.matcher(newName).matches()){
				String newContentName = JOptionPane.showInputDialog("Name of first new content:");
				if (newContentName != null && localPathPattern.matcher(newContentName).matches()){
					ExhibitInfo newE = new ExhibitInfo(newName, 0, 0, null, null);
					newE.setContent(newContentName, originalFiles[0]);
					exhibitParser.getExhibits().add(newE);
					c.exhibitNextDropdown.addItem(newName);
					c.exhibitPreviousDropdown.addItem(newName);
					((ComponentHolder.ExhibitListModel)c.exhibitNameList.getModel()).notifyChange();
					c.contentListModel.notifyChange();
					makeChange();
				}else{
					System.out.println("Invalid tag name " + newContentName);
				}
			}else{
				System.out.println("Invalid exhibit name " + newName);
			}
		}else if (event.getSource().equals(c.newImageButton)){
			ArrayList<String> fileList = new ArrayList<String>();
			for (String file : originalFiles){
				if (false == modifiedFiles.containsKey(file)){
					fileList.add(file);
				}
			}
			for (String file : modifiedFiles.keySet()){
				fileList.add(file);
			}
			Object[] files = fileList.toArray();
			String s = (String)JOptionPane.showInputDialog(this, "File to use:", "New Photo",JOptionPane.PLAIN_MESSAGE,null, files,files[0]);

			if ((s != null) && (s.length() > 0)) {
				currentExhibit.addPhoto(s);
				c.exhibitPhotosModel.notifyChange();
				makeChange();
			}
		}else if (event.getSource().equals(c.loadAPKButton)){
			int result = JOptionPane.showConfirmDialog(null, "This will discard any unsaved changes. Continue?", "Confirm APK Load", JOptionPane.YES_NO_OPTION);
			if (result == JOptionPane.YES_OPTION){
				JFileChooser chooser = new JFileChooser("../");
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
				chooser.setDialogTitle("Select APK file");
				if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
					if (chooser.getSelectedFile().exists()){
						apkLoader.setFile(chooser.getSelectedFile());
						apkLoader.setNewState(true);
						WindowEvent windowClosing = new WindowEvent(this, WindowEvent.WINDOW_CLOSING);
						this.dispatchEvent(windowClosing);
					}
				}
			}
			
		}
	}
	
}