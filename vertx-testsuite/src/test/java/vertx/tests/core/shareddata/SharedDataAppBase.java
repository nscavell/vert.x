/*
 * Copyright (c) 2011-2014 The original author or authors
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

package vertx.tests.core.shareddata;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.ConcurrentAsyncMap;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.impl.VertxInternal;
import org.vertx.java.core.shareddata.Shareable;
import org.vertx.java.core.shareddata.SharedData;
import org.vertx.java.fakecluster.FakeClusterManager;
import org.vertx.java.testframework.TestClientBase;

import java.io.Serializable;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public abstract class SharedDataAppBase extends TestClientBase {

  protected abstract boolean isLocal();

  protected SharedData sharedData;

  @Override
  public void start(Future<Void> startedResult) {
    super.start();

    if (isLocal()) {
      sharedData = vertx.sharedData();
    } else {
      VertxInternal vertx = (VertxInternal) this.vertx;
      sharedData = new SharedData(new FakeClusterManager(vertx));
    }

    tu.appReady();
    startedResult.setResult(null);
  }

  protected <K, V> void get(final ConcurrentAsyncMap<K, V> map, final K key, final Handler<V> handler) {
    map.get(key, new AsyncResultHandler<V>() {
      @Override
      public void handle(AsyncResult<V> result) {
        if (result.succeeded()) {
          handler.handle(result.result());
        } else {
          tu.exception(result.cause(), "Could not retrieve value for key '" + key + "'");
        }
      }
    });
  }

  protected <K, V> void put(final ConcurrentAsyncMap<K, V> map, final K key, final V value, final Handler<V> handler) {
    map.put(key, value, new AsyncResultHandler<V>() {
      @Override
      public void handle(AsyncResult<V> result) {
        if (result.succeeded()) {
          handler.handle(result.result());
        } else {
          tu.exception(result.cause(), "Could not add map entry " + key + "=" + value);
        }
      }
    });
  }

  protected <K, V> AssertingMap<K, V> assertThat(ConcurrentAsyncMap<K, V> map) {
    return new AssertingMap<>(map);
  }

  protected static class SomeClass implements Shareable, Serializable {
  }

  protected class AssertingMap<K, V> {
    private final ConcurrentAsyncMap<K, V> map;

    public AssertingMap(ConcurrentAsyncMap<K, V> map) {
      this.map = map;
    }

    public AssertingMapGet<K, V> get(K key) {
      return new AssertingMapGet<>(map, key);
    }

    public AssertingMapPut<K, V> put(K key, V value) {
      return new AssertingMapPut<>(map, key, value);
    }
  }

  protected class AssertingMapGet<K, V> {
    private final ConcurrentAsyncMap<K, V> map;
    private final K key;

    public AssertingMapGet(ConcurrentAsyncMap<K, V> map, K key) {
      this.map = map;
      this.key = key;
    }

    public void eq(final V value) {
      eq(value, false);
    }

    public void eq(final V value, final boolean complete) {
      get(map, key, new Handler<V>() {
        @Override
        public void handle(V v) {
          if (v == null) {
            tu.azzert(value == null);
          } else {
            tu.azzert(v.equals(value));
          }
          if (complete) {
            tu.testComplete();
          }
        }
      });
    }
  }

  protected class AssertingMapPut<K, V> {
    private final ConcurrentAsyncMap<K, V> map;
    private final K key;
    private final V value;

    public AssertingMapPut(ConcurrentAsyncMap<K, V> map, K key, V value) {
      this.map = map;
      this.key = key;
      this.value = value;
    }

    public void completed(boolean complete) {
      put(map, key, value, new Handler<V>() {
        @Override
        public void handle(V v) {
          tu.testComplete();
        }
      });
    }

    public void eq(final V prev, final boolean complete) {
      put(map, key, value, new Handler<V>() {
        @Override
        public void handle(V v) {
          if (v == null) {
            tu.azzert(prev == null);
          } else {
            tu.azzert(v.equals(prev));
          }
          if (complete) {
            tu.testComplete();
          }
        }
      });
    }
  }
}
