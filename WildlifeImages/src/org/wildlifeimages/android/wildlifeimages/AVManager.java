package org.wildlifeimages.android.wildlifeimages;

import java.io.IOException;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.util.Log;
import android.widget.Toast;

public class AVManager {

	public AVManager(){
	}

	public MediaPlayer playSound(String shortUrl, ContentManager contentManager, AssetManager assets){
		MediaPlayer soundPlayer = new MediaPlayer();
		try{
			AssetFileDescriptor fd = contentManager.getFileDescriptor((shortUrl), assets);
			if (fd.getStartOffset() == -1){
				soundPlayer.setDataSource(fd.getFileDescriptor());
			}else{
				soundPlayer.setDataSource(fd.getFileDescriptor(),fd.getStartOffset(),fd.getLength());
			}
			fd.close();
			soundPlayer.prepare();
			soundPlayer.start();			
		} catch (IOException e){
			Log.e(this.getClass().getName(), Log.getStackTraceString(e));
		}
		return soundPlayer;
	}
}
