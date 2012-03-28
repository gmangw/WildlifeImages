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
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
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
	private Paint labelPaint = new Paint();
	private Paint labelTextPaint = new Paint();
	
	int gray = Color.argb(100, 255, 255, 255);

	RectF bmpRect = new RectF();

	public MultiImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		gestures = new GestureDetector(context, this);
		this.setFocusable(true);
		
		labelPaint.setARGB(127, 0, 0, 0);
		labelPaint.setAntiAlias(true);
		
		labelTextPaint.setARGB(165, 255, 255, 255);
		labelTextPaint.setTextSize(30);
		labelTextPaint.setAntiAlias(true);
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
	public boolean onKeyDown(int keyCode, KeyEvent event){
		switch(keyCode){
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			scrollRight();
			return true;
		case KeyEvent.KEYCODE_DPAD_LEFT:
			scrollLeft();
			return true;
		default:
			return false;
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

	private void scrollRight(){
		if (hasNextImage()){
			currentBitmapIndex++;
			setImageBitmap(getBitmap(shortUrlList[currentBitmapIndex], contentManager));
		}
	}

	private void scrollLeft(){
		if (hasPreviousImage()){
			currentBitmapIndex--;
			setImageBitmap(getBitmap(shortUrlList[currentBitmapIndex], contentManager));
		}
	}

	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		if (velocityX < 0){
			scrollRight();
		}else{
			scrollLeft();
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

	private final Rect bounds = new Rect();
	
	@Override
	public void onDraw(Canvas canvas){
		super.onDraw(canvas);

		String label = (currentBitmapIndex+1) + "/" + shortUrlList.length;

		canvas.getClipBounds(bounds); 
		int bottom = bounds.bottom;

		canvas.drawRect(0, bottom - labelTextPaint.getTextSize(), labelTextPaint.measureText(label)+2, bottom, labelPaint);
		canvas.drawText(label, 1, bottom-1, labelTextPaint);

		float[] rightArrow = {bounds.right, bounds.exactCenterY(), bounds.right-50, bounds.exactCenterY()-30, bounds.right-50, bounds.exactCenterY()+30};
		float[] leftArrow = {0, bounds.exactCenterY(), 50, bounds.exactCenterY()-30, 50, bounds.exactCenterY()+30};

		int[] colors = {gray, gray, gray, gray, gray, gray};
		
		if (hasNextImage()){
			canvas.drawVertices(Canvas.VertexMode.TRIANGLES, 6, rightArrow, 0, null, 0, colors, 0, null, 0, 0, labelPaint);
		}
		if (hasPreviousImage()){
			canvas.drawVertices(Canvas.VertexMode.TRIANGLES, 6, leftArrow, 0, null, 0, colors, 0, null, 0, 0, labelPaint);
		}
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
		if (e.getX() > getWidth()*0.80f){
			scrollRight();
			return true;
		}
		if (e.getX() < getWidth()*0.20f){
			scrollLeft();
			return true;
		}
		return false;
	} 
}
