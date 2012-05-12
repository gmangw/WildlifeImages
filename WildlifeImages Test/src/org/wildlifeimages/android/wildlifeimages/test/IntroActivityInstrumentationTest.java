package org.wildlifeimages.android.wildlifeimages.test;

import org.wildlifeimages.android.wildlifeimages.IntroActivity;
import org.wildlifeimages.android.wildlifeimages.R;

import android.os.Bundle;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.ImageButton;

public class IntroActivityInstrumentationTest extends ActivityInstrumentationTestCase2<IntroActivity> {

	private IntroActivity mActivity;

	public IntroActivityInstrumentationTest() {
		super("org.wildlifeimages.android.wildlifeimages", IntroActivity.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		mActivity = this.getActivity();
	}

	@UiThreadTest
	public void testActions(){
		assertNotNull(mActivity);

		getInstrumentation().callActivityOnPause(mActivity);
		getInstrumentation().callActivityOnResume(mActivity);
		getInstrumentation().callActivityOnSaveInstanceState(mActivity, new Bundle());
	}

	public void testButtons() throws Throwable{
		assertNotNull(mActivity);

		runTestOnUiThread(new Runnable(){
			public void run() {
				ImageButton explore = (ImageButton)mActivity.findViewById(R.id.intro_sidebar_exhibitlist);
				explore.performClick();
			}
		});

		Thread.sleep(250);
		sendKeys(KeyEvent.KEYCODE_BACK);
		assertNotNull(mActivity);

		runTestOnUiThread(new Runnable(){
			public void run() {
				ImageButton photos = (ImageButton)mActivity.findViewById(R.id.intro_sidebar_photos);
				photos.performClick();
			}
		});
		
		Thread.sleep(250);
		sendKeys(KeyEvent.KEYCODE_BACK);
		assertNotNull(mActivity);
		
		runTestOnUiThread(new Runnable(){
			public void run() {
				ImageButton map = (ImageButton)mActivity.findViewById(R.id.intro_sidebar_map);
				map.performClick();
			}
		});
		
		Thread.sleep(250);
		sendKeys(KeyEvent.KEYCODE_BACK);
		assertNotNull(mActivity);
		
		runTestOnUiThread(new Runnable(){
			public void run() {
				ImageButton events = (ImageButton)mActivity.findViewById(R.id.intro_sidebar_events);
				events.performClick();
			}
		});
		
		Thread.sleep(250);
		sendKeys(KeyEvent.KEYCODE_BACK);
		assertNotNull(mActivity);
	}

	@Override
	protected void tearDown() throws Exception{
		super.tearDown();
	}
}
