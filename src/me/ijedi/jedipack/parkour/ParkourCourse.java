package me.ijedi.jedipack.parkour;

import me.ijedi.jedipack.JediPackMain;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ParkourCourse {

    private String CourseId;

    private final String WORLDID = "worldId";
    private final String START = "finish";
    private final String FINISH = "start";
    private final String X = "X";
    private final String Y = "Y";
    private final String Z = "Z";

    private FileConfiguration FileConfiguration;

    /* Config file: 123.yml
    start:
        worldId:
        x:
        y:
        z:
    finish:
        worldId:
        x:
        y:
        z:
    checkpoints:
    - 1:
        worldId:
        x:
        y:
        z:
    *
    * */

    public ParkourCourse(String courseId){
        CourseId = courseId;
        saveConfiguration();
    }



    // Save the ParkourCourse configuration file.
    private void saveConfiguration(){
        try{
            File file = getFile();
            FileConfiguration = FileConfiguration != null ? FileConfiguration : getFileConfiguration();
            FileConfiguration.save(file);

        }catch(IOException e){
            JediPackMain.getThisPlugin().getLogger().info("JediPack Parkour - Error saving configuration file for " + CourseId + ".");
        }
    }

    // Get the File object for this ParkourCourse.
    private File getFile(){
        String folder = JediPackMain.getThisPlugin().getDataFolder() + "/parkour";
        String fileName = CourseId + ".yml";
        File configFile = new File(folder, fileName);

        return configFile;
    }

    // Get the FileConfiguration object for this ParkourCourse.
    private FileConfiguration getFileConfiguration(){
        File configFile = getFile();

        // Create the file if it doesn't exist
        FileConfiguration config;
        if(!configFile.exists()){
            configFile.getParentFile().mkdirs();
            config = YamlConfiguration.loadConfiguration(configFile);
            try{
                config.save(configFile);
            }catch(IOException e){
                JediPackMain.getThisPlugin().getLogger().info("JediPack Parkour - Error saving configuration file for " + CourseId + ".");
            }

        }else{
            config = YamlConfiguration.loadConfiguration(configFile);
        }

        return config;
    }




}
