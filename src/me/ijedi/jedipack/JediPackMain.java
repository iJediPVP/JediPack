package me.ijedi.jedipack;

import me.ijedi.jedipack.initialize.CommandInitializer;
import me.ijedi.jedipack.parkour.ParkourConfiguration;
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

        ParkourConfiguration config = new ParkourConfiguration();
        config.LoadConfiguration(this);
        System.out.println(config.getConfigurationName());

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
