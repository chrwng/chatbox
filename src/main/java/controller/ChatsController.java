package controller;

import org.eclipse.jetty.websocket.api.Session;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import dao.Dao;
import model.Chat;
import model.User;

import static j2html.TagCreator.article;
import static j2html.TagCreator.b;
import static j2html.TagCreator.p;
import static j2html.TagCreator.span;

/**
 * Controller in charge of processing chat message, and broadcasting chat history.
 */
public class ChatsController {

  /**
   * Sends the entire chat history to user.
   *
   * @param content JSON object containing the sender and receiver
   * @param user the session to which the chat history will be sent
   * @throws JSONException
   */
  public static void sendChatHistory(JSONObject content, Session user) throws JSONException {
    String sender = content.getString("sender");
    String receiver = content.getString("receiver");
    sendChatHistoryToUser(user, sender, receiver);
  }

  /**
   * Sends the entire chat history to user.
   *
   * @param content JSON object containing the sender, the receiver and the message
   * @param senderSession the session of the sender
   * @param receiverSession the session of the receiver
   * @throws JSONException
   */
  public static void sendChatMessage(JSONObject content,
      @Nullable Session senderSession, @Nullable Session receiverSession) throws JSONException {
    String sender = content.getString("sender");
    String receiver = content.getString("receiver");
    String msg = content.getString("message");

    Chat chat = new Chat(
        User.fromString(sender), User.fromString(receiver), msg, DateTime.now());

    // Persist the chat to db.
    Dao.saveChat(chat);

    // Only send to sender if the web session is open.
    if (senderSession != null) {
        sendChatToUser(senderSession, chat);
    }

    // Only send to receiver if the web session is open.
    if (receiverSession != null) {
        sendChatToUser(receiverSession, chat);
    }
  }

  private static void sendChatToUser(Session user, Chat chat) {
    try {
        user.getRemote().sendString(String.valueOf(new JSONObject()
            .put("userMessage", createHtmlMessageFromChat(chat))
            .put("sender", chat.getSender().getName())
        ));
    } catch (Exception e) {
        e.printStackTrace();
    }
  }

  private static void sendChatHistoryToUser(Session user, String sender, String receiver) {
    List<Chat> history = Dao.getChats(sender, receiver);
    List<String> chatHistory = history.stream().map(h -> createHtmlMessageFromChat(h)).collect(
        Collectors.toList());
    try {
      JSONObject obj = new JSONObject();
      JSONArray arr = new JSONArray(chatHistory);
      obj.put("history", arr);
      user.getRemote().sendString(String.valueOf(obj));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  //Builds a HTML element with a sender-name, a message, and a timestamp,
  private static String createHtmlMessageFromChat(Chat chat) {
    return article().with(
        b(chat.getSender().getName() + " says:"),
        p(chat.getMessage()),
        span().withClass("timestamp").withText(
            new SimpleDateFormat("HH:mm:ss").format(chat.getTimestamp().toDate()))
    ).render();
  }
}
