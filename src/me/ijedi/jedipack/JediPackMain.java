package me.ijedi.jedipack;

import me.ijedi.jedipack.menu.MenuListener;
import me.ijedi.jedipack.motd.MOTDManager;
import me.ijedi.jedipack.parkour.ParkourManager;
import me.ijedi.jedipack.tabmessage.TabMessageCommand;
import me.ijedi.jedipack.tabmessage.TabMessageManager;
import org.bukkit.plugin.java.JavaPlugin;

public class JediPackMain extends JavaPlugin {

    private static JavaPlugin thisPlugin;
    public static final String PARKOUR_ENABLED = "parkourEnabled";

    @Override
    public void onEnable(){
        // Executed when the plugin is enabled.
        this.getLogger().info("Enabling JediPack..");
        thisPlugin = this;

        // Initialize all the things
        this.saveDefaultConfig();

        getServer().getPluginManager().registerEvents(new MenuListener(this), this);

        // Initialize parkour
        ParkourManager.initializeCourses();

        // Initialize tab messages
        getCommand(TabMessageCommand.BASE_COMMAND).setExecutor(new TabMessageCommand());
        TabMessageManager.intializeTabMessages(false);

        // Initialize motd
        MOTDManager.initializeMotd();

        this.getLogger().info("JediPack is enabled!");
    }

    @Override
    public void onDisable(){
        // Executed when the plugin is disabled.
        this.getLogger().info("Disabling JediPack..");

        this.getLogger().info("JediPack is disabled!");
    }

    public static JavaPlugin getThisPlugin() {
        return thisPlugin;
    }

}
