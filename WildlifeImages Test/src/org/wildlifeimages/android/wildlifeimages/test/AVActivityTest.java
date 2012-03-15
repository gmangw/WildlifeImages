package org.wildlifeimages.android.wildlifeimages.test;

import org.wildlifeimages.android.wildlifeimages.AVActivity;
import org.wildlifeimages.android.wildlifeimages.Common;
import org.wildlifeimages.android.wildlifeimages.ContentManager;
import org.wildlifeimages.android.wildlifeimages.IntroActivity;

import android.test.ActivityInstrumentationTestCase2;

public class AVActivityTest extends ActivityInstrumentationTestCase2<AVActivity> {
	
	private AVActivity mActivity;

	public AVActivityTest() {
		super("org.wildlifeimages.android.wildlifeimages", AVActivity.class);

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
