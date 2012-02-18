package org.wildlifeimages.android.wildlifeimages;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
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
	private ContentManager contentManager = null;
	private int xScrollOffset = 0;

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

	private Bitmap getBitmap(String shortUrl, ContentManager contentManager){
		return contentManager.getBitmap(shortUrl, this.getContext().getAssets());
	}

	public void setImageBitmapList(String[] shortUrlList, ContentManager contentManager){
		//TODO shortUrlList can be modified from here if desired
		this.shortUrlList = shortUrlList;
		currentBitmapIndex = contentManager.getMostRecentIndex(shortUrlList);
		setImageBitmap(getBitmap(shortUrlList[currentBitmapIndex], contentManager));
		this.contentManager = contentManager;
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
			xScrollOffset = 0;
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
			if (hasNextImage()){
				currentBitmapIndex++;
				setImageBitmap(getBitmap(shortUrlList[currentBitmapIndex], contentManager));
			}
		}else{
			if (hasPreviousImage()){
				currentBitmapIndex--;
				setImageBitmap(getBitmap(shortUrlList[currentBitmapIndex], contentManager));
			}
		}
		return false;
	}

	private boolean hasNextImage(){
		return (currentBitmapIndex < (shortUrlList.length-1));
	}

	private boolean hasPreviousImage(){
		return (currentBitmapIndex > 0);
	}

	public void onLongPress(MotionEvent e) {
	}

	@Override
	public void onDraw(Canvas canvas){
		super.onDraw(canvas);

		Paint p = new Paint();
		p.setARGB(127, 0, 0, 0);
		p.setTextSize(30);
		p.setAntiAlias(true);

		String label = (currentBitmapIndex+1) + "/" + shortUrlList.length;

		int bottom = canvas.getClipBounds().bottom;

		canvas.drawRect(0, bottom - p.getTextSize(), p.measureText(label)+2, bottom, p);

		p.setARGB(165, 255, 255, 255);

		canvas.drawText(label, 1, bottom-1, p);
	}

	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {

		Matrix m = new Matrix(baseMatrix);

		xScrollOffset += distanceX;
		if (false == hasNextImage()){
			xScrollOffset = Math.min(0, xScrollOffset);
		}
		if (false == hasPreviousImage()){
			xScrollOffset = Math.max(0, xScrollOffset);
		}
		m.postTranslate(-xScrollOffset, 0);
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
