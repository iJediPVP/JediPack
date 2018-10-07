package me.ijedi.jedipack.parkour;

import me.ijedi.jedipack.JediPackMain;
import me.ijedi.jedipack.common.Util;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class ParkourManager {

    private static HashMap<String, ParkourCourse> ParkourCourses = new HashMap<>();
    private static final String COURSE_PATH = "courses";
    private static final String CONFIG_NAME = "parkourConfig.yml";
    private static FileConfiguration ParkourConfiguration;
    private static File ParkourFile;

    // Load the parkour courses from the configuratoin files.
    public static void initializeCourses(){

        // Get the config section
        ParkourFile = getFile();
        ParkourConfiguration = getFileConfiguration();

        List<String> courseList = ParkourConfiguration.getStringList(COURSE_PATH);
        if(courseList != null){

            // Loop through the course names and initialize each ParkourCourse object
            for (String courseName : courseList) {
                if(!doesCourseExist(courseName)){
                    JediPackMain.getThisPlugin().getLogger().info("Loading course: " + courseName);
                    ParkourCourse course = new ParkourCourse(courseName);
                    ParkourCourses.put(courseName, course);
                }
            }
        } // Else no courses to load.
    }

    // Determine if a course exists.
    public static boolean doesCourseExist(String courseId){
        if(ParkourCourses.containsKey(Util.ToLower(courseId))){
            return true;
        }
        return false;
    }

    // Create a new course if one doesn't exist already.
    public static String createCourse(String courseId){
        if(!doesCourseExist(courseId)){
            // Crate course in a configuration file..
            ParkourCourse newCourse = new ParkourCourse(courseId);

            ParkourCourses.put(courseId, newCourse);
            String[] courseKeys = Util.HashMapToKeyArray(ParkourCourses);

            ParkourConfiguration.set(COURSE_PATH, courseKeys);
            saveConfiguration();

            return String.format("Created course '%s'!", courseId);
        }
        return String.format("Course '%s' already exists.", courseId);
    }

    public static String removeCourse(String courseId){
        if(doesCourseExist(courseId)){

            // TODO: Save armor stand entity ids to config and remove those as well..

            // Remove start, finish and all points from the course.
            ParkourCourse course = ParkourCourses.get(courseId);
            course.removeEntireCourse();

            // Remove it from the manager
            ParkourCourses.remove(courseId);
            String[] courseKeys = Util.HashMapToKeyArray(ParkourCourses);
            ParkourConfiguration.set(COURSE_PATH, courseKeys);
            saveConfiguration();

            return String.format("Course '%s' has been removed!", courseId);
        }

        return String.format("Course '%s' does not exist.", courseId);
    }


    // Set the starting point for the specified parkour course.
    public static String setStart(String courseId, Location location){
        if(doesCourseExist(courseId)){

            ParkourCourse course = ParkourCourses.get(courseId);
            String output = course.setPointLocation(location, true, false, 0);

            return output;
        }
        return String.format("Course '%s' does not exist!", courseId);
    }

    // Set the finishing point for the specified parkour course.
    public static String setFinish(String courseId, Location location){
        if(doesCourseExist(courseId)){

            ParkourCourse course = ParkourCourses.get(courseId);
            String output = course.setPointLocation(location, false, true, 0);

            return output;
        }
        return String.format("Course '%s' does not exist!", courseId);
    }

    public static String setPoint(String courseId, Location location){
        if(doesCourseExist(courseId)){
            ParkourCourse course = ParkourCourses.get(courseId);
            int pointNumber = course.getNextCheckpointNumber();
            String output = course.setPointLocation(location, false, false, pointNumber);

            return output;
        }
        return String.format("Course '%s' does not exist!", courseId);
    }


    // Save the ParkourManager configuration file.
    private static void saveConfiguration(){
        try{
            File file = getFile();
            ParkourConfiguration.save(file);

        }catch(IOException e){
            JediPackMain.getThisPlugin().getLogger().info("JediPack Parkour - Error saving configuration file.");
        }
    }

    // Get the File object for the ParkourManager.
    private static File getFile(){

        if(ParkourFile != null){
            return ParkourFile;
        }

        String folder = JediPackMain.getThisPlugin().getDataFolder() + "/parkour";
        String fileName = CONFIG_NAME;
        File configFile = new File(folder, fileName);

        return configFile;
    }

    // Get the FileConfiguration object for the ParkourManager.
    private static FileConfiguration getFileConfiguration(){
        //File configFile = getFile();

        if(ParkourConfiguration != null){
            return ParkourConfiguration;
        }

        // Create the file if it doesn't exist
        if(!ParkourFile.exists()){
            ParkourFile.getParentFile().mkdirs();
            FileConfiguration config = YamlConfiguration.loadConfiguration(ParkourFile);
            config.set(COURSE_PATH, new String[0]);
            try{
                config.save(ParkourFile);
            }catch(IOException e){
                JediPackMain.getThisPlugin().getLogger().info("JediPack Parkour - Error saving configuration file.");
            }
            return config;

        }else{
            FileConfiguration config = YamlConfiguration.loadConfiguration(ParkourFile);
            return config;
        }
    }



}
