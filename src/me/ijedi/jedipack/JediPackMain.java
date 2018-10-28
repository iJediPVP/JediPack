package me.ijedi.jedipack;

import me.ijedi.jedipack.menu.MenuListener;
import me.ijedi.jedipack.misc.SmiteCommand;
import me.ijedi.jedipack.motd.MOTDCommand;
import me.ijedi.jedipack.motd.MOTDManager;
import me.ijedi.jedipack.parkour.ParkourManager;
import me.ijedi.jedipack.signlock.SignLockManager;
import me.ijedi.jedipack.tabmessage.TabMessageCommand;
import me.ijedi.jedipack.tabmessage.TabMessageManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class JediPackMain extends JavaPlugin {

    private static JavaPlugin thisPlugin;

    // Config names
    private static final String PARKOUR_ENABLED = "parkourEnabled";
    private static final String MOTD_ENABLED = "motdEnabled";
    private static final String TABMESSAGE_ENABLED = "tabMessageEnabled";
    private static final String SIGNLOCKS_ENABLED = "signLocksEnabled";

    // Config values
    public static boolean isParkourEnabled, isMotdEnabled, isTabMessageEnabled, isSignLocksEnabled;


    @Override
    public void onEnable(){
        // Executed when the plugin is enabled.
        this.getLogger().info("Enabling JediPack..");
        thisPlugin = this;

        // Initialize all the things
        initConfig();
        getServer().getPluginManager().registerEvents(new MenuListener(this), this);

        // Initialize parkour
        ParkourManager.initializeCourses();

        // Initialize tab messages
        getCommand(TabMessageCommand.BASE_COMMAND).setExecutor(new TabMessageCommand());
        getCommand(TabMessageCommand.BASE_COMMAND).setTabCompleter(new TabMessageCommand());
        TabMessageManager.intializeTabMessages(false);

        // Initialize motd
        getCommand(MOTDCommand.BASE_COMMAND).setExecutor(new MOTDCommand());
        getCommand(MOTDCommand.BASE_COMMAND).setTabCompleter(new MOTDCommand());
        MOTDManager.initializeMOTD(false);

        // Initialize sign locks
        SignLockManager.initializeSignLocks();


        // Initialize misc
        getCommand(SmiteCommand.BASE_COMMAND).setExecutor(new SmiteCommand());

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

    private void initConfig(){
        this.saveDefaultConfig();

        FileConfiguration config = this.getConfig();
        isParkourEnabled = config.getBoolean(PARKOUR_ENABLED);
        isMotdEnabled = config.getBoolean(MOTD_ENABLED);
        isTabMessageEnabled = config.getBoolean(TABMESSAGE_ENABLED);
        isSignLocksEnabled = config.getBoolean(SIGNLOCKS_ENABLED);
    }

}
