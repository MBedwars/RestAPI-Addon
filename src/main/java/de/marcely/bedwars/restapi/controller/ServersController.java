package de.marcely.bedwars.restapi.controller;

import de.marcely.bedwars.api.remote.RemoteAPI;
import de.marcely.bedwars.api.remote.RemoteServer;
import de.marcely.bedwars.restapi.model.ErrorResponse;
import de.marcely.bedwars.restapi.model.misc.ServerModel;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiResponse;
import java.util.Optional;

public class ServersController {

  @OpenApi(
      summary = "Get all online servers",
      operationId = "getAllServers",
      tags = "Servers",
      responses = {
          @OpenApiResponse(status = "200", content = @OpenApiContent(from = ServerModel[].class))
      },
      path = "/servers",
      methods = {HttpMethod.GET}
  )
  public static void getAll(Context ctx) {
    ctx.json(ServerModel.from(RemoteAPI.get().getServers()));
  }


  @OpenApi(
      summary = "Get a specific online server",
      operationId = "getOneServer",
      tags = "Servers",
      responses = {
          @OpenApiResponse(status = "200", content = @OpenApiContent(from = ServerModel.class)),
          @OpenApiResponse(status = "400", content = {@OpenApiContent(from = ErrorResponse.class)}),
          @OpenApiResponse(status = "404", content = {@OpenApiContent(from = ErrorResponse.class)})
      },
      path = "/servers/{channelName}",
      methods = {HttpMethod.GET}
  )
  public static void getOne(Context ctx) {
    final String channel = validChannelName(ctx);
    final Optional<? extends RemoteServer> server = RemoteAPI.get().getServers().stream()
        .filter(s -> s.getBungeeChannelName().equals(channel))
        .findAny();

    if (server.isEmpty())
      throw new NotFoundResponse("No server under the given channelName found");

    ctx.json(ServerModel.from(server.get()));
  }


  private static String validChannelName(Context ctx) {
    return ctx.pathParamAsClass("channelName", String.class)
        .check(s -> !s.isEmpty(), "channelName cannot be empty")
        .get();
  }
}
