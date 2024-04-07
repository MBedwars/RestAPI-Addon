package de.marcely.bedwars.restapi;

import de.marcely.bedwars.restapi.auth.Permission;
import de.marcely.bedwars.restapi.auth.User;
import de.marcely.bedwars.restapi.util.Util;
import de.marcely.bedwars.tools.YamlConfigurationDescriptor;
import java.io.File;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Configs {

  public static String host = "localhost";
  public static int port = 8084;
  public static List<String> allowedCorsOrigins = Collections.emptyList();

  private static final byte VERSION = 2;

  private static File getFile(RestApiPlugin plugin) {
    return new File(plugin.getAddon().getDataFolder(), "configs.yml");
  }

  public static void load(RestApiPlugin plugin) {
    synchronized (Configs.class) {
      try {
        loadUnchecked(plugin);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private static void loadUnchecked(RestApiPlugin plugin) throws Exception {
    final File file = getFile(plugin);

    if (!file.exists()) {
      save(plugin);
      return;
    }

    // load it
    final FileConfiguration config = new YamlConfiguration();

    try (Reader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
      config.load(reader);
    } catch (Exception e) {
      e.printStackTrace();
    }

    // read it
    Configs.host = config.getString("host", Configs.host);
    Configs.port = config.getInt("port", Configs.port);
    Configs.allowedCorsOrigins = config.getStringList("allowed-cors-origins");

    {
      final List<?> entries = config.getList("users", Collections.emptyList());

      plugin.getAuthController().clear();

      for (Object entry : entries) {
        final Configuration section = Util.yamlRawToConfig(entry);

        if (section == null) {
          plugin.getLogger().warning("(Config) Skipped entry within \"users\" as it wasn't a section (invalid format)");
          continue;
        }

        if (!section.isString("username") || !section.isString("password") || !section.isList("permissions")) {
          plugin.getLogger().warning("(Config) Skipped entry within \"users\" as it was missing required fields");
          continue;
        }

        final User user = new User(
            section.getString("username"),
            section.getString("password").toCharArray(),
            section.getStringList("permissions").stream()
                .map(v -> new Permission(v))
                .collect(Collectors.toList()));

        plugin.getAuthController().add(user);
      }
    }

    // auto update file if newer version
    {
      final int currentVersion = config.getInt("file-version", -1);

      if (currentVersion != VERSION)
        save(plugin);
    }
  }

  private static void save(RestApiPlugin plugin) throws Exception {
    final YamlConfigurationDescriptor config = new YamlConfigurationDescriptor();

    config.addComment("Used for auto-updating the config file. Ignore it");
    config.set("file-version", VERSION);

    config.addEmptyLine();

    config.addComment("Address and port of the http server");
    config.addComment("Use of a reverse proxy for https is recommended");
    config.set("host", Configs.host);
    config.set("port", Configs.port);

    config.addEmptyLine();

    config.addComment("The list of users that are allowed to have access");
    config.addComment("All available permissions (wildcard * is NOT supported):");
    Permission.fetchAll().forEach(p -> config.addComment(" - " + p.getValue()));

    {
      final List<Configuration> entries = new ArrayList<>();

      for (User user : plugin.getAuthController().getAll()) {
        final Configuration section = new MemoryConfiguration();

        section.set("username", user.getUsername());
        section.set("password", new String(user.getPassword()));
        section.set("permissions", user.getPermissions().stream()
            .map(Permission::getValue)
            .toArray());

        entries.add(section);
      }

      config.set("users", entries);
    }

    config.addEmptyLine();

    config.addComment("The list of allowed CORS origins. It adds an extra layer of security");
    config.addComment("It basically refers to the allowed domains that can access the API");
    config.addComment("You may see an \"Cross-Origin Request Blocked\" error if it's not configured correctly");
    config.addComment("Use * to allow all origins, although it is recommended to type in manually the permitted domains");
    config.set("allowed-cors-origins", Configs.allowedCorsOrigins);

    // save
    getFile(plugin).getParentFile().mkdirs();

    try (Writer writer = Files.newBufferedWriter(getFile(plugin).toPath(), StandardCharsets.UTF_8)) {
      writer.write(config.saveToString());
    }
  }
}
