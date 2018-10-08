package me.ijedi.jedipack.parkour;

import me.ijedi.jedipack.JediPackMain;
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
    private final String ENTITY_ID = "entityId";

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
    //checkpoints:
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

            int x = startSection.getInt(X);
            int y = startSection.getInt(Y);
            int z = startSection.getInt(Z);

            StartLocation = new Location(world, x, y, z);
        }

        // Finish
        ConfigurationSection finishSection = CourseConfiguration.getConfigurationSection(FINISH);
        if(finishSection != null){

            // TODO: I should probably handle this in case it fails to parse..
            String worldIdStr = finishSection.getString(WORLDID);
            UUID worldId = UUID.fromString(worldIdStr);
            World world = Bukkit.getWorld(worldId);

            int x = finishSection.getInt(X);
            int y = finishSection.getInt(Y);
            int z = finishSection.getInt(Z);

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

                    int x = configSection.getInt(X);
                    int y = configSection.getInt(Y);
                    int z = configSection.getInt(Z);

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
            return String.format("A starting point for course '%s' has already been set!", CourseId);
        } // TODO: Allow overriding of course start.. Remove old starting location (armor stand and pressure plate) and set new.

        if(isFinish && FinishLocation != null){
            return String.format("A finishing point for course '%s' has already been set!", CourseId);
        } // TODO: Allow overriding of course finish.. Remove old starting location (armor stand and pressure plate) and set new.

        // Get location info
        String worldId = location.getWorld().getUID().toString();
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        Location saveLocation = new Location(location.getWorld(), x, y, z);

        // Determine config path
        String baseConfigPath = "";
        String output = "";
        String pointName = "";
        if (isStart) {
            baseConfigPath = START + ".";
            output = String.format("The starting point for course '%s' has been set!", CourseId);
            StartLocation = saveLocation;
            pointName = "Parkour Start";

        } else if (isFinish) {
            baseConfigPath = FINISH + ".";
            output = String.format("The finishing point for course '%s' has been set!", CourseId);
            FinishLocation = saveLocation;
            pointName = "Parkour Finish";

        } else if (pointNumber > 0) {
            baseConfigPath = POINT + "." + pointNumber + ".";
            output = String.format("Point #'%s' for course '%s' has been set!", pointNumber, CourseId);
            pointName = "Checkpoint #" + pointNumber;

            // If we don't already have this point, add it.
            String pointStr = Integer.toString(pointNumber);
            if(PointLocations.containsKey(pointStr)){ // TODO: Add a way to overwrite points
                output = String.format("Point '#%s' already exists for course '%s'.", pointNumber, CourseId);
                return output;
            }
            PointLocations.put(pointStr, saveLocation);
        }

        // If the config path did not get set, return false
        if (!Util.IsNullOrEmpty(baseConfigPath)) {

            // Store config info
            CourseConfiguration.set(baseConfigPath + WORLDID, worldId);
            CourseConfiguration.set(baseConfigPath + X, x);
            CourseConfiguration.set(baseConfigPath + Y, y);
            CourseConfiguration.set(baseConfigPath + Z, z);

            ParkourPoint point = new ParkourPoint(isStart, isFinish, pointNumber, pointName);
            UUID entityId = point.Spawn(location); // Use the original location here to prevent weird things from happening.. AKA the stands shift over to the next block.

            CourseConfiguration.set(baseConfigPath + ENTITY_ID, entityId.toString());
            saveConfiguration();

            return output;
        } else {
            return String.format("Invalid arguments were given to set this point for course '%s'!", CourseId);
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
            return String.format("Point '#%s' was removed from course '%s'!", pointNumber, CourseId);
        }

        return String.format("Course '%s' does not contain point '#%s'.", CourseId, pointNumber);
    }

    // Remove all points for this course
    public void removeAllPoints(){

        Iterator<String> keys = PointLocations.keySet().iterator();
        while(keys.hasNext()){
            String nextKeyStr = keys.next();
            if(Util.IsInteger(nextKeyStr)){
                int keyInt = Integer.parseInt(nextKeyStr);
                removePoint(keyInt, false);
                //PointLocations.keySet().remove(nextKeyStr);
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
            String entityIdStr = configSection.getString(ENTITY_ID);
            UUID entityId = UUID.fromString(entityIdStr);

            Collection<Entity> nearbyEntities = location.getWorld().getNearbyEntities(location, 3, 3, 3);
            for(Entity e : nearbyEntities){
                if(e.getUniqueId().equals(entityId)){

                    // Remove the entity and replace the pressure plate with air
                    e.remove();
                    location.getBlock().setType(Material.AIR);
                    break;
                }
            }
        }
    }

    // Return the next checkpoint number that will be added to this course.
    public int getNextCheckpointNumber(){
        ArrayList<Integer> pointList = new ArrayList<>();
        for(String key : PointLocations.keySet()){
            if(Util.IsInteger(key)){
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
            if(Util.IsInteger(pointKey)){
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
                    JediPackMain.getThisPlugin().getLogger().info(Boolean.toString(pointLocation == null));
                    String entityIdStr = CourseConfiguration.getString(originalPointPath + "." + ENTITY_ID);

                    UUID entityId = UUID.fromString(entityIdStr);
                    CourseConfiguration.set(originalPointPath, null);

                    // Write the new config for this point
                    String newPointPath = POINT + "." + previousPoint;
                    CourseConfiguration.set(newPointPath + "." + WORLDID, pointLocation.getWorld().getUID().toString());
                    CourseConfiguration.set(newPointPath + "." + X, pointLocation.getBlock().getX());
                    CourseConfiguration.set(newPointPath + "." + Y, pointLocation.getBlock().getY());
                    CourseConfiguration.set(newPointPath + "." + Z, pointLocation.getBlock().getZ());
                    CourseConfiguration.set(newPointPath + "." + ENTITY_ID, entityId.toString());

                    // Update stored list
                    PointLocations.remove(Integer.toString(pointInt));
                    PointLocations.put(previousPoint, pointLocation);

                    // Update the armor stand
                    Collection<Entity> nearbyEntities = pointLocation.getWorld().getNearbyEntities(pointLocation, 3, 3, 3);
                    for(Entity e : nearbyEntities){
                        if(e.getUniqueId().equals(entityId)){
                            e.setCustomName("Checkpoint #" + previousPoint);
                            break;
                        }
                    }
                    saveConfiguration();
                }
            }
        }


    }

    // Save the ParkourCourse configuration file.
    private void saveConfiguration(){
        try{
            File file = getFile();
            CourseConfiguration.save(file);

        }catch(IOException e){
            JediPackMain.getThisPlugin().getLogger().info("JediPack Parkour - Error saving configuration file for " + CourseId + ".");
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
                JediPackMain.getThisPlugin().getLogger().info(String.format("JediPack Parkour - Error saving configuration file for course '%s'.", CourseId));
            }
            return config;

        }else{
            FileConfiguration config = YamlConfiguration.loadConfiguration(CourseFile);
            return config;
        }
    }




}
