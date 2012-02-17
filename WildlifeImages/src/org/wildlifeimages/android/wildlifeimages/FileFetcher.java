package org.wildlifeimages.android.wildlifeimages;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.util.Log;

public class FileFetcher {
	public static byte[] getWebContent(String shortUrl, ContentUpdater progress){
		URL url;
		try {
			url = new URL("http://oregonstate.edu/~wilkinsg/wildlifeimages/" + shortUrl);
		}catch(MalformedURLException e){
			Log.e(FileFetcher.class.getName(), "Caching of " + shortUrl + " failed with MalformedUrlException.");
			return null;
		}
		int length = 0;
		int read = 0;
		byte[] buffer = null;
		try {
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			InputStream webStream = conn.getInputStream();
			int lengthGuess = conn.getContentLength();
			if (lengthGuess == -1){
				lengthGuess = 32768;
				Log.e(FileFetcher.class.getName(), "File size unknown for " + shortUrl);
			}

			if (progress != null){
				progress.setText(shortUrl);
			}

			buffer = new byte[lengthGuess];
			Log.i(FileFetcher.class.getName(), conn.getHeaderFields().toString());	
			while (buffer.length - length > 0){
				read = webStream.read(buffer, length, buffer.length - length);
				if (read == -1){
					break;
				}else{
					length += read;
					if (progress != null){
						progress.publish(100*length/lengthGuess);
						if (progress.isCancelled()){
							throw(new InterruptedException());
						}
					}
				}
			}
			webStream.close();
			conn.disconnect();
		} catch (IOException e) {
			Log.w(FileFetcher.class.getName(), "Caching of " + shortUrl + " failed with IOException: " + e.getMessage());
		} catch (InterruptedException e) {
			Log.d(FileFetcher.class.getName(), "Update cancelled.");
			return null;
		}

		if (length > 0){
			byte[] result = new byte[length];
			System.arraycopy(buffer, 0, result, 0, result.length);
			return result;
		}else{
			return null;
		}	
	}

	public static void recursiveRemove(File f){
		if (f.isDirectory()){
			File[] list = f.listFiles();
			for(int i=0; i<list.length; i++){
				recursiveRemove(list[i]);
			}
		}else{
			f.delete();
		}
	}

	public static void mkdirForFile(File file) throws IOException{
		if (file.getParentFile().exists()){
			return;
		}else{
			mkdirForFile(file.getParentFile());
			if (true == file.getParentFile().mkdir()){ 
				return;
			}else{
				throw(new IOException("Cache subdirectory creation failed: " + file.getParentFile()));
			}
		}
	}

	public static void writeBytesToFile(byte[] content, File f) throws IOException{
		FileOutputStream fOut = new FileOutputStream(f);
		fOut.write(content);
		fOut.close();
	}
}
