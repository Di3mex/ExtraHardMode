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

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;

/**
 * Called to cleanup webs near an entity.
 */
public class WebCleanupTask implements Runnable {

   /**
    * List of blocks to check.
    */
   private final List<Block> webs = new ArrayList<>();

   /**
    * Constructor.
    * 
    * @param changedBlocks
    *           - Block to check.
    */
   public WebCleanupTask(List<Block> changedBlocks) {
      this.webs.addAll(changedBlocks);
   }

   @Override
   public void run() {
      for(Block block : webs) {
         // don't load a chunk just to clean up webs
         if(!block.getChunk().isLoaded()) {
            continue;
         } else if(block.getType() == Material.WEB) {
            // only turn webs to air. there's a chance the web may have been
            // replaced since it was placed.
            block.setType(Material.AIR);
         }
      }
   }
}
