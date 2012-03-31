package org.wildlifeimages.tools.update;

import java.util.ArrayList;

import javax.swing.SpinnerModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class NumberSpinner implements SpinnerModel{
	private int val = 0;
	ArrayList<ChangeListener> listeners = new ArrayList<ChangeListener>();

	@Override
	public void addChangeListener(ChangeListener arg0) {
		listeners.add(arg0);
	}
	@Override
	public Object getNextValue() {
		return ++val;
	}
	@Override
	public Object getPreviousValue() {
		return --val;
	}
	@Override
	public Object getValue() {
		return val;
	}
	@Override
	public void removeChangeListener(ChangeListener arg0) {
		listeners.remove(arg0);
	}
	@Override
	public void setValue(Object arg0) {
		val = Integer.parseInt(arg0.toString());
		try{
			for (ChangeListener l : listeners){
				l.stateChanged(new ChangeEvent(this));
			}
		}catch (NumberFormatException e){};
	}
}