/*
 * Copyright (c) 2011-2013 The original author or authors
 * ------------------------------------------------------
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 *     The Eclipse Public License is available at
 *     http://www.eclipse.org/legal/epl-v10.html
 *
 *     The Apache License v2.0 is available at
 *     http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */

package org.vertx.java.fakecluster;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncSet;
import org.vertx.java.core.ConcurrentAsyncMap;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.spi.Action;
import org.vertx.java.core.spi.VertxSPI;
import org.vertx.java.core.spi.cluster.AsyncMultiMap;
import org.vertx.java.core.spi.cluster.ChoosableIterable;
import org.vertx.java.core.spi.cluster.ClusterManager;
import org.vertx.java.core.spi.cluster.NodeListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class FakeClusterManager implements ClusterManager {

  private static Map<String, FakeClusterManager> nodes =
      Collections.synchronizedMap(new LinkedHashMap<String, FakeClusterManager>());

  private static List<NodeListener> nodeListeners = new ArrayList<>();
  private static ConcurrentMap<String, Map> syncMaps = new ConcurrentHashMap<>();
  private static ConcurrentMap<String, ConcurrentAsyncMap> asyncMaps = new ConcurrentHashMap<>();
  private static ConcurrentMap<String, AsyncMultiMap> asyncMultiMaps = new ConcurrentHashMap<>();
  private static ConcurrentMap<String, Set> syncSets = new ConcurrentHashMap<>();
  private static ConcurrentMap<String, AsyncSet> asyncSets = new ConcurrentHashMap<>();

  private String nodeID;
  private NodeListener nodeListener;
  private VertxSPI vertx;

  public FakeClusterManager(VertxSPI vertx) {
    this.vertx = vertx;
  }

  private static void doJoin(String nodeID, FakeClusterManager node) {
    if (nodes.containsKey(nodeID)) {
      throw new IllegalStateException("Node has already joined!");
    }
    nodes.put(nodeID, node);
    for (NodeListener listener: nodeListeners) {
      listener.nodeAdded(nodeID);
    }
  }

  private static void doLeave(String nodeID) {
    if (!nodes.containsKey(nodeID)) {
      throw new IllegalStateException("Node hasn't joined!");
    }
    nodes.remove(nodeID);
    for (NodeListener listener: nodeListeners) {
      listener.nodeLeft(nodeID);
    }

  }

  private static void doAddNodeListener(NodeListener listener) {
    if (nodeListeners.contains(listener)) {
      throw new IllegalStateException("Listener already registered!");
    }
    nodeListeners.add(listener);
  }

  private static void doRemoveNodeListener(NodeListener listener) {
    if (!nodeListeners.contains(listener)) {
      throw new IllegalStateException("Listener not registered!");
    }
    nodeListeners.remove(listener);
  }

  @Override
  public <K, V> AsyncMultiMap<K, V> getAsyncMultiMap(String name) {
    AsyncMultiMap<K, V> map = (AsyncMultiMap<K, V>)asyncMultiMaps.get(name);
    if (map == null) {
      map = new FakeAsyncMultiMap<>();
      AsyncMultiMap<K, V> prevMap = (AsyncMultiMap<K, V>)asyncMultiMaps.putIfAbsent(name, map);
      if (prevMap != null) {
        map = prevMap;
      }
    }
    return map;
  }

  @Override
  public <K, V> ConcurrentAsyncMap<K, V> getAsyncMap(String name) {
    ConcurrentAsyncMap<K, V> map = (ConcurrentAsyncMap<K, V>) asyncMaps.get(name);
    if (map == null) {
      map = new FakeAsyncMap<>();
      ConcurrentAsyncMap<K, V> prevMap = (ConcurrentAsyncMap<K, V>) asyncMaps.putIfAbsent(name, map);
      if (prevMap != null) {
        map = prevMap;
      }
    }
    return map;
  }

  @Override
  public <K, V> Map<K, V> getSyncMap(String name) {
    Map<K, V> map = (Map<K, V>)syncMaps.get(name);
    if (map == null) {
      map = new ConcurrentHashMap<>();
      Map<K, V> prevMap = (Map<K, V>)syncMaps.putIfAbsent(name, map);
      if (prevMap != null) {
        map = prevMap;
      }
    }
    return map;
  }

  @Override
  public <E> AsyncSet<E> getAsyncSet(String name) {
    AsyncSet<?> set = asyncSets.get(name);
    if (set == null) {
      set = new FakeAsyncSet<>();
      AsyncSet<?> prev = asyncSets.putIfAbsent(name, set);
      if (prev != null) {
        set = prev;
      }
    }
    @SuppressWarnings("unchecked")
    AsyncSet<E> ret = (AsyncSet<E>) set;
    return ret;
  }

  @Override
  public <E> Set<E> getSyncSet(String name) {
    Set<?> set = syncSets.get(name);
    if (set == null) {
      set = new HashSet<>();
      Set<?> prev = syncSets.putIfAbsent(name, set);
      if (prev != null) {
        set = prev;
      }
    }
    @SuppressWarnings("unchecked")
    Set<E> ret = (Set<E>) set;
    return ret;
  }

  @Override
  public String getNodeID() {
    return nodeID;
  }

  @Override
  public List<String> getNodes() {
    return new ArrayList<>(nodes.keySet());
  }

  @Override
  public void nodeListener(NodeListener listener) {
    doAddNodeListener(listener);
    this.nodeListener = listener;
  }

  @Override
  public void join() {
    this.nodeID = UUID.randomUUID().toString();
    doJoin(nodeID, this);
  }

  @Override
  public void leave() {
    if (nodeID == null) {
      // Not joined
      return;
    }
    if (nodeListener != null) {
      doRemoveNodeListener(nodeListener);
      nodeListener = null;
    }
    doLeave(nodeID);
    this.nodeID = null;
  }

  public static void reset() {
    nodes.clear();
    nodeListeners.clear();
    syncMaps.clear();
    asyncMaps.clear();
    asyncMultiMaps.clear();
  }

  private class FakeAsyncMap<K, V> implements ConcurrentAsyncMap<K, V> {

    private ConcurrentMap<K, V> map = new ConcurrentHashMap<>();

    @Override
    public void get(final Object key, Handler<AsyncResult<V>> asyncResultHandler) {
      vertx.executeBlocking(new Action<V>() {
        public V perform() {
          return copyIfRequired(map.get(key));
        }
      }, asyncResultHandler);
    }

    @Override
    public void put(final K k, final V v, Handler<AsyncResult<V>> asyncResultHandler) {
      vertx.executeBlocking(new Action<V>() {
        public V perform() {
          return map.put(k, v);
        }
      }, asyncResultHandler);
    }

    @Override
    public void remove(final Object k, Handler<AsyncResult<V>> completionHandler) {
      vertx.executeBlocking(new Action<V>() {
        public V perform() {
          return copyIfRequired(map.remove(k));
        }
      }, completionHandler);
    }

    @Override
    public void clear(Handler<AsyncResult<Void>> completionHandler) {
      vertx.executeBlocking(new Action<Void>() {
        @Override
        public Void perform() {
          map.clear();
          return null;
        }
      }, completionHandler);
    }

    @Override
    public void putIfAbsent(final K key, final V value, Handler<AsyncResult<V>> resultHandler) {
      vertx.executeBlocking(new Action<V>() {
        @Override
        public V perform() {
          return map.putIfAbsent(key, value);
        }
      }, resultHandler);
    }

    @Override
    public void remove(final Object key, final Object value, Handler<AsyncResult<Boolean>> resultHandler) {
      vertx.executeBlocking(new Action<Boolean>() {
        @Override
        public Boolean perform() {
          return map.remove(key, value);
        }
      }, resultHandler);
    }

    @Override
    public void replace(final K key, final V oldValue, final V newValue, Handler<AsyncResult<Boolean>> resultHandler) {
      vertx.executeBlocking(new Action<Boolean>() {
        @Override
        public Boolean perform() {
          return map.replace(key, oldValue, newValue);
        }
      }, resultHandler);
    }

    @Override
    public void replace(final K key, final V value, Handler<AsyncResult<V>> resultHandler) {
      vertx.executeBlocking(new Action<V>() {
        @Override
        public V perform() {
          return copyIfRequired(map.replace(key, value));
        }
      }, resultHandler);
    }

    @Override
    public void size(Handler<AsyncResult<Integer>> resultHandler) {
      vertx.executeBlocking(new Action<Integer>() {
        @Override
        public Integer perform() {
          return map.size();
        }
      }, resultHandler);
    }

    @Override
    public void isEmpty(Handler<AsyncResult<Boolean>> resultHandler) {
      vertx.executeBlocking(new Action<Boolean>() {
        @Override
        public Boolean perform() {
          return map.isEmpty();
        }
      }, resultHandler);
    }

    @Override
    public void containsKey(final Object key, Handler<AsyncResult<Boolean>> resultHandler) {
      vertx.executeBlocking(new Action<Boolean>() {
        @Override
        public Boolean perform() {
          return map.containsKey(key);
        }
      }, resultHandler);
    }

    @Override
    public void containsValue(final Object value, Handler<AsyncResult<Boolean>> resultHandler) {
      vertx.executeBlocking(new Action<Boolean>() {
        @Override
        public Boolean perform() {
          return map.containsValue(value);
        }
      }, resultHandler);
    }

    @Override
    public void putAll(final Map<? extends K, ? extends V> m, Handler<AsyncResult<Void>> completionHandler) {
      vertx.executeBlocking(new Action<Void>() {
        @Override
        public Void perform() {
          map.putAll(m);
          return null;
        }
      }, completionHandler);
    }

    @Override
    public void keySet(Handler<AsyncResult<Set<K>>> resultHandler) {
      vertx.executeBlocking(new Action<Set<K>>() {
        @Override
        public Set<K> perform() {
          Set<K> copied = new HashSet<>();
          for (K k : map.keySet()) {
            copied.add(copyIfRequired(k));
          }
          return copied;
        }
      }, resultHandler);
    }

    @Override
    public void values(Handler<AsyncResult<Collection<V>>> resultHandler) {
      vertx.executeBlocking(new Action<Collection<V>>() {
        @Override
        public Collection<V> perform() {
          Collection<V> copied = new ArrayList<>();
          for (V v : map.values()) {
            copied.add(copyIfRequired(v));
          }
          return copied;
        }
      }, resultHandler);
    }

    @Override
    public void entrySet(Handler<AsyncResult<Set<Map.Entry<K, V>>>> resultHandler) {
      vertx.executeBlocking(new Action<Set<Map.Entry<K, V>>>() {
        @Override
        public Set<Map.Entry<K, V>> perform() {
          Set<Map.Entry<K, V>> entries = new HashSet<>();
          for (Map.Entry<K, V> entry : map.entrySet()) {
            entries.add(new Entry<>(entry));
          }
          return entries;
        }
      }, resultHandler);
    }
  }

  private static class Entry<K, V> implements Map.Entry<K, V> {

    final Map.Entry<K, V> internalEntry;

    Entry(Map.Entry<K, V> internalEntry) {
      this.internalEntry = internalEntry;
    }

    public K getKey() {
      return copyIfRequired(internalEntry.getKey());
    }

    public V getValue() {
      return copyIfRequired(internalEntry.getValue());
    }

    public V setValue(V value) {
      V old = internalEntry.getValue();
      internalEntry.setValue(value);
      return old;
    }
  }

  private class FakeAsyncMultiMap<K, V> implements AsyncMultiMap<K, V> {

    private ConcurrentMap<K, ChoosableSet<V>> map = new ConcurrentHashMap<>();

    @Override
    public void add(final K k, final V v, Handler<AsyncResult<Void>> completionHandler) {
      vertx.executeBlocking(new Action<Void>() {
        public Void perform() {
          ChoosableSet<V> vals = map.get(k);
          if (vals == null) {
            vals = new ChoosableSet<>(1);
            ChoosableSet<V> prevVals = map.putIfAbsent(k, vals);
            if (prevVals != null) {
              vals = prevVals;
            }
          }
          vals.add(v);
          return null;
        }
      }, completionHandler);
    }

    @Override
    public void get(final K k, Handler<AsyncResult<ChoosableIterable<V>>> asyncResultHandler) {
      vertx.executeBlocking(new Action<ChoosableIterable<V>>() {
        public ChoosableIterable<V> perform() {
          return map.get(k);
        }
      }, asyncResultHandler);
    }

    @Override
    public void remove(final K k, final V v, Handler<AsyncResult<Void>> completionHandler) {
      vertx.executeBlocking(new Action<Void>() {
        public Void perform() {
          ChoosableSet<V> vals = map.get(k);
          if (vals != null) {
            vals.remove(v);
            if (vals.isEmpty()) {
              map.remove(k);
            }
          }
          return null;
        }
      }, completionHandler);
    }

    @Override
    public void removeAllForValue(final V v, Handler<AsyncResult<Void>> completionHandler) {
      vertx.executeBlocking(new Action<Void>() {
        public Void perform() {
          Iterator<Map.Entry<K, ChoosableSet<V>>> mapIter = map.entrySet().iterator();
          while (mapIter.hasNext()) {
            Map.Entry<K, ChoosableSet<V>> entry = mapIter.next();
            ChoosableSet<V> vals = entry.getValue();
            Iterator<V> iter = vals.iterator();
            while (iter.hasNext()) {
              V val = iter.next();
              if (val.equals(v)) {
                iter.remove();
              }
            }
            if (vals.isEmpty()) {
              mapIter.remove();
            }
          }
          return null;
        }
      }, completionHandler);
    }
  }

  private class FakeAsyncSet<E> implements AsyncSet<E> {

    private final Set<E> set = Collections.newSetFromMap(new ConcurrentHashMap<E, Boolean>());

    public void iterator(Handler<AsyncResult<Iterator<E>>> resultHandler) {
      vertx.executeBlocking(new Action<Iterator<E>>() {
        @Override
        public Iterator<E> perform() {
          final Iterator<E> itr = set.iterator();

          return new Iterator<E>() {
            @Override
            public boolean hasNext() {
              return itr.hasNext();
            }

            @Override
            public E next() {
              return copyIfRequired(itr.next());
            }

            @Override
            public void remove() {
              itr.remove();
            }
          };
        }
      }, resultHandler);
    }

    @Override
    public void add(final E element, Handler<AsyncResult<Boolean>> resultHandler) {
      vertx.executeBlocking(new Action<Boolean>() {
        @Override
        public Boolean perform() {
          return set.add(element);
        }
      }, resultHandler);
    }

    @Override
    public void remove(final Object o, Handler<AsyncResult<Boolean>> resultHandler) {
      vertx.executeBlocking(new Action<Boolean>() {
        @Override
        public Boolean perform() {
          return copyIfRequired(set.remove(o));
        }
      }, resultHandler);
    }

    @Override
    public void clear(Handler<AsyncResult<Void>> completionHandler) {
      vertx.executeBlocking(new Action<Void>() {
        @Override
        public Void perform() {
          set.clear();
          return null;
        }
      }, completionHandler);
    }

    @Override
    public void size(Handler<AsyncResult<Integer>> resultHandler) {
      vertx.executeBlocking(new Action<Integer>() {
        @Override
        public Integer perform() {
          return set.size();
        }
      }, resultHandler);
    }

    @Override
    public void isEmpty(Handler<AsyncResult<Boolean>> resultHandler) {
      vertx.executeBlocking(new Action<Boolean>() {
        @Override
        public Boolean perform() {
          return set.isEmpty();
        }
      }, resultHandler);
    }

    @Override
    public void contains(final Object o, Handler<AsyncResult<Boolean>> resultHandler) {
      vertx.executeBlocking(new Action<Boolean>() {
        @Override
        public Boolean perform() {
          return set.contains(o);
        }
      }, resultHandler);
    }

    @Override
    public void toArray(Handler<AsyncResult<Object[]>> resultHandler) {
      vertx.executeBlocking(new Action<Object[]>() {
        @Override
        public Object[] perform() {
          Object[] original = set.toArray();
          Object[] copy = new Object[original.length];
          for (int i = 0; i < copy.length; i++) {
            copy[i] = copyIfRequired(original[i]);
          }

          return copy;
        }
      }, resultHandler);
    }

    @Override
    public <T> void toArray(final T[] a, Handler<AsyncResult<T[]>> resultHandler) {
      vertx.executeBlocking(new Action<T[]>() {
        @Override
        public T[] perform() {
          T[] original = set.toArray(a);
          @SuppressWarnings("unchecked")
          T[] copy = (T[]) Array.newInstance(a.getClass().getComponentType(), original.length);

          for (int i = 0; i < original.length; i++) {
            copy[i] = copyIfRequired(original[i]);
          }

          return copy;
        }
      }, resultHandler);
    }

    @Override
    public void containsAll(final Collection<?> c, Handler<AsyncResult<Boolean>> resultHandler) {
      vertx.executeBlocking(new Action<Boolean>() {
        @Override
        public Boolean perform() {
          return set.containsAll(c);
        }
      }, resultHandler);
    }

    @Override
    public void addAll(final Collection<? extends E> c, Handler<AsyncResult<Boolean>> resultHandler) {
      vertx.executeBlocking(new Action<Boolean>() {
        @Override
        public Boolean perform() {
          return set.addAll(c);
        }
      }, resultHandler);
    }

    @Override
    public void retainAll(final Collection<?> c, Handler<AsyncResult<Boolean>> resultHandler) {
      vertx.executeBlocking(new Action<Boolean>() {
        @Override
        public Boolean perform() {
          return set.retainAll(c);
        }
      }, resultHandler);
    }

    @Override
    public void removeAll(final Collection<?> c, Handler<AsyncResult<Boolean>> resultHandler) {
      vertx.executeBlocking(new Action<Boolean>() {
        @Override
        public Boolean perform() {
          return set.removeAll(c);
        }
      }, resultHandler);
    }
  }

  // Copied from Checker of shared data to ensure we have immutability in the value of the map so they
  private static <T> T copyIfRequired(T obj) {
    if (obj instanceof byte[]) {
      //Copy it
      byte[] bytes = (byte[]) obj;
      byte[] copy = new byte[bytes.length];
      System.arraycopy(bytes, 0, copy, 0, bytes.length);
      return (T) copy;
    } else if (obj instanceof Buffer) {
      //Copy it
      return (T) ((Buffer) obj).copy();
    } else {
      return obj;
    }
  }
}
