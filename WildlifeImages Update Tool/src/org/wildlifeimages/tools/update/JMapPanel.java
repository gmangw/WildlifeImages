package org.wildlifeimages.tools.update;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.LayoutManager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.JPanel;

import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.util.SVGConstants;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.svg.SVGDocument;
import org.wildlifeimages.android.wildlifeimages.ExhibitGroup;
import org.wildlifeimages.android.wildlifeimages.Exhibit.Alias;

public class JMapPanel extends JPanel{

	private static final long serialVersionUID = -4146837675809312050L;

	private final Dimension mapDimension;
	
	private final ExhibitLoader exhibitParser;
	
	public JMapPanel(LayoutManager layout, Dimension mapSize, ExhibitLoader parser) {
		super(layout);
		
		mapDimension = mapSize;
		
		exhibitParser = parser;
	}

	@Override
	public void paint(Graphics g){
		super.paint(g);
		double mapAspect = mapDimension.getWidth()/mapDimension.getHeight();
		double myAspect = 1.0*getWidth()/getHeight();
		int w;
		int h;
		int offsetX = 0;
		int offsetY = 0;

		if (myAspect > mapAspect){
			w = (int)(getWidth() * (mapAspect / myAspect));
			h = getHeight();
			offsetX = (getWidth() - w)/2;
		}else{
			w = getWidth();
			h = (int)(getHeight() * (myAspect / mapAspect));
			offsetY = (getHeight() - h)/2;
		}

		ArrayList<String> names = new ArrayList<String>();
		ArrayList<Integer> pointsX = new ArrayList<Integer>();
		ArrayList<Integer> pointsY = new ArrayList<Integer>();

		for(ExhibitInfo e : exhibitParser.getExhibits()){
			int exhibitX = e.getX();
			int exhibitY = e.getY();
			if (exhibitX != -1 || exhibitY != -1){
				int x = w * exhibitX/100 + offsetX;
				int y = h * exhibitY/100 + offsetY;
				names.add(e.getName());
				pointsX.add(x);
				pointsY.add(y);
			}
			for (Alias a : e.getAliases()){
				exhibitX = a.xPos;
				exhibitY = a.yPos;
				int x = w * exhibitX/100 + offsetX;
				int y = h * exhibitY/100 + offsetY;
				names.add(a.name);
				pointsX.add(x);
				pointsY.add(y);
			}
		}
		for(String groupName : exhibitParser.getGroupNames()){
			ExhibitGroup group = exhibitParser.getGroup(groupName);
			int exhibitX = group.xPos;
			int exhibitY = group.yPos;
			int x = w * exhibitX/100 + offsetX;
			int y = h * exhibitY/100 + offsetY;
			names.add(groupName);
			pointsX.add(x);
			pointsY.add(y);
		}

		int fontHeight = g.getFontMetrics().getHeight();
		for (int i=0; i<names.size(); i++){
			int stringWidth = g.getFontMetrics().stringWidth(names.get(i));
			g.setColor(new Color(0.0f, 0.0f, 0.0f, 0.5f));
			g.fillRect(pointsX.get(i)-stringWidth/2 - 1, pointsY.get(i)-fontHeight+1, stringWidth + 2, fontHeight+2);
			g.setColor(Color.WHITE);
			g.drawString(names.get(i), pointsX.get(i)-stringWidth/2, pointsY.get(i));
		}
	}
	
	public static JSVGCanvas getMapCanvas(Dimension d, ZipInputStream stream){

		JSVGCanvas svgCanvas = new JSVGCanvas();

		try{
			ZipEntry entry;
			for (entry = stream.getNextEntry(); entry != null; entry = stream.getNextEntry()){
				if (entry.getName().equals("res/raw/map.svg")){
					break;
				}
				entry = null;
			}
			if (entry == null){
				throw new IOException("Error: Could not load map.svg");
			}

			String parser = XMLResourceDescriptor.getXMLParserClassName();
			SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
			SVGDocument doc = (SVGDocument)f.createDocument("myURI", stream);

			String widthString = doc.getDocumentElement().getAttribute(SVGConstants.SVG_WIDTH_ATTRIBUTE);
			String heightString = doc.getDocumentElement().getAttribute(SVGConstants.SVG_HEIGHT_ATTRIBUTE);
			int width = Integer.parseInt(widthString.substring(0, widthString.length()-3));
			int height = Integer.parseInt(heightString.substring(0, heightString.length()-3));

			d.setSize(width, height);

			svgCanvas.setSVGDocument(doc);

			stream.close();
		}catch(IOException e){
			System.out.println(e.getMessage());
		}

		return svgCanvas;
	}
}
