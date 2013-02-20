package me.ryanhamshire.ExtraHardMode.task;

import me.ryanhamshire.ExtraHardMode.ExtraHardMode;
import me.ryanhamshire.ExtraHardMode.module.BlockModule;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

/**
 * Called to apply physics to a block and its neighbors if necessary.
 */
public class BlockPhysicsCheckTask implements Runnable {

   /**
    * Plugin instance.
    */
   ExtraHardMode plugin;

   /**
    * Target block.
    */
   Block block;

   /**
    * Recursion count.
    */
   int recursionCount;

   /**
    * Constructor.
    * 
    * @param plugin
    *           - Plugin instance.
    * @param block
    *           - Target block for task.
    * @param recursionCount
    *           - Recursion count for task.
    */
   public BlockPhysicsCheckTask(ExtraHardMode plugin, Block block, int recursionCount) {
      this.plugin = plugin;
      this.block = block;
      this.recursionCount = recursionCount;
   }

   @Override
   public void run() {
      BlockModule module = plugin.getModuleForClass(BlockModule.class);
      block = block.getWorld().getBlockAt(block.getLocation());
      boolean fall = false;
      Material material = block.getType();
      if((block.getRelative(BlockFace.DOWN).getType() == Material.AIR || block.getRelative(BlockFace.DOWN).isLiquid() || block.getRelative(
            BlockFace.DOWN).getType() == Material.TORCH)
            && (material == Material.SAND || material == Material.GRAVEL || module.getFallingBlocks().contains(material))) {
         module.applyPhysics(block);
         fall = true;
      }

      if(fall || this.recursionCount == 0) {
         if(recursionCount < 10) {
            Block neighbor = block.getRelative(BlockFace.UP);
            module.physicsCheck(neighbor, recursionCount + 1, false);

            neighbor = block.getRelative(BlockFace.DOWN);
            module.physicsCheck(neighbor, recursionCount + 1, false);

            neighbor = block.getRelative(BlockFace.EAST);
            module.physicsCheck(neighbor, recursionCount + 1, false);

            neighbor = block.getRelative(BlockFace.WEST);
            module.physicsCheck(neighbor, recursionCount + 1, false);

            neighbor = block.getRelative(BlockFace.NORTH);
            module.physicsCheck(neighbor, recursionCount + 1, false);

            neighbor = block.getRelative(BlockFace.SOUTH);
            module.physicsCheck(neighbor, recursionCount + 1, false);
         }
      }
   }

}
