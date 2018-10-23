package me.ijedi.jedipack.signlock;

import me.ijedi.jedipack.JediPackMain;
import me.ijedi.jedipack.common.MessageTypeEnum;
import me.ijedi.jedipack.common.Util;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class SignLockPlayerInfo {

    /* Config mark up
    signLocks/playerId.yml
      '<lockIdHere>':
        x:
        y:
        z:
        worldId: ''
        shared:
          - '<p1Guid>'
          - '<p2Guid>'
    * */

    public static final String LOCKS = "locks";
    public static final String X = "x";
    public static final String Y = "y";
    public static final String Z = "z";
    public static final String WORLDID = "worldId";

    private UUID playerId;
    private HashMap<UUID, SignLock> signLocks = new HashMap<>();
    private File file;
    private FileConfiguration fileConfiguration;

    public SignLockPlayerInfo(UUID playerId){
        this.playerId = playerId;
    }

    // Load player file
    public void loadPlayerFile(){

        file = new File(getPlayerFileName());

        // Create file and set defaults if it doesn't exist
        if(!file.exists()){
            file.getParentFile().mkdirs();
            fileConfiguration = YamlConfiguration.loadConfiguration(file);

            fileConfiguration.set(LOCKS, new String[0]);

            try{
                fileConfiguration.save(file);
            } catch (IOException e){
                MessageTypeEnum.SignLockMessage.logMessage("Error saving configuration file for player: " + playerId.toString());
                MessageTypeEnum.SignLockMessage.logMessage(e.toString());
            }
        } else {

            // Load lock info from the file
            fileConfiguration = YamlConfiguration.loadConfiguration(file);

            ConfigurationSection lockSection = fileConfiguration.getConfigurationSection(LOCKS);
            if(lockSection != null){
                for(String lockKey : lockSection.getKeys(false)){

                    ConfigurationSection lockInfoSection = lockSection.getConfigurationSection(lockKey);
                    if(lockInfoSection != null){

                        UUID lockId = UUID.fromString(lockKey);
                        double x = lockInfoSection.getDouble(X);
                        double y = lockInfoSection.getDouble(Y);
                        double z = lockInfoSection.getDouble(Z);
                        String worldIdStr = lockInfoSection.getString(WORLDID);
                        UUID worldId = UUID.fromString(worldIdStr);
                        World world = Bukkit.getServer().getWorld(worldId);
                        Location lockLocation = new Location(world, x, y, z);

                        addNewLock(lockId, lockLocation, false);
                    }

                }
            }

        }
    }

    // Returns the file name for this player's file.
    private String getPlayerFileName(){
        return String.format("%s/%s/%s.yml", JediPackMain.getThisPlugin().getDataFolder(), "signLocks", playerId);
    }


    // Add a new lock for this player.
    public void addNewLock(UUID lockId, Location lockLocation, boolean save){

        if(!hasLockAtLocation(lockLocation)){

            if(lockId == null){
                lockId = UUID.randomUUID();
            }

            SignLock newLock = new SignLock(lockId, lockLocation);
            signLocks.put(newLock.getLockId(), newLock);

            // Write lock to config
            if(save){
                newLock.writeToConfig(fileConfiguration, file);
            }
        }

    }

    // Returns if the player has a lock at this location.
    public boolean hasLockAtLocation(Location lockLocation){

        for(SignLock lock : signLocks.values()){
            Location exisitngLoc = lock.getLockLocation();
            if(Util.DoLocationsEqual(lockLocation, exisitngLoc, false)) {
                return true;
            }
        }
        return false;
    }

    // Return the sign locks for this player.
    public HashMap<UUID, SignLock> getSignLocks(){
        return signLocks;
    }

}
