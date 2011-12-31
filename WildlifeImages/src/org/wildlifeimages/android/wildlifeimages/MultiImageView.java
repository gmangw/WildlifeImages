package org.wildlifeimages.android.wildlifeimages;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
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
	private String[] shortUrlList = new String[0];
	private int currentBitmapIndex;
	private WebContentManager webManager = null;

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

	private Bitmap getBitmap(String shortUrl, WebContentManager webManager){
		return webManager.getBitmap(shortUrl, this.getContext().getAssets());
	}

	public void setImageBitmapList(String[] shortUrlList, WebContentManager webManager){
		//TODO shortUrlList can be modified from here if desired
		this.shortUrlList = shortUrlList;
		currentBitmapIndex = webManager.getMostRecentIndex(shortUrlList);
		setImageBitmap(getBitmap(shortUrlList[currentBitmapIndex], webManager));
		this.webManager = webManager;
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

	public boolean onDown(MotionEvent e) {
		if (shortUrlList.length > 1){
			return true;
		}else{
			return false;
		}
	}

	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		if (velocityX < 0){
			if (currentBitmapIndex < (shortUrlList.length-1)){
				currentBitmapIndex++;
				setImageBitmap(getBitmap(shortUrlList[currentBitmapIndex], webManager));
			}
		}else{
			if (currentBitmapIndex > 0){
				currentBitmapIndex--;
				setImageBitmap(getBitmap(shortUrlList[currentBitmapIndex], webManager));
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
