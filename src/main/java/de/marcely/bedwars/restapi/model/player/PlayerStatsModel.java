package de.marcely.bedwars.restapi.model.player;

import de.marcely.bedwars.api.player.PlayerDataAPI;
import de.marcely.bedwars.api.player.PlayerStatSet;
import de.marcely.bedwars.api.player.PlayerStats;
import io.javalin.openapi.JsonSchema;
import io.javalin.openapi.OpenApiDescription;
import io.javalin.openapi.OpenApiExample;
import io.javalin.openapi.OpenApiExampleProperty;
import io.javalin.openapi.OpenApiNullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@AllArgsConstructor
@JsonSchema
public class PlayerStatsModel {

  @Getter(
      onMethod = @__(
          {
              @OpenApiExample("ffe3e36e-d406-4d7a-aeba-0eb6e02a0aba"),
              @OpenApiDescription("The UUID of the player that holds the stats.")
          }))
  private UUID playerUUID;

  @Getter(
      onMethod = @__(
          {
              @OpenApiExample(
                  objects = {
                      @OpenApiExampleProperty(
                          name = "bedwars:play_time",
                          value = "4514941"),
                      @OpenApiExampleProperty(
                          name = "bedwars:win_streak",
                          value = "2"),
                      @OpenApiExampleProperty(
                          name = "bedwars:deaths",
                          value = "15")
                  }),
              @OpenApiDescription("The internally stored information of the stats.")
          }))
  private Map<String, Number> internalMap;

  @Getter(onMethod = @__(@OpenApiDescription("The stats of the player.")))
  private Collection<StatsSetValuePair> statSetValues;

  public static PlayerStatsModel from(PlayerStats input) {
    final Collection<PlayerStatSet> statSets = PlayerDataAPI.get().getRegisteredStatSets();
    final Collection<StatsSetValuePair> statSetValues = new ArrayList<>(statSets.size());

    for (PlayerStatSet set : statSets) {
      Number value = null;

      try {
        value = set.getValue(input);
      } catch (IllegalStateException e) {
      }

      statSetValues.add(new StatsSetValuePair(
          PlayerStatSetModel.from(set),
          value,
          set.getDisplayedValue(input)
      ));
    }

    return new PlayerStatsModel(
        input.getPlayerUUID(),
        Map.ofEntries(input.entrySet().toArray(new Map.Entry[0])),
        statSetValues
    );
  }

  @AllArgsConstructor
  @JsonSchema
  public static class StatsSetValuePair {

    @Getter(onMethod = @__(@OpenApiDescription("The stat set that this entry is for.")))
    private PlayerStatSetModel statSet;

    @Getter(
        onMethod = @__(
            {
                @OpenApiNullable,
                @OpenApiExample("4514941"),
                @OpenApiDescription("The value of the stats entry in the way it is stored internally.")
            }))
    private Number numericalValue;

    @Getter(
        onMethod = @__(
            {
                @OpenApiExample("1h 15m 14s"),
                @OpenApiDescription("The value of the stats entry in the way it is being displayed to the player.")
            }))
    private String stringValue;
  }
}
