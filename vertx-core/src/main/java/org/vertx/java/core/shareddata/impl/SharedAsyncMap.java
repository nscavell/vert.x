/*
 * Copyright (c) 2011-2014 The original author or authors
 *  ------------------------------------------------------
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *      The Eclipse Public License is available at
 *      http://www.eclipse.org/legal/epl-v10.html
 *
 *      The Apache License v2.0 is available at
 *      http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */

package org.vertx.java.core.shareddata.impl;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.ConcurrentAsyncMap;
import org.vertx.java.core.Handler;
import org.vertx.java.core.impl.DefaultFutureResult;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * This is just an async map impl that delegates to a synchronous map.
 *
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class SharedAsyncMap<K, V> implements ConcurrentAsyncMap<K, V> {
  private final ConcurrentMap<K, V> map;

  public SharedAsyncMap(ConcurrentMap<K, V> map) {
    this.map = map;
  }

  @Override
  public void putIfAbsent(K key, V value, Handler<AsyncResult<V>> resultHandler) {
    resultHandler.handle(new DefaultFutureResult<>(map.putIfAbsent(key, value)));
  }

  @Override
  public void remove(Object key, Object value, Handler<AsyncResult<Boolean>> resultHandler) {
    resultHandler.handle(new DefaultFutureResult<>(map.remove(key, value)));
  }

  @Override
  public void replace(K key, V oldValue, V newValue, Handler<AsyncResult<Boolean>> resultHandler) {
    resultHandler.handle(new DefaultFutureResult<>(map.replace(key, oldValue, newValue)));
  }

  @Override
  public void replace(K key, V value, Handler<AsyncResult<V>> resultHandler) {
    resultHandler.handle(new DefaultFutureResult<>(map.replace(key, value)));
  }

  @Override
  public void size(Handler<AsyncResult<Integer>> resultHandler) {
    resultHandler.handle(new DefaultFutureResult<>(map.size()));
  }

  @Override
  public void isEmpty(Handler<AsyncResult<Boolean>> resultHandler) {
    resultHandler.handle(new DefaultFutureResult<>(map.isEmpty()));
  }

  @Override
  public void containsKey(Object key, Handler<AsyncResult<Boolean>> resultHandler) {
    resultHandler.handle(new DefaultFutureResult<>(map.containsKey(key)));
  }

  @Override
  public void containsValue(Object value, Handler<AsyncResult<Boolean>> resultHandler) {
    resultHandler.handle(new DefaultFutureResult<>(map.containsValue(value)));
  }

  @Override
  public void get(Object key, Handler<AsyncResult<V>> resultHandler) {
    resultHandler.handle(new DefaultFutureResult<>(map.get(key)));
  }

  @Override
  public void put(K key, V value, Handler<AsyncResult<V>> resultHandler) {
    resultHandler.handle(new DefaultFutureResult<>(map.put(key, value)));
  }

  @Override
  public void remove(Object key, Handler<AsyncResult<V>> resultHandler) {
    resultHandler.handle(new DefaultFutureResult<>(map.remove(key)));
  }

  @Override
  public void putAll(Map<? extends K, ? extends V> m, Handler<AsyncResult<Void>> completionHandler) {
    map.putAll(m);
    completionHandler.handle(new DefaultFutureResult<>((Void) null));
  }

  @Override
  public void clear(Handler<AsyncResult<Void>> completionHandler) {
    map.clear();
    completionHandler.handle(new DefaultFutureResult<>((Void) null));
  }

  @Override
  public void keySet(Handler<AsyncResult<Set<K>>> resultHandler) {
    resultHandler.handle(new DefaultFutureResult<>(map.keySet()));
  }

  @Override
  public void values(Handler<AsyncResult<Collection<V>>> resultHandler) {
    resultHandler.handle(new DefaultFutureResult<>(map.values()));
  }

  @Override
  public void entrySet(Handler<AsyncResult<Set<Map.Entry<K, V>>>> resultHandler) {
    resultHandler.handle(new DefaultFutureResult<>(map.entrySet()));
  }
}
