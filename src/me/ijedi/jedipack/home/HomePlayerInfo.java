package me.ijedi.jedipack.home;

import me.ijedi.jedipack.common.MessageTypeEnum;
import me.ijedi.jedipack.common.Util;
import me.ijedi.jedipack.common.confighelper.ConfigHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;

public class HomePlayerInfo {

    /* <playerId>.yml
    File Layout:
    homeName:
        worldId:
        x:
        y:
        z:
    * */

    // Config paths
    private static final String WORLD_ID = "worldId";
    private static final String X = "x";
    private static final String Y = "y";
    private static final String Z = "z";

    // Class fields
    private UUID playerId;
    private HashMap<String, HomeInfo> homeInfos = new HashMap<>();

    public HomePlayerInfo(UUID playerId){
        this.playerId = playerId;
    }

    // Returns the config name for this player.
    private String getConfigName(){
        return playerId.toString() + ".yml";
    }

    // Load the player info from the player file.
    public void loadPlayerInfo(){

        // Get the file
        String fileName = ConfigHelper.getFullFilePath(HomeManager.DIRECTORY, getConfigName());
        File file = ConfigHelper.getFile(fileName);
        FileConfiguration config = ConfigHelper.getFileConfiguration(file);

        // Read it
        for(String homeName : config.getConfigurationSection("").getKeys(false)){

            String worldIdStr = config.getString(homeName + "." + WORLD_ID);
            UUID worldId = UUID.fromString(worldIdStr);
            double x = config.getDouble(homeName + "." + X);
            double y = config.getDouble(homeName + "." + Y);
            double z = config.getDouble(homeName + "." + Z);
            Location location = new Location(Bukkit.getWorld(worldId), x, y, z);

            HomeInfo info = new HomeInfo(homeName, location);
            homeInfos.put(homeName, info);
        }
    }

    // Add a home for the given name and location.
    public void addHome(String homeName, Location location){
        homeName = homeName.toLowerCase();
        location = new Location(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
        location = Util.getCenteredBlockLocation(location);

        // Get the file
        String fileName = ConfigHelper.getFullFilePath(HomeManager.DIRECTORY, getConfigName());
        File file = ConfigHelper.getFile(fileName);
        FileConfiguration config = ConfigHelper.getFileConfiguration(file);

        // Write the info
        config.set(homeName + "." + WORLD_ID, location.getWorld().getUID().toString());
        config.set(homeName + "." + X, location.getX());
        config.set(homeName + "." + Y, location.getY());
        config.set(homeName + "." + Z, location.getZ());
        ConfigHelper.saveFile(file, config);

        HomeInfo info = new HomeInfo(homeName, location);
        homeInfos.put(homeName, info);
    }

    // Remove the specified home
    public void removeHome(String homeName){
        homeName = homeName.toLowerCase();

        // Get the file
        String fileName = ConfigHelper.getFullFilePath(HomeManager.DIRECTORY, getConfigName());
        File file = ConfigHelper.getFile(fileName);
        FileConfiguration config = ConfigHelper.getFileConfiguration(file);

        // Remove
        config.set(homeName, null);
        ConfigHelper.saveFile(file, config);

        homeInfos.remove(homeName);
    }

    // Returns true if the player has set the given home name
    public boolean hasHome(String homeName){
        homeName = homeName.toLowerCase();
        return homeInfos.containsKey(homeName);
    }

    // Returns the number of homes this player has set.
    public int getHomeCount(){
        return homeInfos.size();
    }

    // Teleports the player to the specified home.
    public void teleportHome(Player player, String homeName){
        homeName = homeName.toLowerCase();

        // If only one home is set, send them there, else send them to the specified home.
        boolean sendToFirst = getHomeCount() == 1;
        for(HomeInfo info : homeInfos.values()){
            if(sendToFirst || info.getHomeName().equals(homeName)){
                player.teleport(info.getHomeLocation());
                if(Util.isNullOrEmpty(homeName)){
                    homeName = "home";
                }
                MessageTypeEnum.HomeMessage.sendMessage("Teleported to '" + homeName + "'!", player, false);
                return;
            }
        }
    }

}
