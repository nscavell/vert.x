package org.vertx.java.core.sockjs.impl;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.WebSocket;
import org.vertx.java.core.sockjs.SockJSClient;
import org.vertx.java.core.sockjs.SockJSClientSocket;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class DefaultSockJSClient implements SockJSClient {

  private final Vertx vertx;
  private final HttpClient httpClient;
  private boolean closed;

  public DefaultSockJSClient(Vertx vertx, HttpClient httpClient) {
    this.vertx = vertx;
    this.httpClient = httpClient;
  }

  @Override
  public SockJSClient open(String prefix, final Handler<SockJSClientSocket> socketHandler) {
    prefix = parsePrefix(prefix);
    httpClient.connectWebsocket(prefix + "/websocket", new Handler<WebSocket>() {
      @Override
      public void handle(final WebSocket websocket) {
        socketHandler.handle(new DefaultSockJSClientSocket(vertx, websocket));
      }
    });

    return this;
  }

  @Override
  public void close() {
    checkClosed();
    httpClient.close();
    closed = true;
  }

  private void checkClosed() {
    if (closed) {
      throw new IllegalStateException("Client is closed");
    }
  }

  private static String parsePrefix(String prefix) {
    if (prefix == null) throw new NullPointerException();
    if (prefix.charAt(0) != '/') {
      prefix = '/' + prefix;
    }
    if (prefix.charAt(prefix.length() - 1) == '/') {
      prefix = prefix.substring(0, prefix.length() - 1);
    }

    return prefix;
  }


}
