package vertx.tests.core.eventbusbridge;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.sockjs.SockJSClient;
import org.vertx.java.core.sockjs.SockJSClientSocket;
import org.vertx.java.core.sockjs.SockJSServer;
import org.vertx.java.testframework.TestClientBase;

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
 *
 */
public class EventBusBridgeTestClient extends TestClientBase {

  private HttpServer server;

  @Override
  public void start() {
    super.start();

    server = vertx.createHttpServer();

    JsonArray permitted = new JsonArray();
    permitted.add(new JsonObject()); // Let everything through

    SockJSServer sockJSServer = vertx.createSockJSServer(server);
    sockJSServer.bridge(new JsonObject().putString("prefix", "/eventbus"), permitted, permitted);

    server.listen(8080, new Handler<AsyncResult<HttpServer>>() {
      @Override
      public void handle(AsyncResult<HttpServer> result) {
        if (result.succeeded()) {
          tu.appReady();
        } else {
          result.cause().printStackTrace();
          tu.azzert(false, "Failed to listen");
        }
      }
    });
  }

  @Override
  public void stop() {
    server.close(new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.succeeded()) {
          EventBusBridgeTestClient.super.stop();
        } else {
          result.cause().printStackTrace();
          tu.azzert(false, "Failed to listen");
        }
      }
    });
  }

  public void testRegister() {
    final SockJSClient sockJSClient = vertx.createSockJSClient(vertx.createHttpClient().setPort(8080));
    sockJSClient.open("eventbus", new Handler<SockJSClientSocket>() {
      @Override
      public void handle(SockJSClientSocket sockjs) {
        sockjs.registerHandler("test.register", new Handler<JsonObject>() {
          @Override
          public void handle(JsonObject msg) {
            tu.azzert(msg != null);
            tu.azzert(msg.getString("body") != null);
            tu.azzert(msg.getString("body").equals("Hello World !"));
            tu.testComplete();
          }
        });
      }
    });
    vertx.setTimer(100, new Handler<Long>() {
      @Override
      public void handle(Long event) {
        vertx.eventBus().publish("test.register", "Hello World !");
      }
    });
  }

  public void testUnregister() {
    final SockJSClient sockJSClient = vertx.createSockJSClient(vertx.createHttpClient().setPort(8080));
    sockJSClient.open("eventbus", new Handler<SockJSClientSocket>() {
      @Override
      public void handle(final SockJSClientSocket sockjs) {
        final Handler<JsonObject> handler = new Handler<JsonObject>() {
          @Override
          public void handle(JsonObject event) {
            tu.azzert(false);
          }
        };
        sockjs.registerHandler("test.unregister", handler);
        vertx.setTimer(100, new Handler<Long>() {
          @Override
          public void handle(Long event) {
            sockjs.unregisterHandler("test.unregister", handler);
            vertx.setTimer(100, new Handler<Long>() {
              @Override
              public void handle(Long event) {
                vertx.eventBus().publish("test.unregister", "boom");
              }
            });
          }
        });
      }
    });
    vertx.eventBus().registerHandler("test.unregister", new Handler<Message>() {
      @Override
      public void handle(Message event) {
        // Delay test completion to ensure all destinations could be reached
        vertx.setTimer(100, new Handler<Long>() {
          @Override
          public void handle(Long event) {
            tu.testComplete();
          }
        });
      }
    });
  }

  public void testUnregister2() {
    final SockJSClient sockJSClient = vertx.createSockJSClient(vertx.createHttpClient().setPort(8080));
    sockJSClient.open("eventbus", new Handler<SockJSClientSocket>() {
      @Override
      public void handle(final SockJSClientSocket sockjs) {
        final Handler<JsonObject> handler1 = new Handler<JsonObject>() {
          @Override
          public void handle(JsonObject event) {
            vertx.setTimer(100, new Handler<Long>() {
              @Override
              public void handle(Long event) {
                tu.testComplete();
              }
            });
          }
        };
        final Handler<JsonObject> handler2 = new Handler<JsonObject>() {
          @Override
          public void handle(JsonObject event) {
            tu.azzert(false);
          }
        };
        sockjs.registerHandler("test.unregister", handler1);
        sockjs.registerHandler("test.unregister", handler2);
        // Delay then unregister
        vertx.setTimer(100, new Handler<Long>() {
          @Override
          public void handle(Long event) {
            sockjs.unregisterHandler("test.unregister", handler2);
            vertx.setTimer(100, new Handler<Long>() {
              @Override
              public void handle(Long event) {
                vertx.eventBus().publish("test.unregister", "boom");
              }
            });
          }
        });
      }
    });
  }

  public void testSend() {
    vertx.eventBus().registerHandler("test.send", new Handler<Message<JsonObject>>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonObject expected = new JsonObject();
        expected.putString("foo", "bar");
        tu.azzert(message != null);
        tu.azzert(message.body() != null);
        tu.azzert(message.body().equals(expected));
        tu.testComplete();
      }
    });

    final SockJSClient sockJSClient = vertx.createSockJSClient(vertx.createHttpClient().setPort(8080));
    sockJSClient.open("eventbus", new Handler<SockJSClientSocket>() {
      @Override
      public void handle(SockJSClientSocket sockjs) {
        JsonObject msg = new JsonObject();
        msg.putString("foo", "bar");
        sockjs.send("test.send", msg);
      }
    });
  }

  public void testPublish() {
    final int[] count = new int[1];

    // register event bus handler
    vertx.eventBus().registerHandler("test.publish", new Handler<Message<JsonObject>>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonObject expected = new JsonObject();
        expected.putString("name", "nick");
        tu.azzert(message != null);
        tu.azzert(message.body() != null);
        tu.azzert(message.body().equals(expected));
        count[0]++;
      }
    });

    final SockJSClient sockJSClient = vertx.createSockJSClient(vertx.createHttpClient().setPort(8080));
    sockJSClient.open("eventbus", new Handler<SockJSClientSocket>() {
      @Override
      public void handle(SockJSClientSocket sockjs) {
        // register sockjs handler
        sockjs.registerHandler("test.publish", new Handler<JsonObject>() {
          @Override
          public void handle(JsonObject message) {
            JsonObject expected = new JsonObject();
            expected.putString("name", "nick");
            tu.azzert(message != null);
            tu.azzert(message.getObject("body") != null);
            tu.azzert(message.getObject("body").equals(expected));
            count[0]++;
          }
        });
        // publish to all registered handlers
        JsonObject msg = new JsonObject();
        msg.putString("name", "nick");
        sockjs.publish("test.publish", msg);
      }
    });

    //TODO: Best way to verify the pub went to all event bus handlers ?
    vertx.setTimer(100, new Handler<Long>() {
      @Override
      public void handle(Long event) {
        tu.azzert(2 == count[0]);
        tu.testComplete();
      }
    });
  }
}
