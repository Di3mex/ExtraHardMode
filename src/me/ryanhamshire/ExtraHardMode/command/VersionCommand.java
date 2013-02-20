package me.ryanhamshire.ExtraHardMode.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import me.ryanhamshire.ExtraHardMode.ExtraHardMode;
import me.ryanhamshire.ExtraHardMode.service.ICommand;

public class VersionCommand implements ICommand {

   @Override
   public boolean execute(ExtraHardMode plugin, CommandSender sender, Command command, String label, String[] args) {
      sender.sendMessage(ChatColor.GRAY + "========= " + ChatColor.GOLD + plugin.getName() + ChatColor.GRAY + " =========");
      sender.sendMessage(ChatColor.BLUE + "Version: " + ChatColor.WHITE + plugin.getDescription().getVersion());
      sender.sendMessage(ChatColor.BLUE + "Authors:");
      for(String author : plugin.getDescription().getAuthors()) {
         sender.sendMessage(ChatColor.GRAY + " - " + ChatColor.WHITE + author);
      }
      return true;
   }
}
