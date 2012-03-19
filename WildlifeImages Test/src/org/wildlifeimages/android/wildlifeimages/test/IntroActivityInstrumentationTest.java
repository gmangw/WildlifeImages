package org.wildlifeimages.android.wildlifeimages.test;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.Random;

import org.wildlifeimages.android.wildlifeimages.Common;
import org.wildlifeimages.android.wildlifeimages.IntroActivity;
import org.wildlifeimages.android.wildlifeimages.R;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

public class IntroActivityInstrumentationTest extends ActivityInstrumentationTestCase2<IntroActivity> {

	private IntroActivity mActivity;

	public IntroActivityInstrumentationTest() {
		super(IntroActivity.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		mActivity = this.getActivity();
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

		for (int i=0; i<10000; i++){
			value = Float.intBitsToFloat(rand.nextInt());
			clamp1 = Float.intBitsToFloat(rand.nextInt());
			clamp2 = Float.intBitsToFloat(rand.nextInt());
			result = Common.smoothStep(clamp1, clamp2, value);
			assertTrue(result + "", Float.compare(result, 1.0f+TestHost.DELTA) <= +0.0f || 0.0f == Float.compare(value, Float.NaN) || 0.0f == Float.compare(clamp1, Float.NaN) || 0.0f == Float.compare(clamp2, Float.NaN));
			assertTrue(result + "", Float.compare(result, 0.0f-TestHost.DELTA) >= +0.0f || 0.0f == Float.compare(value, Float.NaN) || 0.0f == Float.compare(clamp1, Float.NaN) || 0.0f == Float.compare(clamp2, Float.NaN));
		}
	}

	public void testCommonWriteBytesToFile() throws IOException{
		byte[] test1 = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
		byte[] test2 = new byte[1024];
		byte[] test3 = new byte[4096];

		Random rand = new Random(123456);
		rand.nextBytes(test2);
		rand.nextBytes(test3);

		byte[][] contentList = {test1, test2, test3};

		for (byte[] content : contentList){

			File f = File.createTempFile(rand.nextInt()+"", ".tmp", mActivity.getFilesDir());
			Common.writeBytesToFile(content, f);
			BufferedInputStream s = new BufferedInputStream(new FileInputStream(f));
			byte[] buffer = new byte[1];
			for (byte b : content){
				s.read(buffer);
				assertEquals(b, buffer[0]);
			}

			assertTrue(f.delete());
		}

		File f = File.createTempFile(rand.nextInt()+"", ".tmp", mActivity.getFilesDir());
		try{
			Common.writeBytesToFile(null, f);
			fail();
		}catch(NullPointerException e){
		}

		try{
			Common.writeBytesToFile(new byte[0], null);
			fail();
		}catch(NullPointerException e){
		}
	}

	public void testCommonMkdirForFile() throws IOException{
		Random rand = new Random(123456);

		String filename = rand.nextInt()+".tmp";
		File f = new File(mActivity.getFilesDir(), filename);
		assertFalse(f.exists());
		Common.mkdirForFile(f);
		f = File.createTempFile(filename, "", mActivity.getFilesDir());
		assertTrue(f.exists());
		assertTrue(f.delete());

		filename = rand.nextInt()+".tmp";
		File nestedDir = new File(mActivity.getFilesDir().getPath()+"/does/not/exist");
		assertFalse(nestedDir.exists());
		f = new File(nestedDir, filename);
		Log.e(this.getClass().getName(), f.getPath());
		Common.mkdirForFile(f);
		f = File.createTempFile(filename, "", mActivity.getFilesDir());
		assertTrue(f.exists());
		assertTrue(f.delete());

		assertTrue(nestedDir.delete());
		assertTrue(new File(mActivity.getFilesDir().getPath()+"/does/not").delete());
		assertTrue(new File(mActivity.getFilesDir().getPath()+"/does").delete());
	}

	public void testCommonGetZipUrl() throws IOException{
		assertNull(Common.getZipUrl(""));
		assertNull(Common.getZipUrl("http://www.google.com"));
	}

	public void testBitmapMax(){
		AssetManager assets = mActivity.getAssets();
		Bitmap[] list = new Bitmap[200];
		int i;
		for(i=0; i<list.length; i++){
			try{
				InputStream stream = assets.open("ExhibitContents/Badger/Badger-Boogie-1.jpg");
				list[i] = BitmapFactory.decodeStream(stream);
				stream.close();
			}catch(OutOfMemoryError e){
				break;
			} catch (IOException e) {
				Log.e(Common.class.getName(), "Failed to load bitmap from assets");
			}
		}
		for (int k = 0; k<i; k++){
			list[k].recycle();
		}
		Log.w(Common.class.getName(), "Loaded " + i + " bitmaps");
		assertTrue(i > 20);
	}
}
