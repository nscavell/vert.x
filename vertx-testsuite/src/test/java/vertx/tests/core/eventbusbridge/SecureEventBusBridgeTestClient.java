package vertx.tests.core.eventbusbridge;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.sockjs.SockJSClient;
import org.vertx.java.core.sockjs.SockJSClientSocket;
import org.vertx.java.core.sockjs.SockJSServer;
import org.vertx.java.testframework.TestClientBase;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class SecureEventBusBridgeTestClient extends TestClientBase {

  private HttpServer server;
  private SockJSClient client;

  @Override
  public void start() {
    super.start();

    server = vertx.createHttpServer();

    JsonArray inbound = new JsonArray();
    inbound.addObject(new JsonObject().putString("address", "inbound.address.foo"));
    inbound.addObject(new JsonObject().putString("address_re", "inbound\\.address_re\\.[abc]-[123]"));
    inbound.addObject(new JsonObject().putString("address", "inbound.matching")
      .putObject("match", new JsonObject().putObject("foo", new JsonObject().putString("bar", "baz"))));

    JsonArray outbound = new JsonArray();
    outbound.addObject(new JsonObject().putString("address", "outbound.address.bar"));
    outbound.addObject(new JsonObject().putString("address_re", "outbound\\.address_re\\.[A-Z]{3}-[0-9]{3}"));

    SockJSServer secureSockJSServer = vertx.createSockJSServer(server);
    secureSockJSServer.bridge(new JsonObject().putString("prefix", "/secure-eventbus"), inbound, outbound);

    server.listen(8080, new Handler<AsyncResult<HttpServer>>() {
      @Override
      public void handle(AsyncResult<HttpServer> result) {
        if (result.succeeded()) {
          client = vertx.createSockJSClient(vertx.createHttpClient().setPort(8080));
          tu.appReady();
        } else {
          result.cause().printStackTrace();
          tu.azzert(false, "Secure server failed to listen");
        }
      }
    });
  }

  @Override
  public void stop() {
    client.close();
    server.close(new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.succeeded()) {
          SecureEventBusBridgeTestClient.super.stop();
        } else {
          result.cause().printStackTrace();
          tu.azzert(false, "Failed to listen");
        }
      }
    });
  }

  public void testSecureRegister() {
    final AtomicInteger count = new AtomicInteger();
    final int expected = 2;

    client.open("secure-eventbus", new Handler<SockJSClientSocket>() {
      @Override
      public void handle(SockJSClientSocket sockjs) {
        sockjs.registerHandler("outbound.address.bad", new Handler<JsonObject>() {
          @Override
          public void handle(JsonObject msg) {
            tu.azzert(false);
          }
        });
        sockjs.registerHandler("outbound.address.bar", new Handler<JsonObject>() {
          @Override
          public void handle(JsonObject msg) {
            tu.azzert(2 == msg.getInteger("body"));
            checkComplete(expected, count.incrementAndGet(), 300);
          }
        });
        sockjs.registerHandler("outbound.address_re.ABC-123", new Handler<JsonObject>() {
          @Override
          public void handle(JsonObject msg) {
            tu.azzert(3 == msg.getInteger("body"));
            checkComplete(expected, count.incrementAndGet(), 300);
          }
        });
        sockjs.registerHandler("outbound.address_re.ABC-1233", new Handler<JsonObject>() {
          @Override
          public void handle(JsonObject msg) {
            tu.azzert(false);
          }
        });
      }
    });
    vertx.setTimer(300, new Handler<Long>() {
      @Override
      public void handle(Long event) {
        vertx.eventBus().publish("outbound.address.bad", 1); // should not make it
        vertx.eventBus().publish("outbound.address.bar", 2);
        vertx.eventBus().publish("outbound.address_re.ABC-123", 3);
        vertx.eventBus().publish("outbound.address_re.ABC-1233", 4); // should not make it
      }
    });
  }

  public void testSecureSend() {
    vertx.eventBus().registerHandler("inbound.matching", new Handler<Message<JsonObject>>() {
      @Override
      public void handle(Message<JsonObject> message) {
        tu.azzert(message.body().getObject("foo") != null);
        tu.azzert(message.body().getObject("foo").getString("bar").equals("baz"));
        tu.testComplete();
      }
    }, new AsyncResultHandler<Void>() {
        @Override
        public void handle(AsyncResult<Void> event) {
          if (event.succeeded()) {
            // send it using SockJS client
            client.open("secure-eventbus", new Handler<SockJSClientSocket>() {
              @Override
              public void handle(SockJSClientSocket sockjs) {
                sockjs.send("inbound.matching", new JsonObject().putString("ignore", "this")
                  .putObject("foo", new JsonObject().putString("bar", "baz")));
              }
            });
          } else {
            tu.azzert(false, "Event bus register failed");
          }
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
