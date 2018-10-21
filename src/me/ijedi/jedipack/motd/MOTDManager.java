package me.ijedi.jedipack.motd;

import me.ijedi.jedipack.JediPackMain;
import me.ijedi.jedipack.common.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class MOTDManager {

    /*
    * Arguments:
      <wt1> - 12 Hour world time
      <wt2> - 24 Hour world time
    * */

    // Config names
    private static final String CONFIG_NAME = "motdConfig.yml";
    private static final String ENABLED = "enabled";
    private static final String TOPLINE = "topLine";
    private static final String BOTTOMLINE = "bottomLine";
    private static final String COLOR_SYMBOL = "colorSymbol";
    private static final String WORLD_NAME = "worldForTime";

    // Arguments
    private static final String WORLD_TIME_ARG = "<wt1>";
    private static final String WORLD_TIME24_ARG = "<wt2>";

    // Configs
    private static FileConfiguration motdConfiguration;
    private static File motdFile;

    // Current values
    public static boolean isEnabled;
    private static String topLineMessage;
    private static String bottomLineMessage;
    private static char colorSymbol = '$';
    private static String worldNameForTime;

    // Load MOTD configs.
    public static void initializeMOTD(boolean reload){

        // Load configs
        motdFile = getFile();
        motdConfiguration = getFileConfiguration(reload);

        if(!isEnabled){
            JediPackMain.getThisPlugin().getLogger().info("JediPack MOTD is disabled.");
            return;
        }

        JediPackMain.getThisPlugin().getLogger().info("JediPack MOTD is enabled.");
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
    private static FileConfiguration getFileConfiguration(boolean reload){

        if(motdConfiguration != null && !reload){
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

            colorSymbol = '$';
            config.set(COLOR_SYMBOL, colorSymbol);

            worldNameForTime = "world";
            config.set(WORLD_NAME, worldNameForTime);

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
            colorSymbol = config.getString(COLOR_SYMBOL).toCharArray()[0];
            worldNameForTime = config.getString(WORLD_NAME);

            // Translate colors
            topLineMessage = ChatColor.translateAlternateColorCodes(colorSymbol, topLineMessage);
            bottomLineMessage = ChatColor.translateAlternateColorCodes(colorSymbol, bottomLineMessage);

            return config;
        }
    }

    // Format the given string for motd log messages
    public static String formatMOTDLogString(String str, boolean isError){
        String finalString = ChatColor.GREEN + "" + ChatColor.BOLD + "[MOTD] ";
        if(isError){
            finalString += ChatColor.RED;
        }
        finalString += str;
        return finalString;
    }

    // Returns the formatted MOTD string.
    public static String getMotd(){
        String motd = String.format("%s\n%s", MOTDManager.topLineMessage, MOTDManager.bottomLineMessage);

        // World times
        if(motd.contains(WORLD_TIME_ARG) && !Util.IsNullOrEmpty(worldNameForTime)){
            // 12 hour time
            World worldForTime = Bukkit.getWorld(worldNameForTime);

            if(worldForTime != null){
                long worldLong = worldForTime.getTime();
                String formatted = Util.convertWorldTicksToTimeString(worldLong, false);
                motd = motd.replace(WORLD_TIME_ARG, formatted);
            }

        } else if(motd.contains(WORLD_TIME24_ARG) && !Util.IsNullOrEmpty(worldNameForTime)){
            // 24 hour time
            World worldForTime = Bukkit.getWorld(worldNameForTime);

            if(worldForTime != null){
                long worldLong = worldForTime.getTime();
                String formatted = Util.convertWorldTicksToTimeString(worldLong, true);
                motd = motd.replace(WORLD_TIME24_ARG, formatted);
            }
        }

        return motd;
    }
}
