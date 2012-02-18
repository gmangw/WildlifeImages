package org.wildlifeimages.tools.update;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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

	public void setImage(String shortUrl, ZipFile source){
		if (lastShortUrl == null || (false == lastShortUrl.equalsIgnoreCase(shortUrl))){
			System.out.println("Loading image " + shortUrl);
			try {
				ZipFile zf = new ZipFile("WildlifeImages.apk"); //TODO
				ZipEntry entry = zf.getEntry("assets/" + shortUrl); //TODO
				image = ImageIO.read(zf.getInputStream(entry));
				lastFile = null;
				lastShortUrl = shortUrl;

				float aspect = 1.0f*image.getWidth()/image.getHeight();
				BufferedImage smallImage = new BufferedImage(this.getWidth()+1, this.getHeight()+1, BufferedImage.TYPE_3BYTE_BGR);
				smallImage.createGraphics().drawImage(image, 0, 0, (int)(this.getHeight()*aspect), this.getHeight(), this);
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
			//float aspect = 1.0f*image.getWidth()/image.getHeight();
			//g.drawImage(image, 0, 0, (int)(this.getHeight()*aspect), this.getHeight(), null);
			g.drawImage(image, 0, 0, this);
		}
	}
}
