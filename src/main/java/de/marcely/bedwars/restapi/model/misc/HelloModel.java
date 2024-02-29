package de.marcely.bedwars.restapi.model.misc;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HelloModel {

  private String motd;
  private String version;
  private String productWebsite;
  private String documentationWiki;
  private String documentationSpecification;
  private String[] authenticatedUserPermissions;
}
