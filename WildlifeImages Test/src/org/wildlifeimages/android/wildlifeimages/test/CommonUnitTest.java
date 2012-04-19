package org.wildlifeimages.android.wildlifeimages.test;

import org.wildlifeimages.android.wildlifeimages.Common;
import org.wildlifeimages.android.wildlifeimages.IntroActivity;

import android.content.Intent;
import android.test.ActivityUnitTestCase;

/**
 * @author Graham
 *
 */
public class CommonUnitTest extends ActivityUnitTestCase<IntroActivity>{

	Intent mockIntent = new Intent(Intent.ACTION_MAIN);

	public CommonUnitTest() {
		super(IntroActivity.class);

	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testStartScan(){
		IntroActivity mActivity = startActivity(mockIntent, null, null);
		assertNotNull(mActivity);
		
		Common.startScan(mActivity);
		Intent started = getStartedActivityIntent();
		assertNotNull(started);
	}
	
	public void testStartCamera(){
		IntroActivity mActivity = startActivity(mockIntent, null, null);
		assertNotNull(mActivity);
		
		Common.startCamera(mActivity);
		Intent started = getStartedActivityIntent();
		assertNotNull(started);
	}
	
	public void testOnKeyDown(){
		IntroActivity mActivity = startActivity(mockIntent, null, null);
		assertNotNull(mActivity);
		
		Common.onKeyDown(null, 0, null);
	}
}
