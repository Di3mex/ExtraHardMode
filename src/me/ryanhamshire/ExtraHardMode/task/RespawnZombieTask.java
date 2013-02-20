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

import me.ryanhamshire.ExtraHardMode.ExtraHardMode;
import me.ryanhamshire.ExtraHardMode.module.EntityModule;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;

/**
 * Respawn a zombie for a given location with a target player.
 */
public class RespawnZombieTask implements Runnable {

   /**
    * Plugin instance.
    */
   private ExtraHardMode plugin;
   /**
    * Respawn location.
    */
   private Location location;
   /**
    * Target player.
    */
   private Player player;

   /**
    * Constructor.
    * 
    * @param plugin
    *           - Plugin instance.
    * @param location
    *           - Respawn location.
    * @param target
    *           - Target player.
    */
   public RespawnZombieTask(ExtraHardMode plugin, Location location, Player target) {
      this.plugin = plugin;
      this.location = location;
      this.player = target;
   }

   @Override
   public void run() {
      Chunk chunk = location.getChunk();
      if(!chunk.isLoaded()) {
         return;
      }
      EntityModule module = plugin.getModuleForClass(EntityModule.class);
      Zombie zombie = (Zombie) location.getWorld().spawnEntity(location, EntityType.ZOMBIE);
      // zombie has half normal zombie health
      zombie.setHealth(zombie.getHealth() / 2);
      // this zombie will not drop loot (again)
      module.markLootLess(zombie);
      // zombie is still madat the same player
      if(this.player != null && this.player.isOnline()) {
         zombie.setTarget(this.player);
      }
   }
}
