package me.ijedi.jedipack.back;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.List;

public class BackCommand implements TabExecutor {

    public static final String BASE_COMMAND = "back";
    private final String BACK_PERM = "jp.back";

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {

        /*
        tp event
        death event
        * */

        // Check perms
        if(!commandSender.hasPermission(BACK_PERM)){
            commandSender.sendMessage("You need permission to use this command.");
            return true;
        }

        // Require a player
        if(!(commandSender instanceof Player)){
            commandSender.sendMessage("Only a player can run this command.");
            return true;
        }

        // Make sure the player has a back location
        Player player = (Player) commandSender;
        if(!BackManager.hasBackLocation(player.getUniqueId())){
            player.sendMessage(ChatColor.RED + "You have no location to go back to.");

        } else {

            Location location = BackManager.getBackLocation(player.getUniqueId());
            player.teleport(location);
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return null;
    }
}
