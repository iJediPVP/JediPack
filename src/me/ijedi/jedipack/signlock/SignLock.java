package me.ijedi.jedipack.signlock;

import me.ijedi.jedipack.common.MessageTypeEnum;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class SignLock {

    private UUID lockId;
    private Location lockLocation;
    private int lockNumber;
    private ArrayList<UUID> sharedIds = new ArrayList<>();

    public SignLock(UUID lockId, Location lockLocation, int lockNumber){
        this.lockId = lockId;
        this.lockLocation = lockLocation;
        this.lockNumber = lockNumber;
    }

    public UUID getLockId() {
        return lockId;
    }

    public Location getLockLocation(){
        return lockLocation;
    }

    public int getLockNumber() {
        return lockNumber;
    }

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
}
