package org.wildlifeimages.android.wildlifeimages.test;

import org.wildlifeimages.android.wildlifeimages.AudioActivity;
import org.wildlifeimages.android.wildlifeimages.Common;
import org.wildlifeimages.android.wildlifeimages.ContentManager;
import org.wildlifeimages.android.wildlifeimages.IntroActivity;
import org.wildlifeimages.android.wildlifeimages.R;

import android.content.Intent;
import android.sax.StartElementListener;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;

public class AudioActivityTest extends ActivityInstrumentationTestCase2<AudioActivity> {
	
	private AudioActivity mActivity;

	public AudioActivityTest() {
		super("org.wildlifeimages.android.wildlifeimages", AudioActivity.class);

	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		Intent audioIntent = new Intent();
		audioIntent.putExtra("URL", "ExhibitContents/Lynx/test.mp3");
		setActivityIntent(audioIntent);
		mActivity = this.getActivity();
	}

	public void testPreconditions() {
		assertNotNull(mActivity);
	}
	
	@UiThreadTest
	public void testPlay(){
		Button pauseButton = (Button)mActivity.findViewById(R.id.media_pause_button);
		Button stopButton = (Button)mActivity.findViewById(R.id.media_stop_button);
		SeekBar progress = (SeekBar)mActivity.findViewById(R.id.media_progress);
		
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {}
		
		pauseButton.performClick();
		
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {}
		
		pauseButton.performClick();
		
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {}
		
		progress.setProgress(50);
		
		stopButton.performClick();
	}
}
