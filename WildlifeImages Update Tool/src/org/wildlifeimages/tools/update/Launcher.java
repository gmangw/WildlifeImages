package org.wildlifeimages.tools.update;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class Launcher implements WindowListener{
	public static void main(String[] args){
		new Launcher();
	}
	
	private final APKLoader initialLoader = new APKLoader();
	
	public Launcher(){
		ZipManager zr = new ZipManager(initialLoader);
		zr.addWindowListener(this);
		zr.setVisible(true);
	}

	@Override
	public void windowActivated(WindowEvent arg0) {}

	@Override
	public void windowClosed(WindowEvent arg0) {
		if (initialLoader.isNew() == true){
			initialLoader.setNewState(false);
			ZipManager zr = new ZipManager(initialLoader);
			zr.addWindowListener(this);
			zr.setVisible(true);
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
