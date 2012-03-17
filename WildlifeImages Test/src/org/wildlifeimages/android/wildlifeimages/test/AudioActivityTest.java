package org.wildlifeimages.android.wildlifeimages.test;

import org.wildlifeimages.android.wildlifeimages.AudioActivity;
import org.wildlifeimages.android.wildlifeimages.Common;
import org.wildlifeimages.android.wildlifeimages.ContentManager;
import org.wildlifeimages.android.wildlifeimages.IntroActivity;

import android.test.ActivityInstrumentationTestCase2;

public class AudioActivityTest extends ActivityInstrumentationTestCase2<AudioActivity> {
	
	private AudioActivity mActivity;

	public AudioActivityTest() {
		super("org.wildlifeimages.android.wildlifeimages", AudioActivity.class);

	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		mActivity = this.getActivity();
	}

	public void testPreconditions() {
		assertNotNull(mActivity);
	}
}
