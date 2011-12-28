package org.wildlifeimages.android.wildlifeimages;

import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ImageView;

/**
 * A modified ImageView which contains multiple images, navigable using touch controls.
 * 
 * @author Graham Wilkinson 
 * 		
 */
public class MultiImageView extends ImageView implements GestureDetector.OnGestureListener{

	private GestureDetector gestures;
	private Matrix baseMatrix = new Matrix();
	private String[] bitmapList = new String[0];
	private int currentBitmapIndex;

	RectF bmpRect = new RectF();

	public MultiImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		gestures = new GestureDetector(context, this);
	}

	private void reMeasureMatrix(){
		Matrix m = this.getImageMatrix();
		Rect viewRect = new Rect();
		this.getDrawingRect(viewRect);
		RectF viewRectF = new RectF(viewRect);
		m.setRectToRect(bmpRect, viewRectF, Matrix.ScaleToFit.CENTER);
		this.setImageMatrix(m);
		baseMatrix = new Matrix(m);
		this.setScaleType(ScaleType.MATRIX);
	}

	@Override
	public void onSizeChanged(int w, int h, int oldw, int oldh){
		super.onSizeChanged(w, h, oldw, oldh);

		reMeasureMatrix();
	}

	public void setImageBitmapList(String[] bmpList){
		bitmapList = bmpList.clone();
		currentBitmapIndex = 0;
		setImageBitmap(getBitmapFromAsset(bitmapList[currentBitmapIndex]));
	}

	@Override
	public void setImageBitmap(Bitmap bmp){
		super.setImageBitmap(bmp);
		if (bmp != null){
			bmpRect = new RectF(0, 0, bmp.getWidth(), bmp.getHeight());

			reMeasureMatrix();
		}
	}

	@Override  
	public boolean onTouchEvent(MotionEvent event) { 
		if (event.getAction() == MotionEvent.ACTION_UP){
			this.setImageMatrix(baseMatrix);
		}
		return gestures.onTouchEvent(event);  
	}

	/* http://stackoverflow.com/questions/2752924/android-images-from-assets-folder-in-a-gridview */
	private Bitmap getBitmapFromAsset(String imgUrl)
	{	
		String filename = imgUrl.replaceAll("file:///android_asset/", "");

		try{
			AssetManager assetManager = this.getContext().getAssets();

			InputStream istr = assetManager.open(filename);
			Bitmap bitmap = BitmapFactory.decodeStream(istr);

			return bitmap;
		}catch(IOException e){
			Log.w(this.getClass().getName(), "Asset filename " + filename + " is bad");
			return null;
		}
	}

	public boolean onDown(MotionEvent e) {
		if (bitmapList.length > 1){
			return true;
		}else{
			return false;
		}
	}

	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		if (velocityX < 0){
			if (currentBitmapIndex < (bitmapList.length-1)){
				currentBitmapIndex++;
				setImageBitmap(getBitmapFromAsset(bitmapList[currentBitmapIndex]));
			}
		}else{
			if (currentBitmapIndex > 0){
				currentBitmapIndex--;
				setImageBitmap(getBitmapFromAsset(bitmapList[currentBitmapIndex]));
			}
		}
		return false;
	}

	public void onLongPress(MotionEvent e) {
	}

	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {

		Matrix m = this.getImageMatrix();

		m.postTranslate(-distanceX, 0);
		this.setImageMatrix(m);

		this.invalidate();

		return false;
	}

	public void onShowPress(MotionEvent e) {
	}

	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	} 
}
