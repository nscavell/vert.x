/*
 * Copyright (c) 2011-2013 The original author or authors
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

package org.vertx.java.core.eventbus.impl;

import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.MessageCodec;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class MessageFactory {

  static final byte TYPE_PING = 0;
  static final byte TYPE_BUFFER = 1;
  static final byte TYPE_BOOLEAN = 2;
  static final byte TYPE_BYTEARRAY = 3;
  static final byte TYPE_BYTE = 4;
  static final byte TYPE_CHARACTER = 5;
  static final byte TYPE_DOUBLE = 6;
  static final byte TYPE_FLOAT = 7;
  static final byte TYPE_INT = 8;
  static final byte TYPE_LONG = 9;
  static final byte TYPE_SHORT = 10;
  static final byte TYPE_STRING = 11;
  static final byte TYPE_JSON_OBJECT = 12;
  static final byte TYPE_JSON_ARRAY = 13;
  static final byte TYPE_CUSTOM_OBJECT = 14;
  static final byte TYPE_REPLY_FAILURE = 100;

  private final Map<String, MessageCodec<?>> codecMap = new ConcurrentHashMap<>();

  public <T> void registerCodec(Class<?> type, MessageCodec<T> codec) {
    codecMap.put(type.getName(), codec);
  }

  public <U> BaseMessage<U> createMessage(boolean send, String address, U message) {
    BaseMessage<?> bm;
    if (message instanceof String) {
      bm = new StringMessage(send, address, (String) message, this);
    } else if (message instanceof Buffer) {
      bm = new BufferMessage(send, address, (Buffer) message, this);
    } else if (message instanceof JsonObject) {
      bm = new JsonObjectMessage(send, address, (JsonObject) message, this);
    } else if (message instanceof JsonArray) {
      bm = new JsonArrayMessage(send, address, (JsonArray) message, this);
    } else if (message instanceof byte[]) {
      bm = new ByteArrayMessage(send, address, (byte[]) message, this);
    } else if (message instanceof Integer) {
      bm = new IntMessage(send, address, (Integer) message, this);
    } else if (message instanceof Long) {
      bm = new LongMessage(send, address, (Long) message, this);
    } else if (message instanceof Float) {
      bm = new FloatMessage(send, address, (Float) message, this);
    } else if (message instanceof Double) {
      bm = new DoubleMessage(send, address, (Double) message, this);
    } else if (message instanceof Boolean) {
      bm = new BooleanMessage(send, address, (Boolean) message, this);
    } else if (message instanceof Short) {
      bm = new ShortMessage(send, address, (Short) message, this);
    } else if (message instanceof Character) {
      bm = new CharacterMessage(send, address, (Character) message, this);
    } else if (message instanceof Byte) {
      bm = new ByteMessage(send, address, (Byte) message, this);
    } else if (message == null) {
      bm = new StringMessage(send, address, null, this);
    } else {
      Class<?> c = message.getClass();
      MessageCodec<U> codec = getCodec(c);
      if (codec != null) {
        bm = new ObjectMessage<>(send, address, message, this);
      } else {
        throw new IllegalArgumentException("Cannot send object of class " + c + " on the event bus: " + message);
      }
    }

    @SuppressWarnings("unchecked")
    BaseMessage<U> baseMessage = (BaseMessage<U>) bm;
    return baseMessage;
  }

  BaseMessage read(Buffer buff) {
    byte type = buff.getByte(0);
    switch (type) {
      case TYPE_PING:
        return new PingMessage(buff, this);
      case TYPE_BUFFER:
        return new BufferMessage(buff, this);
      case TYPE_BOOLEAN:
        return new BooleanMessage(buff, this);
      case TYPE_BYTEARRAY:
        return new ByteArrayMessage(buff, this);
      case TYPE_BYTE:
        return new ByteMessage(buff, this);
      case TYPE_CHARACTER:
        return new CharacterMessage(buff, this);
      case TYPE_DOUBLE:
        return new DoubleMessage(buff, this);
      case TYPE_FLOAT:
        return new FloatMessage(buff, this);
      case TYPE_INT:
        return new IntMessage(buff, this);
      case TYPE_LONG:
        return new LongMessage(buff, this);
      case TYPE_SHORT:
        return new ShortMessage(buff, this);
      case TYPE_STRING:
        return new StringMessage(buff, this);
      case TYPE_JSON_OBJECT:
        return new JsonObjectMessage(buff, this);
      case TYPE_JSON_ARRAY:
        return new JsonArrayMessage(buff, this);
      case TYPE_REPLY_FAILURE:
        return new ReplyFailureMessage(buff, this);
      case TYPE_CUSTOM_OBJECT:
        return new ObjectMessage<>(buff, this);
      default:
        throw new IllegalStateException("Invalid type " + type);
    }
  }

  //TODO: Lookup via isAssignableFrom so base classes can be registered.
  <T> MessageCodec<T> getCodec(Class<?> type) {
    return getCodec(type.getName());
  }

  @SuppressWarnings("unchecked")
  <T> MessageCodec<T> getCodec(String type) {
    return (MessageCodec<T>) codecMap.get(type);
  }
}
