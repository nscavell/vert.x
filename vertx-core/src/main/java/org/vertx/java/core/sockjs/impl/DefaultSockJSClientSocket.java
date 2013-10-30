package org.vertx.java.core.sockjs.impl;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.WebSocket;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.sockjs.SockJSClientSocket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
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
  private Map<String, List<Handler>> handlerMap = new HashMap<>();
  private Map<String, AsyncResult<Void>> registerReplyHandlerResults = new HashMap<>();
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
        Iterator<Handler> iter = handlers(address);
        while (iter.hasNext()) {
          Handler handler = iter.next();
          if (handler instanceof RegisterReplyHandler) {
            iter.remove(); // remove reply handlers
          }
          DefaultSockJSClientSocket.this.handle(handler, message.getField("body"));
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
  public <T> SockJSClientSocket registerHandler(final String address, final Handler<T> handler) {
    return registerHandler(address, handler, null);
  }

  @Override
  public <T> SockJSClientSocket registerHandler(String address, Handler<T> handler, Handler<AsyncResult<Void>> resultHandler) {
    internalRegister(address, handler, resultHandler, false);
    return this;
  }

  @Override
  public <T> SockJSClientSocket unregisterHandler(String address, Handler<T> handler) {
    checkClosed();
    List<Handler> handlers = handlerMap.get(address);
    if (handlers != null && handlers.remove(handler) && handlers.isEmpty()) {
      sendUnregister(address);
      handlerMap.remove(address);
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

  private <T> void internalRegister(String address, Handler<T> handler, Handler<AsyncResult<Void>> resultHandler, boolean isReplyHandler) {
    checkClosed();
    String replyAddress = (resultHandler == null) ? null : address + ".io.vertx.reply";

    // register handler
    List<Handler> handlers = handlerMap.get(address);
    if (handlers == null) {
      handlers = new ArrayList<>();
      handlerMap.put(address, handlers);
      if (!isReplyHandler) {
        sendRegister(address, replyAddress);
      }
    }
    handlers.add(handler);

    // register reply handler
    if (resultHandler != null) {
      AsyncResult<Void> result = registerReplyHandlerResults.get(replyAddress);
      if (result != null) { // This means we have already captured the outcome since we don't send multiple registers for same address
        resultHandler.handle(result);
      } else {
        internalRegister(replyAddress, new RegisterReplyHandler(address, replyAddress, resultHandler), null, true);
      }
    }
  }

  private Iterator<Handler> handlers(String address) {
    List<Handler> handlers = handlerMap.get(address);
    if (handlers == null) {
      List<Handler> list = Collections.emptyList();
      return list.iterator();
    }

    return new ArrayList<>(handlers).iterator();
  }

  private void sendPing() {
    send(pingMessage);
  }

  private void sendRegister(String address, String replyAddress) {
    JsonObject message = new JsonObject();
    message.putString("type", "register");
    message.putString("address", address);
    if (replyAddress != null) {
      message.putString("replyAddress", replyAddress);
    }
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
    if (body instanceof byte[]) {
      message.putBinary("body", (byte[]) body);
    } else if (body instanceof Buffer) {
      message.putBinary("body", ((Buffer) body).getBytes());
    } else if (body instanceof Byte) {
      message.putBinary("body", new byte[]{(Byte) body});
    } else {
      message.putValue("body", body);
    }
    send(message.encode());
  }

  private void send(String message) {
    websocket.writeTextFrame(message);
  }

  private <T> void handle(Handler<T> handler, T message) {
    handler.handle(message);
  }

  // Might as well cache this message since we're sending it every 5 seconds
  private static final String pingMessage;

  static {
    JsonObject json = new JsonObject();
    json.putString("type", "ping");
    pingMessage = json.encode();
  }

  private class RegisterReplyHandler implements Handler<JsonObject> {
    private final String address;
    private final String replyAddress;
    private final Handler<AsyncResult<Void>> resultHandler;

    private RegisterReplyHandler(String address, String replyAddress, Handler<AsyncResult<Void>> resultHandler) {
      this.address = address;
      this.replyAddress = replyAddress;
      this.resultHandler = resultHandler;
    }

    @Override
    public void handle(JsonObject message) {
      RegisterResult result = new RegisterResult(message.getString("result"), address);
      registerReplyHandlerResults.put(replyAddress, result);
      resultHandler.handle(result);
    }
  }

  private static class RegisterResult implements AsyncResult<Void> {
    private final Throwable cause;

    private RegisterResult(String resultString, String address) {
      if ("success".equals(resultString)) {
        cause = null;
      } else {
        cause = new Exception("Failed to register handler for address " + address);
      }
    }

    @Override
    public Void result() {
      return null;
    }

    @Override
    public Throwable cause() {
      return cause;
    }

    @Override
    public boolean succeeded() {
      return (cause == null);
    }

    @Override
    public boolean failed() {
      return (cause != null);
    }
  }
}
