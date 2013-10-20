package org.vertx.java.core.sockjs;

import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.streams.ExceptionSupport;

/**
 * //TODO: Should we call this connection or socket or ?
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public interface SockJSClientSocket extends ExceptionSupport<SockJSClientSocket> {

  /**
   * Register's a SockJS handler for the specified address with the vertx event bus
   *
   * @param address the event bus address
   * @param handler the handler which will receive messages
   */
  SockJSClientSocket registerHandler(String address, Handler<JsonObject> handler);

  /**
   * Unregister's a SockJS handler for the specified address. If no more local handlers are registered at this address,
   * it will unregister with the vertx event bus.
   *
   * @param address the event bus address
   * @param handler the handler to unregister
   */
  SockJSClientSocket unregisterHandler(String address, Handler<JsonObject> handler);

  /**
   * Sends a SockJS message to the vertx event bus
   *
   * @param address the event bus address
   * @param message the message to send
   */
  SockJSClientSocket send(String address, JsonObject message);

  /**
   * Publishes a SockJS message to the vertx event bus
   *
   * @param address the event bus address
   * @param message the message to publish
   */
  SockJSClientSocket publish(String address, JsonObject message);

  /**
   * Close handler which will be called when the SockJS connection is closed
   *
   * @param handler the close handler
   */
  SockJSClientSocket closeHandler(Handler<Void> handler);

  /**
   * Closes the SockJS connection
   */
  void close();

  //TODO: Support login
  //TODO: Support reply handlers for send
  //TODO: Support different types of message types (String, Integer, etc) for send/pub
}
