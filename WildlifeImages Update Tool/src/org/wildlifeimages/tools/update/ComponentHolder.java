package org.wildlifeimages.tools.update;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerModel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import org.wildlifeimages.android.wildlifeimages.Exhibit.Alias;
import org.wildlifeimages.android.wildlifeimages.ExhibitGroup;
import org.wildlifeimages.android.wildlifeimages.ExhibitPhoto;
import org.wildlifeimages.android.wildlifeimages.Parser.Event;

public class ComponentHolder implements ChangeListener{
	private final JButton newTagButton = new JButton("Add Page");
	private final JButton removeTagButton = new JButton("Remove Page");
	private final JButton renameTagButton = new JButton("Rename Page");
	private final JButton newExhibitButton = new JButton("Create New Exhibit");
	private final JButton removeExhibitButton = new JButton("Remove Exhibit");
	private final JButton renameExhibitButton = new JButton("Rename Exhibit");
	private final JButton newFileButton = new JButton("Add file to project");
	private final JButton newImageButton = new JButton("Add Photo");
	private final JButton removeImageButton = new JButton("Remove Photo");
	private final JButton addGroupButton = new JButton("New Group");
	private final JButton addGroupExhibitButton = new JButton("Add Exhibit To Group");
	private final JButton removeGroupExhibitButton = new JButton("Remove Exhibit From Group");
	private final JButton removeGroupButton = new JButton("Remove Group");
	private final JButton addAliasButton = new JButton("Add Alias");
	private final JButton removeAliasButton = new JButton("Remove Alias");
	private final JButton editFileButton = new JButton("Open File");
	private final JButton viewFileButton = new JButton("View File");
	private final JButton addEventButton = new JButton("Add Event");
	private final JButton removeEventButton = new JButton("Remove Event");

	private final JList exhibitNameList = new JList();
	private final JList contentList = new JList();
	private final JList exhibitPhotosList = new JList();
	private final JList modifiedFilesList = new JList();
	private final JList groupNameList = new JList();
	private final JList groupExhibitsList = new JList();
	private final JList exhibitAliasesList = new JList();
	private final JList originalFilesList = new JList();
	private final JList eventsList = new JList();

	private final JPanel contentPanel = new JPanel(new GridLayout(1,2,2,2));
	private final JPanel mainPanel = new JPanel(new BorderLayout());
	private final JPanel exhibitDataPanel = new JPanel(new GridLayout(2,4,5,5));
	private final JPanel groupPanel = new JPanel(new GridLayout(2,1,2,2));
	private final JPanel aliasDataPanel = new JPanel(new GridLayout(2,2,2,2));

	private final JMapPanel mapPanel;

	private final JLabel exhibitXCoordOrig = new JLabel();
	private final JLabel exhibitYCoordOrig = new JLabel();
	private final JLabel exhibitContentLabel = new JLabel();
	private final JLabel exhibitNextOrig = new JLabel();
	private final JLabel exhibitPreviousOrig = new JLabel();

	private final JComboBox newContentDropdown = new JComboBox();
	private final JComboBox exhibitPreviousDropdown = new JComboBox();
	private final JComboBox exhibitNextDropdown = new JComboBox();

	private final JImage exhibitPhotosImage = new JImage();

	private final NumberSpinner exhibitXSpinnerModel = new NumberSpinner();
	private final NumberSpinner exhibitYSpinnerModel = new NumberSpinner();
	private final NumberSpinner aliasXSpinnerModel = new NumberSpinner();
	private final NumberSpinner aliasYSpinnerModel = new NumberSpinner();
	private final NumberSpinner groupXSpinnerModel = new NumberSpinner();
	private final NumberSpinner groupYSpinnerModel = new NumberSpinner();

	private final JTextArea photoCaption = new JTextArea();
	private final JTextArea eventDescription = new JTextArea();

	private final JTextField eventName = new JTextField();
	private final JSpinner eventStart;
	private final JSpinner eventEnd;

	private final JSpinner exhibitXCoordField = new JSpinner(exhibitXSpinnerModel);
	private final JSpinner exhibitYCoordField = new JSpinner(exhibitYSpinnerModel);
	private final JSpinner aliasXCoordField = new JSpinner(aliasXSpinnerModel);
	private final JSpinner aliasYCoordField = new JSpinner(aliasYSpinnerModel);
	private final JSpinner groupXCoordField = new JSpinner(groupXSpinnerModel);
	private final JSpinner groupYCoordField = new JSpinner(groupYSpinnerModel);

	private final ModifiedListModel modifiedFilesListModel;
	private final OriginalFilesListModel originalFilesListModel;
	private final ContentListModel contentListModel;
	private final ExhibitPhotosModel exhibitPhotosModel;
	private final ExhibitListModel exhibitNameModel;
	private final GroupListModel groupListModel;
	private final GroupExhibitsModel groupExhibitsModel;
	private final ExhibitAliasesModel exhibitAliasesModel;
	private final EventsListModel eventsListModel;

	final JTabbedPane tabbedPane = new JTabbedPane();
	private final JTabbedPane exhibitTabs = new JTabbedPane();

	private final HTMLEditorKit htmlKit = new HTMLEditorKit();

	private final JEditorPane htmlContentViewer = new JEditorPane();

	private final JPanel photosPanel = new JPanel(new GridLayout(1,2,2,2));
	private final JPanel groupListPanel = new JPanel(new GridLayout(1,2,2,2));
	private final JPanel filePanel = new JPanel(new GridLayout(2,1,2,2));

	private final Border lineBorder = BorderFactory.createLineBorder(Color.GRAY);
	private final Border thinPaddedBorder = BorderFactory.createEmptyBorder(2,2,2,2);
	private final Border mediumPaddedBorder = BorderFactory.createEmptyBorder(5,5,5,5);
	private final Border largePaddedBorder = BorderFactory.createEmptyBorder(10, 10, 10, 10);
	private final CompoundBorder paddedLine = BorderFactory.createCompoundBorder(lineBorder, mediumPaddedBorder);

	private final ManagerInterface peer;

	public ComponentHolder(ManagerInterface manager, Dimension mapDimension){
		peer = manager;
		mapPanel = new JMapPanel(new GridLayout(1,1,2,2), mapDimension, peer.getLoader());
		modifiedFilesListModel = new ModifiedListModel();
		originalFilesListModel = new OriginalFilesListModel();
		contentListModel = new ContentListModel();
		exhibitPhotosModel = new ExhibitPhotosModel();
		exhibitNameModel = new ExhibitListModel();
		groupListModel = new GroupListModel();
		groupExhibitsModel = new GroupExhibitsModel();
		exhibitAliasesModel = new ExhibitAliasesModel();
		eventsListModel = new EventsListModel();
		
		Calendar calendar = Calendar.getInstance();
		Date initDate = calendar.getTime();
		calendar.add(Calendar.YEAR, -100);
		Date earliestDate = calendar.getTime();
		calendar.add(Calendar.YEAR, 200);
		Date latestDate = calendar.getTime();
		SpinnerModel dateModel1 = new SpinnerDateModel(initDate, earliestDate, latestDate, Calendar.YEAR);
		SpinnerModel dateModel2 = new SpinnerDateModel(initDate, earliestDate, latestDate, Calendar.YEAR);
		eventStart = new JSpinner(dateModel1);
		eventEnd = new JSpinner(dateModel2);
		eventStart.setEditor(new JSpinner.DateEditor(eventStart, "MM/dd/yyyy"));
		eventEnd.setEditor(new JSpinner.DateEditor(eventEnd, "MM/dd/yyyy"));
	}

	public void init(){		
		exhibitNameList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		exhibitNameList.setModel(exhibitNameModel);
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
				selectGroup();
			}
		});

		eventsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		eventsList.setModel(eventsListModel);
		eventsList.addListSelectionListener(new ListSelectionListener(){
			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				Event e = eventsListModel.getEvent(eventsList.getSelectedIndex());
				eventDescription.setText(e.getDescription());
				eventName.setText(e.getTitle());
				eventStart.getModel().setValue(e.getStartDay());
				eventEnd.getModel().setValue(e.getEndDay());
			}
		});

		exhibitNextDropdown.setModel(new ExhibitDropdownModel());
		exhibitPreviousDropdown.setModel(new ExhibitDropdownModel());

		htmlContentViewer.setEditable(false);
		htmlContentViewer.setEditorKit(htmlKit);
		Document doc = htmlKit.createDefaultDocument();
		htmlContentViewer.setDocument(doc);

		eventName.getDocument().addDocumentListener(new DocumentListener(){
			private void change(){
				Event e = peer.getLoader().getEvents().get(eventsList.getSelectedIndex());
				e.setTitle(eventName.getText());
				peer.makeChange();
				eventsListModel.notifyChange();
			}
			@Override
			public void changedUpdate(DocumentEvent arg0) {
				change();
			}
			@Override
			public void insertUpdate(DocumentEvent arg0) {
				change();
			}
			@Override
			public void removeUpdate(DocumentEvent arg0) {
				change();
			}
		});

		
		eventDescription.setLineWrap(true);
		eventDescription.setWrapStyleWord(true);
		eventDescription.getDocument().addDocumentListener(new DocumentListener(){
			private void change(){
				Event e = peer.getLoader().getEvents().get(eventsList.getSelectedIndex());
				e.setDescription(eventDescription.getText());
				peer.makeChange();
			}
			@Override
			public void changedUpdate(DocumentEvent arg0) {
				change();
			}
			@Override
			public void insertUpdate(DocumentEvent arg0) {
				change();
			}
			@Override
			public void removeUpdate(DocumentEvent arg0) {
				change();
			}
		});

		photoCaption.setBorder(paddedLine);
		photoCaption.setLineWrap(true);
		photoCaption.setWrapStyleWord(true);
		photoCaption.getDocument().addDocumentListener(new DocumentListener(){
			private void change(){
				getCurrentExhibit().getPhotos()[exhibitPhotosList.getSelectedIndex()].setCaption(photoCaption.getText());
				peer.makeChange();
			}
			@Override
			public void changedUpdate(DocumentEvent arg0) {
				change();
			}
			@Override
			public void insertUpdate(DocumentEvent arg0) {
				change();
			}
			@Override
			public void removeUpdate(DocumentEvent arg0) {
				change();
			}
		});

		exhibitNextDropdown.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				setNext();
			}
		});

		exhibitPreviousDropdown.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				setPrevious();
			}
		});

		addGroupExhibitButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent a) {
				addGroupExhibit();
			}
		});

		removeGroupExhibitButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				removeGroupExhibit();
			}
		});

		addGroupButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				addGroup();
			}
		});

		editFileButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent a) {
				editFile();
			}
		});

		viewFileButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent a) {
				viewFile();
			}
		});

		removeGroupButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				removeGroup();
			}
		});

		newFileButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				peer.addFile();
				modifiedFilesListModel.notifyChange();
			}
		});

		newExhibitButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				addExhibit();
			}
		});

		removeExhibitButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				removeExhibit();
			}
		});

		newTagButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				addContent();
			}
		});

		removeTagButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				removeTag();
			}
		});

		newImageButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				addImage();
			}
		});
		
		addEventButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Event e = new Event();
				e.setTitle("New Event");
				e.setStartDay(new Date());
				peer.getLoader().getEvents().add(e);
				peer.getLoader().getEvents();
				eventsListModel.notifyChange();
				peer.makeChange();
			}
		});
		
		removeEventButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				peer.getLoader().getEvents().remove(eventsList.getSelectedIndex());
				peer.getLoader().getEvents();
				eventsListModel.notifyChange();
				peer.makeChange();
			}
		});

		exhibitXSpinnerModel.addChangeListener(this);
		exhibitYSpinnerModel.addChangeListener(this);

		aliasXSpinnerModel.addChangeListener(this);
		aliasYSpinnerModel.addChangeListener(this);

		groupXSpinnerModel.addChangeListener(this);
		groupYSpinnerModel.addChangeListener(this);
		
		eventStart.getModel().addChangeListener(this);
		eventEnd.getModel().addChangeListener(this);

		newContentDropdown.setEditable(false);
		newContentDropdown.setModel(new ContentDropdownModel());

		newContentDropdown.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				ExhibitInfo e = getCurrentExhibit();
				String currentTag = (String)contentList.getSelectedValue();
				String content = (String)newContentDropdown.getSelectedItem();
				e.setContent(currentTag, content);
				loadHTMLContent(content);
				if (false == e.getContent(currentTag).equals(e.getOrigContents(currentTag))){
					peer.makeChange();
				}
			}
		});

		exhibitPhotosList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		exhibitPhotosList.setModel(exhibitPhotosModel);
		exhibitPhotosList.addListSelectionListener(new ListSelectionListener(){
			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				selectPhoto();
			}
		});

		exhibitContentLabel.setBorder(paddedLine);

		modifiedFilesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		modifiedFilesList.setModel(modifiedFilesListModel);

		originalFilesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		originalFilesList.setModel(originalFilesListModel);

		modifiedFilesList.setSelectionInterval(0, 0);
		exhibitNameList.setSelectionInterval(0, 0);
		contentList.setSelectionInterval(0, 0);
		exhibitPhotosList.setSelectionInterval(0, 0);
		groupNameList.setSelectionInterval(0, 0);
		exhibitAliasesList.setSelectionInterval(0, 0);
		eventsList.setSelectionInterval(0, 0);

		JPanel contentDropdownPanel = new JPanel(new GridLayout(4,1,2,2));
		contentDropdownPanel.add(new JLabel("Original content:"));
		contentDropdownPanel.add(exhibitContentLabel);
		contentDropdownPanel.add(new JLabel("Current content:"));
		contentDropdownPanel.add(newContentDropdown);

		JPanel contentButtonPanel = new JPanel(new GridLayout(3,1,2,2));
		contentButtonPanel.add(newTagButton);
		contentButtonPanel.add(removeTagButton);
		contentButtonPanel.add(renameTagButton);

		for (Component c : contentButtonPanel.getComponents()){
			JButton b = (JButton)c;
			b.setBorder(BorderFactory.createCompoundBorder(b.getBorder(), mediumPaddedBorder));
		}
		
		addEventButton.setBorder(largePaddedBorder);
		removeEventButton.setBorder(largePaddedBorder);

		JPanel contentListPanel = new JPanel(new BorderLayout());
		contentListPanel.add(new JScrollPane(contentList), BorderLayout.CENTER);
		contentListPanel.add(contentButtonPanel, BorderLayout.EAST);

		JPanel contentControlPanel = new JPanel(new GridLayout(2,1,2,2));		
		contentControlPanel.add(contentListPanel);
		contentControlPanel.add(contentDropdownPanel);

		contentPanel.add(new JScrollPane(htmlContentViewer));
		contentPanel.add(contentControlPanel);
		contentPanel.setBorder(mediumPaddedBorder);

		exhibitDataPanel.add(exhibitXCoordOrig);
		exhibitDataPanel.add(exhibitXCoordField);
		exhibitDataPanel.add(exhibitPreviousOrig);
		exhibitDataPanel.add(exhibitPreviousDropdown);
		exhibitDataPanel.add(exhibitYCoordOrig);
		exhibitDataPanel.add(exhibitYCoordField);
		exhibitDataPanel.add(exhibitNextOrig);
		exhibitDataPanel.add(exhibitNextDropdown);

		JPanel exhibitButtonsPanel = new JPanel(new GridLayout(3,1,2,2));
		exhibitButtonsPanel.add(newExhibitButton);
		exhibitButtonsPanel.add(removeExhibitButton);
		exhibitButtonsPanel.add(renameExhibitButton);

		for (Component c : exhibitButtonsPanel.getComponents()){
			JButton b = (JButton)c;
			b.setBorder(BorderFactory.createCompoundBorder(b.getBorder(), mediumPaddedBorder));
		}

		JPanel listPanelTop = new JPanel(new BorderLayout());
		listPanelTop.add(new JLabel("Exhibits:"), BorderLayout.NORTH);
		listPanelTop.add(new JScrollPane(exhibitNameList), BorderLayout.CENTER);
		listPanelTop.add(exhibitButtonsPanel, BorderLayout.EAST);

		JPanel photoButtonPanel = new JPanel(new GridLayout(1,2,2,2));
		photoButtonPanel.add(newImageButton);
		photoButtonPanel.add(removeImageButton);

		JPanel photoDataPanel = new JPanel(new GridLayout(2,1,2,2));
		photoDataPanel.add(new JScrollPane(exhibitPhotosList));
		photoDataPanel.add(photoCaption);

		JPanel photoPanelRight = new JPanel(new BorderLayout());
		photoPanelRight.add(new JLabel("Exhibit Photos:"), BorderLayout.NORTH);
		photoPanelRight.add(photoDataPanel, BorderLayout.CENTER);
		photoPanelRight.add(photoButtonPanel, BorderLayout.SOUTH);

		JPanel aliasPanel = new JPanel(new GridLayout(1,3,2,2));
		aliasDataPanel.add(new JLabel("Alias X Coordinate"));
		aliasDataPanel.add(aliasXCoordField);
		aliasDataPanel.add(new JLabel("Alias Y Coordinate"));
		aliasDataPanel.add(aliasYCoordField);

		JPanel aliasDataLabeled = new JPanel(new BorderLayout());
		aliasDataLabeled.add(new JLabel("Exhibit Aliases:"), BorderLayout.NORTH);
		aliasDataLabeled.add(new JScrollPane(exhibitAliasesList), BorderLayout.CENTER);

		JPanel aliasButtonsPanel = new JPanel(new GridLayout(2,1,2,2));
		aliasButtonsPanel.add(addAliasButton);
		aliasButtonsPanel.add(removeAliasButton);

		aliasPanel.add(aliasDataLabeled);
		aliasPanel.add(aliasDataPanel);
		aliasPanel.add(aliasButtonsPanel);
		aliasPanel.setBorder(paddedLine);

		JPanel groupXCoordPanel = new JPanel(new GridLayout(1,2,2,2));
		groupXCoordPanel.add(new JLabel("Group X Coordinate"));
		groupXCoordPanel.add(groupXCoordField);

		JPanel groupYCoordPanel = new JPanel(new GridLayout(1,2,2,2));
		groupYCoordPanel.add(new JLabel("Group Y Coordinate"));
		groupYCoordPanel.add(groupYCoordField);

		JPanel groupDataButtonsPanel = new JPanel(new GridLayout(4,1,2,2));
		groupDataButtonsPanel.add(addGroupButton);
		groupDataButtonsPanel.add(removeGroupButton);
		groupDataButtonsPanel.add(addGroupExhibitButton);
		groupDataButtonsPanel.add(removeGroupExhibitButton);

		JPanel groupLocationPanel = new JPanel(new GridLayout(2,1,2,2));
		groupLocationPanel.add(groupXCoordPanel);
		groupLocationPanel.add(groupYCoordPanel);
		groupLocationPanel.setBorder(paddedLine);

		JPanel groupDataPanel = new JPanel(new GridLayout(1,2,2,2));
		groupDataPanel.add(groupLocationPanel);
		groupDataPanel.add(groupDataButtonsPanel);

		JPanel combinedDataPanel = new JPanel(new GridLayout(2,1,2,2));
		combinedDataPanel.add(exhibitDataPanel);
		combinedDataPanel.add(aliasPanel);
		combinedDataPanel.setBorder(mediumPaddedBorder);

		photosPanel.add(new JScrollPane(exhibitPhotosImage));
		photosPanel.add(photoPanelRight);
		photosPanel.setBorder(mediumPaddedBorder);

		exhibitTabs.add("Exhibit Content", contentPanel);
		exhibitTabs.add("Exhibit Photos", photosPanel);	
		exhibitTabs.add("Exhibit Properties", combinedDataPanel);

		mainPanel.add(listPanelTop, BorderLayout.NORTH);
		mainPanel.add(exhibitTabs, BorderLayout.CENTER);

		mainPanel.setBorder(mediumPaddedBorder);

		groupListPanel.add(new JScrollPane(groupNameList));
		groupListPanel.add(new JScrollPane(groupExhibitsList));

		JPanel fileButtonPanel = new JPanel(new GridLayout(2,1,2,2));
		fileButtonPanel.add(newFileButton);
		fileButtonPanel.add(editFileButton);

		for (Component c : fileButtonPanel.getComponents()){
			JButton b = (JButton)c;
			b.setBorder(BorderFactory.createCompoundBorder(b.getBorder(),largePaddedBorder));
		}

		JPanel originalListPanel = new JPanel(new BorderLayout());
		originalListPanel.add(new JLabel("Original Files:"), BorderLayout.NORTH);
		originalListPanel.add(new JScrollPane(originalFilesList), BorderLayout.CENTER);
		originalListPanel.add(viewFileButton, BorderLayout.EAST);

		JPanel modifiedListPanel = new JPanel(new BorderLayout());
		modifiedListPanel.add(new JLabel("Added Files:"), BorderLayout.NORTH);
		modifiedListPanel.add(new JScrollPane(modifiedFilesList), BorderLayout.CENTER);
		modifiedListPanel.add(fileButtonPanel, BorderLayout.EAST);

		JPanel eventsSmallDataPanel = new JPanel(new GridLayout(1,4,2,2));
		eventsSmallDataPanel.add(new JLabel("Start Date:"));
		eventsSmallDataPanel.add(eventStart);
		eventsSmallDataPanel.add(new JLabel("End Date:"));
		eventsSmallDataPanel.add(eventEnd);

		JPanel eventsDataPanel = new JPanel(new BorderLayout());
		eventsDataPanel.add(eventName, BorderLayout.NORTH);
		eventsDataPanel.add(new JScrollPane(eventDescription), BorderLayout.CENTER);
		eventsDataPanel.add(eventsSmallDataPanel, BorderLayout.SOUTH);

		JPanel eventButtonsPanel = new JPanel(new GridLayout(1,2,2,2));
		eventButtonsPanel.add(addEventButton);
		eventButtonsPanel.add(removeEventButton);
		
		JPanel eventsListPanel = new JPanel(new BorderLayout());
		eventsListPanel.add(new JScrollPane(eventsList), BorderLayout.CENTER);
		eventsListPanel.add(eventButtonsPanel, BorderLayout.SOUTH);
		
		JPanel eventsPanel = new JPanel(new GridLayout(2,1,2,2));
		eventsPanel.add(eventsListPanel);
		eventsPanel.add(eventsDataPanel);

		filePanel.add(originalListPanel);
		filePanel.add(modifiedListPanel);

		groupPanel.add(groupListPanel);
		groupPanel.add(groupDataPanel);
		groupPanel.setBorder(mediumPaddedBorder);

		tabbedPane.setBorder(thinPaddedBorder);

		tabbedPane.addTab("Exhibits", mainPanel);
		tabbedPane.addTab("Groups", groupPanel);
		tabbedPane.addTab("Map", mapPanel);
		tabbedPane.addTab("Files", filePanel);
		tabbedPane.addTab("Events", eventsPanel);
	}

	void addImage(){
		ArrayList<String> fileList = new ArrayList<String>();
		for (String file : peer.getOriginalFiles()){
			if (false == peer.modifiedFileExists(file)){
				if (ZipManager.isImage(file)){
					fileList.add(file);
				}
			}
		}
		for (String file : peer.getModifiedFileNames()){
			if (ZipManager.isImage(file)){
				fileList.add(file);
			}
		}
		Object[] files = fileList.toArray();
		String s = (String)JOptionPane.showInputDialog(null, "File to use:", "New Photo",JOptionPane.PLAIN_MESSAGE,null, files,files[0]);

		if ((s != null) && (s.length() > 0)) {
			getCurrentExhibit().addPhoto(new ExhibitPhoto(s, null));
			exhibitPhotosModel.notifyChange();
			peer.makeChange();
		}
	}

	void removeExhibit(){
		if (exhibitNameList.getModel().getSize() > 1){
			peer.getLoader().removeExhibit((String)exhibitNameList.getSelectedValue());
			exhibitNameList.setSelectionInterval(0, 0);
			peer.makeChange();
			exhibitNameModel.notifyChange();
		}
	}

	void addContent(){
		String newName = JOptionPane.showInputDialog("Name of new content:");
		if (newName != null && ZipManager.localPathPattern.matcher(newName).matches()){
			getCurrentExhibit().setContent(newName, peer.getOriginalFiles()[0]);
			contentListModel.notifyChange();
			peer.makeChange();
		}else{
			System.out.println("Invalid tag name " + newName);
		}
	}

	void removeTag(){
		if (contentList.getModel().getSize() > 1){
			for (ExhibitInfo e : peer.getLoader().getExhibits()){
				if (e.getName().equals((String)exhibitNameList.getSelectedValue())){
					e.setContent((String)contentList.getSelectedValue(), null);
				}
			}
		}
		contentListModel.notifyChange();
		contentList.setSelectionInterval(0, 0);
	}

	void addExhibit(){
		String newName = JOptionPane.showInputDialog("Name of new exhibit:");
		if (newName != null && ZipManager.exhibitNamePattern.matcher(newName).matches()){
			String newContentName = JOptionPane.showInputDialog("Name of first new content:");
			if (newContentName != null && ZipManager.localPathPattern.matcher(newContentName).matches()){
				ExhibitInfo newE = new ExhibitInfo(newName, 0, 0, null, null);
				newE.setContent(newContentName, peer.getOriginalFiles()[0]);
				peer.getLoader().getExhibits().add(newE);
				exhibitNameModel.notifyChange();
				contentListModel.notifyChange();
				peer.makeChange();
			}else{
				System.out.println("Invalid tag name " + newContentName);
			}
		}else{
			System.out.println("Invalid exhibit name " + newName);
		}
	}

	void setNext(){
		String next = (String)exhibitNextDropdown.getSelectedItem();
		if (next != null){
			ExhibitInfo e = getCurrentExhibit();
			e.setNext(next);
			if (e.origNext == null || false == e.origNext.equals(next)){
				peer.makeChange();
			}
		}
	}

	void setPrevious(){
		String prev = (String)exhibitPreviousDropdown.getSelectedItem();
		if (prev != null){
			ExhibitInfo e = getCurrentExhibit();
			e.setPrevious(prev);
			if (e.origPrevious == null || false == e.origPrevious.equals(prev)){
				peer.makeChange();
			}
		}
	}

	ExhibitInfo getCurrentExhibit(){
		String name = (String)exhibitNameList.getSelectedValue();
		int index = -1;
		ArrayList<ExhibitInfo> exhibitList = peer.getLoader().getExhibits();
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

	String[] getAllContentList(){
		ArrayList<String> list = new ArrayList<String>();
		for (String s : peer.getOriginalFiles()){
			if (false == peer.modifiedFileExists(s)){
				if (ZipManager.isImage(s) == false){
					list.add(s);
				}
			}
		}
		for (String s : peer.getModifiedFileNames()){
			if (ZipManager.isImage(s) == false){
				list.add(s);
			}
		}
		return list.toArray(new String[0]);
	}

	void selectGroup(){
		ExhibitGroup group = peer.getLoader().getGroup(groupNameList.getSelectedValue().toString());
		groupExhibitsModel.notifyChange();
		groupExhibitsList.setSelectionInterval(0, 0);
		groupXSpinnerModel.setValue(group.xPos);
		groupYSpinnerModel.setValue(group.yPos);
	}

	void removeGroupExhibit(){
		peer.makeChange();
		peer.getLoader().removeGroupExhibit(groupExhibitsList.getSelectedValue().toString(), groupNameList.getSelectedValue().toString());
		groupExhibitsModel.notifyChange();
		groupExhibitsList.setSelectionInterval(0, 0);
	}

	void addGroupExhibit(){
		ArrayList<ExhibitInfo> filesList = new ArrayList<ExhibitInfo>();
		for(ExhibitInfo e : peer.getLoader().getExhibits()){
			filesList.add(e);
		}
		String groupName = groupNameList.getSelectedValue().toString();
		ExhibitGroup group = peer.getLoader().getGroup(groupName);

		for (String name : group.exhibits){
			for (int i=0; i<filesList.size(); i++){
				if (filesList.get(i).getName().equals(name)){
					filesList.remove(i);
				}
			}
		}
		Object[] files = filesList.toArray();

		ExhibitInfo e = (ExhibitInfo)JOptionPane.showInputDialog(null, "Exhibit:", "Add which exhibit to group?",JOptionPane.PLAIN_MESSAGE,null, files,files[0]);
		if (e != null){
			String[] names = new String[group.exhibits.length + 1];
			for(int i=0; i<names.length -1; i++){
				names[i] = group.exhibits[i];
			}
			names[names.length-1] = e.getName();
			ExhibitGroup newGroup = new ExhibitGroup(names, -1, -1);
			peer.getLoader().addGroup(groupName, newGroup.exhibits, newGroup.xPos, newGroup.yPos);
			peer.makeChange();
			groupExhibitsModel.notifyChange();
		}
	}

	void editFile(){
		String[] keys = peer.getModifiedFileNames();
		File f = peer.getModifiedFile(keys[modifiedFilesList.getSelectedIndex()]);
		String[] exec = {"cmd.exe", "/C", f.getPath()};
		try {
			Runtime.getRuntime().exec(exec);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void viewFile(){
		String filename = peer.getOriginalFiles()[originalFilesList.getSelectedIndex()];
		String ext = filename.substring(filename.lastIndexOf('.')+1);
		try{
			File f = new File(".tmp" + new Random().nextInt() + "." + ext);
			OutputStream outStream = new FileOutputStream(f);
			InputStream stream = peer.getFileInputStream("assets/" + filename);
			for (int result = stream.read(); result != -1; result = stream.read()){
				outStream.write(result);
			}
			outStream.close();
			String[] exec = {"cmd.exe", "/C", f.getPath()};
			try {
				Runtime.getRuntime().exec(exec);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	void removeGroup(){
		peer.makeChange();
		peer.getLoader().removeGroup(groupNameList.getSelectedValue().toString());
		groupNameList.setSelectionInterval(0, 0);
		groupExhibitsList.setSelectionInterval(0, 0);
		groupExhibitsModel.notifyChange();
		groupListModel.notifyChange();	
	}

	void addGroup(){
		String newName = JOptionPane.showInputDialog("Name of new exhibit:");
		if (newName != null && newName.length() > 0){//TODO add name expression matching
			peer.getLoader().addGroup(newName, new String[0], -1, -1);
			groupListModel.notifyChange();
			peer.makeChange();
		}
	}

	void loadHTMLContent(String shortUrl){
		try{
			StyleSheet style = htmlKit.getStyleSheet();
			BufferedReader r = new BufferedReader(new InputStreamReader(peer.getFileInputStream("assets/ExhibitContents/exhibits.css")));
			style.loadRules(r, null);
		}catch(Exception e){
			//TODO do the try block differently if css has been modified.
		}
		InputStream r = peer.getFileInputStream("assets/" + shortUrl); //TODO do differently if modified
		StringBuffer sb = new StringBuffer(128);
		try{
			byte[] buffer = new byte[128];
			for(int result = r.read(buffer); result != -1; result = r.read(buffer)){
				sb.append(new String(buffer, 0, result));
			}
		}catch(IOException e){
		}
		htmlContentViewer.setText(sb.toString());
	}

	void selectAlias(){
		int index = exhibitAliasesList.getSelectedIndex();
		if (index < getCurrentExhibit().getAliases().length){
			aliasDataPanel.setVisible(true);
			Alias alias = getCurrentExhibit().getAliases()[index];
			aliasXCoordField.getModel().setValue(alias.xPos);
			aliasYCoordField.getModel().setValue(alias.yPos);
		}else{
			aliasDataPanel.setVisible(false);
		}
	}

	void selectContent(){
		String tag = (String)contentList.getSelectedValue();
		ExhibitInfo e = getCurrentExhibit();
		String data = e.getContent(tag);
		exhibitContentLabel.setText(e.getOrigContents(tag));
		newContentDropdown.setSelectedItem(data);
		loadHTMLContent(data);
	}

	void selectExhibit(){
		ExhibitInfo e = getCurrentExhibit();

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

		selectPhoto();
		selectAlias();
	}

	class OriginalFilesListModel extends BasicListModel{
		@Override
		public Object getElementAt(int index) {
			return peer.getOriginalFiles()[index];
		}

		@Override
		public int getSize() {
			return peer.getOriginalFiles().length;
		}
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
		String shortUrl = getCurrentExhibit().getPhotos()[index].shortUrl;

		if (peer.modifiedFileExists(shortUrl)){
			exhibitPhotosImage.setImage(peer.getModifiedFile(shortUrl));
		}else{
			exhibitPhotosImage.setImage(shortUrl, peer.getFileInputStream("assets/" + shortUrl));
		}
		photoCaption.setText(getCurrentExhibit().getPhotos()[index].getCaption());
	}

	class ExhibitListModel extends BasicListModel{
		@Override
		public Object getElementAt(int index) {
			return peer.getLoader().getExhibits().get(index).getName();
		}

		@Override
		public int getSize() {
			return peer.getLoader().getExhibits().size();
		}
	}

	class GroupListModel extends BasicListModel{
		@Override
		public Object getElementAt(int index) {
			String[] names = peer.getLoader().getGroupNames();
			if (index >= getSize()){
				return "";
			}
			return names[index];
		}

		@Override
		public int getSize() {
			return peer.getLoader().getGroupNames().length;
		}
	}


	class ExhibitAliasesModel extends BasicListModel{
		@Override
		public Object getElementAt(int index) {
			return getCurrentExhibit().getAliases()[index].name;
		}

		@Override
		public int getSize() {
			return getCurrentExhibit().getAliases().length;
		}
	}

	class EventsListModel extends BasicListModel{
		ArrayList<Event> events;
		public EventsListModel(){
			events = peer.getLoader().getEvents();
		}

		public Event getEvent(int index) {
			return events.get(index);
		}

		@Override
		public Object getElementAt(int index) {
			return events.get(index).getTitle();
		}

		@Override
		public int getSize() {
			return events.size();
		}
	}

	class GroupExhibitsModel extends BasicListModel{
		@Override
		public Object getElementAt(int index) {
			return peer.getLoader().getGroup(groupNameList.getSelectedValue().toString()).exhibits[index];
		}

		@Override
		public int getSize() {
			ExhibitGroup group = peer.getLoader().getGroup(groupNameList.getSelectedValue().toString());
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
				return getCurrentExhibit().getTag(index);
			}else{
				return "";
			}
		}

		@Override
		public int getSize() {
			return getCurrentExhibit().getTagCount();
		}
	}

	class ExhibitPhotosModel extends BasicListModel{
		@Override
		public Object getElementAt(int index) {
			return getCurrentExhibit().getPhotos()[index].shortUrl;
		}

		@Override
		public int getSize() {
			return getCurrentExhibit().getPhotos().length;
		}
	}

	class ExhibitDropdownModel extends BasicComboBoxModel{
		private String selected = "";

		@Override
		public Object getElementAt(int index) {
			ExhibitInfo e = peer.getLoader().getExhibits().get(index);
			if (e.getName().equals(getCurrentExhibit().getName())){
				return "";
			}else{
				return e.getName();
			}
		}
		@Override
		public int getSize() {
			return peer.getLoader().getExhibits().size();
		}
		@Override
		public Object getSelectedItem() {
			return selected;
		}
		@Override
		public void setSelectedItem(Object arg0) {
			selected = (String)arg0;
			notifyChange();
		}
	}

	class ContentDropdownModel extends BasicComboBoxModel{
		private String selected = null;

		@Override
		public Object getElementAt(int index) {
			return getAllContentList()[index];
		}
		@Override
		public int getSize() {
			return getAllContentList().length;
		}
		@Override
		public Object getSelectedItem() {
			return selected;
		}
		@Override
		public void setSelectedItem(Object arg0) {
			selected = (String)arg0;
			notifyChange();
		}
	}

	@Override
	public void stateChanged(ChangeEvent arg0) {
		ExhibitInfo e = getCurrentExhibit();
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
				e.addAlias(alias.name, val, alias.yPos, alias.tag);
				peer.makeChange();
			}
		}else if (arg0.getSource().equals(aliasYCoordField.getModel())){
			int val = Integer.parseInt(aliasYCoordField.getModel().getValue().toString());
			int index = exhibitAliasesList.getSelectedIndex();
			Alias alias = e.getAliases()[index];
			if (alias.yPos != val){
				e.addAlias(alias.name, alias.xPos, val, alias.tag);
				peer.makeChange();
			}
		}else if (arg0.getSource().equals(groupXCoordField.getModel())){
			int val = Integer.parseInt(groupXCoordField.getModel().getValue().toString());
			String name = groupNameList.getSelectedValue().toString();
			ExhibitGroup group = peer.getLoader().getGroup(name);
			if (val != group.xPos){
				peer.getLoader().addGroup(name, group.exhibits, val, group.yPos);
				peer.makeChange();
			}
		}else if (arg0.getSource().equals(groupYCoordField.getModel())){
			int val = Integer.parseInt(groupYCoordField.getModel().getValue().toString());
			String name = groupNameList.getSelectedValue().toString();
			ExhibitGroup group = peer.getLoader().getGroup(name);
			if (val != group.yPos){
				peer.getLoader().addGroup(name, group.exhibits,group.xPos, val);
				peer.makeChange();
			}
		}else if (arg0.getSource().equals(eventStart.getModel())){
			Event event = peer.getLoader().getEvents().get(eventsList.getSelectedIndex());
			event.setStartDay((Date)eventStart.getModel().getValue());
			peer.makeChange();
		}else if (arg0.getSource().equals(eventEnd.getModel())){
			Event event = peer.getLoader().getEvents().get(eventsList.getSelectedIndex());
			event.setEndDay((Date)eventEnd.getModel().getValue());
			peer.makeChange();
		}
	}

	public JMapPanel getMapPanel() {
		return mapPanel;
	}

}
