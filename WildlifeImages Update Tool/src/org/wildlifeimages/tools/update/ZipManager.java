package org.wildlifeimages.tools.update;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.batik.swing.JSVGCanvas;
import org.wildlifeimages.android.wildlifeimages.ExhibitGroup;

public class ZipManager extends JFrame implements ActionListener{

	private static final long serialVersionUID = 1L;

	private static final Pattern localPathPattern = Pattern.compile("[a-zA-Z0-9_\\-\\.]+(/[a-zA-Z0-9_\\-\\.]+)*");
	private static final Pattern exhibitNamePattern = Pattern.compile("[a-zA-Z0-9_'?,]*");
	private static final Pattern newFileNamePattern = Pattern.compile("[a-zA-Z0-9\\.]+(/[a-zA-Z0-9\\.])*");

	private final Hashtable<String, File> modifiedFiles = new Hashtable<String, File>();

	private JSVGCanvas map;

	private final Dimension mapDimension = new Dimension();

	ExhibitLoader exhibitParser = null;

	final PackageLoader packageLoader;

	String[] originalFiles;
	String currentTag;

	final ComponentHolder c;

	public ZipManager(PackageLoader loader){	
		packageLoader = loader;
		
		this.setTitle("Update Tool");
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		modifiedFiles.clear();

		try {
			ArrayList<String> files = new ArrayList<String>();
			exhibitParser = packageLoader.readPackage(files);
			originalFiles = files.toArray(new String[files.size()]);
		} catch (Exception e) {
			showError("Could not load original package.");
			c = null;
			return;
		}

		c = new ComponentHolder(this);
		c.init();
		
		this.setSize(720, 640);
		this.setLayout(new GridLayout(1,1));
		this.add(c.tabbedPane);
		
		JSVGCanvas newMap = null;
		try{
			newMap = JMapPanel.getMapCanvas(mapDimension, packageLoader.getFileInputStream("res/raw/map.svg"));
		}catch(IOException e){
			newMap = new JSVGCanvas();
		}finally{
			map = newMap;
			map.addMouseMotionListener(c.mapPanel);
		}
		c.mapPanel.add(map);
		c.tabbedPane.setSelectedIndex(1);
		c.tabbedPane.setSelectedIndex(0);
		this.setVisible(true);
	}

	Dimension getMapDimension(){
		return mapDimension;
	}
	
	void makeChange(){
		this.setTitle("Update Tool*");
	}

	public void addFile(String filename, File newFile){
		modifiedFiles.put(filename, newFile);
		c.modifiedFilesListModel.notifyChange();
		c.newContentDropdown.addItem(filename);
		makeChange();
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
			
			File outPage = new File(outFile.getParent(), "update.html");
			String contents = "<html>\n<a href=\"http://www.wildlifeimages.org\">Homepage</a>\n<!-- http://oregonstate.edu/~wilkinsg/wildlifeimages/" + outFile.getName() + " -->\n</html>";
			
			FileOutputStream pageStream = new FileOutputStream(outPage);
			pageStream.write(contents.getBytes());
			pageStream.close();
			
			for(ExhibitInfo e : exhibitParser.getExhibits()){
				File output = new File(e.getName()+".png");
				CreateQR.writeExhibitQR(e.getName(), output);
			}

			this.setTitle("Update Tool");
		} catch (IOException e) {
			showError("Unable to save update file.");
		}
	}

	public void setCurrentTag(String tag){
		currentTag = tag;
	}

	public ExhibitInfo getCurrentExhibit(){
		String name = (String)c.exhibitNameList.getSelectedValue();
		int index = -1;
		ArrayList<ExhibitInfo> exhibitList = exhibitParser.getExhibits();
		for (int i=0; i<exhibitList.size(); i++){
			if (exhibitList.get(i).getName().equals(name)){
				index = i;
			}
		}
		if (index == -1){
			return exhibitList.get(0);
		}else{
			return exhibitList.get(index);
		}
	}

	public String[] getGroupNames(){
		return exhibitParser.getGroupNames();
	}

	public ExhibitGroup getGroup(String name){
		return exhibitParser.getGroup(name);
	}

	public boolean modifiedFileExists(String shortUrl){
		return modifiedFiles.containsKey(shortUrl);
	}

	public File getModifiedFile(String shortUrl){
		return modifiedFiles.get(shortUrl);
	}

	public String[] getModifiedFileNames(){
		return modifiedFiles.keySet().toArray(new String[0]);
	}

	public JSVGCanvas getMap(){
		return map;
	}
	
	public void removeGroup(String groupName) {
		exhibitParser.removeGroup(groupName);
	}
	
	public void removeGroupExhibit(String exhibitName, String groupName) {
		exhibitParser.removeGroupExhibit(exhibitName, groupName);
	}

	public void addGroup(String name, ExhibitGroup group){
		exhibitParser.addGroup(name, group.exhibits, group.xPos, group.yPos);
	}

	public InputStream getFileInputStream(String filename){
		try{
			return packageLoader.getFileInputStream(filename);
		}catch(IOException e){
			showError(e.getMessage());
			return null;
		}
	}

	public ArrayList<ExhibitInfo> getExhibits(){
		return exhibitParser.getExhibits();
	}
	
	public static boolean isImage(String filename){
		final Pattern imageExtensionExpression = Pattern.compile(".+(.jpg|.jpeg|.bmp|.png|.gif)");
		return imageExtensionExpression.matcher(filename.toLowerCase()).matches();
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
				this.saveFile(f);
			}
		}else if (event.getSource().equals(c.newFileButton)){
			JFileChooser chooser = new JFileChooser("../");
			chooser.setDialogTitle("Select new file to add.");
			if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
				if (chooser.getSelectedFile().exists()){
					String newName = JOptionPane.showInputDialog("Local name for new file (such as ExhibitContents/bear.html):");
					if (newName != null){
						if (newFileNamePattern.matcher(newName).matches()){
							addFile(newName, chooser.getSelectedFile());
						}else{
							showError("Error: invalid filename. Spaces and special characters are not allowed.");
						}
					}
				}
			}
		}else if (event.getSource().equals(c.newContentDropdown)){
			ExhibitInfo e = getCurrentExhibit();
			e.setContent(currentTag, (String)c.newContentDropdown.getSelectedItem());
			if (false == e.getContent(currentTag).equals(e.getOrigContents(currentTag))){
				makeChange();
			}
		}else if (event.getSource().equals(c.exhibitPreviousDropdown)){
			String prev = (String)c.exhibitPreviousDropdown.getSelectedItem();
			if (prev != null){
				getCurrentExhibit().setPrevious(prev);
				if (getCurrentExhibit().origPrevious == null || false == getCurrentExhibit().origPrevious.equals(prev)){
					makeChange();
				}
			}
		}else if (event.getSource().equals(c.exhibitNextDropdown)){
			String next = (String)c.exhibitNextDropdown.getSelectedItem();
			if (next != null){
				getCurrentExhibit().setNext(next);
				if (getCurrentExhibit().origNext == null || false == getCurrentExhibit().origNext.equals(next)){
					makeChange();
				}
			}
		}else if (event.getSource().equals(c.newTagButton)){
			String newName = JOptionPane.showInputDialog("Name of new content:");
			if (newName != null && localPathPattern.matcher(newName).matches()){
				getCurrentExhibit().setContent(newName, originalFiles[0]);
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
					if (isImage(file)){
						fileList.add(file);
					}
				}
			}
			for (String file : modifiedFiles.keySet()){
				if (isImage(file)){
					fileList.add(file);
				}
			}
			Object[] files = fileList.toArray();
			String s = (String)JOptionPane.showInputDialog(this, "File to use:", "New Photo",JOptionPane.PLAIN_MESSAGE,null, files,files[0]);

			if ((s != null) && (s.length() > 0)) {
				getCurrentExhibit().addPhoto(s);
				c.exhibitPhotosModel.notifyChange();
				makeChange();
			}
		}else if (event.getSource().equals(c.loadPackageButton)){
			int result = JOptionPane.showConfirmDialog(null, "This will discard any unsaved changes. Continue?", "Confirm Package Load", JOptionPane.YES_NO_OPTION);
			if (result == JOptionPane.YES_OPTION){
				if (true == packageLoader.loadNewPackage()){
					WindowEvent windowClosing = new WindowEvent(this, WindowEvent.WINDOW_CLOSING);
					this.dispatchEvent(windowClosing);
				}
			}
		}
	}
}