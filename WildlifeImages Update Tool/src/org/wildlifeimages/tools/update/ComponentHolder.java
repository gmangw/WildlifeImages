package org.wildlifeimages.tools.update;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class ComponentHolder implements ChangeListener{
	final JButton newTagButton = new JButton("Add Content to Exhibit");
	final JButton newExhibitButton = new JButton("Create New Exhibit");
	final JButton saveButton = new JButton("Save Updates");
	final JButton newFileButton = new JButton("Add file to project");
	final JButton newImageButton = new JButton("Add Photo to Exhibit");
	final JButton loadAPKButton = new JButton("Load APK");
	
	final JList exhibitNameList = new JList();
	final JList contentList = new JList();
	final JList exhibitPhotosList = new JList();
	final JList modifiedFilesList = new JList();
	
	final JPanel contentPanel = new JPanel(new GridLayout(1,2));
	final JPanel mainPanel = new JPanel(new GridLayout(3, 1, 2, 5));
	final JPanel exhibitDataPanel = new JPanel(new GridLayout(2,4));
	final JPanel groupPanel = new JPanel();
	final JMapPanel mapPanel;
	
	final JLabel exhibitXCoordOrig = new JLabel();
	final JLabel exhibitYCoordOrig = new JLabel();
	final JLabel exhibitContentLabel = new JLabel();
	final JLabel exhibitNextOrig = new JLabel();
	final JLabel exhibitPreviousOrig = new JLabel();
	
	final JComboBox newContentDropdown = new JComboBox();
	final JComboBox exhibitPreviousDropdown = new JComboBox();
	final JComboBox exhibitNextDropdown = new JComboBox();
	
	final JImage exhibitPhotosImage = new JImage();
	
	final NumberSpinner exhibitXSpinnerModel = new NumberSpinner("x");
	final NumberSpinner exhibitYSpinnerModel = new NumberSpinner("y");
	
	final ModifiedListModel modifiedFilesListModel;
	final ContentListModel contentListModel;
	final ExhibitPhotosModel exhibitPhotosModel;
	final ExhibitListModel exhibitNameModel;
	
	final JSpinner exhibitXCoordField = new JSpinner(exhibitXSpinnerModel);
	final JSpinner exhibitYCoordField = new JSpinner(exhibitYSpinnerModel);
	
	final ZipManager peer;
	
	public ComponentHolder(ZipManager manager){
		peer = manager;
		mapPanel = new JMapPanel(new GridLayout(1,1), peer.mapDimension, peer.exhibitParser);
		modifiedFilesListModel = new ModifiedListModel();
		contentListModel = new ContentListModel();
		exhibitPhotosModel = new ExhibitPhotosModel();
		exhibitNameModel = new ExhibitListModel();
	}
	
	public void init(){		
		exhibitNameList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		exhibitNameList.setModel(exhibitNameModel);
		exhibitNameList.addListSelectionListener(new ListSelectionListener(){
			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				peer.selectExhibit();
			}
		});

		contentList.setModel(contentListModel);
		contentList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		contentList.addListSelectionListener(new ListSelectionListener(){
			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				peer.selectContent();
			}
		});

		for (ExhibitInfo e : peer.exhibitParser.getExhibits()){
			if (false == e.getName().equals(peer.currentExhibit.getName())){
				exhibitPreviousDropdown.addItem(e.getName());
				exhibitNextDropdown.addItem(e.getName());
			}
		}
		exhibitNextDropdown.addActionListener(peer);
		exhibitPreviousDropdown.addActionListener(peer);

		saveButton.addActionListener(peer);
		saveButton.setSize(100, 20);

		newFileButton.addActionListener(peer);

		newExhibitButton.addActionListener(peer);

		newTagButton.addActionListener(peer);

		newImageButton.addActionListener(peer);
		
		loadAPKButton.addActionListener(peer);
		
		exhibitXSpinnerModel.addChangeListener(this);
		exhibitYSpinnerModel.addChangeListener(this);

		newContentDropdown.setEditable(false);
		newContentDropdown.addItem(" ");
		
		for (String s : peer.originalFiles){
			if (false == peer.modifiedFiles.containsKey(s)){
				newContentDropdown.addItem(s);
			}
		}
		newContentDropdown.addActionListener(peer);

		exhibitPhotosList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		exhibitPhotosList.setModel(exhibitPhotosModel);
		exhibitPhotosList.addListSelectionListener(new ListSelectionListener(){
			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				peer.selectPhoto();
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
		newButtonsPanel.add(loadAPKButton);
		
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

		mapPanel.add(peer.map);
	}
	
	class ModifiedListModel implements ListModel{
		ArrayList<ListDataListener> listeners = new ArrayList<ListDataListener>();
		
		@Override
		public void addListDataListener(ListDataListener newListener) {
			listeners.add(newListener);
		}

		@Override
		public Object getElementAt(int index) {
			Enumeration<String> keys = peer.modifiedFiles.keys();
			String element = keys.nextElement();
			for (int i=0; i<index; i++){
				element = keys.nextElement();
			}
			File f = peer.modifiedFiles.get(element);
			return element + " - " + f.getPath();
		}

		@Override
		public int getSize() {
			return peer.modifiedFiles.size();
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
	
	class ExhibitListModel implements ListModel{
		ArrayList<ListDataListener> listeners = new ArrayList<ListDataListener>();
		
		@Override
		public void addListDataListener(ListDataListener newListener) {
			listeners.add(newListener);
		}

		@Override
		public Object getElementAt(int index) {
			return peer.exhibitParser.getExhibits().get(index).getName();
		}

		@Override
		public int getSize() {
			return peer.exhibitParser.getExhibits().size();
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

	class ContentListModel implements ListModel{
		ArrayList<ListDataListener> listeners = new ArrayList<ListDataListener>();
		
		@Override
		public void addListDataListener(ListDataListener newListener) {
			listeners.add(newListener);
		}

		@Override
		public Object getElementAt(int index) {
			if (this.getSize() > 0){
				return peer.currentExhibit.getTag(index);
			}else{
				return "";
			}
		}

		@Override
		public int getSize() {
			return peer.currentExhibit.getTagCount();
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

	class ExhibitPhotosModel implements ListModel{
		ArrayList<ListDataListener> listeners = new ArrayList<ListDataListener>();
		
		@Override
		public void addListDataListener(ListDataListener newListener) {
			listeners.add(newListener);
		}

		@Override
		public Object getElementAt(int index) {
			return peer.currentExhibit.getPhotos()[index];
		}

		@Override
		public int getSize() {
			return peer.currentExhibit.getPhotos().length;
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
	
	@Override
	public void stateChanged(ChangeEvent arg0) {
		if (arg0.getSource().equals(exhibitXCoordField.getModel())){
			int val = Integer.parseInt(exhibitXCoordField.getModel().getValue().toString());
			peer.currentExhibit.setCoords(val, peer.currentExhibit.getY());
			if (peer.currentExhibit.origXCoord != peer.currentExhibit.getX()){
				peer.makeChange();
			}
		}else if (arg0.getSource().equals(exhibitYCoordField.getModel())){
			int val = Integer.parseInt(exhibitYCoordField.getModel().getValue().toString());
			peer.currentExhibit.setCoords(peer.currentExhibit.getX(), val);
			if (peer.currentExhibit.origYCoord != peer.currentExhibit.getY()){
				peer.makeChange();
			}
		}
	}
}
