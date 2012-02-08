package org.wildlifeimages.android.wildlifeimages;

import java.util.Iterator;

import com.larvalabs.svgandroid.SVG;
import com.larvalabs.svgandroid.SVGParser;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.ImageView;
/**
 * This {@link SurfaceView} draws a map of the facility with clickable points from an {@link ExhibitList} 
 * 
 * @author Graham Wilkinson 
 * 	
 */
class MapView extends ImageView {

	private GestureDetector gestures;

	private float originX;
	private float originY;
	private float scale;

	public final int mapWidth;
	public final int mapHeight;

	private static final Paint p = new Paint();
	private static final Paint rectP = new Paint();
	private static final Paint activeP = new Paint();
	private final float[] points;
	private float[] transformedPoints;
	private final String[] exhibitNames;
	private static String activeName = "";

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

		p.setARGB(255, 255, 255, 255);
		p.setTextSize(20);
		p.setTypeface(Typeface.SERIF);
		p.setTextAlign(Paint.Align.CENTER);
		p.setAntiAlias(true);

		rectP.setARGB(200, 128, 128, 128);
		activeP.setARGB(255, 0, 128, 255);

		ExhibitList exhibitList = ContentManager.getSelf().getExhibitList();

		Iterator<String> list = exhibitList.keys();

		exhibitNames = new String[exhibitList.getCount()];

		points = new float[exhibitList.getCount()*2];
		int i = 0;
		while(list.hasNext()){ 
			Exhibit e = exhibitList.get(list.next());
			points[i] = e.getX()*mapWidth/100;
			i++;
			points[i] = e.getY()*mapHeight/100;
			i++;
			exhibitNames[i/2-1] = e.getName();
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh){
		super.onSizeChanged(w, h, oldw, oldh);

		processScroll(0f, 0f);
		doTransform();
	}

	private void doTransform(){
		Matrix m = new Matrix();
		m.setScale(scale, scale);
		float translateX = (-(originX - getWidth()/2)) - scale*(mapWidth/2);
		float translateY = (-(originY - getHeight()/2)) - scale*(mapHeight/2);
		m.postTranslate(translateX, translateY);

		this.setImageMatrix(m);

		transformedPoints = points.clone();
		m.mapPoints(transformedPoints);

		invalidate();
	}

	@Override  
	public boolean onTouchEvent(MotionEvent event) { 
		if (event.getAction() == MotionEvent.ACTION_UP){
			activeName = "";
		}
		invalidate();
		return gestures.onTouchEvent(event);  
	}  

	@Override
	public void onDraw(Canvas canvas){
		super.onDraw(canvas);

		for(int i=0; i<points.length/2; i++){
			Rect r = new Rect();
			p.getTextBounds(exhibitNames[i], 0, exhibitNames[i].length(), r);
			r.offsetTo((int)transformedPoints[i*2] - r.width()/2, (int)transformedPoints[i*2+1] - r.height() + 3); 
			r.inset(-3, -4);
			if (exhibitNames[i].equals(activeName)){
				canvas.drawRect(r, activeP);
			}else{
				canvas.drawRect(r, rectP);
			}
			canvas.drawText(exhibitNames[i], transformedPoints[i*2], transformedPoints[i*2+1], p);
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
		final float zoomlow = 0.75f;
		final float zoomhi = 1.5f;
		final float zoomdiff = zoomhi - zoomlow;
		final float focalX = 0.30f;
		final float focalY = 0.25f;
		
		originX = clamp(originX + distanceX, getXFromFraction(0.0f), getXFromFraction(1.0f));
		originY = clamp(originY + distanceY, getYFromFraction(0.0f), getYFromFraction(1.0f));
		
		
		float xFraction = (float)Math.max(0.0f, getXFraction(originX));
		float yFraction = (float)Math.max(0.0f, getYFraction(originY));
		float distance = distance(xFraction, yFraction, focalX, focalY);
		scale = (zoomhi+zoomlow)-(zoomlow + zoomdiff*(float)smoothStep(0f, 0.75f, distance));
		//Log.d(this.getClass().getName(), xFraction + ", " + yFraction);
		doTransform();
	}
	
	private float getXFraction(float x){
		return x/scale/mapWidth+0.5f;
	}
	
	private float getYFraction(float y){
		return y/scale/mapHeight+0.5f;
	}
	
	private float getXFromFraction(float xFraction){
		return scale*mapWidth*(xFraction - 0.5f);
	}
	
	private float getYFromFraction(float yFraction){
		return scale*mapHeight*(yFraction - 0.5f);
	}

	public void zoomIn(float x, float y) {
		scale *= 1.1f;
		doTransform();
	}

	public void zoomOut() {
		scale = 1.0f;
		doTransform();
	}

	public void showPress(String name) {
		activeName = name;
	}

	public static float distance(float x1, float y1, float x2, float y2){
		return (float)(Math.sqrt(Math.pow(x2-x1, 2) + Math.pow(y2-y1, 2)));
	}

	public static float clamp(float value, float min, float max){
		if (value < min){
			value = min;
		}else if (value > max){
			value = max;
		}
		return value;
	}
	
	//http://en.wikipedia.org/wiki/Smoothstep
	public static float smoothStep(float edge0, float edge1, float x){
		// Scale, and clamp x to 0..1 range
		x = clamp((x - edge0)/(edge1 - edge0), 0, 1);
		// Evaluate polynomial
		return x*x*x*(x*(x*6 - 15) + 10);
	}
}
