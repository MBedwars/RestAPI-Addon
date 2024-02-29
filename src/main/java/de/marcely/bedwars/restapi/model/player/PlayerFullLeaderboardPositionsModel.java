package de.marcely.bedwars.restapi.model.player;

import io.javalin.openapi.JsonSchema;
import io.javalin.openapi.OpenApiDescription;
import java.util.Map;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@JsonSchema
public class PlayerFullLeaderboardPositionsModel extends PlayerLeaderboardPositionsModel {

  @Getter(onMethod = @__(@OpenApiDescription("All the stats the player owns.")))
  private PlayerStatsModel stats;

  @Getter(onMethod = @__(@OpenApiDescription("All the properties the player owns.")))
  private PlayerPropertiesModel properties;

  public PlayerFullLeaderboardPositionsModel(
      UUID playerUUID,
      Map<String, Integer> leaderboardPositions,
      PlayerStatsModel stats,
      PlayerPropertiesModel properties) {
    super(playerUUID, leaderboardPositions);

    this.stats = stats;
    this.properties = properties;
  }

  public PlayerFullLeaderboardPositionsModel() {
    super(null, null);
  }
}
