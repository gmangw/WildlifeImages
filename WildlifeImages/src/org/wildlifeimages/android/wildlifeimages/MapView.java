package org.wildlifeimages.android.wildlifeimages;

import java.util.Iterator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
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

	private int originX = 0;
	private int originY = 0;

	public MapView(Context context, AttributeSet attrs) {
		super(context, attrs);

		Matrix m = new Matrix();
		m.setScale(0.75f, 0.75f);
		this.setImageMatrix(m);
	}

	public void setExhibitList(ExhibitList list){
		exhibitList = list;
	}

	@Override  
	public boolean onTouchEvent(MotionEvent event) { 
		return gestures.onTouchEvent(event);  
	}  

	@Override
	public void onDraw(Canvas canvas){
		super.onDraw(canvas);

		Drawable map = this.getResources().getDrawable(R.drawable.map);

		Paint p = new Paint();
		p.setARGB(255, 0, 0, 255);

		Iterator<String> list = exhibitList.keys();

		Matrix m = this.getImageMatrix();
		
		while(list.hasNext()){ 
			Exhibit e = exhibitList.get(list.next());
			float x = e.getX()*map.getIntrinsicWidth()/100;
			float y = e.getY()*map.getIntrinsicHeight()/100;
			float[] xy = {x,y};
			m.mapPoints(xy);
			canvas.drawCircle(xy[0], xy[1], 10, p);
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

	public void processSingleTap(WireActivity context, float x, float y){
		Matrix m = new Matrix();
		getImageMatrix().invert(m);
		Drawable map = this.getResources().getDrawable(R.drawable.map);
		
		float[] xy = {x,y};
		m.mapPoints(xy);
		
		float percentHoriz = xy[0]*100/map.getIntrinsicWidth();
		float percentVert = xy[1]*100/map.getIntrinsicHeight();
		Log.w(this.getClass().getName(), x + "," + y);
		Log.w(this.getClass().getName(), percentHoriz + "," + percentVert);
		
		Exhibit selectedExhibit = exhibitList.findNearest((int)percentHoriz, (int)percentVert);
		if(selectedExhibit != null){
			exhibitList.setCurrent(selectedExhibit, Exhibit.TAG_AUTO);

			ExhibitActivity.start(context);
		}
	}
}
