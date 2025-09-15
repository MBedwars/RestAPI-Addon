package de.marcely.bedwars.restapi.model.player;

import io.javalin.openapi.JsonSchema;
import io.javalin.openapi.OpenApiDescription;
import io.javalin.openapi.OpenApiExample;
import io.javalin.openapi.OpenApiNullable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@AllArgsConstructor
@JsonSchema
public class PlayerUUIDModel {

  @Getter(
      onMethod = @__(
          {
              @OpenApiExample("Notch"),
              @OpenApiDescription("The name that was used to look up the UUID. "
                  + "Might not represent the actual name since the look-up is case-insensitive.")
          }))
  private String lookupName;

  @Getter(
      onMethod = @__(
          {
              @OpenApiExample("ffe3e36e-d406-4d7a-aeba-0eb6e02a0aba"),
              @OpenApiDescription("The uuid of the player with the given name. Might be null if no player with the given name was found."),
              @OpenApiNullable
          }))
  private UUID uuid;
}
