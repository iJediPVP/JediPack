package me.ijedi.jedipack.motd;

import me.ijedi.jedipack.JediPackMain;
import me.ijedi.jedipack.common.ConfigHelper;
import me.ijedi.jedipack.common.MessageTypeEnum;
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
      <weather>
    * */

    // Config names
    public static final String DIRECTORY = "motd";
    private static final String CONFIG_NAME = "motdConfig.yml";
    private static final String TOPLINE = "topLine";
    private static final String BOTTOMLINE = "bottomLine";
    private static final String COLOR_SYMBOL = "colorSymbol";
    private static final String WORLD_NAME = "worldForTime";

    // Arguments
    private static final String WORLD_TIME_ARG = "<wt1>";
    private static final String WORLD_TIME24_ARG = "<wt2>";
    private static final String WEATHER_ARG = "<weather>";

    // Current values
    private static World worldForTime;
    private static String topLineMessage;
    private static String bottomLineMessage;
    private static char colorSymbol = '$';
    private static String worldNameForTime;

    // Load MOTD configs.
    public static void initializeMOTD(boolean reload){

        // Check if MOTD is enabled
        if(!JediPackMain.isMotdEnabled){
            MessageTypeEnum.MOTDMessage.logMessage("MOTD is disabled!");
            return;
        }

        MessageTypeEnum.MOTDMessage.logMessage("MOTD is enabled!");
        JavaPlugin plugin = JediPackMain.getThisPlugin();
        plugin.getServer().getPluginManager().registerEvents(new MOTDPingEvent(), plugin);

        // Load the config
        String fileName = ConfigHelper.getFullFilePath(DIRECTORY, CONFIG_NAME);
        File file = ConfigHelper.getFile(fileName);
        FileConfiguration config = ConfigHelper.getFileConfiguration(file);
        loadConfiguration(config, file, reload);
    }

    // Load the MOTD configuration
    private static void loadConfiguration(FileConfiguration configuration, File file, boolean reload){

        // Clear out values for reload
        if(reload){
            worldForTime = null;
            topLineMessage = null;
            bottomLineMessage = null;
            colorSymbol = '$';
            worldNameForTime = null;
        }

        // See if we need to defaults
        if(!configuration.contains(TOPLINE)){
            topLineMessage = "Default Top Line";
            configuration.set(TOPLINE, topLineMessage);

            bottomLineMessage = "Default Bottom Line";
            configuration.set(BOTTOMLINE, bottomLineMessage);

            colorSymbol = '$';
            configuration.set(COLOR_SYMBOL, colorSymbol);

            worldNameForTime = "world";
            configuration.set(WORLD_NAME, worldNameForTime);

            worldForTime = Bukkit.getServer().getWorld(worldNameForTime);

            ConfigHelper.saveFile(file, configuration);

        } else {

            // Read config
            topLineMessage = configuration.getString(TOPLINE);
            bottomLineMessage = configuration.getString(BOTTOMLINE);
            colorSymbol = configuration.getString(COLOR_SYMBOL).toCharArray()[0];
            worldNameForTime = configuration.getString(WORLD_NAME);
            worldForTime = Bukkit.getServer().getWorld(worldNameForTime);

            // Translate colors
            topLineMessage = ChatColor.translateAlternateColorCodes(colorSymbol, topLineMessage);
            bottomLineMessage = ChatColor.translateAlternateColorCodes(colorSymbol, bottomLineMessage);
        }
    }

    // Returns the string to be displayed in the server list.
    public static String getMotd(){
        String motd = String.format("%s\n%s", MOTDManager.topLineMessage, MOTDManager.bottomLineMessage);

        // World times
        // 12 hour time
        if(motd.contains(WORLD_TIME_ARG) && worldForTime != null){
            long worldLong = worldForTime.getTime();
            String formatted = Util.convertWorldTicksToTimeString(worldLong, false);
            motd = motd.replace(WORLD_TIME_ARG, formatted);

        }

        // 24 hour time
        if(motd.contains(WORLD_TIME24_ARG) && worldForTime != null){
            long worldLong = worldForTime.getTime();
            String formatted = Util.convertWorldTicksToTimeString(worldLong, true);
            motd = motd.replace(WORLD_TIME24_ARG, formatted);
        }

        // Weather
        if(motd.contains(WEATHER_ARG) && worldForTime != null){

            String weatherStr = "Sunshine";
            if(worldForTime.hasStorm()){
                weatherStr = "Raining";
            }
            if(worldForTime.isThundering()){
                weatherStr = "Thunder";
            }
            motd = motd.replace(WEATHER_ARG, weatherStr);
        }

        return motd;
    }
}
