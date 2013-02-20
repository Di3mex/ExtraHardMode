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

import org.bukkit.entity.Player;

/**
 * Set the target player's health and food levels.
 */
public class SetPlayerHealthAndFoodTask implements Runnable {

   /**
    * Target player.
    */
   private Player player;
   /**
    * Health level.
    */
   private int health;
   /**
    * Food level.
    */
   private int food;

   /**
    * Constructor.
    * 
    * @param player
    *           - Player to adjust.
    * @param health
    *           - Health level.
    * @param food
    *           - Food level.
    */
   public SetPlayerHealthAndFoodTask(Player player, int health, int food) {
      this.player = player;
      this.health = health;
      this.food = food;
   }

   @Override
   public void run() {
      try {
         try {
            this.player.setHealth(this.health);
         } catch(IllegalArgumentException e) {
         } // if less than zero or higher than max, no changes

         this.player.setFoodLevel(this.food);
      } catch(NullPointerException e) {
         // Catch if player is null.
      }
   }

}
