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
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
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
class MapView extends ImageView {

	private static final boolean DEBUG = false;

	private boolean doZoom = false;

	private GestureDetector gestures;

	private float originX;
	private float originY;
	private float scale;

	public final int mapWidth;
	public final int mapHeight;

	private static final Paint p = new Paint();
	private static final Paint rectP = new Paint();
	private static final Paint activeP = new Paint();
	private float[] points;
	private float[] transformedPoints;
	private String[] displayNames;
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
		if (DEBUG){
			float[][] anchorPoints = ContentManager.getSelf().getExhibitList().getAnchorPoints();
			for (int i=0; i<anchorPoints.length; i++){
				float[] pts = {anchorPoints[i][0]*mapWidth, anchorPoints[i][1]*mapHeight};
				getImageMatrix().mapPoints(pts);
				float l = pts[0];
				float t = pts[1];
				canvas.drawCircle(l, t, anchorPoints[i][2]*10, activeP); 
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
		if (doZoom){
			if (originX + distanceX < getXFromFraction(0.10f)){
				originX = originX - (originX - getXFromFraction(0.10f))/2.0f;
			}else if(originX + distanceX > getXFromFraction(0.85f)){
				originX = originX + (getXFromFraction(0.85f) - originX)/2.0f;
			}else{
				originX = originX + distanceX;
			}
			if (originY + distanceY < getYFromFraction(0.15f)){
				originY = originY - (originY - getYFromFraction(0.15f))/2.0f;
			}else if(originY + distanceY > getYFromFraction(0.80f)){
				originY = originY + (getYFromFraction(0.80f) - originY)/2.0f;
			}else{
				originY = originY + distanceY;
			}

			float xFraction = (float)Math.max(0.0f, getXFraction(originX));
			float yFraction = (float)Math.max(0.0f, getYFraction(originY));

			ExhibitList exhibitList = ContentManager.getSelf().getExhibitList();

			scale = exhibitList.getScale(xFraction, yFraction);

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
}
