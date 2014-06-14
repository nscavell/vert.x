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

package org.vertx.java.core.eventbus.impl;

import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.eventbus.MessageCodec;
import org.vertx.java.core.shareddata.Shareable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class ObjectMessage<U> extends BaseMessage<U> {
  private Buffer data;
  private boolean encoded;

  public ObjectMessage(boolean send, String address, U object, MessageFactory messageFactory) {
    super(send, address, object, messageFactory, object == null ? null : object.getClass().getName());
  }

  public ObjectMessage(Buffer readBuff, MessageFactory factory) {
    super(readBuff, factory);
  }

  @Override
  protected byte type() {
    return MessageFactory.TYPE_CUSTOM_OBJECT;
  }

  @Override
  protected Message<U> copy() {
    if (body instanceof Shareable) {
      return this;
    } else if (body instanceof Cloneable) {
      Class<?> c = body.getClass();
      try {
        Method method = c.getMethod("clone");
        @SuppressWarnings("unchecked")
        U clone = (U) method.invoke(body);
        return new ObjectMessage<>(send, address, clone, messageFactory);
      } catch (NoSuchMethodException e) {
        throw new RuntimeException("No public clone() method for cloneable class " + c);
      } catch (IllegalAccessException | InvocationTargetException e) {
        throw new RuntimeException("Could not invoke clone method for class " + c, e);
      }
    } else {
      throw new RuntimeException(body.getClass() + " does not implement Cloneable or Shareable.");
    }
  }

  @Override
  protected void readBody(int pos, Buffer readBuff) {
    MessageCodec<U> codec = messageFactory.getCodec(decodedType);
    if (codec == null) {
      throw new RuntimeException("Unable to retrieve message codec for type " + decodedType);
    }
    boolean isNull = readBuff.getByte(pos) == (byte) 0;
    if (!isNull) {
      pos++;
      int buffLength = readBuff.getInt(pos);
      pos += 4;
      byte[] bytes = readBuff.getBytes(pos, pos + buffLength);
      data = new Buffer(bytes);
    }
    body = codec.decode(data);
  }

  @Override
  protected void writeBody(Buffer buff) {
    if (!encoded) { // This really shouldn't happen as getBodyLength should be called first.
      MessageCodec<U> codec = messageFactory.getCodec(decodedType);
      if (codec == null) throw new RuntimeException("Could not retrieve message codec for type " + decodedType);

      data = codec.encode(body);
      encoded = true;
    }
    if (data == null) {
      buff.appendByte((byte) 0);
    } else {
      buff.appendByte((byte) 1);
      buff.appendInt(data.length());
      buff.appendBuffer(data);
    }
  }

  @Override
  protected int getBodyLength() {
    if (!encoded) {
      MessageCodec<U> codec = messageFactory.getCodec(decodedType);
      if (codec == null) throw new RuntimeException("Could not retrieve message codec for type " + decodedType);

      data = codec.encode(body);
      encoded = true;
    }
    return 1 + (data == null ? 0 : 4 + data.length());
  }
}
