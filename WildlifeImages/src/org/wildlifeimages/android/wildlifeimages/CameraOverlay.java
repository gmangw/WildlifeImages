package org.wildlifeimages.android.wildlifeimages;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class CameraOverlay extends View {

	public CameraOverlay(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	public void onDraw(Canvas canvas){
		super.onDraw(canvas);
		
		Paint p = new Paint();
		p.setARGB(127, 0, 255, 0);
		
		canvas.drawRect(0, 0, 30, 30, p);
	}
	
}
