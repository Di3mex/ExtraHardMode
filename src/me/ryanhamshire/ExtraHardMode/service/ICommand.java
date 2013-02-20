package me.ryanhamshire.ExtraHardMode.service;

import me.ryanhamshire.ExtraHardMode.ExtraHardMode;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * Represents a command.
 */
public interface ICommand {

   /**
    * Execution method for the command.
    * 
    * @param sender
    *           - Sender of the command.
    * @param command
    *           - Command used.
    * @param label
    *           - Label.
    * @param args
    *           - Command arguments.
    * @return True if valid command and executed. Else false.
    */
   boolean execute(final ExtraHardMode plugin, final CommandSender sender, final Command command, final String label, String[] args);

}
