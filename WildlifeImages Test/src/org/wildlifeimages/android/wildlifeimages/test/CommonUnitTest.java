package org.wildlifeimages.android.wildlifeimages.test;

import java.util.ArrayList;

import org.wildlifeimages.android.wildlifeimages.AudioActivity;
import org.wildlifeimages.android.wildlifeimages.Common;
import org.wildlifeimages.android.wildlifeimages.ContentManager;
import org.wildlifeimages.android.wildlifeimages.ExhibitList;
import org.wildlifeimages.android.wildlifeimages.IntroActivity;
import org.wildlifeimages.android.wildlifeimages.R;

import android.app.Activity;
import android.content.Intent;
import android.test.ActivityUnitTestCase;

public class CommonUnitTest extends ActivityUnitTestCase<IntroActivity>{

	Intent mockIntent = new Intent(Intent.ACTION_MAIN);

	public CommonUnitTest() {
		super(IntroActivity.class);

	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testCommonProcessResultQR(){
		IntroActivity mActivity = startActivity(mockIntent, null, null);
		assertNotNull(mActivity);

		int code_scan = R.integer.CODE_SCAN_ACTIVITY_REQUEST;
		String extra_result = mActivity.loadString(R.string.intent_scan_extra_result);
		String extra_result_format = mActivity.loadString(R.string.intent_scan_extra_result_format);
		Intent intent = new Intent(mActivity, AudioActivity.class);
		intent.putExtra(extra_result, "");
		intent.putExtra(extra_result_format, "");
		Common.processActivityResult(mActivity, code_scan, Activity.RESULT_OK, intent);
		assertNull(getStartedActivityIntent());

		Common.processActivityResult(mActivity, code_scan, Activity.RESULT_CANCELED, intent);
		assertNull(getStartedActivityIntent());


		code_scan = R.integer.CODE_SCAN_2_ACTIVITY_REQUEST;
		extra_result = mActivity.loadString(R.string.intent_scan_2_extra_result);
		intent = new Intent(mActivity, AudioActivity.class);
		intent.putExtra(extra_result, "");
		Common.processActivityResult(mActivity, code_scan, Activity.RESULT_OK, intent);
		assertNull(getStartedActivityIntent());

		Common.processActivityResult(mActivity, code_scan, Activity.RESULT_CANCELED, intent);
		assertNull(getStartedActivityIntent());

		Common.processActivityResult(mActivity, code_scan, Activity.RESULT_OK, intent);
		assertNull(getStartedActivityIntent());

		intent.putExtra(extra_result, "");
		intent.putExtra(extra_result_format, mActivity.loadString(R.string.intent_result_qr));
		Common.processActivityResult(mActivity, code_scan, Activity.RESULT_OK, intent);
		assertNull(getStartedActivityIntent());

		intent.putExtra(extra_result, "Lynx");
		intent.putExtra(extra_result_format, mActivity.loadString(R.string.intent_result_qr));
		Common.processActivityResult(mActivity, code_scan, Activity.RESULT_OK, intent);
		assertNull(getStartedActivityIntent());

		ExhibitList exhibitList = ContentManager.getExhibitList();
		ArrayList<String> names = new ArrayList<String>();
		for (int i=0; i<exhibitList.getCount(); i++){
			names.add(exhibitList.getExhibitAt(i).getName());
		}

		String prefix = "market://search?q=pname:org.wildlifeimages.android.wildlifeimages&extra=Wildlife_Images_Exhibit_";
		
		for (String name : names){
			intent.putExtra(extra_result, prefix + name);
			intent.putExtra(extra_result_format, mActivity.loadString(R.string.intent_result_qr));
			Common.processActivityResult(mActivity, code_scan+1, Activity.RESULT_OK, intent);
			Intent started = getStartedActivityIntent();
			assertNull(started);
		}
		
		for (String name : names){
			intent.putExtra(extra_result, prefix + name);
			intent.putExtra(extra_result_format, mActivity.loadString(R.string.intent_result_qr));
			Common.processActivityResult(mActivity, code_scan+1, Activity.RESULT_CANCELED, intent);
			Intent started = getStartedActivityIntent();
			assertNull(started);
		}

		for (String name : names){
			intent.putExtra(extra_result, prefix + name);
			intent.putExtra(extra_result_format, mActivity.loadString(R.string.intent_result_qr));
			Common.processActivityResult(mActivity, code_scan, Activity.RESULT_OK, intent);
			Intent started = getStartedActivityIntent();
			assertNotNull(started);
			assertEquals(name, started.getStringExtra("Exhibit"));
		}
	}

}
