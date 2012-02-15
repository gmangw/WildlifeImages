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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
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

	private final JPanel mainPanel = new JPanel(new GridLayout(4, 1));

	private final JButton saveButton = new JButton("Save");
	private final JList exhibitNameList = new JList();
	private final JList contentList = new JList();

	private final JPanel listPanel = new JPanel(new GridLayout(1,2, 5, 5));

	private final JPanel exhibitInfoPanel = new JPanel(new GridLayout(1,3));
	private final JLabel exhibitContentLabel = new JLabel();
	private final JComboBox newContentLabel = new JComboBox();


	private final JList modifiedFilesList = new JList();

	private final JPanel newFilePanel = new JPanel(new GridLayout(2,1));
	private final JButton newFileButton = new JButton("Load new file");
	private final JTextField newFileNameField = new JTextField();

	private ExhibitParser exhibitParser = null;

	private final ContentListModel contentListModel = new ContentListModel();
	private final ModifiedListModel modifiedFilesListModel = new ModifiedListModel();

	private final String EXHIBITSFILENAME = "exhibits.xml";

	private ExhibitParser.ExhibitInfo currentExhibit;
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

		exhibitNameList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		exhibitNameList.setModel(new ExhibitListModel());

		exhibitNameList.addListSelectionListener(new ListSelectionListener(){
			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				currentExhibit = exhibitParser.getExhibits().get(exhibitNameList.getSelectedIndex());

				contentListModel.notifyChange();
				contentList.setSelectionInterval(0, 0);
				for (ListSelectionListener l : contentList.getListSelectionListeners()){
					l.valueChanged(new ListSelectionEvent(contentList, 0, 0, false));
				}
			}
		});

		contentList.setModel(contentListModel);
		contentList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		contentList.addListSelectionListener(new ListSelectionListener(){
			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				currentTag = (String)contentList.getSelectedValue();
				ExhibitParser.ExhibitInfo e = currentExhibit;
				String tag = (String)currentTag;
				String data = e.getContents(tag);
				exhibitContentLabel.setText(e.getOrigContents(tag));
				newContentLabel.setSelectedItem(data);
			}
		});

		listPanel.add(new JScrollPane(exhibitNameList));
		listPanel.add(new JScrollPane(contentList));

		newContentLabel.setEditable(false);
		newContentLabel.addItem(" ");
		for (String s : originalFiles){
			if (false == modifiedFiles.containsKey(s)){
				newContentLabel.addItem(s);
			}
		}
		for (String s : modifiedFiles.keySet()){
			newContentLabel.addItem(s);
		}

		newContentLabel.addActionListener(this);

		exhibitNameList.setSelectionInterval(0, 0);
		contentList.setSelectionInterval(0, 0);

		newFileButton.addActionListener(this);
		newFileButton.setEnabled(false);

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

		newFilePanel.add(newFileNameField);
		newFilePanel.add(newFileButton);

		exhibitInfoPanel.add(exhibitContentLabel);
		exhibitInfoPanel.add(newContentLabel);
		exhibitInfoPanel.add(newFilePanel);

		modifiedFilesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		//modifiedFilesListModel = new ModifiedListModel();
		modifiedFilesList.setModel(modifiedFilesListModel);
		modifiedFilesList.setSelectionInterval(0, 0);

		mainPanel.add(listPanel);
		mainPanel.add(exhibitInfoPanel);
		mainPanel.add(new JScrollPane(modifiedFilesList));
		mainPanel.add(saveButton);

		this.setSize(720, 480);
		this.setLayout(new GridLayout(1,1));
		this.add(mainPanel);
	}

	public void addFile(String filename, File newFile){
		modifiedFiles.put(filename, newFile);
		modifiedFilesListModel.notifyChange();
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
			if (modifiedFiles.size() > 0){
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
			}
		}else if (event.getSource().equals(newFileButton)){
			JFileChooser chooser = new JFileChooser();
			chooser.setDialogTitle("Select output zip file name.");
			if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
				addFile(newFileNameField.getText(), chooser.getSelectedFile());
			}
		}else if (event.getSource().equals(newContentLabel)){
			currentExhibit.addContent(currentTag, (String)newContentLabel.getSelectedItem());
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
}