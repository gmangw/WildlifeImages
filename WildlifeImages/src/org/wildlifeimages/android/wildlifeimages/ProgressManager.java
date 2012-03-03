package org.wildlifeimages.android.wildlifeimages;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;

public class ProgressManager implements OnCancelListener {

	private ProgressDialog dialog;

	public UpdateListener finishListener = null;

	private boolean isCancelled = false;

	public ProgressManager() {
	}

	public void incrementProgressBy(int amount) {
		if (dialog != null){
			dialog.incrementProgressBy(amount);
		}
	}

	public void incrementSecondaryProgressBy(int amount){
		if (dialog != null){
			dialog.incrementSecondaryProgressBy(amount);
		}
	}

	public void setProgress(int amount) {
		if (dialog != null){
			dialog.setProgress(amount);
		}
	}

	public void setSecondaryProgress(int amount){
		if (dialog != null){
			dialog.setSecondaryProgress(amount);
		}
	}

	public int getProgress(){
		if (dialog != null){
			return dialog.getProgress();
		}else{
			return 0;
		}
	}

	public void setDialog(ProgressDialog progressDialog){
		dialog = progressDialog;
		dialog.setOnCancelListener(this);
	}

	public void dismiss(boolean result) {
		if (dialog != null){
			dialog.dismiss();
			dialog = null;
		}

		if (finishListener != null){
			finishListener.onUpdateCompleted(result);
		}
	}

	public void show(){
		if (dialog != null){
			dialog.show();
		}
	}

	public void setText(String label) {
		if (dialog != null){
			dialog.setMessage(label);
		}		
	}

	public boolean isCancelled(){
		return isCancelled;
	}

	public void onCancel(DialogInterface dialog) {
		isCancelled = true;
	}

	public void reset() {
		isCancelled = false;
	}

	public void registerUpdateListener(UpdateListener listener) {
		finishListener = listener;
	}

	public Context getContext(){
		if (dialog != null){
			return dialog.getContext();
		}else{
			return null;
		}
	}
}
