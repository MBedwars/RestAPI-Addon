package de.marcely.bedwars.restapi.model.player;

import de.marcely.bedwars.api.player.PlayerProperties;
import io.javalin.openapi.JsonSchema;
import io.javalin.openapi.OpenApiDescription;
import io.javalin.openapi.OpenApiExample;
import io.javalin.openapi.OpenApiExampleProperty;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@AllArgsConstructor
@JsonSchema
public class PlayerPropertiesModel {

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
                          name = "bedwars:username",
                          value = "Notch"),
                      @OpenApiExampleProperty(
                          name = "bedwars:last_login",
                          value = "1709215374501"),
                      @OpenApiExampleProperty(
                          name = "bedwars:rejoin_arena_name",
                          value = "MushroomTestSolo"),
                      @OpenApiExampleProperty(
                          name = "bedwars:rejoin_server_name",
                          value = "Hub_1"),
                  }),
              @OpenApiDescription("All the properties of the player. The key is the property name and the value is the property value.")
          }))
  private Map<String, String> entries;


  public static PlayerPropertiesModel from(PlayerProperties input) {
    final Map<String, String> entries = new HashMap<>();

    for (String key : input.getStoredKeys())
      entries.put(key, input.get(key).orElseThrow());

    return new PlayerPropertiesModel(input.getPlayerUUID(), entries);
  }
}