package me.ryanhamshire.ExtraHardMode.config;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import me.ryanhamshire.ExtraHardMode.ExtraHardMode;
import me.ryanhamshire.ExtraHardMode.service.ModularConfig;

/**
 * Configuration handler for the root config.yml file.
 */
public class RootConfig extends ModularConfig {

   /**
    * Constructor.
    * 
    * @param plugin
    *           - plugin instance.
    */
   public RootConfig(ExtraHardMode plugin) {
      super(plugin);
   }

   @Override
   public void starting() {
      loadDefaults(plugin.getConfig());
      plugin.saveConfig();
      reload();
   }

   @Override
   public void closing() {
      plugin.reloadConfig();
      plugin.saveConfig();
   }

   @Override
   public void save() {
      plugin.saveConfig();
   }

   @Override
   public void set(String path, Object value) {
      final ConfigurationSection config = plugin.getConfig();
      config.set(path, value);
      plugin.saveConfig();
   }

   @Override
   public void reload() {
      plugin.reloadConfig();
      loadSettings(plugin.getConfig());
      boundsCheck();
   }

   @Override
   public void loadSettings(ConfigurationSection config) {
      for(final RootNode node : RootNode.values()) {
         updateOption(node);
      }
   }

   @Override
   public void loadDefaults(ConfigurationSection config) {
      for(RootNode node : RootNode.values()) {
         if(!config.contains(node.getPath())) {
            config.set(node.getPath(), node.getDefaultValue());
         }
      }
   }

   @Override
   public void boundsCheck() {
      // Check worlds
      List<String> list = getStringList(RootNode.WORLDS);
      if(list.isEmpty()) {
         plugin.getLogger().warning(plugin.getTag() + " No worlds selected!");
      }
      List<World> worlds = new ArrayList<World>();
      for(String name : list) {
         World world = plugin.getServer().getWorld(name);
         if(world != null) {
            // Not going to notify on missing world as that will occur in the
            // main plugin execution.
            worlds.add(world);
         }
      }
      // Check y coordinates
      validateYCoordinate(RootNode.STANDARD_TORCH_MIN_Y, worlds);
      validateYCoordinate(RootNode.MORE_MONSTERS_MAX_Y, worlds);
      validateYCoordinate(RootNode.MONSTER_SPAWNS_IN_LIGHT_MAX_Y, worlds);
      // Check percentages
      validatePercentage(RootNode.BROKEN_NETHERRACK_CATCHES_FIRE_PERCENT);
      // TODO should this be special?
      validatePercentage(RootNode.MORE_MONSTERS_MULTIPLIER);
      validatePercentage(RootNode.ZOMBIES_REANIMATE_PERCENT);
      validatePercentage(RootNode.SKELETONS_KNOCK_BACK_PERCENT);
      validatePercentage(RootNode.SKELETONS_RELEASE_SILVERFISH);
      validatePercentage(RootNode.SKELETONS_DEFLECT_ARROWS);
      validatePercentage(RootNode.BONUS_UNDERGROUND_SPIDER_SPAWN_PERCENT);
      validatePercentage(RootNode.BONUS_WITCH_SPAWN_PERCENT);
      validatePercentage(RootNode.CHARGED_CREEPER_SPAWN_PERCENT);
      validatePercentage(RootNode.CREEPERS_DROP_TNT_ON_DEATH_PERCENT);
      validatePercentage(RootNode.NEAR_BEDROCK_BLAZE_SPAWN_PERCENT);
      validatePercentage(RootNode.BONUS_NETHER_BLAZE_SPAWN_PERCENT);
      validatePercentage(RootNode.FLAME_SLIMES_SPAWN_WITH_NETHER_BLAZE_PRESENT);
      validatePercentage(RootNode.NETHER_BLAZES_SPLIT_ON_DEATH_PERCENT);
      validatePercentage(RootNode.PLAYER_DEATH_ITEM_STACKS_FORFEIT_PERCENT);
      validatePercentage(RootNode.PLAYER_RESPAWN_HEALTH);
      validatePercentage(RootNode.PLAYER_RESPAWN_FOOD_LEVEL);
   }

   /**
    * Validate Y coordinate limit for the given configuration option against the
    * list of enabled worlds.
    * 
    * @param node
    *           - Root node to validate.
    * @param worlds
    *           - List of worlds to check against.
    */
   private void validateYCoordinate(RootNode node, List<World> worlds) {
      int value = getInt(node);
      boolean changed = false;
      if(value < 0) {
         plugin.getLogger().warning(plugin.getTag() + " Y coordinate for " + node.getPath() + " cannot be less than 0.");
         set(node, 0);
         changed = true;
      }
      for(World world : worlds) {
         if(value > world.getMaxHeight()) {
            plugin.getLogger().warning(
                  plugin.getTag() + " Y coordinate for " + node.getPath() + " is greater than the max height for world " + world.getName());
            set(node, world.getMaxHeight());
            value = world.getMaxHeight();
            changed = true;
         }
      }
      if(changed) {
         updateOption(node);
      }
   }

   /**
    * Validate percentage value for given configuration option.
    * 
    * @param node
    *           - Root node to validate.
    */
   private void validatePercentage(RootNode node) {
      boolean changed = false;
      int value = getInt(node);
      if(value < 0) {
         plugin.getLogger().warning(plugin.getTag() + " Percentage for " + node.getPath() + " cannot be less than 0.");
         set(node, 0);
         changed = true;
      } else if(value > 100) {
         plugin.getLogger().warning(plugin.getTag() + " Percentage for " + node.getPath() + " cannot be greater than 100.");
         set(node, 100);
         changed = true;
      }
      if(changed) {
         updateOption(node);
      }
   }

}
