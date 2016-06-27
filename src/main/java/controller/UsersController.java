package controller;

import dao.Dao;
import org.eclipse.jetty.websocket.api.Session;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Set;

/**
 * Controller in charge of processing user creation and broadcast to other users.
 */
public class UsersController {

  /**
   * Save a username if it doesn't exist already and broadcast user join to all users.
   *
   * @param username the username that will be saved
   * @param users sessions representing all users
   * @throws JSONException
   */
  public static void saveUserAndBroadcast(String username, Set<Session> users)
      throws JSONException {
    Dao.saveUser(username);
    broadcastUserJoin(users);
  }

  private static void broadcastUserJoin(Set<Session> users) {
    users.stream().filter(Session::isOpen).forEach(session -> {
      try {
        session.getRemote().sendString(String.valueOf(
            new JSONObject().put("userlist", Dao.getUsers())
        ));
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
  }
}
