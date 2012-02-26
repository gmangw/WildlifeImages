package org.wildlifeimages.tools.update;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.swing.BorderFactory;
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
import javax.swing.JTabbedPane;
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

import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.util.SVGConstants;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.svg.SVGDocument;
import org.wildlifeimages.tools.update.ExhibitInfo.Alias;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;


public class ZipManager extends JFrame implements ActionListener{

	private static final long serialVersionUID = 1L;
	
	private static final Pattern localPathPattern = Pattern.compile("[a-zA-Z0-9_\\-\\.]+(/[a-zA-Z0-9_\\-\\.]+)*");

	private static final Pattern exhibitNamePattern = Pattern.compile("[a-zA-Z0-9_'?,]*");

	private static final String assetPath = "assets/";

	private final Hashtable<String, File> modifiedFiles = new Hashtable<String, File>();

	private String[] originalFiles;

	private final JPanel mainPanel = new JPanel(new GridLayout(3, 1, 2, 5));

	private JSVGCanvas map;

	private final JButton newTagButton = new JButton("Add Content to Exhibit");
	private final JButton newExhibitButton = new JButton("Create New Exhibit");

	private final JButton saveButton = new JButton("Save Updates");
	private final JList exhibitNameList = new JList();
	private final JList contentList = new JList();

	private final JPanel contentPanel = new JPanel(new GridLayout(1,2));
	private final JLabel exhibitContentLabel = new JLabel();
	private final JComboBox newContentDropdown = new JComboBox();

	private final JList exhibitPhotosList = new JList();
	private final JImage exhibitPhotosImage = new JImage();
	private final JButton newImageButton = new JButton("Add Photo to Exhibit");

	private final JList modifiedFilesList = new JList();

	private final JButton newFileButton = new JButton("Add file to project");

	private ExhibitLoader exhibitParser = null;

	private final JPanel exhibitDataPanel = new JPanel(new GridLayout(2,4));
	private final JLabel exhibitXCoordOrig = new JLabel();
	private final JSpinner exhibitXCoordField = new JSpinner(new NumberSpinner("x"));
	private final JLabel exhibitYCoordOrig = new JLabel();
	private final JSpinner exhibitYCoordField = new JSpinner(new NumberSpinner("y"));
	private final JLabel exhibitNextOrig = new JLabel();
	private final JComboBox exhibitNextDropdown = new JComboBox();
	private final JLabel exhibitPreviousOrig = new JLabel();
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

	public ZipInputStream getZipStream(){
		//TODO
		return new ZipInputStream(getClass().getResourceAsStream("/resources/WildlifeImages.apk"));
	}

	public ZipManager(){
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		try {
			originalFiles = this.readAPK(getZipStream());
		} catch (XmlPullParserException e) {
			//TODO
			originalFiles = null;
		}

		final Dimension mapDimension = new Dimension();
		map = getMap(mapDimension);

		currentExhibit = exhibitParser.getExhibits().get(0);

		exhibitNameList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		exhibitNameList.setModel(new ExhibitListModel());
		exhibitNameList.addListSelectionListener(new ListSelectionListener(){
			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				selectExhibit();
			}
		});

		contentList.setModel(contentListModel);
		contentList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		contentList.addListSelectionListener(new ListSelectionListener(){
			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				selectContent();
			}
		});

		for (ExhibitInfo e : exhibitParser.getExhibits()){
			if (false == e.getName().equals(currentExhibit.getName())){
				exhibitPreviousDropdown.addItem(e.getName());
				exhibitNextDropdown.addItem(e.getName());
			}
		}
		exhibitNextDropdown.addActionListener(this);
		exhibitPreviousDropdown.addActionListener(this);

		saveButton.addActionListener(this);
		saveButton.setSize(100, 20);

		newFileButton.addActionListener(this);

		newExhibitButton.addActionListener(this);

		newTagButton.addActionListener(this);

		newImageButton.addActionListener(this);

		newContentDropdown.setEditable(false);
		newContentDropdown.addItem(" ");
		for (String s : originalFiles){
			if (false == modifiedFiles.containsKey(s)){
				newContentDropdown.addItem(s);
			}
		}
		newContentDropdown.addActionListener(this);

		exhibitPhotosList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		exhibitPhotosList.setModel(exhibitPhotosModel);
		exhibitPhotosList.addListSelectionListener(new ListSelectionListener(){
			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				selectPhoto();
			}
		});

		exhibitContentLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

		modifiedFilesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		modifiedFilesList.setModel(modifiedFilesListModel);

		modifiedFilesList.setSelectionInterval(0, 0);
		exhibitNameList.setSelectionInterval(0, 0);
		contentList.setSelectionInterval(0, 0);
		exhibitPhotosList.setSelectionInterval(0, 0);

		JPanel contentDropdownPanel = new JPanel(new GridLayout(4, 1));
		contentDropdownPanel.add(new JLabel("Original content:"));
		contentDropdownPanel.add(exhibitContentLabel);
		contentDropdownPanel.add(new JLabel("Current content:"));
		contentDropdownPanel.add(newContentDropdown);
		//contentDropdownPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

		contentPanel.add(new JScrollPane(contentList));
		contentPanel.add(contentDropdownPanel);
		contentPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

		exhibitDataPanel.add(exhibitXCoordOrig);
		exhibitDataPanel.add(exhibitXCoordField);
		exhibitDataPanel.add(exhibitPreviousOrig);
		exhibitDataPanel.add(exhibitPreviousDropdown);
		exhibitDataPanel.add(exhibitYCoordOrig);
		exhibitDataPanel.add(exhibitYCoordField);
		exhibitDataPanel.add(exhibitNextOrig);
		exhibitDataPanel.add(exhibitNextDropdown);
		exhibitDataPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

		JPanel newButtonsPanel = new JPanel(new GridLayout(2,3));
		newButtonsPanel.add(newExhibitButton);
		newButtonsPanel.add(newTagButton);
		newButtonsPanel.add(newImageButton);
		newButtonsPanel.add(newFileButton);
		newButtonsPanel.add(saveButton);
		for (Component c : newButtonsPanel.getComponents()){
			c.setBackground(Color.WHITE);
		}

		JPanel listPanelTop = new JPanel(new BorderLayout());
		listPanelTop.add(new JLabel("Exhibits:"), BorderLayout.NORTH);
		listPanelTop.add(new JScrollPane(exhibitNameList), BorderLayout.CENTER);
		JPanel listPanelBottom = new JPanel(new BorderLayout());
		listPanelBottom.add(new JLabel("Exhibit Photos:"), BorderLayout.NORTH);
		listPanelBottom.add(exhibitPhotosList, BorderLayout.CENTER);
		JPanel listPanel = new JPanel(new GridLayout(2, 1));
		listPanel.add(listPanelTop);
		listPanel.add(listPanelBottom);
		listPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

		JPanel subPanel1 = new JPanel(new GridLayout(1, 2, 2, 5));
		JPanel subPanel2 = new JPanel(new GridLayout(2, 1, 2, 5));
		JPanel subPanel3 = new JPanel(new GridLayout(2, 1, 2, 5));

		subPanel1.add(listPanel);
		subPanel1.add(new JScrollPane(exhibitPhotosImage));
		subPanel2.add(exhibitDataPanel);
		subPanel2.add(contentPanel);
		subPanel3.add(new JScrollPane(modifiedFilesList));
		subPanel3.add(newButtonsPanel);

		mainPanel.add(subPanel1);
		mainPanel.add(subPanel2);
		mainPanel.add(subPanel3);

		mainPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		mainPanel.setBackground(Color.WHITE);

		JPanel mapPanel = new JPanel(new GridLayout(1,1)){
			private static final long serialVersionUID = -2662027617645159327L;

			@Override
			public void paint(Graphics g){
				super.paint(g);
				double mapAspect = mapDimension.getWidth()/mapDimension.getHeight();
				double myAspect = 1.0*getWidth()/getHeight();
				int w;
				int h;
				int offsetX = 0;
				int offsetY = 0;


				if (myAspect > mapAspect){
					w = (int)(getWidth() * (mapAspect / myAspect));
					h = getHeight();
					offsetX = (getWidth() - w)/2;
				}else{
					w = getWidth();
					h = (int)(getHeight() * (myAspect / mapAspect));
					offsetY = (getHeight() - h)/2;
				}

				//int fontHeight = g.getFontMetrics().getHeight();

				for(ExhibitInfo e : exhibitParser.getExhibits()){
					int exhibitX = e.getxCoord();
					int exhibitY = e.getyCoord();
					if (exhibitX != -1 || exhibitY != -1){
						int x = w * exhibitX/100 + offsetX;
						int y = h * exhibitY/100 + offsetY; //+fontHeight/2; //TODO
						int stringWidth = g.getFontMetrics().stringWidth(e.getName());
						g.drawString(e.getName(), x-stringWidth/2, y);
					}
					for (Alias a : e.getAliases()){
						exhibitX = a.xPos;
						exhibitY = a.yPos;
						int x = w * exhibitX/100 + offsetX;
						int y = h * exhibitY/100 + offsetY; //+fontHeight/2; //TODO
						int stringWidth = g.getFontMetrics().stringWidth(a.name);
						g.drawString(a.name, x-stringWidth/2, y);
					}
				}
			}
		};
		mapPanel.add(map);

		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Exhibit Stuff", mainPanel);
		tabbedPane.addTab("Map", mapPanel);

		this.setSize(720, 640);
		this.setLayout(new GridLayout(1,1));
		this.add(tabbedPane);
	}

	public JSVGCanvas getMap(Dimension d){
		ZipInputStream stream = getZipStream();

		JSVGCanvas svgCanvas = new JSVGCanvas();

		try{
			ZipEntry entry;
			for (entry = stream.getNextEntry(); entry != null; entry = stream.getNextEntry()){
				if (entry.getName().equals("res/raw/map.svg")){
					break;
				}
				entry = null;
			}
			if (entry == null){
				throw new IOException("Could not load map.svg");
			}


			String parser = XMLResourceDescriptor.getXMLParserClassName();
			SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
			SVGDocument doc = (SVGDocument)f.createDocument("myURI", stream);

			String widthString = doc.getDocumentElement().getAttribute(SVGConstants.SVG_WIDTH_ATTRIBUTE);
			String heightString = doc.getDocumentElement().getAttribute(SVGConstants.SVG_HEIGHT_ATTRIBUTE);
			int width = Integer.parseInt(widthString.substring(0, widthString.length()-3));
			int height = Integer.parseInt(heightString.substring(0, heightString.length()-3));

			d.setSize(width, height);

			svgCanvas.setSVGDocument(doc);

			stream.close();
		}catch(IOException e){
			//TODO
		}

		return svgCanvas;
	}

	private void selectExhibit(){
		currentExhibit = exhibitParser.getExhibits().get(exhibitNameList.getSelectedIndex());

		contentListModel.notifyChange();
		exhibitPhotosModel.notifyChange();
		contentList.setSelectionInterval(0, 0);
		for (ListSelectionListener l : contentList.getListSelectionListeners()){
			l.valueChanged(new ListSelectionEvent(contentList, 0, 0, false));
		}
		exhibitXCoordField.setValue(currentExhibit.getxCoord());
		exhibitXCoordOrig.setText("X coordinate: (was " + currentExhibit.origXCoord + ")");
		exhibitYCoordField.setValue(currentExhibit.getyCoord());
		exhibitYCoordOrig.setText("Y coordinate: (was " + currentExhibit.origYCoord + ")");

		exhibitNextDropdown.setSelectedItem(currentExhibit.getNext());
		if (currentExhibit.getNext() != null){
			exhibitNextOrig.setText("Next: (was " + currentExhibit.origNext+")");
		}else{
			exhibitPreviousOrig.setText("Next:");
		}
		exhibitPreviousDropdown.setSelectedItem(currentExhibit.getPrevious());
		if (currentExhibit.getPrevious() != null){
			exhibitPreviousOrig.setText("Previous: (was " + currentExhibit.origPrevious+")");
		}else{
			exhibitPreviousOrig.setText("Previous:");
		}

		exhibitPhotosList.setSelectionInterval(0, 0);
		selectPhoto();
	}

	private void selectPhoto(){
		int index = exhibitPhotosList.getSelectedIndex();
		String shortUrl = currentExhibit.getPhotos()[index];

		if (modifiedFiles.containsKey(shortUrl)){
			exhibitPhotosImage.setImage(modifiedFiles.get(shortUrl));
		}else{
			exhibitPhotosImage.setImage(shortUrl, getZipStream());
		}
	}

	private void selectContent(){
		currentTag = (String)contentList.getSelectedValue();
		ExhibitInfo e = currentExhibit;
		String tag = (String)currentTag;
		String data = e.getContents(tag);
		exhibitContentLabel.setText(e.getOrigContents(tag));
		newContentDropdown.setSelectedItem(data);
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

	public String[] readAPK(ZipInputStream zf) throws XmlPullParserException{
		try {

			ArrayList<String> files = new ArrayList<String>();
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
						exhibitParser = new ExhibitLoader(xmlBox);
					}
				}
			}
			zf.close();
			return files.toArray(new String[files.size()]);
		} catch (IOException e) {
			e.printStackTrace();
			//TODO
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
					//TODO
					System.out.println("Error: Cannot read new file " + modifiedFiles.get(key));
				}
			}
			out.putNextEntry(new ZipEntry("exhibits.xml"));

			exhibitParser.writeExhibits(out);

			out.closeEntry();

			out.close();
		} catch (IOException e) {
			e.printStackTrace();
			//TODO
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
				if (chooser.getSelectedFile().exists()){
					this.saveFile(chooser.getSelectedFile());
					String newName = JOptionPane.showInputDialog("Local name for new file (such as ExhibitContents/bear.html):");
					if (newName != null){ //TODO format check
						addFile(newName, chooser.getSelectedFile());
					}
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
			if (newName != null && localPathPattern.matcher(newName).matches()){
				currentExhibit.addContent(newName, originalFiles[0]);
				contentListModel.notifyChange();
			}else{
				System.out.println("Invalid tag name " + newName);
			}
		}else if (event.getSource().equals(newExhibitButton)){
			String newName = JOptionPane.showInputDialog("Name of new exhibit:");
			if (newName != null && exhibitNamePattern.matcher(newName).matches()){
				String newContentName = JOptionPane.showInputDialog("Name of first new content:");
				if (newContentName != null && localPathPattern.matcher(newContentName).matches()){
					ExhibitInfo newE = new ExhibitInfo(newName, 0, 0, null, null);
					newE.addContent(newContentName, originalFiles[0]);
					exhibitParser.getExhibits().add(newE);
					exhibitNextDropdown.addItem(newName);
					exhibitPreviousDropdown.addItem(newName);
					((ExhibitListModel)exhibitNameList.getModel()).notifyChange();
					contentListModel.notifyChange();
				}else{
					System.out.println("Invalid tag name " + newContentName);
				}
			}else{
				System.out.println("Invalid exhibit name " + newName);
			}
		}else if (event.getSource().equals(newImageButton)){
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
				exhibitPhotosModel.notifyChange();
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