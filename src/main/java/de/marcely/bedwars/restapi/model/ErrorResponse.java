package de.marcely.bedwars.restapi.model;

import io.javalin.openapi.JsonSchema;
import io.javalin.openapi.OpenApiDescription;
import io.javalin.openapi.OpenApiExample;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@JsonSchema
public class ErrorResponse {

  @Getter(
      onMethod = @__(
          {
              @OpenApiDescription("The reason for why it failed. Only present if status isn't 200"),
              @OpenApiExample("No arena with the given rest id found")
          }))
  private final String message;
}
