package org.wildlifeimages.tools.update;

import java.util.ArrayList;

import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

public class BasicListModel implements ListModel{
	ArrayList<ListDataListener> listeners = new ArrayList<ListDataListener>();
	
	@Override
	public void addListDataListener(ListDataListener newListener) {
		listeners.add(newListener);
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
	public void removeListDataListener(ListDataListener oldListener) {
		listeners.remove(oldListener);
	}
	
	public void notifyChange(){
		for (ListDataListener listener : listeners){
			listener.contentsChanged(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, this.getSize()));
		}
	}

}
