package de.marcely.bedwars.restapi.model.misc;

import de.marcely.bedwars.api.remote.RemotePlayer;
import io.javalin.openapi.JsonSchema;
import io.javalin.openapi.OpenApiDescription;
import io.javalin.openapi.OpenApiExample;
import io.javalin.openapi.OpenApiNullable;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

@Data
@AllArgsConstructor
@JsonSchema
public class OnlinePlayerModel {

  @Getter(onMethod = @__({
      @OpenApiExample("ffe3e36e-d406-4d7a-aeba-0eb6e02a0aba"),
      @OpenApiDescription("The UUID of the player.")
  }))
  private UUID uuid;

  @Getter(onMethod = @__({
      @OpenApiExample("Notch"),
      @OpenApiDescription("The real name of the player.")
  }))
  private String name;

  @Getter(onMethod = @__({
      @OpenApiExample("Hub_1"),
      @OpenApiDescription("The name of the server on which the player is on as to have it is configured within the proxy configs. May be LOCAL if proxies aren't used.")
  }))
  private String serverChannelName;

  @Getter(onMethod = @__({
      @OpenApiExample("true"),
      @OpenApiDescription("Whether the player is playing a game or not.")
  }))
  private boolean isPlaying;

  @Getter(onMethod = @__({
      @OpenApiExample("false"),
      @OpenApiDescription("Whether the player is spectating a game or not.")
  }))
  private boolean isSpectating;

  @Nullable
  @Getter(
      onMethod = @__(
          {
              @OpenApiNullable,
              @OpenApiExample("SHViXzE6QE11c2hyb29tU29sbyMy"),
              @OpenApiDescription("The rest id of the arena in which the player is on. Note that it may change if e.g. the arena gets renamed!")
          }))
  private String arenaRestId;


  public static OnlinePlayerModel from(RemotePlayer player) {
    return new OnlinePlayerModel(
        player.getUniqueId(),
        player.getName(),
        player.getServer().getBungeeChannelName(),
        player.isPlaying(),
        player.isSpectating(),
        player.getArena() != null ? ArenaModel.toRestId(player.getArena()) : null
    );
  }

  public static Collection<OnlinePlayerModel> from(Collection<? extends RemotePlayer> players) {
    return players.stream()
        .map(OnlinePlayerModel::from)
        .collect(Collectors.toList());
  }
}