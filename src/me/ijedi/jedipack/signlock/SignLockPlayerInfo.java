package me.ijedi.jedipack.signlock;

import me.ijedi.jedipack.JediPackMain;
import me.ijedi.jedipack.common.MessageTypeEnum;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class SignLockPlayerInfo {

    /* Config mark up
    signLocks/playerId.yml
      '<lockIdHere>':
        lockNum:
        x:
        y:
        z:
        worldId: ''
        shared:
          - '<p1Guid>'
          - '<p2Guid>'
    * */

    public static final String LOCKS = "locks";
    public static final String LOCK_NUM = "lockNum";
    public static final String X = "x";
    public static final String Y = "y";
    public static final String Z = "z";
    public static final String WORLDID = "worldId";
    public static final String SHARED = "shared";
    public static final String HOPPERS = "hoppersEnabled";

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

                        int lockNumber = lockInfoSection.getInt(LOCK_NUM);
                        List<String> shared = lockInfoSection.getStringList(SHARED);
                        boolean hoppersEnabled = lockInfoSection.getBoolean(HOPPERS);

                        SignLock newLock = addNewLock(lockId, lockNumber, lockLocation, hoppersEnabled, false);
                        newLock.setSharedIds(shared);
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
    public SignLock addNewLock(UUID lockId, int lockNumber, Location lockLocation, boolean hoppersEnabled, boolean save){

        if(lockId == null){
            lockId = UUID.randomUUID();
        }

        if(lockNumber == 0){
            lockNumber = getNextSignLockNumber();
        }

        SignLock newLock = new SignLock(lockId, lockLocation, lockNumber, playerId, hoppersEnabled);
        signLocks.put(newLock.getLockId(), newLock);

        // Write lock to config
        if(save){
            newLock.writeToConfig(fileConfiguration, file);
        }

        return newLock;
    }

    // Returns if the player has a lock at this location.
    public boolean hasLockAtLocation(Location placedSignLocation, Location placedOnLocation){

        for(SignLock lock : signLocks.values()){

            // Check locations
            if(lock.isLockedLocation(placedSignLocation)){
                return true;
            }

            if(placedOnLocation != null && lock.isLockedLocation(placedOnLocation)){
                return true;
            }
        }
        return false;
    }

    // Returns lock based on the given locations
    public SignLock getLockFromLocation(Location placedSignLocation, Location placedOnLocation){

        for(SignLock lock : signLocks.values()){

            // Check locations
            if(lock.isLockedLocation(placedSignLocation)){
                return lock;
            }

            if(placedOnLocation != null && lock.isLockedLocation(placedOnLocation)){
                return lock;
            }
        }
        return null;
    }

    // Return the sign locks for this player.
    public HashMap<UUID, SignLock> getSignLocks(){
        return signLocks;
    }

    // Get the number for the next sign lock.
    public int getNextSignLockNumber(){
        int maxNumber = 0;
        for(SignLock lock : signLocks.values()){
            if(lock.getLockNumber() > maxNumber){
                maxNumber = lock.getLockNumber();
            }
        }
        return maxNumber + 1;
    }

    // Remove the given sign lock
    public void removeSignLock(SignLock lockToRemove){

        signLocks.remove(lockToRemove.getLockId());

        // Update the config file
        fileConfiguration.set(LOCKS, new String[0]);
        for(SignLock lock : signLocks.values()){
            lock.writeToConfig(fileConfiguration, file);
        }

        try{
            fileConfiguration.save(file);
        } catch (IOException e) {
            String message = String.format("Error saving configuration file for player $s.", playerId.toString());
            MessageTypeEnum.SignLockMessage.logMessage(message);
            MessageTypeEnum.SignLockMessage.logMessage(e.toString());
        }

    }

    // Return the sign lock with he specified lock number
    public SignLock getLockByNumber(int lockNumber){
        for(SignLock lock : signLocks.values()){
            if(lock.getLockNumber() == lockNumber){
                return lock;
            }
        }
        return null;
    }

    // Add a player to the sign lock
    public void addSharedPlayedToLock(SignLock lock, UUID playerId){
        lock.addSharedPlayer(playerId, fileConfiguration, file);
    }

    // Remove access from a player for the given sign lock
    public void removeSharedPlayerFromLock(SignLock lock, UUID playerId){
        lock.removeSharedPlayer(playerId, fileConfiguration, file);
    }

    // Toggle if hoppers are enabled for the given lock
    public boolean toggleHoppersForLock(SignLock lock){
        return lock.toggleHoppers(fileConfiguration, file);
    }

}
