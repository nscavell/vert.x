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

package io.vertx.test.core;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBusReadStream;
import io.vertx.core.eventbus.EventBusWriteStream;
import org.junit.Test;

import static io.vertx.test.core.TestUtils.randomBuffer;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class EventBusStreamTest extends VertxTestBase {

  //TODO: More tests

  @Test
  public void testWriteStream() {
    Buffer data = randomBuffer(1000);
    vertx.eventBus().registerHandler("write-stream", msg -> {
      assertTrue(msg.body() instanceof Buffer);
      assertTrue(TestUtils.buffersEqual(data, (Buffer) msg.body()));
      testComplete();
    });

    EventBusWriteStream stream = vertx.eventBus().writeStream("write-stream");
    stream.writeBuffer(data);

    await();
  }

  @Test
  public void testReadStream() {
    Buffer data = randomBuffer(1000);
    EventBusReadStream stream = vertx.eventBus().readStream("read-stream");
    stream.dataHandler(buffer -> {
      assertTrue(TestUtils.buffersEqual(data, buffer));
      testComplete();
    });

    vertx.eventBus().send("read-stream", data);
    await();
  }
}
