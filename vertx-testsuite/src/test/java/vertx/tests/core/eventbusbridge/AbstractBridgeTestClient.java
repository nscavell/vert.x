package vertx.tests.core.eventbusbridge;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.sockjs.SockJSClient;
import org.vertx.java.core.sockjs.SockJSClientSocket;
import org.vertx.java.core.sockjs.SockJSServer;
import org.vertx.java.testframework.TestClientBase;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public abstract class AbstractBridgeTestClient extends TestClientBase {

  private HttpServer httpServer;

  private SockJSClient sockJSClient;
  private String prefix;

  @Override
  public final void start() {
    super.start();
    if (serverMode()) {
      httpServer = vertx.createHttpServer();
      SockJSServer sockJSServer = vertx.createSockJSServer(httpServer);
      serverCreated(httpServer, sockJSServer);
    }
    if (clientMode()) {
      sockJSClient = vertx.createSockJSClient(vertx.createHttpClient().setPort(port()));
      clientCreated(sockJSClient);
    }
  }

  protected void serverCreated(HttpServer server, SockJSServer sockJSServer) {
    JsonObject config = bridgeConfig();
    prefix = config.getString("prefix");
    sockJSServer.bridge(config, inbound(), outbound());

    server.listen(port(), new Handler<AsyncResult<HttpServer>>() {
      @Override
      public void handle(AsyncResult<HttpServer> result) {
        if (result.succeeded()) {
          tu.appReady();
        } else {
          result.cause().printStackTrace();
          tu.azzert(false, "Failed to start server on port " + port());
        }
      }
    });
  }

  protected void clientCreated(SockJSClient client) {
  }

  protected void connect(Handler<SockJSClientSocket> handler) {
    sockJSClient.open(prefix, handler);
  }

  @Override
  public final void stop() {
    if (sockJSClient != null) {
      sockJSClient.close();
    }
    if (httpServer != null) {
      httpServer.close(new AsyncResultHandler<Void>() {
        @Override
        public void handle(AsyncResult<Void> result) {
          if (result.succeeded()) {
            AbstractBridgeTestClient.super.stop();
          } else {
            result.cause().printStackTrace();
            tu.azzert(false, "Failed to stop server");
          }
        }
      });
    } else {
      super.stop();
    }
  }

  protected JsonObject bridgeConfig() {
    return new JsonObject().putString("prefix", "/eventbus");
  }

  protected JsonArray inbound() {
    return new JsonArray().add(new JsonObject());
  }

  protected JsonArray outbound() {
    return new JsonArray().add(new JsonObject());
  }

  protected int port() {
    return 8080;
  }

  protected boolean serverMode() {
    return false;
  }

  protected boolean clientMode() {
    return false;
  }
}
