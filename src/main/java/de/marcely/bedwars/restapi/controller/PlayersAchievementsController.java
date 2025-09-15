package de.marcely.bedwars.restapi.controller;

import de.marcely.bedwars.api.player.PlayerAchievement;
import de.marcely.bedwars.api.player.PlayerAchievements;
import de.marcely.bedwars.api.player.PlayerDataAPI;
import de.marcely.bedwars.restapi.model.ErrorResponse;
import de.marcely.bedwars.restapi.model.player.PlayerAchievementModel;
import de.marcely.bedwars.restapi.model.player.PlayerAchievementsModel;
import de.marcely.bedwars.restapi.model.player.PlayerAchievementsModel.Earning;
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
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Collectors;

public class PlayersAchievementsController {

  @OpenApi(
      summary = "Get the achievements a player has earned",
      operationId = "getOnePlayersAchievements",
      tags = "Player Achievements",
      pathParams = {
          @OpenApiParam(name = "uuid", type = String.class, description = "The UUID of the player")
      },
      responses = {
          @OpenApiResponse(status = "200", content = @OpenApiContent(from = PlayerAchievementsModel.class)),
          @OpenApiResponse(status = "400", content = {@OpenApiContent(from = ErrorResponse.class)})
      },
      path = "/players/achievements/{uuid}",
      methods = {HttpMethod.GET}
  )
  public static void getOne(Context ctx) {
    final UUID uuid = validUUID(ctx);
    final PlayerAchievements achievements = Util.getAwait(c -> PlayerDataAPI.get().getAchievements(uuid, c));

    ctx.json(PlayerAchievementsModel.from(achievements));
  }



  @OpenApi(
      summary = "Update the achievements a player has earned",
      operationId = "updateOnePlayersAchievements",
      tags = "Player Achievements",
      pathParams = {
          @OpenApiParam(name = "uuid", type = String.class, description = "The UUID of the player")
      },
      queryParams = {
          @OpenApiParam(name = "replaceAll", type = Boolean.class,
              description = "Whether to replace all achievements with the given ones or to just update the ones that are given. Default is false."),
          @OpenApiParam(name = "notify", type = Boolean.class,
              description = "Whether the player shall be notified. Default is true.")
      },
      requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = PlayerAchievementsModel.class)),
      responses = {
          @OpenApiResponse(status = "200", content = @OpenApiContent(from = PlayerAchievementsModel.class)),
          @OpenApiResponse(status = "400", content = {@OpenApiContent(from = ErrorResponse.class)}),
          @OpenApiResponse(status = "404", content = {@OpenApiContent(from = ErrorResponse.class)}),
      },
      path = "/players/achievements/{uuid}",
      methods = {HttpMethod.PUT}
  )
  public static void update(Context ctx) {
    // parse context
    final UUID uuid = validUUID(ctx);
    final boolean replaceAll = ctx.queryParamAsClass("replaceAll", Boolean.class)
        .getOrDefault(false);
    final boolean notify = ctx.queryParamAsClass("notify", Boolean.class)
        .getOrDefault(true);
    final PlayerAchievementsModel replacement = ctx.bodyAsClass(PlayerAchievementsModel.class);
    final PlayerAchievements achievements = Util.getAwait(c -> PlayerDataAPI.get().getAchievements(uuid, c));
    Collection<String> remove;

    // find which ones to remove
    if (!replaceAll)
      remove = Collections.emptyList();
    else {
      remove = achievements.getEarnedIds().stream()
          .filter(id -> replacement.getEarnings().stream().noneMatch(e -> e.getAchievementId().equals(id)))
          .collect(Collectors.toList());
    }

    // find which ones to update
    for (Earning earning : replacement.getEarnings()) {
      final PlayerAchievement achievement = PlayerDataAPI.get().getAchievementTypeById(earning.getAchievementId());

      if (achievement == null)
        throw new NotFoundResponse("No achievement with id exists: " + earning.getAchievementId());
    }

    // update
    for (Earning earning : replacement.getEarnings()) {
      final PlayerAchievement achievement = PlayerDataAPI.get().getAchievementTypeById(earning.getAchievementId());

      achievements.earn(achievement, !notify);
    }

    for (String id : remove) {
      final PlayerAchievement achievement = PlayerDataAPI.get().getAchievementTypeById(id);

      if (achievement == null)
        return;

      achievements.remove(achievement);
    }

    achievements.save();
    ctx.json(PlayerAchievementsModel.from(achievements));
  }



  @OpenApi(
      summary = "Get all existing achievement types",
      operationId = "getAllPlayersAchievementTypes",
      tags = "Player Achievements",
      responses = {
          @OpenApiResponse(status = "200", content = @OpenApiContent(from = PlayerAchievementModel[].class))
      },
      path = "/players/achievement-types",
      methods = {HttpMethod.GET}
  )
  public static void getAllTypes(Context ctx) {
    ctx.json(PlayerAchievementModel.from(PlayerDataAPI.get().getRegisteredAchievementTypes()));
  }


  @OpenApi(
      summary = "Get one existing achievement type by its id",
      operationId = "getAllPlayersAchievementTypes",
      tags = "Player Achievements",
      responses = {
          @OpenApiResponse(status = "200", content = @OpenApiContent(from = PlayerAchievementModel.class)),
          @OpenApiResponse(status = "400", content = {@OpenApiContent(from = ErrorResponse.class)}),
          @OpenApiResponse(status = "404", content = {@OpenApiContent(from = ErrorResponse.class)})
      },
      path = "/players/achievement-types/{id}",
      methods = {HttpMethod.GET}
  )
  public static void getOneType(Context ctx) {
    final String id = ctx.pathParamAsClass("id", String.class)
        .check(s -> !s.isEmpty(), "id cannot be empty")
        .get();
    final PlayerAchievement achievement = PlayerDataAPI.get().getAchievementTypeById(id);

    if (achievement == null)
      throw new NotFoundResponse("No achievement under the given id found");

    ctx.json(PlayerAchievementModel.from(achievement));
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
