package me.ijedi.jedipack.signlock;

import me.ijedi.jedipack.JediPackMain;
import me.ijedi.jedipack.common.MessageTypeEnum;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class SignLockManager {

    private static HashMap<UUID, SignLockPlayerInfo> playerInfoMap = new HashMap<>();
    public static ArrayList<Material> LOCKABLE_CONTAINERS = new ArrayList<Material>(){{
        add(Material.CHEST);
        add(Material.TRAPPED_CHEST);
        add(Material.FURNACE);
        add(Material.HOPPER);
        add(Material.DISPENSER);
        add(Material.DROPPER);
    }};


    // Initialize
    public static void initializeSignLocks(){

        if(!JediPackMain.isSignLocksEnabled){
            MessageTypeEnum.SignLockMessage.logMessage("Sign Locks are disabled!");
            return;
        }

        MessageTypeEnum.SignLockMessage.logMessage("Sign Locks are enabled!");

        // Register events
        JavaPlugin plugin = JediPackMain.getThisPlugin();
        plugin.getServer().getPluginManager().registerEvents(new SignLockChangeEvent(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new SignLockBreakEvent(), plugin);

        // Load player data
        for(Player player : Bukkit.getOnlinePlayers()){
            SignLockPlayerInfo info = new SignLockPlayerInfo(player.getUniqueId());
            info.loadPlayerFile();
            playerInfoMap.put(player.getUniqueId(), info);
        }
    }


    // Return the player info for the specified player id
    public static SignLockPlayerInfo getPlayerInfo(UUID playerId){

        // Add a new player info if we don't already have one
        if(playerInfoMap.containsKey(playerId)){
            return playerInfoMap.get(playerId);

        } else {

            SignLockPlayerInfo info = new SignLockPlayerInfo(playerId);
            info.loadPlayerFile();
            playerInfoMap.put(playerId, info);
            return info;
        }
    }

    // Returns true if the location is locked by a sign
    public static boolean isSignLockLocation(Location location){

        for(SignLockPlayerInfo playerInfo : playerInfoMap.values()){
            if(playerInfo.hasLockAtLocation(location, null)){
                return true;
            }
        }
        return false;
    }

    // Return the SignLock from the given location.
    public static SignLock getLockFromLocation(Location location){
        for(SignLockPlayerInfo playerInfo : playerInfoMap.values()){
            if(playerInfo.hasLockAtLocation(location, null)){
                return playerInfo.getLockFromLocation(location, null);
            }
        }
        return null;
    }

    // Remove the given SignLock
    public static void removeSignLock(SignLock lock){
        SignLockPlayerInfo info = getPlayerInfo(lock.getPlayerId());
        info.removeSignLock(lock);
    }
}
