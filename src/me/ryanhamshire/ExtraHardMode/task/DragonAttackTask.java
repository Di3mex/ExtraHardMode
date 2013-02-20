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

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * Task to allow a dragon to do additional attacks.
 */
public class DragonAttackTask implements Runnable {

   /**
    * Plugin instance.
    */
   private ExtraHardMode plugin;
   /**
    * Target player.
    */
   private Player player;
   /**
    * Attacking dragon.
    */
   private Entity dragon;

   /**
    * Constructor.
    * 
    * @param plugin
    *           - Plugin instance.
    * @param dragon
    *           - Dragon.
    * @param player
    *           - Target player.
    */
   public DragonAttackTask(ExtraHardMode plugin, Entity dragon, Player player) {
      this.plugin = plugin;
      this.dragon = dragon;
      this.player = player;
   }

   @Override
   public void run() {
      if(this.dragon.isDead()) {
         return;
      }
      if(!this.player.isOnline()) {
         return;
      }

      World world = this.dragon.getWorld();
      if(world != this.player.getWorld())
         return;

      Location dragonLocation = this.dragon.getLocation();

      Location targetLocation;
      if(plugin.random(20)) {
         targetLocation = world.getHighestBlockAt(dragonLocation).getLocation();
      } else {
         targetLocation = player.getLocation();
      }

      Location offsetLocation = targetLocation.add(plugin.getRandom().nextInt(10) - 5, plugin.getRandom().nextInt(3) - 1,
            plugin.getRandom().nextInt(10) - 5);

      Vector vector = new Vector(offsetLocation.getX() - dragonLocation.getX(), offsetLocation.getY() - dragonLocation.getY(), offsetLocation.getZ()
            - dragonLocation.getZ());

      Fireball fireball = (Fireball) world.spawnEntity(dragonLocation, EntityType.FIREBALL);
      fireball.setShooter((EnderDragon) this.dragon);
      fireball.setDirection(vector);
   }
}
