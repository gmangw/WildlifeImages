package org.wildlifeimages.tools.update;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
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
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class ZipManager extends JFrame implements ActionListener{

	private static final long serialVersionUID = 1L;

	private static final Pattern filenamePattern = Pattern.compile("[a-zA-Z0-9_\\-\\.]+(/[a-zA-Z0-9_\\-\\.]+)*");

	private static final String assetPath = "assets/";

	private final Hashtable<String, File> modifiedFiles = new Hashtable<String, File>();

	private String[] originalFiles;

	private final JPanel mainPanel = new JPanel(new GridLayout(6, 1));

	private final JButton newTagButton = new JButton("Add Content");
	private final JButton newExhibitButton = new JButton("New Exhibit");

	private final JButton saveButton = new JButton("Save");
	private final JList exhibitNameList = new JList();
	private final JList contentList = new JList();

	private final JPanel listPanel = new JPanel(new GridLayout(1,2));

	private final JPanel exhibitInfoPanel = new JPanel(new GridLayout(1,2));
	private final JLabel exhibitContentLabel = new JLabel();
	private final JComboBox newContentDropdown = new JComboBox();

	private final JList exhibitPhotosList = new JList();
	private final JImage exhibitPhotosImage = new JImage();
	private final JButton newImageButton = new JButton("Add Photo");

	private final JList modifiedFilesList = new JList();

	private final JButton newFileButton = new JButton("Load new file");
	private final JTextField newFileNameField = new JTextField();

	private ExhibitParser exhibitParser = null;

	private final JPanel exhibitDataPanel = new JPanel(new GridLayout(2,2));
	private final JSpinner exhibitXCoordField = new JSpinner(new NumberSpinner("x"));
	private final JSpinner exhibitYCoordField = new JSpinner(new NumberSpinner("y"));
	private final JComboBox exhibitNextDropdown = new JComboBox();
	private final JComboBox exhibitPreviousDropdown = new JComboBox();

	private final ContentListModel contentListModel = new ContentListModel();
	private final ModifiedListModel modifiedFilesListModel = new ModifiedListModel();
	private final ExhibitPhotosModel exhibitPhotosModel = new ExhibitPhotosModel();

	private final String EXHIBITSFILENAME = "exhibits.xml";

	private ExhibitInfo currentExhibit;
	private String currentTag;

	public static void main(String[] args){
		ZipManager zr = new ZipManager();
		zr.setVisible(true);
	}

	public ZipManager(){
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		saveButton.addActionListener(this);
		saveButton.setSize(100, 20);

		try {
			originalFiles = this.readAPK("WildlifeImages.apk");
		} catch (XmlPullParserException e) {
			originalFiles = null;
			e.printStackTrace();
		}

		currentExhibit = exhibitParser.getExhibits().get(0);

		exhibitNameList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		exhibitNameList.setModel(new ExhibitListModel());

		exhibitNameList.addListSelectionListener(new ListSelectionListener(){
			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				currentExhibit = exhibitParser.getExhibits().get(exhibitNameList.getSelectedIndex());

				contentListModel.notifyChange();
				exhibitPhotosModel.notifyChange();
				contentList.setSelectionInterval(0, 0);
				for (ListSelectionListener l : contentList.getListSelectionListeners()){
					l.valueChanged(new ListSelectionEvent(contentList, 0, 0, false));
				}
				exhibitXCoordField.setValue(currentExhibit.getxCoord());
				exhibitYCoordField.setValue(currentExhibit.getyCoord());				

				exhibitNextDropdown.setSelectedItem(currentExhibit.getNext());
				exhibitPreviousDropdown.setSelectedItem(currentExhibit.getPrevious());
				
				exhibitPhotosList.setSelectionInterval(0, 0);
				photoSelected();
			}
		});

		for (ExhibitInfo e : exhibitParser.getExhibits()){
			if (false == e.getName().equals(currentExhibit.getName())){
				exhibitPreviousDropdown.addItem(e.getName());
				exhibitNextDropdown.addItem(e.getName());
			}
		}

		contentList.setModel(contentListModel);
		contentList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		contentList.addListSelectionListener(new ListSelectionListener(){
			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				currentTag = (String)contentList.getSelectedValue();
				ExhibitInfo e = currentExhibit;
				String tag = (String)currentTag;
				String data = e.getContents(tag);
				exhibitContentLabel.setText(e.getOrigContents(tag));
				newContentDropdown.setSelectedItem(data);
			}
		});

		listPanel.add(new JScrollPane(exhibitNameList));
		listPanel.add(new JScrollPane(contentList));

		newContentDropdown.setEditable(false);
		newContentDropdown.addItem(" ");
		for (String s : originalFiles){
			if (false == modifiedFiles.containsKey(s)){
				newContentDropdown.addItem(s);
			}
		}

		newContentDropdown.addActionListener(this);

		newFileButton.addActionListener(this);

		newFileNameField.addKeyListener(new KeyListener(){
			@Override
			public void keyPressed(KeyEvent arg0) {
			}

			@Override
			public void keyReleased(KeyEvent arg0) {
				if (filenamePattern.matcher(newFileNameField.getText()).matches()){
					newFileButton.setEnabled(true);
				}else{
					newFileButton.setEnabled(false);
				}
			}

			@Override
			public void keyTyped(KeyEvent arg0) {	
			}
		});

		exhibitNextDropdown.addActionListener(this);
		exhibitPreviousDropdown.addActionListener(this);

		exhibitInfoPanel.add(exhibitContentLabel);
		exhibitInfoPanel.add(newContentDropdown);

		exhibitPhotosList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		exhibitPhotosList.setModel(exhibitPhotosModel);
		exhibitPhotosList.addListSelectionListener(new ListSelectionListener(){
			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				photoSelected();
			}
		});

		exhibitNameList.setSelectionInterval(0, 0);
		contentList.setSelectionInterval(0, 0);
		exhibitPhotosList.setSelectionInterval(0, 0);

		modifiedFilesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		modifiedFilesList.setModel(modifiedFilesListModel);
		modifiedFilesList.setSelectionInterval(0, 0);

		exhibitDataPanel.add(exhibitXCoordField);
		exhibitDataPanel.add(exhibitPreviousDropdown);
		exhibitDataPanel.add(exhibitYCoordField);
		exhibitDataPanel.add(exhibitNextDropdown);

		JPanel newButtonsPanel = new JPanel(new GridLayout(1,5));

		newExhibitButton.addActionListener(this);
		newTagButton.addActionListener(this);
		newButtonsPanel.add(newExhibitButton);
		newButtonsPanel.add(newTagButton);
		newButtonsPanel.add(newImageButton);
		newButtonsPanel.add(newFileButton);
		newButtonsPanel.add(saveButton);

		JPanel exhibitPhotosPanel = new JPanel(new GridLayout(1, 2));
		exhibitPhotosPanel.add(exhibitPhotosList);
		exhibitPhotosPanel.add(exhibitPhotosImage);

		mainPanel.add(newButtonsPanel);
		mainPanel.add(listPanel);
		mainPanel.add(exhibitInfoPanel);
		mainPanel.add(exhibitDataPanel);
		mainPanel.add(exhibitPhotosPanel);
		mainPanel.add(new JScrollPane(modifiedFilesList));

		this.setSize(720, 640);
		this.setLayout(new GridLayout(1,1));
		this.add(mainPanel);
	}
	
	private void photoSelected(){
		int index = exhibitPhotosList.getSelectedIndex();
		String shortUrl = currentExhibit.getPhotos()[index];
		try{
			if (modifiedFiles.containsKey(shortUrl)){
				exhibitPhotosImage.setImage(modifiedFiles.get(shortUrl));
			}else{
				exhibitPhotosImage.setImage(shortUrl, new ZipFile("WildlifeImages.apk"));
			}
		}catch(IOException e){
			System.out.println("Error loading " + shortUrl);
		}
	}

	public void addFile(String filename, File newFile){
		modifiedFiles.put(filename, newFile);
		modifiedFilesListModel.notifyChange();
		newContentDropdown.addItem(filename);
	}

	private class NumberSpinner implements SpinnerModel{
		private int val = 0;
		private final String type;
		ArrayList<ChangeListener> listeners = new ArrayList<ChangeListener>();

		public NumberSpinner(String spinnerType) {
			type = spinnerType;
		}
		@Override
		public void addChangeListener(ChangeListener arg0) {
			listeners.add(arg0);
		}
		@Override
		public Object getNextValue() {
			return ++val;
		}
		@Override
		public Object getPreviousValue() {
			return --val;
		}
		@Override
		public Object getValue() {
			return val;
		}
		@Override
		public void removeChangeListener(ChangeListener arg0) {
			listeners.remove(arg0);
		}
		@Override
		public void setValue(Object arg0) {
			try{
				val = Integer.parseInt(arg0.toString());
				for (ChangeListener l : listeners){
					if (type.equals("x")){
						currentExhibit.setxCoord(val);
					}else if (type.equals("y")){
						currentExhibit.setyCoord(val);
					}
					l.stateChanged(new ChangeEvent(this));
				}
			}catch (NumberFormatException e){};
		}
	}

	public String[] readAPK(String outFilename) throws XmlPullParserException{
		try {
			ZipFile zf = new ZipFile(outFilename);

			ArrayList<String> files = new ArrayList<String>();
			for (Enumeration<? extends ZipEntry> entries = zf.entries(); entries.hasMoreElements();) {
				ZipEntry item = entries.nextElement();
				String zipEntryName = (item).getName();
				if (false == item.isDirectory() && zipEntryName.startsWith(assetPath)){
					String shortUrl = zipEntryName.substring(assetPath.length());
					files.add(shortUrl);
					if (shortUrl.equals(EXHIBITSFILENAME)){						
						InputStream stream = zf.getInputStream(item);

						XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
						XmlPullParser xmlBox = factory.newPullParser();
						BufferedReader in = new BufferedReader(new InputStreamReader(stream));

						xmlBox.setInput(in);
						System.out.println("Creating parser");
						exhibitParser = new ExhibitParser(xmlBox);
						stream.close();
					}
				}
			}
			zf.close();
			return files.toArray(new String[files.size()]);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
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
					System.out.println("Error: Cannot read new file " + modifiedFiles.get(key)); //TODO
				}
			}
			out.putNextEntry(new ZipEntry("exhibits.xml"));

			exhibitParser.writeExhibits(out);

			out.closeEntry();

			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getSource().equals(saveButton)){
			JFileChooser chooser = new JFileChooser();
			chooser.setFileFilter(new FileFilter(){
				@Override
				public boolean accept(File f) {
					if (f.getName().endsWith(".zip")){
						return true;
					}
					return false;
				}
				@Override
				public String getDescription() {
					return "Zip files (*.zip)";
				}
			});
			chooser.setDialogTitle("Select output zip file name.");
			if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION){
				this.saveFile(chooser.getSelectedFile());
			}
		}else if (event.getSource().equals(newFileButton)){
			JFileChooser chooser = new JFileChooser();
			chooser.setDialogTitle("Select new file to add.");
			if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
				this.saveFile(chooser.getSelectedFile());
				String newName = JOptionPane.showInputDialog("Local name for new file (such as ExhibitContents/bear.html):");
				if (newName != null){ //TODO format check
					addFile(newName, chooser.getSelectedFile());
				}
			}
		}else if (event.getSource().equals(newContentDropdown)){
			currentExhibit.addContent(currentTag, (String)newContentDropdown.getSelectedItem());
		}else if (event.getSource().equals(exhibitPreviousDropdown)){
			String prev = (String)exhibitPreviousDropdown.getSelectedItem();
			if (prev != null){
				currentExhibit.setPrevious(prev);
			}
		}else if (event.getSource().equals(exhibitNextDropdown)){
			String next = (String)exhibitNextDropdown.getSelectedItem();
			if (next != null){
				currentExhibit.setNext(next);
			}
		}else if (event.getSource().equals(newTagButton)){
			String newName = JOptionPane.showInputDialog("Name of new content:"); //TODO
			if (newName != null){ //TODO format check
				currentExhibit.addContent(newName, originalFiles[0]);
				contentListModel.notifyChange();
			}
		}else if (event.getSource().equals(newExhibitButton)){
			String newName = JOptionPane.showInputDialog("Name of new exhibit:");
			if (newName != null){ //TODO format check
				String newContentName = JOptionPane.showInputDialog("Name of first new content:");
				if (newContentName != null){ //TODO format check
					ExhibitInfo newE = new ExhibitInfo(newName, 0, 0, null, null);
					newE.addContent(newContentName, originalFiles[0]);
					exhibitParser.getExhibits().add(newE);
					exhibitNextDropdown.addItem(newName);
					exhibitPreviousDropdown.addItem(newName);
					((ExhibitListModel)exhibitNameList.getModel()).notifyChange();
					contentListModel.notifyChange();
				}
			}
		}
	}

	private class ExhibitListModel implements ListModel{
		ArrayList<ListDataListener> listeners = new ArrayList<ListDataListener>();

		@Override
		public void addListDataListener(ListDataListener newListener) {
			listeners.add(newListener);
		}

		@Override
		public Object getElementAt(int index) {
			return exhibitParser.getExhibits().get(index).getName();
		}

		@Override
		public int getSize() {
			return exhibitParser.getExhibits().size();
		}

		@Override
		public void removeListDataListener(ListDataListener oldListener) {
			listeners.remove(oldListener);
		}

		public void notifyChange(){
			for (ListDataListener listener : listeners){
				listener.contentsChanged(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, this.getSize()));
			}
		}
	}

	private class ContentListModel implements ListModel{
		ArrayList<ListDataListener> listeners = new ArrayList<ListDataListener>();

		@Override
		public void addListDataListener(ListDataListener newListener) {
			listeners.add(newListener);
		}

		@Override
		public Object getElementAt(int index) {
			if (this.getSize() > 0){
				return currentExhibit.getTag(index);
			}else{
				return "";
			}
		}

		@Override
		public int getSize() {
			return currentExhibit.getTagCount();
		}

		@Override
		public void removeListDataListener(ListDataListener oldListener) {
			listeners.remove(oldListener);
		}

		public void notifyChange(){
			for (ListDataListener listener : listeners){
				listener.contentsChanged(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, this.getSize()));
			}
		}
	}

	private class ModifiedListModel implements ListModel{
		ArrayList<ListDataListener> listeners = new ArrayList<ListDataListener>();

		@Override
		public void addListDataListener(ListDataListener newListener) {
			listeners.add(newListener);
		}

		@Override
		public Object getElementAt(int index) {
			Enumeration<String> keys = modifiedFiles.keys();
			String element = keys.nextElement();
			for (int i=0; i<index; i++){
				element = keys.nextElement();
			}
			File f = modifiedFiles.get(element);
			return element + " - " + f.getPath();
		}

		@Override
		public int getSize() {
			return modifiedFiles.size();
		}

		@Override
		public void removeListDataListener(ListDataListener oldListener) {
			listeners.remove(oldListener);
		}

		public void notifyChange(){
			for (ListDataListener listener : listeners){
				listener.contentsChanged(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, this.getSize()));
			}
		}
	}

	private class ExhibitPhotosModel implements ListModel{
		ArrayList<ListDataListener> listeners = new ArrayList<ListDataListener>();

		@Override
		public void addListDataListener(ListDataListener newListener) {
			listeners.add(newListener);
		}

		@Override
		public Object getElementAt(int index) {
			return currentExhibit.getPhotos()[index];
		}

		@Override
		public int getSize() {
			return currentExhibit.getPhotos().length;
		}

		@Override
		public void removeListDataListener(ListDataListener oldListener) {
			listeners.remove(oldListener);
		}

		public void notifyChange(){
			for (ListDataListener listener : listeners){
				listener.contentsChanged(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, this.getSize()));
			}
		}
	}
}