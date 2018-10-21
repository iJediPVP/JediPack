package me.ijedi.jedipack.motd;

import me.ijedi.jedipack.JediPackMain;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MOTDManager {

    private static final String CONFIG_NAME = "motdConfig.yml";
    private static final String ENABLED = "enabled";
    private static final String TOPLINE = "topLine";
    private static final String BOTTOMLINE = "bottomLine";

    // Configs
    private static FileConfiguration motdConfiguration;
    private static File motdFile;

    // Current values
    public static boolean isEnabled;
    public static String topLineMessage;
    public static String bottomLineMessage;

    public static void initializeMotd(){

        // Load configs
        motdFile = getFile();
        motdConfiguration = getFileConfiguration();

        if(!isEnabled){
            JediPackMain.getThisPlugin().getLogger().info("JediPack MOTD is disabled.");
            return;
        }

        JediPackMain.getThisPlugin().getLogger().info("JediPack MOTD is disabled.");
        JavaPlugin plugin = JediPackMain.getThisPlugin();
        plugin.getServer().getPluginManager().registerEvents(new MOTDPingEvent(), plugin);
    }


    // Get the File object for the MOTDManager.
    private static File getFile(){

        if(motdFile != null){
            return motdFile;
        }

        String folder = JediPackMain.getThisPlugin().getDataFolder() + "/motd";
        String fileName = CONFIG_NAME;
        File configFile = new File(folder, fileName);

        return configFile;
    }

    // Get the FileConfiguration object for the MOTDManager
    private static FileConfiguration getFileConfiguration(){

        if(motdConfiguration != null){
            return motdConfiguration;
        }

        // Create the file if it doesn't exist.
        if(!motdFile.exists()){
            motdFile.getParentFile().mkdirs();
            FileConfiguration config = YamlConfiguration.loadConfiguration(motdFile);

            // Defaults
            isEnabled = false;
            config.set(ENABLED, isEnabled);

            topLineMessage = "Default Top Line";
            config.set(TOPLINE, topLineMessage);

            bottomLineMessage = "Default Bottom Line";
            config.set(BOTTOMLINE, bottomLineMessage);

            try{
                config.save(motdFile);
            }
            catch(IOException e){
                String errorMessage = "JediPack MOTD - Error saving configuration file.";
                JediPackMain.getThisPlugin().getLogger().info(errorMessage);
                JediPackMain.getThisPlugin().getLogger().info(e.toString());
            }
            return config;

        } else {
            FileConfiguration config = YamlConfiguration.loadConfiguration(motdFile);

            // Read config
            isEnabled = config.getBoolean(ENABLED);
            topLineMessage = config.getString(TOPLINE);
            bottomLineMessage = config.getString(BOTTOMLINE);

            return config;
        }
    }
}
