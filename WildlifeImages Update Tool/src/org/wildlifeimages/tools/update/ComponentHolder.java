package org.wildlifeimages.tools.update;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipInputStream;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;

import org.wildlifeimages.android.wildlifeimages.Exhibit.Alias;
import org.wildlifeimages.android.wildlifeimages.ExhibitGroup;

public class ComponentHolder implements ChangeListener, ActionListener{
	final JButton newTagButton = new JButton("Add Content to Exhibit");
	final JButton newExhibitButton = new JButton("Create New Exhibit");
	final JButton saveButton = new JButton("Save Updates");
	final JButton newFileButton = new JButton("Add file to project");
	final JButton newImageButton = new JButton("Add Photo to Exhibit");
	final JButton loadPackageButton = new JButton("Load Package");
	final JButton addGroupButton = new JButton("New Group");
	final JButton addGroupExhibitButton = new JButton("Add Exhibit To Group");
	final JButton removeGroupExhibitButton = new JButton("Remove Exhibit From Group");
	final JButton removeGroupButton = new JButton("Remove Group");
	final JButton editFileButton = new JButton("View/Edit File");
	final JButton loadUpdateButton = new JButton("Load Prevous Update");

	final JList exhibitNameList = new JList();
	final JList contentList = new JList();
	final JList exhibitPhotosList = new JList();
	final JList modifiedFilesList = new JList();
	final JList groupNameList = new JList();
	final JList groupExhibitsList = new JList();
	final JList exhibitAliasesList = new JList();

	final JPanel contentPanel = new JPanel(new GridLayout(1,2));
	final JPanel mainPanel = new JPanel(new GridLayout(3, 1, 2, 5));
	final JPanel exhibitDataPanel = new JPanel(new GridLayout(2,4));
	final JPanel groupPanel = new JPanel(new GridLayout(4,1));
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

	final NumberSpinner exhibitXSpinnerModel = new NumberSpinner();
	final NumberSpinner exhibitYSpinnerModel = new NumberSpinner();
	final NumberSpinner aliasXSpinnerModel = new NumberSpinner();
	final NumberSpinner aliasYSpinnerModel = new NumberSpinner();
	final NumberSpinner groupXSpinnerModel = new NumberSpinner();
	final NumberSpinner groupYSpinnerModel = new NumberSpinner();

	final JSpinner exhibitXCoordField = new JSpinner(exhibitXSpinnerModel);
	final JSpinner exhibitYCoordField = new JSpinner(exhibitYSpinnerModel);
	final JSpinner aliasXCoordField = new JSpinner(aliasXSpinnerModel);
	final JSpinner aliasYCoordField = new JSpinner(aliasYSpinnerModel);
	final JSpinner groupXCoordField = new JSpinner(groupXSpinnerModel);
	final JSpinner groupYCoordField = new JSpinner(groupYSpinnerModel);

	final ModifiedListModel modifiedFilesListModel;
	final ContentListModel contentListModel;
	final ExhibitPhotosModel exhibitPhotosModel;
	final ExhibitListModel exhibitNameModel;
	final GroupListModel groupListModel;
	final GroupExhibitsModel groupExhibitsModel;
	final ExhibitAliasesModel exhibitAliasesModel;

	final JTabbedPane tabbedPane = new JTabbedPane();

	final JPanel subPanel1 = new JPanel(new GridLayout(1, 2, 2, 5));
	final JPanel subPanel2 = new JPanel(new GridLayout(2, 1, 2, 5));
	final JPanel subPanel3 = new JPanel(new GridLayout(2, 1, 2, 5));
	final JPanel subPanel4 = new JPanel(new GridLayout(1, 2, 2, 5));
	final JPanel subPanel5 = new JPanel(new GridLayout(2, 3));

	final ZipManager peer;

	public ComponentHolder(ZipManager manager){
		peer = manager;
		mapPanel = new JMapPanel(new GridLayout(1,1,0,0), peer.getMapDimension(), peer);
		modifiedFilesListModel = new ModifiedListModel();
		contentListModel = new ContentListModel();
		exhibitPhotosModel = new ExhibitPhotosModel();
		exhibitNameModel = new ExhibitListModel();
		groupListModel = new GroupListModel();
		groupExhibitsModel = new GroupExhibitsModel();
		exhibitAliasesModel = new ExhibitAliasesModel();
	}

	public void init(){		
		exhibitNameList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		exhibitNameList.setModel(exhibitNameModel);
		exhibitNameList.addListSelectionListener(new ListSelectionListener(){
			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				JList src = (JList)arg0.getSource();
				selectExhibit(src.getSelectedIndex());
				selectPhoto();
				selectAlias();
			}
		});

		contentList.setModel(contentListModel);
		contentList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		contentList.addListSelectionListener(new ListSelectionListener(){
			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				JList src = (JList)arg0.getSource();
				selectContent(src.getSelectedValue().toString());
			}
		});

		groupExhibitsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		groupExhibitsList.setModel(groupExhibitsModel);

		exhibitAliasesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		exhibitAliasesList.setModel(exhibitAliasesModel);
		exhibitAliasesList.addListSelectionListener(new ListSelectionListener(){
			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				selectAlias();
			}
		});

		groupNameList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		groupNameList.setModel(groupListModel);
		groupNameList.addListSelectionListener(new ListSelectionListener(){
			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				JList src = (JList)arg0.getSource();
				ExhibitGroup group = peer.getGroup(src.getSelectedValue().toString());
				groupExhibitsModel.notifyChange();
				groupExhibitsList.setSelectionInterval(0, 0);
				groupXSpinnerModel.setValue(group.xPos);
				groupYSpinnerModel.setValue(group.yPos);
			}
		});

		for (ExhibitInfo e : peer.getExhibits()){
			if (false == e.getName().equals(peer.getCurrentExhibit().getName())){
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
		loadPackageButton.addActionListener(peer);

		addGroupExhibitButton.addActionListener(this);
		removeGroupExhibitButton.addActionListener(this);
		addGroupButton.addActionListener(this);
		editFileButton.addActionListener(this);
		removeGroupButton.addActionListener(this);
		loadUpdateButton.addActionListener(this);

		addGroupExhibitButton.setBackground(Color.WHITE);
		removeGroupExhibitButton.setBackground(Color.WHITE);
		addGroupButton.setBackground(Color.WHITE);
		editFileButton.setBackground(Color.WHITE);
		removeGroupButton.setBackground(Color.WHITE);
		loadUpdateButton.setBackground(Color.WHITE);

		exhibitXSpinnerModel.addChangeListener(this);
		exhibitYSpinnerModel.addChangeListener(this);

		aliasXSpinnerModel.addChangeListener(this);
		aliasYSpinnerModel.addChangeListener(this);

		groupXSpinnerModel.addChangeListener(this);
		groupYSpinnerModel.addChangeListener(this);

		newContentDropdown.setEditable(false);
		newContentDropdown.addItem(" ");

		for (String s : peer.originalFiles){
			if (false == peer.modifiedFileExists(s)){
				newContentDropdown.addItem(s);
			}
		}
		newContentDropdown.addActionListener(peer);

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
		groupNameList.setSelectionInterval(0, 0);
		exhibitAliasesList.setSelectionInterval(0, 0);

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
		newButtonsPanel.add(loadPackageButton);

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

		JPanel aliasPanel = new JPanel(new GridLayout(1,2));
		JPanel aliasDataPanel = new JPanel(new GridLayout(2,1));
		aliasDataPanel.add(aliasXCoordField);
		aliasDataPanel.add(aliasYCoordField);

		aliasPanel.add(new JScrollPane(exhibitAliasesList));
		aliasPanel.add(aliasDataPanel);

		JPanel groupDataPanel = new JPanel(new GridLayout(2,2));
		groupDataPanel.add(groupXCoordField);
		groupDataPanel.add(groupYCoordField);
		groupDataPanel.add(removeGroupButton);
		groupDataPanel.add(removeGroupExhibitButton);

		subPanel1.add(listPanel);
		subPanel1.add(new JScrollPane(exhibitPhotosImage));
		subPanel2.add(exhibitDataPanel);
		subPanel2.add(contentPanel);

		subPanel3.add(aliasPanel);
		subPanel3.add(newButtonsPanel);

		mainPanel.add(subPanel1);
		mainPanel.add(subPanel2);
		mainPanel.add(subPanel3);

		mainPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		mainPanel.setBackground(Color.WHITE);

		subPanel4.add(new JScrollPane(groupNameList));
		subPanel4.add(new JScrollPane(groupExhibitsList));

		subPanel5.add(addGroupButton);
		subPanel5.add(addGroupExhibitButton);
		subPanel5.add(editFileButton);
		subPanel5.add(loadUpdateButton);

		groupPanel.add(subPanel4);
		groupPanel.add(groupDataPanel);
		groupPanel.add(new JScrollPane(modifiedFilesList)); //Add original files list above this?
		groupPanel.add(subPanel5);

		tabbedPane.addTab("Exhibit Stuff", mainPanel);
		tabbedPane.addTab("Map", mapPanel);
		tabbedPane.addTab("Groups", groupPanel);
	}

	void selectAlias(){
		int index = exhibitAliasesList.getSelectedIndex();
		if (index < peer.getCurrentExhibit().getAliases().length){
			aliasXCoordField.setVisible(true);
			aliasYCoordField.setVisible(true);
			Alias alias = peer.getCurrentExhibit().getAliases()[index];
			aliasXCoordField.getModel().setValue(alias.xPos);
			aliasYCoordField.getModel().setValue(alias.yPos);
		}else{
			aliasXCoordField.setVisible(false);
			aliasYCoordField.setVisible(false);
		}
	}

	void selectContent(String tag){
		peer.setCurrentTag(tag);
		ExhibitInfo e = peer.getCurrentExhibit();
		String data = e.getContent(tag);
		exhibitContentLabel.setText(e.getOrigContents(tag));
		newContentDropdown.setSelectedItem(data);
	}

	void selectExhibit(int index){
		ExhibitInfo e = peer.getExhibits().get(index);

		contentListModel.notifyChange();
		exhibitPhotosModel.notifyChange();
		contentList.setSelectionInterval(0, 0);
		for (ListSelectionListener l : contentList.getListSelectionListeners()){
			l.valueChanged(new ListSelectionEvent(contentList, 0, 0, false));
		}
		exhibitXCoordField.getModel().setValue(e.getX());
		exhibitXCoordOrig.setText("X coordinate: (was " + e.origXCoord + ")");
		exhibitYCoordField.getModel().setValue(e.getY());
		exhibitYCoordOrig.setText("Y coordinate: (was " + e.origYCoord + ")");

		exhibitNextDropdown.setSelectedItem(e.getNext());
		if (e.getNext() != null){
			exhibitNextOrig.setText("Next: (was " + e.origNext+")");
		}else{
			exhibitPreviousOrig.setText("Next:");
		}
		exhibitPreviousDropdown.setSelectedItem(e.getPrevious());
		if (e.getPrevious() != null){
			exhibitPreviousOrig.setText("Previous: (was " + e.origPrevious+")");
		}else{
			exhibitPreviousOrig.setText("Previous:");
		}

		exhibitPhotosList.setSelectionInterval(0, 0);
		exhibitAliasesList.setSelectionInterval(0, 0);

		exhibitAliasesModel.notifyChange();
	}

	class ModifiedListModel extends BasicListModel{
		@Override
		public Object getElementAt(int index) {
			String[] keys = peer.getModifiedFileNames();
			File f = peer.getModifiedFile(keys[index]);
			return keys[index] + " - " + f.getPath();
		}

		@Override
		public int getSize() {
			return peer.getModifiedFileNames().length;
		}
	}

	void selectPhoto(){
		int index = exhibitPhotosList.getSelectedIndex();
		String shortUrl = peer.getCurrentExhibit().getPhotos()[index];

		if (peer.modifiedFileExists(shortUrl)){
			exhibitPhotosImage.setImage(peer.getModifiedFile(shortUrl));
		}else{
			exhibitPhotosImage.setImage(shortUrl, peer.getFileInputStream("assets/" + shortUrl));
		}
	}

	class ExhibitListModel extends BasicListModel{
		@Override
		public Object getElementAt(int index) {
			return peer.getExhibits().get(index).getName();
		}

		@Override
		public int getSize() {
			return peer.getExhibits().size();
		}
	}

	class GroupListModel extends BasicListModel{
		@Override
		public Object getElementAt(int index) {
			String[] names = peer.getGroupNames();
			if (index >= getSize()){
				return "";
			}
			return names[index];
		}

		@Override
		public int getSize() {
			return peer.getGroupNames().length;
		}
	}


	class ExhibitAliasesModel extends BasicListModel{
		@Override
		public Object getElementAt(int index) {
			return peer.getCurrentExhibit().getAliases()[index].name;
		}

		@Override
		public int getSize() {
			return peer.getCurrentExhibit().getAliases().length;
		}
	}

	class GroupExhibitsModel extends BasicListModel{
		@Override
		public Object getElementAt(int index) {
			return peer.getGroup(groupNameList.getSelectedValue().toString()).exhibits[index];
		}

		@Override
		public int getSize() {
			ExhibitGroup group = peer.getGroup(groupNameList.getSelectedValue().toString());
			if (group == null){
				return 0;
			}
			return group.exhibits.length;
		}
	}

	class ContentListModel extends BasicListModel{
		@Override
		public Object getElementAt(int index) {
			if (this.getSize() > 0){
				return peer.getCurrentExhibit().getTag(index);
			}else{
				return "";
			}
		}

		@Override
		public int getSize() {
			return peer.getCurrentExhibit().getTagCount();
		}
	}

	class ExhibitPhotosModel extends BasicListModel{
		@Override
		public Object getElementAt(int index) {
			return peer.getCurrentExhibit().getPhotos()[index];
		}

		@Override
		public int getSize() {
			return peer.getCurrentExhibit().getPhotos().length;
		}
	}

	@Override
	public void stateChanged(ChangeEvent arg0) {
		ExhibitInfo e = peer.getCurrentExhibit();
		if (arg0.getSource().equals(exhibitXCoordField.getModel())){
			int val = Integer.parseInt(exhibitXCoordField.getModel().getValue().toString());
			e.setCoords(val, e.getY());
			if (e.origXCoord != e.getX()){
				peer.makeChange();
			}
		}else if (arg0.getSource().equals(exhibitYCoordField.getModel())){
			int val = Integer.parseInt(exhibitYCoordField.getModel().getValue().toString());
			e.setCoords(e.getX(), val);
			if (e.origYCoord != e.getY()){
				peer.makeChange();
			}
		}else if (arg0.getSource().equals(aliasXCoordField.getModel())){
			int val = Integer.parseInt(aliasXCoordField.getModel().getValue().toString());
			int index = exhibitAliasesList.getSelectedIndex();
			Alias alias = e.getAliases()[index];
			if (alias.xPos != val){
				e.addAlias(alias.name, val, alias.yPos);
				peer.makeChange();
			}
		}else if (arg0.getSource().equals(aliasYCoordField.getModel())){
			int val = Integer.parseInt(aliasYCoordField.getModel().getValue().toString());
			int index = exhibitAliasesList.getSelectedIndex();
			Alias alias = e.getAliases()[index];
			if (alias.yPos != val){
				e.addAlias(alias.name, alias.xPos, val);
				peer.makeChange();
			}

		}else if (arg0.getSource().equals(groupXCoordField.getModel())){
			int val = Integer.parseInt(groupXCoordField.getModel().getValue().toString());
			String name = groupNameList.getSelectedValue().toString();
			ExhibitGroup group = peer.getGroup(name);
			if (val != group.xPos){
				peer.addGroup(name, new ExhibitGroup(group.exhibits, val, group.yPos));
				peer.makeChange();
			}
		}else if (arg0.getSource().equals(groupYCoordField.getModel())){
			int val = Integer.parseInt(groupYCoordField.getModel().getValue().toString());
			String name = groupNameList.getSelectedValue().toString();
			ExhibitGroup group = peer.getGroup(name);
			if (val != group.yPos){
				peer.addGroup(name, new ExhibitGroup(group.exhibits,group.xPos, val));
				peer.makeChange();
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource().equals(addGroupButton)){
			String newName = JOptionPane.showInputDialog("Name of new exhibit:");
			if (newName != null && newName.length() > 0){//TODO add name expression matching
				ExhibitGroup group = new ExhibitGroup(new String[0], -1, -1);
				peer.addGroup(newName, group);
				groupListModel.notifyChange();
				peer.makeChange();
			}
		}else if (arg0.getSource().equals(addGroupExhibitButton)){
			ArrayList<ExhibitInfo> filesList = new ArrayList<ExhibitInfo>();
			for(ExhibitInfo e : peer.getExhibits()){
				filesList.add(e);
			}
			String groupName = groupNameList.getSelectedValue().toString();
			ExhibitGroup group = peer.getGroup(groupName);

			for (String name : group.exhibits){
				for (int i=0; i<filesList.size(); i++){
					if (filesList.get(i).getName().equals(name)){
						filesList.remove(i);
					}
				}
			}
			Object[] files = filesList.toArray();

			ExhibitInfo e = (ExhibitInfo)JOptionPane.showInputDialog(peer, "Exhibit:", "Add which exhibit to group?",JOptionPane.PLAIN_MESSAGE,null, files,files[0]);
			if (e != null){
				String[] names = new String[group.exhibits.length + 1];
				for(int i=0; i<names.length -1; i++){
					names[i] = group.exhibits[i];
				}
				names[names.length-1] = e.getName();
				ExhibitGroup newGroup = new ExhibitGroup(names, -1, -1);
				peer.addGroup(groupName, newGroup);
				peer.makeChange();
				groupExhibitsModel.notifyChange();
			}
		}else if (arg0.getSource().equals(editFileButton)){
			String[] keys = peer.getModifiedFileNames();
			File f = peer.getModifiedFile(keys[modifiedFilesList.getSelectedIndex()]);
			String[] exec = {"cmd.exe", "/C", f.getPath()};
			try {
				Runtime.getRuntime().exec(exec);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else if (arg0.getSource().equals(removeGroupExhibitButton)){
			peer.makeChange();
			peer.removeGroupExhibit(groupExhibitsList.getSelectedValue().toString(), groupNameList.getSelectedValue().toString());
			groupExhibitsModel.notifyChange();
			groupExhibitsList.setSelectionInterval(0, 0);
		}else if (arg0.getSource().equals(removeGroupButton)){
			peer.makeChange();
			peer.removeGroup(groupNameList.getSelectedValue().toString());
			groupNameList.setSelectionInterval(0, 0);
			groupExhibitsList.setSelectionInterval(0, 0);
			groupExhibitsModel.notifyChange();
			groupListModel.notifyChange();	
		}
		else if (arg0.getSource().equals(loadUpdateButton)){
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
						ExhibitLoader updateParser = peer.packageLoader.readUpdate(new ZipInputStream(new FileInputStream(chooser.getSelectedFile())), modFiles);
						for (String s : modFiles){
							System.out.println(s);
						}
					}catch(IOException e){
						System.out.println("Could not load");
					}
				}
			}
		}
	}
}
