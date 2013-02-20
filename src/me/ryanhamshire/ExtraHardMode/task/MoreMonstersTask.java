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

package me.ryanhamshire.ExtraHardMode.task;

import java.util.AbstractMap.SimpleEntry;

import me.ryanhamshire.ExtraHardMode.ExtraHardMode;
import me.ryanhamshire.ExtraHardMode.config.Config;
import me.ryanhamshire.ExtraHardMode.module.DataStoreModule;
import me.ryanhamshire.ExtraHardMode.service.PermissionNode;

import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

/**
 * Task to spawn more monsters.
 */
public class MoreMonstersTask implements Runnable {

   /**
    * Plugin instance.
    */
   private ExtraHardMode plugin;

   /**
    * Constructor.
    * 
    * @param plugin
    *           - Plugin instance.
    */
   public MoreMonstersTask(ExtraHardMode plugin) {
      this.plugin = plugin;
   }

   @Override
   public void run() {
      DataStoreModule dataStore = plugin.getModuleForClass(DataStoreModule.class);
      // spawn monsters from the last pass
      for(SimpleEntry<Player, Location> entry : dataStore.getPreviousLocations()) {
         Player player = entry.getKey();
         Location location = entry.getValue();
         Chunk chunk = location.getChunk();
         World world = location.getWorld();

         try {
            // chunk must be loaded, player must not be close, and there must be
            // no other players in the chunk
            if(location.getChunk().isLoaded() && player.isOnline() && location.distanceSquared(player.getLocation()) > 150) {
               boolean playerInChunk = false;
               Entity[] entities = chunk.getEntities();
               for(int j = 0; j < entities.length; j++) {
                  if(entities[j].getType() == EntityType.PLAYER) {
                     playerInChunk = true;
                     break;
                  }
               }

               if(!playerInChunk) {
                  // spawn random monster(s)
                  if(world.getEnvironment() == Environment.NORMAL) {
                     int random = plugin.getRandom().nextInt();
                     EntityType monsterType;
                     int typeMultiplier = 1;

                     // decide which kind and how many
                     // silverfish are most common
                     if(random < 30) {
                        monsterType = EntityType.SILVERFISH;
                        // twice as many if silverfish
                        typeMultiplier = 2;
                     } else if(random < 47) {
                        monsterType = EntityType.SKELETON;
                     } else if(random < 64) {
                        monsterType = EntityType.ZOMBIE;
                     } else if(random < 81) {
                        monsterType = EntityType.CREEPER;
                     } else {
                        monsterType = EntityType.SPIDER;
                     }

                     int totalToSpawn = typeMultiplier;
                     for(int j = 0; j < totalToSpawn; j++) {
                        world.spawnEntity(location, monsterType);
                     }
                  } else if(world.getEnvironment() == Environment.NETHER) {
                     int random = plugin.getRandom().nextInt();

                     if(random < 80) {
                        world.spawnEntity(location, EntityType.PIG_ZOMBIE);
                     } else {
                        world.spawnEntity(location, EntityType.BLAZE);
                     }
                  }
               }
            }
         } catch(IllegalArgumentException exception) {
         } // in case the player is in a different world from the saved location
      }

      // plan for the next pass
      dataStore.getPreviousLocations().clear();
      for(Player player : plugin.getServer().getOnlinePlayers()) {
         Location location = player.getLocation();
         Block playerBlock = location.getBlock();
         World world = player.getWorld();

         if(Config.Enabled_Worlds.contains(player.getWorld().getName()) && player.hasPermission(PermissionNode.BYPASS.getNode())
               && player.getGameMode() == GameMode.SURVIVAL) {
            // Only spawn monsters in normal world. End is crowded with enderman
            // and nether is too extreme anyway, add config later
            if(world.getEnvironment() == Environment.NORMAL
                  && (location.getY() > Config.General_Monster_Rules__Monsters_Spawn_In_Light_Max_Y || location.getBlock().getLightFromSky() > 0)) {
               // the playerBlock should always be air, but if the player stands
               // on a slab he actually is in the slab
               if(playerBlock.getType().equals(Material.AIR)) {
                  for(int i = 0; i <= 3; i++) {
                     Location checkUnder = location.subtract(0, 1, 0);
                     Block checkUnderBlock = checkUnder.getBlock();
                     if(checkUnderBlock.getType() != Material.AIR) {
                        location = checkUnder;
                        playerBlock = location.getBlock();
                        // the playerBlock is now the block where the monster
                        // should spawn on, next up: verify block
                        break;
                     }
                  }
               }
               // no spawning on steps, stairs and transparent blocks
               if(playerBlock.getType().name().endsWith("STEP") || playerBlock.getType().name().endsWith("STAIRS")
                     || playerBlock.getType().isTransparent() || playerBlock.getType().isOccluding() || playerBlock.getType().equals(Material.AIR)) {
                  // don't spawn here
                  return;
               }

               // Once we are here the block is safe to spawn on
               dataStore.getPreviousLocations().add(new SimpleEntry<Player, Location>(player, location));

            }
         }
      }
   }
}
