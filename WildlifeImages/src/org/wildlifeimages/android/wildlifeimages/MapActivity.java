package org.wildlifeimages.android.wildlifeimages;

import android.content.Context;
import android.content.Intent;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * This class will handle the map page.
 * 
 * @author Graham Wilkinson
 * @author Shady Glenn
 * @author Naveen Nanja
 */
public class MapActivity extends WireActivity{

	/**
	 * Invoked when the Activity is created.
	 * 
	 * @param savedState a Bundle containing state saved from a previous execution,
	 * or null if this is a new execution
	 */
	@Override
	public void onCreate(Bundle savedState) {
		super.onCreate(savedState);

		setContentView(R.layout.tour_layout);

		MapView mMapView;
		mMapView = (MapView) findViewById(R.id.map);

		mMapView.setExhibitList(ContentManager.getSelf().getExhibitList());
		mMapView.setGestureDetector(new GestureDetector(this, new MapGestureListener(this)));
		//mMapView.setParent(this);
	}

	/**
	 * Invoked when the Activity is created.
	 * 
	 * @param a Context context that has the location where we are in program flow.
	 */
	public static void start(Context context) {
		Intent mapIntent = new Intent(context, MapActivity.class);
		context.startActivity(mapIntent);
	}

	/**
	 * When you click on the map or gesture, this will interpret your input.
	 */
	private class MapGestureListener implements GestureDetector.OnGestureListener, 
	GestureDetector.OnDoubleTapListener {  
		WireActivity parent;

		public MapGestureListener(MapActivity context) { 
			parent = context;
		}

		//@Override
		public boolean onDoubleTap(MotionEvent e) {
			MapView mMapView = (MapView) findViewById(R.id.map);
			
			mMapView.zoomIn(e.getX(), e.getY());
			return true;
		}
		//@Override
		public boolean onDoubleTapEvent(MotionEvent e) {
			return false;
		}
		//@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			MapView mMapView = (MapView) findViewById(R.id.map);
			
			float x = e.getX();
			float y = e.getY();
			Matrix m = new Matrix();
			mMapView.getImageMatrix().invert(m);
			
			float[] xy = {x,y};
			m.mapPoints(xy);
			
			float percentHoriz = xy[0]*100/mMapView.mapWidth;
			float percentVert = xy[1]*100/mMapView.mapHeight;
			Log.w(this.getClass().getName(), x + "," + y);
			Log.w(this.getClass().getName(), percentHoriz + "," + percentVert);
			
			ExhibitList exhibitList = ContentManager.getSelf().getExhibitList();
			Exhibit selectedExhibit = exhibitList.findNearest((int)percentHoriz, (int)percentVert);
			if(selectedExhibit != null){
				exhibitList.setCurrent(selectedExhibit, Exhibit.TAG_AUTO);

				ExhibitActivity.start(parent);
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
			MapView mMapView = (MapView) findViewById(R.id.map);
			mMapView.zoomOut();
		}
		//@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			MapView mMapView = (MapView) findViewById(R.id.map);
			mMapView.processScroll(distanceX, distanceY);
			
			return true;
		}
		//@Override
		public void onShowPress(MotionEvent e) {
		}
		//@Override
		public boolean onSingleTapUp(MotionEvent e) {
			return false;
		}  
	} 
}
