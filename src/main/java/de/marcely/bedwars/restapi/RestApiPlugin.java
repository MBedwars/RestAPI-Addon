package de.marcely.bedwars.restapi;

import de.marcely.bedwars.restapi.auth.AuthController;
import de.marcely.bedwars.restapi.auth.Permission;
import de.marcely.bedwars.restapi.controller.ArenasController;
import de.marcely.bedwars.restapi.controller.HelloController;
import de.marcely.bedwars.restapi.controller.PlayersAchievementsController;
import de.marcely.bedwars.restapi.controller.PlayersOnlineController;
import de.marcely.bedwars.restapi.controller.PlayersPropertiesController;
import de.marcely.bedwars.restapi.controller.PlayersStatsController;
import de.marcely.bedwars.restapi.controller.ServersController;
import io.javalin.Javalin;
import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.openapi.plugin.redoc.ReDocPlugin;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import static io.javalin.apibuilder.ApiBuilder.*;

@Getter
public class RestApiPlugin extends JavaPlugin {

  private static final byte MBEDWARS_API_NUM = 100;
  private static final String MBEDWARS_API_NAME = "5.4";

  private static RestApiPlugin instance;

  private final Set<Permission> registeredPermissions = new HashSet<>();

  private RestApiAddon addon;
  private Javalin javalinServer;
  private AuthController authController;

  @Override
  public void onLoad() {
    this.registeredPermissions.addAll(Permission.fetchAll());

    // plugins may include custom permissions during this phase
  }

  @Override
  public void onEnable() {
    RestApiPlugin.instance = this;

    if (!validateMBedwars())
      return;

    (this.addon = new RestApiAddon(this)).register();
    (this.authController = new AuthController()).addDefaultUser();

    Configs.load(this);
    runServer();
  }

  @Override
  public void onDisable() {
    if (this.javalinServer == null)
      return;

    this.javalinServer.stop();
  }

  private boolean validateMBedwars() {
    try {
      final Class<?> apiClass = Class.forName("de.marcely.bedwars.api.BedwarsAPI");
      final int apiVersion = (int) apiClass.getMethod("getAPIVersion").invoke(null);

      if (apiVersion < MBEDWARS_API_NUM)
        throw new IllegalStateException();
    } catch (Exception e) {
      getLogger().warning("Sorry, your installed version of MBedwars is not supported. Please install at least v" + MBEDWARS_API_NAME);
      Bukkit.getPluginManager().disablePlugin(this);

      return false;
    }

    return true;
  }

  private void runServer() {
    final HelloController helloController = new HelloController(this);

    this.javalinServer = Javalin.create(config -> {
      config.jetty.defaultHost = Configs.host;
      config.jetty.defaultPort = Configs.port;

      config.registerPlugin(new OpenApiPlugin(pluginConfig -> {
        pluginConfig.withDefinitionConfiguration((version, definition) -> {
          definition.withSecurity(securityConfig -> {
            securityConfig.withBasicAuth();
          });
          definition.withInfo(info -> {
            info.title("MBedwars REST API");
            info.summary("Installed on a Spigot server, it allows you to access a wide variety of data.");
            info.contact("Support", "https://mbedwars.com/support", "support@marcely.de");
            info.version(this.getDescription().getVersion());
          });
        });
      }));
      config.registerPlugin(new ReDocPlugin());
      config.bundledPlugins.enableCors(cors -> {
        cors.addRule(it -> {
          final List<String> withoutAny = Configs.allowedCorsOrigins.stream()
              .filter(v -> !v.equals("*"))
              .collect(Collectors.toList());

          if (withoutAny.size() != Configs.allowedCorsOrigins.size())
            it.anyHost();

          if (withoutAny.isEmpty())
            return;

          it.allowHost(
              withoutAny.get(0),
              Arrays.copyOfRange(withoutAny.toArray(new String[0]), 1, withoutAny.size()));
        });
      });

      config.router.mount(router -> {
        router.beforeMatched(this.authController::handleAccess);
      });

      config.router.apiBuilder(() -> {
        get(helloController::info);

        path("servers", () -> {
          get(ServersController::getAll, Permission.SERVERS_READ);
          get("{channelName}", ServersController::getOne, Permission.SERVERS_READ);
        });

        path("arenas", () -> {
          get(ArenasController::getAll, Permission.ARENAS_READ);
          get("{restId}", ArenasController::getOne, Permission.ARENAS_READ);
        });

        path("players", () -> {
          path("online", () -> {
            get(PlayersOnlineController::getAll, Permission.PLAYERS_READ_ONLINE);
            get("{uuid}", PlayersOnlineController::getOne, Permission.PLAYERS_READ_ONLINE);
          });
          path("stats/{uuid}", () -> {
            get(PlayersStatsController::getOne, Permission.PLAYERS_STATS_READ);
            patch(PlayersStatsController::update, Permission.PLAYERS_STATS_WRITE);
            get("leaderboard", PlayersStatsController::getOneLeaderboard, Permission.PLAYERS_STATS_READ_RANK);
          });
          path("stat-sets", () -> {
            get(PlayersStatsController::getAllSets, Permission.PLAYERS_STATS_READ_SETS);
          });
          path("stats-leaderboard", () -> {
            get(PlayersStatsController::getLeaderboard, Permission.PLAYERS_STATS_READ_LEADERBOARD);
          });
          path("achievements/{uuid}", () -> {
            get(PlayersAchievementsController::getOne, Permission.PLAYERS_ACHIEVEMENTS_READ);
            patch(PlayersAchievementsController::update, Permission.PLAYERS_ACHIEVEMENTS_WRITE);
          });
          path("achievement-types", () -> {
            get(PlayersAchievementsController::getAllTypes, Permission.PLAYERS_ACHIEVEMENTS_READ_TYPES);
          });
          path("properties/{uuid}", () -> {
            get(PlayersPropertiesController::getOne, Permission.PLAYERS_PROPERTIES_READ);
            patch(PlayersPropertiesController::update, Permission.PLAYERS_PROPERTIES_WRITE);
          });
        });
      });
    }).start();
  }
}
