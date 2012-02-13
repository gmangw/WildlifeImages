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
import javax.swing.filechooser.FileFilter;

public class ZipManager extends JFrame implements ActionListener{
	private static final String assetPath = "assets/";

	private final HashMap<String, byte[]> modifiedFiles = new HashMap<String, byte[]>();

	Button saveButton = new Button("Save");
	Button addButton = new Button("Open");

	public static void main(String[] args){
		ZipManager zr = new ZipManager();

		String[] files = zr.readAPK("WildlifeImages.apk");

		for (String s : files){
			System.out.println(s);
		}
	}

	public ZipManager(){
		saveButton.addActionListener(this);
		saveButton.setSize(100, 20);

		addButton.addActionListener(this);
		addButton.setSize(100, 20);

		this.setSize(640, 480);
		this.setLayout(new GridLayout(1,2));
		this.add(addButton);
		this.add(saveButton);

		this.setVisible(true);
	}

	public void addFile(String filename, byte[] data){
		modifiedFiles.put(filename, data);
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
}
