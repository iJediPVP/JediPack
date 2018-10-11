package me.ijedi.jedipack.parkour;

import me.ijedi.jedipack.common.Util;
import me.ijedi.menulibrary.Menu;
import me.ijedi.menulibrary.MenuManager;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ParkourCommand implements CommandExecutor {

    private final String RESTART = "restart";
    private final String CHECKPOINT = "checkpoint";
    private final String[] FIRST_ARG_BLACKLIST = {RESTART, CHECKPOINT};

    private final String CREATE = "create";
    private final String DELETE = "delete";
    private final String START = "start";
    private final String END = "end";

    private final String ADD = "add";
    private final String REMOVE = "remove";
    private final String MOVE = "move";
    private final String EDIT = "edit";

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {

        /* Command layout
        / jppk <courseId> <command>

        // Add or delete a course
        /jppk 123 create    // create the course
        /jppk 123 delete    // remove the course

        // Modify a course
        /jppk 123 start // place the starting point of the course
        /jppk 123 end   // place the ending point of the course

        /jppk 123 1     // place the 'point 1' of the course
        /jppk 123 2     // place the 'point 2' of the course
        /jppk 123 1 remove // remove the 'point 1' from the course
        /jppk 123 1 replace // override existing 'point 1' with a new one

        /jppk 123 checkpoint add // add next checkpoint in this course
        /jppk 123 checkpoint remove 1 // remove checkpoint number 1 from the course
        /jppk 123 checkpoint move 1 // move checkpoint 1 to the specified location

        /jppk 123 edit // open the menu for editing the course

        // Interact with the course
        /jppk restart       // return to the start of the course
        /jppk checkpoint    // return to the last checkpoint

        * */


        // Sse if we have args
        if(args.length > 0 && !Util.IsNullOrEmpty(args[0])){
            String firstArg = Util.ToLower(args[0]);

            // Check for a course id. Anything except commands required for this plugin will be allowed.
            if(!ArrayUtils.contains(FIRST_ARG_BLACKLIST, firstArg)){
                // At this point "firstArg" is our course id. Make sure the course exists.
                if(!ParkourManager.doesCourseExist(firstArg)){
                    String message = ParkourManager.formatParkourString(String.format("Course '%s' does not exist!", firstArg), true);
                    commandSender.sendMessage(message);
                    return true;
                }

                // Check for our second argument.
                if(args.length > 1 && !Util.IsNullOrEmpty(args[1])){
                    String secondArg = Util.ToLower(args[1]);

                    // Handle create
                    if(secondArg.equals(CREATE)) {

                        String output = ParkourManager.createCourse(firstArg);
                        commandSender.sendMessage(output);
                        return true;

                    }else if(secondArg.equals(DELETE)){ // Handle delete

                        String output = ParkourManager.removeCourse(firstArg);
                        commandSender.sendMessage(output);
                        return true;

                    }else if(secondArg.equals(START)){ // Handle start

                        // Only allow this to be executed by a player
                        if(commandSender instanceof Player){

                            // Get the player's location and try to set the starting point for this course.
                            Player player = (Player)commandSender;
                            Location location =  player.getLocation();
                            String output = ParkourManager.setStart(firstArg, location);
                            commandSender.sendMessage(output);
                            return true;

                        }else{
                            commandSender.sendMessage(ParkourManager.formatParkourString("This command can only be executed by a player!", true));
                            return true;
                        }

                    }else if(secondArg.equals(END)) { // Handle end

                        // Only allow this to be executed by a player
                        if (commandSender instanceof Player) {

                            // Get the player's location and try to set the finishing point for this course.
                            Player player = (Player) commandSender;
                            Location location = player.getLocation();
                            String output = ParkourManager.setFinish(firstArg, location);
                            commandSender.sendMessage(output);
                            return true;

                        } else {
                            commandSender.sendMessage(ParkourManager.formatParkourString("This command can only be executed by a player!", true));
                            return true;
                        }

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

                                    // Only allow players to execute
                                    if(commandSender instanceof Player){
                                        Player player = (Player)commandSender;
                                        String output = ParkourManager.setPoint(firstArg,  player.getLocation());
                                        commandSender.sendMessage(output);
                                        return true;
                                    }
                                    else{
                                        commandSender.sendMessage(ParkourManager.formatParkourString("This command can only be executed by a player!", true));
                                        return true;
                                    }

                                } else if(thirdArg.equals(REMOVE)){ // Remove a checkpoint

                                    // Only allow players to execute
                                    if(commandSender instanceof Player){

                                        // See if a point number was given.
                                        if(args.length > 3){

                                            String pointStr = args[3];
                                            if(Util.IsInteger(pointStr)){

                                                // Remove the point
                                                int pointInt = Integer.parseInt(pointStr);
                                                String output = ParkourManager.removePoint(firstArg, pointInt);
                                                commandSender.sendMessage(output);
                                                return true;

                                            } else {
                                                commandSender.sendMessage(ParkourManager.formatParkourString("A point number must be an integer.", true));
                                                return true;
                                            }

                                        } else {
                                            commandSender.sendMessage(ParkourManager.formatParkourString("A point number must be specified.", true));
                                            return true;
                                        }
                                    }
                                    else{
                                        commandSender.sendMessage(ParkourManager.formatParkourString("This command can only be executed by a player!", true));
                                        return true;
                                    }

                                } else if(thirdArg.equals(MOVE)){ // Move a checkpoint

                                }
                            }

                        }

                    } else if(secondArg.equals(EDIT)){

                        // Only a player can run this
                        if(!(commandSender instanceof Player)) {
                            commandSender.sendMessage(ParkourManager.formatParkourString("This command can only be executed by a player!", true));
                            return true;
                        }

                        // Open up the menu for this course
                        Player player = (Player) commandSender;
                        ParkourManager.openCourseMenu(firstArg, player);
                        return true;

                    }// TODO: Else, handle invalid second argument.

                } // TODO: Else, handle this..

            } // TODO: Else, handle this...

        }else{
            // TODO: Do something better than this if we don't have arguments..
            return false;
        }

        return false;
    }
}
