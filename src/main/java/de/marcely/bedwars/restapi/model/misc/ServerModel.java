package de.marcely.bedwars.restapi.model.misc;

import de.marcely.bedwars.api.remote.RemoteAPI;
import de.marcely.bedwars.api.remote.RemotePlayer;
import de.marcely.bedwars.api.remote.RemoteServer;
import io.javalin.openapi.JsonSchema;
import io.javalin.openapi.OpenApiDescription;
import io.javalin.openapi.OpenApiExample;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@AllArgsConstructor
@JsonSchema
public class ServerModel {

  @Getter(
      onMethod = @__(
          {
              @OpenApiExample("Hub_1"),
              @OpenApiDescription("The name of the server as to have it is configured within the proxy configs. May be LOCAL if proxies aren't used.")
          }))
  private String channelName;

  @Getter(
      onMethod = @__(
          {
              @OpenApiExample("5.3.3"),
              @OpenApiDescription("The version of the MBedwars plugin running on the server.")
          }))
  private String pluginVersion;

  @Getter(
      onMethod = @__(
          {
              @OpenApiExample("25"),
              @OpenApiDescription("The version of the MBedwars API the server is using.")
          }))
  private int apiVersion;

  @Getter(
      onMethod = @__(
          {
              @OpenApiExample("true"),
              @OpenApiDescription("Whether the server is a hub server or not (configured within ProxySync addon).")
          }))
  private boolean isHub;

  @Getter(
      onMethod = @__(
          {
              @OpenApiExample("true"),
              @OpenApiDescription("Whether the server is a local server (equally to the server on which the REST API is running on) or not.")
          }))
  private boolean isLocal;

  @Getter(
      onMethod = @__(
          {
              @OpenApiDescription("The rest ids of the arenas created on the server.")
          }))
  private Collection<String> arenas;

  @Getter(
      onMethod = @__(
          {
              @OpenApiDescription("The UUIDs of the online players on the server.")
          }))
  private Collection<UUID> players;


  public static ServerModel from(RemoteServer input) {
    return new ServerModel(
        input.getBungeeChannelName(),
        input.getPluginVersion(),
        input.getAPIVersion(),
        input.isHub(),
        input.isLocal(),
        input.getArenas().stream()
            .map(ArenaModel::toRestId)
            .collect(Collectors.toList()),
        RemoteAPI.get().getOnlinePlayers().stream()
            .filter(p -> p.getServer().equals(input))
            .map(RemotePlayer::getUniqueId)
            .collect(Collectors.toList())
    );
  }

  public static Collection<ServerModel> from(Collection<? extends RemoteServer> input) {
    return input.stream()
        .map(ServerModel::from)
        .collect(Collectors.toList());
  }
}