package org.vertx.java.core.sockjs.impl;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.eventbus.impl.JsonObjectMessage;
import org.vertx.java.core.http.WebSocket;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.sockjs.SockJSClientSocket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SockJS client socket using raw websockets
 *
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class DefaultSockJSClientSocket implements SockJSClientSocket {

  private final Vertx vertx;
  private final WebSocket websocket;
  private final Long timerId;

  private Handler<Void> closeHandler;
  private Map<String, List<Handler<JsonObject>>> handlerMap = new HashMap<>();
  private boolean closed;

  public DefaultSockJSClientSocket(Vertx vertx, WebSocket websocket) {
    this.vertx = vertx;
    this.websocket = websocket;

    // Register close handler
    this.websocket.closeHandler(new Handler<Void>() {
      @Override
      public void handle(Void event) {
        if (timerId != null) {
          DefaultSockJSClientSocket.this.vertx.cancelTimer(timerId);
        }
        if (closeHandler != null) {
          closeHandler.handle(null);
        }
      }
    });
    // Register data handler
    this.websocket.dataHandler(new Handler<Buffer>() {
      @Override
      public void handle(Buffer buffer) {
        //TODO: Handle invalid messages from server
        JsonObject message = new JsonObject(buffer.toString());
        String address = message.getString("address");
        for (Handler<JsonObject> handler : handlers(address)) {
          JsonObject out = new JsonObject();
          out.putValue("body", message.getField("body"));
          handler.handle(out);
        }
      }
    });

    // Send initial ping and then ping every 5 seconds
    sendPing();
    timerId = vertx.setPeriodic(5000, new Handler<Long>() {
      @Override
      public void handle(Long event) {
        sendPing();
      }
    });
  }

  @Override
  public SockJSClientSocket registerHandler(final String address, final Handler<JsonObject> handler) {
    checkClosed();
    List<Handler<JsonObject>> handlers = handlerMap.get(address);
    if (handlers == null) {
      handlers = new ArrayList<>();
      handlerMap.put(address, handlers);
      sendRegister(address);
    }
    handlers.add(handler);

    return this;
  }

  @Override
  public SockJSClientSocket unregisterHandler(String address, Handler<JsonObject> handler) {
    checkClosed();
    List<Handler<JsonObject>> handlers = handlerMap.get(address);
    if (handlers != null && handlers.remove(handler) && handlers.isEmpty()) {
      sendUnregister(address);
    }
    return this;
  }

  @Override
  public SockJSClientSocket send(String address, JsonObject message) {
    checkClosed();
    sendSendOrPub("send", address, message);
    return this;
  }

  @Override
  public SockJSClientSocket publish(String address, JsonObject message) {
    checkClosed();
    sendSendOrPub("publish", address, message);
    return this;
  }

  @Override
  public SockJSClientSocket closeHandler(Handler<Void> handler) {
    this.closeHandler = handler;
    return this;
  }

  @Override
  public SockJSClientSocket exceptionHandler(Handler<Throwable> handler) {
    websocket.exceptionHandler(handler);
    return this;
  }

  @Override
  public void close() {
    websocket.close();
    closed = true;
    //TODO: I don't think we have to unregister everything, server will time them out ?
    handlerMap.clear();
  }

  private void checkClosed() {
    if (closed) {
      throw new IllegalStateException("SockJS socket is closed");
    }
  }

  private Iterable<Handler<JsonObject>> handlers(String address) {
    List<Handler<JsonObject>> handlers = handlerMap.get(address);
    if (handlers == null) {
      return Collections.emptyList();
    }

    return new ArrayList<>(handlers);
  }

  private void sendPing() {
    send(pingMessage);
  }

  private void sendRegister(String address) {
    JsonObject message = new JsonObject();
    message.putString("type", "register");
    message.putString("address", address);
    send(message.encode());
  }

  private void sendUnregister(String address) {
    JsonObject message = new JsonObject();
    message.putString("type", "unregister");
    message.putString("address", address);
    send(message.encode());
  }

  private void sendSendOrPub(String sendOrPub, String address, Object body) {
    JsonObject message = new JsonObject();
    message.putString("type", sendOrPub);
    message.putString("address", address);
    message.putValue("body", body);
    send(message.encode());
  }

  private void send(String message) {
    websocket.writeTextFrame(message);
  }

  // Might as well cache this message since we're sending it every 5 seconds
  private static final String pingMessage;

  static {
    JsonObject json = new JsonObject();
    json.putString("type", "ping");
    pingMessage = json.encode();
  }
}
