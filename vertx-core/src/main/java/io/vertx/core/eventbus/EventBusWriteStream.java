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

import io.vertx.core.streams.WriteStream;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public interface EventBusWriteStream extends WriteStream<EventBusWriteStream> {
  /**
   * The address this write stream is sending data to.
   *
   * @return the address this write stream is sending data to.
   */
  String address();

  /**
   * Set the timeout used for sending messages. If this value is positive then
   * the {@link io.vertx.core.streams.WriteStream#exceptionHandler(io.vertx.core.Handler)} will be called if the other
   * end did not reply. So only use this if you are certain the other side receiving the data
   * is replying.
   *
   * @param timeout sets the timeout (default is -1, meaning that no timeout is used for the send)
   * @return this <code>EventBusWriteStream</code>
   */
  EventBusWriteStream setTimeout(long timeout);
}
