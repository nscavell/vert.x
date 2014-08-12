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

package io.vertx.core.eventbus;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.streams.ReadStream;

/**
 * Represents a read stream for the event bus to provide flow control in things like {@link io.vertx.core.streams.Pump}
 *
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @see io.vertx.core.streams.ReadStream
 */
public interface EventBusReadStream extends ReadStream<EventBusReadStream> {
  /**
   * The address this read stream is receiving data on.
   *
   * @return the address this read stream is receiving data on.
   */
  String address();

  /**
   * Unregister the event bus handler for the address this stream was created for
   *
   * @see io.vertx.core.eventbus.Registration
   */
  void unregister();

  /**
   * Unregisters the event bus handler for the address this stream was created for
   *
   * @param completionHandler called when the unregister completes.
   * @see io.vertx.core.eventbus.Registration
   */
  void unregister(Handler<AsyncResult<Void>> completionHandler);
}
