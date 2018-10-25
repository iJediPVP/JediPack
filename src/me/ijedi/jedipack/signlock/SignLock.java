package me.ijedi.jedipack.signlock;

import me.ijedi.jedipack.common.MessageTypeEnum;
import me.ijedi.jedipack.common.Util;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class SignLock {

    private UUID lockId;
    private UUID playerId;
    private Location lockLocation;
    private int lockNumber;
    private ArrayList<UUID> sharedIds = new ArrayList<>();

    public SignLock(UUID lockId, Location lockLocation, int lockNumber, UUID playerId){
        this.lockId = lockId;
        this.lockLocation = lockLocation;
        this.lockNumber = lockNumber;
        this.playerId = playerId;
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

    // Write lock to config file
    public void writeToConfig(FileConfiguration configuration, File file){

        String lockKey = lockId.toString();
        String lockPath = SignLockPlayerInfo.LOCKS + "." + lockKey + ".";

        configuration.set(lockPath + SignLockPlayerInfo.WORLDID, lockLocation.getWorld().getUID().toString());
        configuration.set(lockPath + SignLockPlayerInfo.X, lockLocation.getX());
        configuration.set(lockPath + SignLockPlayerInfo.Y, lockLocation.getY());
        configuration.set(lockPath + SignLockPlayerInfo.Z, lockLocation.getZ());
        configuration.set(lockPath + SignLockPlayerInfo.LOCK_NUM, lockNumber);

        try{
            configuration.save(file);
        } catch (IOException e) {
            String message = String.format("Error saving configuration file for lock $s.", lockKey);
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
        return playerId == this.playerId;
    }

    // Return true if given player id can open this locked container.
    public boolean hasContainerAccess(UUID playerId){
        if(playerId == this.playerId || sharedIds.contains(playerId)){
            return true;
        }
        return false;
    }

}
