/*
 * Copyright 2014 Red Hat, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * The Apache License v2.0 is available at
 * http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */

package org.vertx.java.tests.core;

import org.junit.Test;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.function.Consumer;

import static org.vertx.java.tests.core.TestUtils.*;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public abstract class EventBusTestBase extends AsyncTestBase {

  protected static final String ADDRESS1 = "some-address1";
  protected static final String ADDRESS2 = "some-address2";

  @Test
  public void testSendNull() {
    testSend(null);
  }

  @Test
  public void testReplyNull() {
    testReply(null);
  }

  @Test
  public void testPublishNull() {
    testPublish(null);
  }

  @Test
  public void testSendString() {
    String str = randomUnicodeString(100);
    testSend(str);
  }

  @Test
  public void testReplyString() {
    String str = randomUnicodeString(100);
    testReply(str);
  }

  @Test
  public void testPublishString() {
    String str = randomUnicodeString(100);
    testPublish(str);
  }

  @Test
  public void testSendBooleanTrue() {
    testSend(true);
  }

  @Test
  public void testSendBooleanFalse() {
    testSend(false);
  }

  @Test
  public void testReplyBooleanTrue() {
    testReply(true);
  }

  @Test
  public void testReplyBooleanFalse() {
    testReply(false);
  }

  @Test
  public void testPublishBooleanTrue() {
    testPublish(true);
  }

  @Test
  public void testPublishBooleanFalse() {
    testPublish(false);
  }

  @Test
  public void testSendBuffer() {
    Buffer sent = randomBuffer(100);
    testSend(sent, (buffer) -> {
      buffersEqual(sent, buffer);
      assertFalse(sent == buffer); // Make sure it's copied
    });
  }

  @Test
  public void testReplyBuffer() {
    Buffer sent = randomBuffer(100);
    testReply(sent, (bytes) -> {
      buffersEqual(sent, bytes);
      assertFalse(sent == bytes); // Make sure it's copied
    });
  }

  @Test
  public void testPublishBuffer() {
    Buffer sent = randomBuffer(100);
    testPublish(sent, (buffer) -> {
      buffersEqual(sent, buffer);
      assertFalse(sent == buffer); // Make sure it's copied
    });
  }

  @Test
  public void testSendByte() {
    testSend(randomByte());
  }

  @Test
  public void testReplyByte() {
    testReply(randomByte());
  }

  @Test
  public void testPublishByte() {
    testPublish(randomByte());
  }

  @Test
  public void testSendByteArray() {
    byte[] sent = randomByteArray(100);
    testSend(sent, (bytes) -> {
      byteArraysEqual(sent, bytes);
      assertFalse(sent == bytes); // Make sure it's copied
    });
  }

  @Test
  public void testReplyByteArray() {
    byte[] sent = randomByteArray(100);
    testReply(sent, (bytes) -> {
      byteArraysEqual(sent, bytes);
      assertFalse(sent == bytes); // Make sure it's copied
    });
  }

  @Test
  public void testPublishByteArray() {
    byte[] sent = randomByteArray(100);
    testPublish(sent, (bytes) -> {
      byteArraysEqual(sent, bytes);
      assertFalse(sent == bytes); // Make sure it's copied
    });
  }

  @Test
  public void testSendCharacter() {
    testSend(randomChar());
  }

  @Test
  public void testReplyCharacter() {
    testReply(randomChar());
  }

  @Test
  public void testPublishCharacter() {
    testPublish(randomChar());
  }

  @Test
  public void testSendDouble() {
    testSend(randomDouble());
  }

  @Test
  public void testReplyDouble() {
    testReply(randomDouble());
  }

  @Test
  public void testPublishDouble() {
    testPublish(randomDouble());
  }

  @Test
  public void testSendFloat() {
    testSend(randomFloat());
  }

  @Test
  public void testReplyFloat() {
    testReply(randomFloat());
  }

  @Test
  public void testPublishFloat() {
    testPublish(randomFloat());
  }

  @Test
  public void testSendInteger() {
    testSend(randomInt());
  }

  @Test
  public void testReplyInteger() {
    testReply(randomInt());
  }

  @Test
  public void testPublishInteger() {
    testPublish(randomInt());
  }

  @Test
  public void testSendLong() {
    testSend(randomLong());
  }

  @Test
  public void testReplyLong() {
    testReply(randomLong());
  }

  @Test
  public void testPublishLong() {
    testPublish(randomLong());
  }

  @Test
  public void testSendShort() {
    testSend(randomShort());
  }

  @Test
  public void testReplyShort() {
    testReply(randomShort());
  }

  @Test
  public void testPublishShort() {
    testPublish(randomShort());
  }

  @Test
  public void testSendJsonArray() {
    JsonArray arr = new JsonArray();
    arr.add(randomUnicodeString(100)).add(randomInt()).add(randomBoolean());
    testSend(arr, (received) -> {
      assertEquals(arr, received);
      assertFalse(arr == received); // Make sure it's copied
    });
  }

  @Test
  public void testReplyJsonArray() {
    JsonArray arr = new JsonArray();
    arr.add(randomUnicodeString(100)).add(randomInt()).add(randomBoolean());
    testReply(arr, (received) -> {
      assertEquals(arr, received);
      assertFalse(arr == received); // Make sure it's copied
    });
  }

  @Test
  public void testPublishJsonArray() {
    JsonArray arr = new JsonArray();
    arr.add(randomUnicodeString(100)).add(randomInt()).add(randomBoolean());
    testPublish(arr, (received) -> {
      assertEquals(arr, received);
      assertFalse(arr == received); // Make sure it's copied
    });
  }

  @Test
  public void testSendJsonObject() {
    JsonObject obj = new JsonObject();
    obj.putString(randomUnicodeString(100), randomUnicodeString(100)).putNumber(randomUnicodeString(100), randomInt());
    testSend(obj, (received) -> {
      assertEquals(obj, received);
      assertFalse(obj == received); // Make sure it's copied
    });
  }

  @Test
  public void testReplyJsonObject() {
    JsonObject obj = new JsonObject();
    obj.putString(randomUnicodeString(100), randomUnicodeString(100)).putNumber(randomUnicodeString(100), randomInt());
    testReply(obj, (received) -> {
      assertEquals(obj, received);
      assertFalse(obj == received); // Make sure it's copied
    });
  }

  @Test
  public void testPublishJsonObject() {
    JsonObject obj = new JsonObject();
    obj.putString(randomUnicodeString(100), randomUnicodeString(100)).putNumber(randomUnicodeString(100), randomInt());
    testPublish(obj, (received) -> {
      assertEquals(obj, received);
      assertFalse(obj == received); // Make sure it's copied
    });
  }

  protected <T> void testSend(T val) {
    testSend(val, null);
  }

  protected abstract <T> void testSend(T val, Consumer<T> consumer);

  protected <T> void testReply(T val) {
    testReply(val, null);
  }

  protected abstract <T> void testReply(T val, Consumer<T> consumer);

  protected <T> void testPublish(T val) {
    testPublish(val, null);
  }

  protected abstract <T> void testPublish(T val, Consumer<T> consumer);
}
