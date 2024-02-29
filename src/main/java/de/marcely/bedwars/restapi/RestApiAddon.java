package de.marcely.bedwars.restapi;

import de.marcely.bedwars.api.BedwarsAddon;

public class RestApiAddon extends BedwarsAddon {

  RestApiAddon(RestApiPlugin plugin) {
    super(plugin);
  }

  @Override
  public String getName() {
    return "RestApi";
  }
}
