package com.loohp.interactivechat.objectholders;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;

public class ConcurrentCacheHashMap<K, V> implements ConcurrentMap<K,V> {
	
	private static final float LOAD_FACTOR = 0.75f;
	
	private ConcurrentHashMap<K, V> mapping;
	private ConcurrentHashMap<K, Long> insertionTime;
	private long timeout;
	
	public ConcurrentCacheHashMap(long timeout) {
		this.timeout = timeout;
    	this.mapping = new ConcurrentHashMap<>();
    	this.insertionTime = new ConcurrentHashMap<>();
    }

    public ConcurrentCacheHashMap(long timeout, int initialCapacity) {
        this(timeout, initialCapacity, LOAD_FACTOR, 1);
    }

    public ConcurrentCacheHashMap(long timeout, Map<? extends K, ? extends V> m) {
    	this(timeout);
        putAll(m);
    }

    public ConcurrentCacheHashMap(long timeout, int initialCapacity, float loadFactor) {
        this(timeout, initialCapacity, loadFactor, 1);
    }

    public ConcurrentCacheHashMap(long timeout, int initialCapacity, float loadFactor, int concurrencyLevel) {
    	this.timeout = timeout;
    	this.mapping = new ConcurrentHashMap<>(initialCapacity, loadFactor, concurrencyLevel);
    	this.insertionTime = new ConcurrentHashMap<>(initialCapacity, loadFactor, concurrencyLevel);
    }
    
    public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}
	
	public void clearAndSetTimeout(long timeout) {
		clear();
		setTimeout(timeout);
	}

	private long now() {
    	return System.currentTimeMillis();
    }
    
    public void cleanUp() {
    	mapping.entrySet().forEach(each -> getAndCheckExpire(each.getKey()));
    }
    
    private V getAndCheckExpire(Object key) {
    	if (key == null) {
    		return null;
    	}
    	V value = mapping.get(key);
    	Long expireTime = insertionTime.get(key);
    	if (value == null || expireTime == null || now() > expireTime) {
    		mapping.remove(key);
    		insertionTime.remove(key);
    		return null;
    	}
    	return value;
    }

	@Override
	public int size() {
		return mapping.size();
	}

	@Override
	public boolean isEmpty() {
		return mapping.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return getAndCheckExpire(key) != null;
	}

	@Override
	public boolean containsValue(Object value) {
		Entry<K, V> entry = mapping.entrySet().stream().filter(each -> each.getValue().equals(value)).findFirst().orElse(null);
		if (entry != null && getAndCheckExpire(entry.getKey()) != null) {
			return true;
		}
		return false;
	}

	@Override
	public V get(Object key) {
		return getAndCheckExpire(key);
	}

	@Override
	public V put(K key, V value) {
		V previous = get(key);
		mapping.put(key, value);
		insertionTime.put(key, now() + timeout);
		return previous;
	}

	@Override
	public V remove(Object key) {
		V previous = get(key);
		if (previous != null) {
			mapping.remove(key);
			insertionTime.remove(key);
		}
		return null;
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		m.entrySet().stream().forEach(each -> put(each.getKey(), each.getValue()));
	}

	@Override
	public void clear() {
		mapping.clear();
		insertionTime.clear();
	}

	@Override
	public Set<K> keySet() {
		return Sets.filter(mapping.keySet(), key -> containsKey(key));
	}

	@Override
	public Collection<V> values() {
		return Collections2.filter(mapping.values(), value -> containsValue(value));
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		return Sets.filter(mapping.entrySet(), entry -> containsKey(entry.getKey()));
	}

	@Override
	public V putIfAbsent(K key, V value) {
		V previous = get(key);
		if (previous == null) {
			mapping.put(key, value);
			insertionTime.put(key, now() + timeout);
		}
		return previous;
	}

	@Override
	public boolean remove(Object key, Object value) {
		V previous = get(key);
		if (previous != null && previous.equals(value)) {
			mapping.remove(key);
			insertionTime.remove(key);
			return true;
		}
		return false;
	}

	@Override
	public boolean replace(K key, V oldValue, V newValue) {
		V previous = get(key);
		if (previous != null && previous.equals(oldValue)) {
			mapping.put(key, newValue);
			insertionTime.put(key, now() + timeout);
			return true;
		}
		return false;
	}

	@Override
	public V replace(K key, V value) {
		V previous = get(key);
		if (previous != null) {
			mapping.put(key, value);
			insertionTime.put(key, now() + timeout);
		}
		return previous;
	}

}
