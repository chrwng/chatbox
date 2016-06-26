package model;

/**
 * Model class for user.
 */
public class User {
  private String name;

  public static User fromString(String name) {
    return new User(name);
  }

  public User(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof User) {
      return name.equals(((User) o).getName());
    }
    return false;
  }
}
