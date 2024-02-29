package de.marcely.bedwars.restapi.controller;

import de.marcely.bedwars.api.remote.RemoteAPI;
import de.marcely.bedwars.api.remote.RemoteArena;
import de.marcely.bedwars.restapi.model.ErrorResponse;
import de.marcely.bedwars.restapi.model.misc.ArenaModel;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiResponse;

public class ArenasController {

  @OpenApi(
      summary = "Get all arenas",
      operationId = "getAllArenas",
      tags = "Arenas",
      responses = {
          @OpenApiResponse(status = "200", content = @OpenApiContent(from = ArenaModel[].class))
      },
      path = "/arenas",
      methods = {HttpMethod.GET}
  )
  public static void getAll(Context ctx) {
    ctx.json(ArenaModel.from(RemoteAPI.get().getArenas()));
  }



  @OpenApi(
      summary = "Get one arena",
      operationId = "getOneArena",
      tags = {"Arenas"},
      pathParams = {
          @OpenApiParam(
              name = "restId", type = String.class,
              description = "The rest id of the arena found within its model. Note that it may change if e.g. the arena gets renamed!")
      },
      responses = {
          @OpenApiResponse(status = "200", content = @OpenApiContent(from = ArenaModel.class)),
          @OpenApiResponse(status = "400", content = {@OpenApiContent(from = ErrorResponse.class)}),
          @OpenApiResponse(status = "404", content = {@OpenApiContent(from = ErrorResponse.class)})
      },
      path = "/arenas/{restId}",
      methods = {HttpMethod.GET}
  )
  public static void getOne(Context ctx) {
    final String restId = validRestId(ctx);

    try {
      final RemoteArena arena = ArenaModel.fromRestId(restId);

      if (arena == null)
        throw new NotFoundResponse("No arena under the given restId found");

      ctx.json(ArenaModel.from(arena));
    } catch (IllegalArgumentException e) {
      throw new BadRequestResponse("restId has an invalid format");
    }
  }


  private static String validRestId(Context ctx) {
    return ctx.pathParamAsClass("restId", String.class)
        .check(s -> !s.isEmpty(), "restId cannot be empty")
        .get();
  }
}
