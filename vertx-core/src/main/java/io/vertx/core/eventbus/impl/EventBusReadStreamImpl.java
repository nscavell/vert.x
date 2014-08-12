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

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.EventBusReadStream;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.Registration;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class EventBusReadStreamImpl implements EventBusReadStream {

  private static final Logger log = LoggerFactory.getLogger(EventBusReadStreamImpl.class);

  private final String address;
  private final Registration registration;

  private Handler<Buffer> dataHandler;
  private Handler<Void> endHandler;
  private Handler<Throwable> exceptionHandler;

  private final Queue<Buffer> pending = new ArrayDeque<>();
  private boolean paused;

  public EventBusReadStreamImpl(EventBus bus, String address) {
    this.address = address;
    this.registration = bus.registerHandler(address, msg -> {
      if (msg.body() instanceof Buffer) {
        handleData((Buffer) msg.body());
      } else {
        handleInvalidMessage(msg);
      }
    });
  }

  @Override
  public String address() {
    return address;
  }

  @Override
  public void unregister() {
    registration.unregister();
  }

  @Override
  public void unregister(Handler<AsyncResult<Void>> completionHandler) {
    registration.unregister(completionHandler);
  }

  @Override
  public EventBusReadStream dataHandler(Handler<Buffer> handler) {
    this.dataHandler = handler;
    return this;
  }

  @Override
  public EventBusReadStream pause() {
    this.paused = true;
    return this;
  }

  @Override
  public EventBusReadStream resume() {
    if (paused) {
      paused = false;
      Buffer data;
      while ((data = pending.poll()) != null) {
        handleData(data);
      }
    }
    return this;
  }

  @Override
  public EventBusReadStream endHandler(Handler<Void> endHandler) {
    this.endHandler = endHandler;
    return this;
  }

  @Override
  public EventBusReadStream exceptionHandler(Handler<Throwable> handler) {
    this.exceptionHandler = handler;
    return this;
  }

  private void handleException(Throwable t) {
    if (exceptionHandler != null) {
      exceptionHandler.handle(t);
    } else {
      log.error(t);
    }
  }

  private void handleData(Buffer data) {
    if (paused) {
      pending.add(data);
    } else if (dataHandler != null) {
      // We say the 'stream' is finished when we receive a 0 length buffer
      if (data.length() == 0 && endHandler != null) {
        endHandler.handle(null);
      } else {
        dataHandler.handle(data);
      }
    }
  }

  private void handleInvalidMessage(Message message) {
    if (message.body() == null) {
      handleException(nullMessage(message.address()));
    } else {
      handleException(invalidBody(message.address(), message.body()));
    }
  }

  private static Throwable nullMessage(String address) {
    String s = "Null message was sent for address " + address + ". This is not supported for event bus streaming. Try sending a buffer with 0 length.";
    return new Exception(s);
  }

  private static Throwable invalidBody(String address, Object body) {
    String s = "Type " + body.getClass() + " was sent for " + address + ". Only Buffers can be sent for event bus streaming.";
    return new Exception(s);
  }
}
