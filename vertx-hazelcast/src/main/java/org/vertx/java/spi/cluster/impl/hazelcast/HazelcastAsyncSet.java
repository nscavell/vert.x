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

package org.vertx.java.spi.cluster.impl.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ISet;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncSet;
import org.vertx.java.core.Handler;
import org.vertx.java.core.spi.Action;
import org.vertx.java.core.spi.VertxSPI;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class HazelcastAsyncSet<E> implements AsyncSet<E> {

  private final VertxSPI vertx;
  private final HazelcastInstance hazelcast;
  private final String name;
  private ISet<E> hazelcastSet;

  public HazelcastAsyncSet(VertxSPI vertx, HazelcastInstance hazelcast, String name) {
    this.vertx = vertx;
    this.hazelcast = hazelcast;
    this.name = name;
  }

  @Override
  public void size(Handler<AsyncResult<Integer>> resultHandler) {
    executeBlocking(new SetAction<Integer>() {
      @Override
      public Integer perform(ISet<E> set) {
        return set.size();
      }
    }, resultHandler);
  }

  @Override
  public void isEmpty(Handler<AsyncResult<Boolean>> resultHandler) {
    executeBlocking(new SetAction<Boolean>() {
      @Override
      public Boolean perform(ISet<E> set) {
        return set.isEmpty();
      }
    }, resultHandler);
  }

  @Override
  public void contains(final Object o, Handler<AsyncResult<Boolean>> resultHandler) {
    executeBlocking(new SetAction<Boolean>() {
      @Override
      public Boolean perform(ISet<E> set) {
        return set.contains(o);
      }
    }, resultHandler);
  }

  @Override
  public void iterator(Handler<AsyncResult<Iterator<E>>> resultHandler) {
    executeBlocking(new SetAction<Iterator<E>>() {
      @Override
      public Iterator<E> perform(ISet<E> set) {
        return set.iterator();
      }
    }, resultHandler);
  }

  @Override
  public void toArray(Handler<AsyncResult<Object[]>> resultHandler) {
    executeBlocking(new SetAction<Object[]>() {
      @Override
      public Object[] perform(ISet<E> set) {
        return set.toArray();
      }
    }, resultHandler);
  }

  @Override
  public <T> void toArray(final T[] a, Handler<AsyncResult<T[]>> resultHandler) {
    executeBlocking(new SetAction<T[]>() {
      @Override
      public T[] perform(ISet<E> set) {
        return set.toArray(a);
      }
    }, resultHandler);
  }

  @Override
  public void add(final E e, Handler<AsyncResult<Boolean>> resultHandler) {
    executeBlocking(new SetAction<Boolean>() {
      @Override
      public Boolean perform(ISet<E> set) {
        return set.add(e);
      }
    }, resultHandler);
  }

  @Override
  public void remove(final Object o, Handler<AsyncResult<Boolean>> resultHandler) {
    executeBlocking(new SetAction<Boolean>() {
      @Override
      public Boolean perform(ISet<E> set) {
        return set.remove(o);
      }
    }, resultHandler);
  }

  @Override
  public void containsAll(final Collection<?> c, Handler<AsyncResult<Boolean>> resultHandler) {
    executeBlocking(new SetAction<Boolean>() {
      @Override
      public Boolean perform(ISet<E> set) {
        return set.containsAll(c);
      }
    }, resultHandler);
  }

  @Override
  public void addAll(final Collection<? extends E> c, Handler<AsyncResult<Boolean>> resultHandler) {
    executeBlocking(new SetAction<Boolean>() {
      @Override
      public Boolean perform(ISet<E> set) {
        return set.addAll(c);
      }
    }, resultHandler);
  }

  @Override
  public void retainAll(final Collection<?> c, Handler<AsyncResult<Boolean>> resultHandler) {
    executeBlocking(new SetAction<Boolean>() {
      @Override
      public Boolean perform(ISet<E> set) {
        return set.retainAll(c);
      }
    }, resultHandler);
  }

  @Override
  public void removeAll(final Collection<?> c, Handler<AsyncResult<Boolean>> resultHandler) {
    executeBlocking(new SetAction<Boolean>() {
      @Override
      public Boolean perform(ISet<E> set) {
        return set.removeAll(c);
      }
    }, resultHandler);
  }

  @Override
  public void clear(Handler<AsyncResult<Void>> completionHandler) {
    executeBlocking(new SetAction<Void>() {
      @Override
      public Void perform(ISet<E> set) {
        set.clear();
        return null;
      }
    }, completionHandler);
  }

  private <T> void executeBlocking(final SetAction<T> originalAction, final Handler<AsyncResult<T>> originalHandler) {
    //TODO: Handle concurrency
    if (hazelcastSet == null) {
      vertx.executeBlocking(new Action<T>() {
        @Override
        public T perform() {
          hazelcastSet = hazelcast.getSet(name);
          return originalAction.perform(hazelcastSet);
        }
      }, originalHandler);
    } else {
      vertx.executeBlocking(new Action<T>() {
        @Override
        public T perform() {
          return originalAction.perform(hazelcastSet);
        }
      }, originalHandler);
    }
  }

  private abstract class SetAction<T> {
    abstract T perform(ISet<E> map);
  }
}
