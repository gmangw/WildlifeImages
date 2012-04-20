package org.wildlifeimages.android.wildlifeimages.test;

import org.wildlifeimages.android.wildlifeimages.IntroActivity;
import org.wildlifeimages.android.wildlifeimages.R;

import android.test.ActivityInstrumentationTestCase2;
import android.view.KeyEvent;

public class WireActivityInstrumentationTest extends ActivityInstrumentationTestCase2<IntroActivity> {

	private IntroActivity mActivity;

	public WireActivityInstrumentationTest() {
		super("org.wildlifeimages.android.wildlifeimages", IntroActivity.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		mActivity = this.getActivity();
	}
	
	//http://stackoverflow.com/questions/3084891/how-to-test-menu
	public void testOnOptionsItemSelected() throws InterruptedException{
		getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_MENU);
		Thread.sleep(1000);
		getInstrumentation().invokeMenuActionSync(mActivity, mActivity.loadInt(R.id.menu_map), 0);
		Thread.sleep(1000);
	}
}
