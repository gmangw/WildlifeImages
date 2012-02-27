package org.wildlifeimages.tools.update;

import java.io.File;
import java.io.IOException;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.Writer;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.*;

public class CreateQR {
	private static final int WIDTH = 408;
	private static final int HEIGHT = 408;
	private static final String prefix = "market://search?q=pname:org.wildlifeimages.android.wildlifeimages&extra=Wildlife_Images_Exhibit_";

	public static void writeExhibitQR(String exhibitName, File outputFile){
		writeQR(prefix + exhibitName, outputFile);
	}

	public static void writeQR(String text, File outputFile){
		BitMatrix matrix = null;
		Writer writer = new MultiFormatWriter();

		try {
			matrix = writer.encode(text, BarcodeFormat.QR_CODE, WIDTH, HEIGHT);
		} catch (com.google.zxing.WriterException e) {
			System.out.println(e.getMessage());
		}
		try {
			MatrixToImageWriter.writeToFile(matrix, "PNG", outputFile);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
}