package de.marcely.bedwars.restapi.auth;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Data;

@Data
public class User {

  private final String username;
  private final char[] password;
  private final Set<Permission> permissions;

  public User(String username, char[] password, Collection<Permission> permissions) {
    this.username = username;
    this.password = password;
    this.permissions = new LinkedHashSet<>(permissions);
  }
}
