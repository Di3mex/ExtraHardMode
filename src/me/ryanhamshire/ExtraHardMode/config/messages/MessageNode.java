package me.ryanhamshire.ExtraHardMode.config.messages;

import me.ryanhamshire.ExtraHardMode.service.ConfigNode;

/**
 * Configuration nodes for messages.yml configuration file.
 */
public enum MessageNode implements ConfigNode {
   NO_TORCHES_HERE("Messages.NoTorchesHere", "There's not enough air flow down here for permanent flames. Use another method to light your way."),
   STONE_MINING_HELP(
         "Messages.StoneMiningHelp",
         "You'll need an iron or diamond pickaxe to break stone.  Try exploring natural formations for exposed ore like coal, which softens stone around it when broken."),
   NO_PLACING_ORE_AGAINST_STONE("Messages.NoPlacingOreAgainstStone", "Sorry, you can't place ore next to stone."),
   REALISTIC_BUILDING("Messages.RealisticBuilding", "You can't build while in the air."),
   LIMITED_TORCH_PLACEMENTS("Messages.LimitedTorchPlacements", "It's too soft there to fasten a torch."),
   NO_CRAFTING_MELON_SEEDS("Messages.NoCraftingMelonSeeds", "That appears to be seedless!"),
   LIMITED_END_BUILDING("Messages.LimitedEndBuilding", "Sorry, building here is very limited.  You may only break blocks to reach ground level."),
   DRAGON_FOUNTAIN_TIP("Messages.DragonFountainTip",
         "Congratulations on defeating the dragon!  If you can't reach the fountain to jump into the portal, throw an ender pearl at it."),
   NO_SWIMMING_IN_ARMOR("Message.NoSwimmingInArmor", "You're carrying too much weight to swim!");

   /**
    * Configuration path.
    */
   private final String path;
   /**
    * Messages are always strings.
    */
   private final VarType type = VarType.STRING;
   /**
    * Default value.
    */
   private final Object defaultValue;

   /**
    * Constructor.
    * 
    * @param path
    *           - Configuration path.
    * @param def
    *           - Default value.
    */
   private MessageNode(String path, Object def) {
      this.path = path;
      this.defaultValue = def;
   }

   @Override
   public String getPath() {
      return path;
   }

   @Override
   public VarType getVarType() {
      return type;
   }

   @Override
   public Object getDefaultValue() {
      return defaultValue;
   }

}
