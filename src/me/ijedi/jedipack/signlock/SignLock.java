package me.ijedi.jedipack.signlock;

import me.ijedi.jedipack.common.MessageTypeEnum;
import me.ijedi.jedipack.common.Util;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SignLock {

    private UUID lockId;
    private UUID playerId;
    private Location lockLocation;
    private int lockNumber;
    private List<String> sharedIds = new ArrayList<>();
    private boolean hoppersEnabled = false;

    public SignLock(UUID lockId, Location lockLocation, int lockNumber, UUID playerId, boolean hoppersEnabled){
        this.lockId = lockId;
        this.lockLocation = lockLocation;
        this.lockNumber = lockNumber;
        this.playerId = playerId;
        this.hoppersEnabled = hoppersEnabled;
    }

    // Getters
    public UUID getLockId() {
        return lockId;
    }

    public Location getLockLocation(){
        return lockLocation;
    }

    public int getLockNumber() {
        return lockNumber;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public boolean isHoppersEnabled() {
        return hoppersEnabled;
    }

    // Setters
    public void setSharedIds(List<String> sharedIds){
        this.sharedIds = sharedIds;
    }

    public void setHoppersEnabled(boolean hoppersEnabled){
        this.hoppersEnabled = hoppersEnabled;
    }


    // Write lock to config file
    public void writeToConfig(FileConfiguration configuration, File file){

        if(!file.exists()){
            file.mkdirs();
        }

        String lockPath = getLockPath();

        configuration.set(lockPath + SignLockPlayerInfo.WORLDID, lockLocation.getWorld().getUID().toString());
        configuration.set(lockPath + SignLockPlayerInfo.X, lockLocation.getX());
        configuration.set(lockPath + SignLockPlayerInfo.Y, lockLocation.getY());
        configuration.set(lockPath + SignLockPlayerInfo.Z, lockLocation.getZ());
        configuration.set(lockPath + SignLockPlayerInfo.LOCK_NUM, lockNumber);
        configuration.set(lockPath + SignLockPlayerInfo.SHARED, new String[0]);
        configuration.set(lockPath + SignLockPlayerInfo.HOPPERS, false);
        save(configuration, file);
    }

    // Returns the config path to this lock
    private String getLockPath(){
        String lockKey = lockId.toString();
        String lockPath = SignLockPlayerInfo.LOCKS + "." + lockKey + ".";
        return lockPath;
    }

    // Save this lock to a file
    private void save(FileConfiguration configuration, File file){
        try{
            configuration.save(file);
        } catch (IOException e) {
            String message = String.format("Error saving configuration file for lock $s.", lockId.toString());
            MessageTypeEnum.SignLockMessage.logMessage(message);
            MessageTypeEnum.SignLockMessage.logMessage(e.toString());
        }
    }

    // Return true if the location of this lock matches the given location.
    public boolean isLockedLocation(Location testLocation){
        // Get the block locked by the sign
        Block lockedContainer = Util.getBlockFromPlacedSign(lockLocation.getBlock());
        Location lockedLocation = Util.centerSignLockLocation(lockedContainer.getLocation());

        if(Util.DoLocationsEqual(testLocation, lockLocation, false)
                ||Util.DoLocationsEqual(testLocation, lockedLocation, false)) {
            return true;
        }

        return false;
    }

    // Return true if given player id can break this lock or container.
    public boolean hasBreakableAccess(UUID playerId){
        return playerId.equals(this.playerId);
    }

    // Return true if given player id can open this locked container.
    public boolean hasContainerAccess(UUID playerId){
        if(playerId.equals(this.playerId) || sharedIds.contains(playerId.toString())){
            return true;
        }
        return false;
    }

    // Give the given player id access to this lock.
    public void addSharedPlayer(UUID playerId, FileConfiguration configuration, File file){

        if(hasContainerAccess(playerId)){
            return;
        }
        sharedIds.add(playerId.toString());

        String lockPath = getLockPath();
        configuration.set(lockPath + SignLockPlayerInfo.SHARED, sharedIds.toArray(new String[sharedIds.size()]));
        save(configuration, file);
    }

    // Remove access from this lock for the given player id.
    public void removeSharedPlayer(UUID playerId, FileConfiguration configuration, File file){

        if(!hasContainerAccess(playerId)){
            return;
        }
        sharedIds.remove(playerId.toString());

        String lockPath = getLockPath();
        configuration.set(lockPath + SignLockPlayerInfo.SHARED, sharedIds.toArray(new String[sharedIds.size()]));

        try{
            configuration.save(file);
        } catch (IOException e) {
            String message = String.format("Error saving configuration file for lock $s.", lockId.toString());
            MessageTypeEnum.SignLockMessage.logMessage(message);
            MessageTypeEnum.SignLockMessage.logMessage(e.toString());
        }
    }

    // Returns if hoppers are enabled.
    public boolean areHoppersEnabled(){
        return hoppersEnabled;
    }

    // Toggle if hoppers are enabled or not.
    public boolean toggleHoppers(FileConfiguration configuration, File file){

        hoppersEnabled = !hoppersEnabled;

        String lockPath = getLockPath();
        configuration.set(lockPath + SignLockPlayerInfo.HOPPERS, hoppersEnabled);

        save(configuration, file);

        return hoppersEnabled;
    }
}
