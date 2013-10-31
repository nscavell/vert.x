/*
 * Copyright 2013 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package vertx.tests.core.eventbusbridge;

import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.sockjs.SockJSClientSocket;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class BridgeClient extends AbstractBridgeTestClient {

  @Override
  protected boolean clientMode() {
    return true;
  }

  public void testSendJson() {
    send(new JsonObject()
      .putString("name", "John Doe")
      .putArray(
        "addresses", new JsonArray().add(
        new JsonObject().putString("street", "123 Maple St")).add(
        new JsonObject().putString("street", "333 First Ave"))
      ).putObject("spouse", new JsonObject().putString("name", "Jane Doe")));
  }

  public void testSendJsonArray() {
    send(new JsonArray()
      .addString("one")
      .addNumber(1)
      .addObject(new JsonObject().putString("foo", "bar"))
    );
  }

  public void testSendString() {
    send("String");
  }

  public void testSendBoolean() {
    send(true);
  }

  public void testSendInteger() {
    send(123);
  }

  public void testSendLong() {
    send(456L);
  }

  public void testSendDouble() {
    send(123.456D);
  }

  public void testSendFloat() {
    send(456.789F);
  }

  public void testSendShort() {
    send((short) 789);
  }

  private void send(final Object message) {
    final String address = "test.send";
    connect("eventbus", new Handler<SockJSClientSocket>() {
      @Override
      public void handle(SockJSClientSocket sockjs) {
        if (message instanceof JsonObject) {
          sockjs.send(address, (JsonObject) message);
        } else if (message instanceof JsonArray) {
          sockjs.send(address, (JsonArray) message);
        } else if (message instanceof String) {
          sockjs.send(address, (String) message);
        } else if (message instanceof Boolean) {
          sockjs.send(address, (Boolean) message);
        } else if (message instanceof Integer) {
          sockjs.send(address, (Integer) message);
        } else if (message instanceof Long) {
          sockjs.send(address, (Long) message);
        } else if (message instanceof Double) {
          sockjs.send(address, (Double) message);
        } else if (message instanceof Float) {
          sockjs.send(address, (Float) message);
        } else if (message instanceof Short) {
          sockjs.send(address, (Short) message);
        } else if (message instanceof Character) {
          sockjs.send(address, (Character) message);
        }
      }
    });
  }
}
