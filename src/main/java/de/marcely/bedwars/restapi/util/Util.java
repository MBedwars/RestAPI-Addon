package de.marcely.bedwars.restapi.util;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.MemoryConfiguration;

public class Util {

  public static <T> T getAwait(Consumer<Consumer<T>> supplier) {
    final CountDownLatch latch = new CountDownLatch(1);
    final AtomicReference<T> cache = new AtomicReference<>();

    supplier.accept((result) -> {
      cache.set(result);
      latch.countDown();
    });

    try {
      latch.await();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }

    return cache.get();
  }

  public static char[] generateRandomPassword(int length) {
    final String charTable = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789?!@#$%^&*()_+{}[]|:;<>,.~`-=";
    final char[] chars = new char[length];
    final SecureRandom rand = new SecureRandom();

    for (int i = 0; i < length; i++)
      chars[i] = charTable.charAt(rand.nextInt(charTable.length()));

    return chars;
  }

  public static Configuration yamlRawToConfig(Object obj) {
    if (obj instanceof Configuration)
      return (Configuration) obj;
    else if (obj instanceof Map) {
      final Map<String, Object> map = (Map<String, Object>) obj;
      final MemoryConfiguration sec = new MemoryConfiguration();

      sec.addDefaults(map);

      // fix that maps don't automatically get changed to sections
      for (Map.Entry<String, Object> e : map.entrySet()) {
        if (!(e.getValue() instanceof Map))
          continue;

        sec.set(e.getKey(), yamlRawToConfig(e.getValue()));
      }

      return sec;
    } else
      return null;
  }
}
