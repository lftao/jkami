package com.javatao.jkami;

import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 缓存类型MAP，在内存紧张是自动回收
 * 
 * @see SoftReference
 * @author tao
 */
public class CacheMap<K, V> implements Map<K, V>, Serializable {
    private static final long serialVersionUID = 1091402635340146766L;
    private Map<K, SoftReference<V>> map = new ConcurrentHashMap<>();

    public V get(Object key) {
        Object val = map.get(key);
        if (val instanceof SoftReference) {
            V v = ((SoftReference<V>) val).get();
            return v;
        }
        return (V) val;
    }

    @Override
    public boolean containsKey(Object key) {
        Object val = map.get(key);
        if (val == null) {
            return false;
        } else {
            if (val instanceof SoftReference) {
                Object v = ((SoftReference<?>) val).get();
                if (v == null) {
                    return false;
                }
            }
            return true;
        }
    }

    @Override
    public V put(K key, V value) {
        SoftReference<V> reference = new SoftReference<>(value);
        Object oldValue = map.put(key, reference);
        if (oldValue instanceof SoftReference) {
            return ((SoftReference<V>) oldValue).get();
        } else {
            return (V) oldValue;
        }
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public V remove(Object key) {
        SoftReference<V> remove = map.remove(key);
        V v = remove.get();
        return v;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        Set<?> entrySet = m.entrySet();
        for (Object o : entrySet) {
            if (o instanceof java.util.Map.Entry) {
                Entry<K, V> entry = (Entry<K, V>) o;
                K key = entry.getKey();
                V value = entry.getValue();
                SoftReference<V> reference = new SoftReference<>(value);
                map.put(key, reference);
            }
        }
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public Set<K> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<V> values() {
        Set<V> out = new HashSet<>();
        Set<java.util.Map.Entry<K, SoftReference<V>>> set = map.entrySet();
        for (Entry<K, SoftReference<V>> entry : set) {
            SoftReference<V> value = entry.getValue();
            V v = value.get();
            if (v != null) {
                out.add(v);
            }
        }
        return out;
    }

    @Override
    public Set<java.util.Map.Entry<K, V>> entrySet() {
        Set<java.util.Map.Entry<K, V>> out = new HashSet<>();
        Set<java.util.Map.Entry<K, SoftReference<V>>> set = map.entrySet();
        for (Entry<K, SoftReference<V>> entry : set) {
            final K key = entry.getKey();
            SoftReference<V> value = entry.getValue();
            final V v = value.get();
            if (v != null) {
                java.util.Map.Entry<K, V> mp = new Entry<K, V>() {
                    @Override
                    public K getKey() {
                        return key;
                    }

                    @Override
                    public V getValue() {
                        return v;
                    }

                    @Override
                    public V setValue(V value) {
                        return value;
                    }
                };
                out.add(mp);
            }
        }
        return out;
    }

    @Override
    public boolean containsValue(Object value) {
        if (value == null)
            return false;
        Set<java.util.Map.Entry<K, SoftReference<V>>> set = map.entrySet();
        for (Entry<K, SoftReference<V>> entry : set) {
            SoftReference<V> val = entry.getValue();
            V v = val.get();
            if (value.equals(v)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 缓存类型MAP，在内存紧张是自动回收<br>
     * 定期清理被回收的数据
     * 
     * @param timerMonitor
     *            检测回收间隔时间
     */
    public CacheMap(int timerMonitor) {
        timerMonitor(timerMonitor);
    }

    /**
     * 缓存类型MAP，在内存紧张是自动回收<br>
     * 定期清理被回收的数据
     */
    public CacheMap() {
        super();
        timerMonitor(60 * 1000 * 30);
    }

    // 定期清理被回收的数据
    private void timerMonitor(int timerMonitor) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                Set<java.util.Map.Entry<K, SoftReference<V>>> entrySet = map.entrySet();
                for (Iterator<Entry<K, SoftReference<V>>> iterator = entrySet.iterator(); iterator.hasNext();) {
                    Entry<K, SoftReference<V>> entry = iterator.next();
                    SoftReference<V> value = entry.getValue();
                    V v = value.get();
                    if (v == null) {
                        iterator.remove();
                    }
                }
            }
        }, 3000, timerMonitor);
    }
}
