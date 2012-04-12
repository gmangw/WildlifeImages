package org.wildlifeimages.android.wildlifeimages;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.KeyEvent;
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
	private ExhibitPhoto[] photoList = new ExhibitPhoto[0];
	private int currentBitmapIndex;
	private int xScrollOffset = 0;
	private Paint labelPaint = new Paint();
	private TextPaint labelTextPaint = new TextPaint();
	private boolean isEmpty = true;
	private StaticLayout captionTextLayout = null;
	private final int gray = Color.argb(100, 255, 255, 255);
	private final int[] colors = {gray, gray, gray, gray, gray, gray};
	float[] rightArrow = {getWidth(), getHeight()/2.0f, getWidth()-50, getHeight()/2.0f-30, getWidth()-50, getHeight()/2.0f+30};
	float[] leftArrow = {0, getHeight()/2.0f, 50, getHeight()/2.0f-30, 50, getHeight()/2.0f+30};

	RectF bmpRect = new RectF();

	public MultiImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		gestures = new GestureDetector(context, this);
		this.setFocusable(true);

		labelPaint.setARGB(127, 0, 0, 0);
		labelPaint.setAntiAlias(true);

		labelTextPaint.setARGB(255, 255, 255, 255);
		labelTextPaint.setTextSize(getResources().getInteger(R.integer.image_caption_size));
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
		if (isEmpty == false){
			setCaption();
		}
		
		float[] tempRight = {getWidth(), getHeight()/2.0f, getWidth()-50, getHeight()/2.0f-30, getWidth()-50, getHeight()/2.0f+30};
		float[] tempLeft = {0, getHeight()/2.0f, 50, getHeight()/2.0f-30, 50, getHeight()/2.0f+30};
		rightArrow = tempRight;
		leftArrow = tempLeft;
	}

	private Bitmap getBitmap(String shortUrl){
		return ContentManager.getBitmap(shortUrl, this.getContext().getAssets());
	}

	public void setImageBitmapList(ExhibitPhoto[] shortUrlList){
		this.photoList = shortUrlList;
		currentBitmapIndex = ContentManager.getMostRecentPhoto(shortUrlList);
		setImageBitmap(getBitmap(shortUrlList[currentBitmapIndex].shortUrl));
	}

	@Override
	public void setImageBitmap(Bitmap bmp){
		super.setImageBitmap(bmp);

		if (bmp != null){
			bmpRect = new RectF(0, 0, bmp.getWidth(), bmp.getHeight());
			reMeasureMatrix();
			isEmpty = false;
			setCaption();
		}else{
			isEmpty = true;
		}
	}

	private void setCaption(){
		String caption = photoList[currentBitmapIndex].getCaption();
		if (caption == null || caption.length() == 0){
			captionTextLayout = null;
		}else{
			captionTextLayout = new StaticLayout(caption, labelTextPaint, getWidth(), Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);	
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
		if (photoList.length > 1){
			return true;
		}else{
			return false;
		}
	}

	private void scrollRight(){
		if (hasNextImage()){
			currentBitmapIndex++;
			setImageBitmap(getBitmap(photoList[currentBitmapIndex].shortUrl));
		}
	}

	private void scrollLeft(){
		if (hasPreviousImage()){
			currentBitmapIndex--;
			setImageBitmap(getBitmap(photoList[currentBitmapIndex].shortUrl));
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
		return (currentBitmapIndex < (photoList.length-1));
	}

	private boolean hasPreviousImage(){
		return (currentBitmapIndex > 0);
	}

	public void onLongPress(MotionEvent e) {
	}

	@Override
	public void onDraw(Canvas canvas){
		super.onDraw(canvas);

		if (captionTextLayout != null){
			canvas.save();
			canvas.translate(0, getHeight() - captionTextLayout.getHeight());
			canvas.drawRect(0, 0, captionTextLayout.getWidth(), captionTextLayout.getHeight(), labelPaint);
			captionTextLayout.draw(canvas);
			canvas.restore();
		}

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
		if (e.getY() < getHeight()*0.80f){
			if (e.getX() > getWidth()*0.80f){
				scrollRight();
				return true;
			}
			if (e.getX() < getWidth()*0.20f){
				scrollLeft();
				return true;
			}
		}else{
			//Toast.makeText(getContext(), shortUrlList[currentBitmapIndex] + ", " + shortUrlList[currentBitmapIndex], Toast.LENGTH_LONG).show();
		}
		return false;
	} 

	public boolean isEmpty(){
		return isEmpty;
	}
}
