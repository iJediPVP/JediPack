package me.ijedi.jedipack.parkour;

import com.sun.corba.se.spi.activation.POANameHelper;
import me.ijedi.jedipack.JediPackMain;
import me.ijedi.jedipack.common.Util;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.Configuration;
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
        if(finishSection != null){

            for(String pointKey : pointsSection.getKeys(false)){

                ConfigurationSection configSection = CourseConfiguration.getConfigurationSection(FINISH);
                if(finishSection != null){

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
            CourseConfiguration.set(START, null);
            removeArmorStandEntity(START, StartLocation);
            saveConfiguration();
            StartLocation = null;
        }
    }

    // Remove the finish location
    public void removeFinish(){
        if(FinishLocation != null){
            CourseConfiguration.set(FINISH, null);
            removeArmorStandEntity(FINISH, FinishLocation);
            saveConfiguration();
            FinishLocation = null;
        }
    }

    // Remove a single point
    public void removePoint(int pointNumber){
        if(PointLocations.containsKey(pointNumber)){
            String pointPath = POINT + "." + pointNumber;
            CourseConfiguration.set(pointPath, null);
            removeArmorStandEntity(pointPath, PointLocations.get(pointNumber));
            saveConfiguration();
            PointLocations.remove(pointNumber);
        }
    }

    // Remove all points for this course
    public void removeAllPoints(){
        for(String key: PointLocations.keySet()){
            if(Util.IsInteger(key)){
                int keyInt = Integer.parseInt(key);
                removePoint(keyInt);
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
            //Bukkit.getEntity(entityId).remove();
            Collection<Entity> nearbyEntities = location.getWorld().getNearbyEntities(location, 3, 3, 3);
            for(Entity e : nearbyEntities){
                if(e.getUniqueId() == entityId){
                    e.remove();
                    break;
                }
            }
            configSection.set(ENTITY_ID, null);
            saveConfiguration();

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
