package de.marcely.bedwars.restapi.controller;

import de.marcely.bedwars.restapi.auth.Permission;
import de.marcely.bedwars.restapi.RestApiPlugin;
import de.marcely.bedwars.restapi.model.misc.HelloModel;
import io.javalin.http.Context;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class HelloController {

  private final RestApiPlugin plugin;

  public void info(Context ctx) {
    ctx.json(new HelloModel(
        "Hello world! The MBedwars REST server is up and running. See the wiki and specification for more information.",
        this.plugin.getDescription().getVersion(),
        "https://mbedwars.com/",
        "https://wiki.mbedwars.com/",
        "https://wiki.mbedwars.com/en/Development/REST-API",
        this.plugin.getAuthController().getPermissions(ctx).stream()
            .map(Permission::getValue)
            .toArray(String[]::new)
    ));
  }
}
