package com.loohp.interactivechat.objectholders;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ConcurrentCacheHashMap<K, V> implements ConcurrentMap<K, V> {

    private static final float LOAD_FACTOR = 0.75f;

    private final ConcurrentHashMap<K, V> mapping;
    private final ConcurrentHashMap<K, Long> insertionTime;
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
        return entry != null && getAndCheckExpire(entry.getKey()) != null;
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
        return new ConcurrentCacheHashMapKeySet<>(this);
    }

    @Override
    public Collection<V> values() {
        return new ConcurrentCacheHashMapValueCollection<>(this);
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return new ConcurrentCacheHashMapEntrySet<>(this);
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

    public static class ConcurrentCacheHashMapKeySet<K, V> implements Set<K> {

        private final ConcurrentCacheHashMap<K, V> backingMap;

        public ConcurrentCacheHashMapKeySet(ConcurrentCacheHashMap<K, V> backingMap) {
            this.backingMap = backingMap;
        }

        @Override
        public int size() {
            return backingMap.size();
        }

        @Override
        public boolean isEmpty() {
            return backingMap.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return backingMap.containsKey(o);
        }

        @Override
        public Iterator<K> iterator() {
            backingMap.cleanUp();
            return new Iterator<K>() {

                private final Iterator<K> itr = backingMap.mapping.keySet().iterator();
                private K current;

                @Override
                public boolean hasNext() {
                    return itr.hasNext();
                }

                @Override
                public K next() {
                    return current = itr.next();
                }

                @Override
                public void remove() {
                    backingMap.remove(current);
                }
            };
        }

        @Override
        public Object[] toArray() {
            backingMap.cleanUp();
            return backingMap.mapping.keySet().toArray();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            backingMap.cleanUp();
            return backingMap.mapping.keySet().toArray(a);
        }

        @Override
        public boolean add(K e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(Object o) {
            return backingMap.remove(o) != null;
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            backingMap.cleanUp();
            return backingMap.mapping.keySet().containsAll(c);
        }

        @Override
        public boolean addAll(Collection<? extends K> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            return backingMap.mapping.entrySet().stream().filter(each -> !c.contains(each.getKey())).map(each -> backingMap.remove(each.getKey(), each.getValue())).anyMatch(each -> each != null);
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            return c.stream().map(each -> backingMap.remove(each)).anyMatch(each -> each != null);
        }

        @Override
        public void clear() {
            backingMap.clear();
        }

    }

    public static class ConcurrentCacheHashMapValueCollection<K, V> implements Collection<V> {

        private final ConcurrentCacheHashMap<K, V> backingMap;

        public ConcurrentCacheHashMapValueCollection(ConcurrentCacheHashMap<K, V> backingMap) {
            this.backingMap = backingMap;
        }

        @Override
        public int size() {
            return backingMap.size();
        }

        @Override
        public boolean isEmpty() {
            return backingMap.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return backingMap.containsValue(o);
        }

        @Override
        public Iterator<V> iterator() {
            backingMap.cleanUp();
            return new Iterator<V>() {

                private final Iterator<Entry<K, V>> itr = backingMap.mapping.entrySet().iterator();
                private Entry<K, V> current;

                @Override
                public boolean hasNext() {
                    return itr.hasNext();
                }

                @Override
                public V next() {
                    return (current = itr.next()).getValue();
                }

                @Override
                public void remove() {
                    backingMap.remove(current.getKey());
                }
            };
        }

        @Override
        public Object[] toArray() {
            backingMap.cleanUp();
            return backingMap.mapping.values().toArray();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            backingMap.cleanUp();
            return backingMap.mapping.values().toArray(a);
        }

        @Override
        public boolean add(V e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(Object o) {
            Optional<Entry<K, V>> opt = backingMap.mapping.entrySet().stream().filter(each -> each.getValue().equals(o)).findFirst();
            if (opt.isPresent()) {
                return backingMap.remove(opt.get().getKey(), opt.get().getValue());
            }
            return false;
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            backingMap.cleanUp();
            return backingMap.mapping.values().containsAll(c);
        }

        @Override
        public boolean addAll(Collection<? extends V> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            return c.stream().map(each -> remove(each)).anyMatch(each -> each);
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            return backingMap.mapping.entrySet().stream().filter(each -> !c.contains(each.getValue()) && backingMap.containsKey(each.getKey())).map(each -> backingMap.remove(each.getKey(), each.getValue())).anyMatch(each -> each);
        }

        @Override
        public void clear() {
            backingMap.clear();
        }

    }

    public static class ConcurrentCacheHashMapEntrySet<K, V> implements Set<Entry<K, V>> {

        private final ConcurrentCacheHashMap<K, V> backingMap;

        public ConcurrentCacheHashMapEntrySet(ConcurrentCacheHashMap<K, V> backingMap) {
            this.backingMap = backingMap;
        }

        @Override
        public int size() {
            return backingMap.size();
        }

        @Override
        public boolean isEmpty() {
            return backingMap.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            if (o instanceof Entry) {
                return backingMap.containsKey(((Entry<?, ?>) o).getKey());
            }
            return false;
        }

        @Override
        public Iterator<Entry<K, V>> iterator() {
            backingMap.cleanUp();
            return new Iterator<Entry<K, V>>() {

                private final Iterator<Entry<K, V>> itr = backingMap.mapping.entrySet().iterator();
                private Entry<K, V> current;

                @Override
                public boolean hasNext() {
                    return itr.hasNext();
                }

                @Override
                public Entry<K, V> next() {
                    return current = itr.next();
                }

                @Override
                public void remove() {
                    backingMap.remove(current.getKey());
                }
            };
        }

        @Override
        public Object[] toArray() {
            backingMap.cleanUp();
            return backingMap.mapping.entrySet().toArray();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            backingMap.cleanUp();
            return backingMap.mapping.entrySet().toArray(a);
        }

        @Override
        public boolean add(Entry<K, V> e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(Object o) {
            if (o instanceof Entry) {
                return backingMap.remove(((Entry<?, ?>) o).getKey(), ((Entry<?, ?>) o).getValue());
            }
            return false;
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            backingMap.cleanUp();
            return c.stream().map(each -> {
                if (each instanceof Entry) {
                    return backingMap.containsKey(((Entry<?, ?>) each).getKey());
                }
                return false;
            }).allMatch(each -> each);
        }

        @Override
        public boolean addAll(Collection<? extends Entry<K, V>> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            backingMap.cleanUp();
            return backingMap.mapping.entrySet().stream().filter(each -> !c.contains(each) && backingMap.containsKey(each.getKey())).map(each -> backingMap.remove(each.getKey(), each.getValue())).anyMatch(each -> each);
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            return c.stream().map(each -> remove(each)).anyMatch(each -> each);
        }

        @Override
        public void clear() {
            backingMap.cleanUp();
        }

    }

}
