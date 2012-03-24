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
			mMapView.setZoomFactor(savedState.getFloat(loadString(R.string.save_map_zoom)));
		}

		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
	}

	protected void onSaveInstanceState(Bundle out){
		MapView mMapView = (MapView) findViewById(R.id.map);
		float[] position = mMapView.getPosition();

		out.putFloat(loadString(R.string.save_map_x), position[0]);
		out.putFloat(loadString(R.string.save_map_y), position[1]);
		out.putFloat(loadString(R.string.save_map_zoom), mMapView.getZoomFactor());
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
		Dialog parent = super.onCreateDialog(id);

		if (parent != null){
			return parent;
		}

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

	/**
	 * When you click on the map or gesture, this will interpret your input.
	 */
	private class MapGestureListener implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {  
		private WireActivity parent;

		public MapGestureListener(MapActivity context) { 
			parent = context;
		}

		public boolean onDoubleTap(MotionEvent e) {
			return false;
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
			return true;
		}

		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			return false;
		}

		public void onLongPress(MotionEvent e) {
		}

		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			MapView mMapView = (MapView) findViewById(R.id.map);

			mMapView.processScroll(distanceX, distanceY);

			return true;
		}

		public void onShowPress(MotionEvent e) {
		}

		public boolean onSingleTapUp(MotionEvent e) {
			return false;
		}  
	} 
}
