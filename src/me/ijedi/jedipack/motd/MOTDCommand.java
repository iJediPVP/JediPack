package me.ijedi.jedipack.motd;

import me.ijedi.jedipack.common.MessageTypeEnum;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class MOTDCommand implements CommandExecutor {

    public static final String BASE_COMMAND = "jpmotd";
    private static final String RELOAD_PERM = "jp.motd.reload";
    private static final String RELOAD_ARG = "reload";

    private final ArrayList<String> HELP_LIST = new ArrayList<String>(){{
        add(ChatColor.GREEN + "" + ChatColor.BOLD + "======= " + ChatColor.AQUA + "JediPack MOTD" + ChatColor.GREEN + "" + ChatColor.BOLD + " =======");
        add(ChatColor.AQUA + "/" + BASE_COMMAND + " " + RELOAD_ARG + ChatColor.GREEN + ": Reloads the MOTD configuration.");
    }};

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {

        // Check for player permission
        if(commandSender instanceof Player && !((Player)commandSender).hasPermission(RELOAD_PERM)){
            commandSender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        // Check for "reload" arg
        if(args.length > 0 && args[0].toLowerCase().equals(RELOAD_ARG)){
            MOTDManager.initializeMOTD(true);
            MessageTypeEnum.MOTDMessage.sendMessage("The MOTD configuration has been reloaded.", commandSender, false);
            return true;
        }

        // Else, send help messages
        for(String msg : HELP_LIST){
            commandSender.sendMessage(msg);
        }
        return true;
    }

}
