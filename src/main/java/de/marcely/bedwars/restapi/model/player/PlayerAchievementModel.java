package de.marcely.bedwars.restapi.model.player;

import de.marcely.bedwars.api.player.PlayerAchievement;
import io.javalin.openapi.JsonSchema;
import io.javalin.openapi.OpenApiDescription;
import io.javalin.openapi.OpenApiExample;
import java.util.Collection;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@AllArgsConstructor
@JsonSchema
public class PlayerAchievementModel {

  @Getter(
      onMethod = @__(
          {
              @OpenApiExample("bedwars:own_bed_destroyed"),
              @OpenApiDescription("The id of the achievement.")
          }))
  private String id;

  @Getter(
      onMethod = @__(
          {
              @OpenApiExample("Traitor!"),
              @OpenApiDescription("The name of the achievement as in the way it would be shown to the player.")
          }))
  private String name;

  @Getter(
      onMethod = @__(
          {
              @OpenApiExample("Destroy your own bed"),
              @OpenApiDescription("The description of the achievement as in the way it would be shown to the player.")
          }))
  private String description;

  @Getter(
      onMethod = @__(
          {
              @OpenApiExample("MBedwars"),
              @OpenApiDescription("The name of the plugin that created the achievement.")
          }))
  private String creatorPluginName;


  public static PlayerAchievementModel from(PlayerAchievement input) {
    return new PlayerAchievementModel(
        input.getId(),
        input.getName(),
        input.getDescription(),
        input.getPlugin().getName());
  }

  public static Collection<PlayerAchievementModel> from(Collection<PlayerAchievement> input) {
    return input.stream()
        .map(PlayerAchievementModel::from)
        .collect(Collectors.toList());
  }
}
