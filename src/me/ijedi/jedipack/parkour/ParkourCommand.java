package me.ijedi.jedipack.parkour;

import me.ijedi.jedipack.common.Util;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class ParkourCommand implements CommandExecutor {

    private final String RESTART = "restart";
    private final String CHECKPOINT = "checkpoint";
    private final String[] FIRST_ARG_BLACKLIST = {RESTART, CHECKPOINT};

    private final String CREATE = "create";
    private final String DELETE = "remove"; // I know this is basically a duplicate, but I'm lazy.
    private final String START = "start";
    private final String END = "finish";

    private final String ADD = "add";
    private final String REMOVE = "remove";
    private final String MOVE = "move";
    private final String EDIT = "edit";

    private final ArrayList<String> HELP_LIST = new ArrayList<String>(){{
        add(ChatColor.GREEN + "" + ChatColor.BOLD + "======= " + ChatColor.AQUA + "JediPack Parkour" + ChatColor.GREEN + "" + ChatColor.BOLD + " =======");
        add(ChatColor.AQUA + "/jppk " + RESTART + ChatColor.GREEN + ": Teleport back to the beginning of a course.");
        add(ChatColor.AQUA + "/jppk " + CHECKPOINT + ChatColor.GREEN + ": Teleport back to the last checkpoint.");
    }};

    private final ArrayList<String> ADMIN_HELP_LIST = new ArrayList<String>(){{
        add(ChatColor.AQUA + "/jppk <courseId> " + CREATE + ChatColor.GREEN + ": Creates a new parkour course with the specified name.");
        add(ChatColor.AQUA + "/jppk <courseId> " + DELETE + ChatColor.GREEN + ": Removes the specified parkour course.");
        add(ChatColor.AQUA + "/jppk <courseId> " + START + ChatColor.GREEN + ": Sets the starting location of the specified parkour course.");
        add(ChatColor.AQUA + "/jppk <courseId> " + END + ChatColor.GREEN + ": Sets the ending location of the specified parkour course.");
        add(ChatColor.AQUA + "/jppk <courseId> " + CHECKPOINT + " " + ADD + ChatColor.GREEN + ": Adds a checkpoint for the specified course.");
        add(ChatColor.AQUA + "/jppk <courseId> " + CHECKPOINT + " " + REMOVE + " <checkpointNumber>" + ChatColor.GREEN + ": Remove the specified checkpoint from the specified course.");
        add(ChatColor.AQUA + "/jppk <courseId> " + EDIT + ChatColor.GREEN + ": Open the menu to edit the specified course.");
    }};

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {

        /* Command layout
        / jppk <courseId> <command>

        // Add or delete a course
        /jppk 123 create    // create the course
        /jppk 123 remove    // remove the course

        // Modify a course
        /jppk 123 start // place the starting point of the course
        /jppk 123 finish   // place the ending point of the course

        /jppk 123 checkpoint add // add next checkpoint in this course
        /jppk 123 checkpoint remove 1 // remove checkpoint number 1 from the course

        /jppk 123 edit // open the menu for editing the course

        // Interact with the course
        /jppk restart       // return to the start of the course
        /jppk checkpoint    // return to the last checkpoint

        * */

        // Only a player can run this
        if(!(commandSender instanceof Player)) {
            commandSender.sendMessage(ParkourManager.formatParkourString("This command can only be executed by a player!", true));
            return true;
        }

        Player player = (Player) commandSender;

        // Sse if we have args
        if(args.length > 0 && !Util.IsNullOrEmpty(args[0])){
            String firstArg = Util.ToLower(args[0]);

            // Check for a course id. Anything except commands required for this plugin will be allowed.
            if(!ArrayUtils.contains(FIRST_ARG_BLACKLIST, firstArg)){
                // At this point "firstArg" is our course id.

                // Check for our second argument.
                if(args.length > 1 && !Util.IsNullOrEmpty(args[1])){
                    String secondArg = Util.ToLower(args[1]);

                    // Handle create
                    if(secondArg.equals(CREATE)) {

                        String output = ParkourManager.createCourse(firstArg);
                        player.sendMessage(output);
                        return true;

                    }else if(secondArg.equals(DELETE)){ // Handle delete

                        String output = ParkourManager.removeCourse(firstArg);
                        player.sendMessage(output);
                        return true;

                    }else if(secondArg.equals(START)){ // Handle start

                        // Get the player's location and try to set the starting point for this course.
                        Location location =  player.getLocation();
                        String output = ParkourManager.setStart(firstArg, location);
                        player.sendMessage(output);

                        // Don't spam the player with message upon creating a starting point
                        ParkourPlayerInfo info = ParkourManager.getPlayerInfo(player, firstArg);
                        info.beginStartMessageCoolDown();
                        return true;
                    }

                    // Make sure the course exists
                    if(!ParkourManager.doesCourseExist(firstArg)){
                        String message = ParkourManager.formatParkourString(String.format("Course '%s' does not exist!", firstArg), true);
                        player.sendMessage(message);
                        return true;
                    }

                    if(secondArg.equals(END)) { // Handle end

                        // Get the player's location and try to set the finishing point for this course.
                        Location location = player.getLocation();
                        String output = ParkourManager.setFinish(firstArg, location);
                        player.sendMessage(output);

                        // Don't spam the player with message upon creating a finishing point
                        ParkourPlayerInfo info = ParkourManager.getPlayerInfo(player, firstArg);
                        info.beginFinishMessageCoolDown(false);
                        return true;

                    } else if(secondArg.equals(CHECKPOINT)){

                        // If there are no other args, return to the last checkpoint
                        if(args.length == 2){
                            // TODO: write this code..
                        }
                        else{
                            // Check for third argument
                            if(args.length > 2){
                                String thirdArg = Util.ToLower(args[2]);
                                // Add a checkpoint
                                if(thirdArg.equals(ADD)){

                                    ParkourCourse course = ParkourManager.getCourse(firstArg);
                                    int nextCheckpoint = course.getNextCheckpointNumber();
                                    String output = ParkourManager.setPoint(firstArg,  player.getLocation());
                                    player.sendMessage(output);


                                    // Don't spam the player with message upon creating a checkpoint
                                    ParkourPlayerInfo info = ParkourManager.getPlayerInfo(player, firstArg);
                                    info.beginCheckpointMessageCoolDown(nextCheckpoint);
                                    return true;

                                } else if(thirdArg.equals(REMOVE)){ // Remove a checkpoint

                                    // See if a point number was given.
                                    if(args.length > 3){

                                        String pointStr = args[3];
                                        if(Util.IsInteger(pointStr)){

                                            // Remove the point
                                            int pointInt = Integer.parseInt(pointStr);
                                            String output = ParkourManager.removePoint(firstArg, pointInt);
                                            player.sendMessage(output);
                                            return true;

                                        } else {
                                            player.sendMessage(ParkourManager.formatParkourString("A point number must be an integer.", true));
                                            return true;
                                        }

                                    } else {
                                        player.sendMessage(ParkourManager.formatParkourString("A point number must be specified.", true));
                                        return true;
                                    }
                                }
                            }
                        }

                    } else if(secondArg.equals(EDIT)){

                        // Open up the menu for this course
                        ParkourManager.openCourseMenu(firstArg, player);
                        return true;

                    }// TODO: Else, handle invalid second argument.

                } // TODO: Else, handle this..

            } else if(firstArg.equals(RESTART)) {

                // If they have started a course, TP them back to the starting location.
                ParkourPlayerInfo info = ParkourManager.getPlayerInfo(player, "");
                if(info.hasStartedAnyCourse()) {

                    // Get the start location of the course and teleport the player there.
                    ParkourCourse course = ParkourManager.getCourse(info.getCourseId());
                    Location startLoc = course.getStartLocation();
                    startLoc = Util.centerLocation(startLoc);
                    startLoc.setYaw(player.getLocation().getYaw());
                    player.teleport(startLoc);
                    String message = ParkourManager.formatParkourString(String.format("You have been teleported back to the start of course '%s'!", info.getCourseId()), false);
                    player.sendMessage(message);

                } else {
                    player.sendMessage(ParkourManager.formatParkourString("You haven't started a parkour course yet!", true));
                }
                return true;

            } else if (firstArg.equals(CHECKPOINT)){

                // Return a player to the last checkpoint they reached.

                // If they have started a course, TP them back to their last checkpoint location.
                ParkourPlayerInfo info = ParkourManager.getPlayerInfo(player, "");
                if(info.hasStartedAnyCourse()) {

                    // Make sure the player has reached a checkpoint
                    if(info.getCurrentCheckpoint() <= 0){
                        String message = ParkourManager.formatParkourString("You haven't reached a checkpoint yet!", true);
                        player.sendMessage(message);
                        return true;
                    }

                    // Get the start location of the course and teleport the player there.
                    ParkourCourse course = ParkourManager.getCourse(info.getCourseId());
                    Location checkpointLoc = course.getCheckpointLocation(info.getCurrentCheckpoint());
                    if(checkpointLoc == null){
                        String message = ParkourManager.formatParkourString("You haven't reached a checkpoint yet!", true);
                        player.sendMessage(message);
                        return true;
                    }

                    checkpointLoc.setYaw(player.getLocation().getYaw());
                    checkpointLoc = Util.centerLocation(checkpointLoc);
                    player.teleport(checkpointLoc);
                    String message = ParkourManager.formatParkourString(String.format("Returned to checkpoint #%s!", info.getCurrentCheckpoint()), false);
                    player.sendMessage(message);
                    info.beginCheckpointMessageCoolDown(info.getCurrentCheckpoint());

                } else {
                    player.sendMessage(ParkourManager.formatParkourString("You haven't started a parkour course yet!", true));
                }
                return true;

            }

        }

        for(String msg : HELP_LIST){
            player.sendMessage(msg);
        }
        for(String msg : ADMIN_HELP_LIST){
            player.sendMessage(msg);
        }

        return true;
    }
}
