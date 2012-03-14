package org.wildlifeimages.android.wildlifeimages;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ListAdapter;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

/**
 * This class will handle the map page.
 * 
 * @author Graham Wilkinson
 * @author Shady Glenn
 * @author Naveen Nanja
 */
public class MapActivity extends WireActivity{

	private static final float ZOOM_FACTOR_START = 0.5f;

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

		MapView mMapView = (MapView) findViewById(R.id.map);

		mMapView.setGestureDetector(new GestureDetector(this, new MapGestureListener(this)));
		if (savedState != null){
			mMapView.processMove(savedState.getFloat(loadString(R.string.save_map_x)), savedState.getFloat(loadString(R.string.save_map_y)));
			if (true == savedState.getBoolean(loadString(R.string.save_map_zoom))){
				mMapView.toggleZoom();
			}
		}

		SeekBar slider = (SeekBar)findViewById(R.id.seek);
		slider.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				float zoomFactor = (.25f + progress/200.0f)*1.5f;
				MapView mMapView = (MapView) findViewById(R.id.map);
				mMapView.setZoomFactor(zoomFactor);
				mMapView.processScroll(0.0f, 0.0f);
			}

			public void onStartTrackingTouch(SeekBar seekBar) {}

			public void onStopTrackingTouch(SeekBar seekBar) {}
		});

		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		int ht = displaymetrics.heightPixels;
		int wt = displaymetrics.widthPixels;
		if (wt > 0){
			int size = Math.max(ht, wt);
			float zoomFactor = size / 800.0f;
			mMapView.setZoomFactor(zoomFactor * ZOOM_FACTOR_START);
		}
	}

	protected void onSaveInstanceState(Bundle out){
		MapView mMapView = (MapView) findViewById(R.id.map);
		float[] position = mMapView.getPosition();

		out.putFloat(loadString(R.string.save_map_x), position[0]);
		out.putFloat(loadString(R.string.save_map_y), position[1]);
		out.putBoolean(loadString(R.string.save_map_zoom), mMapView.isZoomed());
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

	private String findClickedExhibit(MapView mMapView, float x, float y){
		Matrix m = new Matrix();
		mMapView.getImageMatrix().invert(m);

		float[] xy = {x,y};
		m.mapPoints(xy);

		float percentHoriz = xy[0]*100/mMapView.mapWidth;
		float percentVert = xy[1]*100/mMapView.mapHeight;

		String selectedExhibit = mMapView.findNearest((int)percentHoriz, (int)percentVert);
		return selectedExhibit;
	}

	private class GroupListListener implements DialogInterface.OnClickListener{
		private final String name;
		private final MapActivity self;

		public GroupListListener(String groupName, MapActivity parent){
			name = groupName;
			self = parent;
		}

		public void onClick(DialogInterface dialog, int item) {
			ExhibitList exhibitList = ContentManager.getSelf().getExhibitList();
			exhibitList.setCurrent(exhibitList.getGroup(name)[item], Exhibit.TAG_AUTO);
			ExhibitActivity.start(self);	
		}
	}

	@Override
	protected Dialog onCreateDialog(int id){
		super.onCreateDialog(id);

		ExhibitList exhibitList = ContentManager.getSelf().getExhibitList();
		String groupName = null;
		String[] names = exhibitList.getGroupNames();

		for (String name : names){
			if (name.hashCode() == id){
				groupName = name;
				break;
			}
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(groupName);
		builder.setInverseBackgroundForced(false);

		ListAdapter adapter = new GroupListAdapter(groupName);

		DialogInterface.OnClickListener listener = new GroupListListener(groupName, this);

		builder.setAdapter(adapter, listener);
		AlertDialog alert = builder.create();
		return alert;
	}

	@Override
	public boolean onTouchEvent(MotionEvent e){
		return false;
	}

	/**
	 * When you click on the map or gesture, this will interpret your input.
	 */
	private class MapGestureListener implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {  
		private WireActivity parent;

		private boolean currentlyScrolling = false;
		private float previousX0 = -0.0f;
		private float previousY0 = -0.0f;
		private float previousX1 = -0.0f;
		private float previousY1 = -0.0f;

		public MapGestureListener(MapActivity context) { 
			parent = context;
		}

		public boolean onDoubleTap(MotionEvent e) {
			MapView mMapView = (MapView) findViewById(R.id.map);
			mMapView.toggleZoom();
			return true;
		}

		public boolean onDoubleTapEvent(MotionEvent e) {
			return false;
		}

		public boolean onSingleTapConfirmed(MotionEvent e) {
			MapView mMapView = (MapView) findViewById(R.id.map);
			ExhibitList exhibitList = ContentManager.getSelf().getExhibitList();
			String selectedExhibit = findClickedExhibit(mMapView, e.getX(), e.getY());

			if(selectedExhibit != null){
				if (exhibitList.getGroup(selectedExhibit).length > 0){
					showDialog(selectedExhibit.hashCode());
				}else{
					exhibitList.setCurrent(exhibitList.get(selectedExhibit), Exhibit.TAG_AUTO);
					ExhibitActivity.start(parent);	
				}
			}
			return true;
		}

		public boolean onDown(MotionEvent e) {
			MapView mMapView = (MapView) findViewById(R.id.map);
			String selectedExhibit = findClickedExhibit(mMapView, e.getX(), e.getY());
			if(selectedExhibit != null){
				mMapView.showPress(selectedExhibit);
			}
			currentlyScrolling = false;
			return true;
		}

		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			return false;
		}

		public void onLongPress(MotionEvent e) {
		}

		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			MapView mMapView = (MapView) findViewById(R.id.map);
			if (e2.getPointerCount() == 2){
				if (currentlyScrolling == false){
					currentlyScrolling = true;
				}else{
					float previousDistance = Common.distance(previousX0, previousY0,previousX1, previousY1);
					float newDistance = Common.distance(e2.getX(0), e2.getY(0), e2.getX(1), e2.getY(1));
					Log.d(this.getClass().getName(), "Distance " +Math.abs(previousDistance - newDistance));
					if (Math.abs(previousDistance - newDistance) > 1.0f){

						if (previousDistance < newDistance){
							mMapView.setZoomFactor(mMapView.getZoomFactor()* 1.1f);
						}else{
							mMapView.setZoomFactor(mMapView.getZoomFactor()* 0.9f);
						}
					}
				}
				mMapView.processScroll(0, 0);
				previousX0 = e2.getX(0);
				previousY0 = e2.getY(0);
				previousX1 = e2.getX(1);
				previousY1 = e2.getY(1);
			}else if (currentlyScrolling == false){
				mMapView.processScroll(distanceX, distanceY);
			}


			return true;
		}

		public void onShowPress(MotionEvent e) {
		}

		public boolean onSingleTapUp(MotionEvent e) {
			return false;
		}  
	} 
}
