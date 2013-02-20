/*
    ExtraHardMode Server Plugin for Minecraft
    Copyright (C) 2012 Ryan Hamshire

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.ryanhamshire.ExtraHardMode.event;

import java.util.ArrayList;
import java.util.List;

import me.ryanhamshire.ExtraHardMode.ExtraHardMode;
import me.ryanhamshire.ExtraHardMode.config.Config;
import me.ryanhamshire.ExtraHardMode.config.messages.MessageNode;
import me.ryanhamshire.ExtraHardMode.config.messages.MessageConfig;
import me.ryanhamshire.ExtraHardMode.module.BlockModule;
import me.ryanhamshire.ExtraHardMode.service.PermissionNode;
import me.ryanhamshire.ExtraHardMode.task.EvaporateWaterTask;
import me.ryanhamshire.ExtraHardMode.task.RemoveExposedTorchesTask;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Torch;
import org.bukkit.util.Vector;

/**
 * event handlers related to blocks
 */
public class BlockEventHandler implements Listener {
   /**
    * Plugin instance.
    */
   private ExtraHardMode plugin;

   /**
    * Block faces to iterate through.
    */
   private final BlockFace[] blockFaces = new BlockFace[] { BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH,
         BlockFace.WEST };

   /**
    * constructor
    * 
    * @param plugin
    *           - plugin instance.
    */
   public BlockEventHandler(ExtraHardMode plugin) {
      this.plugin = plugin;
   }

   /**
    * When a player breaks a block...
    * 
    * @param breakEvent
    *           - Event that occurred.
    */
   @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
   public void onBlockBreak(BlockBreakEvent breakEvent) {
      Block block = breakEvent.getBlock();
      World world = block.getWorld();
      Player player = breakEvent.getPlayer();

      MessageConfig messages = plugin.getModuleForClass(MessageConfig.class);

      if(!Config.Enabled_Worlds.contains(world.getName()) || player.hasPermission("extrahardmode.bypass"))
         return;

      // FEATURE: very limited building in the end
      // players are allowed to break only end stone, and only to create a stair
      // up to ground level
      if(Config.Enderdragon__No_Building_In_End && world.getEnvironment() == Environment.THE_END) {
         if(block.getType() != Material.ENDER_STONE) {
            breakEvent.setCancelled(true);
            plugin.sendMessage(player, messages.getString(MessageNode.LIMITED_END_BUILDING));
            return;
         } else {
            int absoluteDistanceFromBlock = Math.abs(block.getX() - player.getLocation().getBlockX());
            int zdistance = Math.abs(block.getZ() - player.getLocation().getBlockZ());
            if(zdistance > absoluteDistanceFromBlock) {
               absoluteDistanceFromBlock = zdistance;
            }

            if(block.getY() < player.getLocation().getBlockY() + absoluteDistanceFromBlock) {
               breakEvent.setCancelled(true);
               plugin.sendMessage(player, messages.getString(MessageNode.LIMITED_END_BUILDING));
               return;
            }
         }
      }

      // FEATURE: stone breaks tools much more quickly
      if(Config.World__Mining__Prevent_Tunneling_To_Encourage_Cave_Exploration) {
         ItemStack inHandStack = player.getItemInHand();

         // if breaking stone with an item in hand and the player does NOT have
         // the bypass permission
         if((block.getType() == Material.STONE || block.getType() == Material.ENDER_STONE) && inHandStack != null) {
            // if not using an iron or diamond pickaxe, don't allow breakage and
            // explain to the player
            Material tool = inHandStack.getType();
            if(tool != Material.IRON_PICKAXE && tool != Material.DIAMOND_PICKAXE) {
               notifyPlayer(player, MessageNode.STONE_MINING_HELP, PermissionNode.SILENT_STONE_MINING_HELP, Sound.CAT_HISS, 10);
               breakEvent.setCancelled(true);
               return;
            }

            // otherwise, drastically reduce tool durability when breaking stone
            else {
               short amount = 0;

               if(tool == Material.IRON_PICKAXE)
                  amount = 8;
               else
                  amount = 22;

               inHandStack.setDurability((short) (inHandStack.getDurability() + amount));
            }
         }

         // when ore is broken, it softens adjacent stone
         // important to ensure players can reach the ore they break
         if(block.getType().name().endsWith("ORE") || block.getType().name().endsWith("ORES")) {
            for(BlockFace face : blockFaces) {
               Block adjacentBlock = block.getRelative(face);
               if(adjacentBlock.getType() == Material.STONE)
                  adjacentBlock.setType(Material.COBBLESTONE);
            }
         }
      }

      BlockModule module = plugin.getModuleForClass(BlockModule.class);

      // FEATURE: trees chop more naturally
      if(block.getType() == Material.LOG && Config.World__Better_Tree_Chopping) {
         Block rootBlock = block;
         while(rootBlock.getType() == Material.LOG) {
            rootBlock = rootBlock.getRelative(BlockFace.DOWN);
         }

         if(rootBlock.getType() == Material.DIRT || rootBlock.getType() == Material.GRASS) {
            Block aboveLog = block.getRelative(BlockFace.UP);
            while(aboveLog.getType() == Material.LOG) {
               module.applyPhysics(aboveLog);
               aboveLog = aboveLog.getRelative(BlockFace.UP);
            }
         }
      }

      // FEATURE: more falling blocks
      module.physicsCheck(block, 0, true);

      // FEATURE: no nether wart farming (always drops exactly 1 nether wart
      // when broken)
      if(Config.Farming__No_Farming_Nether_Wart) {
         if(block.getType() == Material.NETHER_WARTS) {
            block.getDrops().clear();
            block.getDrops().add(new ItemStack(Material.NETHER_STALK));
         }
      }

      // FEATURE: breaking netherrack may start a fire
      if(Config.World__Broken_Netherrack_Catches_Fire_Percent > 0 && block.getType() == Material.NETHERRACK) {
         Block underBlock = block.getRelative(BlockFace.DOWN);
         if(underBlock.getType() == Material.NETHERRACK && plugin.random(Config.World__Broken_Netherrack_Catches_Fire_Percent)) {
            breakEvent.setCancelled(true);
            block.setType(Material.FIRE);
         }
      }
   }

   /**
    * When a player places a block...
    * 
    * @param placeEvent
    *           - Event that occurred
    */
   @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
   public void onBlockPlace(BlockPlaceEvent placeEvent) {
      Player player = placeEvent.getPlayer();
      Block block = placeEvent.getBlock();
      World world = block.getWorld();

      MessageConfig messages = plugin.getModuleForClass(MessageConfig.class);

      if(!Config.Enabled_Worlds.contains(world.getName()) || player.hasPermission("extrahardmode.bypass"))
         return;

      // FEATURE: very limited building in the end
      // players are allowed to break only end stone, and only to create a stair
      // up to ground level
      if(Config.Enderdragon__No_Building_In_End && world.getEnvironment() == Environment.THE_END) {
         placeEvent.setCancelled(true);
         plugin.sendMessage(player, messages.getString(MessageNode.LIMITED_END_BUILDING));
         return;
      }

      // FIX: prevent players from placing ore as an exploit to work around the
      // hardened stone rule
      // ORES for redpower
      if(Config.World__Mining__Prevent_Tunneling_To_Encourage_Cave_Exploration
            && (block.getType().name().endsWith("ORE") || block.getType().name().endsWith("ORES"))) {
         ArrayList<Block> adjacentBlocks = new ArrayList<Block>();
         for(BlockFace face : blockFaces) {
            adjacentBlocks.add(block.getRelative(face));
         }

         for(Block adjacentBlock : adjacentBlocks) {
            if(adjacentBlock.getType() == Material.STONE) {
               plugin.sendMessage(player, messages.getString(MessageNode.NO_PLACING_ORE_AGAINST_STONE));
               placeEvent.setCancelled(true);
               return;
            }
         }
      }

      // FEATURE: no farming nether wart
      if(block.getType() == Material.NETHER_WARTS && Config.Farming__No_Farming_Nether_Wart) {
         placeEvent.setCancelled(true);
         return;
      }

      BlockModule module = plugin.getModuleForClass(BlockModule.class);
      // FEATURE: more falling blocks
      module.physicsCheck(block, 0, true);

      // FEATURE: no standard torches, jack o lanterns, or fire on top of
      // netherrack near diamond level
      final int minY = Config.World__Torches__Torch_Max_Y;
      if(minY > 0) {
         if(world.getEnvironment() == Environment.NORMAL
               && block.getY() < minY
               && (block.getType() == Material.TORCH || block.getType() == Material.JACK_O_LANTERN || (block.getType() == Material.FIRE && block
                     .getRelative(BlockFace.DOWN).getType() == Material.NETHERRACK))) {
            notifyPlayer(player, MessageNode.NO_TORCHES_HERE, PermissionNode.SILENT_NO_TORCHES_HERE, Sound.FIZZ, 20);
            placeEvent.setCancelled(true);
            return;
         }
      }

      // FEATURE: players can't place blocks from weird angles (using shift to
      // hover over in the air beyond the edge of solid ground)
      // or directly beneath themselves, for that matter
      if(Config.World__Limited_Block_Placement) {
         if(block.getX() == player.getLocation().getBlockX() && block.getZ() == player.getLocation().getBlockZ()
               && block.getY() < player.getLocation().getBlockY()) {
            notifyPlayer(player, MessageNode.REALISTIC_BUILDING, PermissionNode.SILENT_REALISTIC_BUILDING, Sound.NOTE_STICKS, 1);
            placeEvent.setCancelled(true);
            return;
         }

         Block underBlock = player.getLocation().getBlock().getRelative(BlockFace.DOWN);

         // if standing directly over lava, prevent placement
         if(underBlock.getType() == Material.LAVA || underBlock.getType() == Material.STATIONARY_LAVA) {
            notifyPlayer(player, MessageNode.REALISTIC_BUILDING, PermissionNode.SILENT_REALISTIC_BUILDING, Sound.NOTE_STICKS, 1);
            placeEvent.setCancelled(true);
            return;
         }

         // otherwise if hovering over air, check one block lower
         else if(underBlock.getType() == Material.AIR) {
            underBlock = underBlock.getRelative(BlockFace.DOWN);

            // if over lava or more air, prevent placement
            if(underBlock.getType() == Material.AIR || underBlock.getType() == Material.LAVA || underBlock.getType() == Material.STATIONARY_LAVA) {
               notifyPlayer(player, MessageNode.REALISTIC_BUILDING, PermissionNode.SILENT_REALISTIC_BUILDING, Sound.NOTE_STICKS, 1);
               placeEvent.setCancelled(true);
               return;
            }
         }
      }

      // FEATURE: players can't attach torches to common "soft" blocks
      if(Config.World__Torches__Limited_Torch_Placement && block.getType() == Material.TORCH) {
         Torch torch = new Torch(Material.TORCH, block.getData());
         Material attachmentMaterial = block.getRelative(torch.getAttachedFace()).getType();

         if(attachmentMaterial == Material.DIRT || attachmentMaterial == Material.GRASS || attachmentMaterial == Material.LONG_GRASS
               || attachmentMaterial == Material.SAND) {
            if(Config.World__Play_Sounds__Torch_Fizzing) {
               notifyPlayer(player, MessageNode.LIMITED_TORCH_PLACEMENTS, PermissionNode.SILENT_LIMITED_TORCH_PLACEMENT, Sound.FIZZ, 20);
            }
            placeEvent.setCancelled(true);
            return;
         }
      }
   }

   /**
    * when a dispenser dispenses...
    * 
    * @param event
    *           - Event that occurred.
    */
   @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
   void onBlockDispense(BlockDispenseEvent event) {
      // FEATURE: can't move water source blocks

      if(Config.World__Water__Dont_Move_Source_Blocks) {
         World world = event.getBlock().getWorld();
         if(!Config.Enabled_Worlds.contains(world.getName()))
            return;

         // only care about water
         if(event.getItem().getType() == Material.WATER_BUCKET) {
            // plan to evaporate the water next tick
            Block block;
            Vector velocity = event.getVelocity();
            if(velocity.getX() > 0) {
               block = event.getBlock().getLocation().add(1, 0, 0).getBlock();
            } else if(velocity.getX() < 0) {
               block = event.getBlock().getLocation().add(-1, 0, 0).getBlock();
            } else if(velocity.getZ() > 0) {
               block = event.getBlock().getLocation().add(0, 0, 1).getBlock();
            } else {
               block = event.getBlock().getLocation().add(0, 0, -1).getBlock();
            }

            EvaporateWaterTask task = new EvaporateWaterTask(block);
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, task, 1L);
         }
      }
   }

   /**
    * When a piston pushes...
    * 
    * @param event
    *           - Event that occurred
    */
   @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
   public void onBlockPistonExtend(BlockPistonExtendEvent event) {
      List<Block> blocks = event.getBlocks();
      World world = event.getBlock().getWorld();

      // FEATURE: prevent players from circumventing hardened stone rules by
      // placing ore, then pushing the ore next to stone before breaking it

      if(!Config.World__Mining__Prevent_Tunneling_To_Encourage_Cave_Exploration || !Config.Enabled_Worlds.contains(world.getName()))
         return;

      // which blocks are being pushed?
      for(int i = 0; i < blocks.size(); i++) {
         // if any are ore or stone, don't push
         Block block = blocks.get(i);
         Material material = block.getType();
         if(material == Material.STONE || material.name().endsWith("_ORE")) {
            event.setCancelled(true);
            return;
         }
      }
   }

   /**
    * When a piston pulls...
    * 
    * @param event
    *           - Event that occurred.
    */
   @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
   public void onBlockPistonRetract(BlockPistonRetractEvent event) {
      // FEATURE: prevent players from circumventing hardened stone rules by
      // placing ore, then pulling the ore next to stone before breaking it

      // we only care about sticky pistons
      if(!event.isSticky())
         return;

      Block block = event.getRetractLocation().getBlock();
      World world = block.getWorld();

      if(!Config.World__Mining__Prevent_Tunneling_To_Encourage_Cave_Exploration || !Config.Enabled_Worlds.contains(world.getName()))
         return;

      Material material = block.getType();
      if(material == Material.STONE || material.name().endsWith("_ORE")) {
         event.setCancelled(true);
         return;
      }
   }

   /**
    * When the weather changes...
    * 
    * @param event
    *           - Event that occurred.
    */
   @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
   public void onWeatherChange(WeatherChangeEvent event) {
      // FEATURE: rainfall breaks exposed torches (exposed to the sky)
      World world = event.getWorld();

      if(!Config.Enabled_Worlds.contains(world.getName())) {
         return;
      }

      if(!event.toWeatherState()) {
         return; // if not raining
      }

      // plan to remove torches chunk by chunk gradually throughout the rain
      // period
      Chunk[] chunks = world.getLoadedChunks();
      if(chunks.length > 0) {
         int startOffset = plugin.getRandom().nextInt(chunks.length);
         for(int i = 0; i < chunks.length; i++) {
            Chunk chunk = chunks[(startOffset + i) % chunks.length];

            RemoveExposedTorchesTask task = new RemoveExposedTorchesTask(plugin, chunk);
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, task, i * 20L);
         }
      }
   }

   /**
    * When a block grows...
    * 
    * @param event
    *           - Event that occurred.
    */
   @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
   public void onBlockGrow(BlockGrowEvent event) {
      // FEATURE: fewer seeds = shrinking crops. when a plant grows to its full
      // size, it may be replaced by a dead shrub
      if(plugin.getModuleForClass(BlockModule.class).plantDies(event.getBlock(), event.getNewState().getData().getData())) {
         event.setCancelled(true);
         event.getBlock().setType(Material.LONG_GRASS); // dead shrub
      }
   }

   /**
    * when a tree or mushroom grows...
    * 
    * @param event
    *           - Event that occurred.
    */
   @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
   public void onStructureGrow(StructureGrowEvent event) {
      World world = event.getWorld();
      Block block = event.getLocation().getBlock();

      if(!Config.Enabled_Worlds.contains(world.getName()) || (event.getPlayer() != null && event.getPlayer().hasPermission("extrahardmode.bypass")))
         return;

      // FEATURE: no big plant growth in deserts
      if(Config.Farming__Weak_Food_Crops__Arid_Infertile_Desserts) {
         Biome biome = block.getBiome();
         if(biome == Biome.DESERT || biome == Biome.DESERT_HILLS) {
            event.setCancelled(true);
         }
      }
   }

   // TODO FINISH FEATURE
   /*
    * //FEATURE Make Cobblestone Generators generate SmoothStone
    * 
    * @EventHandler public void onTransformFromTo (BlockFromToEvent event) {
    * //the block that triggered the event Block block = event.getBlock();
    * Material bMaterial = block.getType(); //the block that will be changed
    * Block toBlock = event.getToBlock(); Material toMaterial =
    * toBlock.getType();
    * 
    * if(bMaterial.name().contains("LAVA") ||
    * bMaterial.name().contains("WATER")) { if(toMaterial == Material.AIR) {
    * //Example: SourceBlock is Lava, then either water or stationary water can
    * create cobble Material possibilityOne = ( Material.WATER == bMaterial
    * ||//if Material.STATIONARY_WATER == bMaterial//or ? Material.LAVA//then
    * set to : Material.WATER);//otherwise set to Material possibilityTwo = (
    * Material.WATER == bMaterial ||//if Material.STATIONARY_WATER ==
    * bMaterial//or ? Material.STATIONARY_LAVA//then :
    * Material.STATIONARY_WATER);//otherwise
    * 
    * if(generatesCobble(bMaterial.getId(), toBlock)) {
    * //event.setCancelled(true); } } } }
    * 
    * public boolean generatesCobble(int id, Block b) { int mirrorID1 = (id == 8
    * || id == 9 ? 10 : 8); int mirrorID2 = (id == 8 || id == 9 ? 11 : 9);
    * for(BlockFace face : faces) { Block r = b.getRelative(face, 1);
    * ExtraHardMode
    * .instance.getServer().getPlayer("Diemex94").sendMessage(ChatColor.RED +
    * " Face: " + face.name() + " Material = " + r.getType().name());
    * if(r.getTypeId() == mirrorID1 || r.getTypeId() == mirrorID2) { return
    * true; } } return false; }
    */

   /**
    * Send the player an informative message to explain what he's doing wrong.
    * After that play error sounds instead of spamming the chat window. Uses the
    * permission to temporarily store which message has been shown already.
    * 
    * TODO might want to move this out to a module.
    * 
    * @author diemex
    * @param player
    * @param permissionName
    *           name of the temporary permission
    * @param sound
    *           errorsound to play after the event got cancelled
    * @param soundPitch
    */
   public void notifyPlayer(Player player, MessageNode node, PermissionNode perm, Sound sound, float soundPitch) {
      if(!player.hasPermission(perm.getNode())) {
         MessageConfig config = plugin.getModuleForClass(MessageConfig.class);
         plugin.sendMessage(player, config.getString(node));
         player.playSound(player.getLocation(), sound, 1, soundPitch);
      }

   }

}
