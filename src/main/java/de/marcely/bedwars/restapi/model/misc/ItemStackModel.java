package de.marcely.bedwars.restapi.model.misc;

import de.marcely.bedwars.tools.Helper;
import io.javalin.openapi.JsonSchema;
import io.javalin.openapi.OpenApiDescription;
import io.javalin.openapi.OpenApiExample;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

@Data
@AllArgsConstructor
@JsonSchema
public class ItemStackModel {

  @Getter(onMethod = @__({
      @OpenApiExample("IRON_SWORD"),
      @OpenApiDescription("The material of the item (ItemStack#getType()#name()).")
  }))
  private String material;

  @Getter(onMethod = @__({
      @OpenApiExample("IRON_SWORD:50 {DisplayName: \"§aIron Sword\"}"),
      @OpenApiDescription("The composted string of the item (Helper#composeItemStack(ItemStack)).")
  }))
  private String composed;


  public static ItemStackModel from(ItemStack is) {
    return new ItemStackModel(
        is.getType().name(),
        Helper.get().composeItemStack(is)
    );
  }
}
