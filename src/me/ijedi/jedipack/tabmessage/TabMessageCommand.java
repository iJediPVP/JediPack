package me.ijedi.jedipack.tabmessage;

import me.ijedi.jedipack.common.MessageTypeEnum;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class TabMessageCommand implements CommandExecutor {

    public static final String BASE_COMMAND = "tabmessage";
    private static final String RELOAD_PERM = "jp.tabmessage.reload";
    private static final String RELOAD_ARG = "reload";

    private final ArrayList<String> HELP_LIST = new ArrayList<String>(){{
        add(ChatColor.GREEN + "" + ChatColor.BOLD + "======= " + ChatColor.AQUA + "JediPack TabMessage" + ChatColor.GREEN + "" + ChatColor.BOLD + " =======");
        add(ChatColor.AQUA + "/" + BASE_COMMAND + " " + RELOAD_ARG + ChatColor.GREEN + ": Reloads the tab message configuration.");
    }};

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {

        // Check for player permission
        if(!commandSender.hasPermission(RELOAD_PERM)){
            MessageTypeEnum.TabMessage.sendMessage(ChatColor.RED + "You do not have permission to use this command.", commandSender, true);
            return true;
        }

        // Check for "reload" arg
        if(args.length > 0 && args[0].toLowerCase().equals(RELOAD_ARG)){
            TabMessageManager.intializeTabMessages(true);
            MessageTypeEnum.TabMessage.sendMessage("The TabMessage configuration has been reloaded.", commandSender, false);
            return true;
        }

        // Else, send help messages
        for(String msg : HELP_LIST){
            commandSender.sendMessage(msg);
        }
        return true;
    }

}
