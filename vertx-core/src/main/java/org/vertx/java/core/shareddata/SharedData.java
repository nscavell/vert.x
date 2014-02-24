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

package org.vertx.java.core.shareddata;

import org.vertx.java.core.AsyncSet;
import org.vertx.java.core.ConcurrentAsyncMap;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;
import org.vertx.java.core.shareddata.impl.SharedAsyncMap;
import org.vertx.java.core.shareddata.impl.SharedAsyncSet;
import org.vertx.java.core.shareddata.impl.SharedMap;
import org.vertx.java.core.shareddata.impl.SharedSet;
import org.vertx.java.core.spi.cluster.ClusterManager;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Sometimes it is desirable to share immutable data between different event loops, for example to implement a
 * cache of data.<p>
 * This class allows instances of shared data structures to be looked up and used from different event loops.<p>
 * The data structures themselves will only allow certain data types to be stored into them. This shields you from
 * worrying about any thread safety issues might occur if mutable objects were shared between event loops.<p>
 * The following types can be stored in a shareddata data structure:<p>
 * <pre>
 *   {@link String}
 *   {@link Integer}
 *   {@link Long}
 *   {@link Double}
 *   {@link Float}
 *   {@link Short}
 *   {@link Byte}
 *   {@link Character}
 *   {@code byte[]} - this will be automatically copied, and the copy will be stored in the structure.
 *   {@link org.vertx.java.core.buffer.Buffer} - this will be automatically copied, and the copy will be stored in the
 *   structure.
 *   Classes implementing {@link org.vertx.java.core.shareddata.Shareable}
 * </pre>
 * <p>
 *
 * Instances of this class are thread-safe.<p>
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class SharedData {

  private static final Logger log = LoggerFactory.getLogger(SharedData.class);

  private ConcurrentMap<Object, SharedMap<?, ?>> maps = new ConcurrentHashMap<>();
  private ConcurrentMap<Object, SharedSet<?>> sets = new ConcurrentHashMap<>();

  private ConcurrentMap<String, ConcurrentAsyncMap<?, ?>> asyncMaps = new ConcurrentHashMap<>();
  private ConcurrentMap<String, AsyncSet<?>> asyncSets = new ConcurrentHashMap<>();

  private final ClusterManager clusterManager;

  public SharedData() {
    this(null);
  }

  public SharedData(ClusterManager clusterManager) {
    this.clusterManager = clusterManager;
  }

  /**
   * Return a {@code Map} with the specific {@code name}. All invocations of this method with the same value of {@code name}
   * are guaranteed to return the same {@code Map} instance. <p>
   */
  public <K, V> ConcurrentSharedMap<K, V> getMap(String name) {
    SharedMap<K, V> map = (SharedMap<K, V>) maps.get(name);
    if (map == null) {
      map = new SharedMap<>();
      SharedMap prev = maps.putIfAbsent(name, map);
      if (prev != null) {
        map = prev;
      }
    }
    return map;
  }

  //TODO: javadoc
  public <K, V> ConcurrentAsyncMap<K, V> getAsyncMap(String name) {
    ConcurrentAsyncMap<?, ?> map = asyncMaps.get(name);
    if (map == null) {
      if (clusterManager != null) {
        map = clusterManager.getAsyncMap(name);
      } else {
        map = new SharedAsyncMap<>(new SharedMap<>());
      }
      ConcurrentAsyncMap<?, ?> prev = asyncMaps.putIfAbsent(name, map);
      if (prev != null) {
        map = prev;
      }
    }

    @SuppressWarnings("unchecked")
    ConcurrentAsyncMap<K, V> ret = (ConcurrentAsyncMap<K, V>) map;
    return ret;
  }

  /**
   * Return a {@code Set} with the specific {@code name}. All invocations of this method with the same value of {@code name}
   * are guaranteed to return the same {@code Set} instance. <p>
   */
  public <E> Set<E> getSet(String name) {
    SharedSet<E> set = (SharedSet<E>) sets.get(name);
    if (set == null) {
      set = new SharedSet<>();
      SharedSet prev = sets.putIfAbsent(name, set);
      if (prev != null) {
        set = prev;
      }
    }
    return set;
  }

  public <E> AsyncSet<E> getAsyncSet(String name) {
    AsyncSet<?> set = asyncSets.get(name);
    if (set == null) {
      if (clusterManager != null) {
        set = clusterManager.getAsyncSet(name);
      } else {
        set = new SharedAsyncSet<>(new SharedSet<>());
      }
      AsyncSet<?> prev = asyncSets.putIfAbsent(name, set);
      if (prev != null) {
        set = prev;
      }
    }

    @SuppressWarnings("unchecked")
    AsyncSet<E> ret = (AsyncSet<E>) set;
    return ret;
  }

  /**
   * Remove the {@code Map} with the specific {@code name}.
   */
  public boolean removeAsyncMap(String name) {
    return asyncMaps.remove(name) != null;
  }

  /**
   * Remove the {@code Map} with the specific {@code name}.
   */
  public boolean removeMap(Object name) {
    return maps.remove(name) != null;
  }

  /**
   * Remove the {@code AsyncSet} with the specific {@code name}.
   */
  public boolean removeAsyncSet(String name) {
    return asyncSets.remove(name) != null;
  }

  /**
   * Remove the {@code Set} with the specific {@code name}.
   */
  public boolean removeSet(Object name) {
    return sets.remove(name) != null;
  }
}
