package me.ijedi.jedipack.parkour;

import me.ijedi.jedipack.JediPackMain;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
public class ParkourConfiguration {

    private final String CONFIG_NAME = "parkourConfig.yml";
    private final String CONFIGUATIONN_NAME = "ConfigurationName";
    private final String IS_ENABLED = "IsEnabled";

    private String ConfigurationName = "Parkour Configuration";
    private boolean Enabled = true;


    public ParkourConfiguration LoadConfiguration(){
        JavaPlugin plugin = JediPackMain.getThisPlugin();
        ParkourConfiguration config = new ParkourConfiguration();
        File file = new File(plugin.getDataFolder(), CONFIG_NAME);

        // If the file doesn't exist, create it and set the default values.
        if(!file.exists()){
            plugin.saveResource(CONFIG_NAME, true);
            setDefaults(file, plugin);
        }

        // Read the file
        FileConfiguration fileConfig = YamlConfiguration.loadConfiguration(file);
        ConfigurationName = fileConfig.getString(CONFIGUATIONN_NAME);
        Enabled = fileConfig.getBoolean(IS_ENABLED);

        return config;
    }

    // Set the default values of this configuration.
    public void setDefaults(File file, JavaPlugin plugin){
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        // Default values
        config.set(CONFIGUATIONN_NAME, ConfigurationName);
        config.set(IS_ENABLED, true);

        plugin.saveResource(CONFIG_NAME, true);
    }

    // Getters & setters
    public String getConfigurationName() {
        return ConfigurationName;
    }

    public boolean isEnabled() {
        return Enabled;
    }
}
