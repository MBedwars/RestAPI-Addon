package de.marcely.bedwars.restapi.controller;

import de.marcely.bedwars.api.player.LeaderboardFetchResult;
import de.marcely.bedwars.api.player.PlayerDataAPI;
import de.marcely.bedwars.api.player.PlayerProperties;
import de.marcely.bedwars.api.player.PlayerStatSet;
import de.marcely.bedwars.api.player.PlayerStats;
import de.marcely.bedwars.restapi.model.ErrorResponse;
import de.marcely.bedwars.restapi.model.player.PlayerFullLeaderboardPositionsModel;
import de.marcely.bedwars.restapi.model.player.PlayerLeaderboardPositionsModel;
import de.marcely.bedwars.restapi.model.player.PlayerPropertiesModel;
import de.marcely.bedwars.restapi.model.player.PlayerStatSetModel;
import de.marcely.bedwars.restapi.model.player.PlayerStatsModel;
import de.marcely.bedwars.restapi.util.Util;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class PlayersStatsController {

  @OpenApi(
      summary = "Get the stats a player owns",
      operationId = "getOnePlayersStats",
      tags = "Player Stats",
      pathParams = {
          @OpenApiParam(name = "uuid", type = String.class, description = "The UUID of the player")
      },
      responses = {
          @OpenApiResponse(status = "200", content = @OpenApiContent(from = PlayerStatsModel.class)),
          @OpenApiResponse(status = "400", content = {@OpenApiContent(from = ErrorResponse.class)}),
          @OpenApiResponse(status = "404", content = {@OpenApiContent(from = ErrorResponse.class)})
      },
      path = "/players/stats/{uuid}",
      methods = {HttpMethod.GET}
  )
  public static void getOne(Context ctx) {
    final UUID uuid = validUUID(ctx);
    final PlayerStats properties = Util.getAwait(c -> PlayerDataAPI.get().getStats(uuid, c));

    ctx.json(PlayerStatsModel.from(properties));
  }


  @OpenApi(
      summary = "Get the ranks of a player",
      operationId = "getOnePlayersLeaderboardPositions",
      tags = "Player Stats",
      pathParams = {
          @OpenApiParam(name = "uuid", type = String.class, description = "The UUID of the player")
      },
      queryParams = {
          @OpenApiParam(name = "statSets", type = String[].class, description = "The ids of the stats ranks to get. Don't add too many, this can get heavy on the database.")
      },
      responses = {
          @OpenApiResponse(status = "200", content = @OpenApiContent(from = PlayerLeaderboardPositionsModel.class)),
          @OpenApiResponse(status = "400", content = {@OpenApiContent(from = ErrorResponse.class)}),
          @OpenApiResponse(status = "404", content = {@OpenApiContent(from = ErrorResponse.class)})
      },
      path = "/players/stats/{uuid}/leaderboard",
      methods = {HttpMethod.GET}
  )
  public static void getOneLeaderboard(Context ctx) {
    final UUID uuid = validUUID(ctx);
    final String[] statSetNames = ctx.queryParamAsClass("statSets", String[].class)
        .check(s -> s != null, "statSets query param must be present")
        .check(s -> s.length == 0, "statSets cannot be empty")
        .get();
    final PlayerStatSet[] statSets = new PlayerStatSet[statSetNames.length];
    final Map<String, Integer> positions = new HashMap<>();

    for (int i = 0; i < statSetNames.length; i++) {
      if ((statSets[i] = PlayerDataAPI.get().getStatsSet(statSetNames[i])) == null)
        throw new BadRequestResponse(String.format("Stat set with id '%s' does not exist", statSetNames[i]));
    }

    for (PlayerStatSet set : statSets) {
      positions.put(
          set.getId(),
          Util.getAwait(c -> PlayerDataAPI.get().fetchLeaderboardPosition(uuid, set, c)));
    }

    ctx.json(new PlayerLeaderboardPositionsModel(uuid, positions));
  }


  @OpenApi(
      summary = "Get all existing stat sets",
      operationId = "getAllPlayersStatSets",
      tags = "Player Stats",
      responses = {
          @OpenApiResponse(status = "200", content = @OpenApiContent(from = PlayerStatSetModel[].class))
      },
      path = "/players/stat-sets",
      methods = {HttpMethod.GET}
  )
  public static void getAllSets(Context ctx) {
    ctx.json(PlayerDataAPI.get().getRegisteredStatSets().stream()
        .map(PlayerStatSetModel::from)
        .collect(Collectors.toList()));
  }

  @OpenApi(
      summary = "Get one existing stat sets by its id",
      operationId = "getOnePlayersStatSet",
      tags = "Player Stats",
      responses = {
          @OpenApiResponse(status = "200", content = @OpenApiContent(from = PlayerStatSetModel.class)),
          @OpenApiResponse(status = "400", content = {@OpenApiContent(from = ErrorResponse.class)}),
          @OpenApiResponse(status = "404", content = {@OpenApiContent(from = ErrorResponse.class)})
      },
      path = "/players/stat-sets/{id}",
      methods = {HttpMethod.GET}
  )
  public static void getOneSet(Context ctx) {
    final String id = ctx.pathParamAsClass("id", String.class)
        .check(s -> !s.isEmpty(), "id cannot be empty")
        .get();
    final PlayerStatSet set = PlayerDataAPI.get().getStatsSet(id);

    if (set == null)
      throw new NotFoundResponse("No stat set under the given id found");

    ctx.json(PlayerStatSetModel.from(set));
  }


  @OpenApi(
      summary = "Get the players that are within a certain rank range",
      description = "Some entries may be missing in the result, this means that there is no player with that rank.",
      operationId = "getPlayersLeaderboard",
      tags = "Player Stats",
      queryParams = {
          @OpenApiParam(name = "statSets", type = String[].class, example = "bedwars:wins,bedwars:wl",
              description = "The ids (, is the seperator) of the stats ranks to get. Don't add too many, this can get heavy on the database."),
          @OpenApiParam(name = "minPos", type = Integer.class, description = "The minimum position to get."),
          @OpenApiParam(name = "maxPos", type = Integer.class, description = "The maximum position to get.")
      },
      responses = {
          @OpenApiResponse(status = "200", content = @OpenApiContent(from = PlayerFullLeaderboardPositionsModel[].class)),
          @OpenApiResponse(status = "400", content = {@OpenApiContent(from = ErrorResponse.class)}),
          @OpenApiResponse(status = "404", content = {@OpenApiContent(from = ErrorResponse.class)})
      },
      path = "/players/stats-leaderboard",
      methods = {HttpMethod.GET}
  )
  public static void getLeaderboard(Context ctx) {
    final String[] statSetNames = ctx.queryParamAsClass("statSets", String.class)
        .check(s -> s != null, "statSets query param must be present")
        .check(s -> !s.isEmpty(), "statSets cannot be empty")
        .get().split(",");
    final int minPos = ctx.queryParamAsClass("minPos", Integer.class)
        .check(s -> s != null, "minPos query param must be present")
        .check(i -> i >= 1, "minPos must be greater than 1")
        .get();
    final int maxPos = ctx.queryParamAsClass("maxPos", Integer.class)
        .check(s -> s != null, "maxPos query param must be present")
        .check(i -> i >= minPos, "maxPos must be greater than or equal to minPos")
        .get();
    final PlayerStatSet[] statSets = new PlayerStatSet[statSetNames.length];
    final Map<UUID, PlayerFullLeaderboardPositionsModel> players = new HashMap<>();

    for (int i = 0; i < statSetNames.length; i++) {
      if ((statSets[i] = PlayerDataAPI.get().getStatsSet(statSetNames[i])) == null)
        throw new BadRequestResponse(String.format("Stat set with id '%s' does not exist", statSetNames[i]));
    }

    for (PlayerStatSet set : statSets) {
      final LeaderboardFetchResult res = Util.getAwait(c -> PlayerDataAPI.get().fetchLeaderboard(set, minPos, maxPos, c));

      for (int i = minPos; i <= maxPos; i++) {
        final PlayerStats stats = res.getStatsAtRank(i);
        final PlayerProperties properties = res.getPropertiesAtRank(i);

        if (stats == null || properties == null)
          continue;

        players.computeIfAbsent(
                stats.getPlayerUUID(),
                g0 -> new PlayerFullLeaderboardPositionsModel(
                    stats.getPlayerUUID(),
                    new HashMap<>(),
                    PlayerStatsModel.from(stats),
                    PlayerPropertiesModel.from(properties)
                ))
            .getLeaderboardPositions().put(set.getId(), i);
      }
    }

    ctx.json(players.values());
  }


  private static UUID validUUID(Context ctx) {
    final String raw = ctx.pathParamAsClass("uuid", String.class)
        .check(s -> !s.isEmpty(), "restId cannot be empty")
        .get();

    try {
      return UUID.fromString(raw);
    } catch (IllegalArgumentException e) {
      throw new BadRequestResponse("uuid has an invalid format");
    }
  }
}
