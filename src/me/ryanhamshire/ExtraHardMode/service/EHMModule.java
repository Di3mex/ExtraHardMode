package me.ryanhamshire.ExtraHardMode.service;

import me.ryanhamshire.ExtraHardMode.ExtraHardMode;

/**
 * ExtraHardMode module.
 */
public abstract class EHMModule implements IModule {
   /**
    * Plugin reference.
    */
   protected ExtraHardMode plugin;

   /**
    * Constructor.
    * 
    * @param plugin
    *           - Plugin instance.
    */
   public EHMModule(ExtraHardMode plugin) {
      this.plugin = plugin;
   }
}
