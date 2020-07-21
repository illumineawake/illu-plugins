package net.runelite.cliet.plugins.smokerunecrafter;

import lombok.Getter;

@Getter
public enum EssenceTypes
{
  PURE_ESSENCE("Pure Essence"),
   RUNE_ESSENCE("Rune Essence"),
   DAEYALT_ESSENCE("Daeyalt Essence");
   
   private final String name;
   private String menuOption = "";
   
   EssenceTypes(String name)
   {
      this.name = name;
    }
    
    EssenceTypes(String name, String menuOption)
    {
      this.name = name;
      this.menuOption = menuOption;
     }
  }
