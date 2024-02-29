package de.marcely.bedwars.restapi.model.player;

import de.marcely.bedwars.api.player.PlayerStatSet;
import io.javalin.openapi.JsonSchema;
import io.javalin.openapi.OpenApiDescription;
import io.javalin.openapi.OpenApiExample;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@AllArgsConstructor
@JsonSchema
public class PlayerStatSetModel {

  @Getter(onMethod = @__({
      @OpenApiExample("bedwars:play_time"),
      @OpenApiDescription("The id of the stat set.")
  }))
  private String id;

  @Getter(onMethod = @__({
      @OpenApiExample("Play Time"),
      @OpenApiDescription("The name of the arena as in the way it would be shown to the player.")
  }))
  private String name;

  @Getter(onMethod = @__({
      @OpenApiExample("MBedwars"),
      @OpenApiDescription("The name of the plugin that created the stat set.")
  }))
  private String creatorPluginName;


  public static PlayerStatSetModel from(PlayerStatSet input) {
    return new PlayerStatSetModel(
        input.getId(),
        input.getName(null),
        input.getPlugin().getName()
    );
  }
}
