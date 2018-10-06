package me.ijedi.jedipack.parkour;

import me.ijedi.jedipack.JediPackMain;
import me.ijedi.jedipack.common.Util;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class ParkourManager {

    private static HashMap<String, ParkourCourse> ParkourCourses = new HashMap<>();
    private static final String COURSE_PATH = "courses";
    private static final String CONFIG_NAME = "parkourConfig.yml";
    private static FileConfiguration FileConfiguration;

    public static void initializeCourses(){

        // Get the config section
        FileConfiguration = getFileConfiguration();
        ConfigurationSection configSection = FileConfiguration.getConfigurationSection(COURSE_PATH);
        if(configSection != null){

            // Loop through the keys and intialize PakourCourse objects
            Set<String> configKeys = configSection.getKeys(false);
            if(configKeys != null) {
                for (String key : configKeys) {
                    if(!doesCourseExist(key)){
                        ParkourCourse course = new ParkourCourse(key);
                        ParkourCourses.put(key, course);
                    }
                }
            }
        }
    }

    // Determine if a course exists.
    public static boolean doesCourseExist(String courseId){
        if(ParkourCourses.containsKey(Util.ToLower(courseId))){
            return true;
        }
        return false;
    }

    // Create a new course if one doesn't exist already.
    public static boolean createCourse(String courseId){
        if(!doesCourseExist(courseId)){
            // Crate course in a configuration file..
            FileConfiguration = FileConfiguration != null ? FileConfiguration : getFileConfiguration();
            ParkourCourse newCourse = new ParkourCourse(courseId);

            ParkourCourses.put(courseId, newCourse);
            String[] courseKeys = Util.HashSetToKeyArray(ParkourCourses);
            FileConfiguration.set(COURSE_PATH, courseKeys);
            saveConfiguration();

            return true;
        }
        return false;
    }

    private static File getFile(){
        String folder = JediPackMain.getThisPlugin().getDataFolder() + "/parkour";
        String fileName = CONFIG_NAME;
        File configFile = new File(folder, fileName);

        return configFile;
    }

    private static FileConfiguration getFileConfiguration(){
        File configFile = getFile();

        // Create the file if it doesn't exist
        FileConfiguration config;
        if(!configFile.exists()){
            configFile.getParentFile().mkdirs();
            config = YamlConfiguration.loadConfiguration(configFile);
            config.set(COURSE_PATH, "");
            try{
                config.save(configFile);
            }catch(IOException e){
                JediPackMain.getThisPlugin().getLogger().info("JediPack Parkour - Error saving configuration file.");
            }

        }else{
            config = YamlConfiguration.loadConfiguration(configFile);
        }

        return config;
    }

    private static void saveConfiguration(){
        try{
            File file = getFile();
            FileConfiguration = FileConfiguration != null ? FileConfiguration : getFileConfiguration();
            FileConfiguration.save(file);

        }catch(IOException e){
            JediPackMain.getThisPlugin().getLogger().info("JediPack Parkour - Error saving configuration file.");
        }
    }

}
