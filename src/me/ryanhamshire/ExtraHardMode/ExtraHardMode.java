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

package me.ryanhamshire.ExtraHardMode;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import me.ryanhamshire.ExtraHardMode.command.Commander;
import me.ryanhamshire.ExtraHardMode.config.Config;
import me.ryanhamshire.ExtraHardMode.config.messages.MessageConfig;
import me.ryanhamshire.ExtraHardMode.event.BlockEventHandler;
import me.ryanhamshire.ExtraHardMode.event.EntityEventHandler;
import me.ryanhamshire.ExtraHardMode.event.PlayerEventHandler;
import me.ryanhamshire.ExtraHardMode.module.DataStoreModule;
import me.ryanhamshire.ExtraHardMode.module.DataStoreModule.PlayerData;
import me.ryanhamshire.ExtraHardMode.module.EntityModule;
import me.ryanhamshire.ExtraHardMode.module.BlockModule;
import me.ryanhamshire.ExtraHardMode.service.IModule;
import me.ryanhamshire.ExtraHardMode.task.MoreMonstersTask;

import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main plugin class.
 */
public class ExtraHardMode extends JavaPlugin {

   /**
    * Plugin tag.
    */
   public static final String TAG = "[EHM]";

   /**
    * Registered modules.
    */
   private final Map<Class<? extends IModule>, IModule> modules = new HashMap<>();

   /**
    * for computing random chance
    */
   private final Random randomNumberGenerator = new Random();

   /**
    * initializes well... everything
    */
   @Override
   public void onEnable() {
      Config.load(this);
      // Register modules
      //TODO switch to this
      //registerModule(RootConfig.class, new RootConfig(this));
      registerModule(MessageConfig.class, new MessageConfig(this));
      registerModule(DataStoreModule.class, new DataStoreModule(this));
      registerModule(EntityModule.class, new EntityModule(this));
      registerModule(BlockModule.class, new BlockModule(this));
      
      //Register command
      getCommand("ehm").setExecutor(new Commander(this));

      // register for events
      PluginManager pluginManager = this.getServer().getPluginManager();

      // player events
      PlayerEventHandler playerEventHandler = new PlayerEventHandler(this);
      pluginManager.registerEvents(playerEventHandler, this);

      // block events
      BlockEventHandler blockEventHandler = new BlockEventHandler(this);
      pluginManager.registerEvents(blockEventHandler, this);

      // entity events
      EntityEventHandler entityEventHandler = new EntityEventHandler(this);
      pluginManager.registerEvents(entityEventHandler, this);

      // FEATURE: monsters spawn in the light under a configurable Y level
      MoreMonstersTask task = new MoreMonstersTask(this);
      // Every 60 seconds
      this.getServer().getScheduler().scheduleSyncRepeatingTask(this, task, 1200L, 1200L);
   }

   /**
    * Sends a message to a player. Attempts to not spam the player with
    * messages.
    * 
    * @param player
    *           - Target player.
    * @param message
    *           - Message to send.
    */
   public void sendMessage(Player player, String message) {
      if(player == null) {
         getLogger().warning("Could not send the following message: " + message);
      } else {
         // FEATURE: don't spam messages
         PlayerData playerData = getModuleForClass(DataStoreModule.class).getPlayerData(player.getName());
         long now = Calendar.getInstance().getTimeInMillis();
         if(!message.equals(playerData.lastMessageSent) || now - playerData.lastMessageTimestamp > 30000) {
            player.sendMessage(message);
            playerData.lastMessageSent = message;
            playerData.lastMessageTimestamp = now;
         }
      }
   }

   /**
    * Computes random chance
    * 
    * @param percentChance
    *           - Percentage of success.
    * @return True if it was successful, else false.
    */
   public boolean random(int percentChance) {
      return randomNumberGenerator.nextInt(101) < percentChance;
   }

   /**
    * Get random generator.
    * 
    * @return
    */
   public Random getRandom() {
      return randomNumberGenerator;
   }

   public String getTag() {
      return TAG;
   }

   /**
    * Register a module.
    * 
    * @param clazz
    *           - Class of the instance.
    * @param module
    *           - Module instance.
    * @throws IllegalArgumentException
    *            - Thrown if an argument is null.
    */
   public <T extends IModule> void registerModule(Class<T> clazz, T module) {
      // Check arguments.
      if(clazz == null) {
         throw new IllegalArgumentException("Class cannot be null");
      } else if(module == null) {
         throw new IllegalArgumentException("Module cannot be null");
      }
      // Add module.
      modules.put(clazz, module);
      // Tell module to start.
      module.starting();
   }

   /**
    * Deregister a module.
    * 
    * @param clazz
    *           - Class of the instance.
    * @return Module that was removed. Returns null if no instance of the module
    *         is registered.
    */
   public <T extends IModule> T deregisterModuleForClass(Class<T> clazz) {
      // Check arguments.
      if(clazz == null) {
         throw new IllegalArgumentException("Class cannot be null");
      }
      // Grab module and tell it its closing.
      T module = clazz.cast(modules.get(clazz));
      if(module != null) {
         module.closing();
      }
      return module;
   }

   /**
    * Retrieve a registered module.
    * 
    * @param clazz
    *           - Class identifier.
    * @return Module instance. Returns null is an instance of the given class
    *         has not been registered with the API.
    */
   public <T extends IModule> T getModuleForClass(Class<T> clazz) {
      return clazz.cast(modules.get(clazz));
   }
}
