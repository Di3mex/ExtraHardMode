package me.ryanhamshire.ExtraHardMode.module;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.FallingBlock;

import me.ryanhamshire.ExtraHardMode.ExtraHardMode;
import me.ryanhamshire.ExtraHardMode.config.Config;
import me.ryanhamshire.ExtraHardMode.service.EHMModule;
import me.ryanhamshire.ExtraHardMode.task.BlockPhysicsCheckTask;

/**
 * Module that manages blocks and physics logic.
 */
public class BlockModule extends EHMModule {

   /**
    * which materials beyond sand and gravel should be subject to gravity
    */
   private final List<Material> fallingBlocks = new ArrayList<Material>();

   /**
    * Constructor.
    * 
    * @param plugin
    *           - plugin instance.
    */
   public BlockModule(ExtraHardMode plugin) {
      super(plugin);
   }

   /**
    * Schedule the physics task
    * 
    * @param block
    *           - Target block.
    * @param recursionCount
    *           - Number of times to execute.
    * @param skipCenterBlock
    *           - Whether to skip the center block or not.
    */
   public void physicsCheck(Block block, int recursionCount, boolean skipCenterBlock) {
      int id = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new BlockPhysicsCheckTask(plugin, block, recursionCount), 5L);
      // check if it was scheduled. If not, notify in console.
      if(id == -1) {
         plugin.getLogger().severe("Failed schedule BlockPhysicsCheck task!");
      }
   }

   /**
    * Makes a block subject to gravity
    * 
    * @param block
    *           - Block to apply physics to.
    */
   public void applyPhysics(Block block) {
      // TODO make this optional
      // grass and mycel become dirt when they fall
      if(block.getType() == Material.GRASS || block.getType() == Material.MYCEL) {
         block.setType(Material.DIRT);
      }

      // create falling block
      FallingBlock fallingBlock = block.getWorld().spawnFallingBlock(block.getLocation(), block.getTypeId(), block.getData());
      fallingBlock.setDropItem(true);

      // remove original block
      block.setType(Material.AIR);
   }

   /**
    * Check if the given plant at the block dies.
    * 
    * @param block
    *           - Block to check.
    * @param newDataValue
    *           - Data value to replace.
    * @return True if plant died, else false.
    */
   public boolean plantDies(Block block, byte newDataValue) {
      World world = block.getWorld();
      if(!Config.Enabled_Worlds.contains(world.getName()) || !Config.Farming__Weak_Food_Crops__Enable) {
         return false;
      }

      // not evaluated until the plant is nearly full grown
      if(newDataValue <= (byte) 6) {
         return false;
      }

      Material material = block.getType();
      if(material == Material.CROPS || material == Material.MELON_STEM || material == Material.CARROT || material == Material.PUMPKIN_STEM
            || material == Material.POTATO) {
         int deathProbability = 25;

         // plants in the dark always die
         if(block.getLightFromSky() < 10) {
            deathProbability = 100;
         } else {
            Biome biome = block.getBiome();

            // the desert environment is very rough on crops
            if(biome == Biome.DESERT || biome == Biome.DESERT_HILLS) {
               deathProbability += 50;
            }

            // unwatered crops are more likely to die
            Block belowBlock = block.getRelative(BlockFace.DOWN);
            byte moistureLevel = 0;
            if(belowBlock.getType() == Material.SOIL) {
               moistureLevel = belowBlock.getData();
            }

            if(moistureLevel == 0) {
               deathProbability += 25;
            }
         }

         if(plugin.random(deathProbability)) {
            return true;
         }
      }

      return false;
   }

   /**
    * Get the list of falling blocks.
    * 
    * @return List of materials of falling blocks.
    */
   public List<Material> getFallingBlocks() {
      return fallingBlocks;
   }

   @Override
   public void starting() {
      // parse this final list of additional falling blocks
      for(String materialName : Config.More_Falling_Blocks) {
         Material material = Material.getMaterial(materialName);
         if(material == null) {
            plugin.getLogger().warning("Additional Falling Blocks Configuration: Material not found: " + materialName + ".");
         } else {
            this.fallingBlocks.add(material);
         }
      }
   }

   @Override
   public void closing() {
      fallingBlocks.clear();
   }
}
