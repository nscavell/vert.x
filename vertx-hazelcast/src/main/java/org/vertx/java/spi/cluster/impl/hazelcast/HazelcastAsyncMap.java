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

package org.vertx.java.spi.cluster.impl.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.ConcurrentAsyncMap;
import org.vertx.java.core.Handler;
import org.vertx.java.core.spi.Action;
import org.vertx.java.core.spi.VertxSPI;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

class HazelcastAsyncMap<K, V> implements ConcurrentAsyncMap<K, V> {

  private final VertxSPI vertx;
  private final HazelcastInstance hazelcast;
  private final String name;
  private volatile IMap<K, V> hazelcastMap;

  public HazelcastAsyncMap(VertxSPI vertx, HazelcastInstance hazelcast, String name) {
    this.vertx = vertx;
    this.hazelcast = hazelcast;
    this.name = name;
  }

  @Override
  public void get(final Object key, Handler<AsyncResult<V>> resultHandler) {
    vertx.executeBlocking(new Action<V>() {
      public V perform() {
        return getMap().get(key);
      }
    }, resultHandler);
  }

  @Override
  public void put(final K k, final V v, Handler<AsyncResult<V>> resultHandler) {
    vertx.executeBlocking(new Action<V>() {
      public V perform() {
        return getMap().put(k, HazelcastServerID.convertServerID(v));
      }
    }, resultHandler);
  }

  @Override
  public void remove(final Object key, Handler<AsyncResult<V>> resultHandler) {
    vertx.executeBlocking(new Action<V>() {
      public V perform() {
        return getMap().remove(key);
      }
    }, resultHandler);
  }

  @Override
  public void clear(Handler<AsyncResult<Void>> completionHandler) {
    vertx.executeBlocking(new Action<Void>() {
      public Void perform() {
        getMap().clear();
        return null;
      }
    }, completionHandler);
  }

  @Override
  public void size(Handler<AsyncResult<Integer>> resultHandler) {
    vertx.executeBlocking(new Action<Integer>() {
      public Integer perform() {
        return getMap().size();
      }
    }, resultHandler);
  }

  @Override
  public void isEmpty(Handler<AsyncResult<Boolean>> resultHandler) {
    vertx.executeBlocking(new Action<Boolean>() {
      public Boolean perform() {
        return getMap().isEmpty();
      }
    }, resultHandler);
  }

  @Override
  public void containsKey(final Object key, Handler<AsyncResult<Boolean>> resultHandler) {
    vertx.executeBlocking(new Action<Boolean>() {
      public Boolean perform() {
        return getMap().containsKey(key);
      }
    }, resultHandler);
  }

  @Override
  public void containsValue(final Object value, Handler<AsyncResult<Boolean>> resultHandler) {
    vertx.executeBlocking(new Action<Boolean>() {
      public Boolean perform() {
        return getMap().containsValue(value);
      }
    }, resultHandler);
  }

  @Override
  public void putAll(final Map<? extends K, ? extends V> m, Handler<AsyncResult<Void>> completionHandler) {
    vertx.executeBlocking(new Action<Void>() {
      public Void perform() {
        getMap().putAll(m);
        return null;
      }
    }, completionHandler);
  }

  @Override
  public void keySet(Handler<AsyncResult<Set<K>>> resultHandler) {
    vertx.executeBlocking(new Action<Set<K>>() {
      public Set<K> perform() {
        return getMap().keySet();
      }
    }, resultHandler);
  }

  @Override
  public void values(Handler<AsyncResult<Collection<V>>> resultHandler) {
    vertx.executeBlocking(new Action<Collection<V>>() {
      public Collection<V> perform() {
        return getMap().values();
      }
    }, resultHandler);
  }

  @Override
  public void entrySet(Handler<AsyncResult<Set<Map.Entry<K, V>>>> resultHandler) {
    vertx.executeBlocking(new Action<Set<Map.Entry<K, V>>>() {
      public Set<Map.Entry<K, V>> perform() {
        return getMap().entrySet();
      }
    }, resultHandler);
  }

  @Override
  public void putIfAbsent(final K key, final V value, Handler<AsyncResult<V>> resultHandler) {
    vertx.executeBlocking(new Action<V>() {
      public V perform() {
        return getMap().putIfAbsent(key, value);
      }
    }, resultHandler);
  }

  @Override
  public void remove(final Object key, final Object value, Handler<AsyncResult<Boolean>> resultHandler) {
    vertx.executeBlocking(new Action<Boolean>() {
      public Boolean perform() {
        return getMap().remove(key, value);
      }
    }, resultHandler);
  }

  @Override
  public void replace(final K key, final V oldValue, final V newValue, Handler<AsyncResult<Boolean>> resultHandler) {
    vertx.executeBlocking(new Action<Boolean>() {
      public Boolean perform() {
        return getMap().replace(key, oldValue, newValue);
      }
    }, resultHandler);
  }

  @Override
  public void replace(final K key, final V value, Handler<AsyncResult<V>> resultHandler) {
    vertx.executeBlocking(new Action<V>() {
      public V perform() {
        return getMap().replace(key, value);
      }
    }, resultHandler);
  }

  private IMap<K, V> getMap() {
      IMap<K, V> result = hazelcastMap;
      if (result == null) {
        synchronized (this) {
          result = hazelcastMap;
          if (result == null) {
            result = hazelcastMap = hazelcast.getMap(name);
          }
        }
      }
      return result;
    }
}
