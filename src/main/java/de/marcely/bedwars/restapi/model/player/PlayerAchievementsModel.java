package de.marcely.bedwars.restapi.model.player;

import de.marcely.bedwars.api.player.PlayerAchievement;
import de.marcely.bedwars.api.player.PlayerAchievements;
import de.marcely.bedwars.api.player.PlayerDataAPI;
import io.javalin.openapi.JsonSchema;
import io.javalin.openapi.OpenApiDescription;
import io.javalin.openapi.OpenApiExample;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@AllArgsConstructor
@JsonSchema
public class PlayerAchievementsModel {

  @Getter(
      onMethod = @__(
          {
              @OpenApiExample("ffe3e36e-d406-4d7a-aeba-0eb6e02a0aba"),
              @OpenApiDescription("The UUID of the player that holds the achievements.")
          }))
  private UUID playerUUID;

  @Getter(
      onMethod = @__(
          {
              @OpenApiDescription("All the achievements the player has earned")
          }))
  private Collection<Earning> earnings;


  public static PlayerAchievementsModel from(PlayerAchievements input) {
    final List<Earning> earnings = new ArrayList<>(input.getEarnedIds().size());

    for (String id : input.getEarnedIds()) {
      final PlayerAchievement achievement = PlayerDataAPI.get().getAchievementTypeById(id);

      if (achievement == null)
        continue;

      earnings.add(new Earning(
          PlayerAchievementModel.from(achievement),
          DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(input.getEarnDate(achievement))
      ));
    }

    return new PlayerAchievementsModel(
        input.getPlayerUUID(),
        earnings
    );
  }


  @AllArgsConstructor
  @JsonSchema
  public static class Earning {

    @Getter(onMethod = @__(@OpenApiDescription("Info about the earned achievement")))
    private PlayerAchievementModel achievement;

    @Getter(
        onMethod = @__(
            {
                @OpenApiExample("2011-12-03T10:15:30+01:00"),
                @OpenApiDescription("The date and time when the achievement was earned in ISO 8601 format")
            }))
    private String dateTime;
  }

}
