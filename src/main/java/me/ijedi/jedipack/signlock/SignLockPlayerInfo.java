package me.ijedi.jedipack.signlock;

import me.ijedi.jedipack.common.ConfigHelper;
import me.ijedi.jedipack.common.MessageTypeEnum;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
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

    private static final String DIRECTORY = "signLocks";

    private UUID playerId;
    private HashMap<UUID, SignLock> signLocks = new HashMap<>();

    public SignLockPlayerInfo(UUID playerId){
        this.playerId = playerId;
    }


    // Load player file
    public void loadPlayerFile(){

        // Get the player file
        String fileName = ConfigHelper.getFullFilePath(DIRECTORY, getPlayerFileName());
        File file = ConfigHelper.getFile(fileName);
        FileConfiguration config = ConfigHelper.getFileConfiguration(file);

        ConfigurationSection lockSection = config.getConfigurationSection(LOCKS);
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

    // Returns the file name for this player's file.
    private String getPlayerFileName(){
        return String.format("%s.yml", playerId);
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
            // Get the player file
            String fileName = ConfigHelper.getFullFilePath(DIRECTORY, getPlayerFileName());
            File file = ConfigHelper.getFile(fileName);
            FileConfiguration config = ConfigHelper.getFileConfiguration(file);
            newLock.writeToConfig(config, file);
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

        // Get all of the used numbers
        ArrayList<Integer> usedNumbers = new ArrayList<>();
        int maxNumber = 0;
        for(SignLock lock : signLocks.values()){
            int lockNum = lock.getLockNumber();
            usedNumbers.add(lockNum);
            maxNumber = lockNum > maxNumber ? lockNum : maxNumber;
        }

        // Get the actual sum of the used numbers
        int totalSum = 0;
        for(int x : usedNumbers){
            totalSum += x;
        }

        // If expected - total is not 0, then return the missing number.
        int expectedSum = (maxNumber * (1 + maxNumber)) / 2;
        int diff = expectedSum - totalSum;
        if(diff > 0){
            return diff;
        }

        // Else return the next number.
        return maxNumber + 1;
    }

    // Remove the given sign lock
    public void removeSignLock(SignLock lockToRemove){

        signLocks.remove(lockToRemove.getLockId());

        // Get the player file
        String fileName = ConfigHelper.getFullFilePath(DIRECTORY, getPlayerFileName());
        File file = ConfigHelper.getFile(fileName);
        FileConfiguration config = ConfigHelper.getFileConfiguration(file);

        // Update the config file
        config.set(LOCKS, new String[0]);
        for(SignLock lock : signLocks.values()){
            lock.writeToConfig(config, file);
        }
        ConfigHelper.saveFile(file, config);
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
        // Get the player file
        String fileName = ConfigHelper.getFullFilePath(DIRECTORY, getPlayerFileName());
        File file = ConfigHelper.getFile(fileName);
        FileConfiguration config = ConfigHelper.getFileConfiguration(file);
        lock.addSharedPlayer(playerId, config, file);
    }

    // Remove access from a player for the given sign lock
    public void removeSharedPlayerFromLock(SignLock lock, UUID playerId){
        // Get the player file
        String fileName = ConfigHelper.getFullFilePath(DIRECTORY, getPlayerFileName());
        File file = ConfigHelper.getFile(fileName);
        FileConfiguration config = ConfigHelper.getFileConfiguration(file);
        lock.removeSharedPlayer(playerId, config, file);
    }

    // Toggle if hoppers are enabled for the given lock
    public boolean toggleHoppersForLock(SignLock lock){
        // Get the player file
        String fileName = ConfigHelper.getFullFilePath(DIRECTORY, getPlayerFileName());
        File file = ConfigHelper.getFile(fileName);
        FileConfiguration config = ConfigHelper.getFileConfiguration(file);
        return lock.toggleHoppers(config, file);
    }

    // Return info for the player's sign locks
    public List<String> getSignLockInfo(int page){
        ArrayList<String> infos = new ArrayList<>();
        if(signLocks.size() == 0){
            infos.add(MessageTypeEnum.SignLockMessage.formatMessage("You do not have any sign locks!", true, false));
            return infos;
        }


        infos.add(MessageTypeEnum.SignLockMessage.getListHeader());

        // Get all the lock numbers and their locations
        HashMap<Integer, Location> lockInfos = new HashMap<>();
        for(SignLock lock : getSignLocks().values()){
            lockInfos.put(lock.getLockNumber(), lock.getLockLocation());
        }

        // Sort the numbers
        ArrayList<Integer> lockNums = new ArrayList<>(lockInfos.keySet());
        Collections.sort(lockNums);

        // Figure out the pages
        int pageSize = 10;
        int pageCount = (int)Math.floor(lockNums.size() / pageSize);
        if(lockNums.size() % pageSize > 0){
            pageCount++;
        }

        // Verify page number
        if(page == 0 || page > pageCount){
            infos.add(ChatColor.RED + "Invalid page number!");
            return infos;
        }

        // Fill in the info
        int currentPageMin = (page - 1) * pageSize;
        int currentPageMax = currentPageMin + pageSize - 1;
        for(int x : lockNums){

            // Verify this lock number is on the current page
            if(x < currentPageMin){
                continue;
            }
            if(x > currentPageMax){
                break;
            }

            Location lockLoc = lockInfos.get(x);
            String msg = ChatColor.GOLD + "" + ChatColor.BOLD + "Lock #" + x + ": " +
                    ChatColor.AQUA + lockLoc.getWorld().getName() + ChatColor.GREEN + " -" +
                    ChatColor.AQUA + " X: " + ChatColor.GREEN + lockLoc.getX() +
                    ChatColor.AQUA + " Y: " + ChatColor.GREEN + lockLoc.getY() +
                    ChatColor.AQUA + " Z: " + ChatColor.GREEN + lockLoc.getZ();
            infos.add(msg);
        }

        // Add next page number
        if(pageCount > page){
            infos.add(ChatColor.GREEN + "Next page: " + ChatColor.AQUA + "/" + SignLockCommand.BASE_COMMAND + " " + SignLockCommand.INFO + " " + (page + 1));
        }

        return infos;
    }

}
