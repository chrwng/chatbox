package controller;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Controller in charge of processing connection, disconnection of web sockets, and receiving and
 * processing messages.
 */
@WebSocket
public class WebSocketsController {
  static BiMap<String, Session> userToSession = HashBiMap.create();

  /**
   * Hook method invoked when web socket is connected. Noop for now.
   *
   * @param user the session that is connected
   * @throws Exception
   */
  @OnWebSocketConnect
  public void onConnect(Session user) throws Exception {}

  /**
   * Hook method invoked when web socket is closed.
   *
   * @param user the session that is closed
   * @param statusCode statusCode of the closed connection
   * @param reason the reason for the close
   */
  @OnWebSocketClose
  public void onClose(Session user, int statusCode, String reason) {
    userToSession.inverse().remove(user);
  }

  /**
   * Hook method invoked when message is sent from client through web socket. The message is
   * encoded in a JSON string, and it can be one of the following three types:
   * <ul>
   *   <li>a request to fetch chat history, which happens when a user start to chat with another
   *      user, and is indicated by having a "history" attribute in the JSON message.</li>
   *   <li>a (potentially new) user establishing socket connection, this is indicated by the lack
   *      of a "receiver" attribute in the JSON message.</li>
   *   <li>a regular chat message, where the JSON would contain the "sender", "receiver" and
   *      "message" attributes.</li>
   * </ul>
   *
   * @param user the session through which message is sent
   * @param message the message content
   * @throws JSONException
   */
  @OnWebSocketMessage
  public void onMessage(Session user, String message) throws JSONException {
    JSONObject jsonMsg = new JSONObject(message);
    if (jsonMsg.has("history")) {
        // The presence of history means the message is a request for chat history.
        ChatsController.sendChatHistory(jsonMsg, user);
        return;
    }
    if (!jsonMsg.has("receiver")) {
        // The absence of receiver means this is a registration.
        String username = jsonMsg.getString("sender");

        // Close the existing session for the user as a new connection is established.
        if (userToSession.containsKey(username)) {
            userToSession.get(username).close();
        }
        userToSession.put(username, user);

        UsersController.saveUserAndBroadcast(username, userToSession.values());
        return;
    }

    // Otherwise it is a normal chat message with sender, receiver and message info.
    String sender = jsonMsg.getString("sender");
    String receiver = jsonMsg.getString("receiver");
    ChatsController.sendChatMessage(jsonMsg, userToSession.get(sender),
                                    userToSession.get(receiver));
  }
}
