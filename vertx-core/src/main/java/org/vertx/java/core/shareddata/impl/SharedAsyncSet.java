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
import org.vertx.java.core.AsyncSet;
import org.vertx.java.core.Handler;
import org.vertx.java.core.impl.DefaultFutureResult;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * This is just an async set impl that delegates to a synchronous set.
 *
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class SharedAsyncSet<E> implements AsyncSet<E> {
  private final Set<E> set;

  public SharedAsyncSet(Set<E> set) {
    this.set = set;
  }

  @Override
  public void size(Handler<AsyncResult<Integer>> resultHandler) {
    resultHandler.handle(new DefaultFutureResult<>(set.size()));
  }

  @Override
  public void isEmpty(Handler<AsyncResult<Boolean>> resultHandler) {
    resultHandler.handle(new DefaultFutureResult<>(set.isEmpty()));
  }

  @Override
  public void contains(Object o, Handler<AsyncResult<Boolean>> resultHandler) {
    resultHandler.handle(new DefaultFutureResult<>(set.contains(o)));
  }

  @Override
  public void iterator(Handler<AsyncResult<Iterator<E>>> resultHandler) {
    resultHandler.handle(new DefaultFutureResult<>(set.iterator()));
  }

  @Override
  public void toArray(Handler<AsyncResult<Object[]>> resultHandler) {
    resultHandler.handle(new DefaultFutureResult<>(set.toArray()));
  }

  @Override
  public <T> void toArray(T[] a, Handler<AsyncResult<T[]>> resultHandler) {
    resultHandler.handle(new DefaultFutureResult<>(set.toArray(a)));
  }

  @Override
  public void add(E e, Handler<AsyncResult<Boolean>> resultHandler) {
    resultHandler.handle(new DefaultFutureResult<>(set.add(e)));
  }

  @Override
  public void remove(Object o, Handler<AsyncResult<Boolean>> resultHandler) {
    resultHandler.handle(new DefaultFutureResult<>(set.remove(o)));
  }

  @Override
  public void containsAll(Collection<?> c, Handler<AsyncResult<Boolean>> resultHandler) {
    resultHandler.handle(new DefaultFutureResult<>(set.containsAll(c)));
  }

  @Override
  public void addAll(Collection<? extends E> c, Handler<AsyncResult<Boolean>> resultHandler) {
    resultHandler.handle(new DefaultFutureResult<>(set.addAll(c)));
  }

  @Override
  public void retainAll(Collection<?> c, Handler<AsyncResult<Boolean>> resultHandler) {
    resultHandler.handle(new DefaultFutureResult<>(set.retainAll(c)));
  }

  @Override
  public void removeAll(Collection<?> c, Handler<AsyncResult<Boolean>> resultHandler) {
    resultHandler.handle(new DefaultFutureResult<>(set.removeAll(c)));
  }

  @Override
  public void clear(Handler<AsyncResult<Void>> completionHandler) {
    set.clear();
    completionHandler.handle(new DefaultFutureResult<>((Void) null));
  }
}
