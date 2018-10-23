package me.ijedi.jedipack.signlock;

import me.ijedi.jedipack.common.MessageTypeEnum;
import org.bukkit.Location;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class SignLock {

    private UUID lockId;
    private Location lockLocation;
    private ArrayList<UUID> sharedIds = new ArrayList<>();

    public SignLock(UUID lockId, Location lockLocation){
        this.lockId = lockId;
        this.lockLocation = lockLocation;
    }

    public UUID getLockId() {
        return lockId;
    }

    public Location getLockLocation(){
        return lockLocation;
    }


    public void writeToConfig(FileConfiguration configuration, File file){

        String lockKey = lockId.toString();
        String lockPath = SignLockPlayerInfo.LOCKS + "." + lockKey + ".";

        configuration.set(lockPath + SignLockPlayerInfo.WORLDID, lockLocation.getWorld().getUID().toString());
        configuration.set(lockPath + SignLockPlayerInfo.X, lockLocation.getX());
        configuration.set(lockPath + SignLockPlayerInfo.Y, lockLocation.getY());
        configuration.set(lockPath + SignLockPlayerInfo.Z, lockLocation.getZ());

        try{
            configuration.save(file);
        } catch (IOException e) {
            String message = String.format("Error saving configuration file for lock $s.", lockKey);
            MessageTypeEnum.SignLockMessage.logMessage(message);
            MessageTypeEnum.SignLockMessage.logMessage(e.toString());
        }
    }
}
