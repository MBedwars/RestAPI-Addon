package de.marcely.bedwars.restapi.controller;

import de.marcely.bedwars.api.player.PlayerDataAPI;
import de.marcely.bedwars.api.remote.RemoteAPI;
import de.marcely.bedwars.api.remote.RemotePlayer;
import de.marcely.bedwars.api.remote.RemoteServer;
import de.marcely.bedwars.restapi.model.ErrorResponse;
import de.marcely.bedwars.restapi.model.misc.OnlinePlayerModel;
import de.marcely.bedwars.restapi.model.player.PlayerUUIDModel;
import de.marcely.bedwars.restapi.util.Util;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiRequestBody;
import io.javalin.openapi.OpenApiResponse;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class PlayersOnlineController {

  @OpenApi(
      summary = "Get all players that are currently online",
      operationId = "getAllPlayers",
      tags = "Online Players",
      queryParams = {
          @OpenApiParam(
              name = "serverChannelNames", type = String[].class, example = "Hub-1,Bedwars-1",
              description = "Whether to only fetch the info from certain servers (, is the seperator). Leave it empty to fetch from all servers."),
          @OpenApiParam(
              name = "playerNames", type = String[].class, example = "Notch,md5",
              description = "Whether to only look up certain player given by their names (, is the seperator). Leave it empty to fetch all players."),
      },
      responses = {
          @OpenApiResponse(status = "200", content = @OpenApiContent(from = OnlinePlayerModel[].class)),
          @OpenApiResponse(status = "400", content = {@OpenApiContent(from = ErrorResponse.class)})
      },
      path = "/players/online",
      methods = {HttpMethod.GET}
  )
  public static void getAll(Context ctx) {
    final String[] serverFilters = Optional.of(ctx.queryParamAsClass("serverChannelName", String.class).getOrDefault(""))
        .map(s -> s.isEmpty() ? new String[0] : s.split(","))
        .get();
    final String[] playerNameFilters = Optional.of(ctx.queryParamAsClass("playerNames", String.class).getOrDefault(""))
        .map(s -> s.isEmpty() ? new String[0] : s.split(","))
        .get();

    // wants all
    if (serverFilters.length == 0 && playerNameFilters.length == 0) {
      ctx.json(OnlinePlayerModel.from(RemoteAPI.get().getOnlinePlayers()));
      return;
    }

    // only certain players
    if (serverFilters.length == 0) {
      final Collection<RemotePlayer> players = Arrays.stream(playerNameFilters)
          .map(RemoteAPI.get()::getOnlinePlayer)
          .filter(Objects::nonNull)
          .collect(Collectors.toList());

      ctx.json(OnlinePlayerModel.from(players));
      return;
    }

    // filter
    final Collection<RemoteServer> servers = Arrays.stream(serverFilters)
        .map(name -> RemoteAPI.get().getServers().stream()
            .filter(s -> name.equals(s.getBungeeChannelName()))
            .findAny().orElse(null))
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
    final Collection<RemotePlayer> players = RemoteAPI.get().getOnlinePlayers().stream()
        .filter(p -> servers.contains(p.getServer()))
        .filter(p -> playerNameFilters.length == 0 || Arrays.stream(playerNameFilters)
            .anyMatch(name -> name.equalsIgnoreCase(p.getName())))
        .collect(Collectors.toList());

    ctx.json(OnlinePlayerModel.from(players));
  }


  @OpenApi(
      summary = "Get a specific player that is currently online",
      operationId = "getOnePlayers",
      tags = "Online Players",
      pathParams = {
          @OpenApiParam(name = "uuid", type = String.class, description = "The UUID of the player")
      },
      responses = {
          @OpenApiResponse(status = "200", content = @OpenApiContent(from = OnlinePlayerModel.class)),
          @OpenApiResponse(status = "400", content = {@OpenApiContent(from = ErrorResponse.class)}),
          @OpenApiResponse(status = "404", content = {@OpenApiContent(from = ErrorResponse.class)})
      },
      path = "/players/online/{uuid}",
      methods = {HttpMethod.GET}
  )
  public static void getOne(Context ctx) {
    final UUID uuid = validUUID(ctx);
    final RemotePlayer player = RemoteAPI.get().getOnlinePlayer(uuid);

    if (player == null)
      throw new NotFoundResponse("Player not online");

    ctx.json(OnlinePlayerModel.from(player));
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
