package org.wildlifeimages.android.wildlifeimages;

import java.util.Iterator;

import com.larvalabs.svgandroid.SVG;
import com.larvalabs.svgandroid.SVGParser;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Picture;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageView;
/**
 * This {@link SurfaceView} draws a map of the facility with clickable points from an {@link ExhibitList} 
 * 
 * @author Graham Wilkinson 
 * 	
 */
class MapView extends ImageView {

	private ExhibitList exhibitList;

	private GestureDetector gestures;

	private float originX;
	private float originY;
	private float scale;
	
	public final int mapWidth;
	public final int mapHeight;

	public MapView(Context context, AttributeSet attrs) {
		super(context, attrs);

		SVG svg = SVGParser.getSVGFromResource(getResources(), R.raw.map);
	    Drawable drawable = svg.createPictureDrawable();
		this.setImageDrawable(drawable);
		mapWidth = drawable.getIntrinsicWidth();
		mapHeight = drawable.getIntrinsicHeight();
		scale = 0.75f;
		originX = 0;
		originY = 0;		
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh){
		super.onSizeChanged(w, h, oldw, oldh);
		
		originX = -w/2;
		originY = -h/2;//TODO
		doTransform();
	}
	
	public void setExhibitList(ExhibitList list){
		exhibitList = list;
	}
	
	private void doTransform(){
		Matrix m = new Matrix();
		m.setScale(scale, scale);
		m.postTranslate((-originX) - scale*(mapWidth/2), (-originY) - scale*(mapHeight/2));
		
		this.setImageMatrix(m);
		invalidate();
	}

	@Override  
	public boolean onTouchEvent(MotionEvent event) { 
		return gestures.onTouchEvent(event);  
	}  

	@Override
	public void onDraw(Canvas canvas){
		super.onDraw(canvas);

		Paint p = new Paint();
		Paint rectP = new Paint();
		
		p.setARGB(255, 255, 255, 255);
		p.setTextSize(20);
		p.setTypeface(Typeface.SERIF);
		p.setTextAlign(Paint.Align.CENTER);
		p.setAntiAlias(true);
		
		rectP.setARGB(200, 128, 128, 128);

		Iterator<String> list = exhibitList.keys();

		Matrix m = this.getImageMatrix();
		
		while(list.hasNext()){ 
			Exhibit e = exhibitList.get(list.next());
			float x = e.getX()*mapWidth/100;
			float y = e.getY()*mapHeight/100;
			float[] xy = {x,y};
			m.mapPoints(xy);
			String name = e.getName();
			
			Rect r = new Rect();
			p.getTextBounds(name, 0, name.length(), r);
			r.offsetTo((int)xy[0] - r.width()/2, (int)xy[1] - r.height() + 3); 
			r.inset(-3, -4);
			canvas.drawRect(r, rectP);
			canvas.drawText(name, xy[0], xy[1], p);
		}
	}

	/**
	 * Standard override to get key-press events.
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent msg) {
		return false;
	}

	/**
	 * Standard override for key-up. 
	 */
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent msg) {
		return false;
	}
	
	public void setGestureDetector(GestureDetector gestureListener) {
		gestures = gestureListener;
	}

	public void processScroll(float distanceX, float distanceY){
		originX += distanceX;
		originY += distanceY;
		doTransform();
		
	}

	public void zoomIn(float x, float y) {
		scale *= 1.1f;
		doTransform();
	}

	public void zoomOut() {
		scale = 1.0f;
		doTransform();
	}
}
