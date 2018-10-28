package me.ijedi.jedipack.parkour;

import me.ijedi.jedipack.common.MessageTypeEnum;
import me.ijedi.jedipack.common.Util;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ParkourCommand implements TabExecutor {

    public static final String BASE_COMMAND = "parkour";
    private final String RESTART = "restart";
    private final String CHECKPOINT = "checkpoint";
    private final String[] FIRST_ARG_BLACKLIST = {RESTART, CHECKPOINT};

    private final String CREATE = "create";
    private final String DELETE = "remove"; // I know this is basically a duplicate, but I'm lazy.
    private final String START = "start";
    private final String END = "finish";

    private final String ADD = "add";
    private final String REMOVE = "remove";
    private final String EDIT = "edit";
    private final String COURSEID = "<courseId>";

    // Admin permissions
    private final String PKPERM_ADMIN_CREATE = "jp.parkour.admin.create";
    private final String PKPERM_ADMIN_DELETE = "jp.parkour.admin.delete";
    private final String PKPERM_ADMIN_START = "jp.parkour.admin.start";
    private final String PKPERM_ADMIN_FINISH = "jp.parkour.admin.finish";
    private final String PKPERM_ADMIN_EDIT = "jp.parkour.admin.edit";
    private final String PKPERM_ADMIN_CHECKPOINT_ADD = "jp.parkour.admin.checkpoint.add";
    private final String PKPERM_ADMIN_CHECKPOINT_REMOVE = "jp.parkour.admin.checkpoint.remove";

    public static final String PKPERM_PARKOUR = "jp.parkour.user.course";
    private final String PKPERM_PARKOUR_RESTART = "jp.parkour.user.restart";
    private final String PKPERM_PARKOUR_CHECKPOINT = "jp.parkour.user.checkpoint";

    private final ArrayList<String> HELP_LIST = new ArrayList<String>(){{
        add(ChatColor.GREEN + "" + ChatColor.BOLD + "======= " + ChatColor.AQUA + "JediPack Parkour" + ChatColor.GREEN + "" + ChatColor.BOLD + " =======");
        add(ChatColor.AQUA + "/" + BASE_COMMAND + " " + RESTART + ChatColor.GREEN + ": Teleport back to the beginning of a course.");
        add(ChatColor.AQUA + "/" + BASE_COMMAND + " " + CHECKPOINT + ChatColor.GREEN + ": Teleport back to the last checkpoint.");
    }};

    private final ArrayList<String> ADMIN_HELP_LIST = new ArrayList<String>(){{
        add(ChatColor.AQUA + "/" + BASE_COMMAND + " <courseId> " + CREATE + ChatColor.GREEN + ": Creates a new parkour course with the specified name.");
        add(ChatColor.AQUA + "/" + BASE_COMMAND + " <courseId> " + DELETE + ChatColor.GREEN + ": Removes the specified parkour course.");
        add(ChatColor.AQUA + "/" + BASE_COMMAND + " <courseId> " + START + ChatColor.GREEN + ": Sets the starting location of the specified parkour course.");
        add(ChatColor.AQUA + "/" + BASE_COMMAND + " <courseId> " + END + ChatColor.GREEN + ": Sets the ending location of the specified parkour course.");
        add(ChatColor.AQUA + "/" + BASE_COMMAND + " <courseId> " + CHECKPOINT + " " + ADD + ChatColor.GREEN + ": Adds a checkpoint for the specified course.");
        add(ChatColor.AQUA + "/" + BASE_COMMAND + " <courseId> " + CHECKPOINT + " " + REMOVE + " <checkpointNumber>" + ChatColor.GREEN + ": Remove the specified checkpoint from the specified course.");
        add(ChatColor.AQUA + "/" + BASE_COMMAND + " <courseId> " + EDIT + ChatColor.GREEN + ": Open the menu to edit the specified course.");
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
            MessageTypeEnum.ParkourMessage.sendMessage("This command can only be executed by a player!", commandSender, true);
            return true;
        }

        Player player = (Player) commandSender;

        // Sse if we have args
        if(args.length > 0 && !Util.isNullOrEmpty(args[0])){
            String firstArg = Util.toLower(args[0]);

            // Check for a course id. Anything except commands required for this plugin will be allowed.
            if(!ArrayUtils.contains(FIRST_ARG_BLACKLIST, firstArg)){
                // At this point "firstArg" is our course id.

                // Check for our second argument.
                if(args.length > 1 && !Util.isNullOrEmpty(args[1])){
                    String secondArg = Util.toLower(args[1]);

                    // Handle create
                    if(secondArg.equals(CREATE)) {

                        // Check perms
                        if(Util.hasNoPerms(player, PKPERM_ADMIN_CREATE, MessageTypeEnum.ParkourMessage)){
                            return true;
                        }

                        String output = ParkourManager.createCourse(firstArg);
                        player.sendMessage(output);
                        return true;

                    }else if(secondArg.equals(DELETE)){ // Handle delete

                        // Check perms
                        if(Util.hasNoPerms(player, PKPERM_ADMIN_DELETE, MessageTypeEnum.ParkourMessage)){
                            return true;
                        }

                        String output = ParkourManager.removeCourse(firstArg);
                        player.sendMessage(output);
                        return true;

                    }else if(secondArg.equals(START)){ // Handle start

                        // Check perms
                        if(Util.hasNoPerms(player, PKPERM_ADMIN_START, MessageTypeEnum.ParkourMessage)){
                            return true;
                        }

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
                        MessageTypeEnum.ParkourMessage.sendMessage(String.format("Course '%s' does not exist!", firstArg), player, true);
                        return true;
                    }

                    if(secondArg.equals(END)) { // Handle end

                        // Check perms
                        if(Util.hasNoPerms(player, PKPERM_ADMIN_FINISH, MessageTypeEnum.ParkourMessage)){
                            return true;
                        }

                        // Get the player's location and try to set the finishing point for this course.
                        Location location = player.getLocation();
                        String output = ParkourManager.setFinish(firstArg, location);
                        player.sendMessage(output);

                        // Don't spam the player with message upon creating a finishing point
                        ParkourPlayerInfo info = ParkourManager.getPlayerInfo(player, firstArg);
                        info.beginFinishMessageCoolDown(false);
                        return true;

                    } else if(secondArg.equals(CHECKPOINT)){

                        // Check for third argument
                        if(args.length > 2){
                            String thirdArg = Util.toLower(args[2]);
                            // Add a checkpoint
                            if(thirdArg.equals(ADD)){

                                // Check perms
                                if(Util.hasNoPerms(player, PKPERM_ADMIN_CHECKPOINT_ADD, MessageTypeEnum.ParkourMessage)){
                                    return true;
                                }

                                ParkourCourse course = ParkourManager.getCourse(firstArg);
                                int nextCheckpoint = course.getNextCheckpointNumber();
                                String output = ParkourManager.setPoint(firstArg,  player.getLocation());
                                player.sendMessage(output);


                                // Don't spam the player with message upon creating a checkpoint
                                ParkourPlayerInfo info = ParkourManager.getPlayerInfo(player, firstArg);
                                info.beginCheckpointMessageCoolDown(nextCheckpoint);
                                return true;

                            } else if(thirdArg.equals(REMOVE)){ // Remove a checkpoint

                                // Check perms
                                if(Util.hasNoPerms(player, PKPERM_ADMIN_CHECKPOINT_REMOVE, MessageTypeEnum.ParkourMessage)){
                                    return true;
                                }

                                // See if a point number was given.
                                if(args.length > 3){

                                    String pointStr = args[3];
                                    if(Util.isInteger(pointStr)){

                                        // Remove the point
                                        int pointInt = Integer.parseInt(pointStr);
                                        String output = ParkourManager.removePoint(firstArg, pointInt);
                                        player.sendMessage(output);
                                        return true;

                                    } else {
                                        MessageTypeEnum.ParkourMessage.sendMessage("A point number must be an integer.", player, true);
                                        return true;
                                    }

                                } else {
                                    MessageTypeEnum.ParkourMessage.sendMessage("A point number must be specified.", player, true);
                                    return true;
                                }
                            }
                        }

                    } else if(secondArg.equals(EDIT)){

                        // Check perms
                        if(Util.hasNoPerms(player, PKPERM_ADMIN_EDIT, MessageTypeEnum.ParkourMessage)){
                            return true;
                        }

                        // Open up the menu for this course
                        ParkourManager.openCourseMenu(firstArg, player);
                        return true;

                    }// TODO: Else, handle invalid second argument.

                } // TODO: Else, handle this..

            } else if(firstArg.equals(RESTART)) {

                // Check perms
                if(Util.hasNoPerms(player, PKPERM_PARKOUR_RESTART, MessageTypeEnum.ParkourMessage)){
                    return true;
                }

                // If they have started a course, TP them back to the starting location.
                ParkourPlayerInfo info = ParkourManager.getPlayerInfo(player, "");
                if(info.hasStartedAnyCourse()) {

                    // Get the start location of the course and teleport the player there.
                    ParkourCourse course = ParkourManager.getCourse(info.getCourseId());
                    Location startLoc = course.getStartLocation();
                    startLoc.setYaw(player.getLocation().getYaw());
                    player.teleport(startLoc);
                    MessageTypeEnum.ParkourMessage.sendMessage(String.format("You have been teleported back to the start of course '%s'!", info.getCourseId()), player, false);

                } else {
                    MessageTypeEnum.ParkourMessage.sendMessage("You haven't started a parkour course yet!", player, true);
                }
                return true;

            } else if (firstArg.equals(CHECKPOINT)){

                // Check perms
                if(Util.hasNoPerms(player, PKPERM_PARKOUR_CHECKPOINT, MessageTypeEnum.ParkourMessage)){
                    return true;
                }

                // Return a player to the last checkpoint they reached.

                // If they have started a course, TP them back to their last checkpoint location.
                ParkourPlayerInfo info = ParkourManager.getPlayerInfo(player, "");
                if(info.hasStartedAnyCourse()) {

                    // Make sure the player has reached a checkpoint
                    if(info.getCurrentCheckpoint() <= 0){
                        MessageTypeEnum.ParkourMessage.sendMessage("You haven't reached a checkpoint yet!", player, true);
                        return true;
                    }

                    // Get the start location of the course and teleport the player there.
                    ParkourCourse course = ParkourManager.getCourse(info.getCourseId());
                    Location checkpointLoc = course.getCheckpointLocation(info.getCurrentCheckpoint());
                    if(checkpointLoc == null){
                        MessageTypeEnum.ParkourMessage.sendMessage("You haven't reached a checkpoint yet!", player, true);
                        return true;
                    }

                    checkpointLoc.setYaw(player.getLocation().getYaw());
                    player.teleport(checkpointLoc);
                    MessageTypeEnum.ParkourMessage.sendMessage(String.format("Returned to checkpoint #%s!", info.getCurrentCheckpoint()), player, false);
                    info.beginCheckpointMessageCoolDown(info.getCurrentCheckpoint());

                } else {
                    MessageTypeEnum.ParkourMessage.sendMessage("You haven't started a parkour course yet!", player, true);
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

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        List<String> results = new ArrayList<String>();

        // Only fill results for a player
        if(commandSender instanceof Player){

            // Figure out what to return
            int argsLength = strings.length;
            switch(argsLength){
                case 1:
                    // First args
                    results.add(RESTART);
                    results.add(CHECKPOINT);
                    results.add(COURSEID);
                    break;

                case 2:
                    // Second args - only add these if restart and checkpoint wasn't selected
                    String firstArg = strings[0].toLowerCase();
                    if(!firstArg.equals(RESTART) && !firstArg.equals(CHECKPOINT)){
                        results.add(CREATE);
                        results.add(DELETE);
                        results.add(START);
                        results.add(END);
                        results.add(CREATE);
                        results.add(EDIT);
                        results.add(CHECKPOINT);
                    }

                    break;

                case 3:
                    // This arg is based on the previous arg
                    if(strings[1].toLowerCase().equals(CHECKPOINT)){
                        results.add(ADD);
                        results.add(REMOVE);
                    }
                    break;

                default:
                    break;
            }

        }

        return results;
    }

}
