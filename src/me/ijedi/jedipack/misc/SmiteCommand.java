package me.ijedi.jedipack.misc;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class SmiteCommand implements CommandExecutor {

    private static final String SMITE_PERM = "jp.smite";
    public static final String BASE_COMMAND = "jpsmite";

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {

        // Check for permission
        if(!commandSender.hasPermission(SMITE_PERM)){
            commandSender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        // Check for a player name
        if(args.length > 0){
            String pName = args[0];
            Player player = Bukkit.getServer().getPlayer(pName);

            // Make sure they exist
            if(player == null){
                commandSender.sendMessage(ChatColor.RED + "Player with the name '" + pName + "' was not found!");
                return true;
            }

            player.getWorld().spawnEntity(player.getLocation(), EntityType.LIGHTNING);
            commandSender.sendMessage(ChatColor.GREEN + pName + " has been smitten!");
        }

        return true;
    }

}
