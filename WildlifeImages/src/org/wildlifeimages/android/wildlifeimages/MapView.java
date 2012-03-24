package org.wildlifeimages.android.wildlifeimages;

import java.util.ArrayList;
import java.util.Iterator;

import org.wildlifeimages.android.wildlifeimages.Exhibit.Alias;

import com.larvalabs.svgandroid.SVG;
import com.larvalabs.svgandroid.SVGParser;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ZoomButtonsController;
import android.widget.ZoomButtonsController.OnZoomListener;
/**
 * This {@link SurfaceView} draws a map of the facility with clickable points from an {@link ExhibitList} 
 * 
 * @author Graham Wilkinson 
 * 	
 */
public class MapView extends ImageView {

	private static float mapLeft = 0.10f;
	private static float mapTop = 0.215f;
	private static float mapRight = 0.80f;
	private static float mapBottom = 0.63f;

	private GestureDetector gestures;

	private float originX;
	private float originY;
	private float scale;
	private float scaleMin = Float.MIN_VALUE;

	public final int mapWidth;
	public final int mapHeight;

	private boolean currentlyScrolling = false;
	private float previousX0 = -0.0f;
	private float previousY0 = -0.0f;
	private float previousX1 = -0.0f;
	private float previousY1 = -0.0f;

	private static final Paint p = new Paint();
	private static final Paint smallP = new Paint();
	private static final Paint rectP = new Paint();
	private static final Paint activeP = new Paint();
	private float[] points;
	private float[] transformedPoints;
	private String[] displayNames;
	private static String activeName = "";

	private float zoomFactor = 0.25f;
	private float zoomMinimum = 1.00f;
	private float[][] anchorPoints = {
			{0.00f, 0.00f, 1.50f},
			{1.00f, 0.00f, 1.50f},
			{1.00f, 1.00f, 1.50f},
			{0.00f, 1.00f, 1.50f}
	};
	/*private float[][] anchorPoints = {
			{0.00f, 0.00f, 1.90f},
			{0.00f, 0.25f, 2.50f}, 
			{0.00f, 0.50f, 1.80f},
			{0.00f, 0.75f, 1.70f},
			{0.00f, 1.00f, 1.60f},

			{0.25f, 0.00f, 2.00f},
			{0.25f, 0.25f, 3.00f}, 
			{0.25f, 0.50f, 1.80f},
			{0.25f, 0.75f, 1.70f},
			{0.25f, 1.00f, 1.60f}, 

			{0.50f, 0.00f, 1.90f},
			{0.50f, 0.25f, 2.50f}, 
			{0.50f, 0.50f, 1.30f},
			{0.50f, 0.75f, 1.30f},
			{0.50f, 1.00f, 1.60f},

			{0.75f, 0.00f, 1.90f},
			{0.75f, 0.25f, 2.60f}, 
			{0.75f, 0.50f, 1.60f},
			{0.75f, 0.75f, 1.50f},
			{0.75f, 1.00f, 1.40f},

			{1.00f, 0.00f, 1.90f},
			{1.00f, 0.25f, 1.80f},
			{1.00f, 0.50f, 1.70f},
			{1.00f, 0.75f, 1.60f},
			{1.00f, 1.00f, 1.60f},
	};*/

	private final ZoomButtonsController zoomButtons;

	public MapView(Context context, AttributeSet attrs) {
		super(context, attrs);

		SVG svg = SVGParser.getSVGFromResource(getResources(), R.raw.map);
		Drawable drawable = svg.createPictureDrawable();
		this.setImageDrawable(drawable);
		mapWidth = drawable.getIntrinsicWidth();
		mapHeight = drawable.getIntrinsicHeight();
		scale = Float.MAX_VALUE;
		originX = 0;
		originY = 0;

		p.setARGB(255, 255, 255, 255);
		p.setTextSize(20);
		p.setTypeface(Typeface.SERIF);
		p.setTextAlign(Paint.Align.CENTER);
		p.setAntiAlias(true);

		smallP.setARGB(255, 255, 255, 255);
		smallP.setTextSize(12);
		smallP.setTypeface(Typeface.SERIF);
		smallP.setTextAlign(Paint.Align.CENTER);
		smallP.setAntiAlias(true);

		rectP.setARGB(200, 128, 128, 128);
		activeP.setARGB(255, 0, 128, 255);

		ExhibitList exhibitList = ContentManager.getSelf().getExhibitList();
		setPoints(exhibitList);

		zoomButtons = new ZoomButtonsController(this);
		zoomButtons.setZoomSpeed(50);
		zoomButtons.setOnZoomListener(new OnZoomListener(){

			public void onVisibilityChanged(boolean visible) {
			}

			public void onZoom(boolean zoomIn) {
				if (zoomIn){
					zoomIn();
				}else{
					zoomOut();
				}
				processScroll(0, 0);
			}
		});
	}

	@Override
	protected void onVisibilityChanged(View changedView, int visibility){
		if (visibility != View.VISIBLE){
			zoomButtons.setVisible(false);
		}
	}

	@Override
	protected void onDetachedFromWindow (){
		zoomButtons.setVisible(false);
	}

	private void setPoints(ExhibitList exhibitList){
		Iterator<String> list = exhibitList.keys();

		ArrayList<Float> pointList = new ArrayList<Float>();
		ArrayList<String> nameList = new ArrayList<String>();

		while (list.hasNext()){ 
			Exhibit e = exhibitList.get(list.next());
			int x = e.getX();
			int y = e.getY();
			if (x != -1 || y != -1){
				pointList.add(1.0f * x *mapWidth/100);
				pointList.add(1.0f * y *mapHeight/100);
				nameList.add(e.getName());
			}
			for (Alias a : e.getAliases()){
				if (a.xPos != -1 || a.yPos != -1){
					pointList.add(1.0f * a.xPos *mapWidth/100);
					pointList.add(1.0f * a.yPos *mapHeight/100);
					nameList.add(a.name);
				}
			}
		}
		for (String group : exhibitList.getGroupNames()){
			int x = exhibitList.getGroupX(group);
			int y = exhibitList.getGroupY(group);
			if (x != -1 || y != -1){
				pointList.add(1.0f * x *mapWidth/100);
				pointList.add(1.0f * y *mapHeight/100);
				nameList.add(group);
			}
		}

		displayNames = nameList.toArray(new String[nameList.size()]);

		points = new float[pointList.size()];
		for (int i=0; i<points.length; i++){
			points[i] = pointList.get(i);
		}

		doTransform();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh){
		super.onSizeChanged(w, h, oldw, oldh);

		scaleMin = Math.min(1.0f*getWidth()/mapWidth, 1.0f*getHeight()/mapHeight);
		scale = scaleMin;
		processScroll(0f, 0f);
	}

	private void doTransform(){
		if (getWidth() != 0 && getHeight() != 0){
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
	}

	@Override  
	public boolean onTouchEvent(MotionEvent event) { 
		if (event.getAction() == MotionEvent.ACTION_UP){
			activeName = "";
			currentlyScrolling = false;
		}
		invalidate();
		if (event.getAction() == MotionEvent.ACTION_MOVE){
			if (event.getPointerCount() == 2){
				performPinchZoom(event);
				return true;
			}
		}
		zoomButtons.setVisible(true);
		return gestures.onTouchEvent(event);  
	}  

	private void performPinchZoom(MotionEvent e){
		if (e.getPointerCount() == 2){
			if (currentlyScrolling == false){
				currentlyScrolling = true;
			}else{
				float previousDistance = Common.distance(previousX0, previousY0,previousX1, previousY1);
				float newDistance = Common.distance(e.getX(0), e.getY(0), e.getX(1), e.getY(1));
				if (Math.abs(previousDistance - newDistance) > 1.0f){
					processScroll(0.0f, 0.0f);
					if (previousDistance < newDistance){
						zoomIn();
					}else{
						zoomOut();
					}
				}
			}
			processScroll(0, 0);
			previousX0 = e.getX(0);
			previousY0 = e.getY(0);
			previousX1 = e.getX(1);
			previousY1 = e.getY(1);
		}
	}

	public void zoomIn(){
		setZoomFactor(getZoomFactor()* 1.075f);
	}

	public void zoomOut(){
		setZoomFactor(getZoomFactor()* 0.925f);
	}

	@Override
	public void onDraw(Canvas canvas){
		super.onDraw(canvas);

		for(int i=0; i<points.length/2; i++){
			Rect r = new Rect();
			rectP.setTextAlign(Align.CENTER);

			p.getTextBounds(displayNames[i], 0, displayNames[i].length(), r);
			r.offsetTo((int)transformedPoints[i*2] - r.width()/2, (int)transformedPoints[i*2+1] - r.height() + 3); 
			r.inset(-3, -4);


			if (displayNames[i].equals(activeName)){
				canvas.drawRect(r, activeP);
			}else{
				canvas.drawRect(r, rectP);
			}

			canvas.drawText(displayNames[i], transformedPoints[i*2], transformedPoints[i*2+1], p);
		}
	}

	public void setGestureDetector(GestureDetector gestureListener) {
		gestures = gestureListener;
	}

	public void processMove(float x, float y){
		originX = x;
		originY = y;
		processScroll(0.0f, 0.0f);
	}

	public float[] getPosition(){
		float[] position = {originX, originY};
		return position;
	}

	public void processScroll(float distanceX, float distanceY){
		for (int i = 0; i < 2; i++){
			float temp = scale/scaleMin;
			mapTop = 0.5f/temp;
			mapBottom = 1.0f - 0.5f/temp;
			mapLeft = 0.5f/temp;
			mapRight = 1.0f - 0.5f/temp;
			if (originX + distanceX < getXFromFraction(mapLeft)){
				originX = originX - (originX - getXFromFraction(mapLeft))/2.0f;
			}else if(originX + distanceX > getXFromFraction(mapRight)){
				originX = originX + (getXFromFraction(mapRight) - originX)/2.0f;
			}else{
				originX = originX + distanceX;
			}
			if (originY + distanceY < getYFromFraction(mapTop)){
				originY = originY - (originY - getYFromFraction(mapTop))/2.0f;
			}else if(originY + distanceY > getYFromFraction(mapBottom)){
				originY = originY + (getYFromFraction(mapBottom) - originY)/2.0f;
			}else{
				originY = originY + distanceY;
			}

			float xFraction = (float)Math.max(0.0f, getXFraction(originX));
			float yFraction = (float)Math.max(0.0f, getYFraction(originY));

			scale = getScale(xFraction, yFraction);
			distanceX = 0.0f;
			distanceY = 0.0f;
		}

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

	public void showPress(String name) {
		activeName = name;
	}

	public String findNearest(int percentHoriz, int percentVert) {
		float minDistance = 100000;
		String closest = null;

		for(int i=0; i<displayNames.length; i++){
			float x = points[i*2]/mapWidth*100.0f;
			float y = points[i*2+1]/mapHeight*100.0f;
			float d = Common.distance(percentHoriz, percentVert, x, y);
			if(d < minDistance){
				minDistance = d;
				closest = displayNames[i];
			}
		}
		if (minDistance < 5){
			return closest;
		}else{
			return null;
		}
	}

	public float[][] getAnchorPoints() {
		return anchorPoints;
	}

	public float getScale(float xFraction, float yFraction) {
		float newScale = 0.0f;

		//TODO do this more efficiently?
		for (int i=0; i<anchorPoints.length; i++){
			float distance = Common.distance(xFraction, yFraction, anchorPoints[i][0], anchorPoints[i][1]);
			//TODO I no longer remember what this next line is actually doing
			float zoomCandidate = (anchorPoints[i][2]+zoomMinimum)-(zoomMinimum + (anchorPoints[i][2]-zoomMinimum)*(float)Common.smoothStep(0f, 0.75f, distance));
			newScale = Math.max(newScale, zoomCandidate);
		}

		float result = zoomFactor * newScale;
		if (result < scaleMin){
			zoomFactor = scaleMin / newScale;
			return scaleMin;
		}
		return result;
	}

	public float getZoomFactor(){
		return zoomFactor;
	}

	public void setZoomFactor(float zoomFactor) {
		this.zoomFactor = zoomFactor;
	}

	public void setZoomMinimum(float zoomMinimum) {
		this.zoomMinimum = zoomMinimum;
	}
}
