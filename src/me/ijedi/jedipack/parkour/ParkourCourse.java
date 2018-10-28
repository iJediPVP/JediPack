package me.ijedi.jedipack.parkour;

import me.ijedi.jedipack.JediPackMain;
import me.ijedi.jedipack.common.MessageTypeEnum;
import me.ijedi.jedipack.common.Util;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ParkourCourse {

    private String CourseId;
    private Location StartLocation;
    private Location FinishLocation;
    private HashMap<String, Location> PointLocations = new HashMap<>();

    private final String WORLDID = "worldId";
    private final String START = "start";
    private final String FINISH = "finish";
    private final String POINT = "point";
    private final String X = "x";
    private final String Y = "y";
    private final String Z = "z";
    private final String FOOTER_ID = "footerId";
    private final String HEADER_ID = "headerId";

    private File CourseFile;
    private FileConfiguration CourseConfiguration;

    /* Config file: 123.yml
    start:
        worldId:
        id:
        x:
        y:
        z:
    finish:
        worldId:
        id:
        x:
        y:
        z:
    points:
        1:
            worldId:
            id:
            x:
            y:
            z:
    *
    * */

    public ParkourCourse(String courseId){
        CourseId = courseId;
        CourseFile = getFile();
        CourseConfiguration = getFileConfiguration();
        saveConfiguration();

        // Attempt to load any stored values

        // Start
        ConfigurationSection startSection = CourseConfiguration.getConfigurationSection(START);
        if(startSection != null){

            // TODO: I should probably handle this in case it fails to parse..
            String worldIdStr = startSection.getString(WORLDID);
            UUID worldId = UUID.fromString(worldIdStr);
            World world = Bukkit.getWorld(worldId);

            double x = startSection.getDouble(X);
            double y = startSection.getDouble(Y);
            double z = startSection.getDouble(Z);

            StartLocation = new Location(world, x, y, z);
        }

        // Finish
        ConfigurationSection finishSection = CourseConfiguration.getConfigurationSection(FINISH);
        if(finishSection != null){

            // TODO: I should probably handle this in case it fails to parse..
            String worldIdStr = finishSection.getString(WORLDID);
            UUID worldId = UUID.fromString(worldIdStr);
            World world = Bukkit.getWorld(worldId);

            double x = finishSection.getDouble(X);
            double y = finishSection.getDouble(Y);
            double z = finishSection.getDouble(Z);

            FinishLocation = new Location(world, x, y, z);
        }

        // Points
        ConfigurationSection pointsSection = CourseConfiguration.getConfigurationSection(POINT);
        if(pointsSection != null){

            for(String pointKey : pointsSection.getKeys(false)){

                ConfigurationSection configSection = pointsSection.getConfigurationSection(pointKey);
                if(configSection != null){

                    // TODO: I should probably handle this in case it fails to parse..
                    String worldIdStr = configSection.getString(WORLDID);
                    UUID worldId = UUID.fromString(worldIdStr);
                    World world = Bukkit.getWorld(worldId);

                    double x = configSection.getDouble(X);
                    double y = configSection.getDouble(Y);
                    double z = configSection.getDouble(Z);

                    if(PointLocations.containsKey(pointKey)){
                        continue;
                    }
                    Location location = new Location(world, x, y, z);
                    PointLocations.put(pointKey, location);
                }

            }
        }
    }

    // Set the location of a specified point for this ParkourCourse.
    public String setPointLocation(Location location, boolean isStart, boolean isFinish, int pointNumber) {

        if(isStart && StartLocation != null){
            String msg = MessageTypeEnum.ParkourMessage.formatMessage(String.format("A starting point for course '%s' has already been set!", CourseId), true, true);
            return msg;
        } // TODO: Allow overriding of course start.. Remove old starting location (armor stand and pressure plate) and set new.

        if(isFinish && FinishLocation != null){
            String msg = MessageTypeEnum.ParkourMessage.formatMessage(String.format("A finishing point for course '%s' has already been set!", CourseId), true, true);
            return  msg;
        } // TODO: Allow overriding of course finish.. Remove old starting location (armor stand and pressure plate) and set new.


        // Make sure the block the player's feet is in is AIR.
        if(!location.getBlock().getType().equals(Material.AIR)){
            String msg = MessageTypeEnum.ParkourMessage.formatMessage("A parkour point cannot be placed here!", true, true);
            return msg;
        }

        // Base our saved location off the center of the block of the given location
        Location saveLocation = Util.getCenteredBlockLocation(location.getBlock().getLocation());

        // Verify that the block under the point is a solid.
        Location belowLocation = new Location(saveLocation.getWorld(), saveLocation.getX(), saveLocation.getY() - 1, saveLocation.getZ());
        if(!belowLocation.getBlock().getType().isSolid()){
            String msg = MessageTypeEnum.ParkourMessage.formatMessage("A parkour point must be placed on a solid block!", true, true);
            return msg;
        }


        // Get location info
        String worldId = saveLocation.getWorld().getUID().toString();
        double x = saveLocation.getX();
        double y = saveLocation.getY();
        double z = saveLocation.getZ();

        // Determine config path
        String baseConfigPath = "";
        String output = "";
        String pointName = "";
        if (isStart) {
            baseConfigPath = START + ".";
            output = MessageTypeEnum.ParkourMessage.formatMessage(String.format("The starting point for course '%s' has been set!", CourseId), true, false);
            StartLocation = saveLocation;
            pointName = "Parkour Start";

        } else if (isFinish) {
            baseConfigPath = FINISH + ".";
            output = MessageTypeEnum.ParkourMessage.formatMessage(String.format("The finishing point for course '%s' has been set!", CourseId), true, false);
            FinishLocation = saveLocation;
            pointName = "Parkour Finish";

        } else if (pointNumber > 0) {
            baseConfigPath = POINT + "." + pointNumber + ".";
            output = MessageTypeEnum.ParkourMessage.formatMessage(String.format("Point #'%s' for course '%s' has been set!", pointNumber, CourseId), true, false);
            pointName = "Checkpoint #" + pointNumber;

            // If we don't already have this point, add it.
            String pointStr = Integer.toString(pointNumber);
            if(PointLocations.containsKey(pointStr)){
                output = MessageTypeEnum.ParkourMessage.formatMessage(String.format("Point '#%s' already exists for course '%s'.", pointNumber, CourseId), true, true);
                return output;
            }

            PointLocations.put(pointStr, saveLocation);
        }

        // If the config path did not get set, return false
        if (!Util.isNullOrEmpty(baseConfigPath)) {

            // Store config info
            CourseConfiguration.set(baseConfigPath + WORLDID, worldId);
            CourseConfiguration.set(baseConfigPath + X, x);
            CourseConfiguration.set(baseConfigPath + Y, y);
            CourseConfiguration.set(baseConfigPath + Z, z);

            // Use the original location here to prevent weird things from happening.. AKA the stands shift over to the next block.
            ParkourStand stand = ParkourStand.SpawnStand(saveLocation, isStart, isFinish, pointNumber);
            UUID footerId = stand.getFooterStandId();
            UUID headerId = stand.getHeaderStandId();

            CourseConfiguration.set(baseConfigPath + FOOTER_ID, footerId.toString());
            CourseConfiguration.set(baseConfigPath + HEADER_ID, headerId.toString());
            saveConfiguration();

            return output;
        } else {
            return MessageTypeEnum.ParkourMessage.formatMessage(String.format("Invalid arguments were given to set this point for course '%s'!", CourseId), true, true);
        }
    }

    // Remove the start location
    public void removeStart(){
        if(StartLocation != null){
            removeArmorStandEntity(START, StartLocation);
            CourseConfiguration.set(START, null);
            saveConfiguration();
            StartLocation = null;
        }
    }

    // Remove the finish location
    public void removeFinish(){
        if(FinishLocation != null){
            removeArmorStandEntity(FINISH, FinishLocation);
            CourseConfiguration.set(FINISH, null);
            saveConfiguration();
            FinishLocation = null;
        }
    }

    // Remove a single point
    public String removePoint(int pointNumber, boolean removeFromMap){

        String pointStr = Integer.toString(pointNumber);
        if(PointLocations.containsKey(pointStr)){
            String pointPath = POINT + "." + pointNumber;
            removeArmorStandEntity(pointPath, PointLocations.get(pointStr));
            CourseConfiguration.set(pointPath, null);
            saveConfiguration();
            if(removeFromMap){
                PointLocations.remove(pointStr);
            }
            return MessageTypeEnum.ParkourMessage.formatMessage(String.format("Point '#%s' was removed from course '%s'!", pointNumber, CourseId), true, false);
        }

        return MessageTypeEnum.ParkourMessage.formatMessage(String.format("Course '%s' does not contain point '#%s'.", CourseId, pointNumber), true, true);
    }

    // Remove all points for this course
    public void removeAllPoints(){

        Iterator<String> keys = PointLocations.keySet().iterator();
        while(keys.hasNext()){
            String nextKeyStr = keys.next();
            if(Util.isInteger(nextKeyStr)){
                int keyInt = Integer.parseInt(nextKeyStr);
                removePoint(keyInt, false);
                keys.remove();
            }
        }
    }

    // Delete the configuration file.
    public void removeConfigFile(){
        if(CourseFile.exists()){
            CourseConfiguration = null;
            CourseFile.delete();
        }
    }

    // Totally remove this course from the disk.
    public void removeEntireCourse(){
        removeStart();
        removeFinish();
        removeAllPoints();
        removeConfigFile();
    }

    // Remove the armor stand
    private void removeArmorStandEntity(String configPath, Location location){
        ConfigurationSection configSection = CourseConfiguration.getConfigurationSection(configPath);

        if(configSection != null){
            String footerIdStr = configSection.getString(FOOTER_ID);
            UUID footerId = UUID.fromString(footerIdStr);

            String headerIdStr = configSection.getString(HEADER_ID);
            UUID headerId = UUID.fromString(headerIdStr);

            Collection<Entity> nearbyEntities = location.getWorld().getNearbyEntities(location, 3, 3, 3);
            for(Entity e : nearbyEntities){
                if(e.getUniqueId().equals(footerId) || e.getUniqueId().equals(headerId)){

                    // Remove the entity and replace the pressure plate with air
                    e.remove();
                    location.getBlock().setType(Material.AIR);
                }
            }
        }
    }

    // Return the next checkpoint number that will be added to this course.
    public int getNextCheckpointNumber(){
        ArrayList<Integer> pointList = new ArrayList<>();
        for(String key : PointLocations.keySet()){
            if(Util.isInteger(key)){
                pointList.add(Integer.parseInt(key));
            }
        }

        int maxInt = 1;
        if(pointList.size() > 0){
            maxInt = Collections.max(pointList) + 1;
        }
        return maxInt;
    }

    // Reorder the checkpoints. IE: move #2 to #1, #3 to #2, etc.
    public void reorderCheckPoints(){

        // Make an arraylist so we can sort it
        ArrayList<Integer> pointIntList = new ArrayList<>();
        for(String pointKey : PointLocations.keySet()){
            if(Util.isInteger(pointKey)){
                pointIntList.add(Integer.parseInt(pointKey));
            }
        }

        Collections.sort(pointIntList);

        // Loop through and reassign the point numbers
        for(int pointInt : pointIntList){

            // Skip 1, else we could end up with 0.
            if(pointInt > 1){
                // See if we have a point less than this one
                String previousPoint = Integer.toString(pointInt - 1);
                if(!PointLocations.containsKey(previousPoint)){

                    // Wipe out the old config for this point
                    String originalPointPath = POINT + "." + pointInt;
                    Location pointLocation = PointLocations.get(Integer.toString(pointInt));

                    String footerIdStr = CourseConfiguration.getString(originalPointPath + "." + FOOTER_ID);
                    UUID footerId = UUID.fromString(footerIdStr);

                    String headerIdStr = CourseConfiguration.getString(originalPointPath + "." + HEADER_ID);
                    UUID headerId = UUID.fromString(headerIdStr);

                    CourseConfiguration.set(originalPointPath, null);

                    // Write the new config for this point
                    String newPointPath = POINT + "." + previousPoint;
                    CourseConfiguration.set(newPointPath + "." + WORLDID, pointLocation.getWorld().getUID().toString());
                    CourseConfiguration.set(newPointPath + "." + X, pointLocation.getBlock().getX());
                    CourseConfiguration.set(newPointPath + "." + Y, pointLocation.getBlock().getY());
                    CourseConfiguration.set(newPointPath + "." + Z, pointLocation.getBlock().getZ());
                    CourseConfiguration.set(newPointPath + "." + FOOTER_ID, footerId.toString());
                    CourseConfiguration.set(newPointPath + "." + HEADER_ID, headerId.toString());

                    // Update stored list
                    PointLocations.remove(Integer.toString(pointInt));
                    PointLocations.put(previousPoint, pointLocation);

                    // Update the footer armor stand
                    Collection<Entity> nearbyEntities = pointLocation.getWorld().getNearbyEntities(pointLocation, 3, 3, 3);
                    for(Entity e : nearbyEntities){
                        if(e.getUniqueId().equals(footerId)){
                            e.setCustomName(ParkourStand.formatString("Checkpoint #" + previousPoint, false));
                            break;
                        }
                    }
                    saveConfiguration();
                }
            }
        }
    }

    // Returns the start location for this course.
    public Location getStartLocation(){
        return StartLocation;
    }

    // Returns the finish location for this course.
    public Location getFinishLocation(){
        return FinishLocation;
    }

    // Returns the checkpoint number from the given location. If not found, returns 0.
    public int getCheckpointFromLocation(Location playerLocation){

        playerLocation = Util.getCenteredBlockLocation(playerLocation);

        for(String pointKey : PointLocations.keySet()){

            if(Util.doLocationsEqual(PointLocations.get(pointKey), playerLocation, false, false)){
                return Integer.parseInt(pointKey);
            }
        }
        return 0;
    }

    // Return the location for the given checkpoint number.
    public Location getCheckpointLocation(int checkpointNumber){
        if(PointLocations.containsKey(Integer.toString(checkpointNumber))){
            return PointLocations.get(Integer.toString(checkpointNumber));
        }
        return null;
    }

    // Returns the checkpoint locations for this course.
    public HashMap<String, Location> getPointLocations(){
        return PointLocations;
    }

    // Save the ParkourCourse configuration file.
    private void saveConfiguration(){
        try{
            File file = getFile();
            CourseConfiguration.save(file);

        }catch(IOException e){
            MessageTypeEnum.ParkourMessage.logMessage("Error saving configuration file for " + CourseId + ".");
            MessageTypeEnum.ParkourMessage.logMessage(e.toString());
        }
    }

    // Get the File object for this ParkourCourse.
    private File getFile(){

        if(CourseFile != null){
            return CourseFile;
        }

        String folder = JediPackMain.getThisPlugin().getDataFolder() + "/parkour";
        String fileName = CourseId + ".yml";
        File configFile = new File(folder, fileName);

        return configFile;
    }

    // Get the FileConfiguration object for this ParkourCourse.
    private FileConfiguration getFileConfiguration(){
        //File configFile = getFile();

        if(CourseConfiguration != null){
            return CourseConfiguration;
        }

        // Create the file if it doesn't exist
        if(!CourseFile.exists()){
            CourseFile.getParentFile().mkdirs();
            FileConfiguration config = YamlConfiguration.loadConfiguration(CourseFile);

            try{
                config.save(CourseFile);
            }catch(IOException e){
                MessageTypeEnum.ParkourMessage.logMessage(String.format("Error saving configuration file for course '%s'.", CourseId));
                MessageTypeEnum.ParkourMessage.logMessage(e.toString());
            }
            return config;

        }else{
            FileConfiguration config = YamlConfiguration.loadConfiguration(CourseFile);
            return config;
        }
    }

}
