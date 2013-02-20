package me.ryanhamshire.ExtraHardMode.service;

/**
 * Represents a module.
 */
public interface IModule {
   /**
    * Called when the module has been registered to the API.
    */
   public abstract void starting();

   /**
    * Called when the module has been removed from the API.
    */
   public abstract void closing();
}
