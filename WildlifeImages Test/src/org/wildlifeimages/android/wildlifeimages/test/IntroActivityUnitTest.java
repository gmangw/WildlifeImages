package org.wildlifeimages.android.wildlifeimages.test;

import org.wildlifeimages.android.wildlifeimages.ExhibitListActivity;
import org.wildlifeimages.android.wildlifeimages.IntroActivity;
import org.wildlifeimages.android.wildlifeimages.MapActivity;
import org.wildlifeimages.android.wildlifeimages.PhotosActivity;

import android.content.Intent;
import android.os.Bundle;
import android.test.ActivityUnitTestCase;
import android.view.View;
import org.wildlifeimages.android.wildlifeimages.R;

public class IntroActivityUnitTest extends ActivityUnitTestCase<IntroActivity>{

	Intent mockIntent = new Intent(Intent.ACTION_MAIN);

	public IntroActivityUnitTest() {
		super(IntroActivity.class);

	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void test(){
		IntroActivity mActivity = startActivity(mockIntent, null, null);
		assertNotNull(mActivity);
		
		View v = new View(mActivity);
		
		v.setId(-1);
		mActivity.introProcessSidebar(v);
		assertNull(getStartedActivityIntent());
		
		v.setId(R.id.intro_sidebar_events);
		mActivity.introProcessSidebar(v);
		assertNotNull(getStartedActivityIntent());
		assertEquals(PhotosActivity.class.getName(), getStartedActivityIntent().getComponent().getClassName());
		
		v.setId(R.id.intro_sidebar_photos);
		mActivity.introProcessSidebar(v);
		assertNotNull(getStartedActivityIntent());
		assertEquals(PhotosActivity.class.getName(), getStartedActivityIntent().getComponent().getClassName());
	
		v.setId(R.id.intro_sidebar_map);
		mActivity.introProcessSidebar(v);
		assertNotNull(getStartedActivityIntent());
		assertEquals(MapActivity.class.getName(), getStartedActivityIntent().getComponent().getClassName());
		
		v.setId(R.id.intro_sidebar_exhibitlist);
		mActivity.introProcessSidebar(v);
		assertNotNull(getStartedActivityIntent());
		assertEquals(ExhibitListActivity.class.getName(), getStartedActivityIntent().getComponent().getClassName());
	
		getInstrumentation().callActivityOnPause(mActivity);
		getInstrumentation().callActivityOnResume(mActivity);
		getInstrumentation().callActivityOnSaveInstanceState(mActivity, new Bundle());
	}
}
