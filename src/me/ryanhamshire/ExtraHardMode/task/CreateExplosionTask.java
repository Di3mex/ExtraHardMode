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

import org.bukkit.Location;

/**
 * Creates an explosion of specified power at the target location.
 */
public class CreateExplosionTask implements Runnable {
   /**
    * Location of explosion.
    */
   private Location location;
   /**
    * Power of explosion.
    */
   private float power;

   /**
    * Constructor.
    * 
    * @param location
    *           - Location to make explosion occur.
    * @param power
    *           - Power of resulting explosion.
    */
   public CreateExplosionTask(Location location, float power) {
      this.location = location;
      this.power = power;
   }

   @Override
   public void run() {
      this.location.getWorld().createExplosion(this.location, this.power);
   }
}
