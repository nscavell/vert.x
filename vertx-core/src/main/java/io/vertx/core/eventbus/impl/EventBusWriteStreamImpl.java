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

package io.vertx.core.eventbus.impl;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.EventBusWriteStream;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class EventBusWriteStreamImpl implements EventBusWriteStream {

  private static final Logger log = LoggerFactory.getLogger(EventBusWriteStreamImpl.class);

  private static final int DEFAULT_MAX_BUFFER_SIZE = 8 * 1024;
  private static final int DEFAULT_WRITE_QUEUE_MAX_SIZE = 32 * 1024;

  private final EventBus bus;
  private final String address;
  private final int maxBufferSize;
  private int writeQueueMaxSize;
  private long timeout;

  private Handler<Void> drainHandler;
  private Handler<Throwable> exceptionHandler;

  private final Queue<Buffer> pending = new ArrayDeque<>();
  private long bytesPending;

  public EventBusWriteStreamImpl(EventBus bus, String address) {
    this(bus, address, DEFAULT_MAX_BUFFER_SIZE);
  }

  public EventBusWriteStreamImpl(EventBus bus, String address, int maxBufferSize) {
    this.bus = bus;
    this.address = address;
    this.maxBufferSize = maxBufferSize;
    this.writeQueueMaxSize = DEFAULT_WRITE_QUEUE_MAX_SIZE;
    this.timeout = -1;
  }

  @Override
  public String address() {
    return address;
  }

  @Override
  public EventBusWriteStream setTimeout(long timeout) {
    this.timeout = timeout;
    return this;
  }

  @Override
  public EventBusWriteStream writeBuffer(Buffer data) {
    if (data.length() > maxBufferSize) {
      splitBuffers(data);
    } else {
      pending.add(data);
    }
    bytesPending += data.length();
    checkSend();

    return this;
  }

  @Override
  public EventBusWriteStream setWriteQueueMaxSize(int maxSize) {
    this.writeQueueMaxSize = maxSize;
    return this;
  }

  @Override
  public boolean writeQueueFull() {
    return bytesPending >= writeQueueMaxSize;
  }

  @Override
  public EventBusWriteStream drainHandler(Handler<Void> handler) {
    this.drainHandler = handler;
    return this;
  }

  @Override
  public EventBusWriteStream exceptionHandler(Handler<Throwable> handler) {
    this.exceptionHandler = handler;
    return this;
  }

  private void checkSend() {
    Buffer data;
    while ((data = pending.poll()) != null) {
      doSend(data);
    }

    // Only check drained if we're not doing a sendWithTimeout
    if (timeout <= 0) {
      checkDrained();
    }
  }

  private void doSend(Buffer data) {
    if (timeout > 0) {
      bus.sendWithTimeout(address, data, timeout, ar -> {
        if (ar.succeeded()) {
          bytesPending -= data.length();
          checkDrained();
        } else {
          handleException(ar.cause());
        }
      });
    } else {
      // We assume all writes make it
      bus.send(address, data);
      bytesPending -= data.length();
    }
  }

  private void checkDrained() {
    if (drainHandler != null && bytesPending < writeQueueMaxSize) {
      drainHandler.handle(null);
    }
  }

  private void handleException(Throwable t) {
    if (exceptionHandler != null) {
      exceptionHandler.handle(t);
    } else {
      log.error(t);
    }
  }

  private void splitBuffers(Buffer data) {
    int pos = 0;
    while (pos < data.length() - 1) {
      int end = pos + Math.min(maxBufferSize, data.length() - pos);
      Buffer slice = data.slice(pos, end);
      pending.add(slice);
      pos += maxBufferSize;
    }
  }
}
