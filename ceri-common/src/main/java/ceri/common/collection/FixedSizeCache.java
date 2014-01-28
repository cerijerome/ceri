package ceri.common.collection;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A LinkedHashMap with maximum number of keys, useful for caching.
 * When full a new entry added will cause the oldest entry to be removed. 
 */
public class FixedSizeCache<K, V> extends LinkedHashMap<K, V> {
	private static final long serialVersionUID = -3966836079868526296L;
	private final int maxCount;
	
	/**
	 * Constructor specifying the maximum number of keys.
	 */
	public FixedSizeCache(int maxCount) {
		this.maxCount = maxCount;
	}
	
	@Override
	protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
		return size() > maxCount;
	}
	
}
