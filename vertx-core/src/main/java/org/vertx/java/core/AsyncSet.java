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
import java.util.Iterator;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public interface AsyncSet<E> {

  public void size(Handler<AsyncResult<Integer>> resultHandler);

  public void isEmpty(Handler<AsyncResult<Boolean>> resultHandler);

  public void contains(Object o, Handler<AsyncResult<Boolean>> resultHandler);

  public void iterator(Handler<AsyncResult<Iterator<E>>> resultHandler);

  public void toArray(Handler<AsyncResult<Object[]>> resultHandler);

  public <T> void toArray(T[] a, Handler<AsyncResult<T[]>> resultHandler);

  public void add(E e, Handler<AsyncResult<Boolean>> resultHandler);

  public void remove(Object o, Handler<AsyncResult<Boolean>> resultHandler);

  public void containsAll(Collection<?> c, Handler<AsyncResult<Boolean>> resultHandler);

  public void addAll(Collection<? extends E> c, Handler<AsyncResult<Boolean>> resultHandler);

  public void retainAll(Collection<?> c, Handler<AsyncResult<Boolean>> resultHandler);

  public void removeAll(Collection<?> c, Handler<AsyncResult<Boolean>> resultHandler);

  public void clear(Handler<AsyncResult<Void>> completionHandler);
}
