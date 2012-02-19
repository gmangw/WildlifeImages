package org.wildlifeimages.tools.update;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class JImage extends JPanel{
	private String lastShortUrl = null;
	private String lastFile = null;
	private BufferedImage image = null;

	public JImage(){
		super();
	}
	
	public void setImage(File newImage){
		if (lastFile == null || (false == lastFile.equals(newImage.getName()))){
			try {
				image = ImageIO.read(newImage);
				lastFile = newImage.getName();
				lastShortUrl = null;

				float aspect = 1.0f*image.getWidth()/image.getHeight();
				BufferedImage smallImage = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
				smallImage.createGraphics().drawImage(image, 0, 0, (int)(this.getHeight()*aspect), this.getHeight(), this);
				image = smallImage;
				this.repaint();
			} catch (IOException e) {
			}
		}
	}

	public void setImage(String shortUrl, ZipInputStream source){
		final int previewSize = 92;
		if (lastShortUrl == null || (false == lastShortUrl.equalsIgnoreCase(shortUrl))){
			System.out.println("Loading image " + shortUrl);
			try {
				//ZipFile zf = new ZipFile("WildlifeImages.apk"); //TODO
				//ZipEntry entry = zf.getEntry("assets/" + shortUrl); //TODO
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

				float aspect = 1.0f*image.getWidth()/image.getHeight();
				BufferedImage smallImage = new BufferedImage((int)(previewSize*aspect), previewSize, BufferedImage.TYPE_3BYTE_BGR);
				smallImage.createGraphics().drawImage(image, 0, 0, (int)(previewSize*aspect), previewSize, this);
				image = smallImage;

				source.close();
				this.repaint();
			} catch (IOException e) {
				System.out.println("Error loading image");
			}
		}
	}
	
	@Override
	public void paint(Graphics g){
		super.paint(g);
		if (image != null){
			g.drawImage(image, 0, 0, null);
		}
	}
}
