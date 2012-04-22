package org.wildlifeimages.tools.update;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import org.apache.batik.swing.JSVGCanvas;

public class ZipManager extends JFrame implements ManagerInterface, WindowListener{

	private static final long serialVersionUID = 1L;

	static final Pattern localPathPattern = Pattern.compile("[a-zA-Z0-9_\\-\\.]+(/[a-zA-Z0-9_\\-\\.]+)*");
	static final Pattern exhibitNamePattern = Pattern.compile("[a-zA-Z0-9_'?,]*");
	private static final Pattern newFileNamePattern = Pattern.compile("[a-zA-Z0-9\\.]+(/[a-zA-Z0-9\\.])*");

	private final Hashtable<String, File> modifiedFiles = new Hashtable<String, File>();

	private JSVGCanvas map;

	private final Dimension mapDimension = new Dimension();

	private ExhibitLoader exhibitParser = null;

	private final PackageLoader packageLoader;

	private String[] originalFiles;

	private final ComponentHolder c;

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

		c = new ComponentHolder(this, mapDimension);
		c.init();

		this.setSize(640, 480);
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

		JMenuBar menuBar = new JMenuBar();
		JMenu menu = new JMenu("Menu");
		menuBar.add(menu);
		JMenuItem menuItem = new JMenuItem("Load newer app version");
		menuItem.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				loadPackage();
			}
		});
		menu.add(menuItem);
		menuItem = new JMenuItem("Save updates to file");
		menuItem.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				saveUpdate();
			}
		});
		menu.add(menuItem);
		menuItem = new JMenuItem("Exit");
		menuItem.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(null, "Are you sure you want to exit?", "Quit?", JOptionPane.OK_CANCEL_OPTION)){
					System.exit(0);
				}
			}
		});
		menu.add(menuItem);
		this.setJMenuBar(menuBar);
		this.addWindowListener(this);
		this.setVisible(true);
	}

	public void makeChange(){
		this.setTitle("Update Tool*");
	}
	
	public String[] getOriginalFiles(){
		return Arrays.copyOf(originalFiles, originalFiles.length);
	}

	public ExhibitLoader getLoader(){
		return exhibitParser;
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

	public InputStream getFileInputStream(String filename){
		try{
			return new BufferedInputStream(packageLoader.getFileInputStream(filename));
		}catch(IOException e){
			showError(e.getMessage());
			return null;
		}
	}

	public static boolean isImage(String filename){
		String s = filename.substring(Math.max(0, filename.length()-5));
		return (s.toLowerCase().endsWith("jpg") || s.endsWith("bmp") || s.endsWith("jpeg") || s.endsWith("png") || s.endsWith("gif") );
	}

	public void loadUpdate(){
		JFileChooser chooser = new JFileChooser("../");
		chooser.setFileFilter(new FileFilter(){
			@Override
			public boolean accept(File f) {
				if (f.isDirectory() || f.getName().endsWith(".zip")){
					return true;
				}
				return false;
			}
			@Override
			public String getDescription() {
				return "Update Packages (*.zip)";
			}
		});
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setDialogTitle("Select update file");
		if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
			if (chooser.getSelectedFile().exists()){
				try{
					System.out.println(chooser.getSelectedFile().getName());
					ArrayList<String> modFiles = new ArrayList<String>();
					ExhibitLoader updateParser = packageLoader.readUpdate(new ZipInputStream(new FileInputStream(chooser.getSelectedFile())), modFiles);
					for (String s : modFiles){
						System.out.println(s);
					}
				}catch(IOException e){
					System.out.println("Could not load");
				}
			}
		}
	}

	void loadPackage(){
		int result = JOptionPane.showConfirmDialog(null, "This will discard any unsaved changes. Continue?", "Confirm Package Load", JOptionPane.YES_NO_OPTION);
		if (result == JOptionPane.YES_OPTION){
			if (true == packageLoader.loadNewPackage()){
				WindowEvent windowClosing = new WindowEvent(this, WindowEvent.WINDOW_CLOSING);
				this.dispatchEvent(windowClosing);
			}
		}
	}

	void saveUpdate(){
		JFileChooser chooser = new JFileChooser("../");
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setDialogTitle("Select a location for the new update file.");
		if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION){
			Calendar now = Calendar.getInstance();
			String filename = String.format("update_%1$tY%1$tm%1$td%1$tH%1$tM.zip", now);
			File f = new File(chooser.getSelectedFile(), filename);
			this.saveFile(f);
		}
	}

	public void addFile(){
		JFileChooser chooser = new JFileChooser("../");
		chooser.setDialogTitle("Select new file to add.");
		if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
			if (chooser.getSelectedFile().exists()){
				String newName = JOptionPane.showInputDialog("Local name for new file (such as ExhibitContents/bear.html):", chooser.getSelectedFile().getName());
				if (newName != null){
					if (newFileNamePattern.matcher(newName).matches()){
						modifiedFiles.put(newName, chooser.getSelectedFile());
						makeChange();
					}else{
						showError("Error: invalid filename. Spaces and special characters are not allowed.");
					}
				}
			}
		}
	}

	@Override
	public void windowActivated(WindowEvent arg0) {
	}

	@Override
	public void windowClosed(WindowEvent arg0) {
		File f = new File("./");
		for (File tmp : f.listFiles()){
			if (tmp.getName().startsWith(".tmp")){
				tmp.delete();
			}
		}
	}

	@Override
	public void windowClosing(WindowEvent arg0) {
	}

	@Override
	public void windowDeactivated(WindowEvent arg0) {
	}

	@Override
	public void windowDeiconified(WindowEvent arg0) {
	}

	@Override
	public void windowIconified(WindowEvent arg0) {
	}

	@Override
	public void windowOpened(WindowEvent arg0) {
	}
}