package org.wildlifeimages.android.wildlifeimages;

import java.util.Enumeration;
import java.util.Iterator;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


/**
 * This {@link SurfaceView} draws a map of the facility with clickable points from an {@link ExhibitList} 
 * 
 * @author Graham Wilkinson 
 * 	
 */
class MapView extends SurfaceView implements SurfaceHolder.Callback {

	private ExhibitList exhibitList;
	private TourApp parent;

	private GestureDetector gestures;

	private int originX = 0;
	private int originY = 0;

	public MapView(Context context, AttributeSet attrs) {
		super(context, attrs);

		// register our interest in hearing about changes to our surface
		SurfaceHolder holder = getHolder();
		holder.addCallback(this);

		gestures = new GestureDetector(context, new GestureListener(this));

		//this.setBackgroundDrawable( context.getResources().getDrawable( R.drawable.map) );
		this.setBackgroundColor(Color.WHITE);
		setFocusable(true); // make sure we get key events
	}

	public void setParent(TourApp app){
		parent = app;
	}

	public void setExhibitList(ExhibitList list){
		exhibitList = list;
	}

	@Override  
	public boolean onTouchEvent(MotionEvent event) { 
		return gestures.onTouchEvent(event);  
	}  

	private class GestureListener implements GestureDetector.OnGestureListener, 
	GestureDetector.OnDoubleTapListener {  

		public GestureListener(MapView view) {   
		}

		//@Override
		public boolean onDoubleTap(MotionEvent e) {
			return false;
		}
		//@Override
		public boolean onDoubleTapEvent(MotionEvent e) {
			return false;
		}
		//@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {

			float percentHoriz = 100*(e.getX()/getWidth());
			float percentVert = 100*(e.getY()/(3*getWidth()/4));

			Exhibit selectedExhibit = exhibitList.findNearest((int)percentHoriz, (int)percentVert);
			if(selectedExhibit != null){
				parent.exhibitSwitch(selectedExhibit, Exhibit.TAG_AUTO);
			}

			return true;
		}
		//@Override
		public boolean onDown(MotionEvent e) {
			return true;
		}
		//@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			return false;
		}
		//@Override
		public void onLongPress(MotionEvent e) {
		}
		//@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			return false;
		}
		//@Override
		public void onShowPress(MotionEvent e) {
		}
		//@Override
		public boolean onSingleTapUp(MotionEvent e) {
			return false;
		}  
	} 

	@Override
	public void onDraw(Canvas canvas){
		super.onDraw(canvas);

		Drawable map = this.getResources().getDrawable(R.drawable.map);
		map.setBounds(originX, originY, this.getWidth(), 3*this.getWidth()/4);
		map.draw(canvas);

		/*
    	Paint p = new Paint();
    	p.setARGB(255, 0, 0, 255);

    	Iterator<String> list = exhibitList.keys();

    	while(list.hasNext()){
    		Exhibit e = exhibitList.get(list.next());
    		canvas.drawCircle(e.getX()*getWidth()/100, e.getY()*(3*getWidth()/4)/100, 10, p);
		}
		*/
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

	/**
	 * Standard window-focus override. Notice focus lost so we can pause on
	 * focus lost. e.g. user switches to take a call.
	 */
	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		//TODO
	}

	/* Callback invoked when the surface dimensions change. */
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	/*
	 * Callback invoked when the Surface has been created and is ready to be
	 * used.
	 */
	public void surfaceCreated(SurfaceHolder holder) {
	}

	/*
	 * Callback invoked when the Surface has been destroyed and must no longer
	 * be touched. WARNING: after this method returns, the Surface/Canvas must
	 * never be touched again!
	 */
	public void surfaceDestroyed(SurfaceHolder holder) {
	}


}
