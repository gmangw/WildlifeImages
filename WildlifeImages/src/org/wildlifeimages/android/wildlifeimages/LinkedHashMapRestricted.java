package org.wildlifeimages.android.wildlifeimages;

import java.util.LinkedHashMap;

@SuppressWarnings("serial")
public class LinkedHashMapRestricted<K,V> extends LinkedHashMap<K,V>{

	@Override
	public synchronized V put(K key, V value){
		if (value == null || key == null){
			throw new NullPointerException();
		}
		return super.put(key, value);
	}
}
