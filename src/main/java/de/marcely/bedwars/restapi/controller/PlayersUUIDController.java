package de.marcely.bedwars.restapi.controller;

import de.marcely.bedwars.api.player.PlayerDataAPI;
import de.marcely.bedwars.restapi.model.ErrorResponse;
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
import java.util.Optional;
import java.util.UUID;

public class PlayersUUIDController {

  @OpenApi(
      summary = "Get a player's uuid by their name",
      description = "Only works for players that joined the server at least once. "
          + " Also works for players that aren't online. "
          + " The name look-up is case-insensitive.",
      operationId = "getOnePlayerUUIDByName",
      tags = "Players",
      pathParams = {
          @OpenApiParam(name = "name", type = String.class, description = "The username of the player", example = "Notch")
      },
      responses = {
          @OpenApiResponse(status = "200", content = @OpenApiContent(from = UUID.class), description = "Might not represent the actual Mojang UUID if the server is in offline-mode"),
          @OpenApiResponse(status = "400", content = {@OpenApiContent(from = ErrorResponse.class)}),
          @OpenApiResponse(status = "404", content = {@OpenApiContent(from = ErrorResponse.class)})
      },
      path = "/players/uuid/{name}",
      methods = {HttpMethod.GET}
  )
  public static void getOneByName(Context ctx) {
    final String name = ctx.pathParamAsClass("name", String.class)
        .check(s -> s.length() >= 3, "name must be longer than 3 characters)")
        .check(s -> s.length() <= 16, "name must be shorter than 16 characters")
        .check(s -> s.chars().allMatch(c -> c >= 33 && c <= 126), "name contains invalid characters")
        .get();
    final Optional<UUID> uuid = Util.getAwait(c -> PlayerDataAPI.get().getUUIDByName(name, c));

    if (uuid.isEmpty())
      throw new NotFoundResponse("no player with that name found");

    ctx.json(uuid.get());
  }


  @OpenApi(
      summary = "Get the uuid of multiple players by their name",
      description = "Only works for players that joined the server at least once. "
          + " Also works for players that aren't online."
          + " The name look-up is case-insensitive.",
      operationId = "getManyPlayerUUIDByName",
      tags = "Players",
      requestBody = @OpenApiRequestBody(
          content = @OpenApiContent(from = String[].class, example = "[ \"Notch\" ] "),
          description = "An array of player names",
          required = true
      ),
      responses = {
          // object key: name value: uuid (null if not found)
          @OpenApiResponse(status = "200", content = @OpenApiContent(from = PlayerUUIDModel[].class)),
          @OpenApiResponse(status = "400", content = {@OpenApiContent(from = ErrorResponse.class)})
      },
      path = "/players/uuid",
      methods = {HttpMethod.GET}
  )
  public static void getManyByName(Context ctx) {
    final String[] names = ctx.bodyAsClass(String[].class);

    if (names.length > 10)
      throw new BadRequestResponse("Cannot look up more than 10 names at once");

    // validate names
    for (String name : names) {
      if (name.length() < 3)
        throw new BadRequestResponse("name '" + name + "' must be longer than 3 characters");
      if (name.length() > 16)
        throw new BadRequestResponse("name '" + name + "' must be shorter than 16 characters");
      if (!name.chars().allMatch(c -> c >= 33 && c <= 126))
        throw new BadRequestResponse("name '" + name + "' contains invalid characters");
    }

    // fetch them
    final PlayerUUIDModel[] models = Arrays.stream(names)
        .map(name -> {
          final Optional<UUID> uuid = Util.getAwait(c -> PlayerDataAPI.get().getUUIDByName(name, c));

          return new PlayerUUIDModel(name, uuid.orElse(null));
        })
        .toArray(PlayerUUIDModel[]::new);

    ctx.json(models);
  }
}
