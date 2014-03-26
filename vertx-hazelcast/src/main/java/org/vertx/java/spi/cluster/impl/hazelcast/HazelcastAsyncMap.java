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
import org.vertx.java.core.Handler;
import org.vertx.java.core.spi.Action;
import org.vertx.java.core.spi.VertxSPI;
import org.vertx.java.core.spi.cluster.AsyncMap;

class HazelcastAsyncMap<K, V> implements AsyncMap<K, V> {

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
  public void get(final K k, Handler<AsyncResult<V>> asyncResultHandler) {
    vertx.executeBlocking(new Action<V>() {
      public V perform() {
        return getMap().get(k);
      }
    }, asyncResultHandler);
  }

  @Override
  public void put(final K k, final V v, Handler<AsyncResult<Void>> completionHandler) {
    vertx.executeBlocking(new Action<Void>() {
      public Void perform() {
        getMap().put(k, HazelcastServerID.convertServerID(v));
        return null;
      }
    }, completionHandler);
  }

  @Override
  public void remove(final K k, Handler<AsyncResult<Void>> completionHandler) {
    vertx.executeBlocking(new Action<Void>() {
      public Void perform() {
        getMap().remove(k);
        return null;
      }
    }, completionHandler);
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
