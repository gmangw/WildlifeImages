package org.wildlifeimages.android.wildlifeimages;

import java.util.Hashtable;

@SuppressWarnings("serial")
public class HashTableRestricted<K,V> extends Hashtable<K,V>{

	@Override
	public synchronized V put(K key, V value){
		if (value == null || key == null){
			throw new NullPointerException();
		}
		return super.put(key, value);
	}
}
