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
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ImageView;
/**
 * This {@link SurfaceView} draws a map of the facility with clickable points from an {@link ExhibitList} 
 * 
 * @author Graham Wilkinson 
 * 	
 */
public class MapView extends ImageView {

	private static final float MAP_LEFT = 0.10f;
	private static final float MAP_TOP = 0.215f;
	private static final float MAP_RIGHT = 0.80f;
	private static final float MAP_BOTTOM = 0.63f;

	private boolean doZoom = false;

	private GestureDetector gestures;

	private float originX;
	private float originY;
	private float scale;

	public final int mapWidth;
	public final int mapHeight;

	private static final Paint p = new Paint();
	private static final Paint smallP = new Paint();
	private static final Paint rectP = new Paint();
	private static final Paint activeP = new Paint();
	private float[] points;
	private float[] transformedPoints;
	private String[] displayNames;
	private static String activeName = "";
	
	private float zoomFactor = 0.75f;
	private float zoomMinimum = 1.00f;
	private float zoomExponent = 1.00f;
	private float[][] anchorPoints = {
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
	};

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

		smallP.setARGB(255, 255, 255, 255);
		smallP.setTextSize(12);
		smallP.setTypeface(Typeface.SERIF);
		smallP.setTextAlign(Paint.Align.CENTER);
		smallP.setAntiAlias(true);

		rectP.setARGB(200, 128, 128, 128);
		activeP.setARGB(255, 0, 128, 255);

		ExhibitList exhibitList = ContentManager.getSelf().getExhibitList();
		setPoints(exhibitList);
	}

	public void toggleZoom(){
		doZoom = !doZoom;
		doTransform();
		processScroll(0.0f, 0.0f);
	}

	public boolean isZoomed(){
		return doZoom;
	}

	private void setPoints(ExhibitList exhibitList){
		Iterator<String> list = exhibitList.keys();

		ArrayList<Float> pointList = new ArrayList<Float>();
		ArrayList<String> nameList = new ArrayList<String>();

		for (; list.hasNext(); ){ 
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

		processScroll(0f, 0f);
		doTransform();
	}

	private void doTransform(){
		Matrix m = new Matrix();
		if (doZoom){
			m.setScale(scale, scale);
			float translateX = (-(originX - getWidth()/2)) - scale*(mapWidth/2);
			float translateY = (-(originY - getHeight()/2)) - scale*(mapHeight/2);
			m.postTranslate(translateX, translateY);
		}else{
			RectF rSrc = new RectF(0, 0, mapWidth, mapHeight);
			RectF rDst = new RectF(0, 0, getWidth(), getHeight());
			m.setRectToRect(rSrc, rDst, Matrix.ScaleToFit.CENTER);
		}

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
			rectP.setTextAlign(Align.CENTER);
			if (doZoom == true){
				p.getTextBounds(displayNames[i], 0, displayNames[i].length(), r);
				r.offsetTo((int)transformedPoints[i*2] - r.width()/2, (int)transformedPoints[i*2+1] - r.height() + 3); 
				r.inset(-3, -4);
			}else{
				smallP.getTextBounds(displayNames[i], 0, displayNames[i].length(), r);
				r.offsetTo((int)transformedPoints[i*2] - r.width()/2, (int)transformedPoints[i*2+1] - r.height() + 3); 
				r.inset(-1, -1);
			}

			if (displayNames[i].equals(activeName)){
				canvas.drawRect(r, activeP);
			}else{
				canvas.drawRect(r, rectP);
			}

			if (doZoom == true){
				canvas.drawText(displayNames[i], transformedPoints[i*2], transformedPoints[i*2+1], p);
			}else{
				canvas.drawText(displayNames[i], transformedPoints[i*2], transformedPoints[i*2+1], smallP);
			}
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
		if (Math.abs(distanceX) > 1.0f || Math.abs(distanceY) > 1.0f){
			doZoom = true;
		}
		if (doZoom){
			if (originX + distanceX < getXFromFraction(MAP_LEFT)){
				originX = originX - (originX - getXFromFraction(MAP_LEFT))/2.0f;
			}else if(originX + distanceX > getXFromFraction(MAP_RIGHT)){
				originX = originX + (getXFromFraction(MAP_RIGHT) - originX)/2.0f;
			}else{
				originX = originX + distanceX;
			}
			if (originY + distanceY < getYFromFraction(MAP_TOP)){
				originY = originY - (originY - getYFromFraction(MAP_TOP))/2.0f;
			}else if(originY + distanceY > getYFromFraction(MAP_BOTTOM)){
				originY = originY + (getYFromFraction(MAP_BOTTOM) - originY)/2.0f;
			}else{
				originY = originY + distanceY;
			}

			float xFraction = (float)Math.max(0.0f, getXFraction(originX));
			float yFraction = (float)Math.max(0.0f, getYFraction(originY));

			scale = getScale(xFraction, yFraction);

			doTransform();
		}
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

		for (int i=0; i<anchorPoints.length; i++){
			float distance = (float)Math.pow(Common.distance(xFraction, yFraction, anchorPoints[i][0], anchorPoints[i][1]), zoomExponent);
			float zoomCandidate = (anchorPoints[i][2]+zoomMinimum)-(zoomMinimum + (anchorPoints[i][2]-zoomMinimum)*(float)Common.smoothStep(0f, 0.75f, distance));
			newScale = Math.max(newScale, zoomCandidate);
		}

		//Log.i(this.getClass().getName(), ""+newScale);
		return zoomFactor * newScale;
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

	public void setZoomExponent(float zoomExponent) {
		this.zoomExponent = zoomExponent;
	}
}
