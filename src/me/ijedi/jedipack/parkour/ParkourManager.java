package me.ijedi.jedipack.parkour;

import me.ijedi.jedipack.JediPackMain;
import me.ijedi.jedipack.common.Util;
import me.ijedi.jedipack.menu.Menu;
import me.ijedi.jedipack.menu.MenuManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

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

        // See if parkour is enabled
        JavaPlugin plugin = JediPackMain.getThisPlugin();
        FileConfiguration pluginConfig = plugin.getConfig();
        if(!pluginConfig.getBoolean(JediPackMain.PARKOUR_ENABLED)){
            plugin.getLogger().info(formatParkourString("Parkour is not enabled!", false));
            return;
        }

        // Initialize parkour commands
        plugin.getCommand("jppk").setExecutor(new ParkourCommand());

        // Initialize events
        plugin.getServer().getPluginManager().registerEvents(new ParkourBlockBreakEvent(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new ParkourMenuEvent(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new ParkourPointInteractEvent(), plugin);

        // Get the config section
        ParkourFile = getFile();
        ParkourConfiguration = getFileConfiguration();

        List<String> courseList = ParkourConfiguration.getStringList(COURSE_PATH);
        if(courseList != null){

            // Loop through the course names and initialize each ParkourCourse object
            for (String courseName : courseList) {
                if(!doesCourseExist(courseName)){
                    plugin.getLogger().info(formatParkourString("Loading course: " + courseName, false));
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

            return formatParkourString(String.format("Created course '%s'!", courseId), false);
        }
        return formatParkourString(String.format("Course '%s' already exists.", courseId), true);
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

            return formatParkourString(String.format("Course '%s' has been removed!", courseId), false);
        }

        return formatParkourString(String.format("Course '%s' does not exist.", courseId), true);
    }

    public static ParkourCourse getCourse(String courseId){
        if(doesCourseExist(courseId)){
            ParkourCourse course = ParkourCourses.get(courseId);
            return course;
        }
        return null;
    }


    // Set the starting point for the specified parkour course.
    public static String setStart(String courseId, Location location){
        if(doesCourseExist(courseId)){

            ParkourCourse course = ParkourCourses.get(courseId);
            String output = course.setPointLocation(location, true, false, 0);

            return output;
        }
        return formatParkourString(String.format("Course '%s' does not exist!", courseId), true);
    }

    // Set the finishing point for the specified parkour course.
    public static String setFinish(String courseId, Location location){
        if(doesCourseExist(courseId)){

            ParkourCourse course = ParkourCourses.get(courseId);
            String output = course.setPointLocation(location, false, true, 0);

            return output;
        }
        return formatParkourString(String.format("Course '%s' does not exist!", courseId), true);
    }

    // Create a new checkpoint for the specified parkour course.
    public static String setPoint(String courseId, Location location){
        if(doesCourseExist(courseId)){
            ParkourCourse course = ParkourCourses.get(courseId);
            int pointNumber = course.getNextCheckpointNumber();
            String output = course.setPointLocation(location, false, false, pointNumber);

            return output;
        }
        return formatParkourString(String.format("Course '%s' does not exist!", courseId), true);
    }

    // Remove the specified checkpoint from the specified parkour course.
    public static String removePoint(String courseId, int pointNumber){
        if(doesCourseExist(courseId)){
            ParkourCourse course = ParkourCourses.get(courseId);
            String output = course.removePoint(pointNumber, true);
            course.reorderCheckPoints();

            return output;
        }
        return formatParkourString(String.format("Course '%s' does not exist!", courseId), true);
    }

    // See if a course has a starting point. This assumes that the course exists.
    public static boolean hasStart(String courseId){
        ParkourCourse course = getCourse(courseId);
        JediPackMain.getThisPlugin().getLogger().info(courseId);
        return course.getStartLocation() != null;
    }

    // Remove the starting point form the course. This assumes the course exists and has a starting point.
    public static void removeStart(String courseId){
        ParkourCourse course = getCourse(courseId);
        course.removeStart();
    }

    // See if a course has a finishing point. This assumes that the course exists.
    public static boolean hasFinish(String courseId){
        ParkourCourse course = getCourse(courseId);
        JediPackMain.getThisPlugin().getLogger().info(courseId);
        return course.getFinishLocation() != null;
    }

    // Remove the finishing point form the course. This assumes the course exists and has a finishing point.
    public static void removeFinish(String courseId){
        ParkourCourse course = getCourse(courseId);
        course.removeFinish();
    }

    // Open the course menu for the player
    public static void openCourseMenu(String courseId, Player player){
        try{
            ParkourCourse course = getCourse(courseId);
            Menu menu = ParkourMenuEvent.getMenuFromCourse(courseId, course);
            player.openInventory(new MenuManager().getMenu(menu.getName()));
        }
        catch (NullPointerException e){
            String message = ParkourManager.formatParkourString(String.format("Course '%s' is empty.", courseId), true);
            player.sendMessage(message);
            player.closeInventory();
        }
    }



    // Save the ParkourManager configuration file.
    private static void saveConfiguration(){
        try{
            File file = getFile();
            ParkourConfiguration.save(file);

        }catch(IOException e){
            String errorMessage = formatParkourString("JediPack Parkour - Error saving configuration file.", true);
            JediPackMain.getThisPlugin().getLogger().info(errorMessage);
            JediPackMain.getThisPlugin().getLogger().info(e.toString());
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
                String errorMessage = formatParkourString("JediPack Parkour - Error saving configuration file.", true);
                JediPackMain.getThisPlugin().getLogger().info(errorMessage);
                JediPackMain.getThisPlugin().getLogger().info(e.toString());
            }
            return config;

        }else{
            FileConfiguration config = YamlConfiguration.loadConfiguration(ParkourFile);
            return config;
        }
    }



    // Format the given string for parkour chat messages
    public static String formatParkourString(String str, boolean isError){
        String finalString = ChatColor.GREEN + "" + ChatColor.BOLD + "[Parkour] ";
        if(isError){
            finalString += ChatColor.RED;
        }
        finalString += str;
        return finalString;
    }

    // Returns if the given location belongs to a parkour course
    public static Boolean isParkourBlockLocation(Location location){
        // Check start, finish, and checkpoints
        if(isStartLocation(location, true, null)
                || isFinishLocation(location, true, null)
                || isCheckpointLocation(location, true, null) ){
            return true;
        }
        return false;
    }

    // Returns if the given location is a starting point.
    public static boolean isStartLocation(Location location, boolean checkBelow, ParkourCourse courseToCheck){

        // Check the given course
        if(courseToCheck != null){

            Location courseLoc = courseToCheck.getStartLocation();
            if(courseLoc != null){
                if(Util.DoLocationsEqual(courseLoc, location, false)){
                    return true;
                }
                if(checkBelow && Util.DoLocationsEqual(courseLoc, location, true)){
                    return true;
                }
            }

            return false;
        }

        // Loop through courses and check start location
        for(ParkourCourse course : ParkourCourses.values()){

            Location startLoc = course.getStartLocation();
            if(startLoc != null){
                if(Util.DoLocationsEqual(startLoc, location, false)){
                    return true;
                }
                if(checkBelow && Util.DoLocationsEqual(startLoc, location, true)){
                    return true;
                }
            }
        }

        return false;
    }

    // Returns if the given location is a finishing point.
    public static boolean isFinishLocation(Location location, boolean checkBelow, ParkourCourse courseToCheck){

        // Check the given course
        if(courseToCheck != null){

            Location courseLoc = courseToCheck.getFinishLocation();
            if(courseLoc != null){
                if(Util.DoLocationsEqual(courseLoc, location, false)){
                    return true;
                }
                if(checkBelow && Util.DoLocationsEqual(courseLoc, location, true)){
                    return true;
                }
            }

            return false;
        }

        // Loop through courses and check finish location
        for(ParkourCourse course : ParkourCourses.values()){

            Location finishLoc = course.getFinishLocation();
            if(finishLoc != null){
                if(Util.DoLocationsEqual(finishLoc, location, false)){
                    return true;
                }
                if(checkBelow && Util.DoLocationsEqual(finishLoc, location, true)){
                    return true;
                }
            }
        }

        return false;
    }

    // Return if the given location is a checkpoint.
    public static boolean isCheckpointLocation(Location location, boolean checkBelow, ParkourCourse courseToCheck){

        // Check the given course
        if(courseToCheck != null){

            for(Location pointLoc : courseToCheck.getPointLocations().values()){
                if(Util.DoLocationsEqual(pointLoc, location, false)){
                    return true;
                }
                if(checkBelow && Util.DoLocationsEqual(pointLoc, location, true)){
                    return true;
                }
            }

            return false;
        }

        // Loop through courses and check finish location
        for(ParkourCourse course : ParkourCourses.values()){
            for(Location pointLoc : course.getPointLocations().values()){
                if(Util.DoLocationsEqual(pointLoc, location, false)){
                    return true;
                }
                if(checkBelow && Util.DoLocationsEqual(pointLoc, location, true)){
                    return true;
                }
            }
        }
        return false;
    }

    // Returns the course id from the given location. Returns null if the location does not belong to a course.
    public static String getCourseIdFromLocation(Location location){

        // Loop through the courses
        for(String courseKey : ParkourCourses.keySet()){

            // Check locations
            ParkourCourse course = ParkourCourses.get(courseKey);
            if(isStartLocation(location, false, course)
                    || isFinishLocation(location, false, course)
                    || isCheckpointLocation(location, false, course)){
                return courseKey;
            }

        }

        return null;
    }
}
