package me.ijedi.jedipack.test;

import me.ijedi.jedipack.JediPackMain;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class PingEnableCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        JavaPlugin plugin = JediPackMain.getThisPlugin();
        FileConfiguration config = plugin.getConfig();

        if(config.getBoolean("pingCommandEnabled")){
            config.set("pingCommandEnabled", false);
            commandSender.sendMessage("The Ping command has been disabled. Use /reload for it to take effect.");
        }
        else{
            config.set("pingCommandEnabled", true);
            commandSender.sendMessage("The Ping command has been enabled. Use /reload for it to take effect.");
        }
        plugin.saveConfig();

        return true;
    }
}
