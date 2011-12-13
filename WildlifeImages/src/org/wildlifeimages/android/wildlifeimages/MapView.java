package org.wildlifeimages.android.wildlifeimages;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;


/**
 *
 */
class MapView extends SurfaceView implements SurfaceHolder.Callback {


    /** Pointer to the text view to display "Paused.." etc. */
    private TextView mStatusText;

    private GestureDetector gestures;
    
    public MapView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // register our interest in hearing about changes to our surface
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);

        gestures = new GestureDetector(context, new GestureListener(this));
        
        this.setBackgroundDrawable( context.getResources().getDrawable(
                R.drawable.facilitymap) );
        setFocusable(true); // make sure we get key events
    }

    @Override  
    public boolean onTouchEvent(MotionEvent event) { 
        return gestures.onTouchEvent(event);  
    }  
    
    private class GestureListener implements GestureDetector.OnGestureListener, 
    		GestureDetector.OnDoubleTapListener {  
		MapView view;  
		public GestureListener(MapView view) {  
		    this.view = view;  
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
			return false;
		}
		//@Override
		public boolean onDown(MotionEvent e) {
			return true;
		}
		//@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			// X and Y grow from top left
			if(e1.getY() > e2.getY()){
			}
			return true;
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

    /**
     * Standard override to get key-press events.
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent msg) {
        return false;
    }

    /**
     * Standard override for key-up. We actually care about these, so we can
     * turn off the engine or stop rotating.
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
    }

    /**
     * Installs a pointer to the text view used for messages.
     */
    public void setTextView(TextView textView) {
        mStatusText = textView;
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
