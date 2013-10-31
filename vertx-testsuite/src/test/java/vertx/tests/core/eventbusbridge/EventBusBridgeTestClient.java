package vertx.tests.core.eventbusbridge;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.sockjs.SockJSClientSocket;

import java.util.concurrent.atomic.AtomicInteger;

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
public class EventBusBridgeTestClient extends AbstractBridgeTestClient {

  @Override
  protected boolean serverMode() {
    return true;
  }

  @Override
  protected boolean clientMode() {
    return true;
  }

  public void testRegister() {
    connect(new Handler<SockJSClientSocket>() {
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
    vertx.setTimer(300, new Handler<Long>() {
      @Override
      public void handle(Long event) {
        vertx.eventBus().publish("test.register", "Hello World !");
      }
    });
  }

  public void testRegisterWithResultHandler() {
    final AtomicInteger count = new AtomicInteger();
    final int expected = 4;

    connect(new Handler<SockJSClientSocket>() {
      @Override
      public void handle(SockJSClientSocket sockjs) {
        sockjs.registerHandler("test.register.resulthandler", new Handler<JsonObject>() {
            @Override
            public void handle(JsonObject msg) {
              tu.azzert(msg != null);
              tu.azzert(msg.getString("body") != null);
              tu.azzert(msg.getString("body").equals("Hello World !"));
              checkComplete(expected, count.incrementAndGet());
            }
          }, new AsyncResultHandler<Void>() {
            @Override
            public void handle(AsyncResult<Void> event) {
              checkComplete(expected, count.incrementAndGet());
            }
          }
        );

        sockjs.registerHandler("test.register.resulthandler", new Handler<JsonObject>() {
            @Override
            public void handle(JsonObject msg) {
              tu.azzert(msg != null);
              tu.azzert(msg.getString("body") != null);
              tu.azzert(msg.getString("body").equals("Hello World !"));
              checkComplete(expected, count.incrementAndGet());
            }
          }, new AsyncResultHandler<Void>() {
            @Override
            public void handle(AsyncResult<Void> event) {
              checkComplete(expected, count.incrementAndGet());
            }
          }
        );
      }
    });
    vertx.setTimer(300, new Handler<Long>() {
      @Override
      public void handle(Long event) {
        vertx.eventBus().publish("test.register.resulthandler", "Hello World !");
      }
    });
  }

  public void testUnregister() {
    connect(new Handler<SockJSClientSocket>() {
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
    connect(new Handler<SockJSClientSocket>() {
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
                vertx.eventBus().publish("test.unregister", "blah");
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

    connect(new Handler<SockJSClientSocket>() {
      @Override
      public void handle(SockJSClientSocket sockjs) {
        JsonObject msg = new JsonObject();
        msg.putString("foo", "bar");
        sockjs.send("test.send", msg);
      }
    });
  }

  public void testPublish() {
    final AtomicInteger count = new AtomicInteger(0);

    // register event bus handler
    vertx.eventBus().registerHandler("test.publish", new Handler<Message<JsonObject>>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonObject expected = new JsonObject();
        expected.putString("name", "nick");
        tu.azzert(message != null);
        tu.azzert(message.body() != null);
        tu.azzert(message.body().equals(expected));
        checkComplete(2, count.incrementAndGet());
      }
    });

    connect(new Handler<SockJSClientSocket>() {
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
            checkComplete(2, count.incrementAndGet());
          }
        });
        // publish to all registered handlers
        JsonObject msg = new JsonObject();
        msg.putString("name", "nick");
        sockjs.publish("test.publish", msg);
      }
    });
  }

  private void checkComplete(int expected, int current) {
    if (expected == current) {
      tu.testComplete();
    }
  }

  private void checkComplete(int expected, int current, long delay) {
    if (expected == current) {
      vertx.setTimer(delay, new Handler<Long>() {
        @Override
        public void handle(Long event) {
          tu.testComplete();
        }
      });
    }
  }
}
