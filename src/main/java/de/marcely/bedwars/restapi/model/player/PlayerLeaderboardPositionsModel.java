package de.marcely.bedwars.restapi.model.player;

import io.javalin.openapi.JsonSchema;
import io.javalin.openapi.OpenApiDescription;
import io.javalin.openapi.OpenApiExample;
import io.javalin.openapi.OpenApiExampleProperty;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@AllArgsConstructor
@JsonSchema
public class PlayerLeaderboardPositionsModel {

  @Getter(
      onMethod = @__(
          {
              @OpenApiExample("ffe3e36e-d406-4d7a-aeba-0eb6e02a0aba"),
              @OpenApiDescription("The UUID of the player that holds the properties.")
          }))
  private UUID playerUUID;

  @Getter(
      onMethod = @__(
          {
              @OpenApiExample(
                  objects = {
                      @OpenApiExampleProperty(
                          name = "bedwars:play_time",
                          value = "261"),
                      @OpenApiExampleProperty(
                          name = "bedwars:win_streak",
                          value = "151"),
                      @OpenApiExampleProperty(
                          name = "bedwars:deaths",
                          value = "15")
                  }),
              @OpenApiDescription("The leaderboard positions of the player.")
          }))
  private Map<String, Integer> leaderboardPositions;
}
