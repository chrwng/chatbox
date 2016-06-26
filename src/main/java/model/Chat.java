package model;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Model class for a chat record.
 */
public class Chat {
  private User sender;
  private User receiver;
  private String message;
  private DateTime timestamp;

  public static Chat fromJson(JSONObject chat) throws JSONException {
    User sender = User.fromString((String) chat.get("sender"));
    User receiver = User.fromString((String) chat.get("receiver"));

    // There is a known issue with persisting dates into MongoDB, it'll get converted to UTC date,
    // now we need to convert them back to local time.
    DateTime timestamp = ISODateTimeFormat.dateTime()
        .withZone(DateTimeZone.forID("America/New_York"))
        .parseDateTime((String) chat.getJSONObject("timestamp").get("$date"));

    return new Chat(
        sender,
        receiver,
        (String) chat.get("message"),
        timestamp
    );
  }

  public Chat(User sender, User receiver, String message, DateTime timestamp) {
    this.sender = sender;
    this.receiver = receiver;
    this.message = message;
    this.timestamp = timestamp;
  }

  public User getSender() {
    return sender;
  }

  public User getReceiver() {
    return receiver;
  }

  public String getMessage() {
    return message;
  }

  public DateTime getTimestamp() {
    return timestamp;
  }
}
