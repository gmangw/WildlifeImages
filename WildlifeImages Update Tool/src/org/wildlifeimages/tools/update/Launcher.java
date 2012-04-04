package org.wildlifeimages.tools.update;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.wildlifeimages.tools.update.android.APKLoader;

public class Launcher implements WindowListener{
	public static void main(String[] args){
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} 
		catch (UnsupportedLookAndFeelException e) {
			//TODO
		}
		catch (ClassNotFoundException e) {
			//TODO
		}
		catch (InstantiationException e) {
			//TODO
		}
		catch (IllegalAccessException e) {
			//TODO
		}

		new Launcher();
	}

	private final PackageLoader initialLoader = new APKLoader();

	public Launcher(){
		//initialLoader.loadNewPackage();
		ZipManager zr = new ZipManager(initialLoader);
		zr.addWindowListener(this);
	}

	@Override
	public void windowActivated(WindowEvent arg0) {}

	@Override
	public void windowClosed(WindowEvent arg0) {
		if (initialLoader.isNew() == true){
			initialLoader.setNewState(false);
			ZipManager zr = new ZipManager(initialLoader);
			zr.addWindowListener(this);
		}else{
			System.exit(0);
		}
	}

	@Override
	public void windowClosing(WindowEvent arg0) {}

	@Override
	public void windowDeactivated(WindowEvent arg0) {}

	@Override
	public void windowDeiconified(WindowEvent arg0) {}

	@Override
	public void windowIconified(WindowEvent arg0) {}

	@Override
	public void windowOpened(WindowEvent arg0) {}
}
