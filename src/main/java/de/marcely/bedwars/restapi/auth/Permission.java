package de.marcely.bedwars.restapi.auth;

import io.javalin.security.RouteRole;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Permission implements RouteRole {

  public static final Permission ARENAS_READ = new Permission("arenas.read");
  public static final Permission SERVERS_READ = new Permission("servers.read");
  public static final Permission PLAYERS_READ_ONLINE = new Permission("players.read.online");

  public static final Permission PLAYERS_STATS_READ = new Permission("players.stats.read");
  public static final Permission PLAYERS_STATS_READ_RANK = new Permission("players.stats.read.rank");
  public static final Permission PLAYERS_STATS_READ_LEADERBOARD = new Permission("players.stats.read.leaderboard");
  public static final Permission PLAYERS_STATS_READ_SETS = new Permission("players.stats.read.sets");
  public static final Permission PLAYERS_STATS_WRITE = new Permission("players.stats.write");

  public static final Permission PLAYERS_ACHIEVEMENTS_READ = new Permission("players.achievements.read");
  public static final Permission PLAYERS_ACHIEVEMENTS_READ_TYPES = new Permission("players.achievements.read.types");
  public static final Permission PLAYERS_ACHIEVEMENTS_WRITE = new Permission("players.achievements.read");

  public static final Permission PLAYERS_PROPERTIES_READ = new Permission("players.properties.read");
  public static final Permission PLAYERS_PROPERTIES_WRITE = new Permission("players.properties.write");

  private final String value;


  public static Collection<Permission> fetchAll() {
    try {
      final Field[] fields = Permission.class.getFields();
      final Collection<Permission> permissions = new ArrayList<>(fields.length);

      for (Field f : fields)
        permissions.add((Permission) f.get(null));

      return permissions;
    } catch (Exception e) {
      throw new RuntimeException(e); // shouldn't occur
    }
  }
}