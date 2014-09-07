package com.extrahardmode.command;


import com.extrahardmode.ExtraHardMode;
import com.extrahardmode.service.ICommand;
import com.extrahardmode.service.PermissionNode;
import com.extrahardmode.task.RemoveExposedTorchesTask;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Diemex
 */
public class DebugCommand implements ICommand
{
    @Override
    public boolean execute(ExtraHardMode plugin, CommandSender sender, Command command, String label, String[] args)
    {
        if (sender.hasPermission(PermissionNode.ADMIN.getNode()))
        {
            if (sender instanceof Player)
            {
                Player player = (Player) sender;
                if (args.length > 0)
                {
                    if (args[0].equals("RemoveTorches"))
                    {
                        plugin.getServer().getScheduler().runTask(plugin, new RemoveExposedTorchesTask(plugin, player.getLocation().getChunk(), true));
                        sender.sendMessage(ChatColor.GREEN + plugin.getTag() + "Removed Torches and Crops in the current chunk!");
                    }
                    else if (args[0].equals("logging"))
                    {
	                    boolean debug = false;
	                    if(args.length < 2)
	                    	debug = true;
	                    else
	                    {
		                    try{
		                    	debug = Boolean.parseBoolean(args[1]);
		                    } catch(Exception e)
		                    {
		                    	sender.sendMessage(ChatColor.RED + plugin.getTag() + "Second argument must be 'true' or 'false'!");
		                    }
	                    }
	                    ExtraHardMode.debug = debug;
	                    sender.sendMessage(ChatColor.GOLD + plugin.getTag() + "Set debug logging to: "+debug+"!");
                    }
                    return true;
                } else
                {
                    sender.sendMessage(ChatColor.RED + plugin.getTag() + " You need to specify what you want to debug!");
                    sender.sendMessage(ChatColor.RED + plugin.getTag() + " Available methods \"RemoveTorches\"");
                }
            } else
                sender.sendMessage(ChatColor.RED + plugin.getTag() + "You need to be in game to use debugging functionality!");
        } else
        {
            sender.sendMessage(ChatColor.RED + plugin.getTag() + " Lack permission: " + PermissionNode.ADMIN.getNode());
        }
        return false;
    }
}
