package dao;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

import org.json.JSONObject;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import model.Chat;

/**
 * Data Access Object for users and chats.
 */
public class Dao {
  private static MongoClient mongoClient;
  private static DB db;
  private static DBCollection usersColl;
  private static DBCollection chatsColl;
  static {
    try {
      mongoClient = new MongoClient("localhost", 27017);
      db = mongoClient.getDB("chatbox");
      usersColl = db.getCollection("users");
      chatsColl = db.getCollection("chats");
    } catch (UnknownHostException e) {
      System.out.println(e.getStackTrace());
    }
  }

  /**
   * Saves a user to the database if the user doesn't already exist.
   *
   * @param user the user to save
   */
  public static void saveUser(String user) {
    DBObject userRecord = new BasicDBObject("name", user);
    if (usersColl.findOne(userRecord) == null) {
      usersColl.insert(userRecord);
    }
  }

  /**
   * Gets all users from the database.
   *
   * @return list of usernames in {@link String}
   */
  public static List<String> getUsers() {
    List<String> result = new ArrayList<>();
    // Sort the users alphabetically in ascending order.
    usersColl.find().sort(new BasicDBObject("name", -1))
        .forEach(user -> result.add((String) user.get("name")));
    return result;
  }

  /**
   * Saves a chat record to database.
   *
   * @param chat the chat record that will be saved
   */
  public static void saveChat(Chat chat) {
    DBObject chatRecord = new BasicDBObject("sender", chat.getSender().getName())
        .append("receiver", chat.getReceiver().getName())
        .append("message", chat.getMessage())
        .append("timestamp", chat.getTimestamp().toDate());
    chatsColl.insert(chatRecord);
  }

  /**
   * Get chat history for sender and receiver.
   *
   * @param sender one of the participants of the chat history
   * @param receiver the other participant of the chat history
   *
   * @return a {@link List} of {@link Chat} between the two participants.
   */
  public static List<Chat> getChats(String sender, String receiver) {
    // query has sender as sender, and receiver as receiver.
    List<Chat> result = new ArrayList<>();
    BasicDBList conds = new BasicDBList();
    conds.add(new BasicDBObject("sender", sender));
    conds.add(new BasicDBObject("receiver", receiver));
    BasicDBObject query = new BasicDBObject("$and", conds);

    // query2 has sender as receiver, and receiver as sender.
    BasicDBList conds2 = new BasicDBList();
    conds2.add(new BasicDBObject("sender", receiver));
    conds2.add(new BasicDBObject("receiver", sender));
    BasicDBObject query2 = new BasicDBObject("$and", conds2);

    // fullQuery will get all chats between sender and receiver.
    BasicDBList or = new BasicDBList();
    or.add(query);
    or.add(query2);
    BasicDBObject fullQuery = new BasicDBObject("$or", or);

    // Sort the chats in descending order.
    chatsColl.find(fullQuery).sort(new BasicDBObject("timestamp", 1)).forEach(chat -> {
      try {
        Chat record = Chat.fromJson(new JSONObject(chat.toString()));
        result.add(record);
      } catch (Exception e) {
        System.out.println(e.getStackTrace());
      }
    });
    return result;
  }
}
