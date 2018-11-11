package me.ijedi.jedipack.parkour;

import me.ijedi.jedipack.JediPackMain;
import me.ijedi.jedipack.common.MessageTypeEnum;
import me.ijedi.jedipack.common.Util;
import me.ijedi.jedipack.menu.Menu;
import me.ijedi.jedipack.menu.MenuManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ParkourManager {

    private static HashMap<String, ParkourCourse> ParkourCourses = new HashMap<>();
    private static final String COURSE_PATH = "courses";
    private static final String CONFIG_NAME = "parkourConfig.yml";
    private static FileConfiguration ParkourConfiguration;
    private static File ParkourFile;
    private static HashMap<UUID, ParkourPlayerInfo> PlayerInfos = new HashMap<>();

    // Load the parkour courses from the configuratoin files.
    public static void initializeCourses(){

        // See if parkour is enabled
        JavaPlugin plugin = JediPackMain.getThisPlugin();
        if(!JediPackMain.isParkourEnabled){
            MessageTypeEnum.ParkourMessage.logMessage("Parkour is not enabled!");
            return;
        }

        // Initialize parkour commands
        plugin.getCommand(ParkourCommand.BASE_COMMAND).setExecutor(new ParkourCommand());
        plugin.getCommand(ParkourCommand.BASE_COMMAND).setTabCompleter(new ParkourCommand());

        // Initialize events
        plugin.getServer().getPluginManager().registerEvents(new ParkourEvents(), plugin);

        // Get the config section
        ParkourFile = getFile();
        ParkourConfiguration = getFileConfiguration();

        List<String> courseList = ParkourConfiguration.getStringList(COURSE_PATH);
        if(courseList != null){

            // Loop through the course names and initialize each ParkourCourse object
            for (String courseName : courseList) {
                if(!doesCourseExist(courseName)){
                    MessageTypeEnum.ParkourMessage.logMessage("Loading course: " + courseName);
                    ParkourCourse course = new ParkourCourse(courseName);
                    ParkourCourses.put(courseName, course);

                    // Spawn armor stands
                    if(course.getStartLocation() != null){
                        Location startLoc = course.getStartLocation();
                        Location belowLoc = new Location(startLoc.getWorld(), startLoc.getX(), startLoc.getY() - 1, startLoc.getZ());
                        if(belowLoc.getBlock().getType().equals(Material.AIR)){
                            belowLoc.getBlock().setType(Material.STONE);
                        }
                        course.setPointLocation(startLoc, true, false, 0, true);
                    }
                    if(course.getFinishLocation() != null){
                        Location finishLoc = course.getFinishLocation();
                        Location belowLoc = new Location(finishLoc.getWorld(), finishLoc.getX(), finishLoc.getY() - 1, finishLoc.getZ());
                        if(belowLoc.getBlock().getType().equals(Material.AIR)){
                            belowLoc.getBlock().setType(Material.STONE);
                        }
                        course.setPointLocation(finishLoc, false, true, 0, true);
                    }
                    for(String checkpointStr : course.getPointLocations().keySet()){
                        if(Util.isInteger(checkpointStr)){

                            Location checkpointLoc = course.getPointLocations().get(checkpointStr);
                            Location belowLoc = new Location(checkpointLoc.getWorld(), checkpointLoc.getX(), checkpointLoc.getY() - 1, checkpointLoc.getZ());
                            if(belowLoc.getBlock().getType().equals(Material.AIR)){
                                belowLoc.getBlock().setType(Material.STONE);
                            }

                            int checkpointNum = Integer.parseInt(checkpointStr);
                            course.setPointLocation(checkpointLoc, false, false, checkpointNum, true);
                        }
                    }
                }
            }
        } // Else no courses to load.
    }

    // Remove the armor stands and pressure plates for parkour courses
    public static void cleanup(){

        // See if parkour is enabled
        if(!JediPackMain.isParkourEnabled){
            MessageTypeEnum.ParkourMessage.logMessage("Parkour is not enabled!");
            return;
        }

        // Remove the armor stands and pressure plates from all courses
        for(ParkourCourse course : ParkourCourses.values()){

            // Start
            if(course.getStartLocation() != null){
                course.removeArmorStandEntity(ParkourCourse.START, course.getStartLocation());
                course.getStartLocation().getBlock().setType(Material.AIR);
            }

            // Finish
            if(course.getFinishLocation() != null){
                course.removeArmorStandEntity(ParkourCourse.FINISH, course.getFinishLocation());
                course.getFinishLocation().getBlock().setType(Material.AIR);
            }

            // Checkpoints
            for(String checkpointStr : course.getPointLocations().keySet()){
                Location checkpointLoc = course.getPointLocations().get(checkpointStr);
                course.removeArmorStandEntity(ParkourCourse.POINT + "." + checkpointStr, checkpointLoc);
                checkpointLoc.getBlock().setType(Material.AIR);
            }

        }

    }

    // Determine if a course exists.
    public static boolean doesCourseExist(String courseId){
        if(ParkourCourses.containsKey(Util.toLower(courseId))){
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
            String[] courseKeys = Util.hashMapToKeyArray(ParkourCourses);

            ParkourConfiguration.set(COURSE_PATH, courseKeys);
            saveConfiguration();

            return MessageTypeEnum.ParkourMessage.formatMessage(String.format("Created course '%s'!", courseId), true, false);
        }
        return MessageTypeEnum.ParkourMessage.formatMessage(String.format("Course '%s' already exists.", courseId), true, true);
    }

    public static String removeCourse(String courseId){
        if(doesCourseExist(courseId)){

            // Remove start, finish and all points from the course.
            ParkourCourse course = ParkourCourses.get(courseId);
            course.removeEntireCourse();

            // Remove it from the manager
            ParkourCourses.remove(courseId);
            String[] courseKeys = Util.hashMapToKeyArray(ParkourCourses);
            ParkourConfiguration.set(COURSE_PATH, courseKeys);
            saveConfiguration();

            return MessageTypeEnum.ParkourMessage.formatMessage(String.format("Course '%s' has been removed!", courseId), true, false);
        }

        return MessageTypeEnum.ParkourMessage.formatMessage(String.format("Course '%s' does not exist.", courseId), true, true);
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
            String output = course.setPointLocation(location, true, false, 0, false);

            return output;
        }
        return MessageTypeEnum.ParkourMessage.formatMessage(String.format("Course '%s' does not exist!", courseId), true, true);
    }

    // Set the finishing point for the specified parkour course.
    public static String setFinish(String courseId, Location location){
        if(doesCourseExist(courseId)){

            ParkourCourse course = ParkourCourses.get(courseId);
            String output = course.setPointLocation(location, false, true, 0, false);

            return output;
        }
        return MessageTypeEnum.ParkourMessage.formatMessage(String.format("Course '%s' does not exist!", courseId), true, true);
    }

    // Create a new checkpoint for the specified parkour course.
    public static String setPoint(String courseId, Location location){
        if(doesCourseExist(courseId)){
            ParkourCourse course = ParkourCourses.get(courseId);
            int pointNumber = course.getNextCheckpointNumber();
            String output = course.setPointLocation(location, false, false, pointNumber, false);

            return output;
        }
        return MessageTypeEnum.ParkourMessage.formatMessage(String.format("Course '%s' does not exist!", courseId), true, true);
    }

    // Remove the specified checkpoint from the specified parkour course.
    public static String removePoint(String courseId, int pointNumber){
        if(doesCourseExist(courseId)){
            ParkourCourse course = ParkourCourses.get(courseId);
            String output = course.removePoint(pointNumber, true);
            course.reorderCheckPoints();

            return output;
        }
        return MessageTypeEnum.ParkourMessage.formatMessage(String.format("Course '%s' does not exist!", courseId), true, true);
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
            Menu menu = ParkourEvents.getMenuFromCourse(courseId, course);
            player.openInventory(new MenuManager().getMenu(menu.getName()));
        }
        catch (NullPointerException e){
            String message = MessageTypeEnum.ParkourMessage.formatMessage(String.format("Course '%s' is empty.", courseId), true, true);
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
            MessageTypeEnum.ParkourMessage.logMessage("Error saving configuration file.");
            MessageTypeEnum.ParkourMessage.logMessage(e.toString());
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
                MessageTypeEnum.ParkourMessage.logMessage("Error saving configuration file.");
                MessageTypeEnum.ParkourMessage.logMessage(e.toString());
            }
            return config;

        }else{
            FileConfiguration config = YamlConfiguration.loadConfiguration(ParkourFile);
            return config;
        }
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

        location = Util.getCenteredBlockLocation(location);

        // Check the given course
        if(courseToCheck != null){

            Location courseLoc = courseToCheck.getStartLocation();
            if(courseLoc != null){
                if(Util.doLocationsEqual(courseLoc, location, false, false)){
                    return true;
                }
                if(checkBelow && Util.doLocationsEqual(courseLoc, location, true, false)){
                    return true;
                }
            }

            return false;
        }

        // Loop through courses and check start location
        for(ParkourCourse course : ParkourCourses.values()){

            Location startLoc = course.getStartLocation();
            if(startLoc != null){
                if(Util.doLocationsEqual(startLoc, location, false, false)){
                    return true;
                }
                if(checkBelow && Util.doLocationsEqual(startLoc, location, true, false)){
                    return true;
                }
            }
        }

        return false;
    }

    // Returns if the given location is a finishing point.
    public static boolean isFinishLocation(Location location, boolean checkBelow, ParkourCourse courseToCheck){

        location = Util.getCenteredBlockLocation(location);

        // Check the given course
        if(courseToCheck != null){

            Location courseLoc = courseToCheck.getFinishLocation();
            if(courseLoc != null){
                if(Util.doLocationsEqual(courseLoc, location, false, false)){
                    return true;
                }
                if(checkBelow && Util.doLocationsEqual(courseLoc, location, true, false)){
                    return true;
                }
            }

            return false;
        }

        // Loop through courses and check finish location
        for(ParkourCourse course : ParkourCourses.values()){

            Location finishLoc = course.getFinishLocation();
            if(finishLoc != null){
                if(Util.doLocationsEqual(finishLoc, location, false, false)){
                    return true;
                }
                if(checkBelow && Util.doLocationsEqual(finishLoc, location, true, false)){
                    return true;
                }
            }
        }

        return false;
    }

    // Return if the given location is a checkpoint.
    public static boolean isCheckpointLocation(Location location, boolean checkBelow, ParkourCourse courseToCheck){

        location = Util.getCenteredBlockLocation(location);

        // Check the given course
        if(courseToCheck != null){
            for(Location pointLoc : courseToCheck.getPointLocations().values()){

                if(Util.doLocationsEqual(pointLoc, location, false, false)){
                    return true;
                }
                if(checkBelow && Util.doLocationsEqual(pointLoc, location, true, false)){
                    return true;
                }
            }

            return false;
        }

        // Loop through courses and check finish location
        for(ParkourCourse course : ParkourCourses.values()){

            for(Location pointLoc : course.getPointLocations().values()){
                if(Util.doLocationsEqual(pointLoc, location, false, false)){
                    return true;
                }
                if(checkBelow && Util.doLocationsEqual(pointLoc, location, true, false)){
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



    // Return a ParkourPlayerInfo for the given player and course.
    public static ParkourPlayerInfo getPlayerInfo(Player player, String courseId){

        // Return existing player info. If one doesn't exist, create it.
        if(PlayerInfos.containsKey(player.getUniqueId())){
            return PlayerInfos.get(player.getUniqueId());

        } else {
            ParkourPlayerInfo info = new ParkourPlayerInfo(player.getUniqueId(), courseId);
            PlayerInfos.put(player.getUniqueId(), info);
            return info;
        }

    }

    // Remove ParkourPlayerInfo from our list.
    public static void removePlayerInfo(UUID playerId){
        if(PlayerInfos.containsKey(playerId)){
            PlayerInfos.remove(playerId);
        }
    }
}
