package org.wildlifeimages.tools.update;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class JImage extends JPanel{
	private static final long serialVersionUID = -1069865040054492287L;

	private String lastShortUrl = null;
	private String lastFile = null;
	private BufferedImage image = null;
	
	public void setImage(File newImage){
		if (lastFile == null || (false == lastFile.equals(newImage.getName()))){
			try {
				image = ImageIO.read(newImage);
				lastFile = newImage.getName();
				lastShortUrl = null;
				shrinkImage();
			} catch (IOException e) {
			}
		}
	}

	public void setImage(String shortUrl, ZipInputStream source){
		if (lastShortUrl == null || (false == lastShortUrl.equalsIgnoreCase(shortUrl))){
			System.out.println("Loading image " + shortUrl);
			try {
				ZipEntry entry;
				for (entry = source.getNextEntry(); entry != null; entry = source.getNextEntry()){
					if (entry.getName().equals("assets/" + shortUrl)){
						break;
					}
					entry = null;
				}
				if (entry == null){
					throw new IOException("Could not load " + shortUrl);
				}
				image = ImageIO.read(source);
				lastFile = null;
				lastShortUrl = shortUrl;
				shrinkImage();
				source.close();
				
			} catch (IOException e) {
				System.out.println("Error loading image");
			}
		}
	}
	
	private void shrinkImage(){
		BufferedImage smallImage = new BufferedImage(image.getWidth()/2, image.getHeight()/2, BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D g = smallImage.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.drawImage(image, 0, 0, image.getWidth()/2, image.getHeight()/2, this);
		image = smallImage;
		setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
		revalidate();
		this.repaint();
	}
	
	@Override
	public void paint(Graphics g){
		super.paint(g);
		if (image != null){
			g.drawImage(image, 0, 0, null);
		}
	}
}
