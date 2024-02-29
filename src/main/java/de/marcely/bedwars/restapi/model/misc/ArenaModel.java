package de.marcely.bedwars.restapi.model.misc;

import de.marcely.bedwars.api.arena.ArenaStatus;
import de.marcely.bedwars.api.arena.Team;
import de.marcely.bedwars.api.remote.RemoteAPI;
import de.marcely.bedwars.api.remote.RemoteArena;
import de.marcely.bedwars.api.remote.RemoteServer;
import io.javalin.openapi.JsonSchema;
import io.javalin.openapi.OpenApiDescription;
import io.javalin.openapi.OpenApiExample;
import io.javalin.openapi.OpenApiExampleProperty;
import io.javalin.openapi.OpenApiNullable;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

@Data
@AllArgsConstructor
@JsonSchema
public class ArenaModel {

  @Getter(
      onMethod = @__(
          {
              @OpenApiExample("SHViXzE6QE11c2hyb29tU29sbyMy"),
              @OpenApiDescription("The rest id of the arena found within its model. Note that it may change if e.g. the arena gets renamed!")
          }))
  private String restId;

  @Getter(
      onMethod = @__(
          {
              @OpenApiExample("MushroomTestSolo"),
              @OpenApiDescription("The name of the arena as it is stored on its owning server")
          }))
  private String realName;

  @Getter(
      onMethod = @__(
          {
              @OpenApiExample("§aMushroom"),
              @OpenApiDescription("The name of the arena as it is being shown to players")
          }))
  private String displayName;

  @Getter(
      onMethod = @__(
          {
              @OpenApiExample("@Mushroom#2"),
              @OpenApiDescription("The name of the arena as how the server on which the REST API is located on identifies the arena")
          }))
  private String localName;

  @Getter(
      onMethod = @__(
          {
              @OpenApiExample(
                  objects = {
                      @OpenApiExampleProperty("Notch"),
                      @OpenApiExampleProperty("md5")
                  }),
              @OpenApiDescription("A list of authors of the arena")
          }))
  private List<String> authors;

  @Getter(
      onMethod = @__(
          {
              @OpenApiExample("Notch, md5"),
              @OpenApiDescription("The list of authors in a readable matter")
          }))
  private String displayedAuthors;

  @Getter(
      onMethod = @__(
          {
              @OpenApiDescription("Whether the arena is being owned by the server on which the REST API is located on")
          }))
  private boolean isLocal;

  @Getter(
      onMethod = @__(
          {
              @OpenApiExample("Hub_1"),
              @OpenApiDescription("The proxy channel name of the server on which the arena is located on")
          }))
  private String serverChannelName;

  @Getter(
      onMethod = @__(
          {
              @OpenApiExample("4"),
              @OpenApiDescription("The max amount of players each team can hold")
          }))
  private int playersPerTeam;

  @Getter(
      onMethod = @__(
          {
              @OpenApiExample("2"),
              @OpenApiDescription("The minimum amount of players required to start the game")
          }))
  private int minPlayers;

  @Getter(
      onMethod = @__(
          {
              @OpenApiExample("8"),
              @OpenApiDescription("The max amount of players the game can hold at once")
          }))
  private int maxPlayers;

  @Getter(
      onMethod = @__(
          {
              @OpenApiDescription("The icon of the arena that is being displayed in i.a. GUIs")
          }))
  private ItemStackModel icon;

  @Nullable
  @Getter(
      onMethod = @__(
          {
              @OpenApiNullable,
              @OpenApiExample("world"),
              @OpenApiDescription("The world in which the match is being held")
          }))
  private String gameWorld;

  @Getter(onMethod = @__(@OpenApiExample("LOBBY")))
  private ArenaStatus status;

  @Getter(
      onMethod = @__(
          {
              @OpenApiDescription("All players that are currently in the match. Doesn't include those that have already left")
          }))
  private Collection<OnlinePlayerModel> ingamePlayers;

  @Getter(
      onMethod = @__(
          {
              @OpenApiDescription("All teams that are enabled for the arena. This doesn't mean that they are currently being used in the match!")
          }))
  private Collection<Team> enabledTeams;


  public static ArenaModel from(RemoteArena arena) {
    return new ArenaModel(
        toRestId(arena),
        arena.getRealName(),
        arena.getDisplayName(),
        arena.getName(),
        Arrays.asList(arena.getAuthors()),
        arena.getDisplayedAuthors(),
        arena.isLocal(),
        arena.getRemoteServer().getBungeeChannelName(),
        arena.getPlayersPerTeam(),
        arena.getMinPlayers(),
        arena.getMaxPlayers(),
        ItemStackModel.from(arena.getIcon()),
        arena.getGameWorldName(),
        arena.getStatus(),
        OnlinePlayerModel.from(arena.getRemotePlayers()),
        arena.getEnabledTeams()
    );
  }

  public static Collection<ArenaModel> from(Collection<? extends RemoteArena> arenas) {
    return arenas.stream()
        .map(ArenaModel::from)
        .collect(Collectors.toList());
  }

  public static String toRestId(RemoteArena arena) {
    return Base64.getEncoder().encodeToString(String.format("%s:%s",
        arena.getRemoteServer().getBungeeChannelName(),
        arena.getRealName()).getBytes()
    );
  }

  @Nullable
  public static RemoteArena fromRestId(String restId) {
    final String[] parts = new String(Base64.getDecoder().decode(restId)).split(":");

    if (parts.length != 2)
      throw new IllegalArgumentException("Invalid restId");

    final Optional<? extends RemoteServer> server = RemoteAPI.get().getServers().stream()
        .filter(s -> s.getBungeeChannelName().equals(parts[0]))
        .findAny();

    if (server.isEmpty())
      return null;

    return server.get().getArenaByExactRealName(parts[1]);
  }
}
