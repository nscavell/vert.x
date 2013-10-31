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

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class BridgePeer extends AbstractBridgeTestClient {

  @Override
  protected boolean serverMode() {
    return true;
  }

  public void testSendJsonInitialize() {
    JsonObject json = new JsonObject()
      .putString("name", "John Doe")
      .putArray(
        "addresses", new JsonArray().add(
        new JsonObject().putString("street", "123 Maple St")).add(
        new JsonObject().putString("street", "333 First Ave"))
      ).putObject("spouse", new JsonObject().putString("name", "Jane Doe"));

    assertSendMessageEquals(json, null);
  }

  public void testSendJsonArrayInitialize() {
    assertSendMessageEquals(new JsonArray()
      .addString("one")
      .addNumber(1)
      .addObject(new JsonObject().putString("foo", "bar"))
    , null);
  }

  public void testSendStringInitialize() {
    assertSendMessageEquals("String", null);
  }

  public void testSendBooleanInitialize() {
    assertSendMessageEquals(true, null);
  }

  public void testSendIntegerInitialize() {
    assertSendMessageEquals(123, Integer.class);
  }

  public void testSendLongInitialize() {
    assertSendMessageEquals(456L, Long.class);
  }

  public void testSendDoubleInitialize() {
    assertSendMessageEquals(123.456D, Double.class);
  }

  public void testSendFloatInitialize() {
    assertSendMessageEquals(456.789F, Float.class);
  }

  public void testSendShortInitialize() {
    assertSendMessageEquals((short) 789, Short.class);
  }

  private void assertSendMessageEquals(final Object expected, final Class<? extends Number> numberType) {
    vertx.eventBus().registerHandler("test.send", new Handler<Message<Object>>() {
        @Override
        public void handle(Message<Object> message) {
          Object contents = message.body();
          if (contents instanceof Number && numberType != contents.getClass()) {
            // Double and Integer are default values
            if (numberType == Float.class) {
              contents = ((Number) contents).floatValue();
            } else if (numberType == Short.class) {
              contents = ((Number) contents).shortValue();
            } else if (numberType == Long.class) {
              contents = ((Number) contents).longValue();
            }
          }
          tu.azzert(expected.equals(contents));
          tu.testComplete();
        }
      }, new Handler<AsyncResult<Void>>() {
        @Override
        public void handle(AsyncResult<Void> event) {
          if (event.succeeded()) {
            tu.testComplete();
          } else {
            event.cause().printStackTrace();
            tu.azzert(false, "Failed to register");
          }
        }
      }
    );
  }
}
