package org.wildlifeimages.tools.update;

import java.util.ArrayList;

import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

public class BasicComboBoxModel implements ComboBoxModel{
	ArrayList<ListDataListener> listeners = new ArrayList<ListDataListener>();
	
	@Override
	public void addListDataListener(ListDataListener arg0) {
		listeners.add(arg0);
	}

	@Override
	public Object getElementAt(int arg0) {
		return null;
	}

	@Override
	public int getSize() {
		return 0;
	}

	@Override
	public void removeListDataListener(ListDataListener arg0) {
		listeners.remove(arg0);
	}

	@Override
	public Object getSelectedItem() {
		return null;
	}

	@Override
	public void setSelectedItem(Object anItem) {
	}
	
	public void notifyChange(){
		for (ListDataListener l : listeners){
			l.contentsChanged(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, getSize()));
		}
	}
}
