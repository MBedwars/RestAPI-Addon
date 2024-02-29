package de.marcely.bedwars.restapi.auth;

import de.marcely.bedwars.restapi.util.Util;
import io.javalin.http.Context;
import io.javalin.http.Header;
import io.javalin.http.UnauthorizedResponse;
import io.javalin.security.BasicAuthCredentials;
import io.javalin.security.RouteRole;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.Nullable;

public class AuthController {

  private final Map<String, User> users = new HashMap<>();

  public Collection<User> getAll() {
    return this.users.values();
  }

  public void clear() {
    this.users.clear();
  }

  @Nullable
  public User getByName(String name) {
    return this.users.get(name.toLowerCase());
  }

  public void add(User user) {
    this.users.put(user.getUsername().toLowerCase(), user);
  }

  public void addDefaultUser() {
    add(new User(
        "default",
        Util.generateRandomPassword(30),
        Permission.fetchAll()
    ));
  }

  public Set<Permission> getPermissions(Context ctx) {
    final BasicAuthCredentials credentials = ctx.basicAuthCredentials();

    if (credentials == null)
      return Collections.emptySet();

    final User user = getByName(credentials.getUsername());

    if (user == null || !Arrays.equals(user.getPassword(), credentials.getPassword().toCharArray()))
      return Collections.emptySet();

    return user.getPermissions();
  }

  public void handleAccess(Context ctx) {
    final Set<RouteRole> required = ctx.routeRoles();
    final Set<Permission> having = getPermissions(ctx);

    if (required.isEmpty() || having.stream().anyMatch(required::contains))
      return; // got permission

    ctx.header(Header.WWW_AUTHENTICATE, "Basic");

    throw new UnauthorizedResponse();
  }
}
