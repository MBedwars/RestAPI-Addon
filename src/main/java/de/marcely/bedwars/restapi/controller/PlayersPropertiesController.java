package de.marcely.bedwars.restapi.controller;

import de.marcely.bedwars.api.player.PlayerDataAPI;
import de.marcely.bedwars.api.player.PlayerProperties;
import de.marcely.bedwars.restapi.model.ErrorResponse;
import de.marcely.bedwars.restapi.model.player.PlayerPropertiesModel;
import de.marcely.bedwars.restapi.util.Util;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiRequestBody;
import io.javalin.openapi.OpenApiResponse;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class PlayersPropertiesController {

  @OpenApi(
      summary = "Get the properties a player owns",
      operationId = "getOnePlayersProperties",
      tags = "Player Properties",
      pathParams = {
          @OpenApiParam(name = "uuid", type = String.class, description = "The UUID of the player")
      },
      responses = {
          @OpenApiResponse(status = "200", content = @OpenApiContent(from = PlayerPropertiesModel.class)),
          @OpenApiResponse(status = "400", content = {@OpenApiContent(from = ErrorResponse.class)})
      },
      path = "/players/properties/{uuid}",
      methods = {HttpMethod.GET}
  )
  public static void getOne(Context ctx) {
    final UUID uuid = validUUID(ctx);
    final PlayerProperties properties = Util.getAwait(c -> PlayerDataAPI.get().getProperties(uuid, c));

    ctx.json(PlayerPropertiesModel.from(properties));
  }



  @OpenApi(
      summary = "Update the properties a player owns",
      operationId = "updateOnePlayersProperties",
      tags = "Player Properties",
      pathParams = {
          @OpenApiParam(name = "uuid", type = String.class, description = "The UUID of the player")
      },
      queryParams = {
          @OpenApiParam(name = "replaceAll", type = Boolean.class,
              description = "Whether to replace all properties with the given ones or to just update the ones that are given. Default is false.")
      },
      requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = PlayerPropertiesModel.class)),
      responses = {
          @OpenApiResponse(status = "200", content = @OpenApiContent(from = PlayerPropertiesModel.class)),
          @OpenApiResponse(status = "400", content = {@OpenApiContent(from = ErrorResponse.class)})
      },
      path = "/players/properties/{uuid}",
      methods = {HttpMethod.PUT}
  )
  public static void update(Context ctx) {
    // parse context
    final UUID uuid = validUUID(ctx);
    final boolean replaceAll = ctx.queryParamAsClass("replaceAll", Boolean.class)
        .getOrDefault(false);
    final PlayerPropertiesModel replacement = ctx.bodyAsClass(PlayerPropertiesModel.class);
    final PlayerProperties properties = Util.getAwait(c -> PlayerDataAPI.get().getProperties(uuid, c));
    Collection<String> remove;

    // find which ones to remove
    if (!replaceAll)
      remove = Collections.emptyList();
    else {
      remove = properties.getStoredKeys().stream()
          .filter(id -> !replacement.getEntries().keySet().contains(id))
          .collect(Collectors.toList());
    }

    // update
    for (Map.Entry<String, String> e : replacement.getEntries().entrySet())
      properties.set(e.getKey(), e.getValue());

    for (String key : remove)
      properties.remove(key);

    properties.save();
    ctx.json(PlayerPropertiesModel.from(properties));
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
