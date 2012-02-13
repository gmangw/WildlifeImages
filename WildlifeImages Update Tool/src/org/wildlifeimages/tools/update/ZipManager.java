package org.wildlifeimages.tools.update;

import java.awt.Button;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.filechooser.FileFilter;

public class ZipManager extends JFrame implements ActionListener{

	private static final long serialVersionUID = 1L;

	private static final String assetPath = "assets/";

	private final HashMap<String, byte[]> modifiedFiles = new HashMap<String, byte[]>();

	private String[] originalFiles;

	private final Button saveButton = new Button("Save");
	private final Button addButton = new Button("Open");
	private final JList existingList = new JList();
	private final JList modifiedList = new JList();

	private final JPanel listPanel = new JPanel(new GridLayout(1,2, 5, 5));
	private final JScrollPane listScrollPane = new JScrollPane(listPanel);

	private final ExistingListModel modifiedListModel;

	public static void main(String[] args){
		ZipManager zr = new ZipManager();
		zr.setVisible(true);
	}

	public ZipManager(){
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		saveButton.addActionListener(this);
		saveButton.setSize(100, 20);

		addButton.addActionListener(this);
		addButton.setSize(100, 20);
		
		originalFiles = this.readAPK("WildlifeImages.apk");

		existingList.setModel(new ExistingListModel());

		modifiedListModel = new ExistingListModel(){
			@Override
			public Object getElementAt(int index){
				if (modifiedFiles.containsKey(originalFiles[index])){
					byte[] content = modifiedFiles.get(originalFiles[index]);
					return new String(content, 0, Math.min(10, content.length));
				}else{
					return " ";
				}
			}
		};

		modifiedList.setModel(modifiedListModel);

		listPanel.add(existingList);
		listPanel.add(modifiedList);

		this.setSize(640, 480);
		this.setLayout(new GridLayout(3,1));
		this.add(addButton);
		this.add(saveButton);
		this.add(listScrollPane);
	}

	public void addFile(String filename, byte[] data){
		modifiedFiles.put(filename, data);
		modifiedListModel.notifyChange();
	}

	public String[] readAPK(String outFilename){
		try {
			ZipFile zf = new ZipFile(outFilename);
			ArrayList<String> files = new ArrayList<String>();
			for (Enumeration<? extends ZipEntry> entries = zf.entries(); entries.hasMoreElements();) {
				ZipEntry item = entries.nextElement();
				String zipEntryName = (item).getName();
				if (false == item.isDirectory() && zipEntryName.startsWith(assetPath)){
					files.add(zipEntryName.substring(assetPath.length()));
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
				out.putNextEntry(new ZipEntry(key));

				out.write(modifiedFiles.get(key));

				out.closeEntry();
			}

			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private class ZipFilter extends FileFilter{

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
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getSource().equals(saveButton)){
			if (modifiedFiles.size() > 0){
				JFileChooser chooser = new JFileChooser();
				ZipFilter filter = new ZipFilter();
				chooser.setFileFilter(filter);
				chooser.setDialogTitle("Select output zip file name.");
				if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION){
					this.saveFile(chooser.getSelectedFile());
				}
			}
		}else if (event.getSource().equals(addButton)){
			this.addFile("ExhibitContents/deltaIntro.html", "Updated delta intro page content.".getBytes());
		}
	}

	private class ExistingListModel implements ListModel{
		ArrayList<ListDataListener> listeners = new ArrayList<ListDataListener>();

		@Override
		public void addListDataListener(ListDataListener newListener) {
			listeners.add(newListener);
		}

		@Override
		public Object getElementAt(int index) {
			return originalFiles[index];
		}

		@Override
		public int getSize() {
			return originalFiles.length;
		}

		@Override
		public void removeListDataListener(ListDataListener oldListener) {
			listeners.remove(oldListener);
		}

		public void notifyChange(){
			for (ListDataListener listener : listeners){
				listener.contentsChanged(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, originalFiles.length));
			}
		}
	}
}