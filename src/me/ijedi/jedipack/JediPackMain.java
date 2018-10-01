package me.ijedi.jedipack;

import me.ijedi.jedipack.initialize.CommandInitializer;
import org.bukkit.plugin.java.JavaPlugin;

public class JediPackMain extends JavaPlugin {

    private static JavaPlugin ThisPlugin;

    @Override
    public void onEnable(){
        // Executed when the plugin is enabled.
        this.getLogger().info("Enabling JediPack..");
        ThisPlugin = this;

        // Initialize commands
        CommandInitializer.RegisterCommands(this);
        this.saveDefaultConfig();

        this.getLogger().info("JediPack is enabled!");
    }

    @Override
    public void onDisable(){
        // Executed when the plugin is disabled.
        this.getLogger().info("Disabling JediPack..");

        this.getLogger().info("JediPack is disabled!");
    }

    public static JavaPlugin getThisPlugin() {
        return ThisPlugin;
    }
}
