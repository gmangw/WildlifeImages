package org.wildlifeimages.android.wildlifeimages.test;

import java.util.Random;

import org.wildlifeimages.android.wildlifeimages.AVActivity;
import org.wildlifeimages.android.wildlifeimages.Common;
import org.wildlifeimages.android.wildlifeimages.IntroActivity;
import org.wildlifeimages.android.wildlifeimages.R;

import android.app.Activity;
import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.widget.Button;

public class IntroActivityTest extends ActivityInstrumentationTestCase2<IntroActivity> {

	private IntroActivity mActivity;

	public IntroActivityTest() {
		super("org.wildlifeimages.android.wildlifeimages", IntroActivity.class);

	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		mActivity = this.getActivity();
	}

	public void testPreconditions() {
		assertNotNull(mActivity);
	}
	
	public void testCommonIsIntentAvailable(){
		assertFalse(Common.isIntentAvailable(null, null));
		assertFalse(Common.isIntentAvailable(null, "com.google.zxing.client.android.SCAN"));		
		assertFalse(Common.isIntentAvailable(mActivity, null));
		assertFalse(Common.isIntentAvailable(mActivity, ""));
		assertFalse(Common.isIntentAvailable(mActivity, "NonExistent"));
		assertFalse(Common.isIntentAvailable(mActivity, "SCAN"));
		
		assertTrue(Common.isIntentAvailable(mActivity, mActivity.getResources().getString(org.wildlifeimages.android.wildlifeimages.R.string.intent_action_scan)));
		assertTrue(Common.isIntentAvailable(mActivity, "com.google.zxing.client.android.SCAN"));
	}
	
	public void testCommonIsImageUrl(){
		assertFalse(Common.isImageUrl(""));
		assertFalse(Common.isImageUrl("beaver"));
		assertFalse(Common.isImageUrl(".jpg"));
		assertFalse(Common.isImageUrl(".jp"));
		assertFalse(Common.isImageUrl("jpg"));
		assertFalse(Common.isImageUrl(".jpg"));
		
		assertTrue(Common.isImageUrl("beaver.jpg"));
		assertTrue(Common.isImageUrl("beaver.jpeg"));
		assertTrue(Common.isImageUrl("beaver.gif"));
		assertTrue(Common.isImageUrl("beaver.bmp"));
		assertTrue(Common.isImageUrl("beaver.png"));
	}
	
	public void testCommonDistance(){
		assertEquals(1.0f, Common.distance(0.0f, 0.0f, 1.0f, 0.0f), TestHost.DELTA);
		assertEquals(Math.sqrt(2.0f), Common.distance(0.0f, 0.0f, 1.0f, 1.0f), TestHost.DELTA);
		assertEquals(0.0f, Common.distance(0.0f, 0.0f, 0.0f, 0.0f), TestHost.DELTA);
		
		assertEquals(Float.NaN, Common.distance(Float.NaN, 0.0f, 1.0f, 0.0f));
		assertEquals(Float.NaN, Common.distance(Float.NaN, Float.NaN, Float.NaN, Float.NaN));
		assertEquals(Float.POSITIVE_INFINITY, Common.distance(Float.POSITIVE_INFINITY, 0.0f, 0.0f, 0.0f));
		assertEquals(Float.POSITIVE_INFINITY, Common.distance(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, 0.0f, 0.0f), Float.POSITIVE_INFINITY);
		assertEquals(Float.POSITIVE_INFINITY, Common.distance(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, 0.0f, 0.0f));
		assertEquals(Float.POSITIVE_INFINITY, Common.distance(Float.NEGATIVE_INFINITY, 0.0f, Float.POSITIVE_INFINITY, 0.0f));
		
		assertEquals(Float.MIN_VALUE, Common.distance(Float.MIN_VALUE, Float.MIN_VALUE, 0.0f, 0.0f));
	}
	
	public void testCommonClamp(){
		Random rand = new Random(12345);
        
		float value;
		float clamp1;
		float clamp2;
		float min;
		float max;
		float result;
		
		for (int i=0; i<100000; i++){
	        value = Float.intBitsToFloat(rand.nextInt());
	        clamp1 = Float.intBitsToFloat(rand.nextInt());
	        clamp2 = Float.intBitsToFloat(rand.nextInt());
	        min = Math.min(clamp1, clamp2);
	        max = Math.max(clamp1, clamp2);
			result = Common.clamp(value, clamp1, clamp2);
			assertTrue(Float.compare(result, max) <= +0.0f || 0.0f == Float.compare(value, Float.NaN) || 0.0f == Float.compare(max, Float.NaN));
			assertTrue(Float.compare(result, min) >= +0.0f || 0.0f == Float.compare(value, Float.NaN) || 0.0f == Float.compare(min, Float.NaN));
		}
	}
	
	public void testCommonSmoothStep(){
		Random rand = new Random(12345);
        
		float value;
		float clamp1;
		float clamp2;
		float result;
		
		for (int i=0; i<100000; i++){
	        value = Float.intBitsToFloat(rand.nextInt());
	        clamp1 = Float.intBitsToFloat(rand.nextInt());
	        clamp2 = Float.intBitsToFloat(rand.nextInt());
			result = Common.smoothStep(clamp1, clamp2, value);
			assertTrue(result + "", Float.compare(result, 1.0f+TestHost.DELTA) <= +0.0f || 0.0f == Float.compare(value, Float.NaN) || 0.0f == Float.compare(clamp1, Float.NaN) || 0.0f == Float.compare(clamp2, Float.NaN));
			assertTrue(result + "", Float.compare(result, 0.0f-TestHost.DELTA) >= +0.0f || 0.0f == Float.compare(value, Float.NaN) || 0.0f == Float.compare(clamp1, Float.NaN) || 0.0f == Float.compare(clamp2, Float.NaN));
		}
	}
	
	public void testCommonProcessResultQR(){
		int code_scan = R.integer.CODE_SCAN_ACTIVITY_REQUEST;
		String extra_result = mActivity.loadString(R.string.intent_scan_extra_result);
		String extra_result_format = mActivity.loadString(R.string.intent_scan_extra_result_format);
		Intent intent = new Intent(mActivity, AVActivity.class);
		intent.putExtra(extra_result, "");
		intent.putExtra(extra_result_format, "");
		Common.processActivityResult(mActivity, code_scan, Activity.RESULT_OK, intent);
		assertFalse(mActivity.isFinishing());
		
		Common.processActivityResult(mActivity, code_scan, Activity.RESULT_CANCELED, intent);
		assertFalse(mActivity.isFinishing());
		
		
		code_scan = R.integer.CODE_SCAN_2_ACTIVITY_REQUEST;
		extra_result = mActivity.loadString(R.string.intent_scan_2_extra_result);
		intent = new Intent(mActivity, AVActivity.class);
		intent.putExtra(extra_result, "");
		Common.processActivityResult(mActivity, code_scan, Activity.RESULT_OK, intent);
		assertFalse(mActivity.isFinishing());
		
		Common.processActivityResult(mActivity, code_scan, Activity.RESULT_CANCELED, intent);
		assertFalse(mActivity.isFinishing());
		
		
		Common.processActivityResult(mActivity, code_scan+1, Activity.RESULT_OK, intent);
		assertFalse(mActivity.isFinishing());
		
		intent.putExtra(extra_result, "");
		intent.putExtra(extra_result_format, mActivity.loadString(R.string.intent_result_qr));
		Common.processActivityResult(mActivity, code_scan+1, Activity.RESULT_OK, intent);
		assertFalse(mActivity.isFinishing());
		
		intent.putExtra(extra_result, "Lynx");
		intent.putExtra(extra_result_format, mActivity.loadString(R.string.intent_result_qr));
		Common.processActivityResult(mActivity, code_scan+1, Activity.RESULT_OK, intent);
		assertFalse(mActivity.isFinishing());
		
		String prefix = "market://search?q=pname:org.wildlifeimages.android.wildlifeimages&extra=Wildlife_Images_Exhibit_";
		intent.putExtra(extra_result, prefix + "Lynx");
		Log.e(this.getClass().getName(), mActivity.loadString(R.string.qr_prefix) + "Lynx");
		intent.putExtra(extra_result_format, mActivity.loadString(R.string.intent_result_qr));
		Common.processActivityResult(mActivity, code_scan+1, Activity.RESULT_OK, intent);
		assertFalse(mActivity.isFinishing());
	}
}
