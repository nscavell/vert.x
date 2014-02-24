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

package org.vertx.java.core;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public interface AsyncMap<K, V> {

  void size(Handler<AsyncResult<Integer>> resultHandler);

  void isEmpty(Handler<AsyncResult<Boolean>> resultHandler);

  void containsKey(Object key, Handler<AsyncResult<Boolean>> resultHandler);

  void containsValue(Object value, Handler<AsyncResult<Boolean>> resultHandler);

  void get(Object key, Handler<AsyncResult<V>> resultHandler);

  void put(K key, V value, Handler<AsyncResult<V>> resultHandler);

  void remove(Object key, Handler<AsyncResult<V>> resultHandler);

  void putAll(Map<? extends K, ? extends V> m, Handler<AsyncResult<Void>> completionHandler);

  void clear(Handler<AsyncResult<Void>> completionHandler);

  void keySet(Handler<AsyncResult<Set<K>>> resultHandler);

  void values(Handler<AsyncResult<Collection<V>>> resultHandler);

  void entrySet(Handler<AsyncResult<Set<Map.Entry<K, V>>>> resultHandler);
}
